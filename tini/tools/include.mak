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
# Assume use of cygwin
#
.PHONY: generic_clean

#
# Some debug
#
MKDBG := 1
ifeq ($(MKDBG),1)
$(warning $(SOURCE))
$(warning $(OBJS))
$(warning $(OBJS_ASM))
$(warning $(BIN_DIR))
$(warning $(SRC_DIR))
endif

# Make sure OS-specific variables loaded
ifdef SEP
else
include $(TINI_HOME)/tools/osvars.mak
endif

#
# Befine bootclasspath ...
#
BOOTCLASSPATH := $(TINI_HOME)/bin/tiniclasses.jar

CLASSPATH := .$(CP_SEP)$(TINI_HOME)/bin/modules.jar$(CP_SEP)$(TINI_HOME)/bin/owapi_dependencies_TINI.jar$(CP_SEP)$(BIN_DIR)$(CP_SEP)$(LOCAL_CP)

generic_clean:
	-$(RMDIRCMD) $(BIN_DIR)
	-$(RMDIRCMD) pp_src
	$(if $(EXTRA_CLEAN), -$(RMCMD) $(subst /,$(PATH_SEP),$(EXTRA_CLEAN)))

#
# Rules
#
$(BIN_DIR)/%.tlib: $(SRC_ASM_DIR)/%.a51
	-mkdir $(BIN_DIR)
	$(subst /,$(PATH_SEP),macro -I$(TINI_HOME)/native/lib $<)
	a390 -p 400 -l $*.mpp
	mv -f $*.mpp $*.lst $*.tlib $(BIN_DIR)

$(OBJS): $(SOURCE)
	-mkdir $(BIN_DIR)
	javac -verbose -source 1.2 -target 1.1 -bootclasspath $(BOOTCLASSPATH) -classpath $(CLASSPATH) -d $(BIN_DIR) $(SOURCE)

#
#	gcc -E -P -x c -D TINI -D WIFI_ENABLE -D DISABLE_P5_P6 -D DEBUG -D DO_AUDIBLE $< -o $@
#	gcc -E -P -x c -D TINI -D WIFI_ENABLE -D DISABLE_P5_P6 -D DEBUG $< -o $@
#	gcc -E -P -x c -D TINI -D WIFI_ENABLE -D DISABLE_P5_P6 $< -o $@
#	gcc -E -P -x c -D TINI $< -o $@
#	gcc -E -P -x c -D TINI -D WIFI_ENABLE $< -o $@
#
# Regular, non-wifi release:
#	gcc -E -P -x c -D TINI -D DO_AUDIBLE $< -o $@
#
# Regular, wifi and buzzer release:
#	gcc -E -P -x c -D TINI -D DO_AUDIBLE -D WIFI_ENABLE $< -o $@
#
pp_src/%.java: $(SRC_DIR)/%.java
	-mkdir pp_src
	-mkdir $(MKDIR_TREE_OPT) $(subst /,$(PATH_SEP),$(dir $@))
	gcc -E -P -x c -D TINI -D DO_AUDIBLE -D WIFI_ENABLE $< -o $@


#$(BIN_DIR)/%.class: %.java
#	-mkdir $(BIN_DIR)
#	-mkdir pp_src
#	-gcc -E -P -x c -D TINI $< -o pp_src/$<
#	javac -verbose -source 1.2 -target 1.1 -bootclasspath $(BOOTCLASSPATH) -classpath $(CLASSPATH) -d $(BIN_DIR) pp_src/$<

$(APP).jar: $(OBJS) $(OBJS_ASM)
	jar -cvf $(APP).jar -C bin .

#
# -x ..\..\bin\owapi_dep.txt -> use default dependency file
# -f bin -> use all the classes in the bin directory
# -n src\reg.tlib -> native library
# -o bin\bbqbs.tini -> output
# -d ..\..\bin\tini.db -> some db file
# -p xxx -> place to find the class file thing
#
BD_PATH_TMP := -p $(BOOTCLASSPATH)$(CP_SEP)$(CLASSPATH)
ifeq ($(OS),Linux)
BD_PATH := $(subst $(CP_SEP),\;,$(BD_PATH_TMP))
else
BD_PATH := $(BD_PATH_TMP)
endif
BD_ADD     ?=
BD_DEPFILE ?= -x $(TINI_HOME)/bin/owapi_dep.txt

TC_NATIVE  ?= $(if $(OBJS_ASM),-n $(OBJS_ASM))
TC_CLASS   ?= -f $(BIN_DIR)
TC_OUTPUT  ?= -o $@
TC_DB      ?= -d $(TINI_HOME)/bin/tini.db
TC_VERBOSE ?= -debug -v
TC_EXE     ?= -l -t 470100

$(BIN_DIR)/$(APP).tini: $(OBJS) $(OBJS_ASM) $(EXT_DEP)
	java -cp $(TINI_HOME)/bin/tini.jar BuildDependency $(BD_PATH) $(BD_ADD) $(BD_DEPFILE) $(TC_NATIVE) $(TC_CLASS) $(TC_OUTPUT) $(TC_DB) $(TC_VERBOSE)

$(BIN_DIR)/$(APP).tbin: $(OBJS) $(OBJS_ASM) $(EXT_DEP)
	java -cp $(TINI_HOME)/bin/tini.jar BuildDependency $(BD_PATH) $(BD_ADD) $(BD_DEPFILE) $(TC_NATIVE) $(TC_CLASS) $(TC_OUTPUT) $(TC_DB) $(TC_VERBOSE) $(TC_EXE)

%:: ;
