package com.normie.user_center.common;

/**
 * 聊天系统消息
 */

public class Message {
    private int senderId;
    private int recipientId;
    private String content;
    private String time;

    public Message(int senderId, int recipientId, String content, String time) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.time = time;
    }
}
