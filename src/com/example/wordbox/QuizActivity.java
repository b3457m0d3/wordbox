package com.example.wordbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putString(DummySectionFragment.ARG_WORD_DEFINITION, fm.getDefinition(favourites.get(position)));
			fragment.setArguments(args);
			return fragment;
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

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		
		public static final String ARG_WORD_DEFINITION = "word_definition";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_quiz_dummy,
					container, false);
			WebView dummyWebView = (WebView) rootView.findViewById(R.id.quiz_definition_display);
			dummyWebView.loadData(getArguments().getString(ARG_WORD_DEFINITION), "text/html", null);
			
			WebViewClient mWebClient = new WebViewClient(){		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					// Users select links to jump to a new word.
					DefinitionActivity.makeQuery(getActivity(), url);
					return true;
				}
			};
			dummyWebView.setWebViewClient(mWebClient);
			
			
			return rootView;
		}
	}

}
