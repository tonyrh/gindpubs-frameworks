package com.giniem.gindpubs.workers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.giniem.gindpubs.BookJson;
import com.giniem.gindpubs.Configuration;
import com.giniem.gindpubs.views.MagazineThumb;

public class BookJsonParserTask extends AsyncTask<String, Long, BookJson> {

	private File magazinesDirectory;
	
	private MagazineThumb magThumb;

	public BookJsonParserTask(Context context) {
		this.magazinesDirectory = Configuration.getDiskDir(context);
	}
	
	public BookJsonParserTask(MagazineThumb thumb) {
		this(thumb.getContext());
		this.magThumb = thumb;
	}

	@Override
	protected BookJson doInBackground(String... params) {
		BookJson result  = null;
		
		String workingDir = this.magazinesDirectory.getPath() + File.separator;
		File book = new File(workingDir + params[0] + File.separator + Configuration.getBOOKJSON_NAME());
		
		String rawJson;
		
		try {
			FileInputStream input = new FileInputStream(book);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		      sb.append(line).append("\n");
		    }
		    rawJson = sb.toString();

		    Log.d(this.getClass().toString(), "Book.json read from file: " + rawJson);
		    
		    boolean valid = this.validateJson(rawJson);
		    
		    if (valid) {
			    Log.d(this.getClass().toString(), "Book.json is valid.");
		    	result = new BookJson();
		    	result.fromJson(rawJson);
		    } else {
			    Log.d(this.getClass().toString(), "Book.json is NOT valid.");
		    }
			
			input.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = null;
		}	    
		
		return result;
	}

	private boolean validateJson(final String rawJson) {
		boolean result = true;
		String required[] = {"contents", "zoomable", "-baker-index-height", "-baker-rendering"};
		
		try {
			JSONObject json = new JSONObject(rawJson);
			
			for (String property : required) {
				if (!json.has(property)) {
					Log.e(this.getClass().toString(), "Property missing from json: " + property);
					result = false;
				}
			}
			
			if (json.has("contents")) {
				
				// If the contents is not array, this will result on an exception causing the book
				// to be invalid.
				JSONArray contents = new JSONArray(json.getString("contents"));
				if (contents.length() < 0) {
					result = false;
				}
			}
			
		} catch (JSONException e) {
			result = false;
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	protected void onProgressUpdate(Long... progress) {
	}

	@Override
	protected void onPostExecute(final BookJson result) {
		this.magThumb.setBookJson(result);
	}

}
