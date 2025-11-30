package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "comments",
        foreignKeys = {
                @ForeignKey(
                        entity = PostEntity.class,
                        parentColumns = "id",
                        childColumns = "postId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = PersonaEntity.class,
                        parentColumns = "id",
                        childColumns = "commenterPersonaId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("postId"), @Index("commenterPersonaId")}
)
public class CommentEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long postId;                  // 评论的帖子ID
    public long commenterPersonaId;      // 评论者Persona ID
    public String commenterPersonaName;  // 评论者名称（冗余字段）
    public String content;               // 评论内容
    public long timestamp;               // 评论时间

    public CommentEntity() {
        this.timestamp = System.currentTimeMillis();
    }

    public CommentEntity(long postId, long commenterPersonaId, String commenterPersonaName, String content) {
        this.postId = postId;
        this.commenterPersonaId = commenterPersonaId;
        this.commenterPersonaName = commenterPersonaName;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}