package arrays;

/*
 * The index of element is the number by which it was added.
 * For instance:
 * Maximum size=3
 * add e1 e2 e3 e4
 * indexes are: 2 3 4
 * (e1 is already deleted, due to CycledStringArray realization)
 * 
 * */

public class MagicStringArray {
	private CycledStringArray arr;
	private int maxSize;
	private int shift;
	
	public MagicStringArray(int n){
		arr = new CycledStringArray(n); 
		maxSize = n;		
		shift = 0;
	}
	
	public void add(String elem){
		//that means that startpoint is moved
		if(arr.add(elem) == true){
			shift++;
		}
	}
	
	public String get(int index){
		return arr.get(index - shift);
	}
	
	public int getFirstIndex(){
		return shift;
	}
	
	public int getLastIndex(){
		return shift + arr.getSize() - 1;
	}
	
}
