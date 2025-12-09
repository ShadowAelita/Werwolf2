package com.example.werewolfmoderator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.app.AlertDialog;

public class DayActivity extends AppCompatActivity {
    GameEngine engine;
    ArrayAdapter<String> adapter;
    ListView lvAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        engine = GameHolder.engine;

        lvAlive = findViewById(R.id.lvAlive);
        Button btnLynch = findViewById(R.id.btnLynch);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        lvAlive.setAdapter(adapter);
        refreshList();

        btnLynch.setOnClickListener(v -> {
            int pos = lvAlive.getCheckedItemPosition();
            if (pos < 0) {
                new AlertDialog.Builder(this).setMessage("Bitte einen Spieler auswählen").setPositiveButton("OK", null).show();
                return;
            }
            int idx = -1; int c=0;
            for (int i=0;i<engine.players.size();i++){
                if (!engine.players.get(i).alive) continue;
                if (c==pos) { idx = i; break; }
                c++;
            }
            String result = engine.applyDayLynch(idx);
            new AlertDialog.Builder(this).setMessage(result).setPositiveButton("OK", (d,i2) -> finish()).show();
        });
    }

    private void refreshList() {
        adapter.clear();
        for (Player p: engine.players) if (p.alive) adapter.add(p.name + " - " + (p.role==Role.UNASSIGNED? "?" : p.role.toString()));
        adapter.notifyDataSetChanged();
    }
}
