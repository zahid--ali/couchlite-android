package lelab.couchdb.db;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;

import java.io.File;

public class DatabaseManager {
    public static final String USER_TABLE = "user";
    public static final String MESSAGE_TABLE = "message";
    public static final String INSERT_TIME_TABLE = "insertTime";
    public static final String DELETE_TIME_TABLE = "deleteTime";

    private final static String DATABASE_NAME = "myCouchDb";
    public Database database;

    public DatabaseManager(Context context) {

        // Set database configuration
        try {

            // Set Database configuration
            DatabaseConfiguration config = new DatabaseConfiguration(context);
            File dir = context.getDir("CBL", Context.MODE_PRIVATE);
            config.setDirectory(dir.toString());

            // Create / Open a database with specified name and configuration
            database = new Database(DATABASE_NAME, config);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}

