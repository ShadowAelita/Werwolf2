package com.example.werewolfmoderator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    GameEngine engine;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        engine = GameHolder.engine;

        EditText etName = findViewById(R.id.editName);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnRoleSelect = findViewById(R.id.btnRoleSelect);
        Button btnStartNight = findViewById(R.id.btnStartNight);
        ListView listPlayers = findViewById(R.id.listPlayers);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listPlayers.setAdapter(adapter);
        refreshList();

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                engine.addPlayer(name);
                etName.setText("");
                refreshList();
            }
        });

        listPlayers.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("Spieler entfernen")
                .setMessage("Spieler '"+engine.players.get(position).name+"' entfernen?")
                .setPositiveButton("Ja", (d,i2) -> {
                    engine.removePlayer(position);
                    refreshList();
                })
                .setNegativeButton("Nein", null)
                .show();
            return true;
        });

        btnRoleSelect.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RoleSelectionActivity.class));
        });

        btnStartNight.setOnClickListener(v -> {
            GameHolder.engine = engine;
            startActivity(new Intent(MainActivity.this, NightActivity.class));
        });
    }

    private void refreshList() {
        adapter.clear();
        for (Player p: engine.players) {
            String s = p.name + " - " + (p.role==Role.UNASSIGNED?"(nicht zugewiesen)":p.role.toString()) + (p.alive?"":" [tot]");
            adapter.add(s);
        }
        adapter.notifyDataSetChanged();
    }
}
