package com.example.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Position extends AppCompatActivity implements View.OnClickListener {

    private Button boutonN;
    private Button boutonO;
    private Button boutonE;
    private Button boutonS;
    private String nbTable;
    private String pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_position);
        Intent intent = getIntent();
        nbTable = intent.getStringExtra("nbTable");
        boutonN = findViewById(R.id.buttonN);
        boutonO = findViewById(R.id.buttonO);
        boutonE = findViewById(R.id.buttonE);
        boutonS = findViewById(R.id.buttonS);
        boutonN.setOnClickListener(this);
        boutonO.setOnClickListener(this);
        boutonE.setOnClickListener(this);
        boutonS.setOnClickListener(this);
        System.out.println(nbTable);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonN) {
            pos = "N";
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("nbTable", nbTable);
            intent.putExtra("pos", pos);
            startActivity(intent);
        }
        if (id == R.id.buttonO) {
            pos = "O";
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("nbTable", nbTable);
            intent.putExtra("pos", pos);
            startActivity(intent);
        }
        if (id == R.id.buttonE) {
            pos = "E";
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("nbTable", nbTable);
            intent.putExtra("pos", pos);
            startActivity(intent);
        }
        if (id == R.id.buttonS) {
            pos = "S";
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("nbTable", nbTable);
            intent.putExtra("pos", pos);
            startActivity(intent);
        }
    }
}