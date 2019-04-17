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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import lombok.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class NewHomeworkProcessResult extends BaseHomeworkProcessResult implements Serializable {
    private static final long serialVersionUID = 5320671258526755827L;

    private String id;
    private Date createAt;                          // 创建时间
    private Date updateAt;                          // 修改时间
    private String sourceQuestionId;                // 原题id
    private QuestionWrongReason wrongReason;        // 错题原因

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = 8513894328969847330L;
        private String randomId = RandomUtils.nextObjectId();
        private String time;

        public ID(Date createTime) {
            this.time = Long.toString(createTime.getTime());
        }

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }

    /**
     * 1、这是个兼容方法，慎用，慎用，慎用.
     * 2、仅用于非复合题的连线题
     */
    public void reverseForLianXianTi() {
        // 为了兼容，先判断这个内容是不是顺序的
        if (CollectionUtils.isNotEmpty(userAnswers) && CollectionUtils.isNotEmpty(userAnswers.get(0))) {
            // 这个数组可能存在空字符串元素，取出有值的，空字符串不要
            List<String> tempAnswers = userAnswers.get(0).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 因为只存在二连线题，所以只需要取出前两个元素，进行比较即可
            if (CollectionUtils.isNotEmpty(tempAnswers) && tempAnswers.size() > 1) {
                String[] answers1 = StringUtils.split(tempAnswers.get(0), ",");
                String[] answers2 = StringUtils.split(tempAnswers.get(1), ",");

                if (answers1 != null && answers2 != null
                        && answers1.length == 2 && answers2.length == 2
                        && StringUtils.isNumeric(answers1[0]) && StringUtils.isNumeric(answers2[0])) {

                    int answer1 = SafeConverter.toInt(answers1[0]);
                    int answer2 = SafeConverter.toInt(answers2[0]);

                    if (answer1 > answer2) {
                        Collections.reverse(userAnswers.get(0));
                        if (CollectionUtils.isNotEmpty(subGrasp)) {
                            Collections.reverse(subGrasp.get(0));
                        }
                    }
                }
            }
        }
    }

    public static <T extends BaseHomeworkProcessResult> NewHomeworkProcessResult of(T baseHomeworkProcessResult) {
        if (baseHomeworkProcessResult == null) {
            return null;
        }
        NewHomeworkProcessResult newHomeworkProcessResult = new NewHomeworkProcessResult();

        if (baseHomeworkProcessResult instanceof HomeworkProcessResultHBase) {
            HomeworkProcessResultHBase homeworkProcessResultHBase = (HomeworkProcessResultHBase) baseHomeworkProcessResult;
            newHomeworkProcessResult.setId(homeworkProcessResultHBase.getId());
            newHomeworkProcessResult.setCreateAt(homeworkProcessResultHBase.getCreateAt());
            newHomeworkProcessResult.setUpdateAt(homeworkProcessResultHBase.getUpdateAt());
            newHomeworkProcessResult.setSourceQuestionId(homeworkProcessResultHBase.getSourceQuestionId());
            newHomeworkProcessResult.setWrongReason(homeworkProcessResultHBase.getWrongReason());
        }

        if (baseHomeworkProcessResult instanceof SubHomeworkProcessResult) {
            SubHomeworkProcessResult subHomeworkProcessResult = (SubHomeworkProcessResult) baseHomeworkProcessResult;
            newHomeworkProcessResult.setId(subHomeworkProcessResult.getId());
            newHomeworkProcessResult.setCreateAt(subHomeworkProcessResult.getCreateAt());
            newHomeworkProcessResult.setUpdateAt(subHomeworkProcessResult.getUpdateAt());
            newHomeworkProcessResult.setSourceQuestionId(subHomeworkProcessResult.getSourceQuestionId());
            newHomeworkProcessResult.setWrongReason(subHomeworkProcessResult.getWrongReason());
        }

        newHomeworkProcessResult.setHomeworkTag(baseHomeworkProcessResult.getHomeworkTag());
        newHomeworkProcessResult.setType(baseHomeworkProcessResult.getType());
        newHomeworkProcessResult.setClazzGroupId(baseHomeworkProcessResult.getClazzGroupId());
        newHomeworkProcessResult.setHomeworkId(baseHomeworkProcessResult.getHomeworkId());
        newHomeworkProcessResult.setBookId(baseHomeworkProcessResult.getBookId());
        newHomeworkProcessResult.setUnitId(baseHomeworkProcessResult.getUnitId());
        newHomeworkProcessResult.setUnitGroupId(baseHomeworkProcessResult.getUnitGroupId());
        newHomeworkProcessResult.setLessonId(baseHomeworkProcessResult.getLessonId());
        newHomeworkProcessResult.setSectionId(baseHomeworkProcessResult.getSectionId());
        newHomeworkProcessResult.setUserId(baseHomeworkProcessResult.getUserId());
        newHomeworkProcessResult.setQuestionId(baseHomeworkProcessResult.getQuestionId());
        newHomeworkProcessResult.setQuestionDocId(baseHomeworkProcessResult.getQuestionDocId());
        newHomeworkProcessResult.setQuestionVersion(baseHomeworkProcessResult.getQuestionVersion());
        newHomeworkProcessResult.setStandardScore(baseHomeworkProcessResult.getStandardScore());
        newHomeworkProcessResult.setScore(baseHomeworkProcessResult.getScore());
        newHomeworkProcessResult.setActualScore(baseHomeworkProcessResult.getActualScore());
        newHomeworkProcessResult.setGrasp(baseHomeworkProcessResult.getGrasp());
        newHomeworkProcessResult.setSubGrasp(baseHomeworkProcessResult.getSubGrasp());
        newHomeworkProcessResult.setUserAnswers(baseHomeworkProcessResult.getUserAnswers());
        newHomeworkProcessResult.setScorePercent(baseHomeworkProcessResult.getScorePercent());
        newHomeworkProcessResult.setSubScore(baseHomeworkProcessResult.getSubScore());
        newHomeworkProcessResult.setDuration(baseHomeworkProcessResult.getDuration());
        newHomeworkProcessResult.setSchoolLevel(baseHomeworkProcessResult.getSchoolLevel());
        newHomeworkProcessResult.setSubject(baseHomeworkProcessResult.getSubject());
        newHomeworkProcessResult.setObjectiveConfigType(baseHomeworkProcessResult.getObjectiveConfigType());
        newHomeworkProcessResult.setClientType(baseHomeworkProcessResult.getClientType());
        newHomeworkProcessResult.setClientName(baseHomeworkProcessResult.getClientName());
        newHomeworkProcessResult.setAdditions(baseHomeworkProcessResult.getAdditions());
        newHomeworkProcessResult.setNeedCorrect(baseHomeworkProcessResult.getNeedCorrect());
        newHomeworkProcessResult.setFiles(baseHomeworkProcessResult.getFiles());
        newHomeworkProcessResult.setReview(baseHomeworkProcessResult.getReview());
        newHomeworkProcessResult.setCorrectType(baseHomeworkProcessResult.getCorrectType());
        newHomeworkProcessResult.setCorrection(baseHomeworkProcessResult.getCorrection());
        newHomeworkProcessResult.setTeacherMark(baseHomeworkProcessResult.getTeacherMark());
        newHomeworkProcessResult.setCategoryId(baseHomeworkProcessResult.getCategoryId());
        newHomeworkProcessResult.setPracticeId(baseHomeworkProcessResult.getPracticeId());
        newHomeworkProcessResult.setPictureBookId(baseHomeworkProcessResult.getPictureBookId());
        newHomeworkProcessResult.setVoiceEngineType(baseHomeworkProcessResult.getVoiceEngineType());
        newHomeworkProcessResult.setVoiceCoefficient(baseHomeworkProcessResult.getVoiceCoefficient());
        newHomeworkProcessResult.setVoiceMode(baseHomeworkProcessResult.getVoiceMode());
        newHomeworkProcessResult.setVoiceScoringMode(baseHomeworkProcessResult.getVoiceScoringMode());
        newHomeworkProcessResult.setVest(baseHomeworkProcessResult.getVest());
        newHomeworkProcessResult.setSt(baseHomeworkProcessResult.getSt());
        newHomeworkProcessResult.setOralDetails(baseHomeworkProcessResult.getOralDetails());
        newHomeworkProcessResult.setAppOralScoreLevel(baseHomeworkProcessResult.getAppOralScoreLevel());
        newHomeworkProcessResult.setOralAddition(baseHomeworkProcessResult.getOralAddition());
        newHomeworkProcessResult.setVideoId(baseHomeworkProcessResult.getVideoId());
        newHomeworkProcessResult.setQuestionBoxId(baseHomeworkProcessResult.getQuestionBoxId());
        newHomeworkProcessResult.setQuestionBoxType(baseHomeworkProcessResult.getQuestionBoxType());
        newHomeworkProcessResult.setDubbingId(baseHomeworkProcessResult.getDubbingId());
        newHomeworkProcessResult.setOcrMentalImageDetail(baseHomeworkProcessResult.getOcrMentalImageDetail());
        newHomeworkProcessResult.setOcrDictationImageDetail(baseHomeworkProcessResult.getOcrDictationImageDetail());
        newHomeworkProcessResult.setCourseId(baseHomeworkProcessResult.getCourseId());
        newHomeworkProcessResult.setWordTeachModuleType(baseHomeworkProcessResult.getWordTeachModuleType());
        return newHomeworkProcessResult;
    }
}
