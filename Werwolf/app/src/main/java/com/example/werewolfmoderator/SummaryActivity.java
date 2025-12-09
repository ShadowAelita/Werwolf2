package com.example.werewolfmoderator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ListView lv = findViewById(R.id.listDeaths);
        Button btnContinue = findViewById(R.id.btnNextDay);

        List<String> log = GameHolder.lastNightLog;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, log);
        lv.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, DayActivity.class));
            finish();
        });
    }
}
