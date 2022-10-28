local sw = {}

function sw.build(prog, clkmhz, cmdebug)
    local knit = require("knit")
    local csrc = knit.rglob("sw/librtor", "*.c") + knit.rglob(f"sw/$prog", "*.c")
    local ssrc = knit.rglob("sw/librtor", "*.s") + knit.rglob(f"sw/$prog", "*.s")
    local obj = knit.extrepl(csrc, ".c", ".o") + knit.extrepl(ssrc, ".s", ".o")

    local rvcfg = riscv.config("sw/librtor/memmap.ld", clkmhz)
    rvcfg.flags.cc = rvcfg.flags.cc .. " -Isw/librtor" .. f" -DCLK_FREQ_MHZ=$clkmhz"
    local rvtools = c.toolchain(rvcfg.prefix)
    local crules = c.rules(rvtools, rvcfg.flags)
    local libgcc = c.libgcc(rvtools.cc, rvcfg.flags.cc)

    cmflag = ""
    if cmdebug then
        cmflag = "XCFLAGS=\"-DCORE_DEBUG=1\""
    end

    return r({
    $ sw/$prog/$prog.elf: $obj
        $(rvtools.ld) $(rvcfg.flags.ld) $input $libgcc -o $output
    $ sw/coremark/coremark.elf: sw/coremark
        make CLK_MHZ=$clkmhz -C sw/coremark PORT_DIR=riscinator $cmflag -B
    }, crules)
end

return sw
