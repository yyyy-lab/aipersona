package com.example.aipersona.data.remote.api;


import com.example.aipersona.data.remote.model.ChatRequest;
import com.example.aipersona.data.remote.model.ChatResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GLMApiService {
    // 智谱AI API 端点
    String BASE_URL = "https://open.bigmodel.cn/api/paas/v4/";

    @POST("chat/completions")
    Call<ChatResponse> chat(@Body ChatRequest request);

    // 创建API服务实例
    static GLMApiService create(String apiKey) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(GLMApiService.class);
    }
}
