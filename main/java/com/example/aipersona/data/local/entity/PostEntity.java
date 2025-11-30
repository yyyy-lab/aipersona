package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "posts",
        foreignKeys = @ForeignKey(
                entity = PersonaEntity.class,
                parentColumns = "id",
                childColumns = "personaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("personaId")}
)
public class PostEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long personaId;           // 发帖的Persona ID
    public String personaName;       // Persona名称（冗余字段，方便显示）
    public String content;           // 帖子内容
    public long timestamp;           // 发布时间
    public int likeCount;            // 点赞数（可选功能）
    public int commentCount;         // 评论数（可选功能）

    public PostEntity() {
        this.timestamp = System.currentTimeMillis();
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public PostEntity(long personaId, String personaName, String content) {
        this.personaId = personaId;
        this.personaName = personaName;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.likeCount = 0;
        this.commentCount = 0;
    }
}