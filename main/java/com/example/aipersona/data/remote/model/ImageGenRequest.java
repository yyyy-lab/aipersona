package com.example.aipersona.data.remote.model;

import java.io.Serializable;

/**
 * 图片生成请求体
 * 对应智谱 AI /images/generations 的请求 JSON
 * 只保留最常用的字段：model / prompt / size
 */
public class ImageGenRequest implements Serializable {

    // 模型名称，例如：cogview-4-250304 / cogview-4 / cogview-3-flash / cogview-3
    public String model;

    // 文本提示词
    public String prompt;

    // 图片尺寸，可选，默认 1024x1024
    public String size;

    // 最常用的构造函数（你现在项目里用的就是这个）
    public ImageGenRequest(String model, String prompt) {
        this(model, prompt, "1024x1024");
    }

    // 带 size 的构造函数（如果以后想改尺寸可以用这个）
    public ImageGenRequest(String model, String prompt, String size) {
        this.model = model;
        this.prompt = prompt;
        this.size = size;
    }
}
