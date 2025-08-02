package com.duong_21011224.btl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private Context context;
    private List<Account> accountList;
    private String currentAdminUsername;

    public interface OnAccountActionListener {
        void onToggleRole(Account account);
        void onDelete(Account account);
    }

    private OnAccountActionListener listener;

    public AccountAdapter(Context context, List<Account> accountList, String currentAdminUsername, OnAccountActionListener listener) {
        this.context = context;
        this.accountList = accountList;
        this.currentAdminUsername = currentAdminUsername;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);

        holder.tvUsername.setText("Tên đăng nhập: " + account.getUsername());
        holder.tvPassword.setText("Mật khẩu: " + account.getPassword());
        holder.tvRole.setText("Quyền: " + account.getRole());

        holder.btnToggleRole.setText(account.getRole().equals("admin") ? "Hạ quyền" : "Cấp quyền");

        // Không cho phép admin gốc xóa chính mình
        boolean isAdminRoot = "admin".equals(currentAdminUsername);

        boolean isOtherAdmin = account.getRole().equals("admin") && !account.getUsername().equals("admin");

        holder.btnDelete.setEnabled(isAdminRoot || !account.getRole().equals("admin"));

        holder.btnToggleRole.setOnClickListener(v -> {
            if (account.getUsername() != null && !"admin".equals(account.getUsername())) {
                listener.onToggleRole(account);
            } else {
                Toast.makeText(context, "Không thể thay đổi quyền admin gốc", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (!account.getUsername().equals("admin")) {
                listener.onDelete(account);
            } else {
                Toast.makeText(context, "Không thể xóa tài khoản admin gốc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvPassword, tvRole;
        Button btnToggleRole, btnDelete;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnToggleRole = itemView.findViewById(R.id.btnToggleRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
