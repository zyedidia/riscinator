SRCS=$(wildcard *.s)
PROGS=$(SRCS:.s=.hex)

all: $(PROGS)

%.hex: %.s
	rvasm -base 0 $< > $@

clean:
	rm -f *.hex

.PHONY: all clean
