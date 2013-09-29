package com.diesal11.gameboy;

import java.io.IOException;

public class Z80 {
	protected static final int CARRY8b = 512;
	protected static final int CARRY8b_SHR = 5;

	// FLAGS
	protected static final int FLAG_REG = 5;
	protected static final int ZF_Shift = 7;
	protected static final int NF_Shift = ZF_Shift - 1;
	protected static final int HC_Shift = NF_Shift - 1;
	protected static final int CF_Shift = HC_Shift - 1;
	protected static final int ZF_Mask = 1 << ZF_Shift;
	protected static final int NF_Mask = 1 << NF_Shift;
	protected static final int HC_Mask = 1 << HC_Shift;
	protected static final int CF_Mask = 1 << CF_Shift;

	protected int TotalInstrCount = 0;

	protected int[] regs = new int[8]; // [A,B,C,D,E,F,H,L]
	protected static final int A = 0;
	protected static final int B = 1;
	protected static final int C = 2;
	protected static final int D = 3;
	protected static final int E = 4;
	protected static final int F = FLAG_REG;
	protected static final int H = 6;
	protected static final int L = 7;

	protected int PC;

	// CPU Class variables
	private Cartridge cardridge;
	private int lastException = 0;

	public Z80(String filename) {
		cardridge = new Cartridge(filename);
		if (cardridge.getError() != null) {
			System.out.println("ERROR: " + cardridge.getError());
		} else {
			System.out.println("Succesfully loaded ROM :)");
		}
		reset();
	}

	public void reset() {
		// TODO: Switch to bank 0
		PC = 0x100; // ROM Entry point on bank 0
		for (int i = 0; i < 8; ++i)
			regs[i] = 0;
		TotalInstrCount = 0;
	}

	protected int cycles() {
		return TotalInstrCount;
	}

	private String disassembleinstruction() {
		int instr = cardridge.read(PC);
		String s = String.format("$%02x\t", instr);
		// TODO take count of BC
		s += Disassembler.disassemble(instr, false);
		;
		return s;
	}

	protected void printCPUstatus() {
		String flags = "";
		flags += ((regs[FLAG_REG] & ZF_Mask) == ZF_Mask) ? "Z " : "z ";
		flags += ((regs[FLAG_REG] & NF_Mask) == NF_Mask) ? "N " : "n ";
		flags += ((regs[FLAG_REG] & HC_Mask) == HC_Mask) ? "H " : "h ";
		flags += ((regs[FLAG_REG] & CF_Mask) == CF_Mask) ? "C " : "c ";
		flags += ((regs[FLAG_REG] & (1 << 3)) == (1 << 3)) ? "1 " : "0 ";
		flags += ((regs[FLAG_REG] & (1 << 2)) == (1 << 2)) ? "1 " : "0 ";
		flags += ((regs[FLAG_REG] & (1 << 1)) == (1 << 1)) ? "1 " : "0 ";
		flags += ((regs[FLAG_REG] & (1 << 0)) == (1 << 0)) ? "1 " : "0 ";
		System.out.println("---CPU Status for cycle " + TotalInstrCount + "---");
		System.out.printf("   A=$%02x    B=$%02x    C=$%02x    D=$%02x   E=$%02x   F=$%02x\n", regs[A], regs[B], regs[C], regs[D], regs[E], regs[F]);
		System.out.printf("  PC=$%04x  H=$%04x  L=$%04x  flags=" + flags + "\n", PC, regs[H], regs[L]);
		System.out.printf("  $%04x %s\n", PC, disassembleinstruction());
	}

	protected void inc8b(int reg_index) {
		// Clear & Set HC
		regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
		regs[FLAG_REG] = regs[FLAG_REG] | ((((regs[reg_index] & 0xF) + 1) & 0x10) << 1);

		// Update register
		regs[reg_index] = (++regs[reg_index] & 0xFF);

		// clear & set ZF
		regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
		regs[FLAG_REG] = regs[FLAG_REG] | (((regs[reg_index] == 0) ? 1 : 0) << ZF_Shift);

		// clear & set NF
		regs[FLAG_REG] = regs[FLAG_REG] & ~NF_Mask;
	}

	protected void dec8b(int reg_index) {
		// Clear & Set HC
		regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
		regs[FLAG_REG] = regs[FLAG_REG] | (((regs[reg_index] & 0xF) == 0) ? HC_Mask : 0);

		// Update register
		regs[reg_index] = (--regs[reg_index] & 0xFF);

		// clear & set ZF
		regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
		regs[FLAG_REG] = regs[FLAG_REG] | (((regs[reg_index] == 0) ? 1 : 0) << ZF_Shift);

		// clear & set NF
		regs[FLAG_REG] = regs[FLAG_REG] | NF_Mask;
	}

	protected void inc16b() {
	}

	protected void add8b(int dest, int src) {
		// Clear all flags
		regs[FLAG_REG] = regs[FLAG_REG] & 0x0f;
		// Set HC
		regs[FLAG_REG] = regs[FLAG_REG] | ((((src & 0x0f) + (dest & 0x0f)) & 0x10) > 0 ? 1 : 0);

		// Update register (part 1)
		regs[dest] = (regs[dest] + regs[src]);

		// set CF
		regs[FLAG_REG] = regs[FLAG_REG] | (regs[dest] >> 8) << CF_Shift;

		// Clamp register (part 2)
		regs[dest] &= 0xFF;

		// set ZF
		regs[FLAG_REG] = regs[FLAG_REG] | (((regs[dest] == 0) ? 1 : 0) << ZF_Shift);
	}

	protected void JPnn() {
		int i = cardridge.read(++PC);
		int j = cardridge.read(++PC);
		PC = i << 8 | j;
	}

	protected void JPccnn(boolean cc, int nn) {
		if (cc)
			JPnn();
	}

	protected void JRe(int e) {
		PC += e;
	}

	protected void JRcce(boolean cc, int e) {
		if (cc)
			JRe(e);
	}

	protected int fetch() {
		return cardridge.read(PC);
	}

	private boolean execute(int instr) {
		System.out.printf("Executing instruction $%02x\n", instr);
		++PC; // FIXME: Is de PC niet ook een register in de CPU?
		switch (instr) {
			case 0x00: // NOP
				break;
			case 0x01: // LD BC,&0000
				// TODO
				break;
			case 0x02: // LD (BC),A
				// TODO
				break;
			case 0x03: // INC BC
				// TODO
				break;
			case 0x04: // INC B
				inc8b(B);
				break;
			case 0x05: // DEC B
				dec8b(B);
				break;
			case 0x45: // LD B,L
				// ld8b(B,L);
				break;
			case 0x80: // ADD A,B
				add8b(A, B);
				break;
			case 0x81: // ADD A,C
				add8b(A, C);
				break;
			case 0xc3: // JPNNNN
				JPnn();
				break;
			default:
				System.out.printf("UNKNOWN INSTRUCTION: $%02x\n", instr);
				return false;
		}
		++TotalInstrCount;
		return true;
	}

	protected boolean nextinstruction() {
		printCPUstatus();
		lastException = execute(fetch()) ? 0 : 1;
		return lastException == 0;
	}

	protected int exception() {
		return lastException;
	}

}