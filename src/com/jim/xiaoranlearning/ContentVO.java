package com.jim.xiaoranlearning;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ContentVO implements Comparable<ContentVO>{
	
	/**
	 * used for JSON obj as a key
	 */
	static public final String RAW_SEQUENCE = "rawSequence";
	static public final String DISPLAY_TIMES = "displayTimes";
	static public final String IS_KNOWN = "isKnown";
	static public final String CONTENT = "content";
	private static final String LOG_TAG = "ContentVO";
	
	private int rawSequence;
	private int displayTimes = 1;//when user see it it is already 1
	private boolean isKnown = false;
	private String content;
	
	public ContentVO(){}
	
	static public ContentVO jsonToContentVO(String jsonString){
		JSONObject oneJSONobj;
		ContentVO vo = new ContentVO();
		try {
			oneJSONobj = new JSONObject(jsonString);
			vo.setContent(oneJSONobj.getString(ContentVO.CONTENT));
			vo.setDisplayTimes(oneJSONobj.getInt(ContentVO.DISPLAY_TIMES));
			vo.setKnown(oneJSONobj.getBoolean(ContentVO.IS_KNOWN));
			vo.setRawSequence(oneJSONobj.getInt(ContentVO.RAW_SEQUENCE));
		} catch (JSONException e) {
			Log.v(LOG_TAG, "jsonToContentVO() error: " ); 
			e.printStackTrace();
		}
		return vo;
	}
	
	static public String toJson(ContentVO vo){
		JSONObject jsonOBJ = new JSONObject();
		try {
			jsonOBJ.put(DISPLAY_TIMES, vo.getDisplayTimes());
			jsonOBJ.put(RAW_SEQUENCE, vo.getRawSequence());
			jsonOBJ.put(IS_KNOWN, vo.isKnown());
			jsonOBJ.put(CONTENT, vo.getContent());
		} catch (JSONException e) {
			Log.v(LOG_TAG, "jsonToContentVO() error: " ); 
			e.printStackTrace();
		}
		return jsonOBJ.toString();
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getRawSequence() {
		return rawSequence;
	}
	public void setRawSequence(int rawSequence) {
		this.rawSequence = rawSequence;
	}
	public int getDisplayTimes() {
		return displayTimes;
	}
	public void setDisplayTimes(int displayTimes) {
		this.displayTimes = displayTimes;
	}
	public boolean isKnown() {
		return isKnown;
	}
	public void setKnown(boolean isKnown) {
		this.isKnown = isKnown;
	}
	
	/**
	 * should display from littler to bigger
	 */
	@Override
	public int compareTo(ContentVO target) {
		
		// known first, then not Known
		if (this.isKnown == false && target.isKnown == true) {
			return 1;
		}
		if (this.isKnown == true && target.isKnown == false) {
			return -1;
		}
//		if (this.displayTimes > target.displayTimes){
//			return 1;
//		}
		//if both known or unknown
		return this.rawSequence < target.rawSequence ? -1 :1;
	}
	public void increaseDisplayTimes() {
		displayTimes += 1;
	}
}
