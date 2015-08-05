package com.agoda.interviews;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCases {

	private static Logger logger = null;

	@Before
	public void setUp() {
		logger = Logger.getLogger(TestCases.class.getName());
		logger.info("INITIALIZING test cases");
	}

	@Test
	public void test1() {
		logger.info("Testing Use Case 1");
	}


	@After
	public void cleanUp() {
	}

}