/*
 * Copyright (C) 2020 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytedance.tailor;

import android.os.Debug;

import androidx.annotation.Keep;

import java.io.IOException;
import java.io.RandomAccessFile;

@Keep
public class Tailor {
    static {
        System.loadLibrary("tailor");
    }

    public static synchronized void dumpHprofData(String fileName, boolean isGzip) throws IOException {
        nOpenProxy(fileName, isGzip);
        Debug.dumpHprofData(fileName);
        nCloseProxy();
    }

    public static void cropHprofData(String source, String target, boolean isGzip) throws IOException {
        if (isHprofValid(source)) {
            nCropHprof(source, target, isGzip);
        } else {
            throw new IOException("Bad hprof file " + source);
        }
    }

    static boolean isHprofValid(String path) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path, "r");
            file.seek(file.length() - 9);
            return file.readByte() == 0x2C;
        } catch (IOException e) {
            return false;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    static native void nOpenProxy(String target, boolean gzip);

    static native void nCloseProxy();

    static native void nCropHprof(String source, String target, boolean gzip);
}