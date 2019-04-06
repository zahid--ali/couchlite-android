package lelab.couchdb.message;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
import com.github.javafaker.Faker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lelab.couchdb.R;
import lelab.couchdb.TimeHelper;
import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.Message;
import lelab.couchdb.user.UserActivity;

public class MessageActivity extends AppCompatActivity {
    private static final int count = 5000;
    private String userID;
    private MessageAdapter messageAdapter;
    private TextView tvNoData;
    //couch db
    private DatabaseManager dbMgr;
    //Time Calculator
    private TimeHelper timeHelper;
    private ProgressBar pbMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        pbMessage = findViewById(R.id.pb_message);
        tvNoData = findViewById(R.id.tv_no_data);
        userID = getIntent().getStringExtra("userID");
        dbMgr = new DatabaseManager(this);
        timeHelper = new TimeHelper(this, dbMgr);

        messageAdapter = new MessageAdapter(this);
        RecyclerView rvMessages = findViewById(R.id.rv_messages);
        rvMessages.setAdapter(messageAdapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        getMessageDbData();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddMessageTask().execute();
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
                new UpdateMessageTask().execute();
                return true;
            case R.id.delete:
                new DeleteMessageTask().execute();
                return true;
            case R.id.insertion_time:
                timeHelper.avgInsertTimeDisplay();
                return true;
            case R.id.deletion_time:
                timeHelper.avgDeleteTimeDisplay();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //updating technique 2
//    private void updateAllMessages() {
//        List<String> idList = messageAdapter.getIds();
//        for (String id : idList) {
//            Faker faker = new Faker();
//
//            String msg = faker.shakespeare().asYouLikeItQuote();
//            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
//            String receivedAt = sdf.format(faker.date().birthday());
//            String createdAt = sdf.format(faker.date().birthday());
//
//            Message message = new Message(id, msg, "", "", "", "", "", "", false, "", "", "", "", receivedAt, createdAt, "");
//
//            ObjectMapper objectMapper1 = new ObjectMapper();
//            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            HashMap<String, Object> messageMap = objectMapper1.convertValue(message, HashMap.class);
//            MutableDocument doc = new MutableDocument(id, messageMap);
//            doc.setString("type", DatabaseManager.MESSAGE_TABLE);
//            doc.setString("userID", userID);
//
//            //Save document to database.
//            try {
//                dbMgr.database.save(doc);
//                //Log.d(UserActivity.TAG, "saved");
//            } catch (CouchbaseLiteException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void deleteAllMessages() {
        List<String> idList = messageAdapter.getIds();
        long time = 0;
        for (String id : idList) {
            Document doc = dbMgr.database.getDocument(id);

            try {
                long start = System.nanoTime();
                dbMgr.database.delete(doc);
                long end = System.nanoTime();
                time += (end - start);
                timeHelper.saveDeletionTime(time);

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }

    }

    private void addMessageToDb() {
        ArrayList<Integer> listIds = new ArrayList<Integer>();
        for (int i = 10; i <= (count + 10); i++) {
            listIds.add(i);
        }
        Collections.shuffle(listIds);
        long time = 0;
        Date todayDate = new Date();
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.YEAR, -1);
        Date oldDate = cal.getTime();
        Faker faker = new Faker();
        String id = "";
        long start, end;
        for (int i = 0; i < count; i++) {
            id = String.valueOf(listIds.get(i));
            Message messageModel = new Message();
            messageModel.setId(id);
            messageModel.setConversationId(faker.number().numberBetween(1, 35));
            //0 for plain text message
            //1 for  media message
            //2 for plain text with media message
            messageModel.setMessageType(faker.number().numberBetween(0, 3));

            //adding message to type 0 or 2
            if (messageModel.getMessageType() == 0 || messageModel.getMessageType() == 2) {
                messageModel.setMsgTxt(faker.lorem().sentence(faker.number().numberBetween(5, 1000)));
            }
            //adding media to type 1 or 2
            if (messageModel.getMessageType() == 1 || messageModel.getMessageType() == 2) {
                messageModel.setMediaMimeType(mimeType[faker.number().numberBetween(0, 7)]);
                messageModel.setMedia_name(faker.name().title());
                messageModel.setMediaSize(faker.number().numberBetween(1, 100000));
                messageModel.setMediaUrl(faker.internet().url());
            }
            messageModel.setMessageStatus(message_status[faker.number().numberBetween(0, 5)]);
            messageModel.setStarred(i % 2 == 0);
            messageModel.setCreatedAt(faker.date().between(oldDate, todayDate).toString());
            messageModel.setReceivedAt(faker.date().between(oldDate, todayDate).toString());
            messageModel.setSenderId(faker.number().numberBetween(5, 1000));

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> messageMap = objectMapper1.convertValue(messageModel, HashMap.class);
            MutableDocument doc = new MutableDocument(id, messageMap);
            doc.setString("type", DatabaseManager.MESSAGE_TABLE);
            doc.setString("userID", userID);

            //Save document to database.
            try {
                start = System.nanoTime();
                dbMgr.database.save(doc);
                end = System.nanoTime();
                time += (end - start);
                //Log.d(UserActivity.TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Adding 1000  messages takes: " + TimeUnit.NANOSECONDS.toSeconds(time) + "s");
    }

    private void getMessageDbData() {
        Faker faker = new Faker();
        Expression expression1 = Expression.property("type");
        Expression expression2 = Expression.property("conversationId");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(expression1.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)).and(expression2.equalTo(Expression.intValue(faker.number().numberBetween(1, 17)))));
        long start, end, time = 0;

        try {
            start = System.nanoTime();
            ResultSet results = query.execute();
            time += System.nanoTime() - start;
            Result row;
            List<Message> messages = new ArrayList<>();
//            while ((row = results.next()) != null) {
//                ObjectMapper objectMapper = new ObjectMapper();
//                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//                // Get dictionary corresponding to the database name
//                Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
//                Message message1 = objectMapper.convertValue(valueMap.toMap(), Message.class);
//                messages.add(message1);
//            }
            Log.d("time taken", TimeUnit.NANOSECONDS.toMillis(time) + " ms");
            pbMessage.setVisibility(View.GONE);
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

    private class AddMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addMessageToDb();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getMessageDbData();
            super.onPostExecute(aVoid);
        }
    }

    private class UpdateMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            updateAllMessages();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getMessageDbData();
            super.onPostExecute(aVoid);
        }
    }

    private class DeleteMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            deleteAllMessages();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getMessageDbData();
            super.onPostExecute(aVoid);
        }
    }


    public static String message_status[] = {
            "message successfully sent",
            "pending",
            "message successfully delivered",
            "message sending failed",
            "message read"
    };

    public static String mimeType[] = {
            "audio",
            "video",
            "image",
            "text/vcard",
            "text/plain",
            "pdf",
            "doc"
    };
}