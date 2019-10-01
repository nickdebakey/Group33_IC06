package com.example.group33_ic06;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/*
In Class 06
Group33_InClass06.zip
Nick DeBakey & Lis Rizvanolli
 */

public class MainActivity extends AppCompatActivity {

    TextView tv_category;
    TextView tv_date;
    TextView tv_title;
    TextView tv_description;
    TextView tv_articleNum;
    Button btn_select;
    ImageView iv_main;
    ImageView iv_prev;
    ImageView iv_next;
    ProgressBar pb_loading;

    String[] categories;

    static int currentArticle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

        tv_category = findViewById(R.id.tv_category);
        tv_date = findViewById(R.id.tv_date);
        tv_title = findViewById(R.id.tv_title);
        tv_description = findViewById(R.id.tv_description);
        tv_articleNum = findViewById(R.id.tv_articleNum);
        btn_select = findViewById(R.id.btn_select);
        iv_main = findViewById(R.id.iv_main);
        iv_next = findViewById(R.id.iv_next);
        iv_prev = findViewById(R.id.iv_prev);
        pb_loading = findViewById(R.id.pb_loading);

        categories = new String[] {"business", "entertainment", "general", "health", "science", "sports", "technology"};

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose Category").setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tv_category.setText(categories[i]);
                            new GetDataAsync().execute("https://newsapi.org/v2/top-headlines?category=" + categories[i] + "&apiKey=5c63098c345e438c99411cf1b5a6bec9");
                        }
                    });
                    AlertDialog simpleAlert = builder.create();
                    simpleAlert.show();
                }
                else {
                    Toast.makeText(MainActivity.this, "No Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private class GetDataAsync extends AsyncTask<String, Void, ArrayList<Article>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb_loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Article> doInBackground(String... params) {
            HttpURLConnection connection = null;
            ArrayList<Article> result = new ArrayList<>();
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF-8");
                    JSONObject root = new JSONObject(json);
                    JSONArray articles = root.getJSONArray("articles");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject articlesJson = articles.getJSONObject(i);
                        Article article = new Article();
                        article.title = articlesJson.getString("title");
                        article.date = articlesJson.getString("publishedAt");
                        article.iv_url = articlesJson.getString("urlToImage").trim();
                        article.description = articlesJson.getString("description");
                        result.add(article);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(final ArrayList<Article> result) {
            if (result.size() > 0) {
                pb_loading.setVisibility(View.INVISIBLE);
                updateViews(result);

                makeVisible();

                if (result.size() > 1) {
                    iv_prev.setVisibility(View.VISIBLE);
                    iv_next.setVisibility(View.VISIBLE);

                    iv_prev.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pb_loading.setVisibility(View.VISIBLE);
                            makeInvisible();

                            if (currentArticle == 0) {
                                currentArticle = 19;
                                updateViews(result);
                            }
                            else {
                                currentArticle--;
                                updateViews(result);
                            }
                        }
                    });

                    iv_next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pb_loading.setVisibility(View.VISIBLE);
                            makeInvisible();

                            if (currentArticle == result.size()-1) {
                                currentArticle = 0;
                                updateViews(result);
                            }
                            else {
                                currentArticle++;
                                updateViews(result);
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(MainActivity.this, "No articles", Toast.LENGTH_SHORT).show();
                makeInvisible();
            }

        }

        public void updateViews(ArrayList<Article> result) {
            tv_title.setText(result.get(currentArticle).title);
            tv_date.setText(result.get(currentArticle).date);
            tv_description.setText(result.get(currentArticle).description);
            Picasso.get().load(result.get(currentArticle).iv_url).into(iv_main);
            tv_articleNum.setText(currentArticle+1 + " out of " + result.size());

            pb_loading.setVisibility(View.INVISIBLE);
            makeVisible();
        }

        public void makeInvisible() {
            tv_title.setVisibility(View.INVISIBLE);
            tv_date.setVisibility(View.INVISIBLE);
            iv_main.setVisibility(View.INVISIBLE);
            tv_description.setVisibility(View.INVISIBLE);
            tv_articleNum.setVisibility(View.INVISIBLE);
        }

        public void makeVisible() {
            tv_title.setVisibility(View.VISIBLE);
            tv_date.setVisibility(View.VISIBLE);
            iv_main.setVisibility(View.VISIBLE);
            tv_description.setVisibility(View.VISIBLE);
            tv_articleNum.setVisibility(View.VISIBLE);
        }
    }

}
