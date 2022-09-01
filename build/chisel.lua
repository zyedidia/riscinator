local chisel = {}

function chisel.build(src, pkg, top, gen)
    local firflags := --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables
    local sbt = "sbt --client"
    return {
    $ $gen/%: $gen/%.fir
        firtool --split-verilog -o $gen/$match $input --annotation-file=$gen/$match.anno.json $firflags

    $ $gen/%.fir: $src serve gen
        mkdir -p $gen
        $sbt runMain $pkg.$match

    $ $gen/Soc.fir: $src serve gen
        mkdir -p $gen
        $sbt runMain $pkg.Soc

    $ $gen/%.sv: $gen/%.fir
        firtool --verilog -o $output $input --annotation-file=$gen/$match.anno.json $firflags

    $ serve:VL:
        $sbt exit

    $ gen:VL:
        mkdir -p $gen

    $ format:VB:
        $sbt scalafmtAll
    }
end

return chisel
