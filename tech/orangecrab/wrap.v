module wrap
    (
        input wire clk,
        input wire rst_n,
        input wire rx,
        output wire tx
    );

    `TOP top (
        .clock (clk),
        .reset (!rst_n),
        .io_rx (rx),
        .io_tx (tx)
    );
endmodule
