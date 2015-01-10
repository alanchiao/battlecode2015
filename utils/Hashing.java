package team158.utils;

public class Hashing {
	
	public int hash(int key, int i) {
		return key;
	}
	public boolean add(int[] arr, int key, int value) {
		int i = 0;
		int hashedKey = hash(key,i);
		while (arr[hashedKey] != 0 && i < arr.length) {
			hashedKey = hash(key,++i);
		}
		if (arr[hashedKey] == 0) {
			arr[hashedKey] = value;
			return true;
		}
		else return false;
	}
	public int find(int[] arr, int key) {
		int i = 0;
		int hashedKey = hash(key,i);
		while (arr[hashedKey] == 0 && i < arr.length) {
			hashedKey = hash(key,i++);
		}
		return 0;
	}
	
	
}

