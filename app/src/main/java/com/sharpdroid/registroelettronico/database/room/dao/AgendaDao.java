package com.sharpdroid.registroelettronico.database.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.sharpdroid.registroelettronico.database.entities.LocalAgenda;
import com.sharpdroid.registroelettronico.database.entities.RemoteAgenda;
import com.sharpdroid.registroelettronico.database.entities.RemoteAgendaInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dao
public interface AgendaDao {

    @Insert
    void insert(@NotNull LocalAgenda event);

    @Insert
    void insert(@NotNull List<RemoteAgenda> event);

    @Insert
    void insert(@NotNull RemoteAgendaInfo event);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(@NotNull LocalAgenda event);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(@NotNull RemoteAgenda event);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(@NotNull RemoteAgendaInfo event);

    @Query("DELETE FROM REMOTE_AGENDA WHERE PROFILE = :profile")
    void deleteRemote(long profile);
}