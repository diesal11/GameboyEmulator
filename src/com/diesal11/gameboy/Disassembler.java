package com.diesal11.gameboy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Disassembler {
	private static final String file_name = "instrs.txt";

	// Opcode: opcode to disassemble
	// BC: whether opcode was after BC
	public static final String disassemble(int opcode, boolean BC) {
		BufferedReader reader;

		try {
			File file = new File(file_name);
			FileInputStream fistream = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(fistream));
		} catch (FileNotFoundException fnfe) {
			return "ERROR while loading file " + file_name + "!";
		}

		// Read enough lines from file to get instruction...
		int lines_to_read = BC ? (opcode + 256) : opcode;

		try {
			for (int i = 0; i < lines_to_read; ++i) {
				reader.readLine();
			}

			return reader.readLine();
		} catch (IOException ioe) {
			return "ERROR while reading " + file_name + "!";
		}
	}

	public static void main(String[] args) {
		System.out.println(Disassembler.disassemble(0, false));
		System.out.println(Disassembler.disassemble(0, true));
		System.out.println(Disassembler.disassemble(255, false));
		System.out.println(Disassembler.disassemble(255, true));
	}
}