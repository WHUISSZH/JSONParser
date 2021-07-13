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
//        System.out.println(tokens);
        if (token == null) {
            return new JsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_OBJECT) {
            return parseJsonObject();
        } else if (token.getTokenType() == TokenType.BEGIN_ARRAY) {
            return parseJsonArray();
        } else {
            throw new JsonParseException("Parse error, invalid Token.");
        }
    }

    /**
     * @return 解析后的json结果，也是最后的输出结果
     */
    private JsonObject parseJsonObject() {
        JsonObject jsonObject = new JsonObject();
        int exceptToken = STRING_TOKEN | END_OBJECT_TOKEN; // 保留了两个 token code 的信息; 给这样的初始化值是为了处理嵌套json
        String key = null;
        Object value;
        while (tokens.hasNext()) {
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            switch (tokenType) {
                case BEGIN_OBJECT:
                    checkException(tokenType, exceptToken);
                    jsonObject.put(key, parseJsonObject()); // 递归解析
                    exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN; // 递归处理内部的一个json对象后，下一个token肯定是分割内部json对象的逗号或者直接结束
                    break;
                case END_OBJECT:
                    checkException(tokenType, exceptToken);
                    return jsonObject;
                case BEGIN_ARRAY:
                    checkException(tokenType, exceptToken);
                    jsonObject.put(key, parseJsonArray());
                    exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case NULL:
                    checkException(tokenType, exceptToken);
                    jsonObject.put(key, null);
                    exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case NUMBER:
                    checkException(tokenType, exceptToken);
                    if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
                        jsonObject.put(key, Double.valueOf(tokenValue));
                    } else {
                        Long num = Long.valueOf(tokenValue);
                        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                            jsonObject.put(key, num);
                        } else {
                            jsonObject.put(key, num.intValue());
                        }
                    }
                    exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case BOOLEAN:
                    checkException(tokenType, exceptToken);
                    jsonObject.put(key, Boolean.valueOf(token.getValue()));
                    exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN;
                    break;
                case STRING:
                    checkException(tokenType, exceptToken);
                    Token preToken = tokens.peekPre();
                    /*
                     * 在 JSON 中，字符串既可以作为键，也可作为值。
                     * 作为键时，只期待下一个 Token 类型为 SEP_COLON。
                     * 作为值时，期待下一个 Token 类型为 SEP_COMMA 或 END_OBJECT
                     */
                    if (preToken.getTokenType() == TokenType.SEP_COLON) {
                        value = token.getValue();
                        jsonObject.put(key, value);
                        exceptToken = SEP_COMMA_TOKEN | END_OBJECT_TOKEN;
                    } else {
                        key = token.getValue();
                        exceptToken = SEP_COLON_TOKEN;
                    }
                    break;
                case SEP_COLON:
                    checkException(tokenType, exceptToken);
                    exceptToken = NULL_TOKEN | NUMBER_TOKEN | BOOLEAN_TOKEN | STRING_TOKEN
                            | BEGIN_OBJECT_TOKEN | BEGIN_ARRAY_TOKEN;
                    break;
                case SEP_COMMA:
                    checkException(tokenType, exceptToken);
                    exceptToken = STRING_TOKEN;
                    break;
                case END_DOCUMENT:
                    checkException(tokenType, exceptToken);
                    return jsonObject;
                default:
                    throw new JsonParseException("Unexpected Token.");
            }
            System.out.println(jsonObject);
        }

        throw new JsonParseException("Parse error, invalid Token.");
    }


    private JsonArray parseJsonArray() {
        int exceptToken = BEGIN_ARRAY_TOKEN | END_ARRAY_TOKEN | BEGIN_OBJECT_TOKEN | NULL_TOKEN
                | NUMBER_TOKEN | BOOLEAN_TOKEN | STRING_TOKEN;
        JsonArray jsonArray = new JsonArray();
        while (tokens.hasNext()) {
            Token token = tokens.next();
            TokenType tokenType = token.getTokenType();
            String tokenValue = token.getValue();
            switch (tokenType) {
                case BEGIN_OBJECT:
                    checkException(tokenType, exceptToken);
                    jsonArray.add(parseJsonObject());
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case BEGIN_ARRAY:
                    checkException(tokenType, exceptToken);
                    jsonArray.add(parseJsonArray());
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case END_ARRAY:
                    checkException(tokenType, exceptToken);
                    return jsonArray;
                case NULL:
                    checkException(tokenType, exceptToken);
                    jsonArray.add(null);
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case NUMBER:
                    checkException(tokenType, exceptToken);
                    if (tokenValue.contains(".") || tokenValue.contains("e") || tokenValue.contains("E")) {
                        jsonArray.add(Double.valueOf(tokenValue));
                    } else {
                        Long num = Long.valueOf(tokenValue);
                        if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
                            jsonArray.add(num);
                        } else {
                            jsonArray.add(num.intValue());
                        }
                    }
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case BOOLEAN:
                    checkException(tokenType, exceptToken);
                    jsonArray.add(Boolean.valueOf(tokenValue));
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case STRING:
                    checkException(tokenType, exceptToken);
                    jsonArray.add(tokenValue);
                    exceptToken = SEP_COMMA_TOKEN | END_ARRAY_TOKEN;
                    break;
                case SEP_COMMA:
                    checkException(tokenType, exceptToken);
                    exceptToken = STRING_TOKEN | NULL_TOKEN | NUMBER_TOKEN | BOOLEAN_TOKEN
                            | BEGIN_ARRAY_TOKEN | BEGIN_OBJECT_TOKEN;
                    break;
                case END_DOCUMENT:
                    checkException(tokenType, exceptToken);
                    return jsonArray;
                default:
                    throw new JsonParseException("Unexpected Token.");
            }
        }
        throw new JsonParseException("Parse error, invalid Token.");
    }

    private void checkException(TokenType tokenType, int exceptToken) {
        if ((tokenType.getTokenCode() & exceptToken) == 0) { // token 对应的 code的二进制数在 每个位上互不重叠。
            throw new JsonParseException("Parse error, invalid Token");
        }
    }
}
