CC = javac
SRC_DIR = src
BIN_DIR  = bin
PACKAGE_DIR = org/init0

SOURCE := $(SRC_DIR)/$(PACKAGE_DIR)
BINARY := $(BIN_DIR)/$(PACKAGE_DIR)
BUILD := build

LIBRARY_DIR := libs/ooxml-lib libs
libs := $(foreach dir,$(LIBRARY_DIR),$(wildcard $(dir)/*.jar))
srcs := $(foreach dir,$(SOURCE),$(wildcard $(dir)/*.java))
objects := $(foreach dir,$(SOURCE),$(srcs:$(dir)/%.java=$(BINARY)/%.class))

CFLAGS := -cp $(subst jar ,jar:,$(libs))

all: $(objects)

$(objects): $(srcs)
	@-mkdir $(BIN_DIR)
	$(CC) $(CFLAGS) -d $(BIN_DIR) $^

rebuild: clean all

.PHONY: clean
clean:
	@-rm -r $(BIN_DIR)
