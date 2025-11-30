package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "messages",
        foreignKeys = @ForeignKey(
                entity = PersonaEntity.class,
                parentColumns = "id",
                childColumns = "personaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("personaId")}
)
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long personaId;
    public String content;
    public boolean isFromUser;
    public long timestamp;

    public MessageEntity() {
        this.timestamp = System.currentTimeMillis();
    }

    public MessageEntity(long personaId, String content, boolean isFromUser) {
        this.personaId = personaId;
        this.content = content;
        this.isFromUser = isFromUser;
        this.timestamp = System.currentTimeMillis();
    }
}