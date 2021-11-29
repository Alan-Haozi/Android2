package com.example.note;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "diary.db"; //数据库名
    public static final String TABLE_NAME = "diaryData"; //表名
    public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME +
            "(id integer primary key autoincrement," +
            "title text," +
            "author text," +
            "content text," +
            "picture BLOB," +
            "time text)";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL); //添加数据库，和表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //数据库的升级

    }

    public int insertData(ContentValues values) { //插入数据库数据
        SQLiteDatabase db = getWritableDatabase();
        return (int) db.insert(TABLE_NAME, null, values);
    }

    public int deleteData(String id){ //删除数据库
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, "id is ?", new String[]{id});
    }

    public int updataData(ContentValues values, String id) { //更新数据库数据
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_NAME, values, "id is ?", new String[] {id});
    }

    public List<Note> queryAllFromDb() { //查询所有
        SQLiteDatabase db = getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String author = cursor.getString(cursor.getColumnIndex("author"));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));

                Note note = new Note();
                note.setId(id);
                note.setTitle(title);
                note.setAuthor(author);
                note.setContent(content);
                note.setTime(time);

                noteList.add(note);
            }
        }
        return noteList;
    }

    public List<Note> queryFromDbByTitle(String title) { //按标头进行查询
        if (TextUtils.isEmpty(title)) { //搜索框为空时，默认查询所有
            return queryAllFromDb();
        }

        SQLiteDatabase db = getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, "title like ?", new String[] {"%" + title + "%"}, null, null, null); //模糊匹配

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String titles = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String author = cursor.getString(cursor.getColumnIndex("author"));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex("content"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));

                Note note = new Note();
                note.setId(id);
                note.setTitle(titles);
                note.setAuthor(author);
                note.setContent(content);
                note.setTime(time);

                noteList.add(note);
            }
            cursor.close();
        }
        return noteList;
    }

    public Cursor queryFromDbById(String id) { //按标头进行查询
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME,null,"id=?", new String[]{String.valueOf(id)},null,null,null);
        return cursor;
    }
}
