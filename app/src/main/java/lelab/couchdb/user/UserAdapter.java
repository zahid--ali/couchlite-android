package lelab.couchdb.user;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import lelab.couchdb.R;
import lelab.couchdb.message.MessageActivity;
import lelab.couchdb.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> users;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView phone;
        public View llUserItem;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            llUserItem = itemView.findViewById(R.id.ll_user_item);
        }
    }


    public UserAdapter(Context context) {
        mContext = context;
        users = Collections.emptyList();
    }

    private Context getContext() {
        return mContext;
    }

//    public void addUser(User user) {
//        users.add(user);
//    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        holder.name.setText(user.getName());
        holder.phone.setText(user.getPhoneNumber());
        holder.llUserItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userID", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
