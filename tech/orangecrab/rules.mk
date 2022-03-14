CONS=tech/orangecrab/orangecrab.lpf
WRAP=tech/orangecrab/top.v
REPORT=report.json

BOARDCLEAN=rm -f *.dfu *.bit *.json *_out.config $(REPORT)

synth: $(TOP).dfu

prog: $(TOP).dfu
	sudo dfu-util -D $<

$(TOP).dfu: $(TOP).bit
	cp $< $@
	dfu-suffix -v 1209 -p 5af0 -a $@

$(TOP).json: generated/$(TOP).v
	yosys -p 'read_verilog -DTOP=$(TOP) -noautowire $< $(WRAP); hierarchy -top top; synth_ecp5 -top top -json $@'

$(TOP)_out.config $(REPORT) &: $(CONS) $(TOP).json
	nextpnr-ecp5 -q --lpf-allow-unconstrained --report $(REPORT) --25k --freq 48 --lpf $(CONS) --package CSFBGA285 --textcfg $(TOP)_out.config --json $(TOP).json

$(TOP).bit: $(TOP)_out.config
	ecppack --compress --freq 38.8 --input $< --bit $@

.PHONY: synth prog
