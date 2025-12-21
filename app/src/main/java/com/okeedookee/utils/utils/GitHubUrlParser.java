package com.okeedookee.utils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse GitHub URLs and extract repository information.
 */
public class GitHubUrlParser {

    /**
     * Represents parsed GitHub URL components.
     */
    public static class GitHubUrlInfo {
        private final String owner;
        private final String repo;
        private final String branch;
        private final String filePath;

        public GitHubUrlInfo(String owner, String repo, String branch, String filePath) {
            this.owner = owner;
            this.repo = repo;
            this.branch = branch;
            this.filePath = filePath;
        }

        public String getOwner() {
            return owner;
        }

        public String getRepo() {
            return repo;
        }

        public String getBranch() {
            return branch;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isValid() {
            return owner != null && !owner.isEmpty() &&
                    repo != null && !repo.isEmpty() &&
                    filePath != null && !filePath.isEmpty();
        }
    }

    /**
     * Parses a GitHub URL and extracts owner, repo, branch, and file path.
     * 
     * Supports formats:
     * - https://github.com/owner/repo/blob/branch/path/to/file.csv
     * - https://raw.githubusercontent.com/owner/repo/branch/path/to/file.csv
     * 
     * @param url The GitHub URL to parse
     * @return GitHubUrlInfo object containing parsed components, or null if invalid
     */
    public static GitHubUrlInfo parse(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        url = url.trim();

        // Pattern for github.com/owner/repo/blob/branch/path
        Pattern blobPattern = Pattern.compile(
                "https?://github\\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.+)");

        // Pattern for raw.githubusercontent.com/owner/repo/branch/path
        Pattern rawPattern = Pattern.compile(
                "https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/([^/]+)/(.+)");

        Matcher blobMatcher = blobPattern.matcher(url);
        Matcher rawMatcher = rawPattern.matcher(url);

        if (blobMatcher.matches()) {
            String owner = blobMatcher.group(1);
            String repo = blobMatcher.group(2);
            String branch = blobMatcher.group(3);
            String filePath = blobMatcher.group(4);
            return new GitHubUrlInfo(owner, repo, branch, filePath);
        } else if (rawMatcher.matches()) {
            String owner = rawMatcher.group(1);
            String repo = rawMatcher.group(2);
            String branch = rawMatcher.group(3);
            String filePath = rawMatcher.group(4);
            return new GitHubUrlInfo(owner, repo, branch, filePath);
        }

        return null;
    }
}
