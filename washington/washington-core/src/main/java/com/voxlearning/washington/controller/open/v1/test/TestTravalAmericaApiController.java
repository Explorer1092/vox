/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.test;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.util.HttpUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.util.DigestUtils;
import com.voxlearning.utopia.service.question.api.entity.ZmWord;
import com.voxlearning.utopia.service.question.api.mapper.ZmWordRequest;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author peng.zhang.a
 * @since 2016/6/6
 */
public class TestTravalAmericaApiController {

    public static final String BASE_URL = "http://127.0.0.1:8081/v1/travelAmerica/";


    public static void main(String[] args) {
        getQuestionsByIds();
        getWords();
        getQuestions();
        getWordsByIds();
    }

    public static String getQuestionsByIds() {
        String url = "getQuestionsByIds.vpage";
        Map<String, Object> params = new HashMap<>();
        List<String> questionsId = new ArrayList<>();
        questionsId.add("ZQ_00354719772");
        questionsId.add("57108810e0b50a15cf5fc096");
        params.put("content", JsonUtils.toJson(questionsId));

        return request(BASE_URL + url, params, "getQuestionsByIds");

    }

    /**
     * mongondb office / mongo / office / vox-question / zm_question
     * query params: {"content_type_id":1030072}
     *
     * @return
     */
    public static String getQuestions() {
        String url = "getQuestions.vpage";
        Map<String, Object> params = new HashMap<>();

        //add params
        Map<Integer, Map<String, Integer>> content = new HashMap<>();
        Map<String, Integer> wordMap = new HashMap<>();
        wordMap.put("574ea2c459acd119cc1ff230", 0);
        wordMap.put("574ea2c459acd119cc1ff231", 0);
        wordMap.put("574ea2c459acd119cc1ff232", 0);
        wordMap.put("574ea2c459acd119cc1ff233", 0);
        wordMap.put("574ea2c459acd119cc1ff234", 0);
        content.put(1031008, wordMap);

        wordMap = new HashMap<>();
        wordMap.put("574ea2c459acd119cc1ff230", 0);
        wordMap.put("574ea2c459acd119cc1ff231", 0);
        wordMap.put("574ea2c459acd119cc1ff232", 0);
        wordMap.put("574ea2c459acd119cc1ff233", 0);
        content.put(1031001, wordMap);
//		map = new HashMap<>();
//		wordMap = new HashMap<>();
//		wordMap.put("574ea2c459acd119cc1ff230", 1);
//		map.put("wordMap", wordMap);
//		map.put("contentTypeId", 10300129);
//		content.add(map);

        params.put("content", JsonUtils.toJson(content));

        return request(BASE_URL + url, params, "getQuestions");

    }


    public static String getWords() {
        String url = "getWords.vpage";
        Map<String, Object> params = new HashMap<>();
        Map<Integer, Collection<ZmWordRequest>> content = new HashMap<>();

        List<ZmWordRequest> zmWordRequests = new ArrayList<>();
        ZmWordRequest zmWordRequest = new ZmWordRequest();
        zmWordRequest.setLevel(1);
        zmWordRequest.setLimit(5);
        zmWordRequest.setStart(1);
        zmWordRequests.add(zmWordRequest);
        zmWordRequest = new ZmWordRequest();
        zmWordRequest.setLevel(1);
        zmWordRequest.setLimit(5);
        zmWordRequest.setStart(6);
        zmWordRequests.add(zmWordRequest);
        content.put(1, zmWordRequests);

        zmWordRequests = new ArrayList<>();
        zmWordRequest = new ZmWordRequest();
        zmWordRequest.setLevel(2);
        zmWordRequest.setLimit(5);
        zmWordRequest.setStart(1);
        zmWordRequests.add(zmWordRequest);
        zmWordRequest = new ZmWordRequest();
        zmWordRequest.setLevel(2);
        zmWordRequest.setLimit(5);
        zmWordRequest.setStart(6);
        zmWordRequests.add(zmWordRequest);
        content.put(2, zmWordRequests);


        params.put("content", JsonUtils.toJson(content));

        String result = request(BASE_URL + url, params, "getWords");
        return result;
    }

    public static String getWordsByIds() {
        String url = "getWordByIds.vpage";
        Map<String, Object> params = new HashMap<>();
        //add params
        List<String> content = new ArrayList<>();
        content.add("574ea2c459acd119cc1ff230");
        content.add("574ea2c459acd119cc1ff231");
        content.add("574ea2c459acd119cc1ff232");
        params.put("content", JsonUtils.toJson(content));

        String result = request(BASE_URL + url, params, "getWordsByIds");
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.containsKey("result") && "success".equals(jsonObject.getString("result"))) {
            Map<String, ZmWord> zmWordMap = JsonUtils.fromJsonToMap(jsonObject.getString("wordsResult"), String.class, ZmWord.class);
            //Assert.isTrue(MapUtils.isNotEmpty(zmWordMap));
            //Assert.isTrue(zmWordMap.get("574ea2c459acd119cc1ff230").getId().equals("574ea2c459acd119cc1ff230"));
        }
        return result;
    }

    private static String request(String url, Map<String, Object> params, String menthonName) {
        System.out.println("==================" + menthonName + "=============");
//        System.out.println("url:" + url.toString());
//        System.out.println("content:" + params.get("content"));
        params.put("app_key", "UsaAdventure");
        params.put("session_key", "03226e0a42cfbb8959313206356c676c");
        params.put("sig", signMd5(params, "N1Ua6LPH3mYQ"));
        url = HttpUtils.buildUrlQuery(url, params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        return response.getResponseString();
    }

    static public String signMd5(Map<String, Object> m, String secret) {
        List<String> keys = new ArrayList<>(m.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append('=').append(m.get(key)).append('&');
        }
        sb.setLength(sb.length() - 1);
        try {
            return DigestUtils.md5Hex((sb.toString() + secret).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("failed to generateUrl", e);
        }
    }
}
