package arrays;

import java.util.ArrayList;

/*
 * Contains fixed number of elements, but elements can be added forever,
 * because it uses index of the the oldest element to overwrite it and store the new one.
 * 
 * */

public class CycledStringArray{
	private int maxSize;
	private int start;
	private int size;
	private String[] data;
	
	public CycledStringArray(int n){
		start = 0;
		size = 0;
		data = new String[n];
		maxSize = n;
	}
	public boolean add(String elem){
		if (size < maxSize){
			data[size] = elem;
			size++;
			return false;
		}else{
			data[start] = elem;
			start++;
			if (start == maxSize)
				start = 0;
			return true;
		}
	}
	public String get(int index){
		if(index >= 0 && index < maxSize)
			if (index + start < maxSize)
				return data[index + start];
			else
				return data[index + start - maxSize];
		return "Out of boundaries array error";
	}
	public int getSize(){
		return size;
	}
	public int getMaxSize(){
		return maxSize;
	}
}
