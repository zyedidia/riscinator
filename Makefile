TOP ?= Cpu
TECH ?= orangecrab

SBT=sbt --client

SRC=$(shell find ./src/main/scala -name "*.scala")
TEST=$(shell find ./src/test/scala -name "*.scala")

build: generated/$(TOP).v

generated/$(TOP).v: $(SRC)
	$(SBT) run

test: $(SRC) $(TEST)
	$(SBT) test

sim: $(SRC) $(TEST)
	$(SBT) "testOnly test.$(TOP)SimTest"

include tech/$(TECH)/rules.mk

clean:
	rm -rf generated
	$(BOARDCLEAN)

