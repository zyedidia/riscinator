TOP=Soc
SBT=sbt --client

SRC=$(shell find ./src/main/scala -name "*.scala")
TEST=$(shell find ./src/test/scala -name "*.scala")

build: generated/$(TOP).v

check:
	$(SBT) compile

generated:
	mkdir -p generated

generated/$(TOP).v: $(SRC) generated
	$(SBT) run

test:
	$(SBT) "Test / test"

format:
	$(SBT) scalafmtAll

clean:
	rm -rf generated

.PHONY: build check test clean
