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
		
		// 30 seconds expiration has been added for cache-elements
		cache = new AddressCache(30, TimeUnit.SECONDS);
	}

	/**
	 * This unit test checks for the waiting behaviour of take() method of the cache system.
	 * In case of zero elements in cache, the take() method should resolve to a WAITING state
	 * and resume execution when an element is added to cache.
	 */
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
		String[] testArray = { "www.google.com", "www.ebay.com", "www.msn.com" };
		Date[] expiryTimes = new Date[testArray.length];
		
		Calendar cal = Calendar.getInstance();
		int i = 0;

		for (String site : testArray) {
			try {
				cache.add(InetAddress.getByName(site));
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.SECOND, 35);
				
				// we are storing the probable expire time for each element
				// each element shall expire after 30 seconds and the background daemon
				// that cleans expired elements run every 5 seconds.
				// Hence, 35 seconds is added in the previous step
				expiryTimes[i] = cal.getTime();
				
				// we are just ensuring a gap in expiry time among elements
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
			
			// Following condition checks if the longest expiry time among inserted elements has passed
			if (now.after(expiryTimes[expiryTimes.length - 1])) {
				// After passing of last expiry, all elements should have been removed, provided nothing has
				// been added
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