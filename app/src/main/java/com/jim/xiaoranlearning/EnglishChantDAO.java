package com.jim.xiaoranlearning;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

public class EnglishChantDAO implements ILearningContentDAO {

    static public final String KEY_MAIN_PREFERENCE = "main_prefe";

    public static class ENGLISH_CHANT{
        static public final String KEY_LAST_POSITION_ENGLISH_CHANT = "last_position_english_CHANT";
        static public final String KEY_JSON_DATA_ENGLISH_CHANT = "json_data_English_CHANT"; //JSON and its meta data
    }

    private String mWordsToLearn = null;
    //	private String mWordsToLearn = "一二三四五六七";
    private Context mAndroidContext;
    private static final String LOG_TAG = "EnglishChantDAO";

    private static EnglishChantDAO mDaoInstance = null;
    private JSONArray mJsonArray;
    private ArrayList<ContentVO> mContentArray;


    /**
     * notice must ensure the androidContext initialized by setAndroidContext().
     * @return
     */
    static public EnglishChantDAO getInstance(){

        if (mDaoInstance == null) {
            synchronized (EnglishChantDAO.class) {
                if( mDaoInstance == null){
                    Log.i(LOG_TAG , "createSingleton");
                    mDaoInstance = new EnglishChantDAO();
                }
            }
        }
        return mDaoInstance;
    }

    //TODO clear all settings
    public void init(Context aContext){
        mAndroidContext = aContext;
        mContentArray = new ArrayList<ContentVO>();
        loadNewContent(ContentType.ENGLISH_CHANT);
        Log.i(LOG_TAG, "done init.");
    }

    @Override
    public void loadNewContent(ContentType contentType) {
        Log.i(LOG_TAG, "going to switch content to type: " + contentType);
        SharedPreferences preference = mAndroidContext.getSharedPreferences(KEY_MAIN_PREFERENCE, 0);
        String[] strArr;

        if (!preference.contains(ENGLISH_CHANT.KEY_JSON_DATA_ENGLISH_CHANT)) { //Do nothing if already in SD card. onStart()-> getLastStatus() will get them.

            mWordsToLearn = mAndroidContext.getString(R.string.English_chant);
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
    private EnglishChantDAO(){
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
        editor.putInt(ENGLISH_CHANT.KEY_LAST_POSITION_ENGLISH_CHANT, position);
        editor.putString(ENGLISH_CHANT.KEY_JSON_DATA_ENGLISH_CHANT, jArr);
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
        if (pref.contains(ENGLISH_CHANT.KEY_JSON_DATA_ENGLISH_CHANT)) {
            mContentArray.clear();
            try {
                mJsonArray = new JSONArray(pref.getString(ENGLISH_CHANT.KEY_JSON_DATA_ENGLISH_CHANT, ""));
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
        pos = pref.getInt(ENGLISH_CHANT.KEY_LAST_POSITION_ENGLISH_CHANT, 0);
        Log.v(LOG_TAG, "getLastStatus(): pos: " + pos);
        return pos;
    }

    public ContentVO getCurrentDisplay(int position) {
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
