package com.example.abirshukla.souschef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class sett extends AppCompatActivity {
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sett);
        editText = (EditText) findViewById(R.id.editText);
        if (!dataForUser.getEmail().equals("")) {
            editText.setHint(dataForUser.getEmail());
        }
    }
    public void sub(View view) {
        String email = editText.getText().toString();
        System.out.print("New Email: "+email);
        dataForUser.email = "";
        dataForUser.email = email;
        Toast.makeText(this, "Email  Saved", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
