package org.frags.complexInteractions.objects;

import java.util.List;

public class ConversationStage {

    private Conversation conversation;
    private String id;
    private List<String> text;
    private long delay;
    private List<Option> optionList;

    public ConversationStage(Conversation conversation, String id, List<String> text, long delay, List<Option> optionList) {
        this.conversation = conversation;
        this.id = id;
        this.text = text;
        this.delay = delay;
        this.optionList = optionList;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public String getId() {
        return id;
    }

    public List<String> getText() {
        return text;
    }

    public long getDelay() {
        return delay;
    }

    public List<Option> getOptionList() {
        return optionList;
    }
}
