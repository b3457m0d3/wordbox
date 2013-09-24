package com.example.wordbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

public class FavouritesManager {
	
	public static final int ORDER_RANDOM = 0;
	public static final int ORDER_CHRONOLOGICAL = 1;
	public static final int ORDER_ALPHABETICAL = 2;

	private static final String PREFS_NAME = "WordBoxPrefsFile";
	private static final String TAG = "raytag";
	private static final String EXT_TIME = "_TIME";
	private static final String EXT_DEFINITION = "_DEF";
	
	private static FavouritesManager fm;
	
	private Dictionary dictionary;
	
	private Set<String> favourites;
	private HashMap<String, String> favouritesDefinitions;
	private HashMap<String, String> favouritesTimes;
	
	private FavouritesManager() {
		favourites = new HashSet<String>();
		favouritesDefinitions = new HashMap<String, String>();
		favouritesTimes = new HashMap<String, String>();
		
		dictionary = Dictionary.getInstance();
	}
	
	public static FavouritesManager getInstance() {
		if (fm == null) {
			fm = new FavouritesManager();
		}
		return fm;
	}
	
	public boolean isFavourite(String word) {
		if (word == null)
			return false;
		
		return favourites.contains(word);
	}
	
	public ArrayList<String> getFavourites(int ordering) {
		ArrayList<String> favouriteWords = new ArrayList<String>();
		favouriteWords.addAll(favourites);
		
		switch (ordering) {
			case ORDER_CHRONOLOGICAL:
				//TODO
				break;
			case ORDER_ALPHABETICAL:
				//TODO
				break;
			case ORDER_RANDOM:
			default:
				shuffleList(favouriteWords);
				break;
		}
		
		return favouriteWords;
	}
	
	private <T> void shuffleList(List<T> list) {
		Random rand = new Random();
		for (int i = 0; i < list.size(); i ++) {
			int j = rand.nextInt(list.size());
			T temp = list.get(j);
			list.set(j, list.get(i));
			list.set(i, temp);
		}
	}
	
	public String getDefinition(String word) {
		return favouritesDefinitions.get(word);
	}
	
	public void toggleFavourite(Activity activity, String word) {
		if (favourites.contains(word)) {
			removeFavourite(word);
		}
		else {
			addFavourite(activity, word);
		}
		
		saveFavourites(activity);
	}
	
	public void addFavourite(Activity activity, String word) {
		favourites.add(word);
		
		Time t = new Time();
		favouritesTimes.put(word, t.toString());
		
		favouritesDefinitions.put(word, dictionary.query(activity, word));
	}
	
	public void removeFavourite(String word) {
		favourites.remove(word);
		favouritesTimes.remove(word);
		favouritesDefinitions.remove(word);
	}
	
	
	public void saveFavourites(Activity activity) {
		//Log.v(TAG, "Saving favs: " + favourites.toString());
		SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear(); // No idea why, but this is necessary.
		editor.putStringSet("favourites", favourites);
		
		// Also want to store the definitions and times.
		for (String f : favourites) {
			editor.putString(f + EXT_TIME, favouritesTimes.get(f));
			editor.putString(f + EXT_DEFINITION, favouritesDefinitions.get(f));
			//Log.v(TAG, "SAVED DEF: " + favouritesDefinitions.get(f));
		}
		
		editor.apply();
	}
	
	public void loadFavourites(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
		favourites = settings.getStringSet("favourites", null);
		
		favouritesTimes.clear();
		favouritesDefinitions.clear();
		for (String f : favourites) {
			favouritesTimes.put(f, settings.getString(f + EXT_TIME, null));
			favouritesDefinitions.put(f, settings.getString(f + EXT_DEFINITION, null));
			//Log.v(TAG, "LOADED DEF: " + favouritesDefinitions.get(f));
		}
				
		//Log.v(TAG, "Loaded favs: " + favourites.toString());
	}
}
