.section ".text.boot"

.globl _start
_start:
	la sp, 0x108000
	call _cstart
_halt:
	j _halt
