package com.example.wordbox;

import java.util.ArrayList;

import android.app.Activity;
import android.app.SearchManager;
import android.database.Cursor;
import android.net.Uri;

public final class Dictionary {
	
	public static String AUTHORITY = "livio.pack.lang.en_US.DictionaryProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/dictionary");
	public static final Uri CONTENT_PARTIAL_URI = Uri.parse("content://" + AUTHORITY + "/search_suggest_query");
	// public static final Uri CONTENT_URI3 = Uri.parse("content://" + AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);
	public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;	
	
	private static Dictionary dictionary;
	
	private Dictionary() {
		;
	}
	
	public static Dictionary getInstance() {
		if (dictionary == null) {
			dictionary = new Dictionary();
		}
		return dictionary;
	}
	
	public String query(Activity activity, String word) {
    	Cursor cursor = activity.getContentResolver().query(CONTENT_URI, null, null, new String[] {word}, null);
    	if ((cursor != null) && cursor.moveToFirst()) {
    		int wIndex = cursor.getColumnIndexOrThrow(KEY_WORD);
    		int dIndex = cursor.getColumnIndexOrThrow(KEY_DEFINITION);
    		String def = "<b>" + cursor.getString(wIndex) + "</b><hr style='clear:both'>" + cursor.getString(dIndex);
    		return def;
    	}
    	else {
    		// Word not found.
    		return "Word not found...";
    	}
    }
	
	public ArrayList<String> partialMatch(Activity activity, String partialWord) {
		Cursor cursor = activity.getContentResolver().query(CONTENT_PARTIAL_URI, null, null, new String[] {partialWord}, null);
		ArrayList<String> suggestions = new ArrayList<String>();
		if ((cursor != null) && cursor.moveToFirst()) {
			do {
				int wIndex = cursor.getColumnIndexOrThrow(KEY_WORD);
				suggestions.add(cursor.getString(wIndex));
			} while (cursor.moveToNext());
    		
    		return suggestions;
    	}
    	else {
    		return null;
    	}
	}
	
}
