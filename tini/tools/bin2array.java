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
 * bin2array is a utility that takes a binary stream and writes C style byte
 * array.  The input is classname, package name, and the input file stream.  The
 * output will be on standard out.
 */
public class bin2array {
    
    /**
     * main.
     * <br><br>
     * Usage:<br>
     * bin2array classname pkgname filename<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;classname - the class name to generate<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;packname - package name<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;filename - the input filename<br>
     *
     * @args args Command line arguments
     * @throws Exception exception
     */
    public static void main(String[] args) throws Exception {
        String classname = args[0];
        String pkgname   = args[1];
        String filename  = args[2];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fin = new FileInputStream(filename);
        byte[] buffer = new byte[1024];

        // Load the output stream
        int amount = fin.read(buffer);
        while (amount != -1) {
            baos.write(buffer, 0, amount);
            amount = fin.read(buffer);
        }
        fin.close();

        // Get the output stream
        byte[] output = baos.toByteArray();

        // Print the header
        System.out.println("package " + pkgname + ";");
        System.out.println("public class " + classname + " {");
        System.out.println("public static final int BYTE_AR_SZ = " + Integer.toString(output.length) + ";");
        System.out.println("public static final byte[] ARR = {");

        // Generate the output
        int i;
        for (i = 0; i < output.length - 1; i++) {
            System.out.println("    " + Byte.toString(output[i]) + ",");
        }
        System.out.println("    " + Byte.toString(output[i]));
        System.out.println("};");
        System.out.println("}");
    }
}
