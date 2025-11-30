package com.example.aipersona.data.remote.api;

import com.example.aipersona.data.remote.model.ImageGenRequest;
import com.example.aipersona.data.remote.model.ImageGenResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ImageGenApiService {
    // 智谱AI 图片生成API端点
    String BASE_URL = "https://open.bigmodel.cn/api/paas/v4/";

    @POST("images/generations")
    Call<ImageGenResponse> generateImage(@Body ImageGenRequest request);

    // 创建API服务实例
    static ImageGenApiService create(String apiKey) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + apiKey)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(60, TimeUnit.SECONDS)  // 图片生成较慢，延长超时
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ImageGenApiService.class);
    }
}