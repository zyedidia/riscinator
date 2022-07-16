module top
    (
        input wire clk,
        input wire rst_n,
        input wire usr_btn_i,
        output wire rgb_led0_r,
        output wire rgb_led0_g,
        output wire rgb_led0_b,
        output wire gpio_0,
        output wire gpio_1,
        output wire gpio_2,
        output wire gpio_3
    );

    logic locked;
    logic clk_sys;
    logic rst_sys_n;

    pll u_pll (
        .clkin   (clk),
        .clkout0 (clk_sys),
        .locked
    );

    logic [3:0] rst_cnt;
    logic       rst;

    // ensure reset release is synchronous with the clock
    always @(posedge clk or negedge locked)
        if (!locked)
            rst_cnt <= 4'h8;
        else if (rst_cnt[3])
            rst_cnt <= rst_cnt + 1;

    assign rst = rst_cnt[3];
    assign rst_sys_n = ~rst;

    logic led_r, led_g, led_b;

    assign rgb_led0_r = ~led_r;
    assign rgb_led0_g = ~led_g;
    assign rgb_led0_b = ~led_b;

    `TOP top (
        .clock (clk_sys),
        .reset (!rst_sys_n),
        .io_gpi_0 (usr_btn_i),
        .io_gpo_0 (led_r),
        .io_gpo_1 (led_g),
        .io_gpo_2 (led_b),
        .io_gpo_3 (gpio_0),
        .io_gpo_4 (gpio_1),
        .io_gpo_5 (gpio_2),
        .io_gpo_6 (gpio_3)
    );
endmodule
