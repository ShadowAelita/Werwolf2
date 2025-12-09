package com.example.werewolfmoderator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;

public class RoleSelectionActivity extends AppCompatActivity {
    GameEngine engine;
    ArrayAdapter<String> adapter;
    List<Role> selectedRoles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);
        engine = GameHolder.engine;

        ListView lvRoles = findViewById(R.id.lvRoles);
        Button btnAddRole = findViewById(R.id.btnAddRole);
        Button btnAutoAssign = findViewById(R.id.btnAutoAssign);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvRoles.setAdapter(adapter);

        btnAddRole.setOnClickListener(v -> {
            String[] roles = {"Werwolf","Dorfbewohner","Seher","Hexe","Dorfmatratze","Bäcker","Dieb","Dorftrottel","Rüstung"};
            new AlertDialog.Builder(this)
                .setTitle("Rolle hinzufügen")
                .setItems(roles, (dialog, which) -> {
                    Role r = Role.DORFBEWOHNER;
                    switch(which) {
                        case 0: r = Role.WERWOLF; break;
                        case 1: r = Role.DORFBEWOHNER; break;
                        case 2: r = Role.SEHER; break;
                        case 3: r = Role.HEXE; break;
                        case 4: r = Role.DORFMATRATZE; break;
                        case 5: r = Role.BAECKER; break;
                        case 6: r = Role.DIEB; break;
                        case 7: r = Role.DORFTROTTEL; break;
                        case 8: r = Role.ARMOR; break;
                    }
                    selectedRoles.add(r);
                    refreshRoleList();
                })
                .show();
        });

        btnAutoAssign.setOnClickListener(v -> {
            while (selectedRoles.size() < engine.players.size()) selectedRoles.add(Role.DORFBEWOHNER);
            engine.distributeRoles(selectedRoles);
            new AlertDialog.Builder(this).setMessage("Rollen verteilt").setPositiveButton("OK",null).show();
        });
    }

    private void refreshRoleList() {
        adapter.clear();
        for (Role r: selectedRoles) adapter.add(r.toString());
        adapter.notifyDataSetChanged();
    }
}
