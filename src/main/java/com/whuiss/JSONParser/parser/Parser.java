package com.whuiss.JSONParser.parser;

import com.whuiss.JSONParser.jsonParseException.JsonParseException;
import com.whuiss.JSONParser.model.JsonArray;
import com.whuiss.JSONParser.model.JsonObject;
import com.whuiss.JSONParser.tokenizer.Token;
import com.whuiss.JSONParser.tokenizer.TokenList;
import com.whuiss.JSONParser.tokenizer.TokenType;

/**
 * @author ：HuiZhang
 * @date ：Created in 2021/7/8 21:53
 * @description：
 * @modified By：
 * @version:
 */
public class Parser {
    private static final int BEGIN_OBJECT_TOKEN = 1;
    private static final int END_OBJECT_TOKEN = 2;
    private static final int BEGIN_ARRAY_TOKEN = 4;
    private static final int END_ARRAY_TOKEN = 8;
    private static final int NULL_TOKEN = 16;
    private static final int NUMBER_TOKEN = 32;
    private static final int STRING_TOKEN = 64;
    private static final int BOOLEAN_TOKEN = 128;
    private static final int SEP_COLON_TOKEN = 256;
    private static final int SEP_COMMA_TOKEN = 512;

    private TokenList tokens;

    public Object parse(TokenList tokens) {
        this.tokens = tokens;
        return parse();
    }

    private Object parse() {
        Token token = tokens.next();
        if (token == null) {
            return new JsonObject();
        } else if (token.getTokenType() == TokenType.BEIGN_OBJECT){
            return parseJsonObject();
        }else if (token.getTokenType() == TokenType.BEGIN_ARRAY){
            return parseJsonArray();
        }else {
            throw new JsonParseException("Parse error, invalid Token.");
        }
    }

    private JsonObject parseJsonObject(){

    }

    private JsonArray parseJsonArray(){

    }
}
