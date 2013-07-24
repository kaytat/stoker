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
# $File: //depot/stoker_git/stoker/tini/examples/twitter/makefile $
# $Date: 2013/07/23 $
# $Revision: #2 $
# $Author: kaytat $
#
include $(TINI_HOME)/tools/constants.mak

.PHONY: all clean

APP := twitter
SRC_DIR := stoker/twitter
BIN_DIR := bin

BASE_SOURCE := \
    Twitter.java \
	Base_Twitter.java \
    Twitter_Funcs.java \
    Base64.java

SOURCE := $(addprefix pp_src/,$(BASE_SOURCE))
OBJS   := $(addprefix $(BIN_DIR)/$(SRC_DIR)/,$(BASE_SOURCE))
OBJS   := $(OBJS:%.java=%.class)

SRC_ASM_DIR := .
OBJS_ASM :=

EXTRA_CLEAN := $(APP).jar

all: $(APP).jar

clean: generic_clean

TINI_HOME := $(subst \,/,$(TINI_HOME))

COVERTERPATH := $(TINI_HOME)/tools
COVERTERCLASSPATH := $(COVERTERPATH)/bin
COVERTERCLASSPATH := $(subst ;,\;,$(COVERTERCLASSPATH))

include $(TINI_HOME)/tools/include.mak
