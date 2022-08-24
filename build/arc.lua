local arc = {}

function arc.build(tb, top, gen, dep, debug)
    local d = debug and "-d" or ""
    local bin = "tb-arc"

    local cxx = "g++"
    local arcinclude = import("knit").shell("arc-include")
    local cxxflags := -O3 -I$gen -I$arcinclude -Wall

    return {
    $ $gen/$top.o $gen/$top.h: $gen/$top.fir
        arc $d $input
    $ $bin.o: $tb $gen/$top.h $dep
        $cxx $cxxflags -c $(inputs[1]) -o $output
    $ $bin: $bin.o $gen/$top.o
        $cxx $cxxflags $input -o $output
    $ arc-test:VB: $bin
        ./$bin
    }
end

return arc
