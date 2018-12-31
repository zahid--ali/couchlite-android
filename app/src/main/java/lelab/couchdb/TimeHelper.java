package lelab.couchdb;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.DeleteTimer;
import lelab.couchdb.model.InsertTimer;

import static lelab.couchdb.user.UserActivity.TAG;

public class TimeHelper {
    private Context context;
    private DatabaseManager dbMgr;

    public TimeHelper(Context context, DatabaseManager dbMgr) {
        this.context = context;
        this.dbMgr = dbMgr;
    }

    public void saveInsertionTime(double insertTime) {
        InsertTimer insertTimer = new InsertTimer(insertTime);

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HashMap<String, Object> timerMap = objectMapper1.convertValue(insertTimer, HashMap.class);
        MutableDocument doc = new MutableDocument(timerMap);
        doc.setString("type", DatabaseManager.INSERT_TIME_TABLE);

        //Save document to database.
        try {
            dbMgr.database.save(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void saveDeletionTime(double deleteTime) {
        DeleteTimer deleteTimer = new DeleteTimer(deleteTime);

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HashMap<String, Object> timerMap = objectMapper1.convertValue(deleteTimer, HashMap.class);
        MutableDocument doc = new MutableDocument(timerMap);
        doc.setString("type", DatabaseManager.DELETE_TIME_TABLE);

        //Save document to database.
        try {
            dbMgr.database.save(doc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void avgInsertTimeDisplay() {
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.INSERT_TIME_TABLE)));
        try {
            ResultSet results = query.execute();
            Result row;
            List<InsertTimer> insertTimers = new ArrayList<>();
            while ((row = results.next()) != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Get dictionary corresponding to the database name
                Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                InsertTimer insertTimer = objectMapper.convertValue(valueMap.toMap(), InsertTimer.class);
                insertTimers.add(insertTimer);
            }
            Log.d(TAG, "size: " + insertTimers.size());
            if (insertTimers.size() == 0) {
                Toast.makeText(context, "Avg time is -nil-", Toast.LENGTH_SHORT).show();
            } else {
                double totalTime = 0;
                for (int i = 0; i < insertTimers.size(); i++) {
                    double timeVal = insertTimers.get(i).getTime();
                    Log.d(TAG, i + ": " + timeVal);
                    totalTime = totalTime + timeVal;
                }
                double avg = totalTime / insertTimers.size();
                Toast.makeText(context, "Avg insertion time is: " + avg + "ms", Toast.LENGTH_SHORT).show();
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void avgDeleteTimeDisplay() {
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.DELETE_TIME_TABLE)));
        try {
            ResultSet results = query.execute();
            Result row;
            List<DeleteTimer> deleteTimers = new ArrayList<>();
            while ((row = results.next()) != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Get dictionary corresponding to the database name
                Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                DeleteTimer deleteTimer = objectMapper.convertValue(valueMap.toMap(), DeleteTimer.class);
                deleteTimers.add(deleteTimer);
            }
            Log.d(TAG, "size: " + deleteTimers.size());
            if (deleteTimers.size() == 0) {
                Toast.makeText(context, "Avg time is -nil-", Toast.LENGTH_SHORT).show();
            } else {
                double totalTime = 0;
                for (int i = 0; i < deleteTimers.size(); i++) {
                    double timeVal = deleteTimers.get(i).getTime();
                    Log.d(TAG, i + ": " + timeVal);
                    totalTime = totalTime + timeVal;
                }
                double avg = totalTime / deleteTimers.size();
                Toast.makeText(context, "Avg deletion time is: " + avg + "ms", Toast.LENGTH_SHORT).show();
            }

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
