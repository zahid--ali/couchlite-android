package lelab.couchdb.message;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
    private String userID;
    private MessageAdapter messageAdapter;
    //couch db
    private DatabaseManager dbMgr;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        userID = getIntent().getStringExtra("userID");
        dbMgr = new DatabaseManager(this);

        messageAdapter = new MessageAdapter(this);
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = getRandomNo(10000);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:sss", Locale.US);
                String currentDT = sdf.format(new Date());

                Message message = new Message(id, "", "", "", "", "", "", false, "", "", "", "", currentDT, currentDT, "");

                ObjectMapper objectMapper1 = new ObjectMapper();
                objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                HashMap<String, Object> messageMap = objectMapper1.convertValue(message, HashMap.class);
                MutableDocument doc = new MutableDocument(messageMap);
                doc.setString("key", DatabaseManager.MESSAGE_TABLE);
                doc.setString("userID", userID);

                //Save document to database.
                try {
                    dbMgr.database.save(doc);
                    Log.d(UserActivity.TAG, "saved");
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                Expression expression1 = Expression.property("key");
                Expression expression2 = Expression.property("userID");
                query = QueryBuilder.
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
                    messageAdapter.setMessages(messages);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getRandomNo(final int max) {
        return "" + new Random().nextInt(max);
    }
}