package com.zebra.retrofitdemo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GitHubService {

    /**
     * @Description: 普通的 GET 请求，获取仓库列表
     * @Date: 2019/9/25
     * @Param:
     * @return: 若是一般情况，需要使用 ResponseBody
     */
    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}
