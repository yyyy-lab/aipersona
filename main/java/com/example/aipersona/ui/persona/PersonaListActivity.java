package com.example.aipersona.ui.persona;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.ui.social.FollowListActivity;
import com.example.aipersona.ui.social.SocialSquareActivity;

import java.util.ArrayList;
import java.util.List;

public class PersonaListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PersonaAdapter adapter;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persona_list);

        // 设置标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("我的Persona");
        }

        database = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewPersonas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PersonaAdapter();
        adapter.setOnItemClickListener(persona -> {
            // 点击Persona，跳转到主页
            Intent intent = new Intent(this, PersonaProfileActivity.class);
            intent.putExtra("personaId", persona.id);
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(persona -> {
            // 长按编辑Persona
            Intent intent = new Intent(this, PersonaEditActivity.class);
            intent.putExtra("personaId", persona.id);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // 添加Persona按钮
        FloatingActionButton fab = findViewById(R.id.fabAddPersona);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonaEditActivity.class);
            startActivity(intent);
        });

        // 观察数据变化
        LiveData<List<PersonaEntity>> personasLiveData = database.personaDao().getAllPersonas();
        personasLiveData.observe(this, personas -> {
            adapter.setPersonas(personas);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_social_square) {
            // 打开社交广场
            Intent intent = new Intent(this, SocialSquareActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_follows) {
            // 打开关注列表
            Intent intent = new Intent(this, FollowListActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // RecyclerView适配器
    private static class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.ViewHolder> {
        private List<PersonaEntity> personas = new ArrayList<>();
        private OnItemClickListener onItemClickListener;
        private OnItemClickListener onItemLongClickListener;

        interface OnItemClickListener {
            void onItemClick(PersonaEntity persona);
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }

        void setOnItemLongClickListener(OnItemClickListener listener) {
            this.onItemLongClickListener = listener;
        }

        void setPersonas(List<PersonaEntity> personas) {
            this.personas = personas;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_persona, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PersonaEntity persona = personas.get(position);

            // 安全加载头像
            loadImageSafely(holder.ivAvatar, persona.avatarUri);

            holder.tvName.setText(persona.name);
            holder.tvPersonality.setText(persona.personality);

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(persona);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemClick(persona);
                }
                return true;
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
            return personas.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName;
            TextView tvPersonality;

            ViewHolder(View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.ivPersonaAvatar);
                tvName = itemView.findViewById(R.id.tvPersonaName);
                tvPersonality = itemView.findViewById(R.id.tvPersonality);
            }
        }
    }
}