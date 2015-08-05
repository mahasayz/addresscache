/*
 * The AddressCache has a max age for the elements it's storing, an add method 
 * for adding elements, a remove method for removing, a peek method which 
 * returns the most recently added element, and a take method which removes 
 * and returns the most recently added element.
 */
public class AddressCache {
	public AddressCache(long maxAge, TimeUnit unit) {
	}
	
	/**
	 * add() method must store unique elements only (existing elements must be ignored). 
	 * This will return true if the element was successfully added. 
	 * @param address
	 * @return
	 */

	public boolean add(InetAddress address) {
	}

	/**
	 * remove() method will return true if the address was successfully removed
	 * @param address
	 * @return
	 */
	public boolean remove(InetAddress address) {
	}

	/**
	 * The peek() method will return the most recently added element, 
	 * null if no element exists.
	 * @return
	 */
	public InetAddress peek() {
	}

	/**
	 * take() method retrieves and removes the most recently added element 
	 * from the cache and waits if necessary until an element becomes available.
	 * @return
	 */
	public InetAddress take() {
	}
	
}
