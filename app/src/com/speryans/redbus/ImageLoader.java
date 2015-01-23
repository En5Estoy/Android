package com.speryans.redbus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import com.speryans.redbus.classes.Commons;
import com.speryans.redbus.classes.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.widget.ImageView;

public class ImageLoader {
    
    private HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
    
    private File cacheDir;
    private int stub_id = 0;
    
    private boolean isCacheEnabled;
    
    public ImageLoader(Context context, boolean isCacheEnabled, int placeholder_image ) {
    	this.stub_id = placeholder_image;
    	this.isCacheEnabled = isCacheEnabled;
    	
        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
        
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), Commons.cacheDir);
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
    	  int targetWidth = 50;
    	  int targetHeight = 50;
    	  Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);
    	  
    	  Canvas canvas = new Canvas(targetBitmap);
    	  Path path = new Path();
    	  path.addCircle(((float) targetWidth - 1) / 2, ((float) targetHeight - 1) / 2, (Math.min(((float) targetWidth), ((float) targetHeight)) / 2), Path.Direction.CCW);
    	  
    	  canvas.clipPath(path);
    	  Bitmap sourceBitmap = scaleBitmapImage;
    	  canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
    	  
    	  return targetBitmap;
    }
    
    public void displayImage(String url, Activity activity, ImageView imageView, boolean rounded, int quality) {
    	displayImage(url, activity, imageView, rounded, quality, null);
    }
    
    public void displayImage(String url, Activity activity, ImageView imageView, boolean rounded, int quality, ImageListener il) {
    	try {
    		if( isCacheEnabled ) {
    			if(cache.containsKey(url)) {
    				if( rounded ) {
    					imageView.setImageBitmap( this.getRoundedShape( cache.get(url) ));
    				} else {
    					imageView.setImageBitmap( cache.get(url) );
    				}
    			} else {
    				queuePhoto(url, activity, imageView, rounded, quality, il);
    				imageView.setImageResource(stub_id);
    			}    
    		} else {
    			queuePhoto(url, activity, imageView, rounded, quality, il);
    			imageView.setImageResource(stub_id);
    		}
    	} catch( Exception e ) {
    		Commons.error( e.toString() , e);
    	}
    }
        
    private void queuePhoto(String url, Activity activity, ImageView imageView, boolean rounded, int quality, ImageListener il)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p=new PhotoToLoad(url, imageView, rounded, quality, il);
        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState() == Thread.State.NEW)
            photoLoaderThread.start();
    }
    
    private Bitmap getBitmap(String url, int quality) 
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        File f=new File(cacheDir, filename);
        
        //from SD cache
        Bitmap b = decodeFile(f, quality);
        if(b!=null)
            return b;
        
        //from web
        try {
        	Bitmap bitmap=null;
            URL curl = new URL(url);
            InputStream is= null; //.openStream();
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection urlConnection = (HttpURLConnection) curl.openConnection();
            urlConnection.setInstanceFollowRedirects(true);
            String locHeader = urlConnection.getHeaderField("Location");
            
            if (locHeader != null) {
            	Commons.info(locHeader);
            	is = new URL(locHeader).openStream();
            	try {
            		OutputStream os = new FileOutputStream(f);
            		Utils.CopyStream(is, os);
            		os.close();
            		bitmap = decodeFile(f, quality);
            	} catch( Exception ex ) {
            		bitmap = BitmapFactory.decodeStream(is);
            	}
            } else {
            	is = urlConnection.getInputStream();
            	try {
            		OutputStream os = new FileOutputStream(f);
            		Utils.CopyStream(is, os);
            		os.close();
            		bitmap = decodeFile(f, quality);
            	} catch( Exception ex ) {
            		bitmap = BitmapFactory.decodeStream(is);
            	}
            }
            
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f, int quality){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=quality;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale++;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public boolean rounded;
        public int quality;

        public ImageListener listener = null;
        
        public PhotoToLoad(String u, ImageView i, boolean r, int q, ImageListener il){
            url=u; 
            imageView=i;
            rounded = r;
            quality = q;
            listener = il;
        }
    }
    
    PhotosQueue photosQueue=new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
            for(int j=0 ;j<photosToLoad.size();){
                if(photosToLoad.get(j).imageView==image)
                    photosToLoad.remove(j);
                else
                    ++j;
            }
        }
    }
    
    class PhotosLoader extends Thread {
        public void run() {
            try {
                while(true)
                {
                    //thread waits until there are any images to load in the queue
                    if(photosQueue.photosToLoad.size()==0)
                        synchronized(photosQueue.photosToLoad){
                            photosQueue.photosToLoad.wait();
                        }
                    if(photosQueue.photosToLoad.size()!=0)
                    {
                        PhotoToLoad photoToLoad;
                        synchronized(photosQueue.photosToLoad){
                            photoToLoad=photosQueue.photosToLoad.pop();
                        }
                        Bitmap bmp=getBitmap(photoToLoad.url, photoToLoad.quality);
                        cache.put(photoToLoad.url, bmp);
                        //if(((String)photoToLoad.imageView.getTag()).equals(photoToLoad.url)){
                            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad.imageView, photoToLoad.rounded, photoToLoad.url, photoToLoad.listener);
                            Activity a=(Activity)photoToLoad.imageView.getContext();
                            a.runOnUiThread(bd);
                        //}
                    }
                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }
    
    PhotosLoader photoLoaderThread = new PhotosLoader();
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        ImageView imageView;
        boolean rounded;
        String url;
        ImageListener listener;
        public BitmapDisplayer(Bitmap b, ImageView i, boolean r, String u, ImageListener il){bitmap=b;imageView=i;rounded=r;url=u;listener=il;}
        public void run()
        {
            if(bitmap!=null) {
            	if( rounded ) {
            		imageView.setImageBitmap( ImageLoader.this.getRoundedShape( bitmap ) );
            	} else {
            		imageView.setImageBitmap( bitmap );
            	}
            	
            	if( listener != null ) {
            		listener.imageLoaded(url);
            	}
            } else
                imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
    	try {
    		//clear memory cache
    		cache.clear();
        
    		//clear SD cache
    		File[] files=cacheDir.listFiles();
    		for(File f:files)
    			f.delete();
    	} catch( Exception ex ) {}
    }

    public interface ImageListener {
    	public void imageLoaded( String url );
    }
}
