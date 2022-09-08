local arc = {}

function arc.build(tb, top, gen, dep, debug, rvsym)
    local d = debug and "-d" or ""
    local bin = "tb-arc"

    local cxx = "g++"
    local arcinclude = import("knit").shell("arc-include")
    local rvsyminclude = "/home/zyedidia/programming/rvsym/include"
    local cxxflags := -O2 -I$gen -I$arcinclude -Wall
    local target = "x86_64"
    if rvsym then
        cxx = "riscv64-unknown-elf-g++"
        cxxflags := -O2 -I$gen -I$arcinclude -I$rvsyminclude -Wall -march=rv32i -mabi=ilp32 -mcmodel=medany
        target = "riscv32"
    end

    return {
    $ $gen/$top.o $gen/$top.h: $gen/$top.fir
        arc-steps $input $target
    $ $bin.o: $tb $gen/$top.h $dep
        $cxx $cxxflags -c $(inputs[1]) -o $output
    $ $bin: $bin.o $gen/$top.o
        $cxx $cxxflags $input -o $output
    $ arc-test:VB: $bin
        ./$bin
    }
end

return arc
