package com.example.aipersona.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "likes",
        foreignKeys = @ForeignKey(
                entity = PostEntity.class,
                parentColumns = "id",
                childColumns = "postId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("postId")}
)
public class LikeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long postId;      // 点赞的帖子ID
    public long likedAt;     // 点赞时间

    public LikeEntity() {
        this.likedAt = System.currentTimeMillis();
    }

    public LikeEntity(long postId) {
        this.postId = postId;
        this.likedAt = System.currentTimeMillis();
    }
}