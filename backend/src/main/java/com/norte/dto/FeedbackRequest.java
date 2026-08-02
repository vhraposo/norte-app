package com.norte.dto;

public class FeedbackRequest {
    private boolean agrees;
    private String comment;

    public boolean isAgrees() { return agrees; }
    public void setAgrees(boolean agrees) { this.agrees = agrees; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
