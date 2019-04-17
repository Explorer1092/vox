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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkPartLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineListenPaper;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkFinishRewardInParentAppDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.OfflineListenPaperDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/8/3
 */
@Named
@Service(interfaceClass = NewHomeworkPartLoader.class)
@ExposeService(interfaceClass = NewHomeworkPartLoader.class)
public class NewHomeworkPartLoaderImpl implements NewHomeworkPartLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private NewAccomplishmentLoaderImpl newAccomplishmentLoader;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;
    @Inject private PracticeLoaderClient practiceLoaderClient;
    @Inject private NewHomeworkFinishRewardInParentAppDao newHomeworkFinishRewardInParentAppDao;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject private OfflineListenPaperDao offlineListenPaperDao;

    @Override
    public Map<String, NewHomeworkStudyMaster> getNewHomeworkStudyMasterMap(Collection<String> newHomeworkIds) {
        return newHomeworkStudyMasterDao.loads(newHomeworkIds);
    }

    @Override
    public Map<String, List<String>> getBasicAppVoiceUrl(NewHomework newHomework, Long userId) {
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userId, true);
        List<String> processIds = new ArrayList<>();
        if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
            newHomeworkResult.getPractices().keySet().stream().filter(ObjectiveConfigType.BASIC_APP::equals).forEach(type -> {
                NewHomeworkResultAnswer result = newHomeworkResult.getPractices().get(type);
                for (NewHomeworkResultAppAnswer answer : result.getAppAnswers().values()) {
                    processIds.addAll(answer.getAnswers().values());
                }
            });
        }
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        Set<Long> practiceIds = processResultMap.values().stream().map(NewHomeworkProcessResult::getPracticeId).collect(Collectors.toSet());
        Map<Long, PracticeType> practiceTypeMap = practiceLoaderClient.loadPractices().stream().filter(p -> practiceIds.contains(p.getId())).collect(Collectors.toMap(PracticeType::getId, Function.identity()));
        Map<String, List<String>> voiceMap = new HashMap<>();
        for (String processId : processIds) {
            NewHomeworkProcessResult processResult = processResultMap.get(processId);
            if (processResult == null) continue;
            PracticeType practiceType = practiceTypeMap.get(processResult.getPracticeId());
            if (practiceType == null || !practiceType.getNeedRecord() || !"Normal".equals(processResult.getVoiceScoringMode())) {
                continue;
            }
            String key = StringUtils.join(Arrays.asList(processResult.getLessonId(), practiceType.getCategoryId(), practiceType.getCategoryName()), "#");
            List<String> voices = voiceMap.get(key);
            if (voices == null) {
                voices = new ArrayList<>();

            }
            for (List<NewHomeworkProcessResult.OralDetail> oralDetails : processResult.getOralDetails()) {
                for (NewHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                    if (StringUtils.isBlank(oralDetail.getAudio())) {
                        continue;
                    }
                    voices.add(VoiceEngineTypeUtils.getAudioUrl(oralDetail.getAudio(), processResult.getVoiceEngineType()));
                }
            }

            if (CollectionUtils.isNotEmpty(voices)) {
                voiceMap.put(key, voices);
            }
        }
        return voiceMap;
    }

    @Override
    public NewHomeworkFinishRewardInParentApp getRewardInParentApp(Long userId) {
        return newHomeworkFinishRewardInParentAppDao.load(userId);
    }

    @Override
    public MapMessage updateTimeoutInteger(Long userId, String homeworkId) {
        return newHomeworkFinishRewardInParentAppDao.updateTimeoutInteger(userId, homeworkId);
    }

    @Override
    public MapMessage updateBeforeReceivedInteger(Long userId, String homeworkId) {
        return newHomeworkFinishRewardInParentAppDao.updateBeforeReceivedInteger(userId, homeworkId);
    }

    @Override
    public MapMessage getStudentHomeworkProgress(Long studentId, Subject subject, String homeworkId) {
        if (StringUtils.isBlank(homeworkId)) {
            List<Group> studentGroups = raikouSystem.loadStudentGroups(studentId)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.getId() != null)
                    .collect(Collectors.toList());

            Set<Long> groupIds = studentGroups.stream()
                    .map(Group::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // 所有班级的最新教材
            NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                    .subject(subject)
                    .toList()
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                    .findFirst()
                    .orElse(null);
            if (newClazzBookRef != null && newClazzBookRef.getUpdateDatetime() != null && DateUtils.dayDiff(new Date(), newClazzBookRef.getUpdateDatetime()) <= 15) {
                return MapMessage.successMessage()
                        .add("bookId", newClazzBookRef.getBookId())
                        .add("unitId", newClazzBookRef.getUnitId())
                        .add("sectionId", newClazzBookRef.getSectionID())
                        .add("updateDatetime", newClazzBookRef.getUpdateDatetime());

            }
        } else {
            NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
            String bookId = null;
            String unitId = null;
            String sectionId = null;
            if (newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
                for (List<NewHomeworkBookInfo> newHomeworkBookInfos : newHomeworkBook.getPractices().values()) {
                    for (NewHomeworkBookInfo newHomeworkBookInfo : newHomeworkBookInfos) {
                        bookId = newHomeworkBookInfo.getBookId();
                        unitId = newHomeworkBookInfo.getUnitId();
                        if (StringUtils.isNoneBlank(bookId) && StringUtils.isNoneBlank(unitId)) {
                            sectionId = newHomeworkBookInfo.getSectionId();
                            break;
                        }
                    }
                    if (StringUtils.isNoneBlank(bookId) && StringUtils.isNoneBlank(unitId)) {
                        break;
                    }
                }
                if (StringUtils.isNoneBlank(bookId) && StringUtils.isNoneBlank(unitId)) {
                    return MapMessage.successMessage()
                            .add("bookId", bookId)
                            .add("unitId", unitId)
                            .add("sectionId", sectionId)
                            .add("updateDatetime", newHomeworkBook.getUpdateAt());
                }
            }
        }
        return MapMessage.errorMessage("没有最新数据");
    }

    @Override
    public List<NewHomework.Location> loadNewHomeworkByClazzGroupId(Collection<Long> groupIds, Date startDate, Date endDate) {
        Map<Long, List<NewHomework.Location>> newHomeworkLocationMap = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds);
        if (MapUtils.isEmpty(newHomeworkLocationMap)) {
            return Collections.emptyList();
        }
        return newHomeworkLocationMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(o -> (o.getCreateTime() >= startDate.getTime() && o.getCreateTime() <= endDate.getTime()))
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, OfflineListenPaper> findOfflineListenPaperByIds(Collection<String> ids) {
        return offlineListenPaperDao.loads(ids);
    }

    @Override
    public MapMessage getTeacherHomeworkProgress(Long teacherId, Subject subject) {
        List<GroupTeacherMapper> groupTeacherMappers = groupLoaderClient.loadTeacherGroups(teacherId, false);
        Map<Long, Map<String, Object>> groupHomeworkProgress = new HashMap<>();

        Set<Long> groupIds = groupTeacherMappers.stream()
                .map(GroupMapper::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 所有班级的教材
        Map<Long, List<NewClazzBookRef>> newClazzBookRefMap = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                .subject(subject)
                .toList()
                .stream().collect(Collectors.groupingBy(NewClazzBookRef::getGroupId));
        for (Long groupId : newClazzBookRefMap.keySet()) {
            //班级的最新教材
            NewClazzBookRef newClazzBookRef = newClazzBookRefMap.get(groupId)
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                    .findFirst()
                    .orElse(null);
//                if (newClazzBookRef != null && newClazzBookRef.getUpdateDatetime() != null && DateUtils.dayDiff(new Date(), newClazzBookRef.getUpdateDatetime()) <= 15) {
            groupHomeworkProgress.put(groupId, MapUtils.m("bookId", newClazzBookRef.getBookId(),
                    "unitId", newClazzBookRef.getUnitId(),
                    "sectionId", newClazzBookRef.getSectionID(),
                    "updateDatetime", newClazzBookRef.getUpdateDatetime()));
//                }
        }
        if (groupHomeworkProgress.size() > 0) {
            return MapMessage.successMessage().add("homeworkProgress", groupHomeworkProgress);
        }
        return MapMessage.errorMessage("没有最新数据");
    }

    @Override
    public List<WechatHomeworkMapper> getAllHomeworkMapper(List<NewHomework.Location> newHomeworkList, Long studentId) {
        List<WechatHomeworkMapper> mapperList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(newHomeworkList)) {
            Set<String> homeworkIds = new HashSet<>();
            newHomeworkList.forEach(p -> homeworkIds.add(p.getId()));
            //这些作业学生的全部做题结果
            List<NewHomeworkResult> homeworkResultList = newHomeworkResultLoader.loadNewHomeworkResult(newHomeworkList, studentId, false);
            Map<String, NewHomeworkResult> homeworkResultMap = homeworkResultList.stream().collect(Collectors.toMap(NewHomeworkResult::getHomeworkId, Function.identity()));
            //这些作业的学生错题
            Map<String, List<Map<String, Object>>> wrongQuestionIds = newHomeworkLoader.getStudentWrongQuestionIds(studentId, null, homeworkIds);
            newHomeworkList.forEach(p -> {
                WechatHomeworkMapper mapper = new WechatHomeworkMapper();
                mapper.setCreateTime(new Date(p.getCreateTime()));
                mapper.setStartTime(new Date(p.getStartTime()));
                mapper.setChecked(p.isChecked());
                mapper.setCheckTime(new Date(p.getCheckedTime()));
                mapper.setTeacherId(p.getTeacherId());
                mapper.setEndTime(new Date(p.getEndTime()));
                mapper.setStudentId(studentId);
                mapper.setHomeworkId(p.getId());
                mapper.setSubject(p.getSubject());
                //app上現在還不需要錯題數
                //设置错题数量
                NewHomeworkResult newHomeworkResult = homeworkResultMap.get(p.getId());
                if (newHomeworkResult != null) {
                    mapper.setScore(SafeConverter.toInt(newHomeworkResult.processScore()));
                    String key = DateUtils.dateToString(new Date(p.getCreateTime()), "yyyy.MM.dd");
                    List<Map<String, Object>> mapList = wrongQuestionIds.get(key);
                    if (CollectionUtils.isNotEmpty(mapList)) {
                        Map<String, Object> homeworkWrongMap = mapList.stream().filter(map -> p.getId().equals(map.get("homeworkId"))).findFirst().orElse(null);
                        if (MapUtils.isEmpty(homeworkWrongMap)) {
                            mapper.setWrongCount(0);
                        } else {
                            mapper.setWrongCount(((Set) homeworkWrongMap.get("qid")).size());
                        }
                    }
                }
                //这两个值下面可能会重置
                mapper.setFinished(false);
                mapper.setClassMateFinishCount(0);
                NewAccomplishment newAccomplishment = newAccomplishmentLoader.loadNewAccomplishment(p);
                if (newAccomplishment != null) {
                    mapper.setClassMateFinishCount(newAccomplishment.size());
                    if (newAccomplishment.contains(studentId)) {
                        mapper.setFinished(true);
                    }
                }
                mapperList.add(mapper);
            });
        }
        return mapperList;
    }
}
