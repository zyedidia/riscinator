TOP ?= Soc
TECH ?= orangecrab

SBT=sbt --client

SRC=$(shell find ./src/main/scala -name "*.scala")
TEST=$(shell find ./src/test/scala -name "*.scala")

build: generated/$(TOP).v

check: $(SRC) $(TEST)
	$(SBT) compile

generated/$(TOP).v: $(SRC)
	$(SBT) run

test: $(SRC) $(TEST)
	$(SBT) "testOnly test.core.CoreTest"

sim: $(SRC) $(TEST)
	$(SBT) "testOnly test.$(TOP)Sim"

include tech/$(TECH)/rules.mk

clean:
	rm -rf generated
	$(BOARDCLEAN)

