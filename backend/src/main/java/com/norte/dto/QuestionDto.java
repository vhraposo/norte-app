package com.norte.dto;

import java.util.List;

public class QuestionDto {
    private String id;
    private String text;
    private List<String> options;

    public QuestionDto(String id, String text, List<String> options) {
        this.id = id;
        this.text = text;
        this.options = options;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
}
