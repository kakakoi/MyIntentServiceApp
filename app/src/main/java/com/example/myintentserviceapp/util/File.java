package com.example.myintentserviceapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class File {

    /**
     * コピー中にコールバック実行
     *
     * @param in
     * @param out
     * @param callback
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out, Callback callback) throws IOException {
        //try {
        //try {
        long timer = 1000;
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long outLen = 0;
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            outLen += len;
            endTime = System.currentTimeMillis();
            if ((endTime - startTime) > timer) {
                startTime = endTime;
                callback.call(outLen);
            }
        }
    }
}
