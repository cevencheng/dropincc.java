/*******************************************************************************
 * Copyright (c) 2012 pf_miles.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     pf_miles - initial API and implementation
 ******************************************************************************/
package com.github.pfmiles.dropincc.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.pfmiles.dropincc.CC;
import com.github.pfmiles.dropincc.DropinccException;
import com.github.pfmiles.dropincc.Element;
import com.github.pfmiles.dropincc.Grule;
import com.github.pfmiles.dropincc.TokenDef;
import com.github.pfmiles.dropincc.impl.hotcompile.CompilationResult;
import com.github.pfmiles.dropincc.impl.hotcompile.HotCompileUtil;
import com.github.pfmiles.dropincc.impl.kleene.AbstractKleeneNode;
import com.github.pfmiles.dropincc.impl.kleene.KleeneCompiler;
import com.github.pfmiles.dropincc.impl.kleene.KleeneType;
import com.github.pfmiles.dropincc.impl.lexical.LexerCompiler;
import com.github.pfmiles.dropincc.impl.llstar.GenedKleeneGruleType;
import com.github.pfmiles.dropincc.impl.llstar.PredictingGrule;
import com.github.pfmiles.dropincc.impl.llstar.PredictingKleene;
import com.github.pfmiles.dropincc.impl.runtime.impl.Lexer;
import com.github.pfmiles.dropincc.impl.runtime.impl.LexerPrototype;
import com.github.pfmiles.dropincc.impl.runtime.impl.Parser;
import com.github.pfmiles.dropincc.impl.runtime.impl.ParserPrototype;
import com.github.pfmiles.dropincc.impl.runtime.impl.PreWrittenStringLexerPrototype;
import com.github.pfmiles.dropincc.impl.runtime.impl.StatelessParserPrototype;
import com.github.pfmiles.dropincc.impl.syntactical.GenedGruleType;
import com.github.pfmiles.dropincc.impl.syntactical.ParserCompiler;
import com.github.pfmiles.dropincc.impl.syntactical.PredictingResult;
import com.github.pfmiles.dropincc.impl.syntactical.codegen.ParserCodeGenResult;
import com.github.pfmiles.dropincc.impl.util.Pair;

/**
 * 
 * A analyzed language structure
 * 
 * @author pf-miles
 * 
 */
public class AnalyzedLang {

    private String langName;
    private List<TokenDef> tokens;
    private List<Grule> grules;
    private GruleType startRuleType;
    private Map<TokenDef, TokenType> tokenTypeMapping;
    // grule -> gruleType mapping, inited when AnalyzedLang obj creating,
    // completed after sub-rule rewriting
    private Map<Grule, GruleType> gruleTypeMapping;
    private static final Map<Element, SpecialType> specialTypeMapping = new HashMap<Element, SpecialType>();
    static {
        // special type 1, 'nothing' represents a empty alternative.
        specialTypeMapping.put(CC.NOTHING, SpecialType.NOTHING);
    }
    // token group num -> token type
    private Map<Integer, TokenType> groupNumToType;
    // the token mathcing pattern
    private Pattern tokenPatterns;

    private boolean whitespaceSensitive;

    // Grammar rule type -> alternatives with predicts, analysis & generated
    // from 'gruleTypeMapping' after sub-rule rewriting
    private Map<GruleType, List<CAlternative>> ruleTypeToAlts;

    // kleeneNode -> kleeneNodeType mapping, inited after building
    // 'gruleTypeMapping' immidiatelly
    private Map<AbstractKleeneNode, KleeneType> kleeneTypeMapping;

    // kleene node Type -> match sequence mapping, built while
    // 'AnalyzedLang' compiling(resolveParserAst). For later analysis & code gen
    private Map<KleeneType, List<EleType>> kleeneTypeToNode;

    // grules along with look-ahead dfas, about to generate code
    private List<PredictingGrule> predGrules;
    // kleenes along with its look-ahead dfas
    private List<PredictingKleene> predKleenes;

    // the compiled lexer prototype
    private LexerPrototype lexerPrototype;
    // the generated parser code(in pure java)
    private String parserCode;
    // the compiled parser prototype
    private ParserPrototype parserPrototype;

    private String debugMsgs;
    private String warnings;

    public AnalyzedLang(String name, List<TokenDef> tokens, List<Grule> grules, boolean whitespaceSensitive) {
        this.langName = name;
        // build token -> tokenType mapping
        this.tokens = tokens;
        // Gathering instant tokenDefs...
        this.tokens.addAll(LexerCompiler.collectInstantTokenDefs(grules));

        this.whitespaceSensitive = whitespaceSensitive;

        this.tokenTypeMapping = LexerCompiler.buildTokenTypeMapping(tokens, whitespaceSensitive);

        this.grules = grules;
        // rewrite sub-rules
        List<Grule> genGrules = ParserCompiler.rewriteSubRules(this.grules);
        // build grule -> gruleType mapping for all grules(including generated
        // ones) in 'gruleTypeMapping'
        this.gruleTypeMapping = ParserCompiler.buildGruleTypeMapping(this.grules, genGrules);

        // traverse and register kleene nodes
        this.kleeneTypeMapping = KleeneCompiler.buildKleeneTypeMapping(this.gruleTypeMapping);
    }

    public void compile() {
        // 1.check & compile token rules
        Pair<Map<Integer, TokenType>, Pattern> compiledTokenUnit = LexerCompiler.checkAndCompileTokenRules(this.tokens, this.tokenTypeMapping);
        this.groupNumToType = compiledTokenUnit.getLeft();
        this.tokenPatterns = compiledTokenUnit.getRight();

        // 2.resolving the parser ast
        TypeMappingParam typeMappingParam = new TypeMappingParam(this.tokenTypeMapping, this.gruleTypeMapping, specialTypeMapping, this.kleeneTypeMapping);
        // at this point, 'gruleTypeMapping' contains all grule -> type
        // mappings, including generated grules
        this.ruleTypeToAlts = ParserCompiler.buildRuleTypeToAlts(typeMappingParam);

        // at this time, 'kleeneTypeMapping' should contain all KleeneNode ->
        // KleeneType mapping (built when traverse and register kleene nodes)
        this.kleeneTypeToNode = KleeneCompiler.buildKleeneTypeToNode(typeMappingParam);

        // resolve start grule
        this.startRuleType = resolveStartGruleType(ruleTypeToAlts, kleeneTypeToNode);

        // 3.check or simplify & compute grammar rules
        // detect and report left-recursion, LL parsing needed
        ParserCompiler.checkAndReportLeftRecursions(this.ruleTypeToAlts, this.kleeneTypeToNode);

        // 4.compute predicts, LL(*), detect and report rule conflicts
        PredictingResult predResults = ParserCompiler.computePredictingGrules(this.ruleTypeToAlts, this.kleeneTypeToNode);
        this.predGrules = predResults.getPgs();
        this.predKleenes = predResults.getPks();
        this.debugMsgs = predResults.getDebugMsgs();
        this.warnings = predResults.getWarnings();

        // 5.lexer code gen(TODO using pre-written template code currently,
        // should support stream tokenizing in the future)
        this.lexerPrototype = new PreWrittenStringLexerPrototype(this.groupNumToType, this.tokenPatterns, this.whitespaceSensitive);

        // 6.parser code gen
        ParserCodeGenResult parserCodeGenResult = ParserCompiler.genParserCode(this.langName, this.startRuleType, this.predGrules, this.predKleenes,
                tokenTypeMapping.values(), this.kleeneTypeToNode);
        this.parserCode = parserCodeGenResult.getCode();

        // 7.compile and maintain the code in a separate classloader
        CompilationResult result = HotCompileUtil.compile("com.github.pfmiles.dropincc.impl.runtime.gen." + this.langName, this.parserCode);
        if (!result.isSucceed()) {
            throw new DropinccException("Parser code compilation failed. Reason: " + result.getErrMsg());
        }
        this.parserPrototype = new StatelessParserPrototype(result.<Parser> getCls(), parserCodeGenResult);
    }

    // a grammar rule is the start rule if no other rules invokes it
    private GruleType resolveStartGruleType(Map<GruleType, List<CAlternative>> ruleTypeToAlts, Map<KleeneType, List<EleType>> kleeneTypeToNode) {
        Set<GruleType> allGs = new HashSet<GruleType>();
        for (GruleType t : ruleTypeToAlts.keySet()) {
            if (!(t instanceof GenedGruleType) && !(t instanceof GenedKleeneGruleType))
                allGs.add(t);
        }
        Set<GruleType> enteredGrule = new HashSet<GruleType>();
        for (Map.Entry<GruleType, List<CAlternative>> e : ruleTypeToAlts.entrySet()) {
            GruleType g = e.getKey();
            List<CAlternative> alts = e.getValue();
            if (!enteredGrule.contains(g)) {
                enteredGrule.add(g);
                for (CAlternative alt : alts) {
                    filterOutInvokedGrule(alt.getMatchSequence(), allGs, ruleTypeToAlts, kleeneTypeToNode, enteredGrule);
                }
            }
        }
        if (allGs.isEmpty())
            throw new DropinccException("No start rule found, please check your grammar rules.");
        if (allGs.size() > 1)
            throw new DropinccException(
                    "More than one suspected start rule found in the grammar, dangling rules may exist, please check your grammar rules. These rules are not invoked by others: "
                            + allGs);
        return allGs.iterator().next();
    }

    private void filterOutInvokedGrule(List<EleType> matchSequence, Set<GruleType> allGs, Map<GruleType, List<CAlternative>> ruleTypeToAlts,
            Map<KleeneType, List<EleType>> kleeneTypeToNode, Set<GruleType> enteredGrule) {
        for (EleType ele : matchSequence) {
            if (ele instanceof TokenType) {
                continue;
            } else if (ele instanceof GruleType) {
                allGs.remove((GruleType) ele);
                if (!enteredGrule.contains((GruleType) ele)) {
                    enteredGrule.add((GruleType) ele);
                    for (CAlternative alt : ruleTypeToAlts.get((GruleType) ele)) {
                        filterOutInvokedGrule(alt.getMatchSequence(), allGs, ruleTypeToAlts, kleeneTypeToNode, enteredGrule);
                    }
                }
            } else if (ele instanceof KleeneType) {
                filterOutInvokedGrule(kleeneTypeToNode.get((KleeneType) ele), allGs, ruleTypeToAlts, kleeneTypeToNode, enteredGrule);
            } else {
                throw new DropinccException("Unhandled element type when finding start rule: " + ele);
            }
        }
    }

    /**
     * Create a new instance of the constructing language's lexer.
     * 
     * @return
     */
    public Lexer newLexer(String code) {
        return this.lexerPrototype.create(code);
    }

    /**
     * Create a new parser instance
     * 
     * @param lexer
     * @return
     */
    public Parser newParser(Lexer lexer) {
        return this.parserPrototype.create(lexer);
    }

    public Map<TokenDef, TokenType> getTokenTypeMapping() {
        return tokenTypeMapping;
    }

    public Map<Grule, GruleType> getGruleTypeMapping() {
        return gruleTypeMapping;
    }

    public Map<KleeneType, List<EleType>> getKleeneTypeToNode() {
        return kleeneTypeToNode;
    }

    public Map<Integer, TokenType> getGroupNumToType() {
        return groupNumToType;
    }

    public Map<GruleType, List<CAlternative>> getRuleTypeToAlts() {
        return ruleTypeToAlts;
    }

    public Pattern getTokenPatterns() {
        return tokenPatterns;
    }

    public String getDebugMsgs() {
        return debugMsgs;
    }

    public String getWarnings() {
        return warnings;
    }

}
