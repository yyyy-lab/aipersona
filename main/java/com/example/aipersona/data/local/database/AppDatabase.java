package com.example.aipersona.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aipersona.data.local.dao.CommentDao;
import com.example.aipersona.data.local.dao.FollowDao;
import com.example.aipersona.data.local.dao.LikeDao;
import com.example.aipersona.data.local.dao.MessageDao;
import com.example.aipersona.data.local.dao.PersonaDao;
import com.example.aipersona.data.local.dao.PostDao;
import com.example.aipersona.data.local.entity.CommentEntity;
import com.example.aipersona.data.local.entity.FollowEntity;
import com.example.aipersona.data.local.entity.LikeEntity;
import com.example.aipersona.data.local.entity.MessageEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.local.entity.PostEntity;

@Database(
        entities = {
                PersonaEntity.class,
                MessageEntity.class,
                PostEntity.class,
                FollowEntity.class,
                LikeEntity.class,
                CommentEntity.class
        },
        version = 3,  // 数据库版本号
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract PersonaDao personaDao();
    public abstract MessageDao messageDao();
    public abstract PostDao postDao();
    public abstract FollowDao followDao();
    public abstract LikeDao likeDao();
    public abstract CommentDao commentDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ai_persona_database"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
