main: addi x2, x0, 5
addi x3, x0, 12
addi x7, x3, -9
or x4, x7, x2
and x5, x3, x4
add x5, x5, x4
beq x5, x7, end
slt x4, x3, x4
beq x4, x0, around
addi x5, x0, 0
around: slt x4, x7, x2
add x7, x4, x5
sub x7, x7, x2
sw x7, 84(x3)
lw x2, 96(x0)
add x9, x2, x5
jal x3, end
addi x2, x0, 1
end: add x2, x2, x9
sw x2, 0x20(x3)
done: j done
