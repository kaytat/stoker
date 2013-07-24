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

package flash;

/**
 * Tiny class to read a block of data from the flash.  This classes used a native
 * call, but does not load any libraries.  So the caller MUST be aware and load
 * the library containing the native call before using this class.
 *
 * This class is used in conjunction with a simple read-only file system.  Basically,
 * the files are prepended with a four byte header that contains the size.
 *
 * Although this class does not force any particular layout, the {@link #get_sz}
 * call does offer support to read the header.
 */
public class Flash_Reader {
    /**
     * Read a raw block of data
     * @param flash_addr Location of the start of the data
     * @param block Byte array to place the data
     */
    public static native void get_raw_data(int flash_addr, byte[] block);

    /**
     * Read a raw block of data
     * @param flash_addr Location of the start of the data
     * @param block Byte array to place the data
     * @param sz Number of bytes to get
     */
    public static native void get_raw_data_sz(int flash_addr, byte[] block, int sz);

    /**
     * Get the size
     * @param addr Location of the header
     * @return Size
     */
    static public int get_sz(int addr) {
        byte[] bin_sz = new byte[4];
        get_raw_data(addr, bin_sz);

        int sz = 0;
        int temp;
        for (int i = 0; i < 4; i++) {
            temp = bin_sz[i];
            temp = temp & 0xff;
            temp = temp << (i*8);
            sz |= temp;
        }

        return sz;
    }

    /**
     * Get the data
     * @param addr Address of the raw data
     * @param ar Array to copy the data into
     * @return True on success
     */
    static public boolean get_data(int addr, byte[] ar) {
        get_raw_data(addr, ar);
        return true;
    }

    /**
     * Get the data
     * @param addr Address of the raw data
     * @param ar Array to copy the data into
     * @param sz Size to get
     * @return True on success
     */
    static public boolean get_data(int addr, byte[] ar, int sz) {
        get_raw_data_sz(addr, ar, sz);
        return true;
    }
}
