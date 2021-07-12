package com.whuiss.JSONParser.tokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * @author ：HuiZhang
 * @date ：Created in 2021/7/7 19:55
 * @description：
 * @modified By：
 * @version:
 */
public class CharReader {
    private static final int BUFFER_SIZE = 1024;
    private Reader reader;
    private char[] buffer;
    private int pos; // 读取地址，从 0 开始
    private int size; // 读取的字符串长度

    public CharReader(Reader reader) {
        this.reader = reader;
        this.buffer = new char[BUFFER_SIZE];
    }

    /**
     * 返回 pos 下标处的字符
     *
     * @return
     */
    public char getPosElemet() {
        if (pos - 1 >= size) {
            return (char) -1;
        }
        return buffer[Math.max(0, pos - 1)];
    }

    /**
     * @return 返回 pos 下标处的字符，并将 pos + 1
     * @throws IOException
     */
    public char next() throws IOException {
        if (!hasNext()) {
            return (char) -1;
        }
        return buffer[pos++];
    }

    public boolean hasNext() throws IOException {
        if (pos < size) {
            return true;
        }
        fillBuffer();
        return pos < size;
    }

    private void fillBuffer() throws IOException {
        int n = reader.read(buffer);
        if (n == -1) {
            return;
        }

        pos = 0;
        size = n;
    }

    // 回退
    public void back() {
        pos = Math.max(0, --pos);
    }
}
