.section ".text.boot"

.globl _start
_start:
addi x1, x0, 42
csrw mscratch, x1
csrr x2, mscratch
done: j done
