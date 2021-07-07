package com.whuiss.JSONParser.tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：HuiZhang
 * @date ：Created in 2021/7/7 21:13
 * @description：
 * @modified By：
 * @version:
 */
public class TokenList {
    private List<Token> tokens = new ArrayList<Token>();

    private int pos = 0;

    public void add(Token token) {
        tokens.add(token);
    }

    public Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }

    public Token peekPre() {
        return pos - 1 < tokens.size() ? tokens.get(pos - 2) : null;
    }

    public Token next() {
        return tokens.get(pos++);
    }

    public boolean hasNext(){
        return pos < tokens.size();
    }

    @Override
    public String toString() {
        return "TokenList{" +
                "tokens=" + tokens +
                ", pos=" + pos +
                '}';
    }
}
