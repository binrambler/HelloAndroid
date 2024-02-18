package com.example.hello


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

//все данные должны быть строковые - так проще работать с БД
//после изменения полей, не забыть увеличить на единицу versionDb, чтоб БД обновилась в устройстве
const val versionDb = 1

enum class Config(val valueDefault: String) {
    NAME("defaultName"),
    DATA("defaultData"),
    QTY("0"),
    PRICE("0.1"),
    NEW("defaultNew");

    companion object {
        //поиск значения valueDefault по name
        infix fun findValue(name: String): String =
            Config.entries.first { it.name == name }.valueDefault
    }
}

class DbConfig(context: Context) :
    SQLiteOpenHelper(context, "config.db", null, versionDb) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = "create table MAIN (" +
                "ID integer primary key autoincrement, " +
                "FIELD text, " +
                "VALUE text)"
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //ключ: имя поля, а если value = -1, то поле надо удалить;
        //если = 1, то добавить; если 0 - то ничего не делать
        val fieldMap = mutableMapOf<String, Int>()
        //в fieldMap добавляем поля из БД, т.е. те, которые уже есть
        val cursor = db.query("MAIN", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                fieldMap[cursor.getString(1)] = -1
            } while (cursor.moveToNext())
        }
        cursor.close()
        //у полей, которые уже есть ставим value = 0, а у тех, которых нет = 1
        Config.entries.forEach {
            fieldMap[it.name] = if (it.name in fieldMap.keys) 0 else 1
        }
        //добавляем/удаляем строки в БД
        fieldMap.forEach { it ->
            //удаляем поля
            if (it.value == -1)
                db.delete("MAIN", "FIELD = ?", arrayOf(it.key))
            //добавляем поля
            else if (it.value == 1) {
                val values = ContentValues()
                values.put("FIELD", it.key)
                values.put("VALUE", Config.findValue(it.key))
                db.insert("MAIN", null, values)
            }
        }
    }

    fun addData() {
        val db = this.writableDatabase
        //если в базе уже есть хоть одна строка с настройками, то не добавляем
        val cursor = db.query("MAIN", null, null, null, null, null, null)
        //в БД нет ни одной строки, добавляем
        if (!cursor.moveToFirst()) {
            Config.entries.forEach {
                val values = ContentValues()
                values.put("FIELD", it.name)
                values.put("VALUE", it.valueDefault)
                db.insert("MAIN", null, values)
            }
        }
        cursor.close()
        db.close()
    }

    fun updateData(field: String, value: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("VALUE", value)
        db.update("MAIN", values, "FIELD = ?", arrayOf(field))
        db.close()
    }

    fun readData(field: String): String {
        val db = this.readableDatabase
        val cursor =
            db.query("MAIN", arrayOf("VALUE"), "FIELD = ?", arrayOf(field), null, null, null)
        val result = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        db.close()
        return result
    }
}