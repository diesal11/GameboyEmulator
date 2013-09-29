package com.diesal11.gameboy;

public class TestSuite {
	private Z80 cpu;

	public TestSuite(Z80 cpu) {
		this.cpu = cpu;
	}

	private boolean ld8b_diag() {
		/***************************************************************************************************************
		 * Test LDRR_8b Tests 0x00 <- 0xFF, 0xFF <- 0x00 for not changing flags
		 * and loading good values
		 */
		boolean status = true;

		cpu.regs[cpu.A] = 0;
		cpu.regs[cpu.B] = 0;
		cpu.regs[cpu.C] = 0;
		cpu.regs[cpu.D] = 0;
		cpu.regs[cpu.E] = 0;
		cpu.regs[cpu.F] = 0;
		cpu.regs[cpu.H] = 0;
		cpu.regs[cpu.L] = 0;

		cpu.ld8b(cpu.B, 0xff);
		if (cpu.regs[cpu.B] != 0xff) {
			System.out.println("B != 0xFF after LD B,A (A = 0xFF)");
			status = status && false;
		}
		if (cpu.regs[cpu.F] != 0x00) {
			System.out.println("Flags set after ld8b(B, A)");
			status = status && false;
		}
		cpu.ld8b(cpu.B, 0x00);
		if (cpu.regs[cpu.B] != 0x00) {
			System.out.println("B != 0x00 after LD B,A (A = 0x00)");
			status = status && false;
		}
		if (cpu.regs[cpu.F] != 0x00) {
			System.out.println("Flags set after ld8b(B, A)");
			status = status && false;
		}

		return status;
	}

	private boolean dec8b_diag() {

		/***************************************************************************************************************
		 * Test DEC_8b Tests 0x01 - 1, 0x00 - 1, 0x10 - 1 for setting AND
		 * clearing of flags
		 */
		boolean status = true;

		cpu.regs[cpu.A] = 0x01;
		cpu.regs[cpu.FLAG_REG] = 0x00; // clear all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: 1 - 1 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x01->0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x01->0x00 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0x01;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: 1 - 1 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x01->0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x01->0x00 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0;
		cpu.regs[cpu.FLAG_REG] = 0x00; // clear all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0xff) {
			System.out.println("Error: 0 - 1 != 0xff");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x00->0xff and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x00->0xff and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0xff) {
			System.out.println("Error: 0 - 1 != 0xff");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x00->0xff and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x00->0xff and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0x10;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x0f) {
			System.out.println("Error: 0x10 - 1 != 0x0f");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x10->0x0f and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x10->0x0f and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0x10;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.dec8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x0f) {
			System.out.println("Error: 0x10 - 1 != 0x0f");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: DEC8b: A:0x10->0x0f and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: DEC8b: A:0x10->0x0f and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: DEC8b: DEC'd and NF not set");
			status = status && false;
		}
		return status;
	}

	private boolean inc8b_diag() {
		/***************************************************************************************************************
		 * Test INC_8b Tests 0x00 + 1, 0x0f + 1, 0xff + 1 for setting AND
		 * clearing of flags
		 */
		boolean status = true;
		cpu.regs[cpu.A] = 0;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 1) {
			System.out.println("Error: 0 + 1 != 1");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0->1 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0->1 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 1) {
			System.out.println("Error: 0 + 1 != 1");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0->1 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0->1 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0x0f;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x10) {
			System.out.println("Error: 0x0f + 1 != 0x10");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0x0f->0x10 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0x0f->0x10 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0x0f;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x10) {
			System.out.println("Error: 0x0f + 1 != 0x10");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0x0f->0x10 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0x0f->0x10 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0xff;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: 0xff + 1 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0xff->0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0xff->0x00 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0xff;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.inc8b(cpu.A);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: 0xff + 1 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: INC8b: A:0xff->0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: INC8b: A:0xff->0x00 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) == cpu.NF_Mask) {
			System.out.println("Error: INC8b: Inc'd and NF set");
			status = status && false;
		}
		return status;
	}

	private boolean add8b_diag() {
		/***************************************************************************************************************
		 * Test ADD_8b Tests 0x07 + 0x08, 0x0F + 0x1, 0x10 + 0xf0 for setting
		 * AND clearing of flags
		 */

		boolean status = true;
		cpu.regs[cpu.A] = 0x07;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.add8b(cpu.A, 0x08);
		if (cpu.regs[cpu.A] != 0x0f) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 != 0x0f");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) == cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and CF is set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0x07;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.add8b(cpu.A, 0x08);
		if (cpu.regs[cpu.A] != 0x0f) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 != 0x0f");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) == cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x07 + 0x08 = 0x0f and CF is set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0x0f;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.add8b(cpu.A, 0x01);
		if (cpu.regs[cpu.A] != 0x10) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 != 0x10");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) == cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and CF is set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0x0f;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.add8b(cpu.A, 0x01);
		if (cpu.regs[cpu.A] != 0x10) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 != 0x10");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) == cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and ZF is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) != cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and HC is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) == cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x0f + 0x01 = 0x10 and CF is set");
			status = status && false;
		}

		cpu.regs[cpu.A] = 0x10;
		cpu.regs[cpu.FLAG_REG] = 0; // clear all flags
		cpu.add8b(cpu.A, 0xf0);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) != cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and CF is NOT set");
			status = status && false;
		}
		cpu.regs[cpu.A] = 0x10;
		cpu.regs[cpu.FLAG_REG] = 0xf0; // set all flags
		cpu.add8b(cpu.A, 0xf0);
		if (cpu.regs[cpu.A] != 0x00) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 != 0x00");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.ZF_Mask) != cpu.ZF_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and ZF is NOT set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.NF_Mask) != cpu.NF_Mask) {
			System.out.println("Error: ADD8b: does not set NF");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.HC_Mask) == cpu.HC_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and HC is set");
			status = status && false;
		}
		if ((cpu.regs[cpu.FLAG_REG] & cpu.CF_Mask) != cpu.CF_Mask) {
			System.out.println("Error: ADD8b: 0x10 + 0xf0 = 0x00 and CF is NOT set");
			status = status && false;
		}
		return status;
	}

	public int diagnose(boolean verbose) {
		boolean result;
		int count = 0;
		result = inc8b_diag();
		if (verbose && result) {
			System.out.println("INC8b instruction appears to work ok");
		} else {
			System.out.println("*ERROR* IN INC8b INSTRUCTION!");
			++count;
		}
		result = dec8b_diag();
		if (verbose && result) {
			System.out.println("DEC8b instruction appears to work ok");
		} else {
			System.out.println("*ERROR* IN DEC8b INSTRUCTION!");
			++count;
		}
		result = add8b_diag();
		if (verbose && result) {
			System.out.println("ADD8b instruction appears to work ok");
		} else {
			System.out.println("*ERROR* IN ADD8b INSTRUCTION!");
			++count;
		}
		result = ld8b_diag();
		if (verbose && result) {
			System.out.println("LD8b instruction appears to work ok");
		} else {
			System.out.println("*ERROR* IN LD8b INSTRUCTION!");
			++count;
		}
		if (verbose || count > 0)
			System.out.println("There were errors in " + count + " instructions");
		return count;
	}

}