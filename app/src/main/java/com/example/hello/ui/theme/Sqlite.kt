package com.example.hello.ui.theme

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

fun myTest(context: Context, path: String) {
    val db = SQLiteDatabase.openDatabase("$path/armtp3.db", null, SQLiteDatabase.OPEN_READONLY)
    val cursor = db.rawQuery("select DESCR from VIEW_TEST", null)
    if (cursor.moveToFirst()) {
        do {
            Log.d("bin", "${cursor.getString(0)}")
        } while (cursor.moveToNext())
    }
    cursor.close()
    db.close()
}