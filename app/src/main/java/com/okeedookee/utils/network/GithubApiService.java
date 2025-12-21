package com.okeedookee.utils.network;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GithubApiService {
    @GET("repos/{owner}/{repo}/contents/{path}")
    Call<GithubFileResponse> getFileContent(
        @Header("Authorization") String token,
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("path") String path
    );

    @DELETE("repos/{owner}/{repo}/contents/{path}")
    Call<Void> deleteFile(
        @Header("Authorization") String token,
        @Path("owner") String owner,
        @Path("repo") String repo,
        @Path("path") String path,
        @Query("message") String message,
        @Query("sha") String sha
    );
}
