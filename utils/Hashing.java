package team158.utils;

public class Hashing {
	
	public static void put(int[] arr, int key, int value) {
		int hashedKey = (key*(key+5))%arr.length;
		arr[hashedKey] = value;
	}
	public static int find(int[] arr, int key) {
		//System.out.println("key = " + key);
		int hashedKey = (key*(key+5))%arr.length;
		//System.out.println("hashedKey = " + hashedKey);
		return arr[hashedKey];
	}
	
	
}

