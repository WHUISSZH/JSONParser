package com.whuiss.JSONParser;

import com.whuiss.JSONParser.parser.Parser;
import com.whuiss.JSONParser.tokenizer.CharReader;
import com.whuiss.JSONParser.tokenizer.TokenList;
import com.whuiss.JSONParser.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.StringReader;

public class JSONParser {
    private Tokenizer tokenizer = new Tokenizer();

    private Parser parser = new Parser();

    public Object fromJSON(String json) throws IOException {
        CharReader charReader = new CharReader(new StringReader(json));
        TokenList tokens = tokenizer.tokenize(charReader);

        return parser.parse(tokens);
    }
}
