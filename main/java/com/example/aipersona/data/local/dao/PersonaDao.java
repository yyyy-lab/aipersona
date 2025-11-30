package com.example.aipersona.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aipersona.data.local.entity.PersonaEntity;

import java.util.List;

@Dao
public interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY updatedAt DESC")
    LiveData<List<PersonaEntity>> getAllPersonas();

    @Query("SELECT * FROM personas WHERE id = :id")
    PersonaEntity getPersonaById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPersona(PersonaEntity persona);

    @Update
    void updatePersona(PersonaEntity persona);

    @Delete
    void deletePersona(PersonaEntity persona);
}