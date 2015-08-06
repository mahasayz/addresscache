package com.agoda.interviews;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The AddressCache has a max age for the elements it's storing, an add method 
 * for adding elements, a remove method for removing, a peek method which 
 * returns the most recently added element, and a take method which removes 
 * and returns the most recently added element.
 */
public class AddressCache {

	private static final int MAX_SIZE = 5;
	private long timeToLive;
	private static Map<String, CacheObject> cacheMap;
	ReentrantLock lock = new ReentrantLock();
	Condition notEmpty = lock.newCondition();
	Condition notFull = lock.newCondition();

	private static Logger logger = null;

	static {
		try {
			Thread cleaner = new Thread(new Runnable() {
				int intervalTime = 5000;

				public void run() {
					try {
						while (true) {
							logger.info("Scanning for expired objects");
							if (cacheMap != null) {
								Set<String> keySet = cacheMap.keySet();
								logger.info("Starting Size : " + keySet.size());
								Iterator<String> keys = keySet.iterator();
								while (keys.hasNext()) {
									String key = keys.next();
									CacheObject elem = cacheMap.get(key);
									if (elem.isExpired()) {
										keys.remove();
										logger.info("Removed: "
												+ elem.value.getHostAddress());
									}
								}
								logger.info("Ending Size : " + cacheMap.size());
							}
							Thread.sleep(intervalTime);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			cleaner.setDaemon(true);
			cleaner.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected class CacheObject {
		public long timeToExpire = System.currentTimeMillis();
		public InetAddress value;

		protected CacheObject(InetAddress value) {
			this.value = value;
			timeToExpire += timeToLive;
		}

		public boolean isExpired() {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timeToExpire);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			logger.info("Value: " + this.value + ", Time to Expire: "
					+ sdf.format(cal.getTime()));
			logger.info("Value: " + this.value + ", Current Time: "
					+ sdf.format(new Date()));
			if (cal.getTime().before(new Date()))
				return true;
			else
				return false;
		}
	}

	public AddressCache(long maxAge, TimeUnit unit) {
		timeToLive = unit.toMillis(maxAge);
		logger = Logger.getLogger(AddressCache.class.getName());
		cacheMap = Collections
				.synchronizedMap(new LinkedHashMap<String, CacheObject>());
	}

	/**
	 * add() method must store unique elements only (existing elements must be
	 * ignored). This will return true if the element was successfully added.
	 * 
	 * @param address
	 * @return
	 */

	public boolean add(InetAddress address) {
		if (cacheMap.containsKey(address))
			return false;
		final ReentrantLock lock = this.lock;
		try {
			lock.lockInterruptibly();
			while (cacheMap.size() == MAX_SIZE)
				notFull.await();
			cacheMap.put(address.getHostAddress(), new CacheObject(address));
			logger.info("Added " + address.getHostAddress());
			notEmpty.signal();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, e.getMessage());
		} finally {
			lock.unlock();
		}
		return true;
	}

	/**
	 * remove() method will return true if the address was successfully removed
	 * 
	 * @param address
	 * @return
	 */
	public boolean remove(InetAddress address) {
		if (!cacheMap.containsKey(address.getHostAddress()))
			return false;
		cacheMap.remove(address.getHostAddress());
		return true;
	}

	/**
	 * The peek() method will return the most recently added element, null if no
	 * element exists.
	 * 
	 * @return
	 */
	public InetAddress peek() {
		if (cacheMap.size() == 0) {
			return null;
		}
		final Set<Entry<String, CacheObject>> mapValues = cacheMap.entrySet();
		final int size = mapValues.size();
		final Entry<String, CacheObject>[] queueImpl = new Entry[size];
		mapValues.toArray(queueImpl);

		CacheObject elem = queueImpl[size - 1].getValue();
		if (elem == null)
			return null;
		if (elem.isExpired()) {
			cacheMap.remove(elem.value.getHostAddress());
			return null;
		} else {
			elem.timeToExpire = System.currentTimeMillis() + timeToLive;
			return elem.value;
		}
	}

	/**
	 * take() method retrieves and removes the most recently added element from
	 * the cache and waits if necessary until an element becomes available.
	 * 
	 * @return
	 */
	public InetAddress take() {
		final ReentrantLock lock = this.lock;
		InetAddress retVal = null;
		try {
			lock.lockInterruptibly();
			while (cacheMap.size() == 0)
				notEmpty.await();

			final Set<Entry<String, CacheObject>> mapValues = cacheMap
					.entrySet();
			final int size = mapValues.size();
			final Entry<String, CacheObject>[] queueImpl = new Entry[size];
			mapValues.toArray(queueImpl);

			CacheObject elem = queueImpl[size - 1].getValue();
			if (elem == null)
				retVal = null;
			cacheMap.remove(elem.value.getHostAddress());
			if (elem.isExpired()) {
				retVal = null;
			} else {
				retVal = elem.value;
				logger.info("Got " + retVal.getHostAddress());
			}

			notFull.signal();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, e.getMessage());
		} finally {
			lock.unlock();
		}
		return retVal;
	}

	public int size() {
		return cacheMap.size();
	}
}
