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
 * tbin2bin is a small utilty to convert from the TBIN format to the raw binary stream.
 * The input to this is a list of tbin formatted files.  The output is a raw binary
 * stream starting from the lowest address found all of the TBIN files to the highest
 * address found.  These start and end addresses are aligned on a 0x1000 byte
 * boundary.
 */
public class tbin2bin {
    /**
     * Usage for this utility.
     * tbin2bin output input0 [input1 ...]<br>
     * <br>
     * output - output file name<br>
     * input0 - required first input tbin filename<br>
     * input1 ... - optional list of other tbin input files
     */
    public static void usage() {
        System.out.println("tbin2bin output input0 [input1 ...]");
        System.out.println("  output is the output filename");
        System.out.println("  input0 is the required first tbin input");
        System.out.println("  All other strings are assumed to be more input files");
    }

    /** Main entry point
     *
     * @param  args      Command line arguments
     * @throws Exception Standard exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length <= 2) {
            System.out.println("Incorrect number of args");
            usage();
            System.exit(1);
            return;
        }

        int i;
        String output = args[0];

        Tbin tbin = new Tbin();

        for (i = 1; i < args.length; i++) {
            if (tbin.add(args[i]) == false) {
                System.out.println("Failed");
                System.exit(1);
                return;
            }
        }
        tbin.generate_contig_bin(output);
    }
}

/**
 * Tbin is a class that can decode TBIN files and store them as a raw binary stream
 */
class Tbin {
    /**
     * Array of arrays to represent all the data.  The first dimension represents
     * the set of all banks.  The second dimension represents the actual byte in
     * each 0x10000 byte bank
     */
    byte[][] banks;

    /**
     * Generate a resonable max size of banks.
     */
    Tbin() {
        banks = new byte[256][];
    }

    /**
     * Add the TBIN data from filename into the raw byte stream.
     *
     * @param filename Name of file that contains the TBIN data
     * @throws Exception Exception
     */
    boolean add(String filename) throws Exception {
        // Open the tbin file
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(filename));

        int rd;
        int addr;
        int bank;
        int length;
        byte[] temp_ar = new byte[1];

        while (true) {
            // Get the bottom 16 bits of the addr
            rd = input.read();
            addr = rd;
            if (rd == -1) {
                break;
            }

            rd = input.read();
            addr |= rd << 8;

            // get the bank
            bank = input.read();

            // Get the length
            rd = input.read();
            length = rd;

            rd = input.read();
            length |= rd << 8;
            length++;

            // Make sure the bank exists
            if (banks[bank] == null) {
                banks[bank] = new byte[0x10000];

                for (int i = 0; i < 0x10000; i++) {
                    banks[bank][i] = (byte)0xff;
                }
            }

            System.out.println(
                "add: bank:" + Integer.toHexString(bank) +
                " addr:" + Integer.toHexString(addr) +
                " len:" + Integer.toHexString(length)
                );

            // Read the data into the bank
            for (int j = 0; j < length; j++) {
                rd = input.read(temp_ar, 0, 1);
                if (rd != 1) {
                    System.out.println("add: Could not read entire tbin");
                    break;
                }
                if (banks[bank][addr] != (byte)0xff) {
                    System.out.println("Error: Overlapping images detected");
                    return false;
                }
                banks[bank][addr] = temp_ar[0];
                addr++;
            }

            // Ignore 2 bytes of CRC
            rd = input.read();
            rd = input.read();
        }

        input.close();

        System.out.println("add: read done");

        return true;
    }

    /**
     * Generate the raw byte stream.  This will cycle through all the banks and
     * starting with the first bank used, will output all 0x10000 bytes of that
     * bank into the file described by filename parameter.  So, if everything
     * goes well, the final output size should be multiple of 0x10000
     *
     * @param out_fn Filename for output
     * @throws Exception Exception
     */
    void generate_contig_bin(String out_fn) throws Exception {
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(out_fn));

        int i;
        int start_bank = -1;
        int end_bank = 0;

        // Preprocess the byte array to determine the start and end banks
        for (i = 0; i < 256; i++) {
            if (banks[i] != null) {
                if (start_bank == -1) {
                    start_bank = i;
                }
                end_bank = i;
            }
        }

        System.out.println("generate: s:" + Integer.toHexString(start_bank) + " e:" + Integer.toHexString(end_bank));

        for (i = start_bank; i <= end_bank; i++) {
            if (banks[i] == null) {
                // Bank was skipped.  Output 0xff
                for (int j = 0; j < 0x10000; j++){
                    output.write((byte)0xff);
                }
            }
            else {
                output.write(banks[i], 0, banks[i].length);
            }
        }

        output.close();
        System.out.println("generate: Use start address of:" + Integer.toHexString(start_bank * 0x10000));
        System.out.println("generate: done");
    }
}
