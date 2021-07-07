package com.whuiss.JSONParser.tokenizer;

import com.whuiss.JSONParser.jsonParseException.JsonParseException;

import java.io.IOException;

import static com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer.isWhiteSpace;

/**
 * @author ：HuiZhang
 * @date ：Created in 2021/7/7 21:12
 * @description：
 * @modified By：
 * @version:
 */
public class Tokenizer {
    private TokenList tokens;
    private CharReader charReader;

    public TokenList tokenize(CharReader charReader) throws IOException {
        this.charReader = charReader;
        tokens = new TokenList();
        tokenize();

        return tokens;
    }

    private void tokenize() throws IOException {
        // do-while 读取文件
        Token token;
        do {
            token = start();
            tokens.add(token);
        } while (token.getTokenType() != TokenType.END_DOCUMENT);
    }

    private Token start() throws IOException {
        char ch;
        for (; ; ) {
            if (!charReader.hasNext()) {
                return new Token(TokenType.END_DOCUMENT, null);
            }

            ch = charReader.next();
            if (!isWhiteSpace(ch)) {
                break;
            }
        }

        switch (ch) {
            case '{':
                return new Token(TokenType.BEIGN_OBJECT, String.valueOf(ch));
            case '}':
                return new Token(TokenType.END_OBJECT, String.valueOf(ch));
            case '[':
                return new Token(TokenType.BEGIN_ARRAY, String.valueOf(ch));
            case ']':
                return new Token(TokenType.END_ARRAY, String.valueOf(ch));
            case ',':
                return new Token(TokenType.SEP_COMMA, String.valueOf(ch));
            case ':':
                return new Token(TokenType.SEP_COLON, String.valueOf(ch));
            case 't':
            case 'f':
                return readBoolean();
            case '"':
                return readString();
        }
    }

    private Token readBoolean() throws IOException {
        if (charReader.getPosElemet() == 't'){
            if (!(charReader.next() == 'r') && charReader.next() == 'u' && charReader.next() == 'e'){
                    throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN, "true");
        }else {
            if (!(charReader.next() == 'a') && charReader.next() == 'l'
                    && charReader.next() == 's'&& charReader.next() == 'e'){
                throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN,"false");
        }
    }

    private Token readString() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (;;){
            char next = charReader.next();
            if (next == '\\'){
                if (!isEscape()){
                    throw new JsonParseException("Invalid escape character");
                }
                stringBuilder.append('\\'); // 将特殊字符前的转义字符 \ 加入
                next = charReader.getPosElemet();
                stringBuilder.append(next);
                // 判断 \u four-hex-digits 类型
                if (next == 'u'){
                    for (int i = 0; i < 4; i++) {
                        next = charReader.next();
                        if (isHex(next)){
                            stringBuilder.append(next);
                        }else {
                            throw new JsonParseException("Invalid character");
                        }
                    }
                }
            }
            else if (next == '"'){ // 这里是json 包裹 字符串的后引号，字符串内部的引号已经在特殊字符那里处理完毕了
                return new Token(TokenType.STRING,stringBuilder.toString());
            }else if (next == '\r' || next == '\n'){ // json 值里面不能有换行
                throw new JsonParseException("Invalid character");
            }else {
                stringBuilder.append(next);
            }
        }
    }

    /**
     * @return 判断是否是特殊类型字符
     * @throws IOException
     */
    private boolean isEscape() throws IOException {
        char ch = charReader.next();
        return (ch == '"' || ch == '\\' || ch == 'u' || ch == 'r'
                || ch == 'n' || ch == 'b' || ch == 't' || ch == 'f');
    }

    private boolean isHex(char ch){
        return ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'));
    }
}
