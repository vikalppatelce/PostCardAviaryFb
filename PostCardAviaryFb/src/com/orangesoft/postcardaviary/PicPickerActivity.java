package com.orangesoft.postcardaviary;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.aviary.android.feather.Constants;
import com.aviary.android.feather.FeatherActivity;
import com.facebook.PickerFragment;

public class PicPickerActivity extends Activity
{
  int column_index;
  Button f;
  Button g;
  Uri mImageUri;
  public static final String LOG_TAG = "feather-launcher";
  private static final String FOLDER_NAME = "aviary";
  private static final int ACTION_REQUEST_FEATHER = 100;
  String mOutputFilePath;
  private File mGalleryFolder;
  
  @Override
public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.picpickerscreen);
    g = ((Button)findViewById(R.id.gallerybutton));
    f = ((Button)findViewById(R.id.facebookbutton));
    
    mGalleryFolder = createFolders();
    
    g.setOnClickListener(l);
    f.setOnClickListener(m);
  }
  
  View.OnClickListener l = new View.OnClickListener()
  {
    @Override
	public void onClick(View paramView)
    {
      Intent localIntent = new Intent();
      localIntent.setType("image/*");
      localIntent.setAction("android.intent.action.GET_CONTENT");
      startActivityForResult(Intent.createChooser(localIntent, "Select Picture"), 1);
    }
  };
  View.OnClickListener m = new View.OnClickListener()
  {
    @Override
	public void onClick(View paramView)
    {
    	Intent fbIntent=new Intent(PicPickerActivity.this,com.orangesoft.postcardaviary.fb.MainActivity.class);
    	fbIntent.putExtra("ImageUri", "");
    	startActivityForResult(fbIntent, 2);
    }
  };
  String selectedImagePath;
  Uri selectedImageUri;

  public String getPath(Uri paramUri)
  {
    Cursor localCursor = managedQuery(paramUri, new String[] { "_data" }, null, null, null);
    this.column_index = localCursor.getColumnIndexOrThrow("_data");
    localCursor.moveToNext();
    return localCursor.getString(this.column_index);
  }

  @Override
public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
	  Log.i("PicPickerActivity", String.valueOf(requestCode));
    if ((requestCode == 1) && (resultCode == -1))
    {
    	mImageUri=data.getData();
    	if ( mImageUri != null ) {
    		Log.i("Existing Photo", mImageUri.toString());
			startFeather( mImageUri );
		}
     /* this.selectedImageUri = paramIntent.getData();
      Intent localIntent = new Intent(getApplicationContext(), CustomizeActivity.class);
      this.selectedImagePath = getPath(this.selectedImageUri);
      localIntent.putExtra("IMAGE_PATH", this.selectedImagePath);
      startActivity(localIntent);*/
    }
    else if ((requestCode == 2) && (resultCode == -1))
    {
    	String returnedImageUri=data.getStringExtra("ReturnedImageUri");
    	mImageUri=Uri.parse(returnedImageUri);
    	if ( mImageUri != null ) {
    		Log.i("Facebook Photo", mImageUri.toString());
    		startFeather( mImageUri );
		}
    	Log.i("PicPickerActivity", "mImageUri is Null");
    }
    else if ((requestCode== 100) && (resultCode== -1))
    {
    	updateMedia( mOutputFilePath );
    	selectedImageUri = data.getData();
        Intent localIntent = new Intent(getApplicationContext(), CustomizeActivity.class);
        localIntent.putExtra("IMAGE_PATH",selectedImageUri.toString());
        startActivity(localIntent);
        Log.i("After Android-Feather", mImageUri.toString());
    }
  }
  
  private void updateMedia( String filepath ) {
		Log.i( LOG_TAG, "updateMedia: " + filepath );
		MediaScannerConnection.scanFile( getApplicationContext(), new String[] { filepath }, null, null );
	}
  /*
   * 
   */
  
  private void startFeather( Uri uri ) {

		Log.d( LOG_TAG, "uri: " + uri );

		// first check the external storage availability
		if ( !isExternalStorageAvilable() ) {
			return;
		}

		// create a temporary file where to store the resulting image
		File file = getNextFileName();
		if ( null != file ) {
			mOutputFilePath = file.getAbsolutePath();
		} else {
			new AlertDialog.Builder( this ).setTitle( "Failed" ).setMessage( "Failed to create a new File" )
					.show();
			return;
		}

		// Create the intent needed to start feather
		Intent newIntent = new Intent( this, FeatherActivity.class );

		// === INPUT IMAGE URI ===
		// Mandatory
		// Set the source image uri
		newIntent.setData( uri );

		// === OUTPUT ====
		// Optional 
		// Pass the uri of the destination image file.
		// This will be the same uri you will receive in the onActivityResult
		newIntent.putExtra( Constants.EXTRA_OUTPUT, Uri.parse( "file://" + mOutputFilePath ) );

		// === OUTPUT FORMAT ===
		// Optional
		// Format of the destination image
		newIntent.putExtra( Constants.EXTRA_OUTPUT_FORMAT, Bitmap.CompressFormat.JPEG.name() );

		// === OUTPUT QUALITY ===
		// Optional
		// Output format quality (jpeg only)
		newIntent.putExtra( Constants.EXTRA_OUTPUT_QUALITY, 90 );

		// === ENABLE/DISABLE IAP FOR EFFECTS ===
		// Optional
		// If you want to disable the external effects
		// newIntent.putExtra( Constants.EXTRA_EFFECTS_ENABLE_EXTERNAL_PACKS, false );
		
		// === ENABLE/DISABLE IAP FOR FRAMES===
		// Optional
		// If you want to disable the external borders.
		// Note that this will remove the frames tool.
		// newIntent.putExtra( Constants.EXTRA_FRAMES_ENABLE_EXTERNAL_PACKS, false );		

		// == ENABLE/DISABLE IAP FOR STICKERS ===
		// Optional
		// If you want to disable the external stickers. In this case you must have a folder called "stickers" in your assets folder
		// containing a list of .png files, which will be your default stickers
		// newIntent.putExtra( Constants.EXTRA_STICKERS_ENABLE_EXTERNAL_PACKS, false );
		
		// enable fast rendering preview
		// newIntent.putExtra( Constants.EXTRA_EFFECTS_ENABLE_FAST_PREVIEW, true );

		// == TOOLS LIST ===
		// Optional
		// You can force feather to display only some tools ( see FilterLoaderFactory#Filters )
		// you can omit this if you just want to display the default tools

		/*
		 * newIntent.putExtra( "tools-list", new String[] { FilterLoaderFactory.Filters.ENHANCE.name(),
		 * FilterLoaderFactory.Filters.EFFECTS.name(), FilterLoaderFactory.Filters.STICKERS.name(),
		 * FilterLoaderFactory.Filters.ADJUST.name(), FilterLoaderFactory.Filters.CROP.name(),
		 * FilterLoaderFactory.Filters.BRIGHTNESS.name(), FilterLoaderFactory.Filters.CONTRAST.name(),
		 * FilterLoaderFactory.Filters.SATURATION.name(), FilterLoaderFactory.Filters.SHARPNESS.name(),
		 * FilterLoaderFactory.Filters.DRAWING.name(), FilterLoaderFactory.Filters.TEXT.name(),
		 * FilterLoaderFactory.Filters.MEME.name(), FilterLoaderFactory.Filters.RED_EYE.name(),
		 * FilterLoaderFactory.Filters.WHITEN.name(), FilterLoaderFactory.Filters.BLEMISH.name(),
		 * FilterLoaderFactory.Filters.COLORTEMP.name(), } );
		 */

		// === INLINE BITMAP RESULT ===
		// Optional.
		// You want the result bitmap inline. 
		// This will work only with small bitmaps
		// newIntent.putExtra( Constants.EXTRA_RETURN_DATA, true );

		// === EXIT ALERT ===
		// Optional
		// Uou want to hide the exit alert dialog shown when back is pressed
		// without saving image first
		// newIntent.putExtra( Constants.EXTRA_HIDE_EXIT_UNSAVE_CONFIRMATION, true );

		// === VIBRATION ===
		// Optional
		// Some aviary tools use the device vibration in order to give a better experience
		// to the final user. But if you want to disable this feature, just pass
		// any value with the key "tools-vibration-disabled" in the calling intent.
		// This option has been added to version 2.1.5 of the Aviary SDK
		newIntent.putExtra( Constants.EXTRA_TOOLS_DISABLE_VIBRATION, true );

		// === MAX SIZE ===
		// Optional
		// you can pass the maximum allowed image size, otherwise feather will determine
		// the max size based on the device memory.
		// This will not affect the hi-res image size.
		// Here we're passing the current display size as max image size because after
		// the execution of Aviary we're saving the HI-RES image so we don't need a big
		// image for the preview
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics( metrics );
		int max_size = Math.min( metrics.widthPixels, metrics.heightPixels );		
		max_size = (int) ( max_size / 0.8 );
		newIntent.putExtra( Constants.EXTRA_MAX_IMAGE_SIZE, max_size );

		// === HI-RES ===
		// You need to generate a new session id key to pass to Aviary feather
		// this is the key used to operate with the hi-res image ( and must be unique for every new instance of Feather )
		// The session-id key must be 64 char length

		// ..and start feather
		startActivityForResult( newIntent, ACTION_REQUEST_FEATHER );
	}

  private boolean isExternalStorageAvilable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
			return true;
		}
		return false;
	}
  
  private File getNextFileName() {
		if ( mGalleryFolder != null ) {
			if ( mGalleryFolder.exists() ) {
				File file = new File( mGalleryFolder, "aviary_" + System.currentTimeMillis() + ".jpg" );
				return file;
			}
		}
		return null;
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
}
