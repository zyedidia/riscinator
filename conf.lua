return {
    prog = cli.prog or "blink",
    quiet = tobool(cli.quiet) or true,
    top = cli.top or "Soc",
    tech = cli.tech or "ulx3s",
    cmdebug = tobool(cli.cmdebug) or false,
}
