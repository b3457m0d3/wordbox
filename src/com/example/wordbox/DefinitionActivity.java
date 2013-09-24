package com.example.wordbox;

import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;

public class DefinitionActivity extends Activity {
	
	public final static String QUERY = "com.example.wordbox.QUERY";
	private static final String TAG = "raytag";
	
	private Dictionary dictionary;
	
	private FavouritesManager favouritesManager;
	
	private String currentWord;
	private static Set<String> favourites = new LinkedHashSet<String>();
	public static final String PREFS_NAME = "WordBoxPrefsFile";
	
	WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_definition);
		
		dictionary = Dictionary.getInstance();
		favouritesManager = FavouritesManager.getInstance();
		loadFavourites();
		currentWord = null;
		
		webview = (WebView) findViewById(R.id.definition_display);
		WebViewClient mWebClient = new WebViewClient(){		
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Users select links to jump to a new word.
				// Log.v(TAG, "Link selected: " + url);
				makeQuery(url);
				return true;
			}
		};
		webview.setWebViewClient(mWebClient);
		
		// Get word from intent.
		Intent intent = getIntent();
		String query = intent.getStringExtra(QUERY);
		
		if (query == null) {
			//Log.v(TAG, "no query");
			if (favourites.size() == 0) {
				//TODO: Display first-time-user instructions.
			}
			else if (currentWord == null) {
				//showRandomFavourite(); // This is confusing for users I think. 
			}
		}
		else {
			Log.v(TAG, "QUERY MADE: " + query);
			currentWord = query.trim();
			
			loadDefinition(currentWord);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.definition, menu);
		
		// Configure the collapsable search item.
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        	@Override 
            public boolean onQueryTextChange(String newText) { 
                // TODO: list suggestions
        		// Log.v(TAG, "text change");
                return true; 
            } 

            @Override
            public boolean onQueryTextSubmit(String query) { 
            	// Log.v(TAG, "Query: " + query);
            	makeQuery(query);
                return true; 
            } 
        };
        searchView.setOnQueryTextListener(queryTextListener);
        
        if (currentWord == null) {
        	//menu.removeItem(R.id.action_toggle_favourite);
        }
        else if (isFavourite(currentWord)) {
        	MenuItem toggleFavItem = menu.findItem(R.id.action_toggle_favourite);
        	toggleFavItem.setIcon(R.drawable.ic_rating_important);
        }
        
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_toggle_favourite:
	        	if (currentWord == null) 
	        		return true;
	        	
	        	toggleFavourite(currentWord);
	        	
	        	// TODO: check if the current word is already in the favourited dictionary.
	        	if (isFavourite(currentWord)) {
	        		item.setIcon(R.drawable.ic_rating_important);	        		
	        	}
	        	else {
	        		item.setIcon(R.drawable.ic_rating_not_important);
	        	}
	            return true;
	        case R.id.action_list_favourites:
	        	// Log.v(TAG, "list favs");
	        	showFavourites();
	        	return true;
	        case R.id.action_quiz:
	        	// Log.v(TAG, "starting quiz");
	        	launchQuiz();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		saveFavourites();
	}
	
	private void loadDefinition(String query) {
		// Look up definition for query and load into current WebView.
		
		String definition = getDefinition(query);
		// TODO: deal with missed lookups (word not found in dictionary).
		currentWord = query;
		webview.loadData(definition, "text/html", null);
	}
	
	private void showFavourites() {
		Intent intent = new Intent(this, FavouritesActivity.class);
		startActivity(intent);
	}
	
	public String getDefinition(String word) {
		
		return dictionary.query(this, word);
    }
	
	private void makeQuery(String word) {
		// Send word to new activity to display the definition.
		Intent intent = new Intent(this, DefinitionActivity.class);
    	intent.putExtra(QUERY, word);
    	startActivity(intent);
	}
	
	public static void makeQuery(Activity activity, String word) {
		// Send word to new activity to display the definition.
		Intent intent = new Intent(activity, DefinitionActivity.class);
    	intent.putExtra(QUERY, word);
    	activity.startActivity(intent);
	}
	
	private void toggleFavourite(String word) {
		favouritesManager.toggleFavourite(this, word);
	}
	
	private boolean isFavourite(String word) {	
		return favouritesManager.isFavourite(word);
	}
	
	private void saveFavourites() {
		favouritesManager.saveFavourites(this);
	}
	
	private void loadFavourites() {
		favouritesManager.loadFavourites(this);
	}
		
	private void launchQuiz() {
		Intent intent = new Intent(this, QuizActivity.class);
    	startActivity(intent);
	}
	
}
