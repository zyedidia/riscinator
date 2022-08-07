#include <stdlib.h>
#include "VCore.h"
#include "verilated.h"

int main(int argc, char **argv) {
    Verilated::commandArgs(argc, argv);

    VCore* dut = new VCore;

    return 0;
}
