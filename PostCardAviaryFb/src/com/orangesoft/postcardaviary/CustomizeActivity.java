package com.orangesoft.postcardaviary;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CustomizeActivity extends Activity
{
  public static final int DRAG = 1;
  public static final int NONE = 0;
  private static final String TAG = "Touch";
  public static final int ZOOM = 2;
  public static PointF mid = new PointF();
  public static int mode = 0,ImageWidth,ImageHeight;
  float d = 0.0F;
  Uri f;
  FrameLayout fm;
  String imagePath;
  ImageView iv;
  float[] lastEvent = null;
  Matrix matrix = new Matrix();
  float newRot = 0.0F;
  float oldDist;
  LinearLayout ll;
  Bitmap bm;

  Matrix savedMatrix = new Matrix();
  PointF start = new PointF();
  
  @Override
public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.customizescreen);
    fm = ((FrameLayout)findViewById(R.id.editpiclayout));
    ll=new LinearLayout(this);
    ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    imagePath = getIntent().getStringExtra("IMAGE_PATH");
    iv = new ImageView(this);
    iv.setPadding(10, 10, 25, 25);
    iv.setDrawingCacheEnabled(true);
    tv = new EditText(this);
    tv.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
    tv.setBackgroundColor(Color.TRANSPARENT);
//    tv.setInputType(1);
//    tv.setImeOptions(6);
    ll.addView(tv);
    tv.setSingleLine();
    iv.setOnTouchListener(t);
    addImage(imagePath);
    //Log.d("SIZE", "FrameLayout::"+fm.getWidth()+", "+fm.getHeight()+"ImageView::"+iv.getDrawingCache().getWidth()+","+iv.getDrawingCache().getHeight());
  }

  
  View.OnTouchListener t = new View.OnTouchListener()
  {
	  public boolean onTouch(View paramView, MotionEvent event)
	    {
	      ImageView view = (ImageView)paramView;
	      switch (event.getAction() & MotionEvent.ACTION_MASK)
	      {
	      case MotionEvent.ACTION_DOWN:

	          savedMatrix.set(matrix);
	          start.set(event.getX(), event.getY());
	          Log.d(TAG, "mode=DRAG" );
	          Log.d("ACTION_DOWN", "FrameLayout::"+fm.getWidth()+" ,"+fm.getHeight()+" ImageView:: "+view.getWidth()+", "+view.getHeight());
	          mode = DRAG;
	          break;
	      case MotionEvent.ACTION_POINTER_DOWN:

	          oldDist = spacing(event);
	          Log.d(TAG, "oldDist=" + oldDist);
	          if (oldDist > 10f) {

	              savedMatrix.set(matrix);
	              midPoint(mid, event);
	              mode = ZOOM;
	              Log.d(TAG, "mode=ZOOM" );
	          }
	          break;

	      case MotionEvent.ACTION_MOVE:

	          if (mode == DRAG) {
/*
 * Restricting Image not to be draggable outside of FrameLayout
 */
	        	  matrix.set(savedMatrix); //Top
	        	  if(event.getY()==0)
	        	  {
	        		  matrix.postTranslate(event.getX()+15-start.x, 0+15);
	        		  Log.d("Top", "Top...");
	        	  }
	        	  else if(event.getX()==0) //Left
	        	  {
	        		  matrix.postTranslate(0+15, event.getY()-start.y+15);
	        		  Log.d("LEFT", "Left...");
	        	  }
	        	  else if((event.getY()-ImageHeight)>fm.getHeight()) //Bottom
	        	  {
	        		  matrix.postTranslate(event.getX()-start.x+15, fm.getHeight()-ImageHeight+15);
	        		  Log.d("BOTTOM", "Bottom...");
	        	  }
	        	  else if((event.getX()-ImageWidth)>fm.getWidth()) //Right
	        	  {
	        		  matrix.postTranslate(fm.getWidth()-ImageWidth+15, event.getY()-start.y+15);
	        		  Log.d("RIGHT", "Right....");
	        	  }
	        	  else if(event.getY()==0 && (event.getX()-ImageWidth)>fm.getWidth()) //Top -Right
	        	  {
	        		  matrix.postTranslate(fm.getWidth()-ImageWidth-15, 15);
	        	  }
	        	  else if(event.getY()==0 &&event.getX()==0) //Top-Left
	        	  {
	        		  matrix.postTranslate(15, 15);
	        	  }
	        	  else if((event.getY()-ImageHeight)>fm.getHeight() && event.getX()==0) //Bottom-Left
	        	  {
	        		  matrix.postTranslate(15, fm.getHeight()-ImageHeight-15);
	        	  }
	        	  else if((event.getX()-ImageWidth)>fm.getWidth() && (event.getY()-ImageHeight)>fm.getHeight())
	        	  {
	        		  matrix.postTranslate(fm.getWidth()-ImageWidth-15, fm.getHeight()-ImageHeight-15);
	        	  }
	        	  else
	        	  {
		              matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);  
		              Log.d("ACTION_MOVE", "Starting Point::"+start.x+","+start.y+" Ending Point::"+event.getX()+","+event.getY());
		              Log.d("ACTION_DOWN", "FrameLayout::"+fm.getWidth()+" ,"+fm.getHeight()+" ImageView:: "+view.getWidth()+", "+view.getHeight());

	        	  }
	          }
	          else if (mode == ZOOM) {

	              float newDist = spacing(event);
	              Log.d(TAG, "newDist=" + newDist);
	              if (newDist > 10f) {

	                  matrix.set(savedMatrix);
	                  float scale = newDist / oldDist;
	                  matrix.postScale(scale, scale, mid.x, mid.y);
	              }
	          }
	          break;

	      case MotionEvent.ACTION_UP:
	      case MotionEvent.ACTION_POINTER_UP:

	          mode = NONE;
	          Log.d(TAG, "mode=NONE" );
	          break;
	      }  
	      view.setImageMatrix(matrix);
	      return true;
	     
	    }
	  
	  private void midPoint(PointF point, MotionEvent event) {

		    float x = event.getX(0) + event.getX(1);
		    float y = event.getY(0) + event.getY(1);
		    point.set(x / 2, y / 2);
		}

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
  };
  EditText tv;

  public void addImage(String paramString)
  {
    f = Uri.parse(paramString);
    iv.setImageURI(f);
    iv.setScaleType(ImageView.ScaleType.MATRIX);
  //  iv.setAdjustViewBounds(true);
    fm.addView(iv);
    try
    {
    	bm=BitmapFactory.decodeFile(paramString);
    	ImageHeight=bm.getHeight();
    	ImageWidth=bm.getWidth();
    	
    	Log.d("Image_Size","Image Size:"+ImageHeight+", "+ImageWidth);
    }
    catch (Exception e) {
		// TODO: handle exception
	}
    
  }

  public void newText()
  {
    tv.setHint("Add Greeting Text");
    fm.addView(ll);
  }

  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    getMenuInflater().inflate(R.menu.cutomizemenu, paramMenu);
    return true;
  }

  @Override
public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    switch (paramMenuItem.getItemId())
    {
    case R.id.edit:
    	newText();
    	break;
    default:
      return super.onOptionsItemSelected(paramMenuItem);
    }
    return true;
  }
}