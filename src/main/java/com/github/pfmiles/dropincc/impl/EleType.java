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

/**
 * @author pf-miles
 * 
 */
public abstract class EleType {
    protected int defIndex;

    protected EleType(int defIndex) {
        this.defIndex = defIndex;
    }

    public int getDefIndex() {
        return defIndex;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + defIndex;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EleType other = (EleType) obj;
        if (defIndex != other.defIndex)
            return false;
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[").append(this.defIndex).append("]");
        return sb.toString();
    }
}
