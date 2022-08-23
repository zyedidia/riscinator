local riscv = {}

function riscv.config(ldscript)
    local arch := rv32i
    local abi := ilp32
    local opt := s
    local ldarch := melf32lriscv
    return {
        flags = {
            cc := -O$opt -g -Wall -Wno-unused-function -nostdlib -nostartfiles -ffreestanding -march=$arch -mabi=$abi -std=gnu99 -mcmodel=medany
            as := -march=$arch -mabi=$abi
            ld := -T $ldscript -$ldarch
        },
        prefix := riscv64-unknown-elf-
    }
end

return riscv
