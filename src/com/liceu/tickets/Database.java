package com.liceu.tickets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// public interface definition for Database
interface Database {
    public void refreshDataFromServer(LiceuServer server) throws ServerError;

    public List getTicketList();

    public Ticket getTicket(int tickid);

    public List getComments(int tickid);

    public void removeTicket(int tickid);

    public void addComment(Comment c, int tickid);
}


// Database implementation
class DatabaseImp extends SQLiteOpenHelper implements Database {
    static final String TAG = "Database";
    static final String DB_NAME = "tickets.db";
    static final int DB_VERSION = 1;

    // Constructor
    public DatabaseImp(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called only once, first time the DB is created
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table ticket ( text text, user text, place text, id int )";
        db.execSQL(sql);
        Log.v(TAG, "onCreated SQL: " + sql);
        sql = "create table comment ( text text, user text, tickid int )";
        db.execSQL(sql);
        Log.v(TAG, "onCreated SQL: " + sql);
    }

    // Called whenever newVersion != oldVersion
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists ticket");
        db.execSQL("drop table if exists comment");
        onCreate(db);
        Log.v(TAG, "onUpgrade called");
    }

    public void deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from ticket");
        db.execSQL("delete from comment");
        Log.v(TAG, "deleteAllData");
    }

    private void newTicket(Ticket t) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.clear();
        values.put("text", t.text);
        values.put("user", t.user);
        values.put("place", t.place);
        values.put("id", t.id);

        db.insertOrThrow("ticket", null, values);
    }

    private void newComment(Comment c, int tickid) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.clear();
        values.put("text", c.text);
        values.put("user", c.author);
        values.put("tickid", tickid);

        db.insertOrThrow("comment", null, values);
    }


    private void readComments(JSONArray jsar, int tickid) throws Exception {
        for (int i = 0; i < jsar.length(); i++) {
            JSONObject jo = jsar.getJSONObject(i);
            Comment c = new Comment(jo.getString("text"), jo.getString("author"));
            newComment(c, tickid);
        }
    }

    // Refresh database with server data
    public void refreshDataFromServer(LiceuServer server) throws ServerError {
        try {
            String raw = server.readJson("https://apps.esliceu.com/tickets/gettickets");

            JSONArray ar = new JSONArray(raw);
            int l = ar.length();

            deleteAllData();

            for (int i = 0; i < l; i++) {
                JSONObject jo = ar.getJSONObject(i);
                //List comments = get_comments(jo.getJSONArray("comments"));
                Ticket t = new Ticket(
                        jo.getInt("id"),
                        jo.getString("description"),
                        jo.getString("reporter_email"),
                        jo.getString("place")
                );
                newTicket(t);
                readComments(jo.getJSONArray("comments"), t.id);
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new ServerError();
    }

    private Ticket getTicketFromCursor(Cursor cs) {
        return new Ticket(
                cs.getInt(3),
                cs.getString(0),
                cs.getString(1),
                cs.getString(2)
        );
    }

    // Get ticket list from database
    public List getTicketList() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("ticket", null, null, null, null, null, null);

        List result = new ArrayList();

        cs.moveToFirst();
        while (cs.isAfterLast() == false) {
            result.add(getTicketFromCursor(cs));
            cs.moveToNext();
        }

        return result;
    }

    // Get ticket from database, providing ticket id
    public Ticket getTicket(int tickid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("ticket", null, "id=" + String.valueOf(tickid), null, null, null, null);
        cs.moveToFirst();
        return getTicketFromCursor(cs);
    }

    // Get comment list, providing ticket id
    public List getComments(int tickid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cs = db.query("comment", null, "tickid=" + String.valueOf(tickid), null, null, null, null);

        List result = new ArrayList();

        cs.moveToFirst();
        while (cs.isAfterLast() == false) {
            result.add(new Comment(cs.getString(0), cs.getString(1)));
            cs.moveToNext();
        }

        Collections.reverse(result);
        return result;
    }

    public void removeTicket(int tickid) {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from ticket where id=" + String.valueOf(tickid));
    }

    public void addComment(Comment c, int tickid) {
        newComment(c, tickid);
    }
}
