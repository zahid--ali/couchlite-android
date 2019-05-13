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
import com.couchbase.lite.Function;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lelab.couchdb.R;
import lelab.couchdb.TimeHelper;
import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.Message;
import lelab.couchdb.user.UserActivity;

public class MessageActivity extends AppCompatActivity {
    private static final int count = 3000;
    private String userID;
    private MessageAdapter messageAdapter;
    private TextView tvNoData;
    //couch db
    private DatabaseManager dbMgr;
    //Time Calculator
    private TimeHelper timeHelper;
    private ProgressBar pbMessage;
    Faker faker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        pbMessage = findViewById(R.id.pb_message);
        tvNoData = findViewById(R.id.tv_no_data);
        userID = getIntent().getStringExtra("userID");
        dbMgr = new DatabaseManager(this);
        timeHelper = new TimeHelper(this, dbMgr);
        faker = new Faker();
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
            case R.id.search:
                new SearchMessageTask().execute();
                return true;
            case R.id.fetch:
                new FetchMessageTask().execute();
                return true;
            case R.id.range:
                new RangeMessageTask().execute();
                return true;
            case R.id.aggregation:
                new AggregateMessageTask().execute();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //updating technique 2
    private void updateAllMessages() {
        List<String> idList = messageAdapter.getIds();
        Expression key = Expression.property("type");
        Expression idExp = Expression.property("id");
        long start, time = 0;
        int i = 0;
        Date todayDate = new Date();
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.YEAR, -1);
        Date oldDate = cal.getTime();
        Faker faker = new Faker();
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
        for (String id : idList) {
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)).and(idExp.equalTo(Expression.string(id))));
            try {
                ResultSet results = query.execute();
                Result row;
                while ((row = results.next()) != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    // Get dictionary corresponding to the database name
                    Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                    Message message = objectMapper.convertValue(valueMap.toMap(), Message.class);


                    message.setMessageType(faker.number().numberBetween(0, 3));

                    //adding message to type 0 or 2
                    if (message.getMessageType() == 0 || message.getMessageType() == 2) {
                        message.setMsgTxt(faker.lorem().sentence(faker.number().numberBetween(5, 3000)));
                    }
                    //adding media to type 1 or 2
                    if (message.getMessageType() == 1 || message.getMessageType() == 2) {
                        message.setMediaMimeType(mimeType[faker.number().numberBetween(0, 7)]);
                        message.setMedia_name(faker.name().title());
                        message.setMediaSize(faker.number().numberBetween(1, 300000));
                        message.setMediaUrl(faker.internet().url());
                    }
                    message.setMessageStatus(message_status[faker.number().numberBetween(0, 5)]);
                    message.setStarred(i++ % 2 == 0);
                    message.setReceivedAt(faker.date().between(oldDate, todayDate).toString());
                    message.setDeletedAt(faker.date().between(oldDate, todayDate).toString());

                    ObjectMapper objectMapper1 = new ObjectMapper();
                    objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    HashMap<String, Object> userMap = objectMapper1.convertValue(message, HashMap.class);
                    MutableDocument doc = new MutableDocument(id, userMap);
                    doc.setString("type", DatabaseManager.MESSAGE_TABLE);
                    doc.setString("id", id);

                    //Save document to database.
                    try {
                        start = System.nanoTime();
                        dbMgr.database.save(doc);
                        time += System.nanoTime() - start;

                        //Log.d(TAG, "saved");
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Updating 3000 messages takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    private void deleteAllMessages() {
        List<String> idList = messageAdapter.getIds();
        long time = 0;
        for (String id : idList) {
            Document doc = dbMgr.database.getDocument(id);

            try {
                long start = System.nanoTime();
                dbMgr.database.delete(doc);
                time += (System.nanoTime() - start);
                timeHelper.saveDeletionTime(time);

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Deleting 3000 messages takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");

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
        long start;
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
                messageModel.setMsgTxt(faker.lorem().sentence(faker.number().numberBetween(5, 3000)));
            }
            //adding media to type 1 or 2
            if (messageModel.getMessageType() == 1 || messageModel.getMessageType() == 2) {
                messageModel.setMediaMimeType(mimeType[faker.number().numberBetween(0, 7)]);
                messageModel.setMedia_name(faker.name().title());
                messageModel.setMediaSize(faker.number().numberBetween(1, 300000));
                messageModel.setMediaUrl(faker.internet().url());
            }
            messageModel.setMessageStatus(message_status[faker.number().numberBetween(0, 5)]);
            messageModel.setStarred(i % 2 == 0);
            messageModel.setCreatedAt(faker.date().between(oldDate, todayDate).toString());
            messageModel.setReceivedAt(faker.date().between(oldDate, todayDate).toString());
            messageModel.setSenderId(faker.number().numberBetween(5, 3000));

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
                time += (System.nanoTime() - start);
                //Log.d(UserActivity.TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Adding 3000  messages takes: " + TimeUnit.NANOSECONDS.toSeconds(time) + "s");
    }

    private void getMessageDbData() {
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)));

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

    private void searchData() {
        Expression key = Expression.property("type");
        Expression messageText = Expression.property("msgTxt");
        Query query;
        String randomName;
        long start, time = 0;
        for (int i = 0; i < 300; i++) {
            try {
                randomName = "%" + faker.lorem().word() + "%";
                Log.d("asd", randomName);
                query = QueryBuilder.
                        select(SelectResult.all()).
                        from(DataSource.database(dbMgr.database))
                        .where(Expression.property("type").equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE))
                                .and(Function.lower(Expression.property("msgTxt")).like(Function.lower(Expression.string(randomName)))));

                start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Search a message by name 4000 takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    private void selectData() {
        Expression key = Expression.property("type");
        Expression conversationId = Expression.property("conversationId");
        Faker faker = new Faker();
        long time = 0;
        for (int i = 0; i < count; i++) {
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)).and(conversationId.equalTo(Expression.intValue(faker.number().numberBetween(1, 35)))));

            try {
                long start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Fetching a message by conversation Id 3000 times takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    private List<Message> aggregationQueryMessage(List<Message> messages) {
        long start, time = 0;
        Faker faker = new Faker();
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.expression(Function.count(Expression.all())))
                .from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE))
                        .and(Expression.property("messageType").equalTo(Expression.intValue(faker.number().numberBetween(0, 3)))));
        for (int i = 0; i < count; i++) {
            try {

                start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Aggregation in 3000 messages takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
        return messages;
    }

    private List<Message> executeRangeQuery(List<Message> messages) {

        long start, time = 0;
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
        Faker fakerRange = new Faker();
        Date fromDateRange, toDateRange;

        for (int i = 0; i < count; i++) {
            fromDateRange = fakerRange.date().between(oldDateRange, todayDateRange);
            toDateRange = fakerRange.date().between(fromDateRange, todayDateRange);
            Expression key = Expression.property("type");
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.MESSAGE_TABLE)).and(Expression.property("createdAt").between(Expression.longValue(oldDateRange.getTime()), Expression.longValue(toDateRange.getTime()))));
            try {
                start = System.nanoTime();
                query.execute();
                time += System.nanoTime() - start;

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d("Message Activity", "Ranging in 3000 messages takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
        return messages;

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
            updateAllMessages();
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

    private class RangeMessageTask extends AsyncTask<Void, Void, List<Message>> {
        List<Message> messages = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {

            return executeRangeQuery(messages);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            super.onPostExecute(messages);
            pbMessage.setVisibility(View.GONE);
            if (messages.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                messageAdapter.setMessages(new ArrayList<Message>());
            } else {
                tvNoData.setVisibility(View.GONE);
                messageAdapter.setMessages(messages);
            }
        }
    }

    private class AggregateMessageTask extends AsyncTask<Void, Void, List<Message>> {
        List<Message> messages = new ArrayList<>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            return aggregationQueryMessage(messages);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            super.onPostExecute(messages);
            pbMessage.setVisibility(View.GONE);
            if (messages.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                messageAdapter.setMessages(new ArrayList<Message>());
            } else {
                tvNoData.setVisibility(View.GONE);
                messageAdapter.setMessages(messages);
            }
        }
    }

    private class SearchMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            searchData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pbMessage.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

    private class FetchMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbMessage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            selectData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pbMessage.setVisibility(View.GONE);
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