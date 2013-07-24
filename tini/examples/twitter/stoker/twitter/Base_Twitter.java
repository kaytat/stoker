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

package stoker.twitter;

#ifdef DEBUG
    #define DEBUG_PRINT(_X_) if (dbg_bool) out.println(_X_)
#else
    #define DEBUG_PRINT(_X_)
#endif

import com.dalsemi.shell.server.*;
import java.util.Vector;

abstract public class Base_Twitter extends Thread {

    abstract public String update(String message, boolean verbose_return);

    volatile boolean active = true;
    volatile boolean enable;

    public String hostname = "api.supertweet.net";
    public Twitter_Funcs funcs;
    SystemPrintStream out;

    public String base64_user_password;
    int update_interval_in_minutes;
    String header = "Stoker:";
    Vector q;
    boolean dbg_bool;

    public Base_Twitter(
        boolean dbg,
        SystemPrintStream out,
        Twitter_Funcs f,
        String base64_password,
        boolean enable,
        int update_interval_in_minutes,
        String hdr,
        String hn
        ) {
        int i;

        this.dbg_bool = dbg;
        this.out = out;
        funcs = f;

        this.base64_user_password = base64_password;
        this.enable = enable;
        this.update_interval_in_minutes = update_interval_in_minutes;

        if (this.update_interval_in_minutes <= 0) {
            this.update_interval_in_minutes = 30;
        }

        // Process the header parameter.  If this looks invalid, then ignore
        for (i = 0; i < hdr.length(); i++) {
            if (hdr.charAt(i) != 0xff) {
                break;
            }
        }
        if ((i == hdr.length()) && (i > 0)) {
            // Looks invalid.  Ignore.
        } else {
            header = hdr;
        }

        // Do the same with hostname
        for (i = 0; i < hn.length(); i++) {
            if (hn.charAt(i) != 0xff) {
                break;
            }
        }
        if ((i == hn.length()) && (i > 0)) {
            // Looks invalid.  Ignore.
        } else {
            hostname = hn;
        }

        q = new Vector();
    }

    // For these modification routines, assume the caller has
    // the lock preventing this from running.
    public void set_user_password(String user, String password) {
        if ((user.compareTo("") == 0) || (password.compareTo("") == 0)) {
            // One or both of the arguments is null.  This most likely means
            // the user was changing other twitter parameters (like en/dis or timing).
            // So, ignore this change request.
            // The proper way to disable is to use the disable control on the
            // web page.
            return;
        }

        String concat = user + ":" + password;
        base64_user_password = Base64.encode(concat);
        DEBUG_PRINT("Twitter:set_user_password:" + concat + " " + base64_user_password);

        funcs.set_user_password(base64_user_password);
    }
    public String get_user_password() {
        return base64_user_password;
    }

    public void set_enable(boolean enable) {
        this.enable = enable;
        funcs.set_enable(enable);
    }
    public boolean get_enable() {
        return enable;
    }

    public void set_hostname(String hn) {
        if (hostname.compareTo("") == 0) {
            return;
        }

        DEBUG_PRINT("Twitter:set_hostname:" + hostname);

        hostname = hn;
        funcs.set_hostname(hostname);
    }
    public String get_hostname() {
        return hostname;
    }

    public void set_update_interval_in_minutes(int new_interval) {
        if (new_interval <= 0) {
            // Don't allow an interval of 0.
            return;
        }
        if (update_interval_in_minutes == new_interval) {
            return;
        }

        update_interval_in_minutes = new_interval;
        funcs.set_update_interval_in_minutes(new_interval);

        // Make sure new interval is used immediately
        interrupt();
    }
    public int get_update_interval_in_minutes() {
        return update_interval_in_minutes;
    }

    public void set_header(String s) {
        header = s;
        funcs.set_header(header);
    }
    public String get_header() {
        return header;
    }

    public void queue_update(String s) {
        Twitter_Message m = new Twitter_Message(s, false);

        synchronized (q) {
            q.addElement(m);
        }
        interrupt();
    }

    public void queue_custom(Twitter_Message m) {
        // Twitter_Message m = new Twitter_Message(s, true);

        synchronized (q) {
            q.addElement(m);
        }
        interrupt();
    }

    public void run() {
        while (active) {
            // Sleep for a long time
            try {
                Thread.sleep(update_interval_in_minutes * 60 * 1000);
            } catch (Exception e) {
                DEBUG_PRINT("Twitter:" + e.getMessage());
            }

            Twitter_Message msg = null;

            // Drain if necessary
            if (enable == false || active == false) {
                // Drain messages
                synchronized (q) {
                    while (q.size() != 0) {
                        msg = (Twitter_Message)(q.elementAt(0));
                        q.removeElementAt(0);
                        if ((msg != null) && msg.is_custom) {
                            msg.set_return("Not ready");
                            synchronized (msg) {
                                msg.notify();
                            }
                        }
                    }
                }

                if (active == false) {
                    return;
                }
                continue;
            }

            StringBuffer s = new StringBuffer(header);

            // First, process all the queued updates
            boolean cont = true;
            while (cont) {
                msg = null;
                synchronized (q) {
                    if (q.size() != 0) {
                        msg = (Twitter_Message)(q.elementAt(0));
                        q.removeElementAt(0);
                    }
                }
                if (msg != null) {
                    if (msg.is_custom) {
                        // Custom message.  Just update.
                        msg.set_return(update(msg.message, true));
                        synchronized (msg) {
                            msg.notify();
                        }
                        continue;
                    } else {
                        s.append(msg.message + ":");
                    }
                } else {
                    cont = false;
                }
            }

            // Then process the temp reading
            s.append(funcs.get_update());
            update(s.toString(), false);
        }
    }

    public void force_stop() {
        active = false;
        interrupt();
    }

    public class Twitter_Message {
        String message;
        String ret;
        boolean is_custom;

        public Twitter_Message(String s, boolean b) {
            ret = "";
            message = s;
            is_custom = b;
        }

        public void set_return(String s) {
            ret = s;
        }

        public String get_return() {
            return ret;
        }
    }

}
