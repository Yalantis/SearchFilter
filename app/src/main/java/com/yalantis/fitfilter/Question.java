package com.yalantis.fitfilter;

import java.util.List;

/**
 * Created by galata on 16.09.16.
 */
public class Question {
    private String authorName;
    private String authorJobTitle;
    private String authorAvatar;
    private String date;
    private String text;
    private List<Tag> tags;

    public Question(String authorName, String authorJobTitle, String authorAvatar, String date, String text, List<Tag> tags) {
        this.authorName = authorName;
        this.authorJobTitle = authorJobTitle;
        this.authorAvatar = authorAvatar;
        this.date = date;
        this.text = text;
        this.tags = tags;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorJobTitle() {
        return authorJobTitle;
    }

    public void setAuthorJobTitle(String authorJobTitle) {
        this.authorJobTitle = authorJobTitle;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public boolean hasTag(String string) {
        for (Tag tag : tags) {
            if (tag.getText().equals(string)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;

        Question question = (Question) o;

        if (getAuthorName() != null ? !getAuthorName().equals(question.getAuthorName()) : question.getAuthorName() != null)
            return false;
        if (getAuthorJobTitle() != null ? !getAuthorJobTitle().equals(question.getAuthorJobTitle()) : question.getAuthorJobTitle() != null)
            return false;
        if (getAuthorAvatar() != null ? !getAuthorAvatar().equals(question.getAuthorAvatar()) : question.getAuthorAvatar() != null)
            return false;
        if (getDate() != null ? !getDate().equals(question.getDate()) : question.getDate() != null)
            return false;
        if (getText() != null ? !getText().equals(question.getText()) : question.getText() != null)
            return false;
        return getTags() != null ? getTags().equals(question.getTags()) : question.getTags() == null;

    }

    @Override
    public int hashCode() {
        int result = getAuthorName() != null ? getAuthorName().hashCode() : 0;
        result = 31 * result + (getAuthorJobTitle() != null ? getAuthorJobTitle().hashCode() : 0);
        result = 31 * result + (getAuthorAvatar() != null ? getAuthorAvatar().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        return result;
    }
}
