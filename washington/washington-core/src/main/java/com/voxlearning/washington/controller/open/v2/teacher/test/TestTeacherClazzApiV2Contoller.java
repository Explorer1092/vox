package com.voxlearning.washington.controller.open.v2.teacher.test;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTeacherClazzApiV2Contoller {

    public static void main(String[] args) {
        final String appKey = "17Teacher";
        final String secretKey = "gvUKQN1EFXKp";
        final String sessionKey = "00a240c5e03095e03c6dddd3d5c16ce8";

        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
//        paramMap.put("clazz_level", "1");
//        paramMap.put("clazz_name", "测试班级98");
//        paramMap.put("school_id", "2639");
//        paramMap.put("clazz_edu_system", "J4");
//        paramMap.put("clazz_type", "WALKING");

        paramMap.put("group_id", "80218");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://www.test.17zuoye.net/v2/teacher/exitClazz.vpage";
//        String apiURL = "http://www.test.17zuoye.net/v2/teacher/createClazz.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });

//        post.addParameter("clazz_ids", "[40098135]");
//        System.out.println(JsonUtils.toJson(classMappers));
        System.out.println(post.execute().getResponseString());

        System.out.println("Test End...");
    }

}
