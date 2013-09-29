package com.diesal11.gameboy;

import java.io.IOException;


class Emulator {
	public static final void main(String[] args) {
		Z80 cpu = new Z80("Pokemon Blue.gb");
		TestSuite t = new TestSuite(cpu);

		if (t.diagnose(true) == 0) {
			cpu.reset();
			while (cpu.exception() == 0 && cpu.cycles() < 6) {
				cpu.nextinstruction();
			}
		}
	}
}