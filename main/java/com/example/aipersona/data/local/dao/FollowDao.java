package com.example.aipersona.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aipersona.data.local.entity.FollowEntity;

import java.util.List;

@Dao
public interface FollowDao {
    // 获取所有关注的Persona ID列表
    @Query("SELECT followedPersonaId FROM follows ORDER BY followedAt DESC")
    LiveData<List<Long>> getFollowedPersonaIds();

    // 检查是否已关注某个Persona
    @Query("SELECT COUNT(*) FROM follows WHERE followedPersonaId = :personaId")
    int isFollowing(long personaId);

    // 获取关注数量
    @Query("SELECT COUNT(*) FROM follows WHERE followedPersonaId = :personaId")
    int getFollowerCount(long personaId);

    // 关注
    @Insert
    long follow(FollowEntity follow);

    // 取消关注
    @Query("DELETE FROM follows WHERE followedPersonaId = :personaId")
    void unfollow(long personaId);

    // 删除某个Persona的所有关注记录
    @Query("DELETE FROM follows WHERE followedPersonaId = :personaId")
    void deleteFollowsByPersonaId(long personaId);
}