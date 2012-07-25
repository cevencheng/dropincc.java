package com.github.pfmiles.dropincc.impl.runtime.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.pfmiles.dropincc.impl.TokenType;
import com.github.pfmiles.dropincc.impl.runtime.Token;

/**
 * Common super class for all generated lexer.
 * 
 * @author pf-miles
 * 
 */
public abstract class Lexer implements Enumeration<Token> {

    private List<Token> lookAheadBuf = new ArrayList<Token>();

    /**
     * check if this lexer has more token to get
     */
    public abstract boolean hasMoreElements();

    /**
     * get next token(may get from buffer)
     */
    public abstract Token nextElement();

    /**
     * the real get next token logic according to the underlying lexer
     * implementation.
     * 
     * @return
     */
    protected abstract Token realNext();

    /**
     * Return the current lexing position.
     * 
     * @return
     */
    public abstract int getCurrentPosition();

    /**
     * Return the next 2 tokens' string representation, used as context
     * infomation in reporting an error.
     * 
     * @return
     */
    public abstract String getAheadTokensRepr();

    /**
     * look ahead token type
     * 
     * @param i
     *            lookahead count
     * @return
     */
    public TokenType LA(int i) {
        Token t = this.LT(i);
        return t != null ? t.getType() : null;
    }

    private Token LT(int i) {
        if (i < this.lookAheadBuf.size())
            return this.lookAheadBuf.get(i - 1);
        int lng = i - this.lookAheadBuf.size();
        for (int j = 0; j < lng; j++) {
            this.lookAheadBuf.add(this.realNext());
        }
        return this.lookAheadBuf.get(i - 1);
    }

}