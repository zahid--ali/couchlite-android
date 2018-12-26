package lelab.couchdb.user;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lelab.couchdb.R;
import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.User;

public class UserActivity extends AppCompatActivity {
    private static final int count = 10;
    public static final String TAG = "CouchDbApp";
    private UserAdapter userAdapter;
    private TextView tvNoData;
    //couch db
    private DatabaseManager dbMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        tvNoData = findViewById(R.id.tv_no_data);
        dbMgr = new DatabaseManager(this);

        userAdapter = new UserAdapter(this);
        RecyclerView rvUsers = findViewById(R.id.rv_users);
        rvUsers.setAdapter(userAdapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        getUserDbData();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserToDb();
                getUserDbData();
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
                updateAllUsers();
                getUserDbData();
                return true;
            case R.id.delete:
                deleteAllUsers();
                //try to get from db rather than showing "no data" all together
                getUserDbData();
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
            long start2 = System.currentTimeMillis();
            query.execute();
            long end2 = System.currentTimeMillis();
            double time2 = (end2 - start2);
            Log.d(TAG, "Select * where id: " + anId + " takes: " + time2 + "ms");
            Toast.makeText(this, "Select * where id: " + anId + " takes: " + time2 + "ms", Toast.LENGTH_SHORT).show();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void updateAllUsers() {
        List<String> idList = userAdapter.getIds();
        for (String id : idList) {
            String phNo = getRandomNo(90000);
            User user = new User(id, "Nabil Updated " + id, phNo, "", "", true, false, false, "", "");

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> userMap = objectMapper1.convertValue(user, HashMap.class);
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
    }

    private void deleteAllUsers() {
        List<String> idList = userAdapter.getIds();
        for (String id : idList) {
            Document doc = dbMgr.database.getDocument(id);

            try {
                long start2 = System.currentTimeMillis();
                dbMgr.database.delete(doc);
                long end2 = System.currentTimeMillis();
                double time2 = (end2 - start2);
                Log.d(TAG, "Deleting a user takes: " + time2 + "ms");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUserToDb() {
        for (int i = 0; i < count; i++) {
            String id = getRandomNo(10000);
            String phNo = getRandomNo(90000);
            User user = new User(id, "Nabil " + id, phNo, "", "", true, false, false, "", "");

            ObjectMapper objectMapper1 = new ObjectMapper();
            objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            HashMap<String, Object> userMap = objectMapper1.convertValue(user, HashMap.class);
            MutableDocument doc = new MutableDocument(id, userMap);
            doc.setString("type", DatabaseManager.USER_TABLE);
            doc.setString("id", id);

            //Save document to database.
            try {
                long start = System.currentTimeMillis();
                dbMgr.database.save(doc);
                long end = System.currentTimeMillis();
                double time = (end - start);
                Log.d(TAG, "Adding a data takes: " + time + "ms");
                //Log.d(TAG, "saved");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
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

    private String getRandomNo(final int max) {
        return "" + new Random().nextInt(max);
    }
}
