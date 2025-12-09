package com.example.werewolfmoderator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class NightActivity extends AppCompatActivity {
    GameEngine engine;
    TextView tvInstruction;
    ListView lvChoices;
    Button btnNext;
    ArrayAdapter<String> adapter;
    List<Integer> displayedIndices;
    int step = 0;
    List<Role> sequence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night);

        engine = GameHolder.engine;
        tvInstruction = findViewById(R.id.tvInstruction);
        lvChoices = findViewById(R.id.listAlive);
        btnNext = findViewById(R.id.btnNext);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvChoices.setAdapter(adapter);

        buildSequence();
        showStep();

        lvChoices.setOnItemClickListener((p, v, pos, id) -> {
            int playerIndex = displayedIndices.get(pos);
            Role current = sequence.get(step);
            handleRoleSelection(current, playerIndex);
            step++;
            showStep();
        });

        btnNext.setOnClickListener(v -> {
            step++;
            showStep();
        });
    }

    private void buildSequence() {
        sequence = new ArrayList<>();
        if (engine.round == 1) sequence.add(Role.ARMOR);
        if (engine.round != 1) sequence.add(Role.DIEB);
        sequence.add(Role.BAECKER);
        sequence.add(Role.SEHER);
        sequence.add(Role.DORFMATRATZE);
        sequence.add(Role.WERWOLF);
        sequence.add(Role.HEXE);
    }

    private void showStep() {
        if (step >= sequence.size()) {
            List<String> log = engine.resolveNight();
            GameHolder.lastNightLog = log;
            startActivity(new Intent(this, SummaryActivity.class));
            finish();
            return;
        }

        Role current = sequence.get(step);
        tvInstruction.setText("Bitte wecken: " + roleToGerman(current));
        adapter.clear();
        displayedIndices = new ArrayList<>();
        for (int i=0;i<engine.players.size();i++) {
            Player p = engine.players.get(i);
            if (!p.alive) continue;
            displayedIndices.add(i);
            String label = p.name + (p.role==Role.UNASSIGNED?" (nicht zugewiesen)":" - "+roleToGerman(p.role));
            adapter.add(label);
        }
        adapter.notifyDataSetChanged();
    }

    private void handleRoleSelection(Role current, int playerIndex) {
        switch (current) {
            case ARMOR:
                if (!engine.armorPaired) {
                    if (engine.armorA == -1) {
                        engine.armorA = playerIndex;
                        showMessage("Rüstung: Wähle nun den zweiten Spieler.");
                        step--;
                    } else {
                        engine.armorB = playerIndex;
                        engine.armorPaired = true;
                        showMessage("Rüstungspaar: " + engine.players.get(engine.armorA).name + " & " + engine.players.get(engine.armorB).name);
                    }
                }
                break;
            case DIEB:
                if (engine.diebFirstPick == -1) {
                    engine.diebFirstPick = playerIndex;
                    showMessage("Dieb: Wähle die zweite Karte zum Tauschen.");
                    step--;
                } else {
                    engine.diebSecondPick = playerIndex;
                    engine.diebSwapThisNight = true;
                    showMessage("Dieb tauscht Karten.");
                }
                break;
            case BAECKER:
                showBaeckerDialog(playerIndex);
                break;
            case SEHER:
                String r = roleToGerman(engine.players.get(playerIndex).role);
                new AlertDialog.Builder(this)
                        .setTitle("Seher-Ergebnis")
                        .setMessage(engine.players.get(playerIndex).name + " ist: " + r)
                        .setPositiveButton("OK", null)
                        .show();
                break;
            case DORFMATRATZE:
                engine.matratzeTarget = playerIndex;
                showMessage("Dorfmatratze schläft bei " + engine.players.get(playerIndex).name);
                break;
            case WERWOLF:
                engine.wolfTarget = playerIndex;
                showMessage("Werwölfe wählten " + engine.players.get(playerIndex).name);
                break;
            case HEXE:
                showWitchDialog();
                break;
            default:
                break;
        }
    }

    private void showBaeckerDialog(int playerIndex) {
        new AlertDialog.Builder(this)
                .setTitle("Bäcker - Brot wählen")
                .setItems(new CharSequence[]{"Gut (schützt vor Werwölfen)","Schlecht (tötet in nächster Nacht)"}, (dialog, which) -> {
                    int quality = (which==0)?1:2;
                    engine.baeckerCreateBread(playerIndex, quality);
                    showMessage("Brot gesetzt auf " + engine.players.get(playerIndex).name);
                })
                .show();
    }

    private void showWitchDialog() {
        String message = "Werwölfe griffen an: ";
        if (engine.wolfTarget >=0) message += engine.players.get(engine.wolfTarget).name;
        else message += "Niemand";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hexe")
                .setMessage(message)
                .setPositiveButton("Heilen (wenn verfügbar)", (d, i) -> {
                    if (engine.witchHasSave) {
                        engine.witchSave = engine.wolfTarget;
                        engine.witchHasSave = false;
                        showMessage("Hexe heilt " + (engine.wolfTarget>=0?engine.players.get(engine.wolfTarget).name:"niemand"));
                    } else showMessage("Heiltrank bereits benutzt.");
                })
                .setNegativeButton("Vergiften", (d, i) -> {
                    if (engine.witchHasPoison) {
                        for (int j=0;j<engine.players.size();j++){
                            if (engine.players.get(j).alive && engine.players.get(j).role != Role.HEXE) {
                                engine.witchPoison = j;
                                engine.witchHasPoison = false;
                                showMessage("Hexe vergiftete " + engine.players.get(j).name);
                                break;
                            }
                        }
                    } else showMessage("Gifttrank bereits benutzt.");
                })
                .setNeutralButton("Weiter", null)
                .show();
    }

    private void showMessage(String m) {
        new AlertDialog.Builder(this).setMessage(m).setPositiveButton("OK", null).show();
    }

    private String roleToGerman(Role r) {
        switch (r) {
            case ARMOR: return "Rüstung";
            case DIEB: return "Dieb";
            case BAECKER: return "Bäcker";
            case SEHER: return "Seher";
            case DORFMATRATZE: return "Dorfmatratze";
            case WERWOLF: return "Werwolf";
            case HEXE: return "Hexe";
            case DORFTROTTEL: return "Dorftrottel";
            case DORFBEWOHNER: return "Dorfbewohner";
            default: return "(nicht zugewiesen)";
        }
    }
}
