package com.example.photogalleryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    ImageButton ib_back, ib_next;
    ImageView imageView;
    TextView textView;
    Button buttonGo;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonGo = (Button) findViewById(R.id.buttonGo);
        imageView = (ImageView) findViewById(R.id.imageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        textView = (TextView) findViewById(R.id.textView);
        ib_next = (ImageButton) findViewById(R.id.ib_next);
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        ib_next.setEnabled(false);
        ib_next.setAlpha(127);
        ib_back.setEnabled(false);
        ib_back.setAlpha(127);


        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    new GetDataFromAsync().execute("https://dev.theappsdr.com/apis/photos/keywords.php");
                } else {
                    Toast.makeText(MainActivity.this, "NOT connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() || (networkInfo.getType() != connectivityManager.TYPE_WIFI &&
                networkInfo.getType() != connectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }


    public class GetDataFromAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }

        String result = null;
        String[] listOfItems = null;

        @Override
        protected String[] doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {

                URL urls = new URL(strings[0]);
                connection = (HttpURLConnection) urls.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString(connection.getInputStream(), "UTF8");
                }

                listOfItems = result.split(";");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return listOfItems;
        }

        @Override
        protected void onPostExecute(final String[] s) {
            progressBar.setVisibility(View.INVISIBLE);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select search Item");
            builder.setItems(s, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textView.setText(s[which]);
                    progressBar.setVisibility(View.VISIBLE);
                    new GetImageUrlsAsync().execute("https://dev.theappsdr.com/apis/photos/index.php?keyword=" + s[which]);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    public class GetImageUrlsAsync extends AsyncTask<String, Void, LinkedList<String>> {
        String result = null;
        String[] imageArray = null;
        LinkedList<String> listOfItems = new LinkedList<>();
        int index = 0;

        @Override
        protected LinkedList<String> doInBackground(String... strings) {
            HttpURLConnection connections = null;
            try {
                URL urls = new URL(strings[0]);
                connections = (HttpURLConnection) urls.openConnection();
                connections.connect();
                if (connections.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = IOUtils.toString(connections.getInputStream(), "UTF8");
                }

                imageArray = result.split("\n");
                for (String a : imageArray) {
                    listOfItems.add(a);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connections != null) {
                    connections.disconnect();
                }
            }
            return listOfItems;

        }


        @Override
        protected void onPostExecute(final LinkedList<String> links) {
            progressBar.setVisibility(View.INVISIBLE);
            final int size = links.size();
            new setImageFromAsync().execute(links.get(index));

            ib_next.setEnabled(true);
            ib_next.setAlpha(255);
            ib_back.setEnabled(true);
            ib_back.setAlpha(255);

            ib_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    index++;
                    if (index <= size) {
                        if (index == size) {
                            index = 0;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        new setImageFromAsync().execute(links.get(index));
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });

            ib_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    index--;
                    if (index == -1) {
                        index = size - 1;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    new setImageFromAsync().execute(links.get(index));
                    progressBar.setVisibility(View.INVISIBLE);


                }

            });


        }
    }

    class setImageFromAsync extends AsyncTask<String, Void, Bitmap> {
        Bitmap image = null;


        @Override
        protected Bitmap doInBackground(String... strings) {
            HttpURLConnection connections = null;
            try {
                URL urls = new URL(strings[0]);
                connections = (HttpURLConnection) urls.openConnection();
                connections.connect();
                if (connections.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    image = BitmapFactory.decodeStream(connections.getInputStream());

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connections != null) {
                    connections.disconnect();
                }
            }
            return image;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(image);

        }
    }


}
