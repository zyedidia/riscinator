module top
    (
        input wire clk,
        input wire rst_n,
        output wire rgb_led0_r,
    );

    `TOP top (
        .clock (clk),
        .reset (!rst_n),
        .io_led (rgb_led0_r)
    );
endmodule
