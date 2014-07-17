package com.danielesegato.adme.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Parse an SQLite script line by line from an input stream. This class is not thread safe. It can be used to parse an
 * SQL script skipping comments and dividing multi-line statements into single line statements at each call of the
 * {@link #nextStatement()} method.
 * <p/>
 * <b>Usage:</b>
 * <p/>
 * Call the static {@link #runSQLScript(android.content.Context, android.database.sqlite.SQLiteDatabase, String)}
 * method or do it manually:
 * <pre>
 * Reader scriptReader = null;
 * try {
 *   scriptReader = getScriptReader();
 *   SQLiteScriptParser parser = new SQLiteScriptParser();
 *   parser.setInputReader(scriptReader);
 *   String statement;
 *   while ((statement = parser.nextStatement()) != null) {
 *     db.execSQL(statement);
 *   }
 *   scriptReader.close();
 *   scriptReader = null;
 * } finally {
 *   if (scriptReader != null) {
 *   try {
 *     scriptReader.close();
 *   } catch (IOException e) {
 *     Log.e(LOG_TAG, String.format("An I/O exception is occurred while trying to close SQL script asset '%s'.", sqlScriptAsset), e);
 *   }
 * }
 * </pre>
 * <p/>
 * <b>Limitations:</b>
 * <p/>
 * <ul>
 * <li>Comments can't be placed in the same line with an SQL command</li>
 * <li>Each SQL statement must complete with a semi-colon (;) followed by a new line</li>
 * </ul>
 */
public class SQLiteScriptParser {

    private static final boolean DEBUG = true;

    private static final String LOG_TAG = SQLiteScriptParser.class.getSimpleName();

    private StringBuilder sb;

    private BufferedReader reader;

    /**
     * Execute all the SQL statements contained into scripts file stored into the assets folder.
     *
     * @param context        the android runtime context.
     * @param db             the SQLite database.
     * @param sqlScriptAsset the asset file name.
     * @throws SQLException if some SQL execution fails.
     */
    public static void runSQLScript(final Context context, final SQLiteDatabase db, final String sqlScriptAsset) throws SQLException {
        Reader scriptReader = null;
        try {
            scriptReader = new InputStreamReader(context.getAssets().open(sqlScriptAsset, AssetManager.ACCESS_BUFFER));
            SQLiteScriptParser parser = new SQLiteScriptParser();
            parser.setInputReader(scriptReader);
            String statement;
            while ((statement = parser.nextStatement()) != null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, String.format("Executing SQL Statement: %s", statement));
                }
                db.execSQL(statement);
            }
            scriptReader.close();
            scriptReader = null;
        } catch (IOException e) {
            throw new SQLException(String.format("Cannot read the SQL script: %s", sqlScriptAsset));
        } finally {
            if (scriptReader != null) {
                try {
                    scriptReader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, String.format("An I/O exception is occurred while trying to close SQL script asset '%s'.", sqlScriptAsset), e);
                }
            }
        }
    }

    public SQLiteScriptParser() {
        this.sb = new StringBuilder();
        this.reader = null;
    }

    /**
     * Set the input stream to the SQL script
     *
     * @param is the {@link InputStream}
     */
    public void setInputStream(final InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Set the input reader to the SQL script
     *
     * @param in the {@link Reader}
     */
    public void setInputReader(final Reader in) {
        this.reader = new BufferedReader(in);
    }

    /**
     * Parse the SQL script file returning the next statement string or null if there's no other statement. In no way
     * this method validate the SQL statement.
     *
     * @return the next statement or null if there aren't any other statements
     * @throws IllegalStateException if you didn't set an input stream / reader
     * @throws IOException           I/O exception reading the SQL Script
     * @see #setInputReader(Reader)
     * @see #setInputStream(InputStream)
     */
    public String nextStatement() throws IllegalStateException, IOException {
        if (this.reader == null) {
            throw new IllegalStateException("No Script to read from has been set");
        }
        boolean endStatement = false;
        String line;
        this.sb.setLength(0);
        while (!endStatement && (line = this.reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                // empty line, do nothing
            } else if (line.startsWith("-- ")) {
                // skip comment
                if (DEBUG) {
                    Log.d(LOG_TAG, String.format("Comment: %s", line.substring(3)));
                }
            } else {
                this.sb.append(line).append(' ');
                if (line.endsWith(";")) {
                    endStatement = true;
                }
            }
        }
        final String statement = this.sb.toString().trim();
        if (statement.length() > 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("Returning Statement: %s", statement));
            }
            return statement;
        } else {
            return null;
        }
    }
}