# Riscinator

Riscinator is a simple RISC-V core implemented in Chisel. It is meant for
learning Chisel, and for experimenting with new Chisel features. The core is a
3-stage pipeline implementing the rv32i unprivileged instruction set in under
1000 lines of code. The system as a whole also includes a simple bus and
arbiter, 16K of SRAM memory, and timer, UART, and GPIO modules. There is a
bare-metal C library with drivers for the devices on the system. An example
program in `sw/blink` blinks pin 0 using the timer.

There are synthesis scripts for instantiating the system on an OrangeCrab 25F,
or ULX3S 85F.

# Usage

## Dependencies

Install the dependencies:

* [knit](https://github.com/zyedidia/knit): `eget --pre-release zyedidia/knit`.
* [chisel](https://github.com/chipsalliance/chisel3).
* [espresso](https://github.com/chipsalliance/espresso): `eget chipsalliance/espresso`.
* [firtool](https://github.com/llvm/circt): `eget llvm/circt -f firtool --pre-release`.
* [riscv-gnu-toolchain](https://github.com/riscv-collab/riscv-gnu-toolchain): download from [riscv-gnu-toolchain-prebuilt](https://github.com/zyedidia/riscv-gnu-toolchain-prebuilt).

Optional dependencies:

* [verilator](https://github.com/verilator/verilator): for simulation.
* [oss-cad-suite](https://github.com/YosysHQ/oss-cad-suite-build): for FPGA synthesis.

## Building

Clone the repository:

```
git clone https://github.com/zyedidia/riscinator
```

Riscinator uses [knit](https://github.com/zyedidia/knit) to build.

The Knitfile has the following targets:

* `knit`/`knit build`: generate FIRRTL and Verilog in `generated/`.
* `knit synth`: use Yosys to synthesize for the OrangeCrab 25F.
* `knit prog`: send the bitstream to the FPGA.
* `knit test`: run the test suite.

The tests and example programs require you to have a RISC-V GNU toolchain
installed.

You must also have the MLIR-based FIRRTL compiler `firtool` installed. You can
install it from the prebuilt releases at
[llvm/circt](https://github.com/llvm/circt). Eget can automatically install it
for you with `eget llvm/circt -f firtool`.

By default the Knitfile sets up a design that runs the `blink` program. You can
configure it to use a different program from the `sw` directory with `knit
build prog=...`. If you have the appropriate tools installed, you can also
synthesize for the OrangeCrab ECP5 25F FPGA by running `knit synth
tech=orangecrab`. Use `tech=ulx3s` to sythesize for the ULX3S 85F FPGA.

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
* More FPGA support (Upduino, Nexys A7, and more).

See also [riscv-mini](https://github.com/ucb-bar/riscv-mini).
