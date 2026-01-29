package org.frags.complexInteractions.objects.missions;

public class MobProgress {

    private String mobType;
    private int requiredAmount;
    private int currentAmount;

    public MobProgress(String mobType, int requiredAmount) {
        this.mobType = mobType;
        this.requiredAmount = requiredAmount;
        this.currentAmount = 0;
    }

    public void addProgress() {
        this.currentAmount++;
    }

    public boolean isComplete() {
        return currentAmount >= requiredAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getMobType() {
        return mobType;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }
}
