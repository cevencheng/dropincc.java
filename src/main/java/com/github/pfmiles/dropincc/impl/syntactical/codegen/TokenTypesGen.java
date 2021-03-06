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
import java.util.Collection;

import com.github.pfmiles.dropincc.impl.TokenType;

/**
 * Be responsible for generate token type list in a parser.
 * 
 * @author pf-miles
 * 
 */
public class TokenTypesGen extends CodeGen {
    // [typeName, comment]
    private static final MessageFormat fmt = new MessageFormat("public TokenType {0};// {1}");
    private Collection<TokenType> types;

    public TokenTypesGen(Collection<TokenType> types) {
        this.types = types;
    }

    // TODO token type fields may be simpler, may use ints to represent
    // tokenTypes
    @SuppressWarnings("unchecked")
    public String render(CodeGenContext context) {
        StringBuilder sb = new StringBuilder();
        for (TokenType t : types) {
            String name = t.toCodeGenStr();
            context.fieldTokenTypeMapping.put(name, t);
            sb.append(fmt.format(new String[] { t.toCodeGenStr(), t.toString() })).append("\n");
        }
        return sb.toString();
    }

}
