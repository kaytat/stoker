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

import java.io.*;
import java.util.Vector;
import java.net.URLEncoder;
import com.dalsemi.protocol.http.HTTPClient;
import com.dalsemi.protocol.HeaderManager;
import com.dalsemi.system.TINIOS;
import com.dalsemi.shell.server.*;

public class Twitter extends Base_Twitter {

    public Twitter(
        boolean dbg,
        SystemPrintStream out,
        Twitter_Funcs f,
        String base64_password,
        boolean enable,
        int update_interval_in_minutes,
        String hdr,
        String hn
        ) {
        super(dbg, out, f, base64_password, enable, update_interval_in_minutes, hdr, hn);
    }

    public String update(String message, boolean verbose_return) {
        HeaderManager request_header_manager;
        HeaderManager header_manager;
        InputStream   input_stream;
        HTTPClient    client;
        String        ret = "";

        DEBUG_PRINT("Twitter:sending update");

        if (base64_user_password == null) {
            DEBUG_PRINT("Twitter:update:password not set");
            return "password not set";
        }

        request_header_manager = new HeaderManager();
        header_manager = new HeaderManager();

        String ip_addr = "";
        
        try {
            ip_addr = funcs.resolve_hostname(hostname);
            client = new HTTPClient(ip_addr, header_manager);
        } catch (Exception e) {
            DEBUG_PRINT("Twitter:HTTPClient:" + e.getMessage());
            return e.getMessage();
        }

        StringBuffer auth_value = new StringBuffer("Basic ");

        synchronized (base64_user_password) {
            auth_value.append(base64_user_password);
        }

        // Add fields to the request header
        client.setRequestHeaderManager(request_header_manager);
        request_header_manager.add("Host", hostname);
        request_header_manager.add("Authorization", auth_value.toString());
        request_header_manager.add("Content-Type", "application/x-www-form-urlencoded");

        // Generate the body of the message
        StringBuffer status_message = new StringBuffer("status=");
        status_message.append(URLEncoder.encode(message));
        try {
            client.getOutputStream().write(status_message.toString().getBytes());
        } catch (Exception e) {
            DEBUG_PRINT("Twitter:writing to output stream:" + e.getMessage());
            return e.getMessage();
        }

        // Before sending the request, prep the output that will be returned to the user.
        StringBuffer ret_buf = new StringBuffer();
        int i;

        if (verbose_return) {
            // First the request headers
            ret_buf.append("\r\n===Request header===\r\n");
            for (i = 0; i < request_header_manager.count(); i++) {
                ret_buf.append(request_header_manager.getKey(i));
                ret_buf.append(": ");
                ret_buf.append(request_header_manager.get(i));
                ret_buf.append("\r\n");
            }

            // Request body
            ret_buf.append("\r\n===Request body===\r\n");
            ret_buf.append(status_message.toString());
        }

        // Send the request
        try {
            // input_stream = client.issueCommand("POST /1/statuses/update.xml HTTP/1.1\r\n");
            input_stream = client.issueCommand("POST /1.1/statuses/update.json HTTP/1.1\r\n");
        } catch (Exception e) {
            DEBUG_PRINT("Twitter:issueCommand:" + e.getMessage());
            return e.getMessage();
        }

        if (verbose_return) {
            // Add return headers
            ret_buf.append("\r\n\r\n===Server response header===\r\n");
            for (i = 0; i < header_manager.count(); i++) {
                ret_buf.append(header_manager.getKey(i));
                ret_buf.append(": ");
                ret_buf.append(header_manager.get(i));
                ret_buf.append("\r\n");
            }
        }

        // Add return body
        try {
            if (dbg_bool && verbose_return) {
                ret_buf.append("\r\n\r\n===Server response body===\r\n");
                // Dump the XML only on the debug case.
                BufferedInputStream rd = new BufferedInputStream(input_stream);
                byte[] b = new byte[32];
                int num_read;
                while ((num_read = rd.read(b, 0, 32)) >= 0) {
                    ret_buf.append(new String(b, 0, num_read));
                }
            }
            input_stream.close();
        } catch (IOException e) {
            DEBUG_PRINT("Twitter:return:" + e.getMessage());
        }

        ret = ret_buf.toString();

        DEBUG_PRINT(ret);

        return ret;
    }

}

    