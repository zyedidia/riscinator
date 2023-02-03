local chisel = {}

function chisel.build(src, pkg, top, gen)
    local firflags := --dedup --disable-annotation-unknown --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables
    local sbt = "sbt --client"
    return r{
    $ $gen/%: $gen/%.fir
        firtool --split-verilog -o $gen/$match $input --annotation-file=$gen/$match.anno.json $firflags

    $ $gen/CoreSingle.fir: $src serve gen
        $sbt runMain $pkg.CoreSingle

    $ $gen/Soc.fir: $src serve gen
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
