package com.jim.xiaoranlearning;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * TODO factory pattern to apply, split content and content generation logic - different combination of them are different generation strategy
 * @author ji5
 */
public class Chinese265DAO implements ILearningContantDAO {
	
	static public final String KEY_MAIN_PREFERENCE = "main_prefe";
	
	public static class CHINESE285{
		static public final String KEY_JSON_DATA_CHINESE285 = "json_data_Chinese_285"; //JSON and its meta data 
		static public final String KEY_LAST_POSITION_CHINESE285= "last_position_Chinese285";
	}
	public static class ENGLISH_WORDS{
		static public final String KEY_LAST_POSITION_ENGLISH_WORDS = "last_position_english_words";
		static public final String KEY_JSON_DATA_ENGLISH_WORDS = "json_data_English_words"; //JSON and its meta data 
	}

	static public final String KEY_LAST_CONTNET_TYPE = "last_content_type";
	static public final String KEY_REMEMBERED = "how_much_rememblered";
	
	//private String mWordsToLearn = "爱八巴把爸白伴北笔边变别不步才草长车成吃池虫出处吹春从答打大带到道得的灯地点东冬动都对多朵儿耳二发方放飞分风干高告歌个给跟瓜光国果过孩海好禾和河很红后候花画话欢还黄回会活火己家间见江角叫节姐界金进睛九就觉开看棵可空口快来蓝老乐里力立亮了林流柳六绿妈马满毛么没每美门们米面苗明母木那奶南你年鸟牛女跑朋皮片七奇起气千前青清请秋去全让热人日入三色森沙山上少生声师十什石时世事是手书田条树数水说四松送岁他她它台太桃天跳听同头土兔完玩晚为位问我乌五小笑写心新兴星行许学雪西习下想向像鸭牙眼阳要爷也叶夜一衣用友有又鱼雨园原圆月云再在早找这真正枝知只纸中种竹住捉着子字自总走足最坐做";
	private String mWordsToLearn = null;
//	private String mWordsToLearn = "一二三四五六七";
	private Context mAndroidContext;
	private static final String LOG_TAG = "ContentDAO";
	
	private static ILearningContantDAO mDaoInstance = null;
	private JSONArray mJsonArray;
	private ArrayList<ContentVO> mContentArray;
	
	
	/**
	 * notice must ensure the androidContext initialized by setAndroidContext().
	 * @return
	 */
	static public ILearningContantDAO getInstance(){
		
		if (mDaoInstance == null) {
			synchronized (Chinese265DAO.class) {
				if( mDaoInstance == null){
					Log.i(LOG_TAG , "createSingleton");
					mDaoInstance = new Chinese265DAO();
				}
			}
		}
		return mDaoInstance;
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#init(android.content.Context)
	 */
	@Override
	public void init(Context aContext){
		mAndroidContext = aContext;
 		mContentArray = new ArrayList<ContentVO>(); 
 		loadNewContent(ContentType.CHINESE_265); 

	}

	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#loadNewContent(com.jim.xiaoranlearning.Chinese265DAO.ContentType)
	 */
	@Override
	public void loadNewContent(ContentType contentType) {
		Log.i(LOG_TAG, "going to switch content to type: " + contentType);
		SharedPreferences preference = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
		String[] strArr;

		if (!preference.contains(CHINESE285.KEY_JSON_DATA_CHINESE285)) { //Do nothing if already in SD card. onStart()-> getLastStatus() will get them.
			mWordsToLearn = mAndroidContext.getString(R.string.Chinese_265);
			strArr = mWordsToLearn.split(",");
			for (int i = 0; i < strArr.length; i++) {
				ContentVO vo = new ContentVO();
				vo.setRawSequence(i);
				vo.setContent(strArr[i]);
				mContentArray.add(vo);
			}
			Collections.shuffle(mContentArray);
			int i = 0;
			for (ContentVO vo : mContentArray) {
				vo.setRawSequence(i);
				i = i++;
			}
			Log.w(LOG_TAG, "on init content array length: "+ mContentArray.size());
		}
		//init json strcture only for the fist time, the next time will use the persistent settings.
	}
	
	private Chinese265DAO(){
		Log.i(LOG_TAG , "init ContentDAO singleton");
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#saveLastStatus(int)
	 */
	@Override
	public void saveLastStatus(int position){
		//save position and whole content array
		SharedPreferences pref = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
		SharedPreferences.Editor editor = pref.edit();  
		//Persistence the VO
		Collections.sort(mContentArray);
		JSONArray jArray = new JSONArray();
		for (int i = 0; i < mContentArray.size(); i++) {
			jArray.put(ContentVO.toJson(mContentArray.get(i)));
		}
		String jArr = jArray.toString();

		editor.putInt(CHINESE285.KEY_LAST_POSITION_CHINESE285, position); 
		editor.putString(CHINESE285.KEY_JSON_DATA_CHINESE285, jArr);
		Log.v(LOG_TAG, "saveLastStatus(): position: "+ position + "Json Array to save: "+ jArr);
		editor.commit();
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#getLastStatus()
	 */
	@Override
	public int getLastStatus(){ 
		SharedPreferences pref = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
		ContentType contentType = ContentType.valueOf(pref.getInt(KEY_LAST_CONTNET_TYPE, 0));
		int pos =0;

		// init the content array
		if (pref.contains(CHINESE285.KEY_JSON_DATA_CHINESE285)) {
			mContentArray.clear();
			try {
				mJsonArray = new JSONArray(pref.getString(CHINESE285.KEY_JSON_DATA_CHINESE285, ""));
				Log.v(LOG_TAG, "getLastStatus(): json string >> "+ mJsonArray);
				for (int i = 0; i < mJsonArray.length(); i++) {
					mContentArray.add(ContentVO.jsonToContentVO((String)mJsonArray.get(i)));
				}
				Collections.sort(mContentArray);
				Log.d(LOG_TAG, "getLastStatus(): last item in colection: "+ mContentArray.get(mContentArray.size()-1).getContent());
			} catch (JSONException e) {
				Log.e(LOG_TAG, "parse Jsonarray string error: ");
				e.printStackTrace();
			}
		}
		pos = pref.getInt(CHINESE285.KEY_LAST_POSITION_CHINESE285, 0);
		Log.v(LOG_TAG, "getLastStatus(): pos: " + pos);
		return pos;
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#getCurrentDisplay(int)
	 */
	@Override
	public ContentVO getCurrentDisplay(int position) {
		return mContentArray.get(position);
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#markKnown(int)
	 */
	@Override
	public void markKnown(int position) {
		mContentArray.get(position).setKnown(true);
		Log.d(LOG_TAG, "markKnown(): last item in colection: "+ mContentArray.get(mContentArray.size()-1).getContent());
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#markUnKnown(int)
	 */
	@Override
	public void markUnKnown(int position) {
		mContentArray.get(position).setKnown(false);
		Log.d(LOG_TAG, "markUnKnown(): last item in colection: "+ mContentArray.get(mContentArray.size()-1).getContent());
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#increaseDisplayTimes(int)
	 */
	@Override
	public void increaseDisplayTimes(int pos) {
		Log.d(LOG_TAG, "increaseDisplayTimes() for: "+pos);
		mContentArray.get(pos).increaseDisplayTimes();
	}
	
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#increaseLearned()
	 */
	@Override
	public void increaseLearned() {
//		mLearned += 1;
	}
	/* (non-Javadoc)
	 * @see com.jim.xiaoranlearning.ILearningContantDAO#getContentLength()
	 */
	@Override
	public int getContentLength(){ return mContentArray.size();}
}
