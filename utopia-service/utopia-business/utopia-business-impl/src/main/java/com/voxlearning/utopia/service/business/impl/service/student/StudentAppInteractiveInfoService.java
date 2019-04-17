/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.api.entity.StudentAppInteractiveInfo;
import com.voxlearning.utopia.service.business.impl.dao.StudentAppInteractiveInfoDao;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by tanguohong on 14-4-10.
 */
@Named
public class StudentAppInteractiveInfoService extends BusinessServiceSpringBean {

    @Inject private StudentAppInteractiveInfoDao studentAppInteractiveInfoDao;


    public MapMessage findClazzRank(Long clazzId, Long bookId, Long unitId, Long lessonId, Long practiceId) {
        MapMessage mapMessage = new MapMessage();

        List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

        List<StudentAppInteractiveInfo> studentAppInteractiveInfos = studentAppInteractiveInfoDao.findByUserIdsAndUnitId(userIds, bookId, unitId, lessonId, practiceId);
        Comparator<StudentAppInteractiveInfo> comparator = (o2, o1) -> o1.getScore().compareTo(o2.getScore());
        Collections.sort(studentAppInteractiveInfos, comparator);
        List<Map<String, Object>> rankMaps = processRank(studentAppInteractiveInfos, userMap);
        mapMessage.add("userRanks", rankMaps);
        mapMessage.setSuccess(true);
        return mapMessage;
    }

    private List<Map<String, Object>> processRank(List<StudentAppInteractiveInfo> studentAppInteractiveInfos, Map<Long, User> userMap) {
        List<Map<String, Object>> rankMaps = new ArrayList<>();
        for (StudentAppInteractiveInfo studentAppInteractiveInfo : studentAppInteractiveInfos) {
            Map<String, Object> rankMap = new HashMap<>();
            Long userId = studentAppInteractiveInfo.getUserId();
            User user = userMap.get(userId);
            if (user != null) {
                rankMap.put("userId", userId);
                rankMap.put("userName", user.getProfile().getRealname());
                String avatarUrl;
                if (!StringUtils.isEmpty(user.getProfile().getImgUrl()))
                    avatarUrl = "gridfs/" + user.getProfile().getImgUrl();
                else
                    avatarUrl = "upload/images/avatar/avatar_normal.gif";
                rankMap.put("userImg", avatarUrl);
                rankMap.put("detail", studentAppInteractiveInfo.getDataJson());
                rankMap.put("score", studentAppInteractiveInfo.getScore());
                rankMaps.add(rankMap);
            }
        }
        return rankMaps;
    }

    public MapMessage saveStudentAppInteractiveInfo(Long userId, Long bookId, Long unitId, Long lessonId, Long practiceId, Integer score, Map<String, Object> dataJson) {
        StudentAppInteractiveInfo studentAppInteractiveInfo = studentAppInteractiveInfoDao.findByUserIdAndUnitId(userId, bookId, unitId, lessonId, practiceId);
        if (score <= 0) {
            return MapMessage.errorMessage("分数为0不做处理");
        }
        if (studentAppInteractiveInfo != null) {
            if (score > studentAppInteractiveInfo.getScore()) {
                studentAppInteractiveInfo.setScore(score);
                studentAppInteractiveInfo.setDataJson(dataJson);
                studentAppInteractiveInfoDao.update(studentAppInteractiveInfo.getId(), studentAppInteractiveInfo);
            }
        } else {
            studentAppInteractiveInfo = new StudentAppInteractiveInfo();
            studentAppInteractiveInfo.setUserId(userId);
            studentAppInteractiveInfo.setBookId(bookId);
            studentAppInteractiveInfo.setUnitId(unitId);
            studentAppInteractiveInfo.setLessonId(lessonId);
            studentAppInteractiveInfo.setPracticeId(practiceId);
            studentAppInteractiveInfo.setScore(score);
            studentAppInteractiveInfo.setDataJson(dataJson);
            studentAppInteractiveInfoDao.insert(studentAppInteractiveInfo);
        }
        return MapMessage.successMessage();
    }

}
