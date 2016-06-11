package com.example.canvasjoystick;

import org.junit.runner.*;
import org.junit.runner.notification.Failure;

public class TestRunner {
	public static void main(String args[]){
		Result result = JUnitCore.runClasses(TestCases.class);
		for(Failure failure : result.getFailures()){
			System.out.println(failure.toString());
		}
		
	}
}
