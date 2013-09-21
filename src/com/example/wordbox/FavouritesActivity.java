package com.example.wordbox;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class FavouritesActivity extends Activity {
	
	private ListView wordsListView;
	private ArrayAdapter<String> arrayAdapter;
	private String[] words;
	private OnItemClickListener wordClickHandler;
	
	private static final String TAG = "raytag";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favourites);
		// Show the Up button in the action bar.
		setupActionBar();
		
		wordsListView = (ListView) findViewById(R.id.favouritesListView);
		
		Set<String> favourites = DefinitionActivity.getFavourites();
		words = new String[favourites.size()];
		favourites.toArray(words);
		
		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, words);
		wordsListView.setAdapter(arrayAdapter);
		
		wordClickHandler = new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				Log.v(TAG, "item cliked: " + words[position]);
				makeQuery(words[position]);
			}
		};
		wordsListView.setOnItemClickListener(wordClickHandler);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.favourites, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void makeQuery(String word) {
		// Send word to new activity to display the definition.
		Intent intent = new Intent(this, DefinitionActivity.class);
    	intent.putExtra(DefinitionActivity.QUERY, word);
    	startActivity(intent);
	}

}
