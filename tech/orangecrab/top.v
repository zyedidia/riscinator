module top
	(
		input wire clk,
		input wire rst_n,
		input wire usr_btn_i,
		output wire rgb_led0_r
	);

	`TOP top (
		.clock (clk),
		.reset (!rst_n),
		.io_gpi_0 (usr_btn_i),
		.io_gpo_0 (rgb_led0_r)
	);
endmodule
