package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "follows",
        foreignKeys = @ForeignKey(
                entity = PersonaEntity.class,
                parentColumns = "id",
                childColumns = "followedPersonaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("followedPersonaId")}
)
public class FollowEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long followedPersonaId;  // 被关注的Persona ID
    public long followedAt;         // 关注时间

    public FollowEntity() {
        this.followedAt = System.currentTimeMillis();
    }

    public FollowEntity(long followedPersonaId) {
        this.followedPersonaId = followedPersonaId;
        this.followedAt = System.currentTimeMillis();
    }
}