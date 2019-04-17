/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.AfentiElfLoader;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionSource;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.*;

@Named
@ExposeService(interfaceClass = AfentiElfLoader.class)
public class AfentiElfLoaderImpl extends UtopiaAfentiSpringBean implements AfentiElfLoader {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Override
    public List<WrongQuestionLibrary> loadAndUpdateWrongQuestionLibraryByUserIdAndSubject(Long studentId, Subject subject, AfentiWrongQuestionStateType type) {
        return loadOrUpdateWrongQuestionLibraryByUserIdAndSubject(studentId, subject, type,  true, 300);
    }

    @Override
    public List<WrongQuestionLibrary> loadWrongQuestionLibraryByUserIdAndSubject(Long studentId, Subject subject) {
        return loadWrongQuestionLibraryList(studentId, subject, null);
    }

    private List<WrongQuestionLibrary> loadOrUpdateWrongQuestionLibraryByUserIdAndSubject(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType, boolean update, int limmit) {

        List<WrongQuestionLibrary> questions = loadWrongQuestionLibraryList(studentId, subject, stateType);

        List<WrongQuestionLibrary> realList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(questions)) {
            // 修改删除或者升级的题目
            List<String> allDocIds = new ArrayList<>();
            List<WrongQuestionLibrary> questionList = questions.subList(0, Math.min(limmit, questions.size()));
            questionList.forEach(e -> {
                allDocIds.add(StringUtils.substringBefore(e.getEid(), "-"));
            });

            Map<String, NewQuestion> allQs = loadQuestionsByDocIds(allDocIds);
            Set<String> newIds = allQs.values().stream().map(n -> WrongQuestionLibrary.generateId(studentId, subject, n.getId())).collect(Collectors.toSet());
            Map<String, WrongQuestionLibrary> newLibrary = wrongQuestionLibraryDao.loads(newIds);

            Set<String> disableIds = new HashSet<>();
            for (WrongQuestionLibrary question : questionList) {
                String docId = StringUtils.substringBefore(question.getEid(), "-");

                NewQuestion newQuestion = allQs.get(docId);
                if (newQuestion == null) {
                    disableIds.add(question.getId());
                    continue;
                }

                String newId = WrongQuestionLibrary.generateId(studentId, subject, newQuestion.getId());
                if (!StringUtils.equals(newId, question.getId())) {
                    disableIds.add(question.getId());

                    WrongQuestionLibrary history = newLibrary.get(newId);
                    if (history != null) {
                        continue;
                    }

                    WrongQuestionLibrary questionLibrary = new WrongQuestionLibrary();
                    questionLibrary.setId(newId);
                    questionLibrary.setEid(newQuestion.getId());
                    questionLibrary.setUserId(studentId);
                    questionLibrary.setCreateAt(question.getCreateAt());
                    questionLibrary.setUpdateAt(question.getUpdateAt());
                    questionLibrary.setDisabled(false);
                    questionLibrary.setSource(question.getSource());
                    questionLibrary.setState(question.getState());
                    questionLibrary.setSubject(question.getSubject());

                    if (update) {
                        wrongQuestionLibraryDao.upsert(questionLibrary);
                    }

                    realList.add(questionLibrary);
                    continue;
                }
                realList.add(question);
            }

            if (update && CollectionUtils.isNotEmpty(disableIds)) {
                List<String> disableIdList = new ArrayList<>(disableIds);
                int size = disableIdList.size();
                List<String> disableIdSubList = disableIdList.subList(0, Math.min(10, size));
                wrongQuestionLibraryDao.disableLibrary(disableIdSubList);
            }
        }

        return realList;
    }

    private List<WrongQuestionLibrary> loadWrongQuestionLibraryList(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType) {
        List<WrongQuestionLibrary> questions = afentiLoader.loadWrongQuestionLibraryByUserIdAndSubject(studentId, subject)
                .stream()
                .filter(Objects::nonNull)
                .filter(l -> l.getState() != null)
                .filter(l -> (StringUtils.equals(l.getSource(), AfentiType.学习城堡.name()) ||
                        StringUtils.equals(l.getSource(), StudyType.homework.name())))
                .collect(Collectors.toList());

        if (stateType != null) {
            questions = filterByType(questions, stateType);
        }
        return questions;
    }

    private List<WrongQuestionLibrary> filterByType(List<WrongQuestionLibrary> questions, AfentiWrongQuestionStateType stateType) {
        List<WrongQuestionLibrary> result = new ArrayList<>();
        switch (stateType) {
            case rescued:
                result.addAll(questions.stream()
                        .filter(e -> e.getState() == INCORRECT2MASTER || e.getState() == INCORRECT2SPENDING)
                        .filter(e -> StringUtils.isNotBlank(e.getSeid()))
                        .collect(Collectors.toList()));
                break;
            case incorrect:
                result.addAll(questions.stream()
                        .filter(e -> e.getState() == INCORRECT)
                        .collect(Collectors.toList()));
                break;
            case similar:
                result.addAll(questions.stream()
                        .filter(e -> e.getState() == INCORRECT2CORRECT)
                        .filter(e -> StringUtils.isNotBlank(e.getSeid()))
                        .filter(e -> !StringUtils.equals(e.getSeid(), UtopiaAfentiConstants.NO_SIMILAR_QUESTION))
                        .collect(Collectors.toList()));
                break;
            default:
                break;
        }

        return result;
    }

    private List<WrongQuestionLibrary> filterBySource(List<WrongQuestionLibrary> questions, AfentiWrongQuestionSource source) {
        List<WrongQuestionLibrary> result = new ArrayList<>();
        switch (source) {
            case homework:
                result.addAll(questions.stream()
                        .filter(l -> StringUtils.equals(l.getSource(), StudyType.homework.name()))
                        .collect(Collectors.toList()));
                break;
            case castle:
                result.addAll(questions.stream()
                        .filter(l -> StringUtils.equals(l.getSource(), AfentiType.学习城堡.name()))
                        .collect(Collectors.toList()));
                break;
            default:
                break;
        }

        return result;
    }

    @Override
    public Map<String, NewQuestion> loadQuestionsByDocIds(List<String> docIds) {
        return loadQuestionsByDocIds(docIds, 500);
    }

    @Override
    public Map<String, NewQuestion> loadQuestionsByDocIds(List<String> docIds, int step) {
        Map<String, NewQuestion> result = new HashMap<>();
        int size = docIds.size();
        int index = 0;
        do{
            List<String> docIdList = docIds.subList(index, Math.min(index + step, size));
            Map<String, NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds0(docIdList);
            result.putAll(questions);
            index += step;
        } while (index < size);

        return result;
    }
}
