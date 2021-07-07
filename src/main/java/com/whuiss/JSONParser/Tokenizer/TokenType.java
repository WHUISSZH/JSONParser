package com.whuiss.JSONParser.Tokenizer;

public enum TokenType {
    BEIGN_OBJECT(1),
    END_OBJECT(2),
    BEGIN_ARRAY(4),
    END_ARRAY(8),
    NULL(16),
    NUMBER(32),
    STRING(64),
    BOLLEAN(128),
    SEP_COLMMA(256),
    SEP_COMMA(512),
    END_DOCUMENT(1024);

    // 必须定义此属性，代表枚举量对应的整型值
    private int code;

    TokenType(int code) {
        this.code = code;
    }

    public int getTokenCode(){
        return code;
    }
}
