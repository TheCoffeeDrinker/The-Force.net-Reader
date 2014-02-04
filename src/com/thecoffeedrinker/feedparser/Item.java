package com.thecoffeedrinker.feedparser;

import java.util.HashMap;

/**
 * Class that will keep the content of the parsed feed element 
 * @author carlo
 *
 */
public class Item {
	//the tag the object will keep
	private HashMap<String, String> element;
	
	public Item(){
		element = new HashMap<String, String>();
	}
	
	/**
	 * Add a new element to the item
	 * @param tag 
	 * @param content
	 * @return true if there was no tag and the element was added; false if nothing was added
	 */
	public boolean add(String tag, String content){
		if(element.containsKey(tag)){
			return false;
		}
		element.put(tag,content);
		return true;
	}
	
	public String get(String tag){
		return element.get(tag);
	}
	
}
