package com.example.wordbox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
	private static Set<String> favourites;
	public static final String PREFS_NAME = "WordBoxPrefsFile";
	
	private WebView webview;
	private SearchView searchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_definition);
		
		dictionary = Dictionary.getInstance();
		favouritesManager = FavouritesManager.getInstance();
		if (favourites == null) {
			favourites = new LinkedHashSet<String>();
		}
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
		
		handleIntent(getIntent());
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		// Get word from intent and load definition.
		if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
			Log.v(TAG, "Handling search intent.");
			String query = intent.getStringExtra(SearchManager.QUERY);
			// Log.v(TAG, "QUERY MADE: " + query);
			loadDefinition(query);
		}
		else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
		    // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
		    Log.v(TAG, "got the data");
		    // Uri data = intent.getData();
		    // showResult(data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.definition, menu);
		
		// Get the SearchView and set the searchable configuration.
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		// Assumption: current activity is the searchable filter.
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		// Make sure the icon is always in the action bar.
		// searchView.setIconifiedByDefault(false); // Ugly. Keeps icon up even on expanded search view...
		
		/* old search view
		// Configure the collapsible search item.
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        	@Override 
            public boolean onQueryTextChange(String newText) { 
                // List suggested words.
        		ArrayList<String> partialMatches = partialMatch(newText);
        		if (partialMatches != null) {
        			Log.v(TAG, partialMatches.toString());        			
        		}
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
        */
		
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
		query = query.trim();
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
		searchView.setQuery(word, true);
	}
	
	public static void makeQuery(Activity activity, String word) {
		// Create a search intent.
		Intent intent = new Intent(activity, DefinitionActivity.class);
		intent.setAction(Intent.ACTION_SEARCH);
    	intent.putExtra(SearchManager.QUERY, word);
    	activity.startActivity(intent);
	}
	
	public ArrayList<String> partialMatch(String word) {
		return dictionary.partialMatch(this, word);
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
