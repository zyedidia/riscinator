TOP = Soc
SBT = sbt --client
MEM = sw/blink/blink.mem
TECH = orangecrab
FIRTOOL = 0

SRC = $(shell find ./src/main/scala -name "*.scala")
TEST = $(shell find ./src/test/scala -name "*.scala")

build: generated/$(TOP).v

check:
	$(SBT) compile

generated:
	mkdir -p generated

ifeq ($(FIRTOOL),0)
generated/$(TOP).v: $(SRC) generated $(MEM)
	$(SBT) run $(MEM)
else
generated/$(TOP).v: $(SRC) generated $(MEM)
	$(SBT) run $(MEM)
	firtool -o $@ generated/$(TOP).fir --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables --imconstprop --imdeadcodeelim --inline --dedup --preserve-values=none
endif

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

include tech/$(TECH)/rules.mk

clean:
	rm -rf generated
	$(BOARDCLEAN)

.PHONY: build check test clean
