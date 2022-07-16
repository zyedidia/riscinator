# Riscinator 5000

The Riscinator 5000 is a simple RISC-V core implemented in Chisel. It is meant
for learning Chisel, and for experimenting with new Chisel features. The core
is a 3-stage pipeline implementing the rv32i unprivileged instruction set in
under 500 lines of code. The system as a whole also includes a simple bus and
arbiter, 16K of SRAM memory, a timer, and a GPIO module. There is a bare-metal
C library with drivers for the devices on the system. An example program in
`sw/blink` blinks pin 0 using the timer.

There are synthesis scripts for instantiating the system on an OrangeCrab 25F
FPGA.

# Usage

Clone the repository:

```
git clone --recursive https://github.com/zyedidia/riscinator
```

The makefile has the following targets:

* `make`/`make build`: generate FIRRTL and Verilog in `generated/`.
* `make synth`: use Yosys to synthesize for the OrangeCrab 25F.
* `make prog`: send the bitstream to the FPGA.
* `make test`: run the test suite.

Some of these targets require you to install the tools in `tools/`:

* `bin2hex`: converts binary files to Verilog hex memory files.
* `rvasm`: simple RISC-V assembler.

The targets in `sw` also require you to have a RISC-V GNU toolchain installed.

You can also use the MLIR-based FIRRTL compiler `firtool` from the
[CIRCT](https://github.com/llvm/circt) project by setting `FIRTOOL=1` in the
makefile. Note: firtool does not currently support memory file loading.

By default the makefile generates the design to run an empty program. If you
have the appropriate tools installed, create a `conf.mk` file containing:

```
SW = blink
TECH = orangecrab
```

and run `make synth` to generate a blink program for the OrangeCrab FPGA.

# Future

The Riscinator 5000 is not complete yet, and I plan to add the following
enhancements:

* CSR and privileged ISA support (possibly including interrupts).
* A UART device.
* Size and performance optimizations.
* Better testing.
* Cleaner Chisel implementation.

Some enhancements that I may or may not get to:

* M-extension and C-extension support.
* Optionally, more pipeline stages.
* More FPGA support.

See also [riscv-mini](https://github.com/ucb-bar/riscv-mini).
