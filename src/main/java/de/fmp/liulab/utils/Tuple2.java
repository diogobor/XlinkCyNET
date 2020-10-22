package de.fmp.liulab.utils;

/**
 * Class responsible for creating tuple
 * @author diogobor
 *
 * @param <K>
 * @param <V>
 */
public class Tuple2<K, V> {
	 
    private K first;
    private V second;
  
    public Tuple2(K first, V second){
        this.first = first;
        this.second = second;
    }
 
    public K getFirst() {
    	return first;
    }
    
    public V getSecond() {
    	return second;
    }
}