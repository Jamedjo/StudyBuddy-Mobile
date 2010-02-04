package com.studybuddy.noteviewer;

import com.studybuddy.noteviewer.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.graphics.*;

public class NoteViewer extends Activity {
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Gallery g = (Gallery) findViewById(R.id.gallery);
	    Gallery sg = (Gallery) findViewById(R.id.gallery);
	    g.setAdapter(new ImageAdapter(this));
	    sg.setAdapter(new ImageAdapter(this));

	    g.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(NoteViewer.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;
	
	    private Integer[] mImageIds = {
	            R.drawable.chrysanthemum,
	            R.drawable.desert,
	            R.drawable.hydrangeas,
	            R.drawable.jellyfish,
	            R.drawable.koala,
	            R.drawable.lighthouse,
	            R.drawable.penguins,
	            R.drawable.tulips
	    };
	    
	    public ImageAdapter(Context c) {
	        mContext = c;
	        //TypedArray a = obtainStyledAttributes(android.R.styleable.Theme);
	        //mGalleryItemBackground = a.getResourceId(android.R.styleable.Theme_galleryItemBackground, 0);
	        //a.recycle();
	    }
	
	    public int getCount() {
	        return mImageIds.length;
	    }
	
	    public Object getItem(int position) {
	        	return position;
	    	}
	
	    	public long getItemId(int position) {
	        	return position;
	    	}
	
	    	public View getView(int position, View convertView, ViewGroup parent) {
	    		
	    		int scaleX = 2;
	    		int offsetX = 2, offsetY = 2;
	        	ImageView i = new ImageView(mContext);
	        	ImageView j = new ImageView(mContext);
	
	        	i.setImageResource(mImageIds[position]);
	        	j.setImageResource(mImageIds[position]);
	        	i.setLayoutParams(new Gallery.LayoutParams(300, 200));
	        	j.setLayoutParams(new Gallery.LayoutParams(150, 100));
	        	
	        	Matrix matrix = new Matrix();
	        	matrix.postScale(scaleX, scaleX);
	        	matrix.postTranslate(offsetX, offsetY);
	        //	ImageView.setImageMatrix(matrix);

	        	i.setScaleType(ImageView.ScaleType.FIT_XY);
	        	j.setScaleType(ImageView.ScaleType.FIT_XY);
	        	i.setBackgroundResource(mGalleryItemBackground);
	        	j.setBackgroundResource(mGalleryItemBackground);
	        
	        	return i;
	    	}
		}
	}