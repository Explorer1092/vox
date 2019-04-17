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

package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

/**
 * @author guoqiang.li
 * @since 2016/3/1
 */
@Getter
@Setter
@ToString
public class AssignmentRecordContext {
    Map<String, Integer> questionMap;
    Set<String> packageSet;
    Set<String> paperSet;
    Set<String> appSet;
    Set<String> pictureBookSet;
    Set<String> mentalKpIdSet;

    public AssignmentRecordContext(List<NewHomeworkPracticeContent> practiceContents) {
        questionMap = new HashMap<>();
        packageSet = new HashSet<>();
        paperSet = new HashSet<>();
        appSet = new HashSet<>();
        pictureBookSet = new HashSet<>();
        mentalKpIdSet = new HashSet<>();
        if (practiceContents != null && !practiceContents.isEmpty()) {
            practiceContents.forEach(practice -> {
                ObjectiveConfigType type = practice.getType();
                List<NewHomeworkQuestion> questions = practice.getQuestions();
                List<NewHomeworkApp> apps = practice.getApps();

                if (questions != null && !questions.isEmpty()) {
                    // 口算不记录题的使用次数，记录知识点
                    if (ObjectiveConfigType.MENTAL == type) {
                        questions.forEach(question -> {
                            String kpId = question.getKnowledgePointId();
                            if (StringUtils.isNotBlank(kpId)) {
                                mentalKpIdSet.add(kpId);
                            }
                        });
                    } else {
                        questions.forEach(question -> {
                            // 同步习题精选包,直接存精选包id
                            if (question.getQuestionBoxId() != null && question.getQuestionBoxId().trim().length() > 0) {
                                packageSet.add(question.getQuestionBoxId());
                            }
                            // 试卷,按docId存
                            String paperDocId = TeacherAssignmentRecord.id2DocId(question.getPaperId());
                            if (paperDocId != null && paperDocId.trim().length() > 0) {
                                paperSet.add(paperDocId);
                            }
                            // 试题,按docId存(这里包括了试卷和精选包里面的试题)
                            String questionDocId = TeacherAssignmentRecord.id2DocId(question.getQuestionId());
                            if (questionDocId != null && questionDocId.trim().length() > 0) {
                                if (questionMap.containsKey(questionDocId)) {
                                    questionMap.put(questionDocId, questionMap.get(questionDocId) + 1);
                                } else {
                                    questionMap.put(questionDocId, 1);
                                }
                            }
                        });
                    }
                }

                if (apps != null && !apps.isEmpty()) {
                    apps.forEach(app -> {
                        switch (type) {
                            case BASIC_APP:
                            case LS_KNOWLEDGE_REVIEW:
                            case NATURAL_SPELLING:
                                String appKey = app.getLessonId() + "-" + app.getCategoryId();
                                appSet.add(appKey);
                                break;
                            case READING:
                            case LEVEL_READINGS:
                                // 绘本，按docId存
                                String pictureBookDocId = TeacherAssignmentRecord.id2DocId(app.getPictureBookId());
                                pictureBookSet.add(pictureBookDocId);
                                break;
                            case KEY_POINTS:
                                // 重难点视频，按题存
                                List<NewHomeworkQuestion> newHomeworkQuestions = app.getQuestions();
                                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                                    for (NewHomeworkQuestion question : newHomeworkQuestions) {
                                        String questionDocId = TeacherAssignmentRecord.id2DocId(question.getQuestionId());
                                        if (questionDocId != null && questionDocId.trim().length() > 0) {
                                            if (questionMap.containsKey(questionDocId)) {
                                                questionMap.put(questionDocId, questionMap.get(questionDocId) + 1);
                                            } else {
                                                questionMap.put(questionDocId, 1);
                                            }
                                        }
                                    }
                                }
                                break;
                            case VIDEO_QUESTION:
                            case SITUATIONAL_ORAL:
                                String videoDocId = TeacherAssignmentRecord.id2DocId(app.getVideoId());
                                appSet.add(videoDocId);
                                break;
                            case DUBBING:
                            case DUBBING_WITH_SCORE:
                                String dubbingId = TeacherAssignmentRecord.id2DocId(app.getDubbingId());
                                appSet.add(dubbingId);
                                break;
                            case WORD_TEACH_AND_PRACTICE:
                                String stoneDataId = app.getStoneDataId();
                                if (CollectionUtils.isNotEmpty(app.getWordExerciseQuestions())) {
                                    packageSet.add(stoneDataId + "-" + WordTeachModuleType.WORDEXERCISE);
                                }
                                if (CollectionUtils.isNotEmpty(app.getImageTextRhymeQuestions())) {
                                    packageSet.add(stoneDataId + "-" + WordTeachModuleType.IMAGETEXTRHYME);
                                }
                                if (CollectionUtils.isNotEmpty(app.getChineseCharacterCultureCourseIds())) {
                                    packageSet.add(stoneDataId + "-" + WordTeachModuleType.CHINESECHARACTERCULTURE);
                                }
                                break;
                            case ORAL_COMMUNICATION:
                                appSet.add(app.getStoneDataId());
                                break;
                            default:
                                break;
                        }
                    });
                }
            });
        }
    }
}
