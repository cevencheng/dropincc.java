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
package com.github.pfmiles.dropincc.template.impl.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.pfmiles.dropincc.template.impl.DtNode;
import com.github.pfmiles.dropincc.template.impl.DtVisitor;

/**
 * The root node of the compiling dropin template AST hierarchy.
 * 
 * @author pf-miles
 * 
 */
public class TemplateNode implements DtNode<Object> {

    private List<RenderableNode<?>> renderables = new ArrayList<RenderableNode<?>>();

    public <T> T accept(DtVisitor visitor, Object param) {
        return visitor.visitTemplate(this, param);
    }

}
