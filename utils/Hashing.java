package team158.utils;

public class Hashing {
	public static int[] groupID = new int[7919];
	
	public static void put(int key, int value) {
		int hashedKey = (key*(key+5))%groupID.length;
		groupID[hashedKey] = value;
	}
	public static int find(int key) {
		int hashedKey = (key*(key+5))%groupID.length;
		return groupID[hashedKey];
	}
	
	
}

