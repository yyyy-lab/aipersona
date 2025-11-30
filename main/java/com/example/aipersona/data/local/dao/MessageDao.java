package com.example.aipersona.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aipersona.data.local.entity.MessageEntity;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages WHERE personaId = :personaId ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> getMessagesByPersonaId(long personaId);

    @Query("SELECT * FROM messages WHERE personaId = :personaId ORDER BY timestamp DESC LIMIT :limit")
    List<MessageEntity> getRecentMessages(long personaId, int limit);

    @Insert
    long insertMessage(MessageEntity message);

    @Query("DELETE FROM messages WHERE personaId = :personaId")
    void deleteMessagesByPersonaId(long personaId);
}