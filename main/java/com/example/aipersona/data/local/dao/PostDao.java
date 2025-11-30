package com.example.aipersona.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aipersona.data.local.entity.PostEntity;

import java.util.List;

@Dao
public interface PostDao {
    // 获取所有帖子（按时间倒序）
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<PostEntity>> getAllPosts();

    // 获取指定Persona的帖子
    @Query("SELECT * FROM posts WHERE personaId = :personaId ORDER BY timestamp DESC")
    LiveData<List<PostEntity>> getPostsByPersonaId(long personaId);

    // 根据ID获取帖子
    @Query("SELECT * FROM posts WHERE id = :postId")
    PostEntity getPostById(long postId);

    // 插入帖子
    @Insert
    long insertPost(PostEntity post);

    // 更新帖子
    @Update
    void updatePost(PostEntity post);

    // 删除帖子
    @Delete
    void deletePost(PostEntity post);

    // 删除指定Persona的所有帖子
    @Query("DELETE FROM posts WHERE personaId = :personaId")
    void deletePostsByPersonaId(long personaId);
}