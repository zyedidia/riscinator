CONS=tech/orangecrab/orangecrab.lpf
WRAP=tech/orangecrab/top.v tech/orangecrab/pll.v
REPORT=report.json

VSRC = $(wildcard generated/*.v)

BOARDCLEAN=rm -f *.dfu *.bit *.json *_out.config $(REPORT)

synth: $(TOP).dfu

prog: $(TOP).dfu
	sudo dfu-util -D $<

$(TOP).dfu: $(TOP).bit
	cp $< $@
	dfu-suffix -v 1209 -p 5af0 -a $@

$(TOP).json: $(VSRC) $(WRAP)
	yosys -q -p 'read_verilog -sv -DTOP=$(TOP) -noautowire $^; hierarchy -top top; synth_ecp5 -top top -json $@'

$(TOP)_out.config $(REPORT) &: $(CONS) $(TOP).json
	nextpnr-ecp5 -q --detailed-timing-report --lpf-allow-unconstrained --report $(REPORT) --25k --freq 48 --lpf $(CONS) --package CSFBGA285 --textcfg $(TOP)_out.config --json $(TOP).json

$(TOP).bit: $(TOP)_out.config
	ecppack --compress --freq 38.8 --input $< --bit $@

report: $(REPORT)
	nextime -clk clk_sys -util $(REPORT)

.PHONY: synth prog report
