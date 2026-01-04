package org.frags.complexInteractions.objects.conversation;

import java.util.List;

public class ConversationStage {

    private String conversation;
    private String id;
    private List<String> text;
    private long delay;
    private List<Action> actions;
    private List<Option> optionList;
    private boolean completesConversation;

    public ConversationStage(String conversation, String id, List<String> text, long delay, List<Action> actions, List<Option> optionList, boolean completesConversation) {
        this.conversation = conversation;
        this.id = id;
        this.text = text;
        this.delay = delay;
        this.actions = actions;
        this.optionList = optionList;
        this.completesConversation = completesConversation;
    }

    public boolean getCompletesConversation() {
        return completesConversation;
    }

    public boolean isCompleted() {
        return completesConversation;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getConversation() {
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

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setCompletesConversation(boolean completesConversation) {
        this.completesConversation = completesConversation;
    }

    public List<Option> getOptionList() {
        return optionList;
    }
}
