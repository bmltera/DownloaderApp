package edu.sjsu.android.homework5;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.URL;

public class ThreadedDownloadActivity extends AppCompatActivity {

    ImageView image;
    EditText textBox;
    ProgressDialog pd;
    Handler messageHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (bitmap == null){
                Toast.makeText(getApplicationContext(), "Message download failed", Toast.LENGTH_SHORT).show();
            }
            setImageFromBitmap(bitmap);
            pd.dismiss();
        }

    };
    Handler myHandler = new Handler(); // handler for runnable task
    String url = "";
    Bitmap bitmap = null;
    Runnable foregroundRunnable = new Runnable(){
        @Override
        public void run() {
            if (bitmap == null){
                Toast.makeText(getApplicationContext(), "Runnable download failed", Toast.LENGTH_SHORT).show();
            }
            setImageFromBitmap(bitmap);
            pd.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.imageView);
        textBox = (EditText) findViewById(R.id.editText);
        pd = new ProgressDialog(this);
        pd.setTitle("Download");
        pd.setMessage("test");
        pd.setCancelable(false);
    }

    // returns downloaded bitmap
    Bitmap downloadBitMap(String urlString) throws IOException {
        Bitmap myBitmap = null;
        bitmap = null;
        URL url = new URL(urlString);
        HttpURLConnection urlC = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = new BufferedInputStream(urlC.getInputStream());
            myBitmap = BitmapFactory.decodeStream(in);
            bitmap = myBitmap;
        } catch (Throwable e){
            e.printStackTrace();
        } finally{
            urlC.disconnect();
        }
        return myBitmap;
    }

    public void setImageFromBitmap(Bitmap b){
        image.setImageBitmap(b);
    }

    // onClick for run runnable
    public void runRunnable(View view){
        // get URL and setup progressDialog
        url = textBox.getText().toString();
        pd.setMessage("downloading via runnable");
        pd.show();

        Runnable runnable = new Runnable(){
            public void run(){
                try{
                    Thread background = new Thread(new Runnable(){
                        public void run(){
                            try{
                                downloadBitMap(url);
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                            myHandler.post(foregroundRunnable);
                        }
                    });
                    background.start();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    // onClick for run messages
    public void runMessages(View view){
        // get URL and setup progressDialog
        url = textBox.getText().toString();
        pd.setMessage("downloading via message");
        pd.show();

        // create a thread to run in the background
        Thread background = new Thread(new Runnable(){
            public void run(){
                try{
                    Bitmap bitmap = downloadBitMap(url);
                } catch(Throwable t){
                }
                Message msg = messageHandler.obtainMessage(0,bitmap);
                messageHandler.sendMessage(msg);
            }
        });
        background.start();

    }

    // onClick for run AsyncTask
    public void runAsyncTask(View view){
        new imageTask().execute();

    }

    // onClick for reset image
    public void resetImage(View view){
        image.setImageResource(R.drawable.apple);
    }

    public void setURL1(View view){
        textBox.setText("https://pocketnow.com/files/2010/08/android-hub1.jpg");
    }

    public void setURL2(View view){
        textBox.setText("http://pngimg.com/uploads/apple/apple_PNG12405.png");
    }

    // AsyncTask for AsyncButton
    private class imageTask extends AsyncTask<Void, Void, Bitmap> {

        protected void onPreExecute() {
            url = textBox.getText().toString();
            pd.setMessage("downloading via AsyncTask");
            //pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Bitmap doInBackground(Void... unused) {
            try {
                Bitmap bitmap = downloadBitMap(url);
                return bitmap;
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null){
                Toast.makeText(getApplicationContext(), "AsyncTask download failed", Toast.LENGTH_SHORT).show();
            }
            setImageFromBitmap(bitmap);
            pd.dismiss();
        }
    }
}