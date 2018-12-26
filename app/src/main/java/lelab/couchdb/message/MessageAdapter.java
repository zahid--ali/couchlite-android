package lelab.couchdb.message;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lelab.couchdb.R;
import lelab.couchdb.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView id;
        public TextView received;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            received = itemView.findViewById(R.id.received);
        }
    }


    public MessageAdapter(Context context) {
        mContext = context;
        messages = Collections.emptyList();
    }

    private Context getContext() {
        return mContext;
    }

    public List<String> getIds() {
        List<String> idList = new ArrayList<>();
        for (Message message : messages) {
            idList.add(message.getId());
        }
        return idList;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.id.setText(message.getId());
        holder.received.setText(message.getReceivedAt());

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

}

