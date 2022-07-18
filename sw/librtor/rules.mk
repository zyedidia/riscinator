PREFIX = riscv64-unknown-elf

CC = $(PREFIX)-gcc
AS = $(PREFIX)-as
LD = $(PREFIX)-ld
OBJCOPY = $(PREFIX)-objcopy
OBJDUMP = $(PREFIX)-objdump

LIBRTOR_ROOT = $(shell git rev-parse --show-toplevel)/sw/librtor
INCLUDE = -I$(LIBRTOR_ROOT)
ARCH = rv32i

O ?= s

CFLAGS = -O$(O) $(INCLUDE) -g -Wall -Wno-unused-function -nostdlib -nostartfiles -ffreestanding -march=$(ARCH) -mabi=ilp32 -std=gnu99 -mcmodel=medany
ASFLAGS = -march=$(ARCH) -mabi=ilp32
LDFLAGS = -T $(LIBRTOR_ROOT)/memmap.ld -melf32lriscv
LIBGCC = $(shell $(CC) $(CFLAGS) --print-file-name=libgcc.a)
LDLIBS = $(LIBGCC)

LIBCSRC = $(wildcard $(LIBRTOR_ROOT)/*.c) $(wildcard $(LIBRTOR_ROOT)/libc/*.c)
LIBSSRC = $(wildcard $(LIBRTOR_ROOT)/*.s)
LIBOBJ = $(LIBCSRC:.c=.o) $(LIBSSRC:.s=.o)

SRC = $(wildcard *.c)
OBJ = $(SRC:.c=.o)

all: $(PROG).hex $(PROG).mem

$(PROG).mem: $(PROG).bin
	bin2hex $< > $(PROG).mem

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.s
	$(AS) $(ASFLAGS) $< -c -o $@

$(PROG).elf: $(OBJ) $(LIBOBJ)
	$(LD) $(LDFLAGS) $(LIBOBJ) $(OBJ) $(LDLIBS) -o $@

%.bin: %.elf
	$(OBJCOPY) $< -O binary $@

%.hex: %.elf
	$(OBJCOPY) $< -O ihex $@

%.list: %.elf
	$(OBJDUMP) -D $< > $@

clean:
	rm -f *.o *.elf *.hex *.bin *.list

.PHONY: all install clean

