# Force to use Bash. We need $(shell) spawns a bash instead of sh.
# Because the /bin/sh maybe links to other shell.
SHELL = /bin/bash
NAME = xlsawk
PREFIX = org
OWNER = init0

SRC_DIR = src
BIN_DIR  = bin
BUILD_DIR = build

TARGET = $(NAME).jar
PACKAGE_DIR = $(PREFIX)/$(OWNER)

SOURCE := $(SRC_DIR)/$(PACKAGE_DIR)
BINARY := $(BIN_DIR)/$(PACKAGE_DIR)

LIBRARY_DIR := libs/ooxml-lib libs libs/lib
libs := $(foreach dir,$(LIBRARY_DIR),$(wildcard $(dir)/*.jar))
srcs := $(foreach dir,$(SOURCE),$(wildcard $(dir)/*.java))
objects := $(foreach dir,$(SOURCE),$(srcs:$(dir)/%.java=$(BINARY)/%.class))

CLASSPATH := $(subst jar ,jar:,$(libs))

ifeq (0,$(shell which javac &> /dev/null; echo $$?))
CC = javac
CFLAGS = -cp $(CLASSPATH)
else ifeq (0,$(shell which gcj &> /dev/null; echo $$?))
CC = gcj
CFLAGS = -Wall -C --classpath $(CLASSPATH)
else
$(error No JDK found!)
endif

ifeq (0,$(shell which jar &> /dev/null; echo $$?))
PACKER = jar
else ifeq (0,$(shell which fastjar &> /dev/null; echo $$?))
PACKER = fastjar
else
$(error No Jar packer found!)
endif

all: $(TARGET)

$(TARGET): $(BIN_DIR) pack

pre_build: $(objects)
	@-mkdir -p $(BUILD_DIR)
	-cp -rf $(BIN_DIR)/$(PREFIX) $(BUILD_DIR)

$(libs): pre_build
	-cd $(BUILD_DIR);\
	$(PACKER) xf ../$@

pack: $(libs)
	-cd $(BUILD_DIR);\
	$(PACKER) cMf ../$(TARGET) *

$(BIN_DIR):
	-mkdir -p $@

$(objects): $(BINARY)/%.class: $(SOURCE)/%.java
	$(CC) $(CFLAGS) -d $(BIN_DIR) $<

rebuild: clean all

.PHONY: clean
clean:
	@-rm -rf $(BIN_DIR)
	@-rm -rf $(BUILD_DIR)
	@-rm -rf $(TARGET)
	@-rm -rf *~
	@-rm -rf $(foreach file,$(srcs),$(file)~)
