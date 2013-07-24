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

import java.net.*;
import java.io.*;
import javax.comm.*;
import com.dalsemi.system.*;

/**
 * This implements the raw socket-based communication used between the upgrade
 * application running on the stoker and the application running on the PC.
 */
public final class Flash_Comm {
    /**
     * The socket that the stoker is listening on for upgrade requests.
     */
    public static final int SERVER_SOCKET = 12346;

    /**
     * Request ID for erasing a set of sectors
     */
    public static final int ERASE = 1;

    /**
     * Request ID for writing to a sector
     */
    public static final int WRITE = 2;

    /**
     * Request ID for a simple ping
     */
    public static final int PING  = 3;

    /**
     * Request ID for preparing for the upgrade process.  Currently, this request
     * results in killing slush.
     */
    public static final int PREP_UPGRADE = 4;

    /**
     * Reboot request
     */
    public static final int REBOOT = 5;

    /**
     * Request for launching the upgrade application
     */
    public static final int LAUNCH_UPGRADE = 6;

    /**
     * Input stream used to communicate with the far end.  This is usually created
     * with sockets as the underlying communication link.
     */
    BufferedInputStream buffer_in;
    /**
     * Output stream
     */
    BufferedOutputStream buffer_out;

    /**
     * Constructor
     */
    public Flash_Comm() {
    }

    /**
     * Set the streams.  This is necessary because the underlying socket may change
     * due to network connectivity problems or other unknown breaks in the socket
     * connection.
     * @param buffer_in Input
     * @param buffer_out Output
     */
    public void set_streams(BufferedInputStream buffer_in, BufferedOutputStream buffer_out) {
        this.buffer_in = buffer_in;
        this.buffer_out = buffer_out;
    }

    /**
     * A request packet
     */
    public class Packet {
        /**
         * Request.  The only acceptable values are the static ints defined above.
         */
        public int req;
        /**
         * Sector address
         */
        public int sector_addr;
        /**
         * Sector offset
         */
        public int sector_offset;
        /**
         * A generic parameter
         */
        public int num_param;
        /**
         * Size of a sector
         */
        public int sector_size;

        /**
         * Default constructor
         */
        public Packet() {
            req = sector_addr = sector_offset = num_param = sector_size = 0;
        }

        /**
         * Constructor with all the packet parameters defined
         * @param req Request
         * @param sector_addr Sector address
         * @param sector_offset Sector offset
         * @param num_param Generic parameter
         * @param sector_size Sector size
         */
        public Packet(int req, int sector_addr, int sector_offset, int num_param, int sector_size) {
            this.req = req;
            this.sector_addr = sector_addr;
            this.sector_offset = sector_offset;
            this.num_param = num_param;
            this.sector_size = sector_size;
        }

        /**
         * Utility to dump the contents of the packet
         */
        public void dump() {
            System.out.println(
                "Packet:" +
                " req:"  + Integer.toHexString(req) +
                " addr:" + Integer.toHexString(sector_addr) +
                " off:"  + Integer.toHexString(sector_offset) +
                " num:"  + Integer.toHexString(num_param) +
                " size:" + Integer.toHexString(sector_size)
                );
        }
    }

    /**
     * Acknowledgement
     */
    public class Ack {
        /**
         * Integer acknowledgement which usually contains a status, but this is ill-defined
         * at the moment.
         */
        public int ack;

        /**
         * Default constructor
         */
        public Ack() {
            ack = 0;
        }

        /**
         * Constructor with the integer value specified
         * @param i Integer
         */
        public Ack(int i) {
            ack = i;
        }

        /**
         * Utility to dump the contents
         */
        public void dump() {
            System.out.println("Ack:" + Integer.toHexString(ack));
        }
    }

    /**
     * Send an integer as a stream of four bytes.
     * @param i Integer to send
     * @throws java.lang.Throwable Exception
     */
    void send_int(int i) throws Throwable {
        byte[] raw_bytes = new byte[4];

        raw_bytes[0] = (byte)(i & 0xff);
        raw_bytes[1] = (byte)((i >> 8) & 0xff);
        raw_bytes[2] = (byte)((i >> 16) & 0xff);
        raw_bytes[3] = (byte)((i >> 24) & 0xff);

        buffer_out.write(raw_bytes, 0, 4);
        buffer_out.flush();
    }

    /**
     * Send an acknowledgement
     * @param ack Acknowledgement
     * @throws java.lang.Throwable Exception
     */
    public void send(Ack ack) throws Throwable {
        send_int(ack.ack);
    }

    /**
     * Send a packet
     * @param p Packet
     * @throws java.lang.Throwable Exception
     */
    public void send(Packet p) throws Throwable {
        send_int(p.req);
        send_int(p.sector_addr);
        send_int(p.sector_offset);
        send_int(p.num_param);
        send_int(p.sector_size);
    }

    /**
     * Reconstruct an integer from the stream
     * @throws java.lang.Throwable Throwable
     * @return Integer
     */
    int get_int() throws Throwable {
        byte[] raw_bytes = new byte[4];
        int ret;
        int temp;
        int i = 0;

        while (i != 4) {
            ret = buffer_in.read(raw_bytes, i, 4 - i);
            if (ret == -1) {
                throw new Throwable();
            }
            i += ret;
        }

        ret = 0;
        for (i = 0; i < 4; i++) {
            temp = raw_bytes[i];
            temp = temp & 0xff;
            temp = temp << (i*8);
            ret |= temp;
        }

        return ret;
    }

    /**
     * Get an acknowledgement
     * @throws java.lang.Throwable Exception
     * @return A new acknowledgement
     */
    public Ack get_ack() throws Throwable {
        return new Ack(get_int());
    }

    /**
     * Get a packet
     * @throws java.lang.Throwable Exception
     * @return A new packet object
     */
    public Packet get_packet() throws Throwable {
        int req = get_int();
        int sector_addr = get_int();
        int sector_offset = get_int();
        int num_param = get_int();
        int sector_size = get_int();

        return new Packet(req, sector_addr, sector_offset, num_param, sector_size);
    }
}
