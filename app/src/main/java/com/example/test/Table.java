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

public class Table extends AppCompatActivity implements View.OnClickListener {

    private Button boutonTable1;
    private Button boutonTable2;

    private String nbTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_table);

        boutonTable1 = findViewById(R.id.button);
        boutonTable2 = findViewById(R.id.button2);
        boutonTable1.setOnClickListener(this);
        boutonTable2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button) {
            nbTable = "1";
            Intent intent = new Intent(this, Position.class);
            intent.putExtra("nbTable", nbTable);
            startActivity(intent);

        }
        if (id == R.id.button2) {
            nbTable = "2";
            Intent intent = new Intent(this, Position.class);
            intent.putExtra("nbTable", nbTable);
            startActivity(intent);
        }
    }
}