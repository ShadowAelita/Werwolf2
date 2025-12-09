
package com.example.werewolfmoderator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
    public List<Player> players = new ArrayList<>();
    public int round = 1;
    public boolean armorPaired = false;
    public int armorA = -1;
    public int armorB = -1;

    public int diebFirstPick = -1;
    public int diebSecondPick = -1;
    public boolean diebSwapThisNight = false;

    public int baeckerCurrentHolder = -1;
    public Bread baeckerBread = null;

    public int matratzeIndex = -1;
    public int matratzeTarget = -1;

    public int wolfTarget = -1;

    public int witchSave = -1;
    public int witchPoison = -1;
    public boolean witchHasSave = true;
    public boolean witchHasPoison = true;

    public int dayLynch = -1;

    public List<String> lastNightLog = new ArrayList<>();

    public GameEngine() {}

    public void addPlayer(String name) {
        players.add(new Player(name));
    }

    public void removePlayer(int index) {
        if (index>=0 && index<players.size()) players.remove(index);
    }

    public void distributeRoles(List<Role> roles) {
        List<Role> copy = new ArrayList<>(roles);
        Collections.shuffle(copy);
        for (int i=0;i<players.size();i++) {
            Role r = Role.UNASSIGNED;
            if (i < copy.size()) r = copy.get(i);
            players.get(i).role = r;
            players.get(i).alive = true;
            players.get(i).resetPerGame();
        }
        matratzeIndex = findRoleIndex(Role.DORFMATRATZE);
    }

    public int findRoleIndex(Role role) {
        for (int i=0;i<players.size();i++) {
            Player p = players.get(i);
            if (p.role == role && p.alive) return i;
        }
        return -1;
    }

    public List<Integer> aliveIndices() {
        List<Integer> out = new ArrayList<>();
        for (int i=0;i<players.size();i++) if (players.get(i).alive) out.add(i);
        return out;
    }

    public static class Bread {
        public int quality;
        public int ownerIndex;
        public int passCount;
        public List<Integer> passedTo;

        public Bread(int quality, int owner) {
            this.quality = quality;
            this.ownerIndex = owner;
            this.passCount = 0;
            this.passedTo = new ArrayList<>();
            this.passedTo.add(owner);
        }
    }

    public void baeckerCreateBread(int ownerIndex, int quality) {
        this.baeckerCurrentHolder = ownerIndex;
        this.baeckerBread = new Bread(quality, ownerIndex);
    }

    public boolean baeckerPassBread(int fromIndex, int toIndex, boolean eaten) {
        if (baeckerBread == null) return false;
        if (fromIndex == toIndex) return false;
        if (baeckerBread.passedTo.size() >= 3 && !eaten) {
            baeckerBread = null;
            return false;
        }
        baeckerBread.passCount++;
        baeckerBread.ownerIndex = toIndex;
        baeckerBread.passedTo.add(toIndex);
        return true;
    }

    public List<String> resolveNight() {
        lastNightLog = new ArrayList<>();
        List<Integer> diedThisNight = new ArrayList<>();

        if (diebSwapThisNight && diebFirstPick>=0 && diebSecondPick>=0) {
            Player p1 = players.get(diebFirstPick);
            Player p2 = players.get(diebSecondPick);
            Role r1 = p1.role;
            Role r2 = p2.role;
            p1.role = r2;
            p2.role = r1;
            lastNightLog.add("Dieb tauschte Rollen zwischen " + p1.name + " und " + p2.name);
        }

        if (baeckerBread != null) {
            if (baeckerBread.quality == 2) {
                int victim = baeckerBread.ownerIndex;
                if (victim>=0 && players.get(victim).alive) {
                    diedThisNight.add(victim);
                    lastNightLog.add(players.get(victim).name + " starb durch schlechtes Brot");
                }
            } else if (baeckerBread.quality == 1) {
                lastNightLog.add("Gutes Brot schützt " + players.get(baeckerBread.ownerIndex).name + " diese Nacht");
                players.get(baeckerBread.ownerIndex).protectedByGoodBread = true;
            }
            baeckerBread = null;
        }

        int candidate = wolfTarget;
        lastNightLog.add("Werwölfe zielten auf: " + (candidate>=0 ? players.get(candidate).name : "—"));

        int matratzeAliveIndex = findRoleIndex(Role.DORFMATRATZE);
        boolean candidateDies = false;
        boolean matratzeDies = false;

        if (candidate >= 0) {
            if (matratzeAliveIndex >= 0 && candidate == matratzeAliveIndex) {
                candidateDies = false;
                lastNightLog.add("Dorfmatratze wurde direkt angegriffen und überlebte (Schutz).");
            } else if (matratzeTarget >= 0 && matratzeTarget == candidate) {
                candidateDies = true;
                matratzeDies = true;
                lastNightLog.add("Dorfmatratze schlief bei " + players.get(candidate).name + " — beide sterben, falls nicht geheilt.");
            } else {
                candidateDies = true;
            }
        }

        if (witchSave >= 0) {
            if (witchSave == candidate) {
                candidateDies = false;
                lastNightLog.add("Hexe rettete " + players.get(witchSave).name);
            }
            if (matratzeAliveIndex>=0 && witchSave == matratzeAliveIndex) {
                matratzeDies = false;
                lastNightLog.add("Hexe rettete die Dorfmatratze");
            }
            witchHasSave = false;
        }

        if (witchPoison >= 0) {
            if (witchPoison >=0 && witchPoison < players.size()) {
                int target = witchPoison;
                if (players.get(target).alive) {
                    diedThisNight.add(target);
                    lastNightLog.add(players.get(target).name + " wurde von der Hexe vergiftet");
                }
            }
            witchHasPoison = false;
        }

        if (candidate>=0 && players.get(candidate).protectedByGoodBread) {
            candidateDies = false;
            lastNightLog.add(players.get(candidate).name + " wurde durch gutes Brot vor Werwölfen geschützt");
        }

        if (candidateDies && candidate>=0) {
            if (players.get(candidate).alive) {
                diedThisNight.add(candidate);
                lastNightLog.add(players.get(candidate).name + " wurde von Werwölfen getötet");
            }
        }
        if (matratzeDies && matratzeAliveIndex>=0) {
            if (players.get(matratzeAliveIndex).alive) {
                diedThisNight.add(matratzeAliveIndex);
                lastNightLog.add(players.get(matratzeAliveIndex).name + " (Dorfmatratze) starb");
            }
        }

        List<Integer> unique = new ArrayList<>();
        for (int idx : diedThisNight) if (!unique.contains(idx)) unique.add(idx);
        List<String> diedNames = new ArrayList<>();
        for (int idx: unique) {
            Player p = players.get(idx);
            if (p.alive) {
                p.alive = false;
                diedNames.add(p.name);
            }
        }

        resetNightChoicesAfterResolution();

        if (diedNames.isEmpty()) lastNightLog.add("Keine Toten diese Nacht.");
        else lastNightLog.add("Gestorben: " + String.join(", ", diedNames));

        return lastNightLog;
    }

    private void resetNightChoicesAfterResolution() {
        wolfTarget = -1;
        diebFirstPick = -1;
        diebSecondPick = -1;
        diebSwapThisNight = false;
        matratzeTarget = -1;
        baeckerCurrentHolder = -1;
        baeckerBread = null;
        witchSave = -1;
        witchPoison = -1;
        round++;
        for (Player p: players) {
            p.protectedByGoodBread = false;
        }
    }

    public String applyDayLynch(int lynchedIndex) {
        if (lynchedIndex < 0 || lynchedIndex >= players.size()) return "Ungültige Wahl.";
        Player p = players.get(lynchedIndex);
        if (!p.alive) return "Spieler ist bereits tot.";
        p.alive = false;
        if (p.role == Role.DORFTROTTEL) {
            return p.name + " war Dorftrottel — Dorftrottel gewinnt sofort!";
        } else {
            return p.name + " wurde gelyncht.";
        }
    }

    public void resetGame() {
        players.clear();
        round = 1;
        armorPaired = false;
        armorA = armorB = -1;
        diebFirstPick = diebSecondPick = -1;
        diebSwapThisNight = false;
        baeckerBread = null;
        baeckerCurrentHolder = -1;
        matratzeIndex = -1;
        matratzeTarget = -1;
        wolfTarget = -1;
        witchSave = witchPoison = -1;
        witchHasSave = true;
        witchHasPoison = true;
        dayLynch = -1;
        lastNightLog = new ArrayList<>();
    }
}
