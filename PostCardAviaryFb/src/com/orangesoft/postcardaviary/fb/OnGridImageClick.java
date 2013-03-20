package com.orangesoft.postcardaviary.fb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.orangesoft.postcardaviary.R;

public class OnGridImageClick extends Activity {

	ImageView iv;
	ProgressBar pb;
	String imageUrl;
	Bitmap b=null;
	Drawable d;
	getPhotoFromGrid gd;
	private File mGalleryFolder,imageFile;
	private static final String FOLDER_NAME = "aviaryFb";
	public static final String LOG_TAG = "onGridImageClick";
	Uri fbImageUri;
	Intent intentGrid;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview_image_selected);
		iv=(ImageView)findViewById(R.id.loadedimage);
		pb=(ProgressBar)findViewById(R.id.after_grid_progress);
		intentGrid=getIntent();
		imageUrl=intentGrid.getStringExtra("IMAGE_URL");
		String imageuri=intentGrid.getStringExtra("ImageUri");
		gd=new getPhotoFromGrid();
		gd.execute();
		  
	    mGalleryFolder = createFolders();
	}
	

	private class getPhotoFromGrid extends AsyncTask<Void, Void, Void>
	{
		
		@Override
		protected void onPreExecute()
		{
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL imageURL=new URL(imageUrl);
				HttpURLConnection conn=(HttpURLConnection)imageURL.openConnection();
				Log.i("After GridView", "Opening Connection...");
				InputStream is=conn.getInputStream();
				 d=Drawable.createFromStream(is, "src");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			// TODO Auto-generated method stub
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			pb.setVisibility(View.GONE);
			iv.setImageDrawable(d);
			Bitmap b=drawableToBitmap(d);
			try {
				imageFile=createFile(b);
			} catch (Exception e) {
				e.printStackTrace();
			}
			fbImageUri=Uri.fromFile(imageFile);
			intentGrid.putExtra("ReturnedImageUri", fbImageUri.toString());
			setResult(-1, intentGrid);
			Log.i("onGridImageClick", fbImageUri.toString());
			finish();
		}
	}
	
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	
	  private File createFolders() {

			File baseDir;

			if ( android.os.Build.VERSION.SDK_INT < 8 ) {
				baseDir = Environment.getExternalStorageDirectory();
			} else {
				baseDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
			}

			if ( baseDir == null ) return Environment.getExternalStorageDirectory();

			Log.d( LOG_TAG, "Pictures folder: " + baseDir.getAbsolutePath() );
			File aviaryFolder = new File( baseDir, FOLDER_NAME );

			if ( aviaryFolder.exists() ) return aviaryFolder;
			if ( aviaryFolder.mkdirs() ) return aviaryFolder;

			return Environment.getExternalStorageDirectory();
		}
	  
	  private boolean isExternalStorageAvilable() {
			String state = Environment.getExternalStorageState();
			if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
				return true;
			}
			return false;
		}
	  
	  public File createFile(Bitmap b) throws Exception
	  {
		  File file = new File(mGalleryFolder, System.currentTimeMillis()+".png");
		  file.createNewFile();
		  FileOutputStream fOut;
		  fOut = new FileOutputStream(file);
          b.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		  fOut.flush();
          fOut.close();
		return file;
	  }
}
