package com.example.notemelab3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes_db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_SUBTITLE = "subtitle";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_IMAGE = "image";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NOTES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_SUBTITLE + " TEXT, "
                + COLUMN_TEXT + " TEXT, "
                + COLUMN_COLOR + " TEXT, "
                + COLUMN_IMAGE + " BLOB)";
        db.execSQL(CREATE_TABLE);
    }

    public Cursor getNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " DESC";
        return db.rawQuery(query, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public long addNote(String title, String subtitle, String text, String color, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_SUBTITLE, subtitle);
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IMAGE, image);
        return db.insert(TABLE_NOTES, null, values);
    }

    public long updateNote(int id, String title, String subtitle, String text, String color, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_SUBTITLE, subtitle);
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IMAGE, image);
        return db.update(TABLE_NOTES, values, "id=?", new String[]{String.valueOf(id)});
    }

    public byte[] getImageById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COLUMN_IMAGE}, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            byte[] image = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
            cursor.close();
            return image;
        }
        return null;
    }
    public long deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTES, "id = ?", new String[]{String.valueOf(id)});
    }

}