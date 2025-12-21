package com.okeedookee.utils.network;

public class GithubFileResponse {
    private String name;
    private String path;
    private String sha;
    private String content; // Base64 encoded
    private String encoding;

    public GithubFileResponse(String name, String path, String sha, String content, String encoding) {
        this.name = name;
        this.path = path;
        this.sha = sha;
        this.content = content;
        this.encoding = encoding;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public String getSha() { return sha; }
    public String getContent() { return content; }
    public String getEncoding() { return encoding; }
}
