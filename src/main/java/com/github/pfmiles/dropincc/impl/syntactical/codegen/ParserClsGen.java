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
package com.github.pfmiles.dropincc.impl.syntactical.codegen;

import java.text.MessageFormat;

import com.github.pfmiles.dropincc.impl.GruleType;

/**
 * Generates the whole parser class.
 * 
 * @author pf-miles
 * 
 */
public class ParserClsGen extends CodeGen {

    private static final MessageFormat fmt = getTemplate("parserCls.dt", ParserClsGen.class);

    private String parserClsName;// {0}
    private TokenTypesGen tokenTypes; // {1}
    private AltsActionsGen actions;// {2}
    private PredsGen preds;// {3}
    private RuleDfasGen ruleAltsPredictingDfa; // {4}
    private KleeneDfasGen kleenePreds;// {5}
    private GruleType startRule;// {6}
    private RuleMethodsGen ruleMethods;// {7}

    /**
     * Construct a parserCls using all the components.
     * 
     * @param parserClsName
     * @param tokenTypes
     * @param actions
     * @param preds
     * @param startRule
     * @param ruleMethods
     * @param ruleAltsPredictingMethods
     * @param kleenePredictingMethods
     */
    public ParserClsGen(String parserClsName, TokenTypesGen tokenTypes, AltsActionsGen actions, PredsGen preds, RuleDfasGen ruleAltsPredictingDfa,
            KleeneDfasGen kleenePreds, GruleType startRule, RuleMethodsGen ruleMethods) {
        super();
        this.parserClsName = parserClsName;
        this.tokenTypes = tokenTypes;
        this.actions = actions;
        this.preds = preds;
        this.ruleAltsPredictingDfa = ruleAltsPredictingDfa;
        this.kleenePreds = kleenePreds;
        this.startRule = startRule;
        this.ruleMethods = ruleMethods;
    }

    @SuppressWarnings("unchecked")
    public String render(CodeGenContext context) {
        return fmt.format(new String[] { this.parserClsName, this.tokenTypes.render(context), this.actions.render(context), this.preds.render(context),
                this.ruleAltsPredictingDfa.render(context), this.kleenePreds.render(context), this.startRule.toCodeGenStr(), this.ruleMethods.render(context) });
    }
}
