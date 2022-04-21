SRCS=$(wildcard *.s)
PROGS=$(SRCS:.s=.mem)

all: $(PROGS)

%.mem: %.s
	rvasm -base 0 $< > $@

clean:
	rm -f *.mem

.PHONY: all clean
