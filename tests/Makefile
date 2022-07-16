SRCS=$(wildcard *.s)
PROGS=$(SRCS:.s=.mem)

all: $(PROGS)

%.mem: %.s
	rvasm -base 1048576 $< > $@

clean:
	rm -f *.mem

.PHONY: all clean
