package com.example.aipersona.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aipersona.data.local.entity.CommentEntity;

import java.util.List;

@Dao
public interface CommentDao {
    // 获取某个帖子的所有评论
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    LiveData<List<CommentEntity>> getCommentsByPostId(long postId);

    // 获取评论数量
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    int getCommentCount(long postId);

    // 发表评论
    @Insert
    long insertComment(CommentEntity comment);

    // 删除评论
    @Query("DELETE FROM comments WHERE id = :commentId")
    void deleteComment(long commentId);
}