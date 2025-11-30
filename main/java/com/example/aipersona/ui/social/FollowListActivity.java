package com.example.aipersona.ui.social;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.aipersona.R;
import com.example.aipersona.data.local.database.AppDatabase;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.ui.persona.PersonaProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FollowListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PersonaAdapter adapter;
    private AppDatabase database;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("我的关注");
        }

        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewFollows);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PersonaAdapter();
        adapter.setOnItemClickListener(persona -> {
            // 点击跳转到Persona主页
            Intent intent = new Intent(this, PersonaProfileActivity.class);
            intent.putExtra("personaId", persona.id);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // 观察关注的Persona列表
        LiveData<List<Long>> followedIdsLiveData = database.followDao().getFollowedPersonaIds();
        followedIdsLiveData.observe(this, followedIds -> {
            if (followedIds != null && !followedIds.isEmpty()) {
                loadFollowedPersonas(followedIds);
            } else {
                adapter.setPersonas(new ArrayList<>());
            }
        });
    }

    private void loadFollowedPersonas(List<Long> followedIds) {
        executor.execute(() -> {
            List<PersonaEntity> personas = new ArrayList<>();
            for (Long personaId : followedIds) {
                PersonaEntity persona = database.personaDao().getPersonaById(personaId);
                if (persona != null) {
                    personas.add(persona);
                }
            }

            runOnUiThread(() -> adapter.setPersonas(personas));
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

    // Persona适配器
    private static class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.ViewHolder> {
        private List<PersonaEntity> personas = new ArrayList<>();
        private OnItemClickListener onItemClickListener;

        interface OnItemClickListener {
            void onItemClick(PersonaEntity persona);
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
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