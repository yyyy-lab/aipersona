package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "personas")
public class PersonaEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String avatarUri;
    public String personality;
    public String backgroundStory;
    public String systemPrompt;
    public long createdAt;
    public long updatedAt;

    public PersonaEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // 生成完整的系统提示词
    public String generateFullSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 ").append(name).append("。\n\n");
        prompt.append("性格特点: ").append(personality).append("\n\n");
        prompt.append("背景故事: ").append(backgroundStory).append("\n\n");
        prompt.append("请始终保持这个人设进行对话，用符合你性格的语气和方式回应。");

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            prompt.append("\n\n额外设定:\n").append(systemPrompt);
        }

        return prompt.toString();
    }
}