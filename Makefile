-include conf.mk

TECH ?=
SW ?= dummy

TOP = Soc
SBT = sbt --client
FIRTOOL ?= 1
VDIR = .verilator

SRC = $(shell find ./src/main/scala -name "*.scala")
TEST = $(shell find ./src/test/scala -name "*.scala")

MEM = sw/$(SW)/$(SW).mem

build: generated/$(TOP)

check:
	$(SBT) compile

$(VDIR):
	mkdir -p $(VDIR)

FIRFLAGS = --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables

generated/Core: $(SRC)
	mkdir -p generated
	$(SBT) runMain rtor.Core
	firtool --split-verilog -o $@ $@.fir --annotation-file=$@.anno.json $(FIRFLAGS)

generated/Soc: $(SRC)
	mkdir -p generated
	$(MAKE) -C sw/$(SW)
	$(SBT) runMain rtor.Soc $(MEM)
	firtool --split-verilog -o $@ $@.fir --annotation-file=$@.anno.json $(FIRFLAGS)

test: tests/tb.cc
	$(MAKE) -C tests
	verilator --public -sv -cc -Mdir $(VDIR) generated/Core/*.sv --top Core --exe --build $< -o tb
	./$(VDIR)/tb

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
	rm -rf generated test_run_dir
	$(MAKE) -C tests clean
	$(MAKE) -C sw/$(SW) clean
	$(BOARDCLEAN)

.PHONY: build check test clean
