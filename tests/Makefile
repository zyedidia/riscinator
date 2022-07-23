PREFIX = riscv64-unknown-elf

CC = $(PREFIX)-gcc
AS = $(PREFIX)-as
LD = $(PREFIX)-ld
OBJCOPY = $(PREFIX)-objcopy
OBJDUMP = $(PREFIX)-objdump

ARCH = rv32i
LIBRTOR_ROOT = $(shell git rev-parse --show-toplevel)/sw/librtor
ASFLAGS = -march=$(ARCH) -mabi=ilp32
LDFLAGS = -T $(LIBRTOR_ROOT)/memmap.ld -melf32lriscv

SRCS = $(wildcard *.s)
PROGS = $(SRCS:.s=.mem)

all: $(PROGS)

%.o: %.s
	$(AS) $(ASFLAGS) $< -c -o $@

%.elf: %.o
	$(LD) $(LDFLAGS) $< -o $@

%.bin: %.elf
	$(OBJCOPY) $< -O binary $@

%.mem: %.bin
	hexdump -v -e '1/4 "%08x\n"' $< > $@

# %.mem: %.elf
# 	$(OBJCOPY) $< -O verilog $@

%.list: %.elf
	$(OBJDUMP) -D $< > $@

clean:
	rm -f *.mem

.PHONY: all clean
