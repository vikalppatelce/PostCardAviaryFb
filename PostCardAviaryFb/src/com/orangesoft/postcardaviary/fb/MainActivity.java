package com.orangesoft.postcardaviary.fb;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.orangesoft.postcardaviary.R;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnScrollListener{

	private static String APP_ID = "421610114580722"; // Replace your App ID here
	 
	Button loadMore;
	URL fb;
	GridView gridOfPhotos;
	ProgressBar spin,spin2;
	Handler handler;
    MemoryCache memoryCache=new MemoryCache();
	
	// HOLD THE URL TO MAKE THE API CALL TO
	private String URL,at;
	// FLAG FOR CURRENT PAGE
	int c=30,increasingItem=30,i=0,j=0,k=0;

	// Instance of Facebook Class
    private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    String FILENAME = "AndroidSSO_data";
    SharedPreferences mPrefs;
    SharedPreferences.Editor editor;
    PhotosAdapter adapter;
    ArrayList<getPhotos> arrPhotos;
    getPhotosData gd;
    loadMorePhotos ld;
    loadMorePhotos[] lmd=new loadMorePhotos[1000];
    LinearLayout ll;
    Intent intentMainActivity,intent;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main1);

	    intent=getIntent();
	    String imageuri=intent.getStringExtra("ImageUri");
		
		gridOfPhotos=(GridView)findViewById(R.id.gridphoto);
	    spin=(ProgressBar)findViewById(R.id.load);
		spin2=(ProgressBar)findViewById(R.id.load2);
		loadMore=(Button)findViewById(R.id.loadmore);
		ll=(LinearLayout)findViewById(R.id.ll);
		
		Log.i("Create", "XML inflated...");
		gd=new getPhotosData();
		ld=new loadMorePhotos();
		facebook = new Facebook(APP_ID);
	    mAsyncRunner = new AsyncFacebookRunner(facebook);
	     
	    arrPhotos=new ArrayList<getPhotos>();

	    

	    loginToFacebook();
	    
	    gridOfPhotos.setOnScrollListener(this);
	    loadMore.setOnClickListener(n);
	    
	    gridOfPhotos.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
			
				PhotosAdapter pa=(PhotosAdapter)parent.getAdapter();
				getPhotos gp=(getPhotos)pa.getItem(position);
				
				String name=gp.getPhotoSource();
				try
				{
				if(!name.isEmpty())
				{
				intentMainActivity=new Intent(getApplicationContext(),OnGridImageClick.class);
				intentMainActivity.putExtra("IMAGE_URL", name);
				intentMainActivity.putExtra("ImageUri", "");
				startActivityForResult(intentMainActivity, 2);
				}
				
				}
				catch (Exception e) {
					// TODO: handle exception
					Log.e("GridView", e.getMessage());
				}
			}
		});
	    
	}
	
	View.OnClickListener n=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ll.removeView(loadMore);
			//loadMore.setVisibility(View.INVISIBLE);
			spin2.setVisibility(View.VISIBLE);
			if(ld.getStatus()==Status.PENDING)
			{
				ld.execute();
				Log.i("loadMoreAsync", "new AsyncTask : 0");
				Log.i("PENDING", "Not yet RUNNING");
			}
			else if(true)
			{
				 Log.i("loadMoreAsync", "Creating new AsyncTask");
				 lmd[j]=new loadMorePhotos();
				 lmd[j].execute();
				 Log.i("loadMoreAsync", "Creating LoadMore AsyncTask :"+j);
				 j++;
			/*	while(ld.getStatus()==Status.RUNNING)
				{
					Log.i("loadMore Async", "LoadMore RUNNING");
					if(ld.getStatus()==Status.FINISHED)
						return;
				}*/
				
			}
				
		}
	};

	@SuppressWarnings("deprecation")
	public void loginToFacebook()
	{
		mPrefs = getPreferences(MODE_PRIVATE);
	    String access_token = mPrefs.getString("access_token", null);
	    long expires = mPrefs.getLong("access_expires", 0);
	 
	    if (access_token != null) {
	        facebook.setAccessToken(access_token);
	    }
	 
	    if (expires != 0) {
	        facebook.setAccessExpires(expires);
	    }
	    
	    if(facebook.isSessionValid())
	    {
	    	Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
	    	gd.execute();
	    }
	 
	    if (!facebook.isSessionValid()) {
	        facebook.authorize(this,
	                new String[] { "email", "publish_actions","user_photos"},
	                new DialogListener() {
	                    @Override
	                    public void onCancel() {
	                        // Function to handle cancel event
	                    }
	                    @Override
	                    public void onComplete(Bundle values) {
	                        // Function to handle complete event
	                        // Edit Preferences and update facebook acess_token
	                    	editor = mPrefs.edit();
	                        editor.putString("access_token",
	                                facebook.getAccessToken());
	                        editor.putLong("access_expires",
	                                facebook.getAccessExpires());
	                        editor.commit();
	                        gd.execute();
	                    }
	                    @Override
	                    public void onError(DialogError error) {
	                        // Function to handle error
	                    }
	                    @Override
	                    public void onFacebookError(FacebookError fberror) {
	                        // Function to handle Facebook errors
	                    }
	                });
	    }	
	}
	

	private class getPhotosData extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected Void doInBackground(Void... arg0) {


	        // SET THE INITIAL URL TO GET THE FIRST LOT OF ALBUMS
     	   URL = "https://graph.facebook.com/me/photos/?access_token="
           + mPrefs.getString("access_token", null) + "&limit=30";
	       at=mPrefs.getString("access_token", null);
	        
	        /*
	         * Fetching UserName :Vikalp Patel Setting it to title of the Screen
	         */
	        String nameURL="https://graph.facebook.com/me/?access_token="+at+"&fields=name";
	        
	        /*
	         * 
	         */

	        try {
 
	            HttpClient hc = new DefaultHttpClient();
	            HttpGet get = new HttpGet(URL);
	            HttpResponse rp = hc.execute(get);
	            
	            String st=rp.toString();
	            
	            Log.e("RESPONSE", st);
	            Log.e("ACCESS_TOKEN",at);

	            if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	                String queryAlbums = EntityUtils.toString(rp.getEntity());
	                
	                Log.d("GRAPH-RESPONSE", queryAlbums);
	                
	                int s=rp.getStatusLine().getStatusCode();
	                String a=String.valueOf(s);
	                Log.e("RESPONSE-IF", a);

	                JSONObject JOTemp = new JSONObject(queryAlbums);

	                JSONArray JAPhotos = JOTemp.getJSONArray("data");

	                Log.e("JSONArray", String.valueOf(JAPhotos));
	                Log.e("JSONArray-Length",String.valueOf(JAPhotos.length()));
	                // IN MY CODE, I GET THE NEXT PAGE LINK HERE

	                getPhotos photos;

	                for (int i = 0; i < JAPhotos.length(); i++) {
	                    JSONObject JOPhotos = JAPhotos.getJSONObject(i);
	                    // Log.e("INDIVIDUAL ALBUMS", JOPhotos.toString());
	                    
	                    Log.e("JSON", String.valueOf(i));

	                    if (JOPhotos.has("link")) {

	                        photos = new getPhotos();

	                        // GET THE ALBUM ID
	                        if (JOPhotos.has("id")) {
	                            photos.setPhotoID(JOPhotos.getString("id"));
	                        } else {
	                            photos.setPhotoID(null);
	                        }

	                        // GET THE ALBUM NAME
	                        if (JOPhotos.has("name")) {
	                            photos.setPhotoName(JOPhotos.getString("name"));
	                        } else {
	                            photos.setPhotoName(null);
	                        }

	                        // GET THE ALBUM COVER PHOTO
	                        if (JOPhotos.has("picture")) {
	                            photos.setPhotoPicture(JOPhotos
	                                    .getString("picture"));
	                        } else {
	                            photos.setPhotoPicture(null);
	                        }

	                        // GET THE PHOTO'S SOURCE
	                        if (JOPhotos.has("source")) {
	                            photos.setPhotoSource(JOPhotos
	                                    .getString("source"));
	                        } else {
	                            photos.setPhotoSource(null);
	                        }

	                        arrPhotos.add(photos);
	                    }
	                }
	            }
	            else
	            {
	            	int s=rp.getStatusLine().getStatusCode();
	                String a=String.valueOf(s);
	                Log.e("RESPONSE-Else", a);
	            }
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        Log.e("doInBackground Finished", "after doInBackground");
	        return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {

	    	adapter=new PhotosAdapter(MainActivity.this, arrPhotos);
	    	Log.e("onPostExceute", "inside PostExecute");
	        // SET THE ADAPTER TO THE GRIDVIEW
	        gridOfPhotos.setAdapter(adapter);
             
	      //  Toast.makeText(getApplicationContext(), "on Post Excecute", Toast.LENGTH_SHORT).show();
	        Log.e("onPostExceute", "");
	        spin.setVisibility(View.GONE);
	        
	        Log.e("onPostExceute", "end of PostExecute");
	        //loadMore.setVisibility(View.VISIBLE);
	    }
	    
	    @Override
		protected void onPreExecute()
	    {
	    	spin.setVisibility(View.VISIBLE);
	    }

	}

	private class loadMorePhotos extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected Void doInBackground(Void... arg0) {


	        // Next page request
	        URL ="https://graph.facebook.com/me/photos/?access_token="
	                + mPrefs.getString("access_token", null) + "&limit=30&offset="+c ;
	        Log.i("loadMore", "Url inside loadMore AsyncTask "+URL+"offset:"+c);
            c+=30;
	        try {

	            HttpClient hc = new DefaultHttpClient();
	            HttpGet get = new HttpGet(URL);
	            HttpResponse rp = hc.execute(get);

	            if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	                String queryAlbums = EntityUtils.toString(rp.getEntity());
	                // Log.e("PAGED RESULT", queryAlbums);

	                int s=rp.getStatusLine().getStatusCode();
	                String a=String.valueOf(s);
	                Log.e("loadMore-RESPONSE-IF", a);

	                JSONObject JOTemp = new JSONObject(queryAlbums);

	                JSONArray JAPhotos = JOTemp.getJSONArray("data");

	                // IN MY CODE, I GET THE NEXT PAGE LINK HERE

	                getPhotos photos;

	                for (int i = 0; i < JAPhotos.length(); i++) {
	                    JSONObject JOPhotos = JAPhotos.getJSONObject(i);
	                    // Log.e("INDIVIDUAL ALBUMS", JOPhotos.toString());

	                    if (JOPhotos.has("link")) {

	                        photos = new getPhotos();

	                        // GET THE ALBUM ID
	                        if (JOPhotos.has("id")) {
	                            photos.setPhotoID(JOPhotos.getString("id"));
	                        } else {
	                            photos.setPhotoID(null);
	                        }

	                        // GET THE ALBUM NAME
	                        if (JOPhotos.has("name")) {
	                            photos.setPhotoName(JOPhotos.getString("name"));
	                        } else {
	                            photos.setPhotoName(null);
	                        }

	                        // GET THE ALBUM COVER PHOTO
	                        if (JOPhotos.has("picture")) {
	                            photos.setPhotoPicture(JOPhotos
	                                    .getString("picture"));
	                        } else {
	                            photos.setPhotoPicture(null);
	                        }

	                        // GET THE ALBUM'S PHOTO COUNT
	                        if (JOPhotos.has("source")) {
	                            photos.setPhotoSource(JOPhotos
	                                    .getString("source"));
	                        } else {
	                            photos.setPhotoSource(null);
	                        }

	                        arrPhotos.add(photos);
	                    }
	                }
	            }
	            else{
	            	int s=rp.getStatusLine().getStatusCode();
	                String a=String.valueOf(s);
	                Log.e("RESPONSE-Else", a);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {


	        // APPEND NEW DATA TO THE ARRAYLIST AND SET THE ADAPTER TO THE
	        // LISTVIEW
	        adapter = new PhotosAdapter(MainActivity.this, arrPhotos);
	        gridOfPhotos.setAdapter(adapter);

	        spin2.setVisibility(View.GONE);
	        // Setting new scroll position
	        Log.i("currentPosition", Integer.toString(c));
	        gridOfPhotos.setSelection(c-45);
	        increasingItem+=30;
	        ll.addView(loadMore);
	        Log.i("loadMore-Async", "Adding LoadMore button again");
	        Log.i("loadMore-Async", "LoadMore AsyncTask onPost() finished");
	        //loadMore.setOnClickListener(n);
	        Log.i("loadMore-Async", "Adding Listener to LoadMore");
	    }

		protected void onPreExecute() {
		}
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    System.gc();
	    memoryCache.clear();
	    Log.i("Destroy", "Memory Cache get cleared");
	    }


	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if(gridOfPhotos.getLastVisiblePosition()+1==increasingItem)
		{
			loadMore.setVisibility(View.VISIBLE);
			Log.i("GridView-ScrollListener",Integer.toString(increasingItem) );
		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
	}
	
	public void onActivityResult(int requestCode,int resultCode, Intent data)
	{
		if(requestCode==2 && resultCode==-1)
		{
			String returnedImageUri=data.getStringExtra("ReturnedImageUri");
		    Log.i("MainActivity", returnedImageUri!=null? "returnedUri is Not Null":"returnedUri is Null");
			intent.putExtra("ReturnedImageUri", returnedImageUri);
			setResult(-1, intent);
			finish();
		}
	}
}
