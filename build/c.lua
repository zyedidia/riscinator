local c = {}

function c.toolchain(prefix)
    local prefix = prefix or ""
    return {
        cc := $(prefix)gcc
        as := $(prefix)as
        ld := $(prefix)ld
        objcopy := $(prefix)objcopy
        objdump := $(prefix)objdump
    }
end

function c.rules(tools, flags)
    return {
    $ %.o: %.c
        $(tools.cc) $(flags.cc) -c $input -o $output
    $ %.o: %.s
        $(tools.as) $(flags.as) -c $input -o $output
    $ %.bin: %.elf
        $(tools.objcopy) $input -O binary $output
    $ %.list: %.elf
        $(tools.objdump) -D $input > $output
    $ %.mem: %.bin
        hexdump -v -e '1/4 "%08x\n"' $input > $output
    $ %_mem.h: %.bin
        xxd --include $input > $output
    }
end

function c.libgcc(cc, cflags)
    local knit = import("knit")
    return knit.shell(f"$cc $cflags --print-file-name=libgcc.a")
end

return c
