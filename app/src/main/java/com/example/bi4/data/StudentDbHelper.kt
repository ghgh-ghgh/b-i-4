package com.example.bi4.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "students.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_STUDENTS = "students"
        const val COL_ID = "id"
        const val COL_MSSV = "mssv"
        const val COL_FULL_NAME = "full_name"
        const val COL_EMAIL = "email"
        const val COL_AVATAR = "avatar_url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_STUDENTS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_MSSV TEXT, "
                + "$COL_FULL_NAME TEXT, "
                + "$COL_EMAIL TEXT, "
                + "$COL_AVATAR TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    fun getAllStudents(): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID))
                val mssv = cursor.getString(cursor.getColumnIndexOrThrow(COL_MSSV))
                val fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
                val avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_AVATAR))
                studentList.add(Student(id, mssv, fullName, email, avatarUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return studentList
    }

    fun insertStudent(student: Student): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_MSSV, student.mssv)
            put(COL_FULL_NAME, student.fullName)
            put(COL_EMAIL, student.email)
            put(COL_AVATAR, student.avatarUrl)
        }
        val id = db.insert(TABLE_STUDENTS, null, values)
        db.close()
        return id
    }

    fun updateStudent(student: Student): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_MSSV, student.mssv)
            put(COL_FULL_NAME, student.fullName)
            put(COL_EMAIL, student.email)
            put(COL_AVATAR, student.avatarUrl)
        }
        val rows = db.update(TABLE_STUDENTS, values, "$COL_ID = ?", arrayOf(student.id.toString()))
        db.close()
        return rows
    }

    fun deleteStudent(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_STUDENTS, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}
