#
# Copyright (c) 2013 kaytat
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

#
# $File: //depot/stoker_git/stoker/tini/tools/constants.mak $
# $Date: 2013/07/23 $
# $Revision: #1 $
# $Author: kaytat $
#

# Location of web page.  Notice this does not need the size
# location because the size is encoded in the java class.
INDEX_LOC := 4a0000

# Location of flash library.  This is just a small library and does not take up
# must room.
FLASH_LIB_SZ_LOC := 4a8000
FLASH_LIB_LOC := 4a8004

# Location of flash utility application
FLASH_UTIL_SZ_LOC := 4b0000
FLASH_UTIL_LOC := 4b0004

# Location of alarm 1
ALARM1_SZ_LOC := 4b8000
ALARM1_LOC := 4b8004

# Location of alarm 2
ALARM2_SZ_LOC := 4c0000
ALARM2_LOC := 4c0004

# Location of favicon
FAVICON_SZ_LOC := 4d0000
FAVICON_LOC := 4d0004
