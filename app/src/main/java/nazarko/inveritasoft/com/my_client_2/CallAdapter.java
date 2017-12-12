package nazarko.inveritasoft.com.my_client_2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nazarko.inveritasoft.com.my_client_2.persistence.User;

/**
 * Created by nazarko on 28.11.17.
 */

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {

//    private List<String> mDataList;
    private  OnCallClickListener onCallClickListener;
    private List<User> users;

//    public CallAdapter(List<String> mDataList) {
//        this.mDataList = mDataList;
//    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_text, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final String id = users.get(position).getName();
        viewHolder.getNameTextView().setText(id);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCallClickListener!=null){
                    onCallClickListener.call(id);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (users == null){
            return  0;
        }
        return users.size();
    }

    public void addCallClickListener(OnCallClickListener onCallClickListener) {
        this.onCallClickListener = onCallClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public List<User> getUsers() {
        return users;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }

        public TextView getNameTextView() {
            return name;
        }
    }


}
