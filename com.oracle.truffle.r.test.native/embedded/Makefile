#
# Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

ifeq ($(TOPDIR),)
    TOPDIR = $(abspath ..)
endif

NATIVE_PROJECT = $(subst test.native,native,$(TOPDIR))
ifneq ($(MAKECMDGOALS),clean)
include $(NATIVE_PROJECT)/platform.mk
endif

OSNAME := $(shell uname)

ifeq ($(OSNAME), Linux)
  LD_FLAGS :=  -Wl,--unresolved-symbols=ignore-all
else 
  ifeq ($(OSNAME), SunOS)
    LD_FLAGS := -z lazyload
  endif
endif

FASTR_LIB_DIR = $(abspath ../../lib)


.PHONY: all clean

OBJ = lib
SRC = src
C_SOURCES := $(wildcard $(SRC)/*.c)
C_OBJECTS := $(subst $(SRC),$(OBJ),$(C_SOURCES:.c=.o))


INCLUDE_DIR := $(NATIVE_PROJECT)/include

all: $(OBJ)/main Makefile

$(OBJ)/main: | $(OBJ)

$(OBJ):
	mkdir -p $(OBJ)


$(OBJ)/main: $(SRC)/main.c
	$(CC) $(CFLAGS) -I$(INCLUDE_DIR) $< -o $(OBJ)/main -L $(FASTR_LIB_DIR) -ldl -lR $(LD_FLAGS) -Wl,-rpath,$(FASTR_LIB_DIR)

clean:
	rm -rf $(OBJ)
