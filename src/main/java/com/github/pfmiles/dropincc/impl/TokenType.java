package com.github.pfmiles.dropincc.impl;

/**
 * @author pf-miles
 * 
 */
public class TokenType extends EleType {
    /**
     * Auto-added EOF token type constant
     */
    public static final TokenType EOF = new TokenType(-1, "EOF");
    /**
     * Auto-generated white space token type constant
     */
    public static final TokenType WHITESPACE = new TokenType(-2, "WHITESPACE");

    // just for print only
    private String name;

    /**
     * user defined token defIndex starts from 0, special built-in tokens'
     * defIndex is below 0
     * 
     * @param index
     */
    public TokenType(int index, String name) {
        super(index);
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public String toCodeGenStr() {
        if (this.defIndex >= 0)
            return "tt" + this.defIndex;
        return "tt_" + Math.abs(this.defIndex);
    }

}
