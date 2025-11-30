package com.example.aipersona.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.MessageEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.remote.api.GLMApiService;
import com.example.aipersona.data.remote.api.ImageGenApiService;
import com.example.aipersona.data.repository.ChatRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView recyclerView;
    private EditText etInput;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    private MessageAdapter adapter;
    private ChatRepository repository;
    private long personaId;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 安全地设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("加载中...");
        }

        executor = Executors.newSingleThreadExecutor();

        personaId = getIntent().getLongExtra("personaId", -1);
        if (personaId == -1) {
            Toast.makeText(this, "Persona不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        recyclerView = findViewById(R.id.recyclerViewMessages);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        // 设置RecyclerView
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 初始化Repository
        AppDatabase database = AppDatabase.getInstance(this);

        // 加载Persona信息并设置标题
        loadPersonaInfo(database);

        // API Key（替换为你的智谱AI Key）
        String apiKey = "01978bd504464759aa01907709549ffb.xKgM3uLaJPRb4oYA";
        GLMApiService textApiService = GLMApiService.create(apiKey);
        ImageGenApiService imageApiService = ImageGenApiService.create(apiKey);  // 新增

        repository = new ChatRepository(
                database.personaDao(),
                database.messageDao(),
                textApiService,
                imageApiService  // 新增
        );

        // 观察消息列表
        LiveData<List<MessageEntity>> messagesLiveData = repository.getMessages(personaId);
        messagesLiveData.observe(this, messages -> {
            adapter.setMessages(messages);
            if (!messages.isEmpty()) {
                // 自动滚动到最新消息
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> sendMessage());

        // 输入框回车发送（可选）
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    /**
     * 加载Persona信息并设置标题
     */
    private void loadPersonaInfo(AppDatabase database) {
        executor.execute(() -> {
            try {
                PersonaEntity persona = database.personaDao().getPersonaById(personaId);
                if (persona != null) {
                    runOnUiThread(() -> {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(persona.name);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载Persona失败", e);
            }
        });
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // 禁用输入
        etInput.setEnabled(false);
        btnSend.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        etInput.setText("");

        repository.sendMessage(personaId, content, new ChatRepository.SendMessageCallback() {
            @Override
            public void onSuccess(MessageEntity message) {
                // Repository已经通过Handler切换到主线程
                runOnUiThread(() -> {
                    etInput.setEnabled(true);
                    btnSend.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    etInput.setEnabled(true);
                    btnSend.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                });
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
}