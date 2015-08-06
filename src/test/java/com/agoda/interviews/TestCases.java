package com.agoda.interviews;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCases {

	private static Logger logger = null;
	private AddressCache cache;
	
	@Before
	public void setUp() {
		logger = Logger.getLogger(TestCases.class.getName());
		logger.info("INITIALIZING test cases");
		cache = new AddressCache(30, TimeUnit.SECONDS);
	}

	@Test
	public void blockedBehaviorTest() {
		logger.info("Case 1: Calling take() when no data available in cache. Checking to see state of thread whether WAITING or not");

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(5000);
					cache.add(InetAddress.getByName("www.ebay.com"));
				} catch (InterruptedException | UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}).start();

		Thread consumer = new Thread(new Runnable() {
			public void run() {
				cache.take();
			}
		});
		consumer.start();

		try {
			Thread.sleep(2000);
			assertEquals(Thread.State.WAITING, consumer.getState());
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void cleanUp() {
		cache = null;
	}
	
	@Before
	public void setUpForCase2() {
		cache = new AddressCache(30, TimeUnit.SECONDS);
	}
	
	@Test
	public void testAdditionAndTimelyExpiration() {
		String[] testArray = {"www.google.com", "www.ebay.com", "www.msn.com"};
		Date[] expiryTimes = new Date[testArray.length];
		Calendar cal = Calendar.getInstance();
		int i = 0;
		
		for (String site : testArray) {
			try {
				cache.add(InetAddress.getByName(site));
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.SECOND, 35);
				expiryTimes[i] = cal.getTime();
				Thread.sleep(5000);
				i++;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Arrays.sort(expiryTimes);
		Date now;
		while (true) {
			now = new Date();
			if (now.after(expiryTimes[expiryTimes.length - 1])) {
				assertEquals(0, cache.size());
				break;
			}
		}
	}

	@After
	public void cleanUpAfterCase2() {
		cache = null;
	}
}