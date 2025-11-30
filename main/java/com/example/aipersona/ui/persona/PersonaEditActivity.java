package com.example.aipersona.ui.persona;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.PersonaEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PersonaEditActivity extends AppCompatActivity {
    private static final String TAG = "PersonaEditActivity";

    private ImageView ivAvatar;
    private EditText etName;
    private EditText etPersonality;
    private EditText etBackgroundStory;
    private EditText etSystemPrompt;

    private AppDatabase database;
    private Executor executor;
    private PersonaEntity currentPersona;
    private long personaId = -1;

    private String selectedAvatarUri;

    // 图片选择器
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Log.d(TAG, "选择的图片URI: " + imageUri);

                        // 🔑 尝试获取持久化访问权限
                        try {
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                            Log.d(TAG, "持久化权限获取成功");
                        } catch (SecurityException e) {
                            Log.w(TAG, "无法获取持久化权限: " + e.getMessage());
                        }

                        selectedAvatarUri = imageUri.toString();

                        // 测试是否能立即加载
                        try {
                            ivAvatar.setImageURI(imageUri);
                            if (ivAvatar.getDrawable() != null) {
                                Log.d(TAG, "图片加载成功");
                                Toast.makeText(this, "头像已选择", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "图片加载失败：drawable为null");
                                Toast.makeText(this, "图片加载失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "图片加载异常: " + e.getMessage());
                            Toast.makeText(this, "图片加载异常", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persona_edit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        ivAvatar = findViewById(R.id.ivAvatar);
        etName = findViewById(R.id.etName);
        etPersonality = findViewById(R.id.etPersonality);
        etBackgroundStory = findViewById(R.id.etBackgroundStory);
        etSystemPrompt = findViewById(R.id.etSystemPrompt);

        // 点击头像选择图片
        ivAvatar.setOnClickListener(v -> openImagePicker());

        // 检查是否是编辑模式
        personaId = getIntent().getLongExtra("personaId", -1);
        if (personaId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("编辑 Persona");
            }
            loadPersona();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("创建 Persona");
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // 添加持久化权限标志
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        imagePickerLauncher.launch(intent);
    }

    private void loadPersona() {
        executor.execute(() -> {
            currentPersona = database.personaDao().getPersonaById(personaId);
            if (currentPersona != null) {
                runOnUiThread(() -> {
                    etName.setText(currentPersona.name);
                    etPersonality.setText(currentPersona.personality);
                    etBackgroundStory.setText(currentPersona.backgroundStory);
                    etSystemPrompt.setText(currentPersona.systemPrompt);

                    // 加载头像
                    if (currentPersona.avatarUri != null && !currentPersona.avatarUri.isEmpty()) {
                        selectedAvatarUri = currentPersona.avatarUri;
                        Log.d(TAG, "加载保存的头像URI: " + selectedAvatarUri);
                        loadImageSafely(ivAvatar, currentPersona.avatarUri);
                    }
                });
            }
        });
    }

    /**
     * 安全加载图片，处理权限异常
     */
    private void loadImageSafely(ImageView imageView, String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            Log.d(TAG, "URI为空，显示默认头像");
            imageView.setImageResource(R.drawable.ic_default_avatar);
            return;
        }

        try {
            Uri uri = Uri.parse(uriString);
            Log.d(TAG, "尝试加载图片: " + uri);

            imageView.setImageURI(uri);

            if (imageView.getDrawable() == null) {
                Log.w(TAG, "图片加载后drawable为null，可能是权限问题");
                imageView.setImageResource(R.drawable.ic_default_avatar);
                Toast.makeText(this, "头像加载失败，可重新选择", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "图片加载成功");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "权限失效: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_default_avatar);
            Toast.makeText(this, "头像权限失效，请重新选择", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "图片加载异常: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            savePersona();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePersona() {
        String name = etName.getText().toString().trim();
        String personality = etPersonality.getText().toString().trim();
        String backgroundStory = etBackgroundStory.getText().toString().trim();
        String systemPrompt = etSystemPrompt.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "保存Persona，头像URI: " + selectedAvatarUri);

        executor.execute(() -> {
            try {
                PersonaEntity persona;
                if (currentPersona != null) {
                    // 编辑模式
                    persona = currentPersona;
                    persona.name = name;
                    persona.avatarUri = selectedAvatarUri;
                    persona.personality = personality;
                    persona.backgroundStory = backgroundStory;
                    persona.systemPrompt = systemPrompt;
                    persona.updatedAt = System.currentTimeMillis();
                    database.personaDao().updatePersona(persona);
                } else {
                    // 新建模式
                    persona = new PersonaEntity();
                    persona.name = name;
                    persona.avatarUri = selectedAvatarUri;
                    persona.personality = personality;
                    persona.backgroundStory = backgroundStory;
                    persona.systemPrompt = systemPrompt;
                    database.personaDao().insertPersona(persona);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "保存失败: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}