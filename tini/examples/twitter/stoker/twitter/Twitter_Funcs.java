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

import java.io.*;

/*
    This interface is called when the Twitter object
    applies a change to one of its operating parameters.
*/
abstract public class Twitter_Funcs {
    public Twitter_Funcs() {
    }

    abstract public String get_update();

    abstract public void set_user_password(String new_user_password);
    abstract public void set_enable(boolean enable);
    abstract public void set_update_interval_in_minutes(int new_interval);
    abstract public void set_header(String header);
    abstract public void set_hostname(String hostname);
    abstract public String resolve_hostname(String hostname) throws IOException;
}
