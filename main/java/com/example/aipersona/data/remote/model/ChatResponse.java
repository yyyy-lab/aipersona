package com.example.aipersona.data.remote.model;


import java.util.List;

public class ChatResponse {
    public String id;
    public List<Choice> choices;

    public static class Choice {
        public Message message;
        public String finish_reason;
    }

    public static class Message {
        public String role;
        public String content;
    }

    // 获取AI回复内容
    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
            return choices.get(0).message.content;
        }
        return "";
    }
}