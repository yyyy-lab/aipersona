package com.example.aipersona.data.remote.model;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<ChatMessage> messages;
    public float temperature;

    public ChatRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.temperature = 0.7f;
    }

    public static class ChatMessage {
        public String role;  // "system", "user", "assistant"
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}