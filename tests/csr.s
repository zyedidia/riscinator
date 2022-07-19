.section ".text.boot"

.globl _start
_start:
addi x1, x1, 1
csrw mscratch, x1
csrr x2, mscratch
ecall
done: j done
