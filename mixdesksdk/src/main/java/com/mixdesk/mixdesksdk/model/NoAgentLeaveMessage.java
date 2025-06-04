package com.mixdesk.mixdesksdk.model;

public class NoAgentLeaveMessage extends BaseMessage {

    public NoAgentLeaveMessage(String content) {
        setItemViewType(TYPE_NO_AGENT_TIP);
        setContent(content);
    }

}