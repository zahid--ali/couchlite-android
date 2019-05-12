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
import lelab.couchdb.model.User;

public class UserActivity extends AppCompatActivity {
    private static final int count = 4000;
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
            case R.id.fetch:
                new FetchUserTask().execute();
                return true;
            case R.id.search:
                new SearchUserTask().execute();
                return true;
            case R.id.update:
                new UpdateUserTask().execute();
                return true;
            case R.id.delete:
                new DeleteUserTask().execute();
                return true;
            case R.id.range:
                new RangeUserTask().execute();
                return true;
            case R.id.aggregation:
                new AggregateTask().execute();
                return true;
            case R.id.join:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectData() {
        Expression key = Expression.property("type");
        Expression id = Expression.property("id");
        Faker faker = new Faker();
        long time = 0;
        for (int i = 0; i < count; i++) {
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE)).and(id.equalTo(Expression.string(faker.random().nextInt(0, 999).toString()))));

            try {
                long start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Fetching a users by id 4000 times takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    private void searchData() {
        Expression key = Expression.property("type");
        Expression name = Expression.property("name");
        Faker faker = new Faker();
        long time = 0;
        for (int i = 0; i < count; i++) {
            String randomName = "%" + faker.name().firstName() + "%";
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE))
                            .and(Function.lower(name).like(Function.lower(Expression.string(randomName)))));
            try {
                long start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Search a users by name 4000 takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
    }

    //updating technique 1
    private void updateAllUsers() {
        List<String> idList = userAdapter.getIds();
        Expression key = Expression.property("type");
        Expression idExp = Expression.property("id");
        long start, time = 0;
        Faker faker = new Faker();
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
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


                    user1.setImageUrl(faker.internet().image());
                    user1.setStatus(faker.shakespeare().romeoAndJulietQuote());
                    user1.setActive(faker.number().numberBetween(1, 40000) % 2 == 0);
                    user1.setReported(false);
                    user1.setBlocked(false);
                    user1.setUpdatedAt(faker.date().between(oldDateRange, todayDateRange));

                    ObjectMapper objectMapper1 = new ObjectMapper();
                    objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    HashMap<String, Object> userMap = objectMapper1.convertValue(user1, HashMap.class);
                    MutableDocument doc = new MutableDocument(id, userMap);
                    doc.setString("type", DatabaseManager.USER_TABLE);
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
        Log.d(UserActivity.TAG, "Updating 4000 users takes: " + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
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
                time += (end2 - start2);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Deleting 4000 users takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
    }

    private void addUserToDb() {
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
        Faker faker = new Faker();
        ArrayList<Integer> listIds = new ArrayList<Integer>();
        for (int i = 1; i <= count; i++) {
            listIds.add(i);
        }
        Collections.shuffle(listIds);
        long time = 0;
        for (int i = 0; i < count; i++) {


            User user = new User(String.valueOf(listIds.get(i)), faker.name().fullName(), faker.phoneNumber().cellPhone(), faker.internet().image(), faker.shakespeare().romeoAndJulietQuote(), isPrime(i), false, isPrime(i), faker.date().between(oldDateRange, todayDateRange), null);

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> userMap = objectMapper1.convertValue(user, HashMap.class);
            MutableDocument doc = new MutableDocument(String.valueOf(listIds.get(i)), userMap);
            doc.setString("type", DatabaseManager.USER_TABLE);
            doc.setString("id", String.valueOf(listIds.get(i)));

            //Save document to database.
            try {
                long start = System.nanoTime();
                dbMgr.database.save(doc);
                long end = System.nanoTime();
                time += System.nanoTime() - start;

                //Log.d(TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Adding 4000 users takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
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

    private List<User> aggregationQueryUser(List<User> users) {
        long start, time = 0;
        Expression key = Expression.property("type");
        Query query = QueryBuilder.
                select(SelectResult.expression(Function.count(Expression.all())))
                .from(DataSource.database(dbMgr.database))
                .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE))
                        .and(Expression.property("active").equalTo(Expression.booleanValue(true))));
        for (int i = 0; i < count; i++) {
            try {
                start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Aggregate in 4000 users takes: " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
        return users;
    }

    private List<User> executeRangeQuery(List<User> users) {

        long start, time = 0;
        Calendar calRange = new GregorianCalendar();
        Date todayDateRange = calRange.getTime();
        calRange.add(Calendar.YEAR, -1);
        Date oldDateRange = calRange.getTime();
        Faker fakerRange = new Faker();
        Date fromDateRange, toDateRange;
        Expression key = Expression.property("type");
        Expression key1 = Expression.property("createdAt");
        for (int i = 0; i < count; i++) {
        fromDateRange = fakerRange.date().between(oldDateRange, todayDateRange);
        toDateRange = fakerRange.date().between(fromDateRange, todayDateRange);
            Query query = QueryBuilder.
                    select(SelectResult.all()).
                    from(DataSource.database(dbMgr.database))
                    .where(key.equalTo(Expression.string(DatabaseManager.USER_TABLE)).and(Expression.property("createdAt").between(Expression.longValue(oldDateRange.getTime()), Expression.longValue(toDateRange.getTime()))));

            try {
                start = System.nanoTime();
                query.execute();
                time += (System.nanoTime() - start);

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
        Log.d(UserActivity.TAG, "Ranging in 4000 users takes: " + TimeUnit.NANOSECONDS.toSeconds(time) + " s");
        return users;

    }

    private void joinQuery() {
        //TODO join Query
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

    private class FetchUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            selectData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pbUser.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

    private class SearchUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            searchData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pbUser.setVisibility(View.GONE);
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

    private class RangeUserTask extends AsyncTask<Void, Void, List<User>> {
        List<User> users = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(Void... voids) {

            return executeRangeQuery(users);
        }

        @Override
        protected void onPostExecute(List<User> users) {
            super.onPostExecute(users);
            pbUser.setVisibility(View.GONE);
            if (users.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                userAdapter.setUsers(new ArrayList<User>());
            } else {
                tvNoData.setVisibility(View.GONE);
                userAdapter.setUsers(users);
            }
        }
    }

    private class AggregateTask extends AsyncTask<Void, Void, List<User>> {
        List<User> users = new ArrayList<>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbUser.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            return aggregationQueryUser(users);
        }

        @Override
        protected void onPostExecute(List<User> users) {
            super.onPostExecute(users);
            pbUser.setVisibility(View.GONE);
            if (users.size() == 0) {
                tvNoData.setVisibility(View.VISIBLE);
                userAdapter.setUsers(new ArrayList<User>());
            } else {
                tvNoData.setVisibility(View.GONE);
                userAdapter.setUsers(users);
            }
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
