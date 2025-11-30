package com.example.aipersona.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersona.R;
import com.example.aipersona.data.local.entity.MessageEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<MessageEntity> messages = new ArrayList<>();
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageEntity message = messages.get(position);

        // 检查是否包含图片
        if (message.content.startsWith("[IMAGE]")) {
            // 图文消息
            String[] parts = message.content.split("\n", 2);
            String imageUrl = parts[0].replace("[IMAGE]", "");
            String textContent = parts.length > 1 ? parts[1] : "";

            // 显示图片
            holder.ivImage.setVisibility(View.VISIBLE);
            loadImageFromUrl(holder.ivImage, imageUrl);

            // 显示文字
            holder.tvContent.setText(textContent);
        } else {
            // 纯文本消息
            holder.ivImage.setVisibility(View.GONE);
            holder.tvContent.setText(message.content);
        }

        holder.tvTime.setText(timeFormat.format(new Date(message.timestamp)));

        // 根据消息来源设置布局
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.messageContainer.getLayoutParams();

        if (message.isFromUser) {
            // 用户消息：右对齐，蓝色背景
            params.gravity = Gravity.END;
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_user);
            holder.tvContent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            // AI消息：左对齐，灰色背景
            params.gravity = Gravity.START;
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_ai);
            holder.tvContent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        }

        holder.messageContainer.setLayoutParams(params);
    }

    /**
     * 从URL加载图片（使用系统ImageView）
     */
    private void loadImageFromUrl(ImageView imageView, String url) {
        // 简单实现：可以使用Glide或Picasso库优化
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                        .decodeStream(imageUrl.openConnection().getInputStream());

                imageView.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
                imageView.post(() -> imageView.setImageResource(R.drawable.ic_default_avatar));
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        ImageView ivImage;
        TextView tvContent;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            ivImage = itemView.findViewById(R.id.ivMessageImage);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}