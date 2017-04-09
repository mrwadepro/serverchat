package keys;

import java.nio.ByteBuffer;

public class COMP_128
{
/************************
 * a3a8
 ************************/
	private static char table_0[] = {
	         102,177,186,162,  2,156,112, 75, 55, 25,  8, 12,251,193,246,188,
	         109,213,151, 53, 42, 79,191,115,233,242,164,223,209,148,108,161,
	         252, 37,244, 47, 64,211,  6,237,185,160,139,113, 76,138, 59, 70,
	          67, 26, 13,157, 63,179,221, 30,214, 36,166, 69,152,124,207,116,
	         247,194, 41, 84, 71,  1, 49, 14, 95, 35,169, 21, 96, 78,215,225,
	         182,243, 28, 92,201,118,  4, 74,248,128, 17, 11,146,132,245, 48,
	         149, 90,120, 39, 87,230,106,232,175, 19,126,190,202,141,137,176,
	         250, 27,101, 40,219,227, 58, 20, 51,178, 98,216,140, 22, 32,121,
	          61,103,203, 72, 29,110, 85,212,180,204,150,183, 15, 66,172,196,
	          56,197,158,  0,100, 45,153,  7,144,222,163,167, 60,135,210,231,
	         174,165, 38,249,224, 34,220,229,217,208,241, 68,206,189,125,255,
	         239, 54,168, 89,123,122, 73,145,117,234,143, 99,129,200,192, 82,
	         104,170,136,235, 93, 81,205,173,236, 94,105, 52, 46,228,198,  5,
	          57,254, 97,155,142,133,199,171,187, 50, 65,181,127,107,147,226,
	         184,218,131, 33, 77, 86, 31, 44, 88, 62,238, 18, 24, 43,154, 23,
	          80,159,134,111,  9,114,  3, 91, 16,130, 83, 10,195,240,253,119,
	         177,102,162,186,156,  2, 75,112, 25, 55, 12,  8,193,251,188,246,
	         213,109, 53,151, 79, 42,115,191,242,233,223,164,148,209,161,108,
	          37,252, 47,244,211, 64,237,  6,160,185,113,139,138, 76, 70, 59,
	          26, 67,157, 13,179, 63, 30,221, 36,214, 69,166,124,152,116,207,
	         194,247, 84, 41,  1, 71, 14, 49, 35, 95, 21,169, 78, 96,225,215,
	         243,182, 92, 28,118,201, 74,  4,128,248, 11, 17,132,146, 48,245,
	          90,149, 39,120,230, 87,232,106, 19,175,190,126,141,202,176,137,
	          27,250, 40,101,227,219, 20, 58,178, 51,216, 98, 22,140,121, 32,
	         103, 61, 72,203,110, 29,212, 85,204,180,183,150, 66, 15,196,172,
	         197, 56,  0,158, 45,100,  7,153,222,144,167,163,135, 60,231,210,
	         165,174,249, 38, 34,224,229,220,208,217, 68,241,189,206,255,125,
	          54,239, 89,168,122,123,145, 73,234,117, 99,143,200,129, 82,192,
	         170,104,235,136, 81, 93,173,205, 94,236, 52,105,228, 46,  5,198,
	         254, 57,155, 97,133,142,171,199, 50,187,181, 65,107,127,226,147,
	         218,184, 33,131, 86, 77, 44, 31, 62, 88, 18,238, 43, 24, 23,154,
	         159, 80,111,134,114,  9, 91,  3,130, 16, 10, 83,240,195,119,253
	     }, table_1[] = {
	          19, 11, 80,114, 43,  1, 69, 94, 39, 18,127,117, 97,  3, 85, 43,
	          27,124, 70, 83, 47, 71, 63, 10, 47, 89, 79,  4, 14, 59, 11,  5,
	          35,107,103, 68, 21, 86, 36, 91, 85,126, 32, 50,109, 94,120,  6,
	          53, 79, 28, 45, 99, 95, 41, 34, 88, 68, 93, 55,110,125,105, 20,
	          90, 80, 76, 96, 23, 60, 89, 64,121, 56, 14, 74,101,  8, 19, 78,
	          76, 66,104, 46,111, 50, 32,  3, 39,  0, 58, 25, 92, 22, 18, 51,
	          57, 65,119,116, 22,109,  7, 86, 59, 93, 62,110, 78, 99, 77, 67,
	          12,113, 87, 98,102,  5, 88, 33, 38, 56, 23,  8, 75, 45, 13, 75,
	          95, 63, 28, 49,123,120, 20,112, 44, 30, 15, 98,106,  2,103, 29,
	          82,107, 42,124, 24, 30, 41, 16,108,100,117, 40, 73, 40,  7,114,
	          82,115, 36,112, 12,102,100, 84, 92, 48, 72, 97,  9, 54, 55, 74,
	         113,123, 17, 26, 53, 58,  4,  9, 69,122, 21,118, 42, 60, 27, 73,
	         118,125, 34, 15, 65,115, 84, 64, 62, 81, 70,  1, 24,111,121, 83,
	         104, 81, 49,127, 48,105, 31, 10,  6, 91, 87, 37, 16, 54,116,126,
	          31, 38, 13,  0, 72,106, 77, 61, 26, 67, 46, 29, 96, 37, 61, 52,
	         101, 17, 44,108, 71, 52, 66, 57, 33, 51, 25, 90,  2,119,122, 35
	     }, table_2[] = {
	          52, 50, 44,  6, 21, 49, 41, 59, 39, 51, 25, 32, 51, 47, 52, 43,
	          37,  4, 40, 34, 61, 12, 28,  4, 58, 23,  8, 15, 12, 22,  9, 18,
	          55, 10, 33, 35, 50,  1, 43,  3, 57, 13, 62, 14,  7, 42, 44, 59,
	          62, 57, 27,  6,  8, 31, 26, 54, 41, 22, 45, 20, 39,  3, 16, 56,
	          48,  2, 21, 28, 36, 42, 60, 33, 34, 18,  0, 11, 24, 10, 17, 61,
	          29, 14, 45, 26, 55, 46, 11, 17, 54, 46,  9, 24, 30, 60, 32,  0,
	          20, 38,  2, 30, 58, 35,  1, 16, 56, 40, 23, 48, 13, 19, 19, 27,
	          31, 53, 47, 38, 63, 15, 49,  5, 37, 53, 25, 36, 63, 29,  5,  7
	     }, table_3[] = {
	           1,  5, 29,  6, 25,  1, 18, 23, 17, 19,  0,  9, 24, 25,  6, 31,
	          28, 20, 24, 30,  4, 27,  3, 13, 15, 16, 14, 18,  4,  3,  8,  9,
	          20,  0, 12, 26, 21,  8, 28,  2, 29,  2, 15,  7, 11, 22, 14, 10,
	          17, 21, 12, 30, 26, 27, 16, 31, 11,  7, 13, 23, 10,  5, 22, 19
	     }, table_4[] = {
	          15, 12, 10,  4,  1, 14, 11,  7,  5,  0, 14,  7,  1,  2, 13,  8,
	          10,  3,  4,  9,  6,  0,  3,  2,  5,  6,  8,  9, 11, 13, 15, 12
	     };
	private static char table[][] = { table_0, table_1, table_2, table_3, table_4 };	
	
	public static long[] A3A8(/* in  16*/ String r, /* in 16*/ String ke)
		 {
			char key[] = ke.toCharArray();
			char rand[] = r.toCharArray();
	 		char simoutput[] = new char[12];
		 	char x[] = new char[32], bit[]= new char[128];
		 	int i, j, k, l, m, n, y, z, next_bit;
		 
		 	/* ( Load RAND into last 16 bytes of input ) */
		 	for (i=16; i<32; i++)
		 		x[i] = rand[i-16];
		 
		 	/* ( Loop eight times ) */
		 	for (i=1; i<9; i++) {
		 		/* ( Load key into first 16 bytes of input ) */
		 		for (j=0; j<16; j++)
		 			x[j] = key[j];
		 		/* ( Perform substitutions ) */
		 		for (j=0; j<5; j++)
		 			for (k=0; k<(1<<j); k++)
		 				for (l=0; l<(1<<(4-j)); l++) {
		 					m = l + k*(1<<(5-j));
		 					n = m + (1<<(4-j));
		 					y = (x[m]+2*x[n]) % (1<<(9-j));
		 					z = (2*x[m]+x[n]) % (1<<(9-j));
		 					x[m] = table[j][y];
		 					x[n] = table[j][z];
		 				}
		 		/* ( Form bits from bytes ) */
		 		for (j=0; j<32; j++)
		 			for (k=0; k<4; k++)
		 				bit[4*j+k] = (char)((x[j]>>(3-k)) & 1);
		 		/* ( Permutation but not on the last loop ) */
		 		if (i < 8)
		 			for (j=0; j<16; j++) {
		 				x[j+16] = 0;
		 				for (k=0; k<8; k++) {
		 					next_bit = ((8*j + k)*17) % 128;
		 					x[j+16] |= bit[next_bit] << (7-k);
		 				}
		 			}
		 	}
		 
		 	/*
		 	 * ( At this stage the vector x[] consists of 32 nibbles.
		 	 *   The first 8 of these are taken as the output SRES. )
		 	 */
		 
		 	/* The remainder of the code is not given explicitly in the
		 	 * standard, but was derived by reverse-engineering.
		 	 */
		 
		 	for (i=0; i<4; i++)
		 		simoutput[i] = (char)((x[2*i]<<4) | x[2*i+1]);
		 	for (i=0; i<6; i++)
		 		simoutput[4+i] = (char)((x[2*i+18]<<6) | (x[2*i+18+1]<<2)
		 				| (x[2*i+18+2]>>2));
		 	simoutput[4+6] = (char)((x[2*6+18]<<6) | (x[2*6+18+1]<<2));
		 	simoutput[4+7] = 0;
		 	
		 	long[] ret = new long[2];
		 	ByteBuffer bf = ByteBuffer.allocate(Long.BYTES+Integer.BYTES);
		 	System.out.println("simoutput length: "+simoutput.length);
		 	System.out.println("bf limit: "+bf.limit());
		 	for(int val =0; val< simoutput.length; val++)
		 	{
		 		bf.put((byte)simoutput[val]);
		 	}
		 	bf.position(0);
		 	ret[0] = bf.getLong();
		 	ret[1] = bf.getInt();
		 	
		 	return ret;
		 }
/************************
 * a5
 * Copyright (C) 1998-1999: Marc Briceno, Ian Goldberg, and David Wagner
 ************************/
	
	/* Masks for the three shift registers */
	private static long R1MASK = 0x07FFFF; /* 19 bits, numbered 0..18 */
	private static long R2MASK = 0x3FFFFF; /* 22 bits, numbered 0..21 */
	private static long R3MASK = 0x7FFFFF; /* 23 bits, numbered 0..22 */

	/* Middle bit of each of the three shift registers, for clock control */
	private static long R1MID = 0x000100; /* bit 8 */
	private static long R2MID =	0x000400; /* bit 10 */
	private static long R3MID =	0x000400; /* bit 10 */

	/* Feedback taps, for clocking the shift registers.
	 * These correspond to the primitive polynomials
	 * x^19 + x^5 + x^2 + x + 1, x^22 + x + 1,
	 * and x^23 + x^15 + x^2 + x + 1. */
	private static long R1TAPS = 0x072000; /* bits 18,17,16,13 */
	private static long R2TAPS = 0x300000; /* bits 21,20 */
	private static long R3TAPS = 0x700080; /* bits 22,21,20,7 */

	/* Output taps, for output generation */
	private static long R1OUT =	0x040000; /* bit 18 (the high bit) */
	private static long R2OUT =	0x200000; /* bit 21 (the high bit) */
	private static long R3OUT =	0x400000; /* bit 22 (the high bit) */

//	typedef unsigned char byte;
//	typedef unsigned long word;
//	typedef word bit;
	
	/* Calculate the parity of a 32-bit word, i.e. the sum of its bits modulo 2 */
	private static long parity(long x)
	{
		x ^= x>>16;
		x ^= x>>8;
		x ^= x>>4;
		x ^= x>>2;
		x ^= x>>1;
		return x&1;
	}

	/* Clock one shift register */
	private static long clockone(long reg, long mask, long taps) 
	{
		long t = reg & taps;
		reg = (reg << 1) & mask;
		reg |= parity(t);
		return reg;
	}

	/* The three shift registers.  They're in global variables to make the code
	 * easier to understand.
	 * A better implementation would not use global variables. */
	private static long R1, R2, R3;

	/* Look at the middle bits of R1,R2,R3, take a vote, and
	 * return the majority value of those 3 bits. */
	private static boolean majority() {
		long sum;
		sum = parity(R1&R1MID) + parity(R2&R2MID) + parity(R3&R3MID);
		if (sum >= 2)
			return true;
		else
			return false;
	}

	/* Clock two or three of R1,R2,R3, with clock control
	 * according to their middle bits.
	 * Specifically, we clock Ri whenever Ri's middle bit
	 * agrees with the majority value of the three middle bits.*/
	private static void clock() {
		boolean maj = majority();
		if (((R1&R1MID)!=0) == maj)
			R1 = clockone(R1, R1MASK, R1TAPS);
		if (((R2&R2MID)!=0) == maj)
			R2 = clockone(R2, R2MASK, R2TAPS);
		if (((R3&R3MID)!=0) == maj)
			R3 = clockone(R3, R3MASK, R3TAPS);
	}

	/* Clock all three of R1,R2,R3, ignoring their middle bits.
	 * This is only used for key setup. */
	private static void clockallthree() {
		R1 = clockone(R1, R1MASK, R1TAPS);
		R2 = clockone(R2, R2MASK, R2TAPS);
		R3 = clockone(R3, R3MASK, R3TAPS);
	}

	/* Generate an output bit from the current state.
	 * You grab a bit from each register via the output generation taps;
	 * then you XOR the resulting three bits. */
	private static long getbit()
	{
		return parity(R1&R1OUT)^parity(R2&R2OUT)^parity(R3&R3OUT);
	}

	/* Do the A5/1 key setup.  This routine accepts a 64-bit key and
	 * a 22-bit frame number. */
	private static void keysetup(long key[]/*8*/, long frame)
	{
		int i;
		long keybit, framebit;

		/* Zero out the shift registers. */
		R1 = R2 = R3 = 0;

		/* Load the key into the shift registers,
		 * LSB of first byte of key array first,
		 * clocking each register once for every
		 * key bit loaded.  (The usual clock
		 * control rule is temporarily disabled.) */
		for (i=0; i<64; i++) {
			clockallthree(); /* always clock */
			keybit = (key[i/8] >> (i&7)) & 1; /* The i-th bit of the
	key */
			R1 ^= keybit; R2 ^= keybit; R3 ^= keybit;
		}

		/* Load the frame number into the shift
		 * registers, LSB first,
		 * clocking each register once for every
		 * key bit loaded.  (The usual clock
		 * control rule is still disabled.) */
		for (i=0; i<22; i++) {
			clockallthree(); /* always clock */
			framebit = (frame >> i) & 1; /* The i-th bit of the frame #
	*/
			R1 ^= framebit; R2 ^= framebit; R3 ^= framebit;
		}

		/* Run the shift registers for 100 clocks
		 * to mix the keying material and frame number
		 * together with output generation disabled,
		 * so that there is sufficient avalanche.
		 * We re-enable the majority-based clock control
		 * rule from now on. */
		for (i=0; i<100; i++) {
			clock();
		}

		/* Now the key is properly set up. */
	}
		
	/* Generate output.  We generate 228 bits of
	 * keystream output.  The first 114 bits is for
	 * the A->B frame; the next 114 bits is for the
	 * B->A frame.  You allocate a 15-byte buffer
	 * for each direction, and this function fills
	 * it in. */
	private static void run(long AtoBkeystream[], long BtoAkeystream[])
	{
		int i;

		/* Zero out the output buffers. */
		for (i=0; i<=113/8; i++)
			AtoBkeystream[i] = BtoAkeystream[i] = 0;
		
		/* Generate 114 bits of keystream for the
		 * A->B direction.  Store it, MSB first. */
		for (i=0; i<114; i++) {
			clock();
			AtoBkeystream[i/8] |= getbit() << (7-(i&7));
		}

		/* Generate 114 bits of keystream for the
		 * B->A direction.  Store it, MSB first. */
		for (i=0; i<114; i++) {
			clock();
			BtoAkeystream[i/8] |= getbit() << (7-(i&7));
		}
	}
	
	/* Test the code by comparing it against
	 * a known-good test vector. */
	public static void test() {
		long key[] = {0x12, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF};
		long frame = 0x134;
		long goodAtoB[] = { 0x53, 0x4E, 0xAA, 0x58, 0x2F, 0xE8, 0x15,
		                      0x1A, 0xB6, 0xE1, 0x85, 0x5A, 0x72, 0x8C, 0x00 };
		long goodBtoA[] = { 0x24, 0xFD, 0x35, 0xA3, 0x5D, 0x5F, 0xB6,
		                      0x52, 0x6D, 0x32, 0xF9, 0x06, 0xDF, 0x1A, 0xC0 };
		long AtoB[] = new long[15], BtoA[] = new long[15];
		int i, failed=0;

		keysetup(key, frame);
		run(AtoB, BtoA);

		/* Compare against the test vector. */
		for (i=0; i<15; i++)
			if (AtoB[i] != goodAtoB[i])
				failed = 1;
		for (i=0; i<15; i++)
			if (BtoA[i] != goodBtoA[i])
				failed = 1;
		
		if (failed ==0) {
			System.out.printf("Self-check succeeded: everything looks ok.\n");
			return;
		} 
		else
		{
			/* Problems!  The test vectors didn't compare*/
			System.out.printf("\nI don't know why this broke; contact the authors.\n");
			System.exit(1);
		}
	}
	/**
	 * generates a random 128 bit number
	 * @return a random 128 bit number
	 */
	public static String gen_rand_128Bit()
	{
		String ret = "";
		for(int x = 0; x< 16; x++)
		{
			ret += (byte)(Math.random()*Byte.MAX_VALUE);
		}
		return ret;
	}
	
	/**
	 * encrypts a string of data using a cipher key
	 * @param data the data to encrypt
	 * @param key the cipher key
	 * @return encrypted data
	 */
	public static String cipher(String data, long key)
	{
		//TODO actually encrypt the data
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES).putLong(key);
		
		
		return data;
	}
}
