package com.example.wordbox;

import java.util.LinkedHashSet;
import java.util.Random;
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
	
	// For hooking into dictionary content provider.
	public static String AUTHORITY = "livio.pack.lang.en_US.DictionaryProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/dictionary");
	public static final Uri CONTENT_URI3 = Uri.parse("content://" + AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);
	public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;	
	
	public final static String QUERY = "com.example.wordbox.QUERY";
	private static final String TAG = "raytag";
	
	private String currentWord;
	private static Set<String> favourites = new LinkedHashSet<String>();
	private static final String PREFS_NAME = "WordBoxPrefsFile";
	
	private WebView webview;
	
	private Random random;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		random = new Random();
		loadFavourites();
		currentWord = null;
		
		webview = new WebView(this);
		WebViewClient mWebClient = new WebViewClient(){		
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Users select links to jump to a new word.
				// Log.v(TAG, "Link selected: " + url);
				loadDefinition(url);
				return true;
			}
		};
		webview.setWebViewClient(mWebClient);
		setContentView(webview);	
		
		// Get word from intent.
		Intent intent = getIntent();
		String query = intent.getStringExtra(QUERY);
		
		if (query == null) {
			//Log.v(TAG, "no query");
			if (favourites.size() == 0) {
				//TODO: Display first-time-user instructions.
			}
			else if (currentWord == null) {
				showRandomFavourite();
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
        		// Log.v(TAG, "textchange");
                return true; 
            } 

            @Override
            public boolean onQueryTextSubmit(String query) { 
                // TODO: load the definition in a new activity.
            	Log.v(TAG, "Query: " + query);
            	loadDefinition(query);
                return true; 
            } 
        };
        searchView.setOnQueryTextListener(queryTextListener);
        
        if (currentWord == null) {
        	menu.removeItem(R.id.action_toggle_favourite);
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
	        	Log.v(TAG, "list favs");
	        	showFavourites();
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
	
	// Get a random favourited word and display the definition.
	private void showRandomFavourite() {
		Log.v(TAG, "Loading random favourite...");
		String[] fwords = new String[favourites.size()];
		favourites.toArray(fwords);
		loadDefinition(fwords[random.nextInt(favourites.size())]);
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
    	Cursor cursor = getContentResolver().query(CONTENT_URI, null, null, new String[] {word}, null);
    	if ((cursor != null) && cursor.moveToFirst()) {
    		// int wIndex = cursor.getColumnIndexOrThrow(KEY_WORD);
    		int dIndex = cursor.getColumnIndexOrThrow(KEY_DEFINITION);
    		return cursor.getString(dIndex);
    	}
    	else {
    		// Word not found.
    		return null;
    	}
    }
	
	private void makeQuery(String word) {
		// Send word to new activity to display the definition.
		Intent intent = new Intent(this, DefinitionActivity.class);
    	intent.putExtra(QUERY, word);
    	startActivity(intent);
	}
	
	private void toggleFavourite(String word) {
		Log.v(TAG, "toggle fav: " + word);
		if (favourites.contains(word)) {
			favourites.remove(word);
		}
		else {
			favourites.add(word);
		}
		saveFavourites();
	}
	
	private boolean isFavourite(String word) {
		if (word == null)
			return false;
		
		return favourites.contains(word) ? true : false;
	}
	
	private void saveFavourites() {
		//Log.v(TAG, "Saving favs: " + favourites.toString());
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear(); // No idea why, but this is necessary.
		editor.putStringSet("favourites", favourites);
		editor.apply();
	}
	
	private void loadFavourites() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		favourites = settings.getStringSet("favourites", null);
		Log.v(TAG, "Loaded favs: " + favourites.toString());
	}
	
	public static Set<String> getFavourites() {
		return favourites;
	}
	
}
