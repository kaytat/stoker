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
# $File: //depot/stoker_git/stoker/tini/examples/Flash_Util/makefile $
# $Date: 2013/07/23 $
# $Revision: #2 $
# $Author: kaytat $
#
include $(TINI_HOME)/tools/constants.mak

.PHONY: all

APP := flash_util
BIN_DIR := bin

#BASE_SOURCE = flash_util/Flash_Util.java flash/Flash_Comm.java flash/Flash_Reader.java flash/Flash_Constants.java
#SOURCE := $(addprefix pp_src/,$(BASE_SOURCE))
#OBJS := $(SOURCE:%.java=$(BIN_DIR)/%.class)

BASE_SOURCE := \
    $(wildcard flash_util/*.java) \
    $(wildcard flash/*.java) \
    flash/Flash_Constants.java

SRC_DIR := .
SOURCE := $(addprefix pp_src/,$(BASE_SOURCE))
OBJS := $(BASE_SOURCE:%.java=$(BIN_DIR)/%.class)

SRC_ASM_DIR := .
OBJS_ASM := $(BIN_DIR)/flash_util.tlib

EXTRA_CLEAN := tbin tini stats.html flash/Flash_Constants.java

all: $(BIN_DIR)/$(APP).tbin $(BIN_DIR)/$(APP).tini

clean: generic_clean

TINI_HOME := $(subst \,/,$(TINI_HOME))

COVERTERPATH := $(TINI_HOME)/tools
COVERTERCLASSPATH := $(COVERTERPATH)/bin
COVERTERCLASSPATH := $(subst ;,\;,$(COVERTERCLASSPATH))

TC_EXE := -l -t $(FLASH_UTIL_LOC)

pp_src/flash/Flash_Constants.java: $(TINI_HOME)/tools/constants.mak
	perl gen_constants.pl $(TINI_HOME)/tools/constants.mak > pp_src/flash/Flash_Constants.java

#
# When the application runs, assume the native library is already loaded.
# So, do not include the native library in the build.  To do this, override
# the TC_NATIVE variable so that the default is not used.
#
TC_NATIVE :=

include $(TINI_HOME)/tools/include.mak
