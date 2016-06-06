package com.example.canvasjoystick;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCases {

	@Test
	public void test() {
		assertEquals("tan-1(1/0) ", 1.57, Math.atan2(1, 0), 2);
		assertEquals("tan(1/0) ", 1, Math.tan(1.5707963267948966), 2);
	}

}
