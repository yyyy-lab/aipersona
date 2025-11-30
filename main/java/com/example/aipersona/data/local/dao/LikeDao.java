package com.example.aipersona.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aipersona.data.local.entity.LikeEntity;

@Dao
public interface LikeDao {

    // 获取某个帖子的点赞数量
    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    int getLikeCount(long postId);

    // 判断某个帖子当前是否被点赞（返回数量，大于 0 就表示已点赞）
    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    int isLiked(long postId);

    // 点赞（插入一条 Like 记录）
    @Insert
    long like(LikeEntity like);

    // 取消点赞（根据 postId 删除记录）
    @Query("DELETE FROM likes WHERE postId = :postId")
    void unlike(long postId);

    // 可选：删除某个帖子的所有点赞记录（如果以后需要的话）
    @Query("DELETE FROM likes WHERE postId = :postId")
    void deleteLikesByPostId(long postId);
}
