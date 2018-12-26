package lelab.couchdb.user;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lelab.couchdb.R;
import lelab.couchdb.db.DatabaseManager;
import lelab.couchdb.model.User;

public class UserActivity extends AppCompatActivity {
    public static final String TAG = "CouchDbApp";
    private UserAdapter userAdapter;
    //couch db
    private DatabaseManager dbMgr;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        dbMgr = new DatabaseManager(this);

        userAdapter = new UserAdapter(this);
        RecyclerView rvUsers = findViewById(R.id.rv_users);
        rvUsers.setAdapter(userAdapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = getRandomNo(10000);
                String phNo = getRandomNo(90000);
                User user = new User(id, "Nabil " + id, phNo, "", "", true, false, false, "", "");

                ObjectMapper objectMapper1 = new ObjectMapper();
                objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                HashMap<String, Object> userMap = objectMapper1.convertValue(user, HashMap.class);
                MutableDocument doc = new MutableDocument(userMap);
                doc.setString("key", DatabaseManager.USER_TABLE);

                //Save document to database.
                try {
                    dbMgr.database.save(doc);
                    Log.d(TAG, "saved");
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                Expression key = Expression.property("key");
                query = QueryBuilder.
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
                    userAdapter.setUsers(users);
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
