local sw = {}

function sw.build(prog)
    local knit = import("knit")
    local csrc = knit.join(knit.rglob("sw/librtor", "*.c"), knit.rglob(f"sw/$prog", "*.c"))
    local ssrc = knit.join(knit.rglob("sw/librtor", "*.s"), knit.rglob(f"sw/$prog", "*.s"))
    local obj = knit.join(knit.extrepl(csrc, ".c", ".o"), knit.extrepl(ssrc, ".s", ".o"))

    local rvcfg = riscv.config("sw/librtor/memmap.ld")
    rvcfg.flags.cc = rvcfg.flags.cc .. " -Isw/librtor"
    local rvtools = c.toolchain(rvcfg.prefix)
    local crules = c.rules(rvtools, rvcfg.flags)
    local libgcc = c.libgcc(rvtools.cc, rvcfg.flags.cc)

    return r({
    $ sw/$prog/$prog.elf: $obj
        $(rvtools.ld) $(rvcfg.flags.ld) $input $libgcc -o $output
    }, crules)
end

return sw
