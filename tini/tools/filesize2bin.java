/*

Copyright (c) 2013 kaytat

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

*/

import java.io.*;

/**
 * filesize2bin is small utility that gets the size of a file and write it
 * in binary form into the output file.  The size of the file is 32 bits, and so
 * the output file is 4 bytes.  The layout is as follows:<br>
 * <br>
 * byte 0: [7:0]<br>
 * byte 1: [15:8]<br>
 * byte 2: [23:16]<br>
 * byte 3: [31:24]<br>
 */
public class filesize2bin {
    /**
     * Usage.
     * <br><br>
     * filesize2bin file output<br>
     *   file - the size of the file<br>
     *   output - the output file that contains the 4 byte size<br>
     */
    public static void usage() {
        System.out.println("filesize2bin file output");
        System.out.println("  file is the size which will be queried");
        System.out.println("  output is the output file");
    }

    /**
     * main.
     * @param args Command line arguments
     * @throws Exception exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Incorrect number of args");
            usage();
            return;
        }

        // int i = Integer.parseInt(args[0], 16);
        File f = new File(args[0]);
        int i = (int)f.length();

        FileOutputStream fout = new FileOutputStream(args[1]);

        // [7:0]
        fout.write(i & 0xff);
        // [15:8]
        fout.write((i >> 8) & 0xff);
        // [23:16]
        fout.write((i >> 16) & 0xff);
        // [31:24]
        fout.write((i >> 24) & 0xff);
        fout.close();

        System.out.println("filesize2bin: i:" + Integer.toHexString(i));
    }
}
