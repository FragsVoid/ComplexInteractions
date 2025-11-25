package org.frags.complexInteractions.objects;

public class Option {

    private String id;
    private String text;
    private String nextStage;

    public Option(String id, String text, String nextStage) {
        this.id = id;
        this.text = text;
        this.nextStage = nextStage;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getNextStage() {
        return nextStage;
    }
}
