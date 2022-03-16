PREFIX=riscv64-unknown-elf

RV_ROOT ?= /opt/riscv

CC=$(PREFIX)-gcc
AS=$(PREFIX)-as
LD=$(PREFIX)-ld
OBJCOPY=$(PREFIX)-objcopy
OBJDUMP=$(PREFIX)-objdump

LIBRV_ROOT ?= $(shell git rev-parse --show-toplevel)/sw/librv
INCLUDE=-I$(LIBRV_ROOT)
ARCH=rv32i

O ?= s

CFLAGS=-O$(O) $(INCLUDE) -g -Wall -Wno-unused-function -nostdlib -nostartfiles -ffreestanding -march=$(ARCH) -mabi=ilp32 -std=gnu99 -mcmodel=medany
ASFLAGS=-march=$(ARCH) -mabi=ilp32
LDFLAGS=-T $(LIBRV_ROOT)/memmap.ld -L$(RV_ROOT)/$(PREFIX)/lib/$(ARCH)/ilp32 -L$(RV_ROOT)/lib/gcc/$(PREFIX)/11.1.0/$(ARCH)/ilp32 -melf32lriscv
LDLIBS=-lgcc

LIBCSRC=$(wildcard $(LIBRV_ROOT)/*.c) $(wildcard $(LIBRV_ROOT)/libc/*.c)
LIBSSRC=$(wildcard $(LIBRV_ROOT)/*.s)
LIBOBJ=$(LIBCSRC:.c=.o) $(LIBSSRC:.s=.o)

SRC=$(wildcard *.c)
OBJ=$(SRC:.c=.o)

all: $(PROG).hex

install: $(PROG).bin
	bin2hex $< > $(LIBRV_ROOT)/../../mem/$(PROG).hex

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

