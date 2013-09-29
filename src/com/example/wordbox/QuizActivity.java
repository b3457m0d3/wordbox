package com.example.wordbox;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class QuizActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	private static final String TAG = "raytag";
	
	private FavouritesManager fm;
	
	private ArrayList<String> favourites;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		fm = FavouritesManager.getInstance();
		fm.loadFavourites(this);
		
		// Load info to be used in the quiz.
		favourites = fm.getFavourites(FavouritesManager.ORDER_RANDOM);
		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quiz, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.action_shuffle:
				gotoQuiz();
				return true;
			case R.id.action_list_favourites:
				gotoFavourites();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		fm.saveFavourites(this);
	}
	
	private void gotoFavourites() {
		Intent intent = new Intent(this, FavouritesActivity.class);
		startActivity(intent);
	}
	
	private void gotoQuiz() {
		Intent intent = new Intent(this, QuizActivity.class);
		startActivity(intent);
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			
			Fragment fragment = new FlashCard();
			Bundle args = new Bundle();
			args.putString(FlashCard.ARG_WORD, favourites.get(position));
			args.putString(FlashCard.ARG_DEFINITION, fm.getDefinition(favourites.get(position)));
			fragment.setArguments(args);
			return fragment;
			
//			Fragment fragment = new CardFrontFragment();
//			Bundle args = new Bundle();
//			args.putString(CardFrontFragment.ARG_WORD, favourites.get(position));
//			fragment.setArguments(args);
//			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			// return 3;
			return favourites.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return favourites.get(position);
		}
	}
	
	public static class FlashCard extends Fragment implements OnTouchListener, Handler.Callback {
		public static final String ARG_WORD = "flashcard_word";
		public static final String ARG_DEFINITION = "flashcard_definition";
		public static final int CLICK_ON_WEBVIEW = 1;
		public static final int CLICK_ON_URL = 2;
		
		private final Handler handler = new Handler(this);
		
		
		private TextView wordView;
		private WebView definitionView;
		private WebViewClient defintionViewClient;
		private int animationDuration;
		
		private String word;
		private String definition;
		private String lastURL;	
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_quiz_card, container, false);

			wordView = (TextView) rootView.findViewById(R.id.quiz_word);
			definitionView = (WebView) rootView.findViewById(R.id.quiz_definition);
			
			// Initially hide the definition.
			definitionView.setVisibility(View.GONE);
			
			animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
			
			word = getArguments().getString(ARG_WORD);
			wordView.setText(word);
			wordView.setBackgroundResource(android.R.color.white);
			wordView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v(TAG, "wordView clicked");
					crossfade(definitionView, wordView);
				}
			});
			
			definitionView.setOnTouchListener(this);			
			defintionViewClient = new WebViewClient(){		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// Users select links to jump to a new word.
					lastURL = url;
					handler.sendEmptyMessage(CLICK_ON_URL);
					return true;
				}
			};
			definitionView.setWebViewClient(defintionViewClient);
			definition = getArguments().getString(ARG_DEFINITION);
			definitionView.loadData(definition, "text/html", null);
			
			return rootView;
		}
		
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.quiz_definition && event.getAction() == MotionEvent.ACTION_UP){
            	handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 400);
            }
            return false;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == CLICK_ON_URL){
                handler.removeMessages(CLICK_ON_WEBVIEW);
				DefinitionActivity.makeQuery(getActivity(), lastURL);
                return true;
            }
            if (msg.what == CLICK_ON_WEBVIEW){
                crossfade(wordView, definitionView);
                return true;
            }
            return false;
        }
		
		private void crossfade(View viewIn, final View viewOut) {
			// Set the content view to 0% opacity but visible, so that it is visible
		    // (but fully transparent) during the animation.
		    viewIn.setAlpha(0f);
		    viewIn.setVisibility(View.VISIBLE);

		    // Animate the content view to 100% opacity, and clear any animation
		    // listener set on the view.
		    viewIn.animate()
		            .alpha(1f)
		            .setDuration(animationDuration)
		            .setListener(null);

		    // Animate the loading view to 0% opacity. After the animation ends,
		    // set its visibility to GONE as an optimization step (it won't
		    // participate in layout passes, etc.)
		    viewOut.animate()
		            .alpha(0f)
		            .setDuration(animationDuration)
		            .setListener(new AnimatorListenerAdapter() {
		                @Override
		                public void onAnimationEnd(Animator animation) {
		                	viewOut.setVisibility(View.GONE);
		                }
		            });

		}
		
	}
	
	/**
     * A fragment representing the front of the card.
     */
	public static class CardFrontFragment extends Fragment {
		
		public static final String ARG_WORD = "word";

		public CardFrontFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_quiz_card_front,
					container, false);
			TextView textView = (TextView) rootView.findViewById(R.id.quiz_word_display);
			textView.setText(getArguments().getString(ARG_WORD));
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v(TAG, "word is clicked");
				}
			});
			
			return rootView;
		}
	}
		
	/**
     * A fragment representing the back of the card.
     */
	public static class CardBackFragment extends Fragment implements OnTouchListener, Handler.Callback {
    	
		public static final String ARG_WORD_DEFINITION = "word_definition";
    	public static final int CLICK_ON_WEBVIEW = 1;
    	public static final int CLICK_ON_URL = 2;
		
    	private final Handler handler = new Handler(this);
    	
    	private WebView webView;
    	private WebViewClient client;
    	
    	private String lastURL;
    	
		public CardBackFragment() {}		
		
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	
        	View rootView = inflater.inflate(R.layout.fragment_quiz_card_back,
					container, false);
			webView = (WebView) rootView.findViewById(R.id.quiz_definition_display);
			webView.setOnTouchListener(this);
			
			client = new WebViewClient(){		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// Users select links to jump to a new word.
					lastURL = url;
					handler.sendEmptyMessage(CLICK_ON_URL);
					return true;
				}
			};
			webView.setWebViewClient(client);
			webView.loadData(getArguments().getString(ARG_WORD_DEFINITION), "text/html", null);
			
			return rootView;
        }
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.quiz_definition_display && event.getAction() == MotionEvent.ACTION_DOWN){
                handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 500);
            }
            return false;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == CLICK_ON_URL){
                handler.removeMessages(CLICK_ON_WEBVIEW);
				DefinitionActivity.makeQuery(getActivity(), lastURL);
                return true;
            }
            if (msg.what == CLICK_ON_WEBVIEW){
                Log.v(TAG, "WebView clicked");
                return true;
            }
            return false;
        }
    }

	
}
