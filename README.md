# Riscinator 5000

The Riscinator 5000 is a simple RISC-V core implemented in Chisel. It is meant
for learning Chisel, and for experimenting with new Chisel features. The core
is a 3-stage pipeline implementing the rv32i unprivileged instruction set in
under 500 lines of code. The system as a whole also includes a simple bus and
arbiter, 16K of SRAM memory, and timer, UART, and GPIO modules. There is a
bare-metal C library with drivers for the devices on the system. An example
program in `sw/blink` blinks pin 0 using the timer.

There are synthesis scripts for instantiating the system on an OrangeCrab 25F
FPGA.

# Usage

Clone the repository:

```
git clone --recursive https://github.com/zyedidia/riscinator
```

Chisel uses [espresso](https://github.com/chipsalliance/espresso) for logic
minimization, so you must install it to build Riscinator. Run `mkdir build; cd
build; cmake ..; make` and then copy the built binary onto your PATH.

The makefile has the following targets:

* `make`/`make build`: generate FIRRTL and Verilog in `generated/`.
* `make synth`: use Yosys to synthesize for the OrangeCrab 25F.
* `make prog`: send the bitstream to the FPGA.
* `make test`: run the test suite.

The tests and example programs require you to have a RISC-V GNU toolchain
installed.

You can also use the MLIR-based FIRRTL compiler `firtool` from the
[CIRCT](https://github.com/llvm/circt) project by setting `FIRTOOL=1` in the
makefile. Note: you need to use a patched version of firtool that has support
for the memory initialization annotation. I distribute binaries with support
from [here](https://github.com/zyedidia/circt-builder). To download one with eget, use
`eget zyedidia/circt-builder -f firtool -a readmem`

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
    * In progress (currently there is limited support)
* Size and performance optimizations.
* Better testing.
* Cleaner Chisel implementation.

Some enhancements that I may or may not get to:

* M-extension and C-extension support.
* Optionally, more pipeline stages.
* More FPGA support (ULX3S, Upduino, Nexys A7, and more).

See also [riscv-mini](https://github.com/ucb-bar/riscv-mini).
