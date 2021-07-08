package com.whuiss.JSONParser.tokenizer;

import com.whuiss.JSONParser.jsonParseException.JsonParseException;

import java.io.IOException;

import static com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer.isWhiteSpace;
import static java.lang.Character.isDigit;

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
            case '-':
                return readNumber();
        }

        if (isDigit(ch)){
            return readNumber();
        }

        throw new JsonParseException("Illegal character"); //没有在前面几句中返回结果，证明这个json对象有错
    }

    private Token readBoolean() throws IOException {
        if (charReader.getPosElemet() == 't') {
            if (!(charReader.next() == 'r') && charReader.next() == 'u' && charReader.next() == 'e') {
                throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN, "true");
        } else {
            if (!(charReader.next() == 'a') && charReader.next() == 'l'
                    && charReader.next() == 's' && charReader.next() == 'e') {
                throw new JsonParseException("Invalid json string");
            }
            return new Token(TokenType.BOOLEAN, "false");
        }
    }

    private Token readString() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (; ; ) {
            char next = charReader.next();
            if (next == '\\') {
                if (!isEscape()) {
                    throw new JsonParseException("Invalid escape character");
                }
                stringBuilder.append('\\'); // 将特殊字符前的转义字符 \ 加入
                next = charReader.getPosElemet();
                stringBuilder.append(next);
                // 判断 \u four-hex-digits 类型
                if (next == 'u') {
                    for (int i = 0; i < 4; i++) {
                        next = charReader.next();
                        if (isHex(next)) {
                            stringBuilder.append(next);
                        } else {
                            throw new JsonParseException("Invalid character");
                        }
                    }
                }
            } else if (next == '"') { // 这里是json 包裹 字符串的后引号，字符串内部的引号已经在特殊字符那里处理完毕了
                return new Token(TokenType.STRING, stringBuilder.toString());
            } else if (next == '\r' || next == '\n') { // json 值里面不能有换行
                throw new JsonParseException("Invalid character");
            } else {
                stringBuilder.append(next);
            }
        }
    }

    private Token readNumber() throws IOException {
        char ch = charReader.getPosElemet();
        StringBuilder sb = new StringBuilder();
        if (ch == '-') { // 处理负数
            sb.append(ch);
            ch = charReader.next();
            if (ch == '0') { // 处理 0.XXXX
                sb.append(ch);
                sb.append(readFracAndExp());
            }else if (isDigit(ch)){
                do {
                    sb.append(ch);
                    ch = charReader.next();
                }while (isDigit(ch));
                if (ch != (char)-1){
                    charReader.back();
                    sb.append(readFracAndExp());
                }
            }else {
                throw new JsonParseException("Invaild minus number");
            }
        }else if (ch == '0'){ // 处理小数
            sb.append(ch);
            sb.append(readFracAndExp());
        }else {
            do {
                sb.append(ch);
                ch = charReader.next();
            }while (isDigit(ch));
                if (ch != (char)-1){
                    charReader.back();
                    sb.append(readFracAndExp());
                }
        }
        return new Token(TokenType.NUMBER, sb.toString());
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

    /**
     *
     * @param ch
     * @return 判断是不是十六进制的 OX0000 - OXFFFF
     */
    private boolean isHex(char ch) {
        return ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'));
    }


    /**
     *
     * @return 读取小数点后部分 和 科学计数法
     * @throws IOException
     */
    private String readFracAndExp() throws IOException {
        StringBuilder sb = new StringBuilder();
        char ch = charReader.next();
        if (ch == '.') { // 读取小数
            sb.append(ch);
            ch = charReader.next();
            if (!isDigit(ch)) {
                throw new JsonParseException("Invalid fraction");
            }
            do {
                sb.append(ch);
                ch = charReader.next();
            } while (isDigit(ch));
            // 如果是科学计数法，这里将处理 E+09 这部分
//                 * 1.03乘10的9次方，可简写为“1.03E+09”的形式
//                    * -1.03乘10的9次方，可简写为“-1.03E+09”的形式
//                    * 1.03乘10的-9次方，可简写为“1.03E-09”的形式
//                    * -1.03乘10的-9次方，可简写为“-1.03E-09”的形式
            if (isExp(ch)) {
                sb.append(ch);
                sb.append(readExp()); // 读取 e 后面的 +09
            }else {
                if (ch != (char)-1){
                    charReader.back();
                }
            }
        }else if (isExp(ch)){ // 0.
            sb.append(ch);
            sb.append(readExp());
        }else {
            charReader.back();
        }
        return sb.toString();
    }

    /**
     * 1.03乘10的9次方，可简写为“1.03E+09”的形式
     * -1.03乘10的9次方，可简写为“-1.03E+09”的形式
     * 1.03乘10的-9次方，可简写为“1.03E-09”的形式
     * -1.03乘10的-9次方，可简写为“-1.03E-09”的形式
     * 读取读取 e 后面的 +09
     * @return
     * @throws IOException
     */
    private String readExp() throws IOException {
        StringBuilder sb = new StringBuilder();
        char ch = charReader.next();
        if (ch == '+' || ch == '-') {
            sb.append(ch);
            ch = charReader.next();
            if (isDigit(ch)) {
                do {
                    sb.append(ch);
                    ch = charReader.next();
                } while (isDigit(ch));
 // "salay": “1.03E+09” 读取完 9 后，pos 变量已经到了 “ 位置，再进行switch判断时，
                if (ch != (char) -1) {
                    charReader.back();
                }
            } else {
                throw new JsonParseException("Invaild nagx");
            }
        } else {
            throw new JsonParseException("Invaild nagx");
        }
        return sb.toString();
    }


    /**
     * 判断是不是科学计数法中的  e E
     * @param ch
     * @return
     */
    private boolean isExp(char ch) {
        return ch == 'e' || ch == 'E';
    }
}
