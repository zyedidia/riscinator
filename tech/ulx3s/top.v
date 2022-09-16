module top
    (
        input wire clk_25mhz,
        input wire [1:0] btn,
        input wire ftdi_txd,
        output wire ftdi_rxd,
        output wire [6:0] led
    );

    `TOP top (
        .clock    (clk_25mhz),
        .reset    (1'b0),
        .io_rx    (ftdi_txd), // confusingly backwards
        .io_tx    (ftdi_rxd),
        .io_gpi_0 (btn[1]),
        .io_gpo_0 (led[0]),
        .io_gpo_1 (led[1]),
        .io_gpo_2 (led[2]),
        .io_gpo_3 (led[3]),
        .io_gpo_4 (led[4]),
        .io_gpo_5 (led[5]),
        .io_gpo_6 (led[6])
    );
endmodule
