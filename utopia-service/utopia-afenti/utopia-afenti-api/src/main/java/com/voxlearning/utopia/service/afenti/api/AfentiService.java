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

package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20170905")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AfentiService extends IPingable {

    void completeGuide(Long studentId, String name);

    MapMessage getAfentiLastestOrderStatus(Long userId, Subject subject);

    boolean hasValidAfentiOrder(Long userId, Subject subject);

    MapMessage fetchAfentiBook(Long userId, Subject subject, AfentiLearningType type);

    List<AfentiBook> fetchAfentiBooks(Long userId, Subject subject, AfentiLearningType type);

    MapMessage generateAfentiRank(Collection<String> bookIds, Subject subject);

    MapMessage generateAfentiRankForMath(Collection<String> bookIds);

    MapMessage generateAfentiRankForChinese(Collection<String> bookIds);

    boolean addUserPurchaseInfo(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate);

    boolean addUserRewardInfo(StudentDetail studentDetail, Integer integral);

    /**
     * 错题宝首页
     *
     * @param studentId  学生ID
     * @param subject    学科
     * @param clazzLevel 年级
     * @param termType   学期类型(1:上学期 2:下学期)
     * @param bookSeries 出版社类型(全国通用COMMON,人教RENJIAO)
     */
    MapMessage wrongQuestionPlusIndex(Long studentId, Subject subject, Integer clazzLevel, Integer termType, String bookSeries);

    /**
     * 查询当前学期的视频列表
     *
     * @param studentId  学生ID
     * @param subject    学科
     * @param clazzLevel 年级
     * @param termType   学期类型(1:上学期 2:下学期)
     * @param bookSeries 出版社类型(全国通用COMMON,人教RENJIAO)
     */
    MapMessage getCurrentCourseVideoList(Long studentId, Subject subject, Integer clazzLevel, Integer termType, String bookSeries);

    /**
     * 查询当前课时的视频详细信息
     *
     * @param lessonId  课时ID
     * @param hasOpened 是否已经开通了服务
     */
    MapMessage getCurrentLessonVideoDetail(String lessonId, Boolean hasOpened);

    /**
     * 查询是否加入过视频观看记录
     *
     * @param studentId 学生ID
     * @param lessonId  课时ID
     */
    Boolean isAddedVideoViewRecord(Long studentId, String lessonId);

    /**
     * 加入视频观看记录
     *
     * @param studentId 学生ID
     * @param lessonId  课时ID
     */
    void addVideoViewRecord(Long studentId, String lessonId);

    @NoResponseWait
    void sendMessage(Message message);

    @NoResponseWait
    void sendLoginMessage(Message message);

}
