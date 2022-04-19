`define CLK_MHZ 12

module top
    (
        input wire gpio_2,
        input wire gpio_28,

        output wire led_red,
        output wire led_green,
        output wire led_blue,

        output wire gpio_23,
        output wire gpio_25,
        output wire gpio_26,
        output wire gpio_27,

        output wire spi_cs
    );

    assign spi_cs = 1'b1;

    wire clk_sys;
    SB_HFOSC u_SB_HFOSC (.CLKHFPU(1'b1), .CLKHFEN(1'b1), .CLKHF(clk_sys));

    wire led_r, led_g, led_b;

    `TOP top (
        .clock (clk_sys),
        .reset (gpio_2),
        .io_gpi_0 (gpio_28),
        .io_gpo_0 (led_r),
        .io_gpo_1 (led_g),
        .io_gpo_2 (led_b),
        .io_gpo_3 (gpio_23),
        .io_gpo_4 (gpio_25),
        .io_gpo_5 (gpio_26),
        .io_gpo_6 (gpio_27)
    );

    SB_RGBA_DRV rgb (
        .RGBLEDEN (1'b1),
        .RGB0PWM  (led_g),
        .RGB1PWM  (led_b),
        .RGB2PWM  (led_r),
        .CURREN   (1'b1),
        .RGB0     (led_green),
        .RGB1     (led_blue),
        .RGB2     (led_red)
    );
    defparam rgb.CURRENT_MODE = "0b1";
    defparam rgb.RGB0_CURRENT = "0b000001";
    defparam rgb.RGB1_CURRENT = "0b000001";
    defparam rgb.RGB2_CURRENT = "0b000001";
endmodule
