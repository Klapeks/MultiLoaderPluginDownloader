package com.klapeks.mlpd.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class uArrayMap<K,V> {
	
	protected HashMap<K, ArrayList<V>> hm;
	
	public uArrayMap() {
		hm = new HashMap<>();
	}
	
	public void addIn(K key, V val) {
		ArrayList<V> arval = get(key);
		if (!arval.contains(val)) {
			arval.add(val);
			hm.put(key, arval);
		}
	}
	
	public ArrayList<V> get(K key){
		return hm.containsKey(key) ? hm.get(key) : new ArrayList<>();
	}

	public boolean remove(K key, V val){
		ArrayList<V> arval = get(key);
		boolean b = arval.remove(val);
		if (arval.isEmpty()) {
			hm.remove(key);
			return b;
		}
		hm.put(key, arval);
		return b;
	}
	
	public boolean containsKey(K key) {
		return hm.containsKey(key);
	}
	
	public boolean containsValue(K key, V val) {
		ArrayList<V> arval = get(key);
		return arval.contains(val);
	}

	public ArrayList<V> delete(K key){
		return hm.remove(key);
	}
	public void clear(){
		hm.clear();
	}
	
	@Deprecated
	public HashMap<K, ArrayList<V>> getOriginal(){
		return hm;
	}

	public int keySize() {
		return hm.size();
	}
	public int valueSize(K key) {
		return get(key).size();
	}
	public boolean isEmpty() {
		return hm.isEmpty();
	}
	public Set<K> keySet() {
		return hm.keySet();
	}
}
