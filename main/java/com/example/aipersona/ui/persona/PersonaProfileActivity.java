package com.example.aipersona.ui.persona;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.FollowEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.ui.chat.ChatActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PersonaProfileActivity extends AppCompatActivity {
    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvPersonality;
    private TextView tvBackgroundStory;
    private Button btnChat;
    private Button btnFollow;

    private AppDatabase database;
    private Executor executor;
    private long personaId;
    private PersonaEntity persona;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persona_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Persona主页");
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        personaId = getIntent().getLongExtra("personaId", -1);
        if (personaId == -1) {
            Toast.makeText(this, "Persona不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        ivAvatar = findViewById(R.id.ivProfileAvatar);
        tvName = findViewById(R.id.tvProfileName);
        tvPersonality = findViewById(R.id.tvProfilePersonality);
        tvBackgroundStory = findViewById(R.id.tvProfileBackgroundStory);
        btnChat = findViewById(R.id.btnChatWithPersona);
        btnFollow = findViewById(R.id.btnFollowPersona);

        // 加载Persona信息
        loadPersona();

        // 检查关注状态
        checkFollowStatus();

        // 聊天按钮点击事件
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("personaId", personaId);
            startActivity(intent);
        });

        // 关注按钮点击事件
        btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void loadPersona() {
        executor.execute(() -> {
            persona = database.personaDao().getPersonaById(personaId);

            if (persona != null) {
                runOnUiThread(() -> {
                    // 安全设置头像
                    loadImageSafely(ivAvatar, persona.avatarUri);

                    // 设置信息
                    tvName.setText(persona.name);

                    if (persona.personality != null && !persona.personality.isEmpty()) {
                        tvPersonality.setText("性格：" + persona.personality);
                    } else {
                        tvPersonality.setText("性格：暂无描述");
                    }

                    if (persona.backgroundStory != null && !persona.backgroundStory.isEmpty()) {
                        tvBackgroundStory.setText(persona.backgroundStory);
                    } else {
                        tvBackgroundStory.setText("还没有背景故事...");
                    }

                    // 更新标题
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(persona.name + " 的主页");
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void checkFollowStatus() {
        executor.execute(() -> {
            int count = database.followDao().isFollowing(personaId);
            isFollowing = count > 0;

            runOnUiThread(() -> updateFollowButton());
        });
    }

    private void toggleFollow() {
        executor.execute(() -> {
            if (isFollowing) {
                // 取消关注
                database.followDao().unfollow(personaId);
                isFollowing = false;
            } else {
                // 关注
                FollowEntity follow = new FollowEntity(personaId);
                database.followDao().follow(follow);
                isFollowing = true;
            }

            runOnUiThread(() -> {
                updateFollowButton();
                String message = isFollowing ? "关注成功" : "已取消关注";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("已关注");
            btnFollow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            btnFollow.setText("+ 关注");
            btnFollow.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * 安全加载图片，处理权限异常
     */
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}