local verilator = {}

function verilator.tb(tb, verilog, top, dep)
    local vdir = ".verilator"
    return r{
    $ $vdir/tb: $tb $dep
        verilator --public -sv -cc -Mdir $vdir $verilog --top $top --exe --build $tb -o tb
    $ test:VB: $vdir/tb
        ./$input
    }
end

function verilator.sim(sim, verilog, top, dep)
    local vdir = ".verilator"
    return r{
    $ tests/rtor_mem.h: tests/rtor.bin
        cd tests; xxd --include rtor.bin > rtor_mem.h
    $ $vdir/sim: $sim $dep
        verilator --public -sv -cc -Mdir $vdir $verilog --top $top --exe --trace --build $sim -o sim
    $ sim:VB: $vdir/sim
        ./$input
    }
end

return verilator
