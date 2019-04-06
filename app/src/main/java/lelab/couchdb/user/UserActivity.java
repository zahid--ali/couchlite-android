package lelab.couchdb.user;

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
import android.widget.Toast;

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
import lelab.couchdb.model.User;

public class UserActivity extends AppCompatActivity {
    private static final int count = 10;
    public static final String TAG = "CouchDbApp";
    private UserAdapter userAdapter;
    private TextView tvNoData;
    //couch db
    private DatabaseManager dbMgr;
    //Time Calculator
    private TimeHelper timeHelper;

    private ProgressBar pbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        pbUser = findViewById(R.id.pb_user);
        tvNoData = findViewById(R.id.tv_no_data);
        dbMgr = new DatabaseManager(this);
        timeHelper = new TimeHelper(this, dbMgr);

        userAdapter = new UserAdapter(this);
        RecyclerView rvUsers = findViewById(R.id.rv_users);
        rvUsers.setAdapter(userAdapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        getUserDbData();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                pbUser.setVisibility(View.VISIBLE);
//                addUserToDb();
//                getUserDbData();
                new AddUserTask().execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.select:
                selectData();
                return true;
            case R.id.update:
                new UpdateUserTask().execute();
                return true;
            case R.id.delete:
                new DeleteUserTask().execute();
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

    private void selectData() {
        String anId = userAdapter.getAnId();
        Expression key = Expression.property("type");
        Expression id = Expression.property("id");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE)).and(id.equalTo(Expression.string(anId))));
        try {
            long start2 = System.nanoTime();
            query.execute();
            long end2 = System.nanoTime();
            double time2 = (end2 - start2);
            Log.d(TAG, "Select * where id: " + anId + " takes: " + time2 + "ms");
            Toast.makeText(this, "Select * where id: " + anId + " takes: " + time2 + "ms", Toast.LENGTH_SHORT).show();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    //updating technique 1
    private void updateAllUsers() {
        List<String> idList = userAdapter.getIds();
        Expression key = Expression.property("type");
        Expression idExp = Expression.property("id");

        for (String id : idList) {
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE)).and(idExp.equalTo(Expression.string(id))));
            try {
                ResultSet results = query.execute();
                Result row;
                while ((row = results.next()) != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    // Get dictionary corresponding to the database name
                    Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                    User user1 = objectMapper.convertValue(valueMap.toMap(), User.class);

                    Faker faker = new Faker();
                    String name = faker.name().fullName();
                    Log.d(TAG, "id " + id + " name " + name);

                    user1.setName(name);
                    user1.setImageUrl("");
                    user1.setStatus("");
                    user1.setActive(true);
                    user1.setReported(false);
                    user1.setBlocked(false);
                    user1.setCreatedAt("");
                    user1.setUpdatedAt("");

                    ObjectMapper objectMapper1 = new ObjectMapper();
                    objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    HashMap<String, Object> userMap = objectMapper1.convertValue(user1, HashMap.class);
                    MutableDocument doc = new MutableDocument(id, userMap);
                    doc.setString("type", DatabaseManager.USER_TABLE);
                    doc.setString("id", id);

                    //Save document to database.
                    try {
                        dbMgr.database.save(doc);
                        //Log.d(TAG, "saved");
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteAllUsers() {
        List<String> idList = userAdapter.getIds();
        long time = 0;
        for (String id : idList) {
            Document doc = dbMgr.database.getDocument(id);

            try {
                long start2 = System.nanoTime();
                dbMgr.database.delete(doc);
                long end2 = System.nanoTime();
                time += (end2- start2);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Deleting 2000 users takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    private void addUserToDb() {
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
        ArrayList<Integer> listIds = new ArrayList<Integer>();
        for (int i = 1; i <= count; i++) {
            listIds.add(i);
        }
        Collections.shuffle(listIds);
        long time = 0;
        for (int i = 0; i < count; i++) {
            Faker faker = new Faker();
            String id = String.valueOf(listIds.get(i));
            String phNo = faker.phoneNumber().cellPhone();
            String name = faker.name().fullName();

            User user = new User(id, name, phNo, faker.internet().image(), faker.shakespeare().romeoAndJulietQuote(), isPrime(i), false, isPrime(i), faker.date().between(oldDateRange, todayDateRange).toString(), "");

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> userMap = objectMapper1.convertValue(user, HashMap.class);
            MutableDocument doc = new MutableDocument(id, userMap);
            doc.setString("type", DatabaseManager.USER_TABLE);
            doc.setString("id", id);

            //Save document to database.
            try {
                long start = System.nanoTime();
                dbMgr.database.save(doc);
                long end = System.nanoTime();
                time += (end - start);

                //Log.d(TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Adding a data takes: " + TimeUnit.NANOSECONDS.toSeconds(time) + " s");
    }

    private void getUserDbData() {
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.all()).
                from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE)));
        try {
            ResultSet results = query.execute();
            Result row;
            List<User> users = new ArrayList<>();
            while ((row = results.next()) != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // Get dictionary corresponding to the database name
                Dictionary valueMap = row.getDictionary(dbMgr.database.getName());
                User user1 = objectMapper.convertValue(valueMap.toMap(), User.class);
                users.add(user1);
            }
            Log.d(TAG, ": " + users.size());
            pbUser.setVisibility(View.GONE);
            if (users.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                userAdapter.setUsers(new ArrayList<User>());
            } else {
                tvNoData.setVisibility(View.GONE);
                userAdapter.setUsers(users);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private class AddUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addUserToDb();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getUserDbData();
            super.onPostExecute(aVoid);
        }
    }

    private class UpdateUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            updateAllUsers();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getUserDbData();
            super.onPostExecute(aVoid);
        }
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            deleteAllUsers();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getUserDbData();
            super.onPostExecute(aVoid);
        }
    }

    public static boolean isPrime(int num) {
        if (num < 2) return false;
        if (num == 2) return true;
        if (num % 2 == 0) return false;
        for (int i = 3; i * i <= num; i += 2)
            if (num % i == 0) return false;
        return true;
    }

}
