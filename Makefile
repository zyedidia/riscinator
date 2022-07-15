TOP=Soc
SBT=sbt --client
MEM=tests/riscvtest.hex

SRC=$(shell find ./src/main/scala -name "*.scala")
TEST=$(shell find ./src/test/scala -name "*.scala")

build: generated/$(TOP).v

firtool: generated/$(TOP).mfc.v

check:
	$(SBT) compile

generated:
	mkdir -p generated

generated/$(TOP).v generated/$(TOP).fir: $(SRC) generated
	$(SBT) run $(MEM)

generated/$(TOP).mfc.v: generated/$(TOP).fir
	firtool -o $@ $< --disable-annotation-unknown --annotation-file=generated/$(TOP).anno.json --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables --imconstprop --imdeadcodeelim --inline --dedup --preserve-values=none

test:
	$(MAKE) -C tests
	$(SBT) "Test / test"

format:
	$(SBT) scalafmtAll

clean:
	rm -rf generated

.PHONY: build check test clean
