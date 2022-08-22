local chisel = {}

function chisel.build(src, sbt, main, top, gen, args)
    local firflags := --disable-annotation-unknown -O=release --lowering-options=noAlwaysComb,disallowPackedArrays,disallowLocalVariables
    return {
    $ $gen/$top.fir: $src
        mkdir -p $gen
        $sbt runMain $main $args

    $ $gen/$top/$top.sv: $gen/$top.fir
        firtool --split-verilog -o $gen/$top $input --annotation-file=$gen/$top.anno.json $firflags
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
