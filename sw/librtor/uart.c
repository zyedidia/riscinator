#include "uart.h"
#include "bits.h"
#include "mem.h"

#include "libc/tinyprintf.h"

typedef struct {
    unsigned tx_data;
    unsigned rx_data;
    unsigned dvsr;
    unsigned clear;
} uart_reg_t;

#define BIT_EMPTY 8
#define BIT_FULL 9
#define BIT_DATA_LOW 0
#define BIT_DATA_HIGH 7

static volatile uart_reg_t* const uart = (uart_reg_t*) 0x30000;

void uart_set_baud(int baud) {
    uint32_t dvsr = CLK_FREQ_MHZ*1000000 / 16 / baud - 1;
    put32(&uart->dvsr, dvsr);
}

int uart_rx_empty() {
    return bit_get(get32(&uart->rx_data), BIT_EMPTY);
}

int uart_tx_full() {
    return bit_get(get32(&uart->rx_data), BIT_FULL);
}

void uart_tx(uint8_t byte) {
    while (uart_tx_full()) {}
    put32(&uart->tx_data, (uint32_t) byte);
}

int uart_rx() {
    if (uart_rx_empty()) {
        return -1;
    }
    uint32_t data = bits_get(get32(&uart->rx_data), BIT_DATA_LOW, BIT_DATA_HIGH);
    put32(&uart->clear, 0);
    return (int) data;
}

void uart_putc(void* p, char c) {
    (void) p; // ignore
    uart_tx(c);
}

