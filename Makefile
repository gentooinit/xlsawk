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

LIBRARY_DIR := libs/ooxml-lib libs
libs := $(foreach dir,$(LIBRARY_DIR),$(wildcard $(dir)/*.jar))
srcs := $(foreach dir,$(SOURCE),$(wildcard $(dir)/*.java))
objects := $(foreach dir,$(SOURCE),$(srcs:$(dir)/%.java=$(BINARY)/%.class))

CLASSPATH := $(subst jar ,jar:,$(libs))

ifneq (,$(shell whereis javac | cut -d: -f2))
CC = javac
CFLAGS = -cp $(CLASSPATH)
else ifneq (,$(shell whereis gcj | cut -d: -f2))
CC = gcj
CFLAGS = -Wall -C --classpath $(CLASSPATH)
else
$(error No JDK found!)
endif

ifneq (,$(shell whereis jar | cut -d: -f2))
PACKER = jar
else ifneq (,$(shell whereis fastjar | cut -d: -f2))
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
