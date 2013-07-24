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

package flash_util;

import java.io.*;
import java.net.*;
import javax.comm.*;
import com.dalsemi.system.*;
import flash.Flash_Comm;

#ifdef DISABLE_P5_P6
    #define CLR_DISPLAY()
#else
    #define CLR_DISPLAY() clr_display()
#endif

/**
 * Utility that runs on the Stoker to update the Stoker/slush software.
 */
public final class Flash_Util {
    /**
     * Native to clear the LCD
     */
    public static native void clr_display();

    /**
     * Native to erase a sector
     * @param flash_addr Flash address.  Assuming the flash sector size is 64k, the flash itself will ignore
     * the lower 16 bits of this address.  The upper 16 bits will uniquely identify
     * the sector.
     */
    public static native void erase_sector(int flash_addr);

    /**
     * Write a block
     * @param flash_addr Address
     * @param block Data to write
     */
    public static native void program_block(int flash_addr, byte[] block);

    /**
     * Input stream
     */
    BufferedInputStream buffer_in  = null;
    /**
     * Output stream
     */
    BufferedOutputStream buffer_out = null;
    /**
     * The socket used for listening for upgrade requests
     */
    ServerSocket server = null;
    /**
     * The input socket returned when the listening socket is accepted
     */
    Socket in_sock = null;
    /**
     * A helper so that memory does not have to be constantly allocated/deallocated
     */
    byte[] block;

    /**
     * Default constructor
     */
    Flash_Util() {
        block = new byte[1024];
    }

    /**
     * The main loop
     */
    void loop() {
        int read_count = 0;
        int written_count = 0;
        int arg;
        int req;
        int sector_addr;
        int sector_offset;
        int sector_size;
        int num_param;
        int addr;
        int i;
        int total_read_count;

        Flash_Comm flash_comm = new Flash_Comm();

        while (true) {
            // Some debug info - this was done just to see some of the real-time
            // operating parameters of this stand-alone process.
            // System.out.println("User: " + TINIOS.getCurrentUserName());
            // System.out.println("Shell: " + TINIOS.getShellName());

            try {
                if (server == null) {
                    server = new ServerSocket(Flash_Comm.SERVER_SOCKET, 1);
                }
                in_sock = server.accept();
                buffer_in = new BufferedInputStream(in_sock.getInputStream());
                buffer_out = new BufferedOutputStream(in_sock.getOutputStream());

                flash_comm.set_streams(buffer_in, buffer_out);
                Flash_Comm.Packet packet = flash_comm.get_packet();

                packet.dump();
                req = packet.req;
                sector_addr = packet.sector_addr;
                sector_offset = packet.sector_offset;
                num_param = packet.num_param;
                sector_size = packet.sector_size;

                switch (req) {
                    case Flash_Comm.ERASE:
                        addr = sector_addr;
                        for (i = 0; i < num_param; i++) {
                            System.out.println("Flash_Util: erase:" + Integer.toHexString(addr));
                            Thread.sleep(1000);
                            erase_sector(addr);
                            addr += sector_size;
                        }

                        flash_comm.send(flash_comm.new Ack(addr));
                        break;

                    case Flash_Comm.WRITE:
                        // In this case, num_param is number of 1k blocks to write
                        addr = sector_addr + sector_offset;

                        for (i = 0; i < num_param; i++) {
                            total_read_count = 0;

                            while (total_read_count != 1024) {
                                read_count = buffer_in.read(block, total_read_count, 1024 - total_read_count);
                                System.out.println("Flash_Util: addr: " + Integer.toHexString(addr) + " rd:" + Integer.toHexString(read_count));
                                if (read_count == -1) {
                                    throw new Throwable();
                                }
                                total_read_count += read_count;
                            }
                            program_block(addr, block);
                            addr += 1024;
                        }

                        // Send an acknowledge out
                        flash_comm.send(flash_comm.new Ack(addr));
                        break;

                    case Flash_Comm.PREP_UPGRADE:
                        CLR_DISPLAY();

                        //
                        // Kill the slush task
                        //
                        try {
                            TINIOS.killTask(2);
                        }
                        catch (Exception e) {
                            System.out.println("Flash_Util: exception killing slush: " + e.getMessage());
                        }

                        // Some special things must be taken care of.  First make sure
                        // the serial server is shutdown so the user cannot interact with
                        // the serial shell.
                        try {
                            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier("serial0");

                            // Wait two seconds.  Since slush has been killed, the serial port
                            // should be handed over immediately, but wait anyway.
                            SerialPort serialPort = (SerialPort)cpi.open("init", 2000);

                            serialPort.setSerialPortParams(
                                115200,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE
                                );

                            System.setIn(serialPort.getInputStream());
                            System.setOut(new PrintStream(serialPort.getOutputStream()));
                            System.setErr(new PrintStream(serialPort.getOutputStream()));
                        }
                        catch (Exception e) {
                            System.out.println("Flash_Util: exception while getting com: " + e.getMessage());
                        }

                        //
                        // Finally garbage collect
                        //
                        System.gc();
                        Thread.sleep(1000);
                        flash_comm.send(flash_comm.new Ack(Flash_Comm.PREP_UPGRADE));
                        break;

                    case Flash_Comm.REBOOT:
                        TINIOS.blastHeapOnReboot(TINIOS.BLAST_ALL);
                        TINIOS.reboot();
                        break;

                    default:
                    case Flash_Comm.PING:
                        break;

                }

                // Success.  Keep going.
                buffer_in.close();
                buffer_in = null;
                buffer_out.close();
                buffer_out = null;
                in_sock.close();
                in_sock = null;

                System.out.println("Flash_Util: done");
                continue;
            }
            catch (Throwable t) {
                // Something went wrong.
                System.out.println("Flash_Util:" + t + " " + t.getMessage());

                // Attempt to shutdown the network objects
                if (server != null) {
                    try {
                        server.close();
                    }
                    catch (Throwable c_t) {
                        System.out.println("Flash_Util: close:" + c_t + " " + c_t.getMessage());
                    }
                    server = null;
                }

                if (in_sock != null) {
                    try {
                        in_sock.close();
                    }
                    catch (Throwable c_t) {
                        System.out.println("Flash_Util: close:" + c_t + " " + c_t.getMessage());
                    }
                    in_sock = null;
                }

                // Clear out buffers
                if (buffer_in != null) {
                    try {
                        buffer_in.close();
                    }
                    catch (Throwable in_close_t) {
                    }
                    buffer_in = null;
                }
                if (buffer_out != null) {
                    try {
                        buffer_out.close();
                    }
                    catch (Throwable out_close_t) {
                    }
                    buffer_out = null;
                }

                // Wait a bit before continuing
                try {
                    Thread.sleep(1000);
                }
                catch (Throwable sl_t) {
                    System.out.println("Flash_Util: sleep:" + sl_t + " " + sl_t.getMessage());
                }
                continue;
            }
        }
    }

    /**
     * Main
     * @param args Arguments, which are unused
     * @throws java.lang.Exception Exception
     */
    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name", "").equals("slush")) {
            System.loadLibrary("ram_lib.tlib");
        }
        (new Flash_Util()).loop();
    }
}
