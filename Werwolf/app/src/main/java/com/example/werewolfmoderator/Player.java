package com.example.werewolfmoderator;

public class Player {
    public String name;
    public Role role;
    public boolean alive = true;
    public boolean protectedByGoodBread = false;

    public Player(String name) {
        this.name = name;
        this.role = Role.UNASSIGNED;
        this.alive = true;
        this.protectedByGoodBread = false;
    }

    public void resetPerGame() {
        this.protectedByGoodBread = false;
    }
}
