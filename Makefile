-include conf.mk

TECH ?=
SW ?= dummy

TOP = Soc
SBT = sbt --client
FIRTOOL = 0

SRC = $(shell find ./src/main/scala -name "*.scala")
TEST = $(shell find ./src/test/scala -name "*.scala")

MEM = sw/$(SW)/$(SW).mem

build: generated/$(TOP).v

check:
	$(SBT) compile

generated:
	mkdir -p generated

ifeq ($(FIRTOOL),0)
generated/$(TOP).v: $(SRC) generated
	$(SBT) run $(MEM)
else
generated/$(TOP).v: $(SRC) generated
	$(SBT) run $(MEM)
	firtool -o $@ generated/$(TOP).fir --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables --imconstprop --imdeadcodeelim --inline --dedup --preserve-values=none
endif

sw:
	$(MAKE) -C sw/$(SW)

test:
	$(MAKE) -C tests
	$(SBT) "testOnly *SocTest"

sim:
	$(MAKE) -C tests
	$(SBT) "testOnly *SocSim"

format:
	$(SBT) scalafmtAll

shutdown:
	$(SBT) shutdown

ifneq ($(TECH),)
include tech/$(TECH)/rules.mk
endif

clean:
	rm -rf generated
	$(MAKE) -C tests clean
	$(MAKE) -C sw/$(SW) clean
	$(BOARDCLEAN)

.PHONY: build check test clean sw
