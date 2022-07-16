package main

import (
	"bytes"
	"encoding/binary"
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"strings"
)

func bin2hex(data []byte) []string {
	hexdat := make([]string, 0)
	i := 0
	const sz = 4
	for i < len(data) {
		var ui uint32
		buf := bytes.NewReader(data[i:])
		binary.Read(buf, binary.LittleEndian, &ui)
		hexdat = append(hexdat, fmt.Sprintf("%08x", ui))
		i += int(buf.Size()) - buf.Len()
	}
	return hexdat
}

func dump(hexdat []string) {
	fmt.Println(strings.Join(hexdat, "\n"))
}

func main() {
	flag.Parse()
	args := flag.Args()

	for _, a := range args {
		data, err := ioutil.ReadFile(a)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			continue
		}
		hexdat := bin2hex(data)
		dump(hexdat)
	}
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}
