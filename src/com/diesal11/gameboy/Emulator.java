package com.diesal11.gameboy;

class Emulator {
	public static final void main(String[] args) {
		Cartridge cartridge = new Cartridge("Pokemon Blue.gb");
		if(cartridge.getError() != null) {
			System.out.println("ERROR: "+cartridge.getError());
		}
		else {
			System.out.println("Succesfully loaded ROM! :D");
			Z80 cpu = new Z80(cartridge);
			TestSuite t = new TestSuite(cpu);
			if(true) {
				if (t.diagnose(true) == 0) {
					cpu.reset();
					while (cpu.exception() == 0) {
						cpu.nextinstruction();
					}
				}
			}
		}
	}
}