package com.example.aipersona.ui.social;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.FollowEntity;
import com.example.aipersona.data.local.entity.LikeEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.local.entity.PostEntity;
import com.example.aipersona.ui.persona.PersonaProfileActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SocialSquareActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private AppDatabase database;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_square);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("社交广场");
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(database, executor);

        // 点击头像/昵称跳转到Persona主页
        adapter.setOnProfileClickListener(post -> {
            Intent intent = new Intent(this, PersonaProfileActivity.class);
            intent.putExtra("personaId", post.personaId);
            startActivity(intent);
        });

        // 点击评论按钮
        adapter.setOnCommentClickListener(post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", post.id);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // 发帖按钮
        FloatingActionButton fab = findViewById(R.id.fabCreatePost);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePostActivity.class);
            startActivity(intent);
        });

        // 观察帖子列表
        LiveData<List<PostEntity>> postsLiveData = database.postDao().getAllPosts();
        postsLiveData.observe(this, posts -> {
            if (posts != null) {
                loadAvatarsAndSetPosts(posts);
            }
        });
    }

    // 加载头像并设置帖子列表
    private void loadAvatarsAndSetPosts(List<PostEntity> posts) {
        executor.execute(() -> {
            Map<Long, String> avatarMap = new HashMap<>();

            // 批量加载所有Persona的头像
            for (PostEntity post : posts) {
                if (!avatarMap.containsKey(post.personaId)) {
                    PersonaEntity persona = database.personaDao().getPersonaById(post.personaId);
                    if (persona != null && persona.avatarUri != null) {
                        avatarMap.put(post.personaId, persona.avatarUri);
                    }
                }
            }

            runOnUiThread(() -> {
                adapter.setPosts(posts, avatarMap);
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 帖子适配器
    private static class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
        private List<PostEntity> posts = new ArrayList<>();
        private Map<Long, String> avatarMap = new HashMap<>();
        private OnProfileClickListener onProfileClickListener;
        private OnCommentClickListener onCommentClickListener;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        private AppDatabase database;
        private Executor executor;

        public PostAdapter(AppDatabase database, Executor executor) {
            this.database = database;
            this.executor = executor;
        }

        interface OnProfileClickListener {
            void onProfileClick(PostEntity post);
        }

        interface OnCommentClickListener {
            void onCommentClick(PostEntity post);
        }

        void setOnProfileClickListener(OnProfileClickListener listener) {
            this.onProfileClickListener = listener;
        }

        void setOnCommentClickListener(OnCommentClickListener listener) {
            this.onCommentClickListener = listener;
        }

        void setPosts(List<PostEntity> posts, Map<Long, String> avatarMap) {
            this.posts = posts;
            this.avatarMap = avatarMap;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PostEntity post = posts.get(position);

            // 安全设置头像
            String avatarUri = avatarMap.get(post.personaId);
            loadImageSafely(holder.ivAvatar, avatarUri);

            holder.tvPersonaName.setText(post.personaName);
            holder.tvContent.setText(post.content);
            holder.tvTime.setText(dateFormat.format(new Date(post.timestamp)));

            // 加载点赞和评论数
            loadInteractionCounts(post.id, holder);

            // 检查是否已点赞
            checkLikeStatus(post.id, holder);

            // 检查是否已关注
            checkFollowStatus(post.personaId, holder);

            // 点击头像/昵称跳转主页
            View.OnClickListener profileClickListener = v -> {
                if (onProfileClickListener != null) {
                    onProfileClickListener.onProfileClick(post);
                }
            };
            holder.ivAvatar.setOnClickListener(profileClickListener);
            holder.tvPersonaName.setOnClickListener(profileClickListener);

            // 点赞按钮
            holder.btnLike.setOnClickListener(v -> toggleLike(post.id, holder));

            // 评论按钮
            holder.btnComment.setOnClickListener(v -> {
                if (onCommentClickListener != null) {
                    onCommentClickListener.onCommentClick(post);
                }
            });

            // 关注按钮
            holder.btnFollow.setOnClickListener(v -> toggleFollow(post.personaId, holder));
        }

        private void loadInteractionCounts(long postId, ViewHolder holder) {
            executor.execute(() -> {
                int likeCount = database.likeDao().getLikeCount(postId);
                int commentCount = database.commentDao().getCommentCount(postId);

                holder.itemView.post(() -> {
                    holder.btnLike.setText("👍 " + likeCount);
                    holder.btnComment.setText("💬 " + commentCount);
                });
            });
        }

        private void checkLikeStatus(long postId, ViewHolder holder) {
            executor.execute(() -> {
                int count = database.likeDao().isLiked(postId);
                boolean isLiked = count > 0;

                holder.itemView.post(() -> {
                    if (isLiked) {
                        holder.btnLike.setTextColor(holder.itemView.getContext()
                                .getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        holder.btnLike.setTextColor(holder.itemView.getContext()
                                .getResources().getColor(android.R.color.darker_gray));
                    }
                });
            });
        }

        private void checkFollowStatus(long personaId, ViewHolder holder) {
            executor.execute(() -> {
                int count = database.followDao().isFollowing(personaId);
                boolean isFollowing = count > 0;

                holder.itemView.post(() -> {
                    if (isFollowing) {
                        holder.btnFollow.setText("已关注");
                        holder.btnFollow.setTextColor(holder.itemView.getContext()
                                .getResources().getColor(android.R.color.darker_gray));
                    } else {
                        holder.btnFollow.setText("+ 关注");
                        holder.btnFollow.setTextColor(holder.itemView.getContext()
                                .getResources().getColor(android.R.color.holo_orange_dark));
                    }
                });
            });
        }

        private void toggleLike(long postId, ViewHolder holder) {
            executor.execute(() -> {
                int count = database.likeDao().isLiked(postId);
                boolean isLiked = count > 0;

                if (isLiked) {
                    database.likeDao().unlike(postId);
                } else {
                    LikeEntity like = new LikeEntity(postId);
                    database.likeDao().like(like);
                }

                // 重新加载数据
                holder.itemView.post(() -> {
                    loadInteractionCounts(postId, holder);
                    checkLikeStatus(postId, holder);
                });
            });
        }

        private void toggleFollow(long personaId, ViewHolder holder) {
            executor.execute(() -> {
                int count = database.followDao().isFollowing(personaId);
                boolean isFollowing = count > 0;

                if (isFollowing) {
                    database.followDao().unfollow(personaId);
                } else {
                    FollowEntity follow = new FollowEntity(personaId);
                    database.followDao().follow(follow);
                }

                holder.itemView.post(() -> {
                    checkFollowStatus(personaId, holder);
                    String message = !isFollowing ? "关注成功" : "已取消关注";
                    Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                });
            });
        }

        private void loadImageSafely(ImageView imageView, String uriString) {
            if (uriString == null || uriString.isEmpty()) {
                imageView.setImageResource(R.drawable.ic_default_avatar);
                return;
            }

            try {
                Uri uri = Uri.parse(uriString);
                imageView.setImageURI(uri);

                if (imageView.getDrawable() == null) {
                    imageView.setImageResource(R.drawable.ic_default_avatar);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvPersonaName;
            TextView tvContent;
            TextView tvTime;
            Button btnLike;
            Button btnComment;
            Button btnFollow;

            ViewHolder(View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivPostAvatar);
                tvPersonaName = itemView.findViewById(R.id.tvPersonaName);
                tvContent = itemView.findViewById(R.id.tvPostContent);
                tvTime = itemView.findViewById(R.id.tvPostTime);
                btnLike = itemView.findViewById(R.id.btnLike);
                btnComment = itemView.findViewById(R.id.btnComment);
                btnFollow = itemView.findViewById(R.id.btnFollow);
            }
        }
    }
}