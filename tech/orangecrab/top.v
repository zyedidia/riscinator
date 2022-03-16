module top
	(
		input wire clk,
		input wire rst_n,
		input wire usr_btn_i,
		output wire rgb_led0_r,
		output wire rgb_led0_g,
		output wire rgb_led0_b
	);

	assign rgb_led0_g = 1'b1;
	assign rgb_led0_b = 1'b1;

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

	`TOP top (
		.clock (clk_sys),
		.reset (!rst_sys_n),
		.io_gpi_0 (usr_btn_i),
		.io_gpo_0 (rgb_led0_r)
	);
endmodule
