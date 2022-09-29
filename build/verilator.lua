local verilator = {}

function verilator.tb(tb, verilog, top, dep)
    local vdir = ".verilator"
    return {
    $ $vdir/tb: $tb $dep generated/$top
        verilator --public -sv -cc -Mdir $vdir $verilog --top $top --exe --build $tb -o tb
    $ test:VB: $vdir/tb
        ./$input
    }
end

function verilator.sim(sim, verilog, top, dep)
    local vdir = ".verilator"
    return {
    $ tests/rtor_mem.h: tests/rtor.bin
        cd tests; xxd --include rtor.bin > rtor_mem.h
    $ $vdir/sim: $sim $dep generated/$top
        verilator --public -sv -cc -Mdir $vdir $verilog --top $top --exe --trace --build $sim -o sim
    $ sim:VB: $vdir/sim
        ./$input
    }
end

return verilator
