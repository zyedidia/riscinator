# Riscinator

Riscinator is a simple RISC-V core implemented in Chisel. It is meant for
learning Chisel, and for experimenting with new Chisel features. The core is a
3-stage pipeline implementing the rv32i unprivileged instruction set in under
1000 lines of code. The system as a whole also includes a simple bus and
arbiter, 16K of SRAM memory, and timer, UART, and GPIO modules. There is a
bare-metal C library with drivers for the devices on the system. An example
program in `sw/blink` blinks pin 0 using the timer.

There are synthesis scripts for instantiating the system on an OrangeCrab 25F
FPGA.

# Usage

Clone the repository:

```
git clone https://github.com/zyedidia/riscinator
```

The makefile has the following targets:

* `make`/`make build`: generate FIRRTL and Verilog in `generated/`.
* `make synth`: use Yosys to synthesize for the OrangeCrab 25F.
* `make prog`: send the bitstream to the FPGA.
* `make test`: run the test suite.

The tests and example programs require you to have a RISC-V GNU toolchain
installed.

You must also have the MLIR-based FIRRTL compiler `firtool` installed. You can
install it from the prebuilt releases at
[llvm/circt](https://github.com/llvm/circt). Eget can automatically install it
for you with `eget llvm/circt -f firtool`.

By default the makefile generates the design to run an empty program. If you
have the appropriate tools installed, create a `conf.mk` file containing:

```
SW = blink
TECH = orangecrab
```

and run `make synth` to generate a blink program for the OrangeCrab FPGA.

# Future

The Riscinator is not complete yet, and I plan to add the following
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
