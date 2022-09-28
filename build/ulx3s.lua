local ulx3s = {}

function ulx3s.build(cons, wrap, top, verilog, dep, quiet)
    local report := report.json
    local q = quiet and "-q" or ""
    return {
    $ synth:V: $top.bit

    $ prog:V: $top.bit
        sudo fujprog $input

    $ $top.json: $wrap $dep
        yosys $q -p 'read_verilog -sv -DTOP=$top -noautowire $verilog $wrap; hierarchy -top top; synth_ecp5 -top top -json $output'

    $ $(top)_out.config $report: $cons $top.json
        nextpnr-ecp5 $q --detailed-timing-report --lpf-allow-unconstrained --report $report --85k --freq 25 --lpf $cons --package CABGA381 --textcfg $(top)_out.config --json $top.json

    $ $top.bit: $(top)_out.config
        ecppack --compress --freq 38.8 --input $input --bit $output

    $ report:VB: $report
        nextime -clk clk_sys -util $input
    }
end

return ulx3s
