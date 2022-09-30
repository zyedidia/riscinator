module top
    (
        input wire clk_25mhz,
        input wire [1:0] btn,
        input wire ftdi_txd,
        output wire ftdi_rxd,
        output wire [6:0] led
    );

    logic locked;
    logic clk_sys;
    logic rst_sys_n;

    pll u_pll (
        .clkin   (clk_25mhz),
        .clkout0 (clk_sys),
        .locked
    );

    logic [3:0] rst_cnt;
    logic       rst;

    // ensure reset release is synchronous with the clock
    always @(posedge clk_25mhz or negedge locked)
        if (!locked)
            rst_cnt <= 4'h8;
        else if (rst_cnt[3])
            rst_cnt <= rst_cnt + 1;

    assign rst = rst_cnt[3];
    assign rst_sys_n = ~rst;

    `TOP top (
        .clock    (clk_sys),
        .reset    (!rst_sys_n),
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
