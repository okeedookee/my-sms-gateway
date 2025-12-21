package com.okeedookee.utils.config;

/**
 * GitHub configuration for SMS Gateway.
 * Update these values with your GitHub repository details.
 */
public class GitHubConfig {

    // GitHub file URL
    // Format: https://github.com/owner/repo/blob/branch/path/to/file.csv
    // Or: https://raw.githubusercontent.com/owner/repo/branch/path/to/file.csv
    // Example: https://github.com/octocat/my-repo/blob/main/sms.csv
    public static final String FILE_URL = "https://github.com/okeedookee/sms-gateway-storage/blob/main/sms.txt";

    // GitHub Personal Access Token with repo permissions
    // To generate a token: GitHub Settings > Developer settings > Personal access
    // tokens
    public static final String TOKEN = "YOUR_GITHUB_TOKEN";

    private GitHubConfig() {
        // Private constructor to prevent instantiation
    }
}
