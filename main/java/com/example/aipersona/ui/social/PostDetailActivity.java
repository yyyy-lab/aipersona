package com.example.aipersona.ui.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.CommentEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.local.entity.PostEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostDetailActivity extends AppCompatActivity {
    private TextView tvPostContent;
    private RecyclerView recyclerViewComments;
    private Spinner spinnerPersona;
    private EditText etComment;
    private Button btnSendComment;

    private AppDatabase database;
    private Executor executor;
    private CommentAdapter adapter;

    private long postId;
    private List<PersonaEntity> personas = new ArrayList<>();
    private PersonaEntity selectedPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("帖子详情");
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        postId = getIntent().getLongExtra("postId", -1);
        if (postId == -1) {
            Toast.makeText(this, "帖子不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvPostContent = findViewById(R.id.tvPostContent);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        spinnerPersona = findViewById(R.id.spinnerCommentPersona);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);

        // 设置评论列表
        adapter = new CommentAdapter();
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(adapter);

        // 加载帖子内容
        loadPost();

        // 加载Persona列表
        loadPersonas();

        // 观察评论列表
        LiveData<List<CommentEntity>> commentsLiveData = database.commentDao().getCommentsByPostId(postId);
        commentsLiveData.observe(this, comments -> {
            adapter.setComments(comments);
        });

        // 发送评论
        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadPost() {
        executor.execute(() -> {
            PostEntity post = database.postDao().getPostById(postId);
            if (post != null) {
                runOnUiThread(() -> {
                    tvPostContent.setText(post.content);
                });
            }
        });
    }

    private void loadPersonas() {
        database.personaDao().getAllPersonas().observe(this, personas -> {
            this.personas = personas;

            if (personas == null || personas.isEmpty()) {
                Toast.makeText(this, "请先创建一个Persona", Toast.LENGTH_SHORT).show();
                return;
            }

            // 设置Spinner
            List<String> personaNames = new ArrayList<>();
            for (PersonaEntity persona : personas) {
                personaNames.add(persona.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    personaNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPersona.setAdapter(adapter);

            spinnerPersona.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedPersona = personas.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedPersona = null;
                }
            });

            // 默认选择第一个
            if (!personas.isEmpty()) {
                selectedPersona = personas.get(0);
            }
        });
    }

    private void sendComment() {
        String content = etComment.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPersona == null) {
            Toast.makeText(this, "请选择Persona", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                CommentEntity comment = new CommentEntity(
                        postId,
                        selectedPersona.id,
                        selectedPersona.name,
                        content
                );

                database.commentDao().insertComment(comment);

                runOnUiThread(() -> {
                    etComment.setText("");
                    Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "评论失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
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

    // 评论适配器
    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
        private List<CommentEntity> comments = new ArrayList<>();
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        void setComments(List<CommentEntity> comments) {
            this.comments = comments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CommentEntity comment = comments.get(position);

            holder.tvCommenterName.setText(comment.commenterPersonaName);
            holder.tvCommentContent.setText(comment.content);
            holder.tvCommentTime.setText(dateFormat.format(new Date(comment.timestamp)));
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCommenterName;
            TextView tvCommentContent;
            TextView tvCommentTime;

            ViewHolder(View itemView) {
                super(itemView);
                tvCommenterName = itemView.findViewById(R.id.tvCommenterName);
                tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
                tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            }
        }
    }
}