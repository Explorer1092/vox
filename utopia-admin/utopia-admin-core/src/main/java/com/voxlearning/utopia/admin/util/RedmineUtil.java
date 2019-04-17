/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2015/11/25
 */
@Slf4j
public final class RedmineUtil {

    private static final String CREATE_ISSUE = "http://project.17zuoye.net/redmine/issues.json?key=";
    private static final int DEFAULT_PROJECT = 1;  // 17zuoye web

    public static boolean createIssue(String redmineKey, String subject, String description, Integer assigned, Priority priority, Tracker tracker) {
        Map<String, Object> params = new HashMap<>();
        params.put("project_id", DEFAULT_PROJECT);
        params.put("subject", subject);
        params.put("description", description);
        params.put("assigned_to_id", assigned);
        int priorityCode = priority == null ? Priority.默认.code : priority.code;
        params.put("priority_id", priorityCode);
        int trackerCode = tracker == null ? Tracker.Default.code : tracker.code;
        params.put("tracker_id", trackerCode);
        Map<String, Object> issue = Collections.singletonMap("issue", params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(CREATE_ISSUE + redmineKey).json(issue).execute();
        if (response.getStatusCode() != 201) {
            log.warn("Fail to create issue with statusCode = {}, exception = {}", response.getStatusCode(), response.getHttpClientException());
            return false;
        }
        return true;
    }

    public enum Priority {
        默认(2),
        低(1),
        普通(2),
        高(3),
        紧急(4),
        立刻(5);

        public final int code;

        Priority(int code) {
            this.code = code;
        }
    }

    public enum Tracker {
        Default(1),
        Bug(1),
        Feature(2),
        Task(3),
        Enhancement(4),
        Feedback(5),
        Design(6),
        AB_Test(7);

        public final int code;

        Tracker(int code) {
            this.code = code;
        }
    }
}
