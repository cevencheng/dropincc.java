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
package com.github.pfmiles.dropincc.template.impl;

import com.github.pfmiles.dropincc.template.impl.ast.TemplateNode;
import com.github.pfmiles.dropincc.template.impl.visitor.DropinTemplateToJavaVisitor;

/**
 * Compile template string to java source string.
 * 
 * @author pf-miles
 * 
 */
public class DropinTemplateCompiler {

    /**
     * Compile template string to java source string.
     * 
     * @param tempStr
     * @return
     */
    public static String compile(String tempStr) {
        TemplateNode temp = new DropinTemplateParser(new DropinTemplateLexer(tempStr)).parse();
        return temp.accept(new DropinTemplateToJavaVisitor(), null);
    }

}
