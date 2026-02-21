package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private final UserActionsListener listener;

    public interface UserActionsListener {
        void onViewDetails(User user);
        void onUpdateStatus(User user, String newStatus);
    }

    public UserAdapter(List<User> userList, UserActionsListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.userList = newUsers;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvEmail, tvStatus;
        ImageView ivMenu;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvStatus = itemView.findViewById(R.id.tv_user_status);
            ivMenu = itemView.findViewById(R.id.iv_user_menu);
        }

        void bind(final User user) {
            tvInitials.setText(user.getInitials());
            tvName.setText(user.fullname);
            tvEmail.setText(user.email);

            if (user.isActive()) {
                tvStatus.setText("Active");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.teal_700)); // Use your active color
            } else {
                tvStatus.setText("Blocked");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red)); // Use your blocked color
            }

            itemView.setOnClickListener(v -> listener.onViewDetails(user));

            ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(itemView.getContext(), ivMenu);
                popup.getMenuInflater().inflate(R.menu.menu_user_options, popup.getMenu());

                // Dynamically set the title for block/unblock
                if (user.isActive()) {
                    popup.getMenu().findItem(R.id.action_toggle_status).setTitle("Block User");
                } else {
                    popup.getMenu().findItem(R.id.action_toggle_status).setTitle("Unblock User");
                }

                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_view_details) {
                        listener.onViewDetails(user);
                        return true;
                    } else if (id == R.id.action_toggle_status) {
                        String newStatus = user.isActive() ? "blocked" : "active";
                        listener.onUpdateStatus(user, newStatus);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}