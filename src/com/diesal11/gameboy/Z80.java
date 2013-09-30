package com.diesal11.gameboy;

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

	private int[] HRAM = new int[0x7F]; //HighRAM
	private int[][] WRAM = new int[0x08][0x10000]; //8x4k InternalRAM
	private int CurrentWRAMBank = 0;

	protected int IR;
	protected int PC;
	protected int SP;
	// CPU Class variables
	private Cartridge cartridge;// = new Cartridge("Pokemon Blue.gb");
	private int lastException = 0;
	private Disassembler deasm;

	public Z80(Cartridge cartridge) {
		deasm = new Disassembler(cartridge, this);
		this.cartridge = cartridge;
		reset();
	}

	private int read(int index) {
		/* Memorymap:
		 * 0000-3FFF   16KB ROM Bank 00     (in cartridge, fixed at bank 00)
		 * 4000-7FFF   16KB ROM Bank 01..NN (in cartridge, switchable bank number)
		 * 8000-9FFF   8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
		 * A000-BFFF   8KB External RAM     (in cartridge, switchable bank, if any)
		 * C000-CFFF   4KB Work RAM Bank 0 (WRAM)
		 * D000-DFFF   4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
		 * E000-FDFF   Same as C000-DDFF (ECHO)    (typically not used)
		 * FE00-FE9F   Sprite Attribute Table (OAM)
		 * FEA0-FEFF   Not Usable
		 * FF00-FF7F   I/O Ports
		 * FF80-FFFE   High RAM (HRAM)
		 * FFFF        Interrupt Enable Register
		 */
		int b = 0; // b==byte read
		if (index < 0) { //Invalid
			System.out.println("ERROR: CPU.read(): No negative addresses in GameBoy memorymap.");
			b = -1;
		} else if (index < 0x4000) { //16KB ROM Bank 00     (in cartridge, fixed at bank 00)
			b = cartridge.read(index);
		} else if (index < 0x8000) { //16KB ROM Bank 01..NN (in cartridge, switchable bank number)
			b = cartridge.read(index);
		} else if (index < 0xA000) { //8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
			System.out.println("TODO: CPU.read(): VRAM Read");
			b = 0;
		} else if (index < 0xC000) { //8KB External RAM     (in cartridge, switchable bank, if any)
			b=cartridge.read(index);
		} else if (index < 0xd000) { //4KB Work RAM Bank 0 (WRAM)
			b = WRAM[0][index - 0xc000];
		} else if (index < 0xe000) { //4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
			b = WRAM[CurrentWRAMBank][index - 0xd000];
		} else if (index < 0xfe00) { //Same as C000-DDFF (ECHO)    (typically not used)
			System.out.println("TODO: CPU.read(): ECHO RAM Read");
			b = read(index - 0x2000);
		} else if (index < 0xfea0) { //Sprite Attribute Table (OAM)
			System.out.println("TODO: CPU.read(): Sprite Attribute Table");
			b = 0;
		} else if (index < 0xff00) { //Not Usable
			System.out.println("WARNING: CPU.read(): Read from unusable memory (0xfea-0xfeff)");
			b = 0;
		} else if (index < 0xff80) { //I/O Ports
			System.out.println("TODO: CPU.read(): Read from IO ports");
			if (index == 0xff44) HRAM[index - 0xff00] = (HRAM[index - 0xff00] + 1) % 153;//vblank hax
			b = 0;
		} else if (index < 0xffff) { //High RAM (HRAM)
			b = HRAM[index-0xff80];
		} else if (index < 0x10000) { // Interrupt Enable Register (0xffff)
			System.out.println("TODO: CPU.read(): Read from Interrupt Enable Register (0xffff)");
			b = 0;
		} else {
			System.out.println("ERROR: CPU.read(): Out of range memory access: $" + index);
			b = 0;
		}
		return b;
	}

	private void write(int index, int value) {
		/* Memorymap:
		 * 0000-3FFF   16KB ROM Bank 00     (in cartridge, fixed at bank 00)
		 * 4000-7FFF   16KB ROM Bank 01..NN (in cartridge, switchable bank number)
		 * 8000-9FFF   8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
		 * A000-BFFF   8KB External RAM     (in cartridge, switchable bank, if any)
		 * C000-CFFF   4KB Work RAM Bank 0 (WRAM)
		 * D000-DFFF   4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
		 * E000-FDFF   Same as C000-DDFF (ECHO)    (typically not used)
		 * FE00-FE9F   Sprite Attribute Table (OAM)
		 * FEA0-FEFF   Not Usable
		 * FF00-FF7F   I/O Ports
		 * FF80-FFFE   High RAM (HRAM)
		 * FFFF        Interrupt Enable Register
		 */
		if (index < 0) { //Invalid
			System.out.println("ERROR: CPU.write(): No negative addresses in GameBoy memorymap.");
		} else if (index < 0x4000) { //16KB ROM Bank 00     (in cartridge, fixed at bank 00)
			cartridge.write(index, value);
		} else if (index < 0x8000) { //16KB ROM Bank 01..NN (in cartridge, switchable bank number)
			cartridge.write(index, value);
		} else if (index < 0xA000) { //8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
			System.out.println("TODO: CPU.write(): VRAM Write");
		} else if (index < 0xC000) { //8KB External RAM     (in cartridge, switchable bank, if any)
			cartridge.write(index, value);
		} else if (index < 0xd000) { //4KB Work RAM Bank 0 (WRAM)
			WRAM[0][index - 0xc000] = value;
		} else if (index < 0xe000) { //4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
			WRAM[CurrentWRAMBank][index - 0xd000] = value;
		} else if (index < 0xfe00) { //Same as C000-DDFF (ECHO)    (typically not used)
			write(index-0x2000, value);
		} else if (index < 0xfea0) { //Sprite Attribute Table (OAM)
			System.out.println("TODO: CPU.write(): Sprite Attribute Table");
		} else if (index < 0xff00) { //Not Usable
			System.out.println("TODO: CPU.write(): Write to unusable memory (0xfea-0xfeff)");
		} else if (index < 0xff80) { //I/O Ports
			System.out.println("TODO: CPU.write(): Write to IO ports");
			if (index == 0xff70) { //FF70 - SVBK - CGB Mode Only - WRAM Bank
				CurrentWRAMBank = Math.max(value & 0x07, 1);
			}
		} else if (index < 0xffff) { //High RAM (HRAM)
			HRAM[index-0xff80] = value;
		} else if (index < 0x10000) { // Interrupt Enable Register (0xffff)
			System.out.println("TODO: CPU.write(): Write to Interrupt Enable Register (0xffff)");
		} else {
			System.out.println("ERROR: CPU.write(): Out of range memory access: $" + index);
		}
	}

	public void reset() {
		// TODO: Switch to bank 0
		PC = 0x100; // ROM Entry point on bank 0
		// AF=$01B0
		regs[A] = 0x01;
		regs[F] = 0xb0;
		// BC=$0013
		regs[B] = 0x00;
		regs[C] = 0x13;
		// DE=$00D8
		regs[D] = 0x00;
		regs[E] = 0xd8;
		// HL=$014D
		regs[H] = 0x01;
		regs[L] = 0x4d;
		TotalInstrCount = 0;

		// Stack Pointer=$FFFE
		SP = 0xfffe;
		/*TODO:INTERNAL RAM
		  [$FF05] = $00   ; TIMA
		  [$FF06] = $00   ; TMA
		  [$FF07] = $00   ; TAC
		  [$FF10] = $80   ; NR10
		  [$FF11] = $BF   ; NR11
		  [$FF12] = $F3   ; NR12
		  [$FF14] = $BF   ; NR14
		  [$FF16] = $3F   ; NR21
		  [$FF17] = $00   ; NR22
		  [$FF19] = $BF   ; NR24
		  [$FF1A] = $7F   ; NR30
		  [$FF1B] = $FF   ; NR31
		  [$FF1C] = $9F   ; NR32
		  [$FF1E] = $BF   ; NR33
		  [$FF20] = $FF   ; NR41
		  [$FF21] = $00   ; NR42
		  [$FF22] = $00   ; NR43
		  [$FF23] = $BF   ; NR30
		  [$FF24] = $77   ; NR50
		  [$FF25] = $F3   ; NR51
		  [$FF26] = $F1-GB, $F0-SGB ; NR52
		  [$FF40] = $91   ; LCDC
		  [$FF42] = $00   ; SCY
		  [$FF43] = $00   ; SCX
		  [$FF45] = $00   ; LYC
		  [$FF47] = $FC   ; BGP
		  [$FF48] = $FF   ; OBP0
		  [$FF49] = $FF   ; OBP1
		  [$FF4A] = $00   ; WY
		  [$FF4B] = $00   ; WX
		  [$FFFF] = $00   ; IE
		*/
	}

	protected int cycles() {
		return TotalInstrCount;
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
		System.out.printf("   A=$%02x    B=$%02x    C=$%02x    D=$%02x   E=$%02x   F=$%02x   H=$%02x   L=$%02x\n", regs[A], regs[B], regs[C], regs[D], regs[E], regs[F], regs[H], regs[L]);
		System.out.printf("  PC=$%04x SP=$%04x                           flags=" + flags + "\n", PC, SP);
		System.out.println("  " + deasm.disassemble(PC));
	}

	protected int readmem8b(int H, int L) {
		return read((regs[H] << 8) | regs[L]);
	}

	protected void writemem8b(int H, int L, int val) {
		write((regs[H] << 8) | regs[L], val);
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

	protected void inc16b(int ri1, int ri2) {
		// 16-bit inc/dec doesnt affect any flags
		++regs[ri2];
		if (regs[ri2] > 0xFF) {
			regs[ri2] &= 0xFF;
			++regs[ri1];
			regs[ri1] &= 0xFF;
		}
	}

	protected void dec16b(int ri1, int ri2) {
		// 16-bit inc/dec doesnt affect any flags
		--regs[ri2];
		if (regs[ri2] < 0) {
			regs[ri2] &= 0xFF;
			--regs[ri1];
			regs[ri1] &= 0xFF;
		}
	}

	protected void add8b(int dest, int val) {
		// Clear all flags (Including ZF)
		regs[FLAG_REG] = (regs[FLAG_REG] & 0x00);

		// Set HC
		regs[FLAG_REG] = regs[FLAG_REG] | (((((regs[dest] & 0x0f) + (val & 0x0f)) & 0x10) != 0 ? 1 : 0) << HC_Shift);

		// Update register (part 1)
		regs[dest] = (regs[dest] + val);

		// set CF
		regs[FLAG_REG] = regs[FLAG_REG] | (regs[dest] >> 8) << CF_Shift;

		// Clamp register (part 2)
		regs[dest] &= 0xFF;

		// set ZF
		regs[FLAG_REG] = regs[FLAG_REG] | (((regs[dest] == 0) ? 1 : 0) << ZF_Shift);
	}

	protected void sub8b(int dest, int val) {
		// Clear all flags except NF which is set
		regs[FLAG_REG] = NF_Mask;

		// Set HC
		regs[FLAG_REG] |= (((regs[dest] & 0x0F) - (val & 0x0F)) < 0) ? HC_Mask : 0;

		// Update register (part 1)
		regs[dest] = (regs[dest] - val);

		// set CF
		regs[FLAG_REG] |= (regs[dest] < 0) ? CF_Mask : 0;

		// Clamp register (part 2)
		regs[dest] &= 0xFF;

		// set ZF
		regs[FLAG_REG] |= regs[dest] == 0 ? ZF_Mask : 0;
	}

	protected void ld8b(int dest, int val) {
		regs[dest] = val;
	}

	protected void ld8bmem(int location, int val) {
		write(location, val);
	}

	protected void cp(int val) {
		int i = regs[A];
		sub8b(A, val);
		regs[A] = i;
	}

	protected void xor(int val) {
		regs[F] = 0;
		regs[A] ^= val;
		regs[F] |= (regs[A] == 0 ? ZF_Mask : 0);
	}

	protected void or(int val) {
		regs[F] = 0;
		regs[A] |= val;
		regs[F] |= (regs[A] == 0 ? ZF_Mask : 0);
	}

	protected void and(int val) {
		regs[F] = HC_Mask;
		regs[A] &= val;
		regs[F] |= (regs[A] == 0 ? ZF_Mask : 0);
	}

	protected void JPnn() {
		int i = read(PC++);
		int j = read(PC++);
		//System.out.println("i=" + i + " j=" + j);
		PC = j << 8 | i; //SHOULD be endian correct
	}

	protected void JRe(int e) {
		PC += e;
	}

	protected void sbc(int dest, int val) {
		sub8b(dest, val + ((regs[FLAG_REG] & CF_Mask) >> CF_Shift));
	}

	protected void adc(int dest, int val) {
		add8b(dest, val + ((regs[FLAG_REG] & CF_Mask) >> CF_Shift));
	}

	protected void JRcce(boolean cc, int e) {
		if (cc) JRe(e);
	}

	protected void push(int val) {
		//Should be Endian corrent
		write(--SP, (val >> 8) & 0xFF);
		write(--SP, val & 0xFF);
	}

	protected int pop() {
		//Should be endian correct
		int l = read(SP++);
		int h = read(SP++);
		return (l | (H << 8));
	}

	protected int fetch() {
		return read(PC);
	}

	static int nopCount = 0;

	private boolean execute(int instr) {
		boolean nop = false;
		//System.out.printf("Executing instruction $%02x\n", instr);
		++PC; //FIXME: Is de PC niet ook een register in de CPU?
		switch (instr) {
			case 0x00: // NOP
				nop = true;
				break;
			case 0x01: // LD BC, nn
				regs[C] = read(PC++);
				regs[B] = read(PC++);
				break;
			case 0x02: // LD (BC), A
				writemem8b(B, C, regs[A]);
				break;
			case 0x03: // INC BC
				inc16b(B, C);
				break;
			case 0x04: // INC B
				inc8b(B);
				break;
			case 0x05: // DEC B
				dec8b(B);
				break;
			case 0x0b: // DEC BC
				dec16b(B, C);
				break;
			case 0x0c: // INC  C
				inc8b(C);
				break;
			case 0x0d: // DEC  C
				dec8b(C);
				break;
			case 0x11: // LD DE, nn
				regs[E] = read(PC++);
				regs[D] = read(PC++);
				break;
			case 0x13: // INC DE
				inc16b(D, E);
				break;
			case 0x14: // INC  D
				inc8b(D);
				break;
			case 0x15: // DEC  D
				dec8b(D);
				break;
			case 0x18: {// JR   &00
				int x = read(PC++);
				PC += ((x >= 128) ? -(x ^ 0xFF) - 1 : x);
			}
				;
				break;
			case 0x1b: // DEC DE
				dec16b(D, E);
				break;
			case 0x1c: // INC  E
				inc8b(E);
				break;
			case 0x1d: // DEC  E
				dec8b(E);
				break;
			case 0x20: // JR NZ, n
				if ((regs[F] & ZF_Mask) != ZF_Mask) {
					int x = read(PC++);
					PC += ((x >= 128) ? -(x ^ 0xFF) - 1 : x);
				} else
					++PC;
				break;
			case 0x21: // LD HL, nn
				regs[L] = read(PC++);
				regs[H] = read(PC++);
				break;
			case 0x22: // LDI (HL), A
				writemem8b(H, L, regs[A]);
				inc16b(H, L);
				break;
			case 0x23: // INC HL
				inc16b(H, L);
				break;
			case 0x28: // JR   Z, n
				if ((regs[F] & ZF_Mask) == ZF_Mask) {
					int x = read(PC++);
					PC += ((x >= 128) ? -(x ^ 0xFF) - 1 : x);
				} else
					++PC;
				break;
			case 0x2b: // DEC HL
				dec16b(H, L);
				break;
			case 0x2c: // INC L
				inc8b(L);
				break;
			case 0x2d: // DEC  L
				dec8b(L);
				break;
			case 0x2f: // CPL
				xor(0xFF);
				break;
			case 0x31: {// LD SP, nn
				int l = read(PC++);
				int h = read(PC++);
				SP = l | (h << 8);
			}
				;
				break;
			case 0x33: // INC SP
				++SP; //16-bit inc/dec doesnt affect any flags
				SP &= 0xffff;
				break;
			case 0x36: // LD (HL), n
				writemem8b(H, L, read(PC++));
				break;
			case 0x3b: // DEC SP
				--SP; //16-bit inc/dec doesnt affect any flags
				SP &= 0xffff;
				break;
			case 0x3e: // LD A, n
				regs[A] = read(PC++);
				break;
			case 0x40: // LD B, B
				ld8b(B, regs[B]);
				break;
			case 0x41: // LD B, C
				ld8b(B, regs[C]);
				break;
			case 0x42: // LD   B,D
				ld8b(B, regs[D]);
				break;
			case 0x43: // LD   B,E
				ld8b(B, regs[E]);
				break;
			case 0x44: // LD   B,H
				ld8b(B, regs[H]);
				break;
			case 0x45: // LD   B,L
				ld8b(B, regs[L]);
				break;
			case 0x46: // LD   B,(HL)
				ld8b(B, readmem8b(H, L));
				break;
			case 0x47: // LD   B,A
				ld8b(C, regs[A]);
				break;
			case 0x48: // LD   C,B
				ld8b(C, regs[B]);
				break;
			case 0x49: // LD   C,C
				ld8b(C, regs[C]);
				break;
			case 0x4a: // LD   C,D
				ld8b(C, regs[D]);
				break;
			case 0x4b: // LD   C,E
				ld8b(C, regs[E]);
				break;
			case 0x4c: // LD   C,H
				ld8b(C, regs[H]);
				break;
			case 0x4d: // LD   C,L
				ld8b(C, regs[L]);
				break;
			case 0x4e: // LD   C,(HL)
				ld8b(C, readmem8b(H, L));
				break;
			case 0x4f: // LD   C,A
				ld8b(C, regs[A]);
				break;
			case 0x50: // LD   D,B
				ld8b(D, regs[B]);
				break;
			case 0x51: // LD   D,C
				ld8b(D, regs[C]);
				break;
			case 0x52: // LD   D,D
				ld8b(D, regs[D]);
				break;
			case 0x53: // LD   D,E
				ld8b(D, regs[E]);
				break;
			case 0x54: // LD   D,H
				ld8b(D, regs[H]);
				break;
			case 0x55: // LD   D,L
				ld8b(D, regs[L]);
				break;
			case 0x56: // LD   D,(HL)
				ld8b(D, readmem8b(H, L));
				break;
			case 0x57: // LD   D,A
				ld8b(D, regs[A]);
				break;
			case 0x58: // LD   E,B
				ld8b(E, regs[B]);
				break;
			case 0x59: // LD   E,C
				ld8b(E, regs[C]);
				break;
			case 0x5a: // LD   E,D
				ld8b(E, regs[D]);
				break;
			case 0x5b: // LD   E,E
				ld8b(E, regs[E]);
				break;
			case 0x5c: // LD   E,H
				ld8b(E, regs[H]);
				break;
			case 0x5d: // LD   E,L
				ld8b(E, regs[L]);
				break;
			case 0x5e: // LD   E,(HL)
				ld8b(E, readmem8b(H, L));
				break;
			case 0x5f: // LD   E,A
				ld8b(E, regs[A]);
				break;
			case 0x60: // LD   H,B
				ld8b(H, regs[B]);
				break;
			case 0x61: // LD   H,C
				ld8b(H, regs[C]);
				break;
			case 0x62: // LD   H,D
				ld8b(H, regs[D]);
				break;
			case 0x63: // LD   H,E
				ld8b(H, regs[E]);
				break;
			case 0x64: // LD   H,H
				ld8b(H, regs[H]);
				break;
			case 0x65: // LD   H,L
				ld8b(H, regs[L]);
				break;
			case 0x66: // LD   H,(HL)
				ld8b(H, readmem8b(H, L));
				break;
			case 0x67: // LD   H,A
				ld8b(H, regs[A]);
				break;
			case 0x68: // LD   L,B
				ld8b(L, regs[B]);
				break;
			case 0x69: // LD   L,C
				ld8b(L, regs[C]);
				break;
			case 0x6a: // LD   L,D
				ld8b(L, regs[D]);
				break;
			case 0x6b: // LD   L,E
				ld8b(L, regs[E]);
				break;
			case 0x6c: // LD   L,H
				ld8b(L, regs[H]);
				break;
			case 0x6d: // LD   L,L
				ld8b(L, regs[L]);
				break;
			case 0x6e: // LD   L,(HL)
				ld8b(L, readmem8b(H, L));
				break;
			case 0x6f: // LD   L,A
				ld8b(L, regs[A]);
				break;
			case 0x70: // LD   (HL),B
				writemem8b(H, L, regs[B]);
				break;
			case 0x71: // LD   (HL),C
				writemem8b(H, L, regs[C]);
				break;
			case 0x72: // LD   (HL),D
				writemem8b(H, L, regs[D]);
				break;
			case 0x73: // LD   (HL),E
				writemem8b(H, L, regs[E]);
				break;
			case 0x74: // LD   (HL),H
				writemem8b(H, L, regs[H]);
				break;
			case 0x75: // LD   (HL),L
				writemem8b(H, L, regs[L]);
				break;
			//case 0x76: // HALT
			//	break;
			case 0x77: // LD   (HL),A
				writemem8b(H, L, regs[A]);
				break;
			case 0x78: // LD   A,B
				ld8b(A, regs[B]);
				break;
			case 0x79: // LD   A,C
				ld8b(A, regs[C]);
				break;
			case 0x7a: // LD   A,D
				ld8b(A, regs[D]);
				break;
			case 0x7b: // LD   A,E
				ld8b(A, regs[E]);
				break;
			case 0x7c: // LD   A,H
				ld8b(A, regs[H]);
				break;
			case 0x7d: // LD   A,L
				ld8b(A, regs[L]);
				break;
			case 0x7e: // LD   A,(HL)
				ld8b(A, readmem8b(H, L));
				break;
			case 0x7f: // LD   A,A
				ld8b(A, regs[A]);
				break;
			case 0x80: // ADD  A,B
				add8b(A, regs[B]);
				break;
			case 0x81: // ADD  A,C
				add8b(A, regs[C]);
				break;
			case 0x82: // ADD  A,D
				add8b(A, regs[D]);
				break;
			case 0x83: // ADD  A,E
				add8b(A, regs[E]);
				break;
			case 0x84: // ADD  A,H
				add8b(A, regs[H]);
				break;
			case 0x85: // ADD  A,L
				add8b(A, regs[L]);
				break;
			case 0x86: // ADD  A,(HL)
				add8b(A, readmem8b(H, L));
				break;
			case 0x87: // ADD  A,A
				add8b(A, regs[A]);
				break;
			case 0x88: // ADC  A,B
				adc(A, regs[B]);
				break;
			case 0x89: // ADC  A,C
				adc(A, regs[C]);
				break;
			case 0x8a: // ADC  A,D
				adc(A, regs[D]);
				break;
			case 0x8b: // ADC  A,E
				adc(A, regs[E]);
				break;
			case 0x8c: // ADC  A,H
				adc(A, regs[H]);
				break;
			case 0x8d: // ADC  A,L
				adc(A, regs[L]);
				break;
			case 0x8e: // ADC  A,(HL)
				adc(A, readmem8b(H, L));
				break;
			case 0x8f: // ADC  A,A
				adc(A, regs[A]);
				break;
			case 0x98: // SBC  A,B
				sbc(A, regs[B]);
				break;
			case 0x99: // SBC  A,C
				sbc(A, regs[C]);
				break;
			case 0x9a: // SBC  A,D
				sbc(A, regs[D]);
				break;
			case 0x9b: // SBC  A,E
				sbc(A, regs[E]);
				break;
			case 0x9c: // SBC  A,H
				sbc(A, regs[H]);
				break;
			case 0x9d: // SBC  A,L
				sbc(A, regs[L]);
				break;
			case 0x9e: // SBC  A,(HL)
				sbc(A, readmem8b(H, L));
				break;
			case 0x9f: // SBC  A,A
				sbc(A, regs[A]);
				break;
			case 0xa0: // AND B
				and(regs[B]);
				break;
			case 0xa1: // AND C
				and(regs[C]);
				break;
			case 0xa2: // AND D
				and(regs[D]);
				break;
			case 0xa3: // AND E
				and(regs[E]);
				break;
			case 0xa4: // AND H
				and(regs[H]);
				break;
			case 0xa5: // AND L
				and(regs[L]);
				break;
			case 0xa6: // AND (HL)
				and(readmem8b(H, L));
				break;
			case 0xa7: // AND A
				and(regs[A]);
				break;
			case 0xa8: // XOR B
				xor(regs[B]);
				break;
			case 0xa9: // XOR C
				xor(regs[C]);
				break;
			case 0xaa: // XOR D
				xor(regs[D]);
				break;
			case 0xab: // XOR E
				xor(regs[E]);
				break;
			case 0xac: // XOR H
				xor(regs[H]);
				break;
			case 0xad: // XOR L
				xor(regs[L]);
				break;
			case 0xae: // XOR (HL)
				xor(readmem8b(H, L));
				break;
			case 0xaf: // XOR A
				xor(regs[A]);
				break;
			case 0xb0: // OR  B
				or(regs[B]);
				break;
			case 0xb1: // OR  C
				or(regs[C]);
				break;
			case 0xb2: // OR  D
				or(regs[D]);
				break;
			case 0xb3: // OR  E
				or(regs[E]);
				break;
			case 0xb4: // OR  H
				or(regs[H]);
				break;
			case 0xb5: // OR  L
				or(regs[L]);
				break;
			case 0xb6: // OR  (HL)
				or(readmem8b(H, L));
				break;
			case 0xb7: // OR  A
				or(regs[A]);
				break;
			case 0xb8: // CP   B
				cp(regs[B]);
				break;
			case 0xb9: // CP   C
				cp(regs[C]);
				break;
			case 0xba: // CP   D
				cp(regs[D]);
				break;
			case 0xbb: // CP   E
				cp(regs[E]);
				break;
			case 0xbc: // CP   H
				cp(regs[H]);
				break;
			case 0xbd: // CP   L
				cp(regs[L]);
				break;
			case 0xbe: // CP   (HL)
				cp(readmem8b(H, L));
				break;
			case 0xbf: // CP   A
				cp(regs[A]);
				break;
			case 0xc3: // JPNNNN
				JPnn();
				break;
			case 0xc9: // RET
				PC = pop();
				break;
			case 0xcd: // CALL &0000
				push(PC + 2);
				JPnn();
				break;
			case 0xd1: {// POP DE
				int x = pop();
				regs[D] = x >> 8;
				regs[E] = x & 0xff;
			}
				;
				break;
			case 0xd5: // PUSH DE
				push(regs[D] << 8 | regs[E]);
				break;
			case 0xe0: // LDH
				write(0xff00 | read(PC++), regs[A]);
				break;
			case 0xe6: // AND nn
				and(read(PC++));
				break;
			case 0xea: {// LD (nnnn), A
				int a = read(PC++);
				int b = read(PC++);
				ld8bmem((b << 8) + a, regs[A]);
			}
				break;
			case 0xee: // XOR   &00
				xor(read(PC++));
				break;
			case 0xf0: // LDH
				regs[A] = read(0xff00 | read(PC++));
				break;
			case 0xf3: // DI
				IR = 0x00;
				break;
			case 0xfe: // CP n
				cp(read(PC++));
				break;
			case 0xda: //D4 JMP CF,&0000
				if ((regs[FLAG_REG] & CF_Mask) != CF_Mask) { //call to nn, SP=SP-2, (SP)=PC, PC=nn
					JPnn();
				} else {
					PC += 2;
				}
				break;
			case 0xFF: // RST &38
				push(PC);
				PC = 0x38;
				break;
			case 0xcb: // prefix instruction
				instr = cartridge.read(PC++);
				switch (instr) {
					case 0x80: // RES 0,B
						regs[B] &= ~(1 << 0);
						break;
					case 0x81: // RES 0,C
						regs[C] &= ~(1 << 0);
						break;
					case 0x82: // RES 0,D
						regs[D] &= ~(1 << 0);
						break;
					case 0x83: // RES 0,E
						regs[E] &= ~(1 << 0);
						break;
					case 0x84: // RES 0,H
						regs[H] &= ~(1 << 0);
						break;
					case 0x85: // RES 0,L
						regs[L] &= ~(1 << 0);
						break;
					case 0x86: // RES 0,(HL)
						writemem8b(H, L, readmem8b(H, L) & ~(1 << 0));
						break;
					case 0x87: // RES 0,A
						regs[A] &= ~(1 << 0);
						break;
					default:
						System.out.printf("UNKNOWN PREFIX INSTRUCTION: $%02x\n", instr);
						PC -= 2; // we failed to execute the instruction, so restore PC
						return false;
				}
				break;
			default:
				System.out.printf("UNKNOWN INSTRUCTION: $%02x\n", instr);
				PC -= 1; // we failed to execute the instruction, so restore PC
				return false;
		}
		PC &= 0xffff;
		SP &= 0xffff;
		++TotalInstrCount;
		if (nop) {
			++nopCount;
		} else {
			nopCount = 0;
		}
		if (nopCount > 5) {
			System.out.println("Executing a lot of NOPs, aborting!");
			return false;
		}
		return true;
	}

	protected boolean nextinstruction() {
		lastException = execute(fetch()) ? 0 : 1;
		return lastException == 0;
	}

	protected int exception() {
		return lastException;
	}

}