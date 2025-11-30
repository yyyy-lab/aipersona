package com.example.aipersona.ui.social;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.local.entity.PostEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreatePostActivity extends AppCompatActivity {
    private Spinner spinnerPersona;
    private EditText etPostContent;

    private AppDatabase database;
    private Executor executor;

    private List<PersonaEntity> personas = new ArrayList<>();
    private PersonaEntity selectedPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("发布动态");
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        spinnerPersona = findViewById(R.id.spinnerPersona);
        etPostContent = findViewById(R.id.etPostContent);

        // 加载用户的Persona列表
        loadPersonas();
    }


    private void loadPersonas() {
        // 直接在主线程观察 LiveData
        database.personaDao().getAllPersonas().observe(this, personas -> {
            this.personas = personas;  // 保存到成员变量

            if (personas == null || personas.isEmpty()) {
                Toast.makeText(this, "请先创建一个Persona", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // 设置 Spinner
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

            // 默认选中第一个
            if (!personas.isEmpty()) {
                selectedPersona = personas.get(0);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_publish) {
            publishPost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPersona == null) {
            Toast.makeText(this, "请选择一个Persona", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                PostEntity post = new PostEntity(
                        selectedPersona.id,
                        selectedPersona.name,
                        content
                );

                database.postDao().insertPost(post);

                runOnUiThread(() -> {
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "发布失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}