package com.example.aipersona.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.aipersona.data.local.dao.MessageDao;
import com.example.aipersona.data.local.dao.PersonaDao;
import com.example.aipersona.data.local.entity.MessageEntity;
import com.example.aipersona.data.local.entity.PersonaEntity;
import com.example.aipersona.data.remote.api.GLMApiService;
import com.example.aipersona.data.remote.api.ImageGenApiService;
import com.example.aipersona.data.remote.model.ChatRequest;
import com.example.aipersona.data.remote.model.ChatResponse;
import com.example.aipersona.data.remote.model.ImageGenRequest;
import com.example.aipersona.data.remote.model.ImageGenResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final PersonaDao personaDao;
    private final MessageDao messageDao;
    private final GLMApiService textApiService;
    private final ImageGenApiService imageApiService;
    private final Executor executor;
    private final Handler mainHandler;
    private final Random random;

    // 触发图片生成的关键词
    private static final String[] IMAGE_KEYWORDS = {
            "画", "绘制", "生成图片", "图片", "draw", "image", "picture", "看看"
    };

    // 模拟回复内容库
    private static final String[] MOCK_RESPONSES = {
            "这真是个有趣的话题！我很喜欢和你聊这个。",
            "让我想想...嗯，我觉得你说得很有道理。",
            "哈哈，你总是能让我开心！",
            "我理解你的想法，这确实值得深思。",
            "太棒了！继续说吧，我在听呢。"
    };

    // 模式切换标志
    private static final boolean USE_MOCK_MODE = false;  // 改为false使用真实API
    private static final boolean ENABLE_IMAGE_GEN = true;  // 是否启用图片生成

    public ChatRepository(PersonaDao personaDao, MessageDao messageDao,
                          GLMApiService textApiService, ImageGenApiService imageApiService) {
        this.personaDao = personaDao;
        this.messageDao = messageDao;
        this.textApiService = textApiService;
        this.imageApiService = imageApiService;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.random = new Random();
    }

    public LiveData<List<MessageEntity>> getMessages(long personaId) {
        return messageDao.getMessagesByPersonaId(personaId);
    }

    public void sendMessage(long personaId, String userMessage, SendMessageCallback callback) {
        executor.execute(() -> {
            try {
                // 1. 保存用户消息
                MessageEntity userMsg = new MessageEntity(personaId, userMessage, true);
                messageDao.insertMessage(userMsg);

                // 2. 获取Persona信息
                PersonaEntity persona = personaDao.getPersonaById(personaId);
                if (persona == null) {
                    mainHandler.post(() -> callback.onError("Persona不存在"));
                    return;
                }

                // 3. 检查是否需要生成图片
                if (ENABLE_IMAGE_GEN && shouldGenerateImage(userMessage)) {
                    // 图文模式
                    handleImageResponse(personaId, persona, userMessage, callback);
                } else {
                    // 纯文本模式
                    if (USE_MOCK_MODE) {
                        handleMockResponse(personaId, persona, userMessage, callback);
                    } else {
                        handleTextResponse(personaId, persona, callback);
                    }
                }

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("发送失败: " + e.getMessage()));
            }
        });
    }

    /**
     * 判断是否需要生成图片
     */
    private boolean shouldGenerateImage(String message) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : IMAGE_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
//    // 先临时改成：所有消息都当成“要生成图片”
//    private boolean shouldGenerateImage(String message) {
//        return true;
//    }



    /**
     * 处理图文回复（先生成图片，再回复文字）
     */
    private void handleImageResponse(long personaId, PersonaEntity persona,
                                     String userMessage, SendMessageCallback callback) {
        // 1. 提取图片描述
        String imagePrompt = extractImagePrompt(userMessage);

        // 2. 调用图片生成API
        ImageGenRequest request = new ImageGenRequest("cogview-3", imagePrompt);

        imageApiService.generateImage(request).enqueue(new Callback<ImageGenResponse>() {
            @Override
            public void onResponse(Call<ImageGenResponse> call, Response<ImageGenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getImageUrl();

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // 保存图片消息（URL格式：[IMAGE]url）
                        executor.execute(() -> {
                            String content = "[IMAGE]" + imageUrl + "\n这是为你生成的图片：" + imagePrompt;
                            MessageEntity aiMsg = new MessageEntity(personaId, content, false);
                            long msgId = messageDao.insertMessage(aiMsg);
                            aiMsg.id = msgId;

                            mainHandler.post(() -> callback.onSuccess(aiMsg));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError("图片生成失败"));
                    }
                } else {
                    // 图片生成失败，回退到文字回复
                    handleTextResponse(personaId, persona, callback);
                }
            }

            @Override
            public void onFailure(Call<ImageGenResponse> call, Throwable t) {
                // 图片生成失败，回退到文字回复
                handleTextResponse(personaId, persona, callback);
            }
        });
    }

    /**
     * 提取图片描述
     */
    private String extractImagePrompt(String message) {
        // 移除触发词，提取实际描述
        String prompt = message;
        for (String keyword : IMAGE_KEYWORDS) {
            prompt = prompt.replace(keyword, "").trim();
        }

        // 如果没有描述，返回默认值
        if (prompt.isEmpty() || prompt.length() < 2) {
            return "一个美丽的场景";
        }

        return prompt;
    }

    /**
     * 处理纯文本回复
     */
    private void handleTextResponse(long personaId, PersonaEntity persona, SendMessageCallback callback) {
        // 构建对话历史
        List<MessageEntity> history = messageDao.getRecentMessages(personaId, 10);
        List<ChatRequest.ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatRequest.ChatMessage("system", persona.generateFullSystemPrompt()));

        for (int i = history.size() - 1; i >= 0; i--) {
            MessageEntity msg = history.get(i);
            String role = msg.isFromUser ? "user" : "assistant";
            // 过滤掉图片标记
            String content = msg.content.replaceAll("\\[IMAGE\\].*?\n", "");
            messages.add(new ChatRequest.ChatMessage(role, content));
        }

        ChatRequest request = new ChatRequest("glm-4-flash", messages);

        textApiService.chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiContent = response.body().getContent();
                    if (aiContent != null && !aiContent.isEmpty()) {
                        executor.execute(() -> {
                            MessageEntity aiMsg = new MessageEntity(personaId, aiContent, false);
                            long msgId = messageDao.insertMessage(aiMsg);
                            aiMsg.id = msgId;
                            mainHandler.post(() -> callback.onSuccess(aiMsg));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError("AI回复为空"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("API调用失败: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                mainHandler.post(() -> callback.onError("网络错误: " + t.getMessage()));
            }
        });
    }

    /**
     * 模拟模式：生成本地回复
     */
    private void handleMockResponse(long personaId, PersonaEntity persona,
                                    String userMessage, SendMessageCallback callback) {
        try {
            Thread.sleep(1000 + random.nextInt(1000));

            String aiResponse = generateMockResponse(persona, userMessage);

            MessageEntity aiMsg = new MessageEntity(personaId, aiResponse, false);
            long msgId = messageDao.insertMessage(aiMsg);
            aiMsg.id = msgId;

            MessageEntity finalAiMsg = aiMsg;
            mainHandler.post(() -> callback.onSuccess(finalAiMsg));

        } catch (Exception e) {
            mainHandler.post(() -> callback.onError("模拟回复失败: " + e.getMessage()));
        }
    }

    private String generateMockResponse(PersonaEntity persona, String userMessage) {
        String response = MOCK_RESPONSES[random.nextInt(MOCK_RESPONSES.length)];

        String personality = persona.personality != null ? persona.personality.toLowerCase() : "";

        if (personality.contains("活泼") || personality.contains("开朗")) {
            response = "哇！" + response + " 😊";
        } else if (personality.contains("温柔") || personality.contains("体贴")) {
            response = "嗯嗯，" + response;
        }

        return response;
    }

    public interface SendMessageCallback {
        void onSuccess(MessageEntity message);
        void onError(String error);
    }
}