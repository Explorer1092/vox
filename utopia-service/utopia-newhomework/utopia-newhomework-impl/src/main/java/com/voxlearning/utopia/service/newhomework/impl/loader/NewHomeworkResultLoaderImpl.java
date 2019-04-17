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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkResultLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.ReadReciteStandardRate;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultAnswerHBase;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewhomeworkIntegralCalculator;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultAnswerDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultExtendedInfoDao;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkResultAnswerHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkResultHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkHBaseHelper;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = NewHomeworkResultLoader.class)
@ExposeService(interfaceClass = NewHomeworkResultLoader.class)
public class NewHomeworkResultLoaderImpl implements NewHomeworkResultLoader {

    private static final int PAGE_SIZE = 1000;

    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private SubHomeworkResultDao subHomeworkResultDao;
    @Inject
    private SubHomeworkResultAnswerDao subHomeworkResultAnswerDao;
    @Inject
    private DoHomeworkProcessor doHomeworkProcessor;
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject private HomeworkResultHBasePersistence homeworkResultHBasePersistence;
    @Inject private HomeworkResultAnswerHBasePersistence homeworkResultAnswerHBasePersistence;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Inject private SubHomeworkResultExtendedInfoDao subHomeworkResultExtendedInfoDao;

    @Override
    public Map<String, NewHomeworkResult> loads(Collection<String> ids, boolean needAnswer) {
        Map<String, NewHomeworkResult> newHomeworkResultMap = new HashMap<>();
        Set<String> subIds = new HashSet<>();
        for (String id : ids) {
            if (StringUtils.isBlank(id)) return Collections.emptyMap();
            String[] segments = StringUtils.split(id, "-");
            if (segments.length != 4) return Collections.emptyMap();
            String hid = segments[2];
            if (NewHomeworkUtils.isSubHomework(hid) || NewHomeworkUtils.isShardHomework(hid)) {
                subIds.add(id);
            }
        }

        if (CollectionUtils.isNotEmpty(subIds)) {
            Map<String, SubHomeworkResult> subHomeworkResultMap = loadSubHomeworkResults(subIds);
            List<String> homeworkIds = subHomeworkResultMap.values().stream().map(SubHomeworkResult::getHomeworkId).collect(Collectors.toList());
            Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkIds);
            for (String id : subHomeworkResultMap.keySet()) {
                SubHomeworkResult subHomeworkResult = subHomeworkResultMap.get(id);
                if (needAnswer) {
                    newHomeworkResultMap.put(id, HomeworkTransform.SubHomeworkResultToNew(subHomeworkResultMap.get(id), loadSubHomeworkResultAnswers(initSubHomeworkResultAnswerIds(newHomeworkMap.get(subHomeworkResult.getHomeworkId()), subHomeworkResult.getUserId())).values()));
                } else {
                    newHomeworkResultMap.put(id, HomeworkTransform.SubHomeworkResultToNew(subHomeworkResultMap.get(id), Collections.emptyList()));
                }
            }
        }
        return newHomeworkResultMap;
    }

    @Override
    public NewHomeworkResult loadNewHomeworkResult(NewHomework.Location location, Long userId, boolean needAnswer) {
        if (location == null || userId == null) {
            return null;
        }
        NewHomework newHomework = newHomeworkLoader.load(location.getId());
        if (newHomework == null) {
            return null;
        }
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
            if (needAnswer) {
                List<String> subHomeworkResultAnswerIds = initSubHomeworkResultAnswerIds(newHomework, userId);
                return HomeworkTransform.SubHomeworkResultToNew(loadSubHomeworkResult(id.toString()), loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds).values());
            } else {
                return HomeworkTransform.SubHomeworkResultToNew(loadSubHomeworkResult(id.toString()), Collections.emptyList());
            }
        } else {
            return null;
        }
    }

    public Map<ObjectiveConfigType, List<String>> initSubHomeworkResultAnswerIdsMap(NewHomework newHomework, Long userId) {
        List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
        if (CollectionUtils.isEmpty(practices)) {
            return Collections.emptyMap();
        }
        Set<ObjectiveConfigType> typeSet = practices.stream().map(NewHomeworkPracticeContent::getType).collect(Collectors.toSet());
        //该作业形式没有题, 只有视频课程
        typeSet.remove(ObjectiveConfigType.ORAL_INTERVENTIONS);
        if (CollectionUtils.isEmpty(typeSet)) {
            return Collections.emptyMap();
        }
        Map<ObjectiveConfigType, List<String>> resultMap = new HashMap<>();
        typeSet.forEach(o -> {
            List<String> values = fetchSubHomeworkResultAnswerIdsByType(newHomework, userId, Collections.singleton(o));
            if (CollectionUtils.isNotEmpty(values)) {
                resultMap.put(o, values);
            }
        });
        return resultMap;
    }

    //初始化SubHomeworkResultAnswerIds
    @Override
    public List<String> initSubHomeworkResultAnswerIds(NewHomework newHomework, Long userId) {
        List<String> subHomeworkResultAnswerIds = new ArrayList<>();
        processSubHomeworkResultAnswerIds(newHomework.processSubHomeworkResultAnswerIds(), subHomeworkResultAnswerIds, newHomework, userId);
        return subHomeworkResultAnswerIds;
    }

    private void processSubHomeworkResultAnswerIds(List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs, List<String> subHomeworkResultAnswerIds, NewHomework newHomework, Long userId) {
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
            subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, userId));
        }
    }

    public List<String> fetchSubHomeworkResultAnswerIdsByType(NewHomework newHomework, Long userId, Set<ObjectiveConfigType> type) {
        List<String> subHomeworkResultAnswerIds = new ArrayList<>();
        processSubHomeworkResultAnswerIds(newHomework.processSubHomeworkResultAnswerIdsByObjectConfigType(type), subHomeworkResultAnswerIds, newHomework, userId);
        return subHomeworkResultAnswerIds;
    }

    @Override
    public Map<Long, NewHomeworkResult> loadNewHomeworkResult(NewHomework.Location location, Collection<Long> userIds, boolean needAnswer) {
        if (location == null || CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(location.getId());
        if (newHomework == null) {
            return Collections.emptyMap();
        }
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        Set<String> ids = userIds.stream().map(userId -> new NewHomeworkResult.ID(day, location.getSubject(),
                location.getId(), userId.toString()).toString()).collect(Collectors.toSet());
        Map<Long, NewHomeworkResult> newHomeworkResultMap = new HashMap<>();
        if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
            if (needAnswer) {
                loadSubHomeworkResults(ids).values().forEach(nh -> newHomeworkResultMap.put(nh.userId, HomeworkTransform.SubHomeworkResultToNew(nh, loadSubHomeworkResultAnswers(initSubHomeworkResultAnswerIds(newHomework, nh.getUserId())).values())));
            } else {
                loadSubHomeworkResults(ids).values().forEach(nh -> newHomeworkResultMap.put(nh.userId, HomeworkTransform.SubHomeworkResultToNew(nh, Collections.emptyList())));
            }
        }
        return newHomeworkResultMap;
    }

    @Override
    public List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId) {
        if (CollectionUtils.isEmpty(locations) || userId == null) {
            return Collections.emptyList();
        }
        Set<String> hids = new HashSet<>();

        Set<String> subIds = new HashSet<>();
        locations.forEach(location -> {
            hids.add(location.getId());
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
                subIds.add(new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString()).toString());
            }
        });
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(hids);
        List<NewHomeworkResult> newHomeworkResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subIds)) {
            loadSubHomeworkResults(subIds).values().forEach(nh -> newHomeworkResults.add(HomeworkTransform.SubHomeworkResultToNew(nh, loadSubHomeworkResultAnswers(initSubHomeworkResultAnswerIds(newHomeworkMap.get(nh.getHomeworkId()), nh.getUserId())).values())));
        }
        return newHomeworkResults;
    }

    @Override
    public List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId, boolean needAnswer) {
        if (CollectionUtils.isEmpty(locations) || userId == null) {
            return Collections.emptyList();
        }
        Set<String> hids = new HashSet<>();
        Set<String> subIds = new HashSet<>();
        locations.forEach(location -> {
            hids.add(location.getId());
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            if (NewHomeworkUtils.isSubHomework(location.getId()) || NewHomeworkUtils.isShardHomework(location.getId())) {
                subIds.add(new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString()).toString());
            }
        });
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(hids);
        List<NewHomeworkResult> newHomeworkResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subIds)) {
            if (needAnswer) {
                loadSubHomeworkResults(subIds).values().forEach(nh -> newHomeworkResults.add(HomeworkTransform.SubHomeworkResultToNew(nh, loadSubHomeworkResultAnswers(initSubHomeworkResultAnswerIds(newHomeworkMap.get(nh.getHomeworkId()), nh.getUserId())).values())));
            } else {
                loadSubHomeworkResults(subIds).values().forEach(nh -> newHomeworkResults.add(HomeworkTransform.SubHomeworkResultToNew(nh, Collections.emptyList())));
            }
        }
        return newHomeworkResults;
    }

    /**
     * 获取某个用户在某次作业中包含的需要进行批改的试题
     * TODO 优化 xuesong.zhang
     *
     * @param newHomework 作业
     * @return <作业形式, List<题id,processId>>
     */
    public Map<ObjectiveConfigType, Map<String, String>> getCorrectQuestions(NewHomework newHomework, NewHomeworkResult newHomeworkResult) {


        // 获取作业中的主观试题,匹配用户的主观题
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> map = newHomework.findPracticeContents();
        Map<ObjectiveConfigType, Map<String, String>> result = new HashMap<>();

        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : map.entrySet()) {
            if (entry.getKey() == ObjectiveConfigType.READ_RECITE) {
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> tempMap1 = newHomeworkResult.getPractices();
                // 并不是所有的作业形式学生都会做
                if (MapUtils.isNotEmpty(tempMap1) && tempMap1.get(entry.getKey()) != null && tempMap1.get(entry.getKey()).isFinished()) {
                    NewHomeworkResultAnswer answer = tempMap1.get(entry.getKey());
                    Map<String, String> tempMap = answer.getAnswers();

                    // 包含主观作答试题的试题id
                    List<String> list = entry.getValue().processNewHomeworkQuestion(false).stream()
                            .filter(NewHomeworkQuestion::isSubjectiveQuestion)
                            .map(NewHomeworkQuestion::getQuestionId)
                            .collect(Collectors.toList());

                    // 遍历出学生在某种作业形式中都做了哪些
                    Map<String, String> studentMap = new HashMap<>();
                    tempMap.entrySet().stream()
                            .filter(entry1 -> list.contains(entry1.getKey()))
                            .forEach(entry1 -> studentMap.put(entry1.getKey(), entry1.getValue()));

                    result.put(entry.getKey(), studentMap);
                }
            } else {
                result.put(entry.getKey(), Collections.emptyMap());
            }
        }
        return result;
    }


    /**
     * @deprecated use {@link com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl#finishCorrect(NewHomework.Location, Long, ObjectiveConfigType, Boolean, Boolean)} instead.
     */
    @Deprecated
    public Boolean finishCorrect(NewHomework.Location location,
                                 Long userId,
                                 ObjectiveConfigType type,
                                 Boolean finishCorrect,
                                 Boolean allFinishCorrect) {
        return subHomeworkResultDao.finishCorrect(HomeworkTransform.NewHomeworkLocationToSub(location), userId, type, finishCorrect, allFinishCorrect) != null;
    }

    @Override
    public Map<String, NewHomeworkResult> findByHomework(NewHomework newHomework) {
        //取学生结果数据
        return findNewHomeworkResultForSubHomework(newHomework);
    }

    private Map<String, NewHomeworkResult> findNewHomeworkResultForSubHomework(NewHomework newHomework) {
        Map<String, NewHomeworkResult> newHomeworkResultMap = new HashMap<>();
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_1");
        Set<SubHomeworkResult.Location> locations = findSubResultLocationsByHomework(newHomework);
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_2");
        List<String> resultIds = new ArrayList<>();
        List<String> allResultAnswerIds = new ArrayList<>();
        Map<Long, List<String>> userResultAnswerIdsMap = new HashMap<>();
        if (locations != null) {
            for (SubHomeworkResult.Location location : locations) {
                Long userId = location.getUserId();
                resultIds.add(location.getId());
                List<String> resultAnswerIds = initSubHomeworkResultAnswerIds(newHomework, userId);
                allResultAnswerIds.addAll(resultAnswerIds);
                userResultAnswerIdsMap.put(userId, resultAnswerIds);
            }
        }
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_3");
        Map<String, SubHomeworkResult> subHomeworkResultMap = loadSubHomeworkResults(resultIds);
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_4");
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = loadSubHomeworkResultAnswers(allResultAnswerIds);
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_5");
        if (subHomeworkResultMap != null) {
            for (SubHomeworkResult subHomeworkResult : subHomeworkResultMap.values()) {
                List<String> userResultAnswerIds = userResultAnswerIdsMap.get(subHomeworkResult.getUserId());
                List<SubHomeworkResultAnswer> subHomeworkResultAnswers = new ArrayList<>();
                if (userResultAnswerIds != null) {
                    for (String id : userResultAnswerIds) {
                        if (subHomeworkResultAnswerMap != null) {
                            SubHomeworkResultAnswer subHomeworkResultAnswer = subHomeworkResultAnswerMap.get(id);
                            if (subHomeworkResultAnswer != null) {
                                subHomeworkResultAnswers.add(subHomeworkResultAnswer);
                            }
                        }
                    }
                }
                newHomeworkResultMap.put(subHomeworkResult.getId(), HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, subHomeworkResultAnswers));
            }
        }
        FlightRecorder.dot("findNewHomeworkResultForSubHomework_6");
        return newHomeworkResultMap;
    }

    @Override
    public Map<String, NewHomeworkResult> findByHomeworkForReport(NewHomework newHomework) {
        Map<String, Set<NewHomeworkResult>> resultMap = findByHomeworksForReport(Collections.singleton(newHomework));
        Map<String, NewHomeworkResult> newHomeworkResultMaps = new LinkedHashMap<>();
        if (resultMap.containsKey(newHomework.getId())) {
            resultMap.get(newHomework.getId())
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(result -> newHomeworkResultMaps.put(result.getId(), result));
        }
        return newHomeworkResultMaps;
    }

    @Override
    public Map<String, Set<NewHomeworkResult>> findByHomeworksForReport(Collection<NewHomework> newHomeworks) {
        if (CollectionUtils.isEmpty(newHomeworks)) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> subMap = new LinkedHashMap<>();

        for (NewHomework newHomework : newHomeworks) {
            if (newHomework != null) {
                String homeworkId = newHomework.getId();
                if (NewHomeworkUtils.isSubHomework(homeworkId) || NewHomeworkUtils.isShardHomework(homeworkId)) {
                    Set<String> ids = findSubResultLocationsByHomework(newHomework)
                            .stream()
                            .map(SubHomeworkResult.Location::getId)
                            .collect(Collectors.toSet());
                    subMap.put(newHomework.getId(), ids);
                }
            }
        }
        Set<String> subIds = subMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<String, SubHomeworkResult> subHomeworkResultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(subIds)) {
            subHomeworkResultMap = loadSubHomeworkResults(subIds);
        }
        Map<String, Set<NewHomeworkResult>> result = new LinkedHashMap<>();
        for (NewHomework newHomework : newHomeworks) {
            if (newHomework != null) {
                String homeworkId = newHomework.getId();
                if (NewHomeworkUtils.isSubHomework(homeworkId) || NewHomeworkUtils.isShardHomework(homeworkId)) {
                    Set<String> ids = subMap.get(homeworkId);
                    if (CollectionUtils.isNotEmpty(ids)) {
                        Set<NewHomeworkResult> newHomeworkResultSet = new HashSet<>();
                        for (String id : ids) {
                            SubHomeworkResult subHomeworkResult = subHomeworkResultMap.get(id);
                            if (subHomeworkResult != null) {
                                NewHomeworkResult newHomeworkResult = HomeworkTransform.SubHomeworkResultToNew(subHomeworkResult, Collections.emptyList());
                                newHomeworkResultSet.add(newHomeworkResult);
                            }
                        }
                        result.put(homeworkId, newHomeworkResultSet);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Integer homeworkIntegral(boolean repair, NewHomeworkResult newHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultsMap = newHomeworkResult.getPractices();
        int integral = 0;
        for (ObjectiveConfigType oct : resultsMap.keySet()) {
            NewHomeworkResultAnswer na = resultsMap.get(oct);
            if(na != null && na.getFinishAt() != null){
                if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(oct)) {
                    /**纸质口算 ： 按时完成，给5学豆 ；补做完成，得1学豆 **/
                    if (repair) { //
                        integral = integral + 1;
                    } else {
                        integral = integral + 5;
                    }
                    continue;
                }
                // 引擎打分的课文读背通过达标率奖励学豆
                if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(oct) || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(oct) ) {
                    // 平均达标率
                    Double precision = na.getScore();
                    // 补做完成，且达标比例大于等于60%，得1学豆；否则为0学豆
                    if (repair) {
                        if (precision >= ReadReciteStandardRate.SIXTY_PERCENT.getPrecision()) {
                            ++integral;
                        }
                    } else {
                        if (precision >= ReadReciteStandardRate.TWENTY_PERCENT.getPrecision()
                                && precision < ReadReciteStandardRate.FORTY_PERCENT.getPrecision()) {
                            ++integral;
                        } else if (precision >= ReadReciteStandardRate.FORTY_PERCENT.getPrecision()
                                && precision < ReadReciteStandardRate.SIXTY_PERCENT.getPrecision()) {
                            integral = integral + 2;
                        } else if (precision >= ReadReciteStandardRate.SIXTY_PERCENT.getPrecision()
                                && precision < ReadReciteStandardRate.EIGHTY_PERCENT.getPrecision()) {
                            integral = integral + 3;
                        } else if (precision >= ReadReciteStandardRate.EIGHTY_PERCENT.getPrecision()
                                && precision < ReadReciteStandardRate.ONE_HUNDRED_PERCENT.getPrecision()) {
                            integral = integral + 4;
                        } else if (precision >= ReadReciteStandardRate.ONE_HUNDRED_PERCENT.getPrecision()) {
                            integral = integral + 5;
                        }
                    }

                } else {
                    int score = 100;
                    if (na.processScore(oct) != null) {
                        score = na.processScore(oct);
                    }
                    if (repair) {
                        integral += NewhomeworkIntegralCalculator.calculateStudentRepairNewHomeworkIntegralAmount(score, oct);
                    } else {
                        integral += NewhomeworkIntegralCalculator.calculateStudentNewHomeworkIntegralAmount(score, oct);
                    }
                }
            }

        }
        return integral;
    }

    @Override
    public Integer generateFinishHomeworkActivityIntegral(Integer integral, NewHomework newHomework, Integer regionCode) {
        return doHomeworkProcessor.generateFinishHomeworkActivityIntegral(integral, newHomework, regionCode);
    }

    @Override
    public Map<Long, Map<String, Integer>> getCurrentMonthHomeworkRankByGroupId(Long studentId) {
        //很老的学生端的老接口停止输出数据。
        return Collections.emptyMap();
    }

    public Map<String, SubHomeworkResultAnswer> loadSubHomeworkResultAnswers(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = new HashMap<>();
        Set<String> hBaseIds = new HashSet<>();
        Set<String> subIds = new HashSet<>();
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        for (String id : ids) {
            if (HomeworkHBaseHelper.isHBaseResultAnswerId(id, isOpen)) {
                hBaseIds.add(id);
            } else {
                subIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(hBaseIds)) {
            Map<String, HomeworkResultAnswerHBase> resultAnswerHBaseMap = homeworkResultAnswerHBasePersistence.loads(hBaseIds);
            resultAnswerHBaseMap.forEach((k, v) -> subHomeworkResultAnswerMap.put(k, HomeworkTransform.HomeworkResultAnswerHBaseToSub(v)));
        }
        if (CollectionUtils.isNotEmpty(subIds)) {
            Map<String, SubHomeworkResultAnswer> resultAnswerMap = loadsByIds(subIds);
            subHomeworkResultAnswerMap.putAll(resultAnswerMap);
        }
        Map<String, SubHomeworkResultAnswer> answerMap = new LinkedHashMap<>();
        for (String id : ids){
            if(subHomeworkResultAnswerMap.containsKey(id)){
                answerMap.put(id,subHomeworkResultAnswerMap.get(id));
            }
        }
        return answerMap;
    }

    private Map<String, SubHomeworkResultAnswer> loadsByIds(Collection<String> ids) {
        Set<String> idSet = CollectionUtils.toLinkedHashSet(ids);
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }
        List<List<String>> idsList = NewHomeworkUtils.splitList(new ArrayList<>(idSet), PAGE_SIZE);
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = new LinkedHashMap<>();
        for (List<String> idList : idsList) {
            Map<String, SubHomeworkResultAnswer> result = subHomeworkResultAnswerDao.loads(idList);
            if (MapUtils.isNotEmpty(result)) {
                subHomeworkResultAnswerMap.putAll(result);
            }
        }
        return subHomeworkResultAnswerMap;
    }

    public SubHomeworkResult loadSubHomeworkResult(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        if (HomeworkHBaseHelper.isHBaseResultId(id, isOpen)) {
            HomeworkResultHBase homeworkResultHBase = homeworkResultHBasePersistence.load(id);
            return HomeworkTransform.HomeworkResultHBaseToSub(homeworkResultHBase);
        }
        return subHomeworkResultDao.load(id);
    }

    public Map<String, SubHomeworkResult> loadSubHomeworkResults(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkResult> subHomeworkResultMap = new HashMap<>();
        Set<String> hBaseIds = new HashSet<>();
        Set<String> subIds = new HashSet<>();
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        for (String id : ids) {
            if (HomeworkHBaseHelper.isHBaseResultId(id, isOpen)) {
                hBaseIds.add(id);
            } else {
                subIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(hBaseIds)) {
            Map<String, HomeworkResultHBase> homeworkResultHBaseMap = homeworkResultHBasePersistence.loads(hBaseIds);
            homeworkResultHBaseMap.forEach((k, v) -> subHomeworkResultMap.put(k, HomeworkTransform.HomeworkResultHBaseToSub(v)));
        }
        if (CollectionUtils.isNotEmpty(subIds)) {
            Map<String, SubHomeworkResult> resultMap = subHomeworkResultDao.loads(subIds);
            subHomeworkResultMap.putAll(resultMap);
        }
        return subHomeworkResultMap;
    }

    public Set<SubHomeworkResult.Location> findSubResultLocationsByHomework(NewHomework newHomework) {
        if (newHomework == null) {
            return Collections.emptySet();
        }
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        if (HomeworkHBaseHelper.isHBaseHomeworkId(newHomework.getId(), isOpen)) {
            Set<Long> groupStudentIds = new HashSet<>(studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId()));
            Set<String> resultIds = groupStudentIds.stream()
                    .map(studentId -> new SubHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), studentId.toString()).toString())
                    .collect(Collectors.toSet());
            Map<String, HomeworkResultHBase> hBaseMap = homeworkResultHBasePersistence.loads(resultIds);
            Set<SubHomeworkResult.Location> result = new HashSet<>();
            if (MapUtils.isNotEmpty(hBaseMap)) {
                result = hBaseMap.values().stream().map(hBase -> HomeworkTransform.HomeworkResultHBaseToSub(hBase).toLocation()).collect(Collectors.toSet());
            }
            return result;
        } else {
            List<Long> groupStudentIds = studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
            return subHomeworkResultDao.findByHomework(day, newHomework.getSubject(), newHomework.getId(), groupStudentIds);
        }
    }

    @Override
    public SubHomeworkResultExtendedInfo loadSubHomeworkResultExtentedInfo(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }

        return subHomeworkResultExtendedInfoDao.load(id);
    }
}
