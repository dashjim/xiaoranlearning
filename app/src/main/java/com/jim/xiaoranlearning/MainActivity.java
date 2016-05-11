package com.jim.xiaoranlearning;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//OnInitListener is the interface for TTS
public class MainActivity extends FragmentActivity {

	private static final String LOG_TAG = "MainActivity";
	private static Context mAndroidContext;
    private static int mDeviceWidth = 720;
	private static float mScreenScaleDensity = 5;
	/**
	 * FIXME
	 * The {@link PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link FragmentStatePagerAdapter}.
	 */
	static SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;
	static private ILearningContantDAO mContentDao;
	//static private int mCurrentPosition = 0;
	static private Activity mCurrentActivity;
	static private TextToSpeech mTTS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        refreshDeviceWidth();

		//init ContentDAO
		mAndroidContext = getApplicationContext();
		mContentDao = Chinese265DAO.getInstance();
//		mContentDao = EnglishWordsDAO.getInstance();
		mContentDao.init(mAndroidContext);
		mTTS = new TextToSpeech(this, new TTSListener());
		
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mCurrentActivity = MainActivity.this;
	}

    private void refreshDeviceWidth() {
        mDeviceWidth = this.getResources().getDisplayMetrics().widthPixels;
		mScreenScaleDensity = this.getResources().getDisplayMetrics().scaledDensity;
        Log.d(LOG_TAG, "device width is: " + mDeviceWidth);
    }

    @Override
	protected void onStart() {
		super.onStart();
		mSectionsPagerAdapter.notifyDataSetChanged();
		mViewPager.setCurrentItem(mContentDao.getLastStatus());
		readEnglish();
		Log.d(LOG_TAG, "onStart(): resume current position: "+ mViewPager.getCurrentItem());
	}

    @Override
    protected void onResume(){
        super.onResume();
		mSectionsPagerAdapter.notifyDataSetChanged();
        refreshDeviceWidth(); // For rotation
    }
	
	@Override
	protected void onStop() {
		super.onStop();
		mContentDao.saveLastStatus(mViewPager.getCurrentItem() );
		Log.d(LOG_TAG, "onStop(): saved current position: "+ mViewPager.getCurrentItem());
	}
	@Override
	protected void onDestroy() {
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}
	
	/***** menu **********/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_English_words:
            	if (!(mContentDao instanceof EnglishWordsDAO)) {//Need to switch
            		onStop();
            		//load new data and status
            		mContentDao = EnglishWordsDAO.getInstance();
            		mContentDao.init(mAndroidContext);
            		onStart();
				}
        		mSectionsPagerAdapter.notifyDataSetChanged();
            	break;
            case R.id.action_Chinese258:
            	if (!(mContentDao instanceof Chinese265DAO)) {//Need to switch
            		onStop();
            		//load new data and status
            		mContentDao = Chinese265DAO.getInstance();
            		mContentDao.init(mAndroidContext);
            		onStart();
            	}
            	mSectionsPagerAdapter.notifyDataSetChanged();
            	break;
            case R.id.action_English_chant:
                if (!(mContentDao instanceof EnglishChantDAO)) {//Need to switch
                    onStop();
                    //load new data and status
                    mContentDao = EnglishChantDAO.getInstance();
                    mContentDao.init(mAndroidContext);
                    onStart();
                }
                mSectionsPagerAdapter.notifyDataSetChanged();
                break;
            case R.id.action_Manual:
                String url = "http://blog.sevenche.com/2015/11/Android-app-for-kids-to-learn-Chinese/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            default: //Chinese 258
            	break;
        }
        return true;
    }
    
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			// mCurrentPosition = position;
			Log.d(LOG_TAG, "getItem(): "+ position);
			Fragment fragment = new OneSlide();
			Bundle args = new Bundle();
			args.putInt(OneSlide.ARG_SECTION_NUMBER, position + 1);
			args.putInt(OneSlide.ARG_SECTION_READ_TIMES, mContentDao.getCurrentDisplay(position).getDisplayTimes());
			args.putCharSequence(OneSlide.ARG_SECTION_CONTENT, mContentDao.getCurrentDisplay(position).getContent());
            args.putBoolean(OneSlide.ARG_SECTION_CONTENT_REMEMBERED, mContentDao.getCurrentDisplay(position).isKnown());
			mContentDao.increaseDisplayTimes(position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(LOG_TAG, "instantiateItem(): "+ position);
			return super.instantiateItem(container, position);
		}
		
		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
		@Override
		public int getCount() {
			return mContentDao.getContentLength();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			return mContentDao.getCurrentDisplay(position).getContent();
		}
	}

	/**
	 * The main display area which display the slideable content
	 */
	public static class OneSlide extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String ARG_SECTION_READ_TIMES = "section_read_times";
		public static final String ARG_SECTION_CONTENT = "section_content";
		public static final String ARG_SECTION_CONTENT_REMEMBERED = "section_content_remembered";

		public OneSlide() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView sequenceTextView = (TextView) rootView.findViewById(R.id.section_label);
			TextView contentTextView = (TextView) rootView.findViewById(R.id.section_content);
			
			sequenceTextView.setText("NO. "+Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER))
                                    + " - Learned "
                                    + Integer.toString(getArguments().getInt(ARG_SECTION_READ_TIMES))
                                    + " times"
            );
			CharSequence currentText = getArguments().getCharSequence(ARG_SECTION_CONTENT);
			boolean isKnown = getArguments().getBoolean(ARG_SECTION_CONTENT_REMEMBERED);
			contentTextView.setText(currentText);
			
            if (mContentDao instanceof Chinese265DAO) {
                // need to change from pix to sp size
                contentTextView.setTextSize((float)(mDeviceWidth/currentText.length()/ mScreenScaleDensity * 0.8));
            }else if(mContentDao instanceof EnglishWordsDAO){
                contentTextView.setTextSize((float)(mDeviceWidth/currentText.length()/ mScreenScaleDensity));
            }else{
                contentTextView.setTextSize((float)(mDeviceWidth/currentText.length()/ mScreenScaleDensity));
            }

            if (isKnown){
                contentTextView.setTextColor(Color.BLUE);
            }
            Log.d(LOG_TAG, "current text size." + mDeviceWidth / currentText.length() / mScreenScaleDensity * 0.8);

            contentTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // a workaround of a big size text display bug
			contentTextView.setOnLongClickListener(new LongClickHandler());
			contentTextView.setOnTouchListener(new TouchClickHandler());
			
			readEnglish();
			Log.i(LOG_TAG, "onCreatView() created display: "+ currentText + " current position: "+mViewPager.getCurrentItem());
			return rootView;
		}
	}
	
	/**
	 * allow user to move a display to left.
	 * @author ji5
	 */
	public static class LongClickHandler implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			Log.d(LOG_TAG,"long pressed" + mViewPager.getCurrentItem());
			new AlertDialog.Builder(mCurrentActivity)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(mAndroidContext.getString(R.string.ui_do_you_know_it))
					.setMessage(mAndroidContext.getString(R.string.ui_move_current_word_to_left))
					.setPositiveButton(mAndroidContext.getString(R.string.ui_OK), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									markAsKnown();
								}
							})
					.setNegativeButton(mAndroidContext.getString(R.string.ui_Delete), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delete();
						}
					})
					.setNeutralButton(mAndroidContext.getString(R.string.ui_Cancel), null)
					.show();

			return true; //don't want others to handle the event any more!
		}
		
		public void markAsKnown(){
			mContentDao.markKnown(mViewPager.getCurrentItem());
			mSectionsPagerAdapter.notifyDataSetChanged();//Wish UI could change too.
			Toast.makeText(mCurrentActivity, mAndroidContext.getString(R.string.ui_next_time_takes_effect), Toast.LENGTH_SHORT).show();
			Log.d(LOG_TAG, "markAsKnown(): " + mViewPager.getCurrentItem());
		}
		public void delete(){
			mContentDao.delete(mViewPager.getCurrentItem());
		}
	}

	/**
	 * allow user to move a display to left.
	 * @author ji5
	 */
	public static class TouchClickHandler implements View.OnTouchListener{

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {

			Log.d(LOG_TAG, "long pressed" + mViewPager.getCurrentItem());
			final EditText input = new EditText(mCurrentActivity);
			new AlertDialog.Builder(mCurrentActivity)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(input)
					.setTitle(mAndroidContext.getString(R.string.ui_add_new))
					.setMessage(mAndroidContext.getString(R.string.ui_add_new_card))
					.setPositiveButton(mAndroidContext.getString(R.string.ui_OK),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									saveNewCard(input.getText().toString().length() > 1
											? input.getText().toString() : ";-)");
									mSectionsPagerAdapter.notifyDataSetChanged();

								}
							}).setNegativeButton(mAndroidContext.getString(R.string.ui_Cancel), null).show();

			return true; //don't want others to handle the event any more!
		}

		public void saveNewCard(String content){
			ContentVO vo = new ContentVO();
			vo.setContent(content);
			mContentDao.addContent(vo);
		}
	}

	private class TTSListener implements OnInitListener {
		/**
		 * the method defined for TTS
		 */
		@Override
		public void onInit(int status) {

			// TTS is successfully initialized
			if (status == TextToSpeech.SUCCESS) {
				// Setting speech language
				int result = mTTS.setLanguage(Locale.US);
				// If your device doesn't support language you set above
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					// Cook simple toast message with message
					Toast.makeText(mAndroidContext, "Language not supported",
							Toast.LENGTH_LONG).show();
					Log.e("TTS", "Language is not supported");
				}
				// Enable the button - It was disabled in main.xml (Go back and Check it)
				else {
					Log.i("TTS", "TTS enabled");
				}
				// TTS is not initialized properly
			} else {
				Toast.makeText(mAndroidContext, "TTS Initilization Failed",
						Toast.LENGTH_LONG).show();
				Log.e("TTS", "Initilization Failed");
			}
		}
	}
	
	private static void readEnglish() {
		//TTS
		if (mContentDao instanceof EnglishWordsDAO) {
			String content = mContentDao.getCurrentDisplay(mViewPager.getCurrentItem()).getContent();
			char[] array = content.toCharArray();
			String abc = ". ";
			for (int i = 0; i < array.length; i++) {
				abc += array[i]+".. ";
			}
			Log.d(LOG_TAG, "talking: "+ content+abc+content);
			mTTS.speak(content+abc+content, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
