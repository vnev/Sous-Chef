package com.example.abirshukla.souschef;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    Intent a;
    SharedPreferences sharedPref;
    ProgressDialog pd;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref != null) {
            String email = sharedPref.getString("email", "");
            if (!email.equals("") && DataForUser.getEmail().equals("")) {
                System.out.println(email);
                DataForUser.setEmail(email);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sous Chef");
        toolbar.hideOverflowMenu();
        setSupportActionBar(toolbar);
        if (DataForUser.getEmail().equals("")) {
            Intent se = new Intent(MainActivity.this,Sett.class);
            startActivity(se);
        }
        String ea = DataForUser.getEmail();
        // we can just pass in the toolbar ID since Android searches up the view hierarchy
        // until it finds a coordinator layout, at which point it attaches the Snackbar to it
        Snackbar.make(findViewById(R.id.toolbar), ea, Snackbar.LENGTH_SHORT).show();
        ImageView im = (ImageView) findViewById(R.id.imageView);
        Button voiceB = (Button) findViewById(R.id.button);

        a = new Intent(MainActivity.this, Ac1.class);
        pd = new ProgressDialog(this);
        pd.setMessage("Acquiring data for your dish...");
    }

    public void prom (View view) {
        promptSpeechInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent se = new Intent(MainActivity.this, Sett.class);
            startActivity(se);
        }

        if (id == R.id.action_search) {
            promptTextInput();
        }

        return super.onOptionsItemSelected(item);
    }

    public void emA (View view) {

    }

    private void promptTextInput() {
        if (searchView != null) {
            searchView.setQueryHint("Enter the name of the dish...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    String res = searchView.getQuery().toString();
                    pd.show();
                    a.putExtra("nameOfDish", res);
                    res = res.replace(" ", "%20");
                    String url = "http://abirshukla.pythonanywhere.com/searchCook/" + res + "/";
                    getHTML(url);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    private void promptSpeechInput() {
        String speech_prompt = "What is the name of the dish?";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                speech_prompt);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Snackbar.make(findViewById(R.id.toolbar), "Speech not supported", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // doesn't work yet
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            pd.hide();
            pd.cancel();
            Snackbar.make(findViewById(R.id.toolbar), "Prompt cancelled", Snackbar.LENGTH_SHORT)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String res = "";
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    res = result.get(0);
                }
                break;
            }

        }
        //Intent speak = new Intent(MainActivity.this, speaker.class);
        //speak.putExtra("res",res);
        //startActivity(speak);
        pd.show();
        a.putExtra("nameOfDish", res);
        res = res.replace(" ", "%20");
        String url = "http://abirshukla.pythonanywhere.com/searchCook/" + res + "/";
        getHTML(url);
    }

    public void getHTML(final String url) {
        System.out.println("Begin HTML");
        System.out.println("Final Url: "+url);
        final String[] d = new String[1];
        Ion.with(getApplicationContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        System.out.println("First Result: " + result);
                        a.putExtra("code", result);
                        a.putExtra("subject","");
                        pd.dismiss();
                        startActivity(a);
                    }
                });
    }
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("email",DataForUser.getEmail());
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = sharedPref.edit();
        String email = DataForUser.getEmail();
        editor.putString("email",email);
        editor.commit();
        super.onDestroy();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String email = savedInstanceState.getString("email");
    }
}
