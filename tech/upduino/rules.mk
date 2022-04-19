CONS=tech/upduino/upduino.pcf
WRAP=tech/upduino/top.v
REPORT=report.json

BOARDCLEAN=rm -f *.bin *.json *.asc $(REPORT)

synth: $(TOP).bin

prog: $(TOP).bin
	sudo iceprog $<

$(TOP).json: $(WRAP) generated/$(TOP).v
	yosys -p 'read_verilog -defer -sv -DTOP=$(TOP) -noautowire $^; hierarchy -top top; synth_ice40 -abc2 -top top -json $@'

$(TOP).asc $(REPORT) &: $(CONS) $(TOP).json
	nextpnr-ice40 --report $(REPORT) --up5k --pcf $(CONS) --package sg48 --asc $(TOP).asc --json $(TOP).json

$(TOP).bin: $(TOP).asc
	icepack $< $@

.PHONY: synth prog
