local oc = {}

function oc.build(cons, wrap, top, verilog, dep, quiet)
    local report := report.json
    local q = quiet and "-q" or ""
    return {
    $ synth:V: $top.dfu

    $ prog:VB: $top.dfu
        sudo dfu-util -D $input

    $ $top.dfu: $top.bit
        cp $input $output
        dfu-suffix -v 1209 -p 5af0 -a $output

    $ $top.json: $wrap $dep
        yosys $q -p 'read_verilog -sv -DTOP=$top -noautowire $verilog $wrap; hierarchy -top top; synth_ecp5 -top top -json $output'

    $ $(top)_out.config $report: $cons $top.json
        nextpnr-ecp5 $q --detailed-timing-report --lpf-allow-unconstrained --report $report --25k --freq 48 --lpf $cons --package CSFBGA285 --textcfg $(top)_out.config --json $top.json

    $ $top.bit: $(top)_out.config
        ecppack --compress --freq 38.8 --input $input --bit $output

    $ report:VB: $report
        nextime -clk clk_sys -util $input
    }
end

return oc
