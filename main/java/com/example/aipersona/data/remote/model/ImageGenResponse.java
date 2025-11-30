package com.example.aipersona.data.remote.model;

import java.io.Serializable;
import java.util.List;

/**
 * 图片生成响应体
 * 对应智谱 AI /images/generations 的响应 JSON
 */
public class ImageGenResponse implements Serializable {

    // 请求创建时间（Unix 时间戳，秒）
    public long created;

    // 图片数据数组，目前一般只返回一张图片
    public List<ImageData> data;

    // 内容安全信息（可选）
    public List<ContentFilter> content_filter;

    /**
     * 图片数据
     * 对应 JSON: { "url": "..." }
     */
    public static class ImageData implements Serializable {
        public String url;   // 图片 URL
    }

    /**
     * 内容安全信息（可选）
     * 对应 JSON: { "role": "...", "level": 0-3 }
     */
    public static class ContentFilter implements Serializable {
        public String role;  // assistant / user / history
        public int level;    // 0-3，0 最严重
    }

    /**
     * 方便使用的工具方法：返回第一张图片的 URL
     * ChatRepository 里就是调用的这个方法
     */
    public String getImageUrl() {
        if (data != null && !data.isEmpty() && data.get(0) != null) {
            return data.get(0).url;
        }
        return null;
    }
}
