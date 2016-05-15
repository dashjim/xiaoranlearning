package com.jim.xiaoranlearning;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SelfDefinedDAO implements ILearningContentDAO {

    static public final String KEY_MAIN_PREFERENCE = "main_prefe";

    public static class SELF_DEFINED{
        static public final String KEY_LAST_POSITION_SELF_DEFINED = "last_position_self_defined";
        static public final String KEY_JSON_DATA_SELF_DEFINED = "json_data_self_defined"; //JSON and its meta data
    }

    private String mWordsToLearn = null;
    //	private String mWordsToLearn = "一二三四五六七";
    private Context mAndroidContext;
    private static final String LOG_TAG = "SelfDefinedDAO";

    private static SelfDefinedDAO mDaoInstance = null;
    private JSONArray mJsonArray;
    private ArrayList<ContentVO> mContentArray;


    /**
     * notice must ensure the androidContext initialized by setAndroidContext().
     * @return
     */
    static public SelfDefinedDAO getInstance(){

        if (mDaoInstance == null) {
            synchronized (SelfDefinedDAO.class) {
                if( mDaoInstance == null){
                    Log.i(LOG_TAG , "createSingleton");
                    mDaoInstance = new SelfDefinedDAO();
                }
            }
        }
        return mDaoInstance;
    }

    //TODO clear all settings
    public void init(Context aContext){
        mAndroidContext = aContext;
        mContentArray = new ArrayList<ContentVO>();
        loadNewContent(ContentType.SELF_DEFINED);
        Log.i(LOG_TAG, "done init.");
    }

    @Override
    public void loadNewContent(ContentType contentType) {
        Log.i(LOG_TAG, "going to switch content to type: " + contentType);
        SharedPreferences preference = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
        String[] strArr;

        if (!preference.contains(SELF_DEFINED.KEY_JSON_DATA_SELF_DEFINED)) { //Do nothing if already in SD card. onStart()-> getLastStatus() will get them.

            mWordsToLearn = mAndroidContext.getString(R.string.self_defined);
            strArr = mWordsToLearn.split(",");
            //init json strcture only for the fist time, the next time will use the persistent settings.
            for (int i = 0; i < strArr.length; i++) {
                ContentVO vo = new ContentVO();
                vo.setRawSequence(i);
                vo.setContent(strArr[i]);
                mContentArray.add(vo);
            }
            //Collections.shuffle(mContentArray);
            Log.w(LOG_TAG, "on init content array length: "+ mContentArray.size());
        }
    }
    /**
     * This is a singleton, call getInstance()
     */
    private SelfDefinedDAO(){
        Log.i(LOG_TAG , "init ContentDAO singleton");
    }

    /**
     * save current position, current vo array, how much marked as Known. //TODO
     * @param position
     */
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
        editor.putInt(SELF_DEFINED.KEY_LAST_POSITION_SELF_DEFINED, position);
        editor.putString(SELF_DEFINED.KEY_JSON_DATA_SELF_DEFINED, jArr);
        Log.v(LOG_TAG, "saveLastStatus(): position: "+ position + "Json Array to save: "+ jArr);
        editor.apply();
    }

    /**
     * Will be called from UI onStart().
     * @return last position
     */
    public int getLastStatus(){
        SharedPreferences pref = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
        int pos =0;
        // init the content array
        if (pref.contains(SELF_DEFINED.KEY_JSON_DATA_SELF_DEFINED)) {
            mContentArray.clear();
            try {
                mJsonArray = new JSONArray(pref.getString(SELF_DEFINED.KEY_JSON_DATA_SELF_DEFINED, ""));
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
        pos = pref.getInt(SELF_DEFINED.KEY_LAST_POSITION_SELF_DEFINED, 0);
        Log.v(LOG_TAG, "getLastStatus(): pos: " + pos);
        return pos;
    }

    public ContentVO getCurrentDisplay(int position) {
        Log.d(LOG_TAG, "current display: "+ position);
        return mContentArray.get(position);
    }

    public void markKnown(int position) {
        mContentArray.get(position).setKnown(true);
        Collections.sort(mContentArray);
        Log.d(LOG_TAG, "markKnown(): last item in colection: "+ mContentArray.get(mContentArray.size()-1).getContent());
    }

    public void markUnKnown(int position) {
        mContentArray.get(position).setKnown(false);
        Collections.sort(mContentArray);
        Log.d(LOG_TAG, "markUnKnown(): last item in colection: "+ mContentArray.get(mContentArray.size()-1).getContent());
    }

    public void increaseDisplayTimes(int pos) {
        Log.d(LOG_TAG, "increaseDisplayTimes() for: "+pos);
        mContentArray.get(pos).increaseDisplayTimes();
    }

    public void increaseLearned() {
//		mLearned += 1;
    }

    public int getContentLength(){
        return mContentArray.size();
    }


    @Override
    public void addContent(ContentVO aVO) {
        aVO.setRawSequence(mContentArray.size());
        mContentArray.add(aVO);
    }

    @Override
    public void delete(int index) {
        mContentArray.remove(index);
    }
}
