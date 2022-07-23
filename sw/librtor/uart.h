#pragma once

#include <stdint.h>

#include "libc/tinyprintf.h"

void uart_set_baud(int baud);
int uart_rx_empty();
int uart_tx_full();
void uart_tx(uint8_t byte);
int uart_rx();

void uart_putc(void* p, char c);
