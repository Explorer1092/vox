package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.mapper.DisplayStudentHomeWorkHistoryMapper;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.CrmAudioData;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.crm.CrmUnitQuestion;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkBlackWhiteListDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages.OcrClassifyImagesPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.SelfStudyHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.*;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.GenerateSelfStudyHomeworkConfigTypes;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.NeedSelfStudyHomeworkSubjects;

/**
 * @author xuesong.zhang
 * @since 2017/1/16
 */
@Named
@Service(interfaceClass = NewHomeworkCrmLoader.class)
@ExposeService(interfaceClass = NewHomeworkCrmLoader.class)
public class NewHomeworkCrmLoaderImpl implements NewHomeworkCrmLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private SubHomeworkDao subHomeworkDao;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private PracticeServiceClient practiceServiceClient;
    @Inject private VacationHomeworkBookDao vacationHomeworkBookDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private NewHomeworkPartLoaderImpl newHomeworkPartLoader;
    @Inject private PracticeLoaderClient practiceLoaderClient;
    @Inject private NewAccomplishmentLoaderImpl newAccomplishmentLoader;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject private SelfStudyHomeworkReportDao selfStudyHomeworkReportDao;
    @Inject private VacationHomeworkLoaderImpl vacationHomeworkLoader;
    @Inject private ShardHomeworkDao shardHomeworkDao;
    @Inject private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject private HomeworkBlackWhiteListDao homeworkBlackWhiteListDao;
    @Inject private OcrClassifyImagesPersistence ocrClassifyImagesPersistence;

    @Override
    public Collection<String> findIdsByCheckedTimes(Date start, Date end) {
        return subHomeworkDao.findIdsByCheckedTimes(start, end);
    }

    @Override
    public Collection<String> findIdsByTeacherIdAndCheckedTimes(Long teacherId, Date start) {
        return subHomeworkDao.findIdsByTeacherIdAndCheckedTimes(teacherId, start);
    }

    @Override
    public Collection<NewHomework.Location> findIdsByTeacherIdAndCreateAt(Long teacherId, Date start, Date end) {
        Collection<SubHomework.Location> subList = subHomeworkDao.findIdsByTeacherIdAndCreateAt(teacherId, start, end);
        Collection<ShardHomework.Location> shardList = shardHomeworkDao.findIdsByTeacherIdAndCreateAt(teacherId, start, end);

        List<NewHomework.Location> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subList)) {
            subList.forEach(o -> {
                if (HomeworkTransform.SubHomeworkLocationToNew(o) != null) {
                    resultList.add(HomeworkTransform.SubHomeworkLocationToNew(o));
                }
            });
        }
        if (CollectionUtils.isNotEmpty(shardList)) {
            shardList.forEach(o -> {
                if (HomeworkTransform.ShardHomeworkLocationToNew(o) != null) {
                    resultList.add(HomeworkTransform.ShardHomeworkLocationToNew(o));
                }
            });
        }
        return resultList;
    }

    @Override
    public List<NewHomework.Location> findHomeworkByEndTime(Date begin, Date end) {
        Collection<SubHomework.Location> subList = subHomeworkDao.findHomeworkByEndTime(begin, end);
        Collection<ShardHomework.Location> shardList = shardHomeworkDao.findHomeworkByEndTime(begin, end);
        List<NewHomework.Location> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subList)) {
            subList.forEach(o -> {
                if (HomeworkTransform.SubHomeworkLocationToNew(o) != null) {
                    resultList.add(HomeworkTransform.SubHomeworkLocationToNew(o));
                }
            });
        }
        if (CollectionUtils.isNotEmpty(shardList)) {
            shardList.forEach(o -> {
                if (HomeworkTransform.ShardHomeworkLocationToNew(o) != null) {
                    resultList.add(HomeworkTransform.ShardHomeworkLocationToNew(o));
                }
            });
        }
        return resultList;
    }

    @Override
    public Page<NewHomework.Location> loadGroupNewHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable, boolean includeDisabled) {
        Map<Long, List<SubHomework.Location>> subLocationMap = subHomeworkDao.loadSubHomeworksByClazzGroupIdsWithTimeLimit(groupIds, startDate, endDate);
        Map<Long, List<ShardHomework.Location>> shardLocationMap = shardHomeworkDao.loadShardHomeworksByClazzGroupIdsWithTimeLimit(groupIds, startDate, endDate);

        Set<NewHomework.Location> locationSet = new HashSet<>();
        if (MapUtils.isNotEmpty(subLocationMap)) {
            subLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(o -> {
                        NewHomework.Location location = HomeworkTransform.SubHomeworkLocationToNew(o);
                        if (location != null) {
                            if (includeDisabled) {
                                locationSet.add(location);
                            } else {
                                if (!o.isDisabled()) {
                                    locationSet.add(location);
                                }
                            }
                        }
                    });
        }

        if (MapUtils.isNotEmpty(shardLocationMap)) {
            shardLocationMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(o -> {
                        NewHomework.Location location = HomeworkTransform.ShardHomeworkLocationToNew(o);
                        if (location != null) {
                            if (includeDisabled) {
                                locationSet.add(location);
                            } else {
                                if (!o.isDisabled()) {
                                    locationSet.add(location);
                                }
                            }
                        }
                    });
        }

        if (CollectionUtils.isEmpty(locationSet)) {
            return new PageImpl<>(Collections.<NewHomework.Location>emptyList(), pageable, 0);
        }
        List<NewHomework.Location> resultList = locationSet.stream()
                .filter(o -> o.getType() != null)
                .filter(o -> o.getType().getTypeId() == NewHomeworkType.PlatformType)
                .filter(o -> o.getType() != NewHomeworkType.BasicReview)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .collect(Collectors.toList());

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (page <= 0) {
            page = 1;
        }
        pageable = new PageRequest(page, size);
        int fromIndex = (page - 1) * size;
        int toIndex = page * size;
        if (toIndex >= resultList.size()) {
            toIndex = resultList.size();
        }
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }
        return new PageImpl<>(new LinkedList<>(resultList.subList(fromIndex, toIndex)), pageable, resultList.size());
    }

    @Override
    public List<DisplayStudentHomeWorkHistoryMapper> crmLoadStudentNewHomeworkHistory(StudentDetail student, Date startDate, Date endDate) {
        if (student == null || student.getClazz() == null) {
            return Collections.emptyList();
        }
        Long studentId = student.getId();
        List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
        Set<Long> groupIds = new HashSet<>();
        Map<Long, Group> groupMapperMap = new HashMap<>();
        // 学生所在分组
        for (Group group : groupMappers) {
            groupIds.add(group.getId());
            groupMapperMap.put(group.getId(), group);

        }
        // 读取作业
        Map<String, String> homeworkResultIdMap = new HashMap<>();
        Map<Long, List<NewHomework.Location>> groupHomework = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds, startDate, endDate);
        List<NewHomework.Location> locations = new ArrayList<>();
        for (List<NewHomework.Location> locationList : groupHomework.values()) {
            for (NewHomework.Location location : locationList) {
                if (location.getCreateTime() > startDate.getTime() && location.getCreateTime() <= endDate.getTime()) {
                    String day = DayRange.newInstance(location.getCreateTime()).toString();
                    Subject subject = location.getSubject();
                    Long groupId = location.getClazzGroupId();
                    NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, subject, location.getId(), studentId.toString());
                    homeworkResultIdMap.put(location.getId(), id.toString());
                    Group group = groupMapperMap.get(groupId);
                    //当组学科和作业学科不匹配则过滤掉（因为老师换科会导致当前组学科和之前布置作业学科不一致）
                    if (group != null && !group.getSubject().equals(subject)) {
                        continue;
                    }
                    locations.add(location);
                }
            }
        }
        locations.sort((h1, h2) -> Long.compare(h2.getCreateTime(), h1.getCreateTime()));

        //作业课本单元信息
        Map<String, NewHomeworkBook> newHomeworkBookInfoMap = newHomeworkLoader.loadNewHomeworkBooks(homeworkResultIdMap.keySet());

        //读取作业结果
        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(homeworkResultIdMap.values(), false);

//        Map<String, CorrectQuestionResult> correctQuestionResultMap = correctQuestionResultDao.loads(homeworkResultIdMap.values());
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkResultIdMap.keySet());

        // 生成结果
        List<DisplayStudentHomeWorkHistoryMapper> mappers = new LinkedList<>();
        for (NewHomework.Location location : locations) {
            String homeworkId = location.getId();
            NewHomework newHomework = newHomeworkMap.get(homeworkId);
            if (newHomework == null) continue;
            NewHomeworkResult homeworkResult = newHomeworkResultMap.get(homeworkResultIdMap.get(homeworkId));
            DisplayStudentHomeWorkHistoryMapper mapper = new DisplayStudentHomeWorkHistoryMapper();
            mapper.setHomeworkId(newHomework.getId());
            mapper.setCreateTime(newHomework.getCreateAt().getTime());
            mapper.setStartDate(DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日"));
            mapper.setEndDate(DateUtils.dateToString(newHomework.getEndTime(), "MM月dd日"));
            mapper.setChecked(newHomework.isHomeworkChecked());
            mapper.setSubject(newHomework.getSubject());
            mapper.setHomeworkType(newHomework.getType() != null ? newHomework.getType().name() : NewHomeworkType.Normal.name());
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            mapper.setTypes(types);
            Set<String> bookNames = new HashSet<>();
            Set<String> unitNames = new HashSet<>();
            NewHomeworkBook newHomeworkBook = newHomeworkBookInfoMap.get(homeworkId);
            if (newHomeworkBook != null) {
                bookNames = newHomeworkBook.processBookNameList();
                unitNames = newHomeworkBook.processUnitNameList();
            }
            mapper.setBookName(StringUtils.join(bookNames, ","));
            mapper.setUnitNames(StringUtils.join(unitNames, ","));

            Integer avgScore = null;
            boolean homeworkFinished = false;
            if (homeworkResult != null) {
                mapper.setNote(homeworkResult.getComment());
                if (homeworkResult.isFinished()) {
                    mapper.setSubmitTime(DateUtils.dateToString(homeworkResult.getFinishAt()));
                }
                homeworkFinished = homeworkResult.getFinishAt() != null;
                if (homeworkFinished) avgScore = homeworkResult.processScore();
            }
            if (homeworkFinished && avgScore == null) {
                if (homeworkResult.isCorrected()) {
                    mapper.setCorrectedType("已完成");
                } else {
                    mapper.setCorrectedType("未批改");
                }

            }
            mapper.setFinished(homeworkFinished);
            mapper.setState(!homeworkFinished ? "UNFINISHED" : "FINISHED");
            if (!newHomework.isHomeworkChecked() && homeworkFinished) {
                mapper.setState("UNCHECKED");
            }
            mapper.setHomeworkScore(avgScore);
            mappers.add(mapper);
        }

        return mappers;
    }

    @Override
    public List<CrmUnitQuestion> fetchCrmUnitQuestion(boolean isVacationHomework, String hid) {
        Map<String, CrmUnitQuestion> crmUnitQuestionMap = new LinkedHashMap<>();
        BaseHomeworkBook newHomeworkBook;
        if (isVacationHomework) {
            newHomeworkBook = vacationHomeworkBookDao.load(hid);
        } else {
            newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(hid);
        }
        if (newHomeworkBook != null && MapUtils.isNotEmpty(newHomeworkBook.getPractices())) {
            for (List<NewHomeworkBookInfo> newHomeworkBookInfos : newHomeworkBook.getPractices().values()) {
                if (CollectionUtils.isNotEmpty(newHomeworkBookInfos)) {
                    for (NewHomeworkBookInfo newHomeworkBookInfo : newHomeworkBookInfos) {
                        if (newHomeworkBookInfo != null) {
                            if (newHomeworkBookInfo.getUnitId() != null) {
                                CrmUnitQuestion crmUnitQuestion;
                                if (crmUnitQuestionMap.containsKey(newHomeworkBookInfo.getUnitId())) {
                                    crmUnitQuestion = crmUnitQuestionMap.get(newHomeworkBookInfo.getUnitId());
                                } else {
                                    crmUnitQuestion = new CrmUnitQuestion();
                                    crmUnitQuestion.setUnitName(SafeConverter.toString(newHomeworkBookInfo.getUnitName(), ""));
                                    crmUnitQuestion.setUnitId(newHomeworkBookInfo.getUnitId());
                                    crmUnitQuestionMap.put(newHomeworkBookInfo.getUnitId(), crmUnitQuestion);
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getQuestions())) {
                                    crmUnitQuestion.getQidList().addAll(newHomeworkBookInfo.getQuestions().stream().filter(StringUtils::isNotBlank).map(CrmUnitQuestion.CrmQuestion::new).collect(Collectors.toList()));
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getPapers())) {
                                    crmUnitQuestion.getPaperList().addAll(newHomeworkBookInfo.getPapers());
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getVideos())) {
                                    crmUnitQuestion.getVideoList().addAll(newHomeworkBookInfo.getVideos());
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getPictureBooks())) {
                                    crmUnitQuestion.getPictureBookList().addAll(newHomeworkBookInfo.getPictureBooks());
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getQuestionBoxIds())) {
                                    crmUnitQuestion.getQuestionBoxIdList().addAll(newHomeworkBookInfo.getQuestionBoxIds());
                                }
                                if (CollectionUtils.isNotEmpty(newHomeworkBookInfo.getDubbingIds())) {
                                    crmUnitQuestion.getDobbingIdsList().addAll(newHomeworkBookInfo.getDubbingIds());
                                }
                            }
                        }
                    }
                }
            }
        }
        crmUnitQuestionMap.forEach((key, value) -> value.handle());
        return new LinkedList<>(crmUnitQuestionMap.values());
    }

    @Override
    public Map<String, Object> studentSpecNewHomeworkDetail(Long studentId, String homeworkId) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        Subject subject = newHomework.getSubject();
        List<Map<String, Object>> resultList = new LinkedList<>();
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, subject, location.getId(), studentId.toString());
        NewHomeworkResult result = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        Map<String, String> oldQuestionIdToProcessId = new LinkedHashMap<>();

        Map<String, String> selectItemMap = new LinkedHashMap<>();
        selectItemMap.put("0.0", "全部");
        if (result != null) {
            LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = result.getPractices();
            List<String> qIds = newHomework.findAllQuestionIds();
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(qIds);
            if (MapUtils.isNotEmpty(practices)) {
                List<String> processIds = result.findAllHomeworkProcessIds(true);
                Map<String, NewHomeworkProcessResult> processResultsMap = newHomeworkProcessResultLoader.loads(homeworkId, processIds);
                for (ObjectiveConfigType oct : practices.keySet()) {
                    if (oct == ObjectiveConfigType.READING) {
                        LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = practices.get(oct).getAppAnswers();
                        List<String> pictureBookIds = apps.values()
                                .stream()
                                .map(NewHomeworkResultAppAnswer::getPictureBookId)
                                .collect(Collectors.toList());
                        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
                        if (MapUtils.isNotEmpty(apps)) {
                            for (NewHomeworkResultAppAnswer app : apps.values()) {
                                String selectItemKey = "1," + app.getPictureBookId();
                                PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                                selectItemMap.put(selectItemKey, oct.getValue() + ":" + pictureBook.getName());
                                String itemName = oct.getValue() + ":" + pictureBook.getName();
                                if (MapUtils.isNotEmpty(app.getAnswers())) {
                                    app.getAnswers()
                                            .values()
                                            .stream()
                                            .map(pid -> {
                                                NewHomeworkProcessResult pr = processResultsMap.get(pid);
                                                NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                                return translateNewHomeworkProcessForReadingOfNotOral(pr, newQuestion, selectItemKey, itemName, "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            })
                                            .filter(Objects::nonNull)
                                            .forEach(resultList::add);
                                }
                                if (MapUtils.isNotEmpty(app.getOralAnswers())) {
                                    app.getOralAnswers()
                                            .values()
                                            .stream()
                                            .map(pid -> {
                                                NewHomeworkProcessResult pr = processResultsMap.get(pid);
                                                NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                                return translateNewHomeworkProcessForReadingOfNotOral(pr, newQuestion, selectItemKey, itemName, "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            })
                                            .filter(Objects::nonNull)
                                            .forEach(resultList::add);

                                }
                            }
                        }
                    } else if (oct == ObjectiveConfigType.BASIC_APP
                            || oct == ObjectiveConfigType.LS_KNOWLEDGE_REVIEW
                            || oct == ObjectiveConfigType.NATURAL_SPELLING) {
                        LinkedHashMap<String, NewHomeworkResultAppAnswer> apps = practices.get(oct).getAppAnswers();
                        List<String> lessons = apps
                                .values()
                                .stream()
                                .map(NewHomeworkResultAppAnswer::getLessonId)
                                .collect(Collectors.toList());
                        Map<String, NewBookCatalog> lessonNewBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogIds(lessons);
                        Set<String> unitsIds = new HashSet<>();
                        Map<String, String> lessonToUnit = new LinkedHashMap<>();
                        for (NewBookCatalog n : lessonNewBookCatalog.values()) {
                            List<NewBookCatalogAncestor> l = n.getAncestors();
                            Map<String, NewBookCatalogAncestor> m = l
                                    .stream()
                                    .collect(Collectors.toMap(NewBookCatalogAncestor::getNodeType, Function.identity()));
                            unitsIds.add(m.get("UNIT").getId());
                            lessonToUnit.put(n.getId(), m.get("UNIT").getId());
                        }
                        Map<String, NewBookCatalog> units = newContentLoaderClient.loadBookCatalogByCatalogIds(unitsIds);
                        if (MapUtils.isNotEmpty(apps)) {
                            NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(oct);
                            Map<String, NewHomeworkApp> map = target.getApps()
                                    .stream()
                                    .collect(Collectors.toMap(o -> o.getCategoryId() + "-" + o.getLessonId(), Function.identity()));
                            apps.values()
                                    .stream()
                                    .filter(app -> MapUtils.isNotEmpty(app.getAnswers()))
                                    .map(app -> {
                                        PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
                                        String selectItemKey = "2," + oct.getKey() + app.getPracticeId();
                                        selectItemMap.put(selectItemKey, oct.getValue() + ":" + practiceType.getCategoryName());
                                        NewBookCatalog lesson = lessonNewBookCatalog.get(app.getLessonId());
                                        NewBookCatalog unit = units.get(lessonToUnit.get(app.getLessonId()));
                                        List<Map<String, Object>> re = new LinkedList<>();
                                        NewHomeworkApp newHomeworkApp = map.get(app.getCategoryId() + "-" + app.getLessonId());
                                        List<NewHomeworkQuestion> questions = newHomeworkApp.fetchQuestions();
                                        for (NewHomeworkQuestion question : questions) {
                                            NewHomeworkProcessResult pr = processResultsMap.get(app.getAnswers().get(question.getQuestionId()));
                                            if (pr == null)
                                                continue;
                                            NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                            Map<String, Object> mq = translateNewHomeworkProcessForReadingOfNotOral(pr, newQuestion, selectItemKey, oct.getValue() + ":" + practiceType.getCategoryName(), Objects.nonNull(unit) ? unit.getAlias() : "", Objects.nonNull(lesson) ? lesson.getAlias() : "", oct, oldQuestionIdToProcessId, processResultsMap);
                                            if (MapUtils.isNotEmpty(mq)) {
                                                re.add(mq);
                                            }
                                        }
                                        return re;
                                    })
                                    .flatMap(Collection::stream)
                                    .forEach(resultList::add);

                        }
                    } else if (oct == ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) {
                        if (result.getPractices().get(oct) != null && CollectionUtils.isNotEmpty(result.getPractices().get(oct).ocrMentalAnswers)) {
                            List<String> ocrMentalProcessIds = result.getPractices().get(oct).ocrMentalAnswers.stream().collect(Collectors.toList());
                            Map<String, NewHomeworkProcessResult> ocrMentalProcessResultsMap = newHomeworkProcessResultLoader.loads(homeworkId, ocrMentalProcessIds);
                            if (!MapUtils.isEmpty(ocrMentalProcessResultsMap)) {
                                ocrMentalProcessResultsMap.values().stream().map(p -> {
                                    List<Map<String, Object>> re = new LinkedList<>();
                                    String imageUrl = p.getOcrMentalImageDetail() != null ? p.getOcrMentalImageDetail().getImg_url() : "";
                                    String originImageUrl = "";
                                    if (imageUrl.equals(NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_DEFAULT_IMG)) {
                                        originImageUrl = ocrClassifyImagesPersistence.getOriginImageUrlByProcessId(p.getId());
                                    }
                                    re.add(MapUtils.m(
                                            "pId", p.getId(),
                                            "itemName", p.getObjectiveConfigType().getValue(),
                                            "createTime", p.getCreateAt(),
                                            "content", JsonUtils.toJson(p),
                                            "imageUrl", imageUrl,
                                            "originImageUrl", originImageUrl,
                                            "objectiveConfigType", p.getObjectiveConfigType()
                                    ));
                                    return re;
                                }).flatMap(Collection::stream).forEach(resultList::add);
                            }
                        }
                    } else if (oct == ObjectiveConfigType.OCR_DICTATION) {
                        if (result.getPractices().get(oct) != null && CollectionUtils.isNotEmpty(result.getPractices().get(oct).getOcrDictationAnswers())) {
                            List<String> ocrDictationProcessIds = result.getPractices().get(oct).ocrDictationAnswers.stream().collect(Collectors.toList());
                            Map<String, NewHomeworkProcessResult> ocrDicTationProcessResultsMap = newHomeworkProcessResultLoader.loads(homeworkId, ocrDictationProcessIds);
                            if (!MapUtils.isEmpty(ocrDicTationProcessResultsMap)) {
                                ocrDicTationProcessResultsMap.values().stream().map(p -> {
                                    List<Map<String, Object>> re = new LinkedList<>();
                                    String imageUrl = p.getOcrDictationImageDetail() != null ? p.getOcrDictationImageDetail().getImg_url() : "";
                                    String originImageUrl = "";
                                    if (imageUrl.equals(NewHomeworkConstants.OCR_DICTATION_DEFAULT_IMG)) {
                                        originImageUrl = ocrClassifyImagesPersistence.getOriginImageUrlByProcessId(p.getId());
                                    }
                                    re.add(MapUtils.m(
                                            "pId", p.getId(),
                                            "itemName", p.getObjectiveConfigType().getValue(),
                                            "createTime", p.getCreateAt(),
                                            "content", JsonUtils.toJson(p),
                                            "originImageUrl", originImageUrl,
                                            "imageUrl", p.getOcrDictationImageDetail() != null ? p.getOcrDictationImageDetail().getImg_url() : "",
                                            "objectiveConfigType", p.getObjectiveConfigType()
                                    ));
                                    return re;
                                }).flatMap(Collection::stream).forEach(resultList::add);
                            }

                        }
                    } else if (oct == ObjectiveConfigType.ORAL_COMMUNICATION) {
                        if (result.getPractices().get(oct) == null || MapUtils.isEmpty(result.getPractices().get(oct).getAppAnswers())) {
                            continue;
                        }
                        Map<String, SubHomeworkProcessResult> processResultMap = getOralCommunicationProcessResult(newHomework, studentId, result.getPractices().get(oct).getAppAnswers());
                        if (MapUtils.isEmpty(processResultMap)) {
                            continue;
                        }
                        String selectItemKey = "3," + oct.getKey();
                        selectItemMap.put(selectItemKey, oct.getValue());
                        final NewHomeworkBookInfo bookInfo;
                        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
                        if (newHomeworkBook != null && CollectionUtils.isNotEmpty(newHomeworkBook.getPractices().get(oct)) && newHomeworkBook.getPractices().get(oct).get(0) != null) {
                            bookInfo = newHomeworkBook.getPractices().get(oct).get(0);
                        } else {
                            bookInfo = null;
                        }
                        processResultMap.values().stream().map(p -> {
                            List<Map<String, Object>> re = new LinkedList<>();
                            long duration = new BigDecimal(p.getDuration()).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue();
                            List<String> audioUrls = new LinkedList<>();
                            List<String> audioInfo = new LinkedList<>();
                            VoiceEngineType voiceEngineType = p.getVoiceEngineType();
                            List<NewHomeworkProcessResult.OralDetail> oralDetails = CollectionUtils.isNotEmpty(p.getOralDetails()) ?
                                    p.getOralDetails()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .collect(Collectors.toList()) :
                                    new LinkedList<>();
                            handleOralDetail(null, audioUrls, audioInfo, oralDetails, voiceEngineType);
                            re.add(MapUtils.m(
                                    "pId", p.getId(),
                                    "qId", p.getDialogId(),
                                    "selectItemKey", selectItemKey,
                                    "itemName", p.getObjectiveConfigType().getValue(),
                                    "unitName", bookInfo != null ? bookInfo.getUnitName() : "",
                                    "lessonName", p.getStoneId(),
                                    "createTime", p.getCreateAt(),
                                    "score", p.getScore(),
                                    "duration", (duration / 60 > 0 ? (duration / 60 + "分") : "") + duration % 60 + "秒",
                                    "content", JsonUtils.toJson(p),
                                    "objectiveConfigType", p.getObjectiveConfigType(),
                                    "fileNames", Lists.newArrayList(),
                                    "audioUrls", audioUrls,
                                    "audioInfo", audioInfo,
                                    "clientName", p.getClientName(),
                                    "clientType", p.getClientType(),
                                    "appOralScoreLevel", p.getAppOralScoreLevel() != null ? p.getAppOralScoreLevel() : ""
                            ));
                            return re;
                        }).flatMap(Collection::stream).forEach(resultList::add);
                    } else {
                        List<String> pIds = result.findHomeworkProcessIdsByObjectiveConfigType(oct);
                        String selectItemKey = "4," + oct.getKey();
                        selectItemMap.put(selectItemKey, oct.getValue());
                        if (CollectionUtils.isNotEmpty(pIds)) {
                            pIds.stream()
                                    .map(pid -> {
                                        NewHomeworkProcessResult pr = processResultsMap.get(pid);
                                        NewQuestion newQuestion = questionMap.get(pr.getQuestionId());
                                        return translateNewHomeworkProcessForReadingOfNotOral(pr, newQuestion, selectItemKey, oct.getValue(), "", "", oct, oldQuestionIdToProcessId, processResultsMap);
                                    })
                                    .filter(Objects::nonNull)
                                    .forEach(resultList::add);

                        }
                    }
                }
            }
        }
        User user = userLoaderClient.loadUser(studentId);
        resultList.sort(Comparator.comparingLong(o2 -> ((Date) o2.get("createTime")).getTime()));
        resultList.forEach(o -> o.put("createTime", DateUtils.dateToString((Date) o.get("createTime"), "yyyy-MM-dd HH:mm:ss")));
        return MapUtils.m(
                "stResultDetailList", resultList,
                "realName", user != null ? user.fetchRealname() : "",
                "resultList", JsonUtils.toJson(resultList),
                "selectItemKey", selectItemMap.keySet(),
                "selectItemValue", selectItemMap.values(),
                "homeworkId", homeworkId,
                "studentId", studentId,
                "subject", subject,
                "isContainOralMentalPractise", result != null
                        && result.getPractices() != null
                        && CollectionUtils.isNotEmpty(result.getPractices().keySet())
                        && (result.getPractices().keySet().contains(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)
                        || result.getPractices().keySet().contains(ObjectiveConfigType.OCR_DICTATION)
                ) ? true : null
        );
    }

    private Map<String, SubHomeworkProcessResult> getOralCommunicationProcessResult(NewHomework newHomework, Long studentId, LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers) {
        if (MapUtils.isEmpty(appAnswers)) {
            return null;
        }
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(appAnswers.keySet());
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return null;
        }
        Map<String, StoneBufferedData> stoneMap = stoneBufferedDataList.stream().collect(Collectors.toMap(StoneBufferedData::getId, Function.identity()));
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : appAnswers.entrySet()) {
            String stoneId = entry.getKey();
            StoneBufferedData stoneBufferedData = stoneMap.get(stoneId);
            List<String> dialogList = Lists.newArrayList();
            if (stoneBufferedData.getOralPracticeConversion() != null) {
                dialogList = stoneBufferedData.getOralPracticeConversion().getTopics()
                        .stream()
                        .filter(t -> CollectionUtils.isNotEmpty(t.getContents()))
                        .map(Topic::getContents)
                        .flatMap(List::stream)
                        .filter(c -> CollectionUtils.isNotEmpty(c.getDialogs()))
                        .map(OralContent::getDialogs)
                        .flatMap(List::stream)
                        .filter(Objects::nonNull)
                        .filter(d -> SafeConverter.toInt(d.getRequiredAnswer()) == 1)
                        .map(Dialog::getUuid)
                        .collect(Collectors.toList());
            }
            if (stoneBufferedData.getInteractiveVideo() != null) {
                dialogList = stoneBufferedData.getInteractiveVideo().getContents()
                        .stream()
                        .filter(v -> "record".equals(v.getContentType()))
                        .map(VideoContent::getUuid)
                        .collect(Collectors.toList());
            }
            if (stoneBufferedData.getInteractivePictureBook() != null) {
                dialogList = stoneBufferedData.getInteractivePictureBook().getPages().stream()
                        .filter(p -> CollectionUtils.isNotEmpty(p.getSections()))
                        .map(InteractivePictureBook.Page::getSections)
                        .flatMap(List::stream)
                        .filter(s -> CollectionUtils.isNotEmpty(s.getQuestions()))
                        .map(InteractivePictureBook.Section::getQuestions)
                        .flatMap(List::stream)
                        .filter(q -> "record".equals(q.getContentType()))
                        .map(InteractivePictureBook.Question::getUuid)
                        .collect(Collectors.toList());
            }
            dialogList.forEach(d -> {
                SubHomeworkResultAnswer.ID aid = new SubHomeworkResultAnswer.ID();
                aid.setDay(day);
                aid.setHid(newHomework.getId());
                aid.setJoinKeys(Collections.singleton(stoneId));
                aid.setType(ObjectiveConfigType.ORAL_COMMUNICATION);
                aid.setUserId(SafeConverter.toString(studentId));
                aid.setQuestionId(d);
                subHomeworkResultAnswerIds.add(aid.toString());
            });
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        if (MapUtils.isEmpty(subHomeworkResultAnswerMap)) {
            return null;
        }
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .filter(Objects::nonNull)
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        return newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(newHomeworkProcessResultIds);
    }

    private Map<String, Object> translateNewHomeworkProcessForReadingOfNotOral(NewHomeworkProcessResult pr, NewQuestion newQuestion, String selectItemKey, String itemName, String unitName, String lessonName, ObjectiveConfigType oct, Map<String, String> oldQuestionIdToProcessId, Map<String, NewHomeworkProcessResult> processResultsMap) {
        if (newQuestion == null || pr == null) {
            return null;
        }
        List<NewHomeworkQuestionFile> files = CollectionUtils.isNotEmpty(pr.getFiles()) ?
                pr.getFiles()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()) :
                new LinkedList<>();
        List<NewHomeworkProcessResult.OralDetail> oralDetails = CollectionUtils.isNotEmpty(pr.getOralDetails()) ?
                pr.getOralDetails()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()) :
                new LinkedList<>();
        List<String> audioUrls = new LinkedList<>();
        List<String> audioInfo = new LinkedList<>();

        List<CrmAudioData.Summary> summaries = new LinkedList<>();
        VoiceEngineType voiceEngineType = pr.getVoiceEngineType();
        handleOralDetail(summaries, audioUrls, audioInfo, oralDetails, voiceEngineType);
        List<String> fileNames = new LinkedList<>();
        List<String> relativeUrls = new LinkedList<>();
        for (NewHomeworkQuestionFile n : files) {
            fileNames.add(n.getFileName());
            relativeUrls.add(NewHomeworkQuestionFileHelper.getFileUrl(n));
        }
        long duration = new BigDecimal(pr.getDuration()).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue();
        String sourceAnswer = "";
        List<NewQuestionsSubContents> subContents = CollectionUtils.isNotEmpty(newQuestion.getContent().getSubContents()) ? newQuestion.getContent().getSubContents() : Collections.emptyList();
        if (CollectionUtils.isNotEmpty(newQuestion.getAnswers())) {
            List<List<String>> standardAnswers =
                    subContents
                            .stream()
                            .map(o -> o.getAnswerList(pr.getSubject()))
                            .collect(Collectors.toList());

            sourceAnswer = NewHomeworkUtils.pressAnswer(subContents, standardAnswers);

        }
        String userAnswers = "";
        if (CollectionUtils.isNotEmpty(pr.getUserAnswers())) {
            userAnswers = NewHomeworkUtils.pressAnswer(subContents, pr.getUserAnswers());
        }
        String subGrasp = "";
        if (CollectionUtils.isNotEmpty(pr.getUserAnswers())) {
            subGrasp = StringUtils.join(pr
                            .getSubGrasp()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(o -> Objects.equals(o, Boolean.TRUE) ? "对" : "错")
                            .collect(Collectors.toList()),
                    ",");
        }
        String correctNewHomeworkProcessInfo = oct == ObjectiveConfigType.EXAM &&
                oldQuestionIdToProcessId != null &&
                oldQuestionIdToProcessId.containsKey(pr.getQuestionId()) &&
                processResultsMap.containsKey(oldQuestionIdToProcessId.get(pr.getQuestionId())) ?
                JsonUtils.toJson(processResultsMap.get(oldQuestionIdToProcessId.get(pr.getQuestionId()))) : "";
        boolean isHaveCorrectNewHomeworkProcessInfo = StringUtils.isNotBlank(correctNewHomeworkProcessInfo);
        return MapUtils.m(
                "itemName", itemName,
                "qId", pr.getQuestionId(),
                "selectItemKey", selectItemKey,
                "createTime", pr.getCreateAt(),
                "unitName", unitName,
                "lessonName", lessonName,
                "standardScore", pr.getStandardScore(),
                "audioUrls", audioUrls,
                "sourceAnswer", sourceAnswer,
                "audioInfo", audioInfo,
                "isHaveCorrectNewHomeworkProcessInfo", isHaveCorrectNewHomeworkProcessInfo,
                "correctNewHomeworkProcessInfo", correctNewHomeworkProcessInfo,
                "score", pr.getScore(),
                "duration", (duration / 60 > 0 ? (duration / 60 + "分") : "") + duration % 60 + "秒",
                "grasp", SafeConverter.toBoolean(pr.getGrasp()) ? "是" : "否",
                "userAnswers", userAnswers,
                "subGrasp", subGrasp,
                "files", pr.getFiles(),
                "fileNames", fileNames,
                "relativeUrls", relativeUrls,
                "summaries", summaries,
                "oralDetails", pr.getOralDetails(),
                "content", JsonUtils.toJson(pr),
                "clientName", pr.getClientName(),
                "clientType", pr.getClientType(),
                "appOralScoreLevel", pr.getAppOralScoreLevel() != null ? pr.getAppOralScoreLevel() : "",
                "objectiveConfigType", pr.getObjectiveConfigType()
        );
    }

    private void handleOralDetail(List<CrmAudioData.Summary> summaries, List<String> audioUrls, List<String> audioInfo, List<NewHomeworkProcessResult.OralDetail> oralDetails, VoiceEngineType voiceEngineType) {
        for (NewHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
            String voiceUrl = oralDetail.getAudio();
            if (StringUtils.isNotEmpty(voiceUrl)) {
                if (voiceEngineType == VoiceEngineType.ChiVox) {
                    voiceUrl = "http://" + voiceUrl + ".mp3";
                }
//                else {
//                    String voiceData = NewHomeworkUtils.sendGet(voiceUrl.replaceFirst("audio/play", "result"));
//                    if (StringUtils.isNotBlank(voiceData)) {
//                        CrmAudioData crmAudioData = JsonUtils.fromJson(voiceData, CrmAudioData.class);
//                        if (crmAudioData != null) {
//                            CrmAudioData.Summary summary = crmAudioData.refineInfo();
//                            if (summary.isFlag()) {
//                                summaries.add(summary);
//                            }
//                        }
//                    }
//                }
                audioUrls.add(voiceUrl);
                audioInfo.add("总分:" + oralDetail.getMacScore() + "\n流利度:" + oralDetail.getFluency() + "\n完整度:" + oralDetail.getIntegrity() + "\n发音标准:" + oralDetail.getPronunciation() + "\n星级:" + oralDetail.getStar() + "\n各组关键词均值:" + oralDetail.getKeyStandardScore() + "\n是否有keywords:" + oralDetail.getIsHasKeyWords());
            }
        }
    }

    @Override
    public Map<String, Object> homeworkNewHomepage(String homeworkId) {

        //应试题目预览所需变量值
        String mainUrl = RuntimeMode.isDevelopment() ? "//www.test.17zuoye.net" : ProductConfig.getMainSiteBaseUrl();
        Map<String, Object> resultMap = MapUtils.m("env", RuntimeMode.isDevelopment() ? "test" : RuntimeMode.current().getStageMode(),
                "domain", mainUrl,
                "imgDomain", mainUrl,
                "category", "primary",
                "ms_crm_admin_url", juniorCrmAdminUrlBase()
        );
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return resultMap;
        }

        Subject subject = newHomework.getSubject();
        Map<String, NewHomeworkStudyMaster> newHomeworkStudyMasterMap = newHomeworkPartLoader.getNewHomeworkStudyMasterMap(Collections.singleton(homeworkId));
        resultMap.put("masterNames", "");

        if (newHomeworkStudyMasterMap.containsKey(homeworkId)) {
            NewHomeworkStudyMaster newHomeworkStudyMaster = newHomeworkStudyMasterMap.get(homeworkId);
            if (newHomeworkStudyMaster != null) {
                if (newHomeworkStudyMaster.getMasterStudentList() != null) {
                    StringBuffer masterNames = new StringBuffer();
                    for (NewHomeworkStudyMaster.MasterStudent masterStudent : newHomeworkStudyMaster.getMasterStudentList()) {
                        masterNames.append(SafeConverter.toString(masterStudent.getUserName()))
                                .append("(")
                                .append(SafeConverter.toString(masterStudent.getUserId()))
                                .append(")")
                                .append(",");
                    }
                    resultMap.put("masterNames", masterNames);
                }
            }
        }

        List<Map<String, Object>> homeworkDetails = new LinkedList<>();
        Date startDateTime = newHomework.getStartTime();
        Date endDateTime = newHomework.getEndTime();
        List<ObjectiveConfigType> types = Lists.newArrayList();
        if (subject == Subject.ENGLISH) {
            for (NewHomeworkPracticeContent practice : newHomework.getPractices()) {
                ObjectiveConfigType oct = practice.getType();
                types.add(oct);
                if (oct == ObjectiveConfigType.BASIC_APP
                        || oct == ObjectiveConfigType.LS_KNOWLEDGE_REVIEW
                        || oct == ObjectiveConfigType.NATURAL_SPELLING
                        || oct == ObjectiveConfigType.READING) {
                    homeworkDetails.addAll(this.fetchNewEnValue(practice, oct));
                } else {
                    homeworkDetails.addAll(
                            newHomework.findQuestionIds(oct, true)
                                    .stream()
                                    .map(qId -> MiscUtils.m(
                                            "exam", qId,
                                            "type", oct.getValue()
                                    ))
                                    .collect(Collectors.toList()));
                }
            }
        } else {
            for (NewHomeworkPracticeContent practice : newHomework.getPractices()) {
                types.add(practice.getType());
                List<String> questionIds = newHomework.findQuestionIds(practice.getType(), false);
                if (CollectionUtils.isEmpty(questionIds) && practice.getType() == ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) {
                    homeworkDetails.addAll(Collections.singleton(MiscUtils.m(
                            "exam", practice.getWorkBookName() + "第" + practice.getHomeworkDetail() + "页",
                            "type", practice.getType().getValue()
                    )));
                } else {
                    homeworkDetails.addAll(questionIds.stream()
                            .map(qId ->
                                    MiscUtils.m(
                                            "exam", qId,
                                            "type", practice.getType().getValue()
                                    ))
                            .collect(Collectors.toList()));
                }
            }
        }

        List<Map<String, Object>> studentAccomplishmentList = new LinkedList<>();
        // 新作业只显示完成作业的学生，部分完成的不显示了
        Map<String, NewHomeworkResult> newHomeworkResultMaps = newHomeworkResultLoader.findByHomeworkForReport(newHomework);
        NewAccomplishment accomplishment = newAccomplishmentLoader.loadNewAccomplishment(newHomework.toLocation());
        if (MapUtils.isNotEmpty(newHomeworkResultMaps)) {
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId()).stream().collect(Collectors.toMap(LongIdEntity::getId, o -> o));
            boolean needCorrect = needCorrect(types, newHomework);
            Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap = Maps.newHashMap();
            Map<String, String> homeWorkCorrect = Maps.newHashMap();
            if (needCorrect) {
                List<String> homeworkToSelfStudyIds = Lists.newArrayList();
                for (NewHomeworkResult newHomeworkResult : newHomeworkResultMaps.values()) {
                    homeworkToSelfStudyIds.add(new HomeworkSelfStudyRef.ID(newHomework.getId(), newHomeworkResult.getUserId()).toString());
                }
                homeWorkCorrect = isHomeWorkCorrect(homeworkToSelfStudyIds, selfStudyHomeworkReportMap);
            }
            Map<String, String> finalHomeWorkCorrect = homeWorkCorrect;
            studentAccomplishmentList.addAll(
                    newHomeworkResultMaps
                            .values()
                            .stream()
                            .filter(o -> userMap.containsKey(o.getUserId()))
                            .map(newHomeworkResult -> {
                                        Long duration = newHomeworkResult.processDuration();
                                        Integer score = newHomeworkResult.processScore();
                                        String homeworkToSelfStudyId = new HomeworkSelfStudyRef.ID(newHomework.getId(), newHomeworkResult.getUserId()).toString();
                                        return MiscUtils.m(
                                                "studentId", newHomeworkResult.getUserId(),
                                                "studentName", userMap.get(newHomeworkResult.getUserId()).fetchRealname(),
                                                "isFinished", newHomeworkResult.isFinished() ? "是" : "否",
                                                "score", score != null ? score : "",
                                                "finishAt", newHomeworkResult.getFinishAt() != null ? newHomeworkResult.getFinishAt() : "",
                                                "duration", duration != null ? (duration / 60 > 0 ? (duration / 60 + "分") : "") + duration % 60 + "秒" : "",
                                                "clientType", accomplishment != null
                                                        && accomplishment.getDetails() != null
                                                        && accomplishment.getDetails().get(SafeConverter.toString(newHomeworkResult.getUserId())) != null ? SafeConverter.toString(accomplishment.getDetails().get(SafeConverter.toString(newHomeworkResult.getUserId())).getClientType(), "") : "",
                                                "correct", needCorrect ? finalHomeWorkCorrect.get(homeworkToSelfStudyId) : "无需",
                                                "selfStudyHomeworkReportJson", JsonUtils.toJson(selfStudyHomeworkReportMap.get(homeworkToSelfStudyId))
                                        );

                                    }
                            ).collect(Collectors.toList()));
        }


        List<CrmUnitQuestion> crmUnitQuestions = this.fetchCrmUnitQuestion(false, homeworkId);

        resultMap.put("crmUnitQuestions", crmUnitQuestions);

        resultMap.put("homeworkId", homeworkId);

        String time = "";
        if (newHomework.getDuration() != null) {
            time = newHomework.getDuration() / 60 + "分" + newHomework.getDuration() % 60 + "秒";
        }

        resultMap.put("time", time);

        if (subject == Subject.MATH) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.getPractices().stream().filter(p -> p.getType().equals(ObjectiveConfigType.MENTAL_ARITHMETIC)
                    || p.getType().equals(ObjectiveConfigType.MENTAL)).findFirst().orElse(null);
            if (newHomeworkPracticeContent != null && newHomeworkPracticeContent.getTimeLimit() != null) {
                resultMap.put("timeLimit", newHomeworkPracticeContent.getTimeLimit().getDesc());//口算限定时间
            }
        }

        resultMap.put("source", newHomework.getSource() != null ? SafeConverter.toString(newHomework.getSource().getDesc()) : "");
        resultMap.put("clazzGroupId", newHomework.getClazzGroupId());
        resultMap.put("teacherId", newHomework.getTeacherId());
        Teacher teacher = teacherLoaderClient.loadTeacher(newHomework.getTeacherId());
        resultMap.put("teacherName", teacher.fetchRealname());
        resultMap.put("homeworkSubject", subject.name());
        resultMap.put("homeworkInfo", MiscUtils.m("startDatetime", startDateTime, "endDatetime", endDateTime, "checkedAt", newHomework.getCheckedAt()));
        resultMap.put("homeworkDetails", homeworkDetails);
        resultMap.put("studentAccomplishmentList", studentAccomplishmentList);
        resultMap.put("baseUrl", ProductConfig.getMainSiteBaseUrl());
        return resultMap;
    }


    private Map<String, String> isHomeWorkCorrect(List<String> homeworkToSelfStudyIds, Map<String, SelfStudyHomeworkReport> selfStudyHomeworkReportMap) {
        Map<String, String> studentCorrentMap = Maps.newHashMap();
        Map<String, HomeworkSelfStudyRef> homeworkSelfStudyRefMap = homeworkSelfStudyRefDao.loads(homeworkToSelfStudyIds);
        // 获取订正作业id，用于拿到订正报告
        List<String> selfStudyIds = homeworkSelfStudyRefMap.values()
                .stream()
                .filter(o -> StringUtils.isNotBlank(o.getSelfStudyId()))
                .map(HomeworkSelfStudyRef::getSelfStudyId)
                .collect(Collectors.toList());

        Map<String, SelfStudyHomeworkReport> selfHomeworkReportMap = selfStudyHomeworkReportDao.loads(selfStudyIds);
        for (String homeworkToSelfStudyId : homeworkToSelfStudyIds) {
            if (homeworkSelfStudyRefMap.containsKey(homeworkToSelfStudyId)) {
                HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefMap.get(homeworkToSelfStudyId);
                if (StringUtils.isBlank(homeworkSelfStudyRef.getSelfStudyId())) {
                    studentCorrentMap.put(homeworkToSelfStudyId, "未订正");
                } else {
                    SelfStudyHomeworkReport selfStudyHomeworkReport = selfHomeworkReportMap.get(homeworkSelfStudyRef.getSelfStudyId());
                    if (selfStudyHomeworkReport != null) {
                        selfStudyHomeworkReportMap.put(homeworkToSelfStudyId, selfStudyHomeworkReport);
                        studentCorrentMap.put(homeworkToSelfStudyId, "已完成");
                    } else {
                        studentCorrentMap.put(homeworkToSelfStudyId, "未订正");
                    }
                }
            } else {
                studentCorrentMap.put(homeworkToSelfStudyId, "无需");
            }
        }
        return studentCorrentMap;
    }

    private String juniorCrmAdminUrlBase() {
        String host = "zx-admin.17zuoye.net";
        if (RuntimeMode.isProduction()) {
            host = "zx-admin.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            host = "zx-admin.staging.17zuoye.net";
        } else if (RuntimeMode.isTest()) {
            host = "zx-admin.test.17zuoye.net";
        } else if (RuntimeMode.isDevelopment()) {
            host = "local.zx-admin.17zuoye.net";
        }
        return "http://" + host;
    }

    private List<Map<String, Object>> fetchNewEnValue(NewHomeworkPracticeContent newHomeworkPracticeContent, ObjectiveConfigType objectiveConfigType) {
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isEmpty(apps)) return Collections.emptyList();
        List<Map<String, Object>> result = new LinkedList<>();
        if (objectiveConfigType == ObjectiveConfigType.READING) {
            List<String> pictureBookIds = newHomeworkPracticeContent
                    .getApps()
                    .stream()
                    .map(NewHomeworkApp::getPictureBookId)
                    .collect(Collectors.toList());
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
            List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
            Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList
                    .stream()
                    .collect(Collectors
                            .toMap(PictureBookSeries::getId, Function.identity()));
            List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
            Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList
                    .stream()
                    .collect(Collectors
                            .toMap(PictureBookTopic::getId, Function.identity()));
            for (NewHomeworkApp app : apps) {
                Map<String, Object> m = NewHomeworkContentDecorator.decoratePictureBook(pictureBookMap.get(app.getPictureBookId()), pictureBookSeriesMap, pictureBookTopicMap, null, null);
                m.put("type", "READING");
                result.add(m);
            }
        } else if (objectiveConfigType == ObjectiveConfigType.BASIC_APP
                || objectiveConfigType == ObjectiveConfigType.LS_KNOWLEDGE_REVIEW
                || objectiveConfigType == ObjectiveConfigType.NATURAL_SPELLING) {
            List<String> lessonIds = apps.stream().map(NewHomeworkApp::getLessonId).collect(Collectors.toList());
            Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
            for (NewHomeworkApp app : apps) {
                if (!ms.containsKey(app.getLessonId()))
                    continue;
                PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                Map<String, NewBookCatalogAncestor> m = ms
                        .get(app.getLessonId())
                        .getAncestors()
                        .stream()
                        .collect(Collectors
                                .toMap(NewBookCatalogAncestor::getNodeType, Function.identity()));
                String qids = StringUtils.join(
                        app.getQuestions()
                                .stream()
                                .map(NewHomeworkQuestion::getQuestionId)
                                .collect(Collectors.toList())
                                .toArray(),
                        ",");

                result.add(
                        MiscUtils.m(
                                "lessonId", app.getLessonId(),
                                "qids", qids,
                                "unitId", m.containsKey("UNIT") ? m.get("UNIT").getId() : "",
                                "bookId", m.containsKey("BOOK") ? m.get("BOOK").getId() : "",
                                "practiceId", app.getPracticeId(),
                                "practiceName", practiceType.getPracticeName(),
                                "categoryId", app.getCategoryId(),
                                "categoryName", practiceType.getCategoryName(),
                                "type", "BASIC_APP"
                        ));
            }
        }
        return result;
    }

    /**
     * 是否需要订正
     *
     * @param types
     * @param newHomework
     * @return
     */
    private boolean needCorrect(List<ObjectiveConfigType> types, NewHomework newHomework) {
        boolean showCorrect = false;
        boolean isConfigTrue = true;
        try {
            String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "GET_CORRECT_FUN_IS_SHOW");
            isConfigTrue = ConversionUtils.toBool(config);
        } catch (IllegalArgumentException e) {
            // logger.info("CommonConfigLoaderClient GET_CORRECT_FUN_IS_SHOW : e{}", e.getMessage());
        }
        if (CollectionUtils.containsAny(GenerateSelfStudyHomeworkConfigTypes, types)
                && NewHomeworkConstants.showWrongQuestionInfo(newHomework.getCreateAt(), RuntimeMode.getCurrentStage())
                && NeedSelfStudyHomeworkSubjects.contains(newHomework.getSubject())
                && isConfigTrue
        ) {
            showCorrect = true;
        }
        return showCorrect;
    }

    @Override
    public Map<String, Object> vacationHomeworkNewHomepage(String hid) {
        Map<String, Object> resultMap = Maps.newHashMap();
        VacationHomework vacationHomework = vacationHomeworkLoader.loadVacationHomeworkIncludeDisabled(hid);
        Subject subject = vacationHomework.getSubject();
        List<Map<String, Object>> homeworkDetails = new LinkedList<>();
        if (subject == Subject.ENGLISH) {
            for (NewHomeworkPracticeContent practice : vacationHomework.getPractices()) {
                ObjectiveConfigType oct = practice.getType();
                if (oct == ObjectiveConfigType.BASIC_APP
                        || oct == ObjectiveConfigType.LS_KNOWLEDGE_REVIEW
                        || oct == ObjectiveConfigType.NATURAL_SPELLING
                        || oct == ObjectiveConfigType.READING) {
                    homeworkDetails.addAll(this.fetchNewEnValue(practice, oct));
                } else {
                    homeworkDetails.addAll(
                            vacationHomework.findQuestionIds(oct, true)
                                    .stream()
                                    .map(qId -> MiscUtils.m(
                                            "exam", qId,
                                            "type", oct.getValue()
                                    ))
                                    .collect(Collectors.toList()));
                }
            }
        } else {
            for (NewHomeworkPracticeContent practice : vacationHomework.getPractices()) {
                homeworkDetails.addAll(
                        vacationHomework.findQuestionIds(practice.getType(), false)
                                .stream()
                                .map(qId ->
                                        MiscUtils.m(
                                                "exam", qId,
                                                "type", practice.getType().getValue()
                                        ))
                                .collect(Collectors.toList()));
            }
        }
        List<CrmUnitQuestion> crmUnitQuestions = this.fetchCrmUnitQuestion(true, hid);
        resultMap.put("hid", hid);
        resultMap.put("homeworkDetails", homeworkDetails);
        resultMap.put("homeworkSubject", subject.name());
        resultMap.put("crmUnitQuestions", crmUnitQuestions);
        resultMap.put("success", true);
        return resultMap;
    }

    @Override
    public PageImpl<HomeworkBlackWhiteList> loadNewHomeworkBlackWhiteLists(String businessType, String idType, String blackWhiteId, Pageable pageable) {
        return homeworkBlackWhiteListDao.loadBlackWhiteLists(businessType, idType, blackWhiteId, pageable);
    }

}
