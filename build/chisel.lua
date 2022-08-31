local chisel = {}

function chisel.build(src, pkg, top, gen)
    local firflags := --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables
    local sbt = "sbt --client"
    return {
    $ $gen/%.fir: $src serve
        mkdir -p $gen
        $sbt runMain $pkg.$match

    $ $gen/Soc.fir: $src serve
        mkdir -p $gen
        $sbt runMain $pkg.Soc

    $ $gen/%.sv: $gen/%.fir
        firtool --verilog -o $output $input --annotation-file=$gen/$match.anno.json $firflags

    $ $gen/(.*)/(.*).sv:R: $gen/$$1.fir
        firtool --split-verilog -o $gen/$match1 $input --annotation-file=$gen/$match1.anno.json $firflags

    $ serve:VL:
        $sbt exit

    $ format:VB:
        $sbt scalafmtAll
    }
end

return chisel
