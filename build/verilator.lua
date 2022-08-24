local verilator = {}

function verilator.build(tb, verilog, top, dep)
    local vdir = ".verilator"
    return {
    $ $vdir/tb: $tb $dep
        verilator --public -sv -cc -Mdir $vdir $verilog --top $top --exe --build $tb -o tb
    $ test:VB: $vdir/tb
        ./$input
    }
end

return verilator
