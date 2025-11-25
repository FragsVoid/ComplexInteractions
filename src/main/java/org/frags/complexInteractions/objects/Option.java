package org.frags.complexInteractions.objects;

public class Option {

    private String id;
    private String text;
    private ConversationStage nextStage;

    public Option(String id, String text, ConversationStage nextStage) {
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

    public ConversationStage getNextStage() {
        return nextStage;
    }
}
