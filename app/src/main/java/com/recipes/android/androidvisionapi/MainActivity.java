package com.recipes.android.androidvisionapi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public VisionServiceClient visionServiceClient=new VisionServiceRestClient("d2bce5d3cfed4a35b20c669a18cc7da4","https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");
    TextToSpeech t1;
    private static final int CAMERA_REQUEST = 1888;
    ImageView imageView;
    Bitmap mBitmap;
    LinearLayout mylayout;
    AnimationDrawable animationDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mylayout = findViewById(R.id.mylayout);
        animationDrawable = (AnimationDrawable) mylayout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();

        //Text to speech
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                    t1.setSpeechRate(0.8f);
                }
                t1.speak("Welcome To Vision", TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });

        Button photoButton = this.findViewById(R.id.button1);

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    t1.speak("Opening Camera           Press Volume key to take picture", TextToSpeech.QUEUE_FLUSH, null, null);


                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }


            public void onClick1() {

                //Convert Image to stream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                t1.speak("Back To HomeScreen", TextToSpeech.QUEUE_FLUSH, null, null);

                @SuppressLint("StaticFieldLeak") final AsyncTask<InputStream, String, String> visionTest = new AsyncTask<InputStream, String, String>() {

                    ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
                     @Override
                    protected String doInBackground(InputStream... inputStreams) {
                        try {
                            publishProgress("Recognizing...");
                            String[] features = {"Description"};
                            String[] details = {};

                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);


                            return new Gson().toJson(result);
                        } catch (Exception e) {
                            return e.getMessage();
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mDialog.dismiss();
                        TextView textView = findViewById(R.id.txtDescription);
                        StringBuilder stringBuilder = new StringBuilder();
                        String c = "Unable To Fetch Description";
                        try {
                            AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);


                            for (Caption caption : result.description.captions) {
                                    stringBuilder.append(caption.text);
                            }


                        }
                        catch (Exception e)
                        {
                           //Log.e(String.valueOf(this),e.getMessage());
                            stringBuilder.append(c);
                        }

                        textView.setText(stringBuilder);

                            t1.speak("Object Is "+String.valueOf(stringBuilder),TextToSpeech.QUEUE_FLUSH,null,null);




                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        mDialog.setMessage(values[0]);
                    }
                };


                visionTest.execute(inputStream);
            }





    //Camera
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mBitmap = photo;
            imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(photo);
            onClick1();
        }
    }




}
