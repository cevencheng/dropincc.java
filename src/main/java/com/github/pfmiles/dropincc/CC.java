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
package com.github.pfmiles.dropincc;

import com.github.pfmiles.dropincc.impl.kleene.KleeneStarNode;

/**
 * 
 * This is a utility class that contains a handful of static methods or
 * constants to use.
 * 
 * @author pf-miles
 * 
 */
public class CC {
    private CC() {
    }

    /**
     * represents a 'blank' alternative of a grammar rule
     */
    public static final Element NOTHING = new Element() {
        private static final long serialVersionUID = -897759698260072002L;
    };

    /**
     * The kleene star syntactic suger
     * 
     * @param elements
     * @return
     */
    public static KleeneStarNode ks(Element... elements) {
        return new KleeneStarNode(elements);
    }
}