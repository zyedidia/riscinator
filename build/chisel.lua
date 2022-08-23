local chisel = {}

function chisel.build(src, sbt, pkg, top, gen, mem)
    local firflags := --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables
    return {
    $ $gen/%.fir: $src
        mkdir -p $gen
        $sbt runMain $pkg.$match

    $ $gen/Soc.fir: $src $mem
        mkdir -p $gen
        $sbt runMain $pkg.Soc $mem

    $ $gen/(.*)/(.*).sv:R: $gen/$$1.fir
        firtool --split-verilog -o $gen/$match1 $input --annotation-file=$gen/$match1.anno.json $firflags
    }
end

function chisel.format(sbt, gen)
    return {
    $ format:VB:
        $sbt scalafmtAll
    }
end

function chisel.clean(gen)
    return {
    $ clean:VB:
        rm -rf $gen test_run_dir
    }
end

return chisel
