package com.example.bookhub.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String description;
    private String content;
    private byte[] coverImage;
    private int categoryId;
    private int userId;

    // Optional fields that are not saved in database but used for display
    private String categoryName;
    private String userName;

    public Book() {
    }

    public Book(String title, String author, String description, String content, byte[] coverImage, int categoryId, int userId) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.content = content;
        this.coverImage = coverImage;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public Book(int id, String title, String author, String description, String content, byte[] coverImage, int categoryId, int userId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.content = content;
        this.coverImage = coverImage;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(byte[] coverImage) {
        this.coverImage = coverImage;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", categoryId=" + categoryId +
                ", userId=" + userId +
                '}';
    }
}