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

generated:
	mkdir -p generated

$(VDIR):
	mkdir -p $(VDIR)

FIRFLAGS = --annotation-file=generated/$(TOP).anno.json --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables

generated/$(TOP): $(SRC) generated
	$(MAKE) -C sw/$(SW)
	$(SBT) runMain rtor.$(TOP) $(MEM)
	firtool --split-verilog -o generated/$(TOP) generated/$(TOP).fir $(FIRFLAGS)

$(VDIR)/tb: tests/tb.cc generated/$(TOP) $(VDIR)
	verilator --public -sv -cc -Mdir $(VDIR) generated/$(TOP)/*.sv --top $(TOP) --exe --build $< -o tb

test: $(VDIR)/tb
	$(MAKE) -C tests
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
