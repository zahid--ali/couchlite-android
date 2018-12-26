package lelab.couchdb.message;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lelab.couchdb.R;
import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.Message;
import lelab.couchdb.user.UserActivity;

public class MessageActivity extends AppCompatActivity {
    private static final int count = 10;
    private String userID;
    private MessageAdapter messageAdapter;
    private TextView tvNoData;
    //couch db
    private DatabaseManager dbMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        tvNoData = findViewById(R.id.tv_no_data);
        userID = getIntent().getStringExtra("userID");
        dbMgr = new DatabaseManager(this);

        messageAdapter = new MessageAdapter(this);
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        getMessageDbData();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMessageToDb();
                getMessageDbData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.update:
                updateAllMessages();
                getMessageDbData();
                return true;
            case R.id.delete:
                deleteAllMessages();
                //try to get from db rather than showing "no data" all together
                getMessageDbData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAllMessages() {
        List<String> idList = messageAdapter.getIds();
        for (String id : idList) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:sss", Locale.US);
            String currentDT = sdf.format(new Date());

            Message message = new Message(id, "", "", "", "", "", "", false, "", "", "", "", currentDT, currentDT, "");

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> messageMap = objectMapper1.convertValue(message, HashMap.class);
            MutableDocument doc = new MutableDocument(id, messageMap);
            doc.setString("type", DatabaseManager.MESSAGE_TABLE);
            doc.setString("userID", userID);

            //Save document to database.
            try {
                dbMgr.database.save(doc);
                //Log.d(UserActivity.TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteAllMessages() {
        List<String> idList = messageAdapter.getIds();
        for (String id : idList) {
            Document doc = dbMgr.database.getDocument(id);

            try {
                long start = System.currentTimeMillis();
                dbMgr.database.delete(doc);
                long end = System.currentTimeMillis();
                double time = (end - start);
                Log.d(UserActivity.TAG, "Deleting a message takes: " + time + "ms");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessageToDb() {
        for (int i = 0; i < count; i++) {
            String id = getRandomNo(10000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:sss", Locale.US);
            String currentDT = sdf.format(new Date());

            Message message = new Message(id, "", "", "", "", "", "", false, "", "", "", "", currentDT, currentDT, "");

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> messageMap = objectMapper1.convertValue(message, HashMap.class);
            MutableDocument doc = new MutableDocument(id, messageMap);
            doc.setString("type", DatabaseManager.MESSAGE_TABLE);
            doc.setString("userID", userID);

            //Save document to database.
            try {
                long start = System.currentTimeMillis();
                dbMgr.database.save(doc);
                long end = System.currentTimeMillis();
                double time = (end - start);
                Log.d(UserActivity.TAG, "Adding a message takes: " + time + "ms");
                //Log.d(UserActivity.TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void getMessageDbData() {
        Expression expression1 = Expression.property("type");
        Expression expression2 = Expression.property("userID");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(expression1.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)).and(expression2.equalTo(Expression.string(userID))));
        try {
            ResultSet results = query.execute();
            Result row;
            List<Message> messages = new ArrayList<>();
            while ((row = results.next()) != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Get dictionary corresponding to the database name
                Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                Message message1 = objectMapper.convertValue(valueMap.toMap(), Message.class);
                messages.add(message1);
            }
            Log.d(UserActivity.TAG, ": " + messages.size());
            if (messages.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                messageAdapter.setMessages(new ArrayList<Message>());
            } else {
                tvNoData.setVisibility(View.GONE);
                messageAdapter.setMessages(messages);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private String getRandomNo(final int max) {
        return "" + new Random().nextInt(max);
    }
}