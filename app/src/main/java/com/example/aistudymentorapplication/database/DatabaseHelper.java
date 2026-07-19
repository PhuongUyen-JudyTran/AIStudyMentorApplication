package com.example.aistudymentorapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.aistudymentorapplication.model.ChatMessage;
import com.example.aistudymentorapplication.model.ChatSession;
import com.example.aistudymentorapplication.model.QuizResult;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper implementation for managing AI Chat history and Quiz results.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ai_study_mentor.db";
    private static final int DATABASE_VERSION = 2;

    // Table Sessions
    private static final String TABLE_SESSIONS = "chat_sessions";
    private static final String COL_SESSION_ID = "session_id";
    private static final String COL_TITLE = "title";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";

    // Table Messages
    private static final String TABLE_MESSAGES = "chat_messages";
    private static final String COL_MESSAGE_ID = "message_id";
    private static final String COL_MSG_SESSION_ID = "session_id";
    private static final String COL_SENDER = "sender";
    private static final String COL_MESSAGE = "message";
    private static final String COL_TIMESTAMP = "timestamp";

    // Table Quiz Results
    private static final String TABLE_QUIZ_RESULTS = "quiz_results";
    private static final String COL_RESULT_ID = "result_id";
    private static final String COL_QUIZ_SUBJECT = "subject";
    private static final String COL_QUIZ_LEVEL = "level";
    private static final String COL_QUIZ_SCORE = "score";
    private static final String COL_QUIZ_TOTAL = "total";
    private static final String COL_QUIZ_DURATION = "duration_sec";
    private static final String COL_QUIZ_CREATED_AT = "created_at";

    /**
     * Initializes the SQLite database.
     * This constructor sets the database name and version.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database tables when the app is first installed or data is cleared.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSessionsTable = "CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_CREATED_AT + " INTEGER, " +
                COL_UPDATED_AT + " INTEGER)";

        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COL_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MSG_SESSION_ID + " INTEGER, " +
                COL_SENDER + " TEXT, " +
                COL_MESSAGE + " TEXT, " +
                COL_TIMESTAMP + " INTEGER, " +
                "FOREIGN KEY(" + COL_MSG_SESSION_ID + ") REFERENCES " + TABLE_SESSIONS + "(" + COL_SESSION_ID + "))";

        String createQuizResultsTable = "CREATE TABLE " + TABLE_QUIZ_RESULTS + " (" +
                COL_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_QUIZ_SUBJECT + " TEXT, " +
                COL_QUIZ_LEVEL + " TEXT, " +
                COL_QUIZ_SCORE + " INTEGER, " +
                COL_QUIZ_TOTAL + " INTEGER, " +
                COL_QUIZ_DURATION + " INTEGER, " +
                COL_QUIZ_CREATED_AT + " INTEGER)";

        db.execSQL(createSessionsTable);
        db.execSQL(createMessagesTable);
        db.execSQL(createQuizResultsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createQuizResultsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_QUIZ_RESULTS + " (" +
                    COL_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_QUIZ_SUBJECT + " TEXT, " +
                    COL_QUIZ_LEVEL + " TEXT, " +
                    COL_QUIZ_SCORE + " INTEGER, " +
                    COL_QUIZ_TOTAL + " INTEGER, " +
                    COL_QUIZ_DURATION + " INTEGER, " +
                    COL_QUIZ_CREATED_AT + " INTEGER)";
            db.execSQL(createQuizResultsTable);
        }
    }

    // --- Session CRUD (Create, Read, Update, Delete) Operations ---

    /**
     * Inserts a new chat session into the database.
     * @return the row ID of the newly inserted session.
     */
    public long insertSession(ChatSession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, session.getTitle());
        values.put(COL_CREATED_AT, session.getCreatedAt());
        values.put(COL_UPDATED_AT, session.getUpdatedAt());
        return db.insert(TABLE_SESSIONS, null, values);
    }

    /**
     * Updates an existing session's title or timestamp.
     */
    public void updateSession(ChatSession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, session.getTitle());
        values.put(COL_UPDATED_AT, session.getUpdatedAt());
        db.update(TABLE_SESSIONS, values, COL_SESSION_ID + "=?", new String[]{String.valueOf(session.getSessionId())});
    }

    /**
     * Deletes a session and all messages associated with it (Cascading delete).
     */
    public void deleteSession(long sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete messages of this session
        db.delete(TABLE_MESSAGES, COL_MSG_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)});
        // Then delete the session itself
        db.delete(TABLE_SESSIONS, COL_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)});
    }

    /**
     * Fetches all chat sessions, ordered by the most recently updated.
     */
    public List<ChatSession> getAllSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, null, null, null, null, null, COL_UPDATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                ChatSession session = new ChatSession(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_SESSION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
                );
                sessions.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sessions;
    }

    public ChatSession getSessionById(long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, null, COL_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)}, null, null, null);
        
        ChatSession session = null;
        if (cursor != null && cursor.moveToFirst()) {
            session = new ChatSession(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_SESSION_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT))
            );
            cursor.close();
        }
        return session;
    }

    // --- Message CRUD (Create, Read) Operations ---

    /**
     * Saves a new chat message (either from User or AI) to the database.
     */
    public void insertMessage(ChatMessage message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MSG_SESSION_ID, message.getSessionId());
        values.put(COL_SENDER, message.getSender());
        values.put(COL_MESSAGE, message.getMessage());
        values.put(COL_TIMESTAMP, message.getTimestamp());
        db.insert(TABLE_MESSAGES, null, values);
    }

    /**
     * Retrieves all messages belonging to a specific session ID, ordered by time.
     */
    public List<ChatMessage> getMessagesBySessionId(long sessionId) {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null, COL_MSG_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)}, null, null, COL_TIMESTAMP + " ASC");

        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_MESSAGE_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_MSG_SESSION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                );
                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    // --- Quiz Result CRUD Operations ---

    /**
     * Inserts a new quiz result into the database.
     */
    public long insertQuizResult(QuizResult result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_QUIZ_SUBJECT, result.getSubject());
        values.put(COL_QUIZ_LEVEL, result.getLevel());
        values.put(COL_QUIZ_SCORE, result.getScore());
        values.put(COL_QUIZ_TOTAL, result.getTotal());
        values.put(COL_QUIZ_DURATION, result.getDurationSec());
        values.put(COL_QUIZ_CREATED_AT, result.getCreatedAt());
        return db.insert(TABLE_QUIZ_RESULTS, null, values);
    }

    /**
     * Fetches all quiz results, ordered by most recent.
     */
    public List<QuizResult> getAllQuizResults() {
        List<QuizResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_QUIZ_RESULTS, null, null, null, null, null, COL_QUIZ_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                QuizResult result = new QuizResult(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESULT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_QUIZ_SUBJECT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_QUIZ_LEVEL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUIZ_SCORE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUIZ_TOTAL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUIZ_DURATION)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_QUIZ_CREATED_AT))
                );
                results.add(result);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    /**
     * Retrieves recent questions asked by the user (sender='user') for personalization.
     */
    public List<String> getRecentUserQuestions(int limit) {
        List<String> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{COL_MESSAGE}, 
                COL_SENDER + "=?", new String[]{"user"}, 
                null, null, COL_TIMESTAMP + " DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                questions.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questions;
    }
}
