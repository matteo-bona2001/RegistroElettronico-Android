package com.sharpdroid.registroelettronico.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sharpdroid.registroelettronico.Interfaces.API.Event;
import com.sharpdroid.registroelettronico.Interfaces.Client.LocalEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sharpdroid.registroelettronico.Utils.Metodi.toLowerCase;

public class AgendaDB extends SQLiteOpenHelper {
    private final static String DB_NAME = "AgendaDB";
    private final static String TABLE_API = "api";
    private final static String TABLE_LOCAL = "local";
    private final static String columns[] = {
            "id", "code", "title",
            "start", "end", "allDay",
            "data_inserimento", "nota_2", "master_id",
            "classe_id", "classe_desc", "gruppo",
            "autore_desc", "autore_id", "tipo",
            "materia_desc", "materia_id"
    };  //COUNT = 17
    private final static String l_columns[] = {
            "uuid", "title", "content", "type", "day", "subject_id", "prof_id"
    };  //COUNT = 7
    private static int DB_VERSION = 8;

    public AgendaDB(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_API + " (" +
                columns[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                columns[1] + " INTEGER, " +
                columns[2] + " TEXT," +
                columns[3] + " INTEGER," +  //START
                columns[4] + " INTEGER," +  //END
                columns[5] + " INTEGER," +
                columns[6] + " INTEGER," +  //INSERIMENTO
                columns[7] + " TEXT," +
                columns[8] + " TEXT," +
                columns[9] + " TEXT," +
                columns[10] + " TEXT," +
                columns[11] + " INTEGER," +
                columns[12] + " TEXT," +
                columns[13] + " TEXT," +
                columns[14] + " TEXT," +
                columns[15] + " TEXT," +
                columns[16] + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_LOCAL + " (" +
                l_columns[0] + " TEXT UNIQUE PRIMARY KEY, " +
                l_columns[1] + " TEXT, " +
                l_columns[2] + " TEXT, " +
                l_columns[3] + " TEXT, " +
                l_columns[4] + " INTEGER, " +
                l_columns[5] + " INTEGER, " +
                l_columns[6] + " INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_API);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCAL);
        onCreate(db);
    }

    public void addEvents(List<Event> events) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(TABLE_API, null, null);
        ContentValues values;
        for (Event e : events) {
            values = new ContentValues();
            values.put("code", e.getId());
            values.put("title", e.getTitle());
            values.put("start", e.getStart().getTime());
            values.put("end", e.getEnd().getTime());
            values.put("allDay", e.isAllDay() ? 1 : 0);
            values.put("data_inserimento", e.getData_inserimento().getTime());
            values.put("nota_2", toLowerCase(e.getNota_2()));
            values.put("master_id", e.getMaster_id());
            values.put("classe_id", e.getClasse_id());
            values.put("classe_desc", e.getClasse_desc());
            values.put("gruppo", e.getGruppo());
            values.put("autore_desc", toLowerCase(e.getAutore_desc()));
            values.put("autore_id", e.getAutore_id());
            values.put("tipo", toLowerCase(e.getTipo()));
            values.put("materia_desc", toLowerCase(e.getMateria_desc()));
            values.put("materia_id", e.getMateria_id());
            db.insert(TABLE_API, null, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addLocalEvent(LocalEvent e) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues values;
        values = new ContentValues();
        values.put(l_columns[0], e.getUuid());
        values.put(l_columns[1], e.getTitle());
        values.put(l_columns[2], e.getContent());
        values.put(l_columns[3], e.getType());
        values.put(l_columns[4], e.getDay().getTime());
        values.put(l_columns[5], e.getSubjectId());
        values.put(l_columns[6], e.getProfId());
        db.insert(TABLE_LOCAL, null, values);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Event> getEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_API, new String[]{});
        List<Event> list = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            list.add(new Event(c.getString(1), c.getString(2), new Date(c.getLong(3)), new Date(c.getLong(4)), c.getInt(5) == 1, new Date(c.getLong(6)), c.getString(7), c.getString(8), c.getString(9), c.getString(10), c.getInt(11), c.getString(12), c.getString(13), c.getString(14), c.getString(15), c.getString(16)));
        }

        c.close();
        return list;
    }

    public List<Event> getLocalEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_LOCAL, new String[]{});
        List<Event> list = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            list.add(new Event(null, c.getString(1), new Date(c.getLong(4)), null, true, null, c.getString(2), null, null, null, 0, null, c.getString(6), c.getString(3), null, c.getString(5)));
        }

        c.close();
        return list;
    }

    public String getClassDescription() {
        List<Event> events = getEvents();
        if (!events.isEmpty())
            return events.get(0).getClasse_desc();
        else return null;
    }

    public List<Event> getEvents(long day) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_API + " WHERE " + columns[3] + " BETWEEN ? AND ?", new String[]{String.valueOf(day), String.valueOf(day + 86400000)});
        List<Event> list = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            list.add(new Event(c.getString(1), c.getString(2), new Date(c.getLong(3)), new Date(c.getLong(4)), c.getInt(5) == 1, new Date(c.getLong(6)), c.getString(7), c.getString(8), c.getString(9), c.getString(10), c.getInt(11), c.getString(12), c.getString(13), c.getString(14), c.getString(15), c.getString(16)));
        }

        c.close();
        return list;
    }

    public List<Event> getLocalEvents(long day) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_LOCAL + " WHERE " + l_columns[4] + " BETWEEN ? AND ?", new String[]{String.valueOf(day), String.valueOf(day + 86399999)});
        List<Event> list = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            list.add(new Event(null, c.getString(1), new Date(c.getLong(4)), null, true, null, c.getString(2), null, null, null, 0, null, c.getString(6), c.getString(3), null, c.getString(5)));
        }

        c.close();
        return list;
    }

    public List<Event> getAllEvents(long day) {
        List<Event> list = new ArrayList<>();
        list.addAll(getLocalEvents(day));
        list.addAll(getEvents(day));
        return list;
    }

    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        list.addAll(getLocalEvents());
        list.addAll(getEvents());
        return list;
    }
}
