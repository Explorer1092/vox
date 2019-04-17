package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.OutsideReadingLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.OutsideReadingDynamicCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.outside.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.OutsideReadingDynamicCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.*;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.question.api.entity.stone.data.Expression;
import com.voxlearning.utopia.service.question.api.entity.stone.data.NiceExpression;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ReadingOTOBook;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ReadingOTOMission;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = OutsideReadingLoader.class)
public class OutsideReadingLoadermpl extends SpringContainerSupport implements OutsideReadingLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private OutsideReadingAchievementDao outsideReadingAchievementDao;
    @Inject private OutsideReadingDao outsideReadingDao;
    @Inject private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject private OutsideReadingResultDao outsideReadingResultDao;
    @Inject private OutsideReadingProcessResultDao outsideReadingProcessResultDao;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private OutsideReadingCollectionDao outsideReadingCollectionDao;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @Override
    public MapMessage loadBookshelf(Long userId, String cdnUrl) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        StudentBookshelfResp bookshelfResp = new StudentBookshelfResp();
        bookshelfResp.setStudentId(userId);
        bookshelfResp.setStudentName(studentDetail.fetchRealnameIfBlankId());
        bookshelfResp.setStudentImage(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, studentDetail.fetchImageUrl()));

        OutsideReadingAchievement achievement = outsideReadingAchievementDao.load(userId);
        if (achievement != null) {
            bookshelfResp.setTotalReadingCount(achievement.getTotalReadingCount());
            bookshelfResp.setGoldenWordsCount(achievement.getGoldenWordsCount());
        }
        Group groupMapper = raikouSystem.loadStudentGroups(userId)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组信息不存在");
        }

        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupMapper.getId());
        List<String> bookIds = outsideReadings.stream().map(OutsideReading::findBookId).collect(Collectors.toList());
        LinkedHashMap<String, ReadingOTOBook> readingBookMaps = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIds);
        Map<String, OutsideReadingResult> outsideReadingResultMap = outsideReadingResultDao.loads(Lists.transform(outsideReadings, OutsideReading::getId), userId);
        outsideReadings.sort((o1, o2) -> {
            int compare = o2.getCreateAt().compareTo(o1.getCreateAt());
            if (compare == 0) {
                ReadingOTOBook readingOTOBook1 = readingBookMaps.get(o1.findBookId());
                ReadingOTOBook readingOTOBook2 = readingBookMaps.get(o2.findBookId());
                if (readingOTOBook1 != null && readingOTOBook2 != null) {
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    compare = collator.compare(readingOTOBook1.getBookName(), readingOTOBook2.getBookName());
                }
            }
            return compare;
        });

        for (OutsideReading outsideReading : outsideReadings) {
            OutsideReadingPractice practices = outsideReading.getPractices();
            String bookId = practices.getBookId();
            ReadingOTOBook readingOTOBook = readingBookMaps.get(bookId);
            if (readingOTOBook == null) {
                continue;
            }
            StudentBookshelfResp.ShelfBookInfo shelfBookInfo = new StudentBookshelfResp.ShelfBookInfo();
            shelfBookInfo.setOutsideReadingId(outsideReading.getId());
            shelfBookInfo.setBookId(bookId);
            shelfBookInfo.setCoverPic(readingOTOBook.getCoverPic());
            shelfBookInfo.setBookName(readingOTOBook.getBookName());
            shelfBookInfo.setEndTime(outsideReading.getEndTime());
            shelfBookInfo.setTotalMission(practices.getMissions().size());
            OutsideReadingResult outsideReadingResult = outsideReadingResultMap.get(OutsideReadingResult.generateId(outsideReading.getId(), userId));
            if (outsideReadingResult != null) {
                long finishedCount = outsideReadingResult.getNotNullMissionResults().values().stream().filter(OutsideReadingMissionResult::isFinished).count();
                shelfBookInfo.setFinishMission(SafeConverter.toInt(finishedCount));
                if (finishedCount > 0) {
                    String missionName = practices.getMissions().get(SafeConverter.toInt(finishedCount - 1)).getMissionName();
                    shelfBookInfo.setDynamic(StringUtils.join("已完成", missionName, "的阅读打卡"));
                }
            }
            bookshelfResp.getShelfBookInfos().add(shelfBookInfo);
        }
        return MapMessage.successMessage().add("result", bookshelfResp);
    }


    @Override
    public Map<String, Boolean> loadOutsideReadingStatus(Long userId) {
        Group groupMapper = raikouSystem.loadStudentGroups(userId)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        if (groupMapper == null) {
            return Collections.emptyMap();
        }

        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupMapper.getId());
        Map<String, OutsideReading> outsideReadingMap = outsideReadings.stream().collect(Collectors.toMap(OutsideReading::getId, Function.identity()));
        Map<String, OutsideReadingResult> outsideReadingResultMap = outsideReadingResultDao.loads(Lists.transform(outsideReadings, OutsideReading::getId), userId);
        Set<String> finishedReadingIds = outsideReadingResultMap.values().stream().filter(OutsideReadingResult::isFinished).map(OutsideReadingResult::getReadingId).collect(Collectors.toSet());

        return MapUtils.transform(outsideReadingMap, reading -> finishedReadingIds.contains(reading.getId()));
    }

    @Override
    public List<OutsideReading> loadUnFinishedOutsideReadings(Long userId) {
        Group groupMapper = raikouSystem.loadStudentGroups(userId)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        if (groupMapper == null) {
            return Collections.emptyList();
        }

        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupMapper.getId());
        Map<String, OutsideReadingResult> outsideReadingResultMap = outsideReadingResultDao.loads(Lists.transform(outsideReadings, OutsideReading::getId), userId).values()
                .stream()
                .collect(Collectors.toMap(OutsideReadingResult::getReadingId, Function.identity()));
        return outsideReadings.stream()
                .filter(reading -> !(outsideReadingResultMap.get(reading.getId()) != null && outsideReadingResultMap.get(reading.getId()).isFinished())
                        && reading.getEndTime() != null)
                .sorted((o1, o2) -> Long.compare(o2.getEndTime().getTime(), o1.getEndTime().getTime()))
                .collect(Collectors.toList());
    }


    @Override
    public MapMessage loadStudentBookDetail(Long userId, String outsideReadingId) {
        OutsideReading outsideReading = outsideReadingDao.load(outsideReadingId);
        if (outsideReading == null || outsideReading.findBookId() == null) {
            return MapMessage.errorMessage("课外阅读不存在");
        }
        LinkedHashMap<String, ReadingOTOBook> readingOTOBookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(Collections.singleton(outsideReading.findBookId()));
        ReadingOTOBook readingOTOBook = readingOTOBookMap.get(outsideReading.findBookId());
        if (readingOTOBook == null) {
            return MapMessage.errorMessage("图书信息不存在");
        }

        StudentBookDetailResp bookDetailResp = new StudentBookDetailResp();
        bookDetailResp.setOutsideReadingId(outsideReadingId);
        bookDetailResp.setBookId(outsideReading.findBookId());
        bookDetailResp.setBookName(readingOTOBook.getBookName());
        bookDetailResp.setAuthor(readingOTOBook.getAuthor());
        bookDetailResp.setCoverPic(readingOTOBook.getCoverPic());
        bookDetailResp.setDescription(readingOTOBook.getDescription());
        bookDetailResp.setTotalWords(readingOTOBook.getTotalNum());
        bookDetailResp.setEndTime(outsideReading.getEndTime());

        OutsideReadingResult outsideReadingResult = outsideReadingResultDao.initOutsideReadingResult(outsideReadingId, outsideReading.findBookId(), userId);
        if (outsideReadingResult == null) {
            return MapMessage.errorMessage("系统繁忙, 请稍后重试");
        }
        List<String> missionIds = Lists.transform(outsideReading.findMissions(), OutsideReadingMission::getMissionId);
        LinkedHashMap<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(missionIds);

        String missionStatus = StudentBookDetailResp.MissionStatus.UNLOCK.name();
        for (OutsideReadingMission readingMission : outsideReading.findMissions()) {
            String missionId = readingMission.getMissionId();
            StudentBookDetailResp.Mission mission = new StudentBookDetailResp.Mission();
            mission.setDoUrl(UrlUtils.buildUrlQuery("/student/outside/reading/do" + Constants.AntiHijackExt,
                    MapUtils.m("outsideReadingId", outsideReadingId, "missionId", missionId)));
            mission.setMissionId(missionId);
            mission.setMissionName(readingMission.getMissionName());
            OutsideReadingMissionResult missionResult = outsideReadingResult.getMissionResult(missionId);
            mission.setStatus(missionStatus);
            if (missionResult != null && missionResult.isFinished()) {
                mission.setStar(missionResult.getStar());
            } else {
                missionStatus = StudentBookDetailResp.MissionStatus.LOCK.name();
            }

            ReadingOTOMission readingOTOMission = missionMap.get(missionId);
            if (readingOTOMission != null) {
                mission.setMissionWords(readingOTOMission.getTotalNum());
                mission.setMissionDescription(readingOTOMission.getDescription());
                //导读
                StudentBookDetailResp.LeadinAudio leadinAudio = new StudentBookDetailResp.LeadinAudio();
                leadinAudio.setAudioTitle(readingOTOMission.getLeadinAudioTitle());
                leadinAudio.setAudioUrl(readingOTOMission.getLeadinAudio());
                leadinAudio.setAudioDuration(readingOTOMission.getLeadinAudioDuration());
                bookDetailResp.getLeadinAudios().add(leadinAudio);
                bookDetailResp.getMissions().add(mission);
            }
        }
        return MapMessage.successMessage().add("result", bookDetailResp);
    }

    @Override
    public OutsideReading findOutsideReadingById(String outsideReadingId) {
        return outsideReadingDao.load(outsideReadingId);
    }

    @Override
    public MapMessage loadQuestions(String outsideReadingId, String missionId) {
        OutsideReading outsideReading = findOutsideReadingById(outsideReadingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage().setInfo("阅读任务不存在");
        }
        LinkedHashMap<String, OutsideReadingMission> missionMap = outsideReading.getMissionMap();
        if (missionMap == null) {
            return MapMessage.errorMessage().setInfo("阅读任务关卡列表为空");
        }
        OutsideReadingMission mission = missionMap.get(missionId);
        if (mission == null) {
            return MapMessage.errorMessage().setInfo("阅读任务关卡为空");
        }
        List<String> qids = new ArrayList<>();
        qids.addAll(mission.getQuestionIds());
        qids.addAll(mission.getSubjectiveQuestionIds());
        return MapMessage.successMessage().add("qids", qids);
    }

    @Override
    public MapMessage loadQuestionsAnswer(Long studentId, String outsideReadingId, String missionId) {
        Map<String, Object> questionAnswerMap = new HashMap<>();
        OutsideReadingResult outsideReadingResult = outsideReadingResultDao.load(outsideReadingId, studentId);
        if (outsideReadingResult == null) {
            return MapMessage.errorMessage().setInfo("学生没有开始过此阅读任务");
        }
        OutsideReadingMissionResult ormr = outsideReadingResult.getMissionResult(missionId);
        if (ormr == null) {
            return MapMessage.successMessage().add("answers", questionAnswerMap);
        }
        Collection<String> processIds = ormr.getAnswers().values();
        Map<String, OutsideReadingProcessResult> processResultsMap = outsideReadingProcessResultDao.loads(processIds);
        if (processResultsMap.isEmpty()) {
            return MapMessage.successMessage().add("answers", questionAnswerMap);
        }
        for (OutsideReadingProcessResult orpr : processResultsMap.values()) {
            Map<String, Object> value = MapUtils.m(
                    "files", getFileUrls(orpr),
                    "subMaster", orpr.getSubGrasp(),
                    "master", orpr.getGrasp(),
                    "userAnswers", orpr.getUserAnswers()
            );
            questionAnswerMap.put(orpr.getQuestionId(), value);
        }
        return MapMessage.successMessage().add("answers", questionAnswerMap);
    }

    @NotNull
    private List<List<String>> getFileUrls(OutsideReadingProcessResult orpr) {
        List<List<String>> imgList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(orpr.getFiles())) {
            for (List<NewHomeworkQuestionFile> fileList : orpr.getFiles()) {
                if (CollectionUtils.isNotEmpty(fileList)) {
                    List<String> imgs = new LinkedList<>();
                    for (NewHomeworkQuestionFile file : fileList) {
                        imgs.add("https://oss-image.17zuoye.com/" + file.getRelativeUrl());
                    }
                    imgList.add(imgs);
                }
            }
        }
        return imgList;
    }

    @Override
    public MapMessage goldenWordsIndex(Long userId, String outsideReadingId, String missionId) {
        OutsideReading outsideReading = findOutsideReadingById(outsideReadingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在");
        }
        NiceExpression niceExpression = fetchNiceExpressionByMissionId(missionId);
        if (niceExpression == null || CollectionUtils.isEmpty(niceExpression.getExpressions())) {
            return MapMessage.errorMessage("没有找到关卡对应的好词好句");
        }
        List<Expression> expressions = niceExpression.getExpressions();

        Map<String, OutsideReadingCollection> outsideReadingCollectionMap = outsideReadingCollectionDao.loadOutsideReadingCollectionByStudentId(userId);
        List<Map<String, Object>> goldenWordsList = new LinkedList<>();
        for (int i = 0; i < expressions.size(); i++) {
            Expression expression = expressions.get(i);
            OutsideReadingCollection.ID id = new OutsideReadingCollection.ID(userId, outsideReading.findBookId(), missionId, i);
            Map<String, Object> goldenWordsMap = MapUtils.m("goldenWords", expression.getExpression(), "selected", outsideReadingCollectionMap.containsKey(id.toString()));
            goldenWordsList.add(goldenWordsMap);
        }

        return MapMessage.successMessage()
                .add("goldenWordsTitle", niceExpression.getExpressionTitle())
                .add("goldenWordsList", goldenWordsList);
    }


    @Override
    public MapMessage fetchMissionAchievement(Long userId, String outsideReadingId, String missionId) {
        OutsideReadingResult readingResult = outsideReadingResultDao.load(outsideReadingId, userId);
        OutsideReadingMissionResult missionResult = readingResult.getMissionResult(missionId);
        if (missionResult == null) {
            return MapMessage.errorMessage("没有找到关卡对应的好词好句");
        }

        Integer star = missionResult.getStar();
        Double addReadingCount = 0D;
        if (star != null && star == 3) {
            LinkedHashMap<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(Collections.singleton(missionId));
            ReadingOTOMission readingOTOMission = missionMap.get(missionId);
            if (readingOTOMission != null) {
                addReadingCount += readingOTOMission.getTotalNum();
            }
        }

        OutsideReadingAchievement achievement = outsideReadingAchievementDao.load(userId);
        return MapMessage.successMessage()
                .add("readingFinished", readingResult.isFinished())
                .add("star", star)
                .add("addReadingCount", addReadingCount)
                .add("totalReadingCount", achievement == null ? 0 : achievement.getTotalReadingCount());
    }


    @Override
    public MapMessage fetchAchievement(Long userId, String cdnUrl) {
        Group groupMapper = raikouSystem.loadStudentGroups(userId)
                .stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        if (groupMapper == null) {
            return MapMessage.errorMessage("班组信息不存在");
        }
        List<User> groupStudents = studentLoaderClient.loadGroupStudents(groupMapper.getId());
        List<Long> studentIds = Lists.transform(groupStudents, User::getId);
        Map<Long, OutsideReadingAchievement> achievementMap = outsideReadingAchievementDao.loads(studentIds);
        OutsideReadingAchievement myAchievement = achievementMap.get(userId);

        //成就
        double groupTotalReadingCount = achievementMap.values().stream().mapToDouble(OutsideReadingAchievement::getTotalReadingCount).sum();
        Double avgReadingCount = new BigDecimal(groupTotalReadingCount).divide(new BigDecimal(groupStudents.size()), 10, BigDecimal.ROUND_HALF_UP).doubleValue();
        ReadingAchievementResp achievementResp = new ReadingAchievementResp();
        achievementResp.setAvgReadingCount(avgReadingCount);
        if (myAchievement != null) {
            achievementResp.setTotalReadingCount(myAchievement.getTotalReadingCount());
        }

        //动态
        OutsideReadingDynamicCacheManager cacheManager = newHomeworkCacheService.getOutsideReadingDynamicCacheManager();
        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupMapper.getId());
        //展示最近30天的动态
        List<OutsideReadingDynamicCacheMapper> groupReadingDynamics = cacheManager.loadDynamicByReadingIds(Lists.transform(outsideReadings, OutsideReading::getId))
                .stream()
                .filter(dynamic -> System.currentTimeMillis() - dynamic.getFinishAt().getTime() < DateUtils.DAY_TIME_LENGTH_IN_MILLIS * 30)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(groupReadingDynamics)) {
            groupReadingDynamics.forEach(dynamic -> dynamic.setStudentImage(NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, dynamic.getStudentImage())));
        }
        achievementResp.setReadingDynamics(groupReadingDynamics);

        //好词好句
        Map<String, OutsideReadingCollection> readingCollectionMap = outsideReadingCollectionDao.loadOutsideReadingCollectionByStudentId(userId);
        List<String> labels = readingCollectionMap.values().stream().map(OutsideReadingCollection::getLabels).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        achievementResp.setLabels(labels);
        return MapMessage.successMessage().add("achievement", achievementResp);
    }

    @Override
    public MapMessage loadReportBookList(Long teacherId, Long groupId) {
        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupId);
        List<TeacherReportBookListResp> teacherReportBookList = new LinkedList<>();
        if (CollectionUtils.isEmpty(outsideReadings)) {
            return MapMessage.successMessage().add("teacherReportBookList", teacherReportBookList);
        }

        List<String> bookIds = Lists.transform(outsideReadings, OutsideReading::findBookId);
        LinkedHashMap<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIds);
        Map<String, Double> readingFinishRateMap = fetchOutsideReadingFinishRate(outsideReadings, Collections.singleton(groupId));
        outsideReadings.sort((o1, o2) -> {
            int compare = o2.getCreateAt().compareTo(o1.getCreateAt());
            if (compare == 0) {
                ReadingOTOBook readingOTOBook1 = bookMap.get(o1.findBookId());
                ReadingOTOBook readingOTOBook2 = bookMap.get(o2.findBookId());
                if (readingOTOBook1 != null && readingOTOBook2 != null) {
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    compare = collator.compare(readingOTOBook1.getBookName(), readingOTOBook2.getBookName());
                }
            }
            return compare;
        });

        for (OutsideReading outsideReading : outsideReadings) {
            TeacherReportBookListResp resp = new TeacherReportBookListResp();
            resp.setReadingId(outsideReading.getId());
            resp.setBookId(outsideReading.findBookId());
            ReadingOTOBook readingOTOBook = bookMap.get(outsideReading.findBookId());
            if (readingOTOBook == null) {
                continue;
            }
            resp.setBookName(readingOTOBook.getBookName());
            resp.setCoverPic(readingOTOBook.getCoverPic());
            resp.setEndTime(outsideReading.getEndTime());
            resp.setRemainMs(outsideReading.getEndTime().getTime() - System.currentTimeMillis());
            resp.setFinishRate(readingFinishRateMap.get(outsideReading.getId()));
            teacherReportBookList.add(resp);
        }
        return MapMessage.successMessage().add("teacherReportBookList", teacherReportBookList);
    }

    /**
     * 查询课外阅读任务完成率
     *
     * @param outsideReadings 阅读任务
     * @param groupIds        阅读任务对应的班组ID
     * @return <readingId, finishRate>
     */
    public Map<String, Double> fetchOutsideReadingFinishRate(Collection<OutsideReading> outsideReadings, Collection<Long> groupIds) {
        Map<Long, List<Long>> groupStudentIdsMap = studentLoaderClient.loadGroupStudentIds(groupIds);
        Map<String, OutsideReadingResult> outsideReadingResultMap = outsideReadingResultDao.loads(outsideReadings, groupStudentIdsMap);

        Map<String, Double> finishRateMap = new HashMap<>();
        Map<String, List<OutsideReadingResult>> readingIdResultsMap = outsideReadingResultMap.values().stream().collect(Collectors.groupingBy(OutsideReadingResult::getReadingId));
        for (OutsideReading outsideReading : outsideReadings) {
            List<OutsideReadingResult> outsideReadingResults = readingIdResultsMap.get(outsideReading.getId());
            List<Long> studentIds = groupStudentIdsMap.get(outsideReading.getClazzGroupId());
            Double finishRate = getReadingFinishRate(studentIds, outsideReading, outsideReadingResults);
            finishRateMap.put(outsideReading.getId(), finishRate);
        }
        return finishRateMap;
    }

    @NotNull
    private Double getReadingFinishRate(List<Long> studentIds, OutsideReading outsideReading, Collection<OutsideReadingResult> outsideReadingResults) {
        Double finishRate = 0D;
        if (CollectionUtils.isNotEmpty(outsideReadingResults)) {
            long finishCount = outsideReadingResults.stream().flatMap(mission -> mission.getNotNullMissionResults().values().stream()).filter(OutsideReadingMissionResult::isFinished).count();
            int groupStudentCount = studentIds.size();
            int missionCount = outsideReading.getMissionMap().size();

            finishRate = new BigDecimal(finishCount * 100)
                    .divide(new BigDecimal(groupStudentCount * missionCount), 0, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        }
        return finishRate;
    }

    @Override
    public MapMessage loadReportBookDetail(String readingId) {
        OutsideReading outsideReading = outsideReadingDao.load(readingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在了");
        }
        LinkedHashMap<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(Collections.singleton(outsideReading.findBookId()));
        ReadingOTOBook readingOTOBook = bookMap.get(outsideReading.findBookId());
        if (readingOTOBook == null) {
            return MapMessage.errorMessage("没有找到你要查看的图书信息");
        }

        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(outsideReading.getClazzGroupId());
        Map<String, OutsideReadingResult> outsideReadingResultMap = outsideReadingResultDao.loads(outsideReading.getId(), studentIds);
        Double readingFinishRate = getReadingFinishRate(studentIds, outsideReading, outsideReadingResultMap.values());
        Map<Long, OutsideReadingResult> studentResultMap = outsideReadingResultMap.values().stream().collect(Collectors.toMap(OutsideReadingResult::getStudentId, Function.identity()));

        TeacherReportBookDetailResp resp = new TeacherReportBookDetailResp();
        resp.setReadingId(outsideReading.getId());
        resp.setBookId(outsideReading.findBookId());
        resp.setBookName(readingOTOBook.getBookName());
        resp.setCoverPic(readingOTOBook.getCoverPic());
        resp.setAuthor(readingOTOBook.getAuthor());
        resp.setTotalWords(readingOTOBook.getTotalNum());
        resp.setEndTime(outsideReading.getEndTime());
        resp.setRemainMs(outsideReading.getEndTime().getTime() - System.currentTimeMillis());
        int missionCount = outsideReading.getMissionMap().size();
        resp.setLeadinAudioCount(missionCount);
        resp.setMissionCount(missionCount);
        resp.setGroupAvgFinishRate(readingFinishRate);
        List<User> groupStudents = studentLoaderClient.loadGroupStudents(outsideReading.getClazzGroupId());
        for (User student : groupStudents) {
            OutsideReadingResult outsideReadingResult = studentResultMap.get(student.getId());
            long finishCount = 0L;
            Date lastFinishAt = new Date();
            if (outsideReadingResult != null) {
                List<OutsideReadingMissionResult> missionResults = outsideReadingResult.getNotNullMissionResults().values().stream().filter(OutsideReadingMissionResult::isFinished).collect(Collectors.toList());
                finishCount = missionResults.size();
                if (finishCount != 0L) {
                    OutsideReadingMissionResult missionResult = missionResults.get(missionResults.size() - 1);
                    lastFinishAt = missionResult.getFinishAt();
                }
            }
            resp.getStudentDetails().add(new TeacherReportBookDetailResp.StudentDetail(student.getId(), student.fetchRealnameIfBlankId(), SafeConverter.toInt(finishCount), lastFinishAt));
        }
        List<TeacherReportBookDetailResp.StudentDetail> studentDetails = resp.getStudentDetails();
        if (CollectionUtils.isNotEmpty(studentDetails)) {
            studentDetails.sort((a, b) -> {
                int compareResult = Integer.compare(b.getFinishCount(), a.getFinishCount());
                if (compareResult == 0) {
                    return a.getLastFinishAt().compareTo(b.getLastFinishAt());
                } else {
                    return compareResult;
                }
            });
        }
        return MapMessage.successMessage().add("teacherReportBookDetail", resp);
    }

    @Override
    public MapMessage fetchAnswerDetail(String readingId, String cdnUrl) {
        OutsideReading outsideReading = outsideReadingDao.load(readingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在了");
        }
        List<User> groupStudents = studentLoaderClient.loadGroupStudents(outsideReading.getClazzGroupId());
        Map<Long, User> userMap = groupStudents.stream().collect(Collectors.toMap(User::getId, Function.identity(), (o1, o2) -> o1));
        Map<String, OutsideReadingResult> readingResultMap = outsideReadingResultDao.loads(readingId, Lists.transform(groupStudents, User::getId));
        List<String> allSubjectiveQIds = outsideReading.findAllSubjectiveQuestionIds();

        //获取所有的processResultId
        List<String> processResultIds = new LinkedList<>();
        for (OutsideReadingResult readingResult : readingResultMap.values()) {
            Map<String, OutsideReadingMissionResult> missionResults = readingResult.getNotNullMissionResults();
            for (OutsideReadingMissionResult missionResult : missionResults.values()) {
                Map<String, String> answers = Maps.filterKeys(missionResult.getAnswers(), allSubjectiveQIds::contains);
                processResultIds.addAll(answers.values());
            }
        }

        Map<String, OutsideReadingProcessResult> readingProcessResultMap = outsideReadingProcessResultDao.loads(processResultIds);
        Map<String, List<OutsideReadingProcessResult>> questionProcessResultMap = readingProcessResultMap.values().stream().collect(Collectors.groupingBy(OutsideReadingProcessResult::getQuestionId));
        List<Map<String, Object>> answerDetailList = new LinkedList<>();
        for (String questionId : allSubjectiveQIds) {
            Map<String, Object> answerDetailMap = new LinkedHashMap<>();
            answerDetailMap.put("questionId", questionId);

            List<OutsideReadingProcessResult> questionProcessResults = questionProcessResultMap.get(questionId);
            List<Map<String, Object>> studentDetailList = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(questionProcessResults)) {
                questionProcessResults.sort(Comparator.comparing(OutsideReadingProcessResult::getCreateAt).reversed());
                for (OutsideReadingProcessResult processResult : questionProcessResults) {
                    User user = userMap.get(processResult.getUserId());
                    Date finishAt = processResult.getCreateAt();
                    String showTime = DateUtils.dateToString(finishAt, "yyyy-MM-dd");
                    studentDetailList.add(MapUtils.m("studentId", processResult.getUserId(),
                            "studentName", user.fetchRealnameIfBlankId(),
                            "studentImage", NewHomeworkUtils.getUserAvatarImgUrl(cdnUrl, user.fetchImageUrl()),
                            "finishAt", finishAt,
                            "showTime", showTime,
                            "processResultId", processResult.getId(),
                            "fileUrls", getFileUrls(processResult)));
                }
            }
            answerDetailMap.put("studentDetail", studentDetailList);
            answerDetailList.add(answerDetailMap);
        }

        return MapMessage.successMessage().add("answerDetail", answerDetailList);
    }

    @Override
    public MapMessage fetchAnswerShareDetail(String readingId, String processResultId) {
        OutsideReading outsideReading = outsideReadingDao.load(readingId);
        if (outsideReading == null) {
            return MapMessage.errorMessage("阅读任务不存在了");
        }
        LinkedHashMap<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(Collections.singleton(outsideReading.findBookId()));
        ReadingOTOBook readingOTOBook = bookMap.get(outsideReading.findBookId());
        if (readingOTOBook == null) {
            return MapMessage.errorMessage("没有找到你要查看的图书信息");
        }
        int missionCount = outsideReading.getMissionMap().size();
        OutsideReadingProcessResult processResult = outsideReadingProcessResultDao.load(processResultId);
        if (processResult == null) {
            return MapMessage.errorMessage("学生做题详情不存在");
        }
        List<List<String>> fileUrls = getFileUrls(processResult);
        Student student = studentLoaderClient.loadStudent(processResult.getUserId());

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("bookId", outsideReading.findBookId());
        resMap.put("bookName", readingOTOBook.getBookName());
        resMap.put("coverPic", readingOTOBook.getCoverPic());
        resMap.put("totalWords", readingOTOBook.getTotalNum());
        resMap.put("leadinAudioCount", missionCount);
        resMap.put("missionCount", missionCount);
        resMap.put("studentName", student.fetchRealnameIfBlankId());
        resMap.put("questionId", processResult.getQuestionId());
        resMap.put("fileUrls", fileUrls);
        return MapMessage.successMessage().add("answerShareInfo", resMap);
    }

    @Override
    public MapMessage loadReportClazzAchievement(Long groupId) {
        List<User> groupStudents = studentLoaderClient.loadGroupStudents(groupId);
        List<Long> studentIds = Lists.transform(groupStudents, User::getId);
        if (CollectionUtils.isEmpty(studentIds)) {
            return MapMessage.successMessage("班级没有学生,请去班级管理添加学生");
        }

        List<OutsideReading> outsideReadings = loadOutsideReadingByGroupId(groupId);
        if (CollectionUtils.isEmpty(outsideReadings)) {
            return MapMessage.successMessage("当前班级没有进行中的阅读计划, 快去推荐图书吧");
        }

        Map<Long, OutsideReadingAchievement> achievementMap = outsideReadingAchievementDao.loads(studentIds);
        //总成就
        double groupTotalReadingCount = achievementMap.values().stream().mapToDouble(OutsideReadingAchievement::getTotalReadingCount).sum();
        double groupTotalGoldenWordsCount = achievementMap.values().stream().mapToDouble(OutsideReadingAchievement::getGoldenWordsCount).sum();
        Double avgReadingCount = new BigDecimal(groupTotalReadingCount).divide(new BigDecimal(groupStudents.size()), 10, BigDecimal.ROUND_HALF_UP).doubleValue();
        Double avgGoldenWordsCount = new BigDecimal(groupTotalGoldenWordsCount).divide(new BigDecimal(groupStudents.size()), 10, BigDecimal.ROUND_HALF_UP).doubleValue();
        ClazzAchievementResp achievementResp = new ClazzAchievementResp();
        achievementResp.setAvgReadingCount(avgReadingCount);
        achievementResp.setAvgGoldenWordsCount(avgGoldenWordsCount);

        //班级没有学生阅读成就, 直接返回
        if (MapUtils.isEmpty(achievementMap)) {
            return MapMessage.successMessage().add("achievement", achievementResp);
        }
        //学生成就详情
        for (User student : groupStudents) {
            OutsideReadingAchievement achievement = achievementMap.get(student.getId());
            ClazzAchievementResp.StudentAchievement studentAchievement = new ClazzAchievementResp.StudentAchievement(
                    student.getId(),
                    student.fetchRealnameIfBlankId(),
                    achievement == null ? 0D : achievement.getTotalReadingCount(),
                    achievement == null ? 0 : achievement.getGoldenWordsCount());

            achievementResp.getStudentAchievements().add(studentAchievement);
        }

        //成就字数&好词好句数量排序
        achievementResp.getStudentAchievements().sort((o1, o2) -> {
            int compare = Double.compare(o2.getTotalReadingCount(), o1.getTotalReadingCount());
            if (compare == 0) {
                compare = Integer.compare(o2.getGoldenWordsCount(), o1.getGoldenWordsCount());
            }
            return compare;
        });
        return MapMessage.successMessage().add("achievement", achievementResp);
    }

    @Override
    public Map<Long, List<OutsideReading>> loadOutsideReadingByGroupId(Collection<Long> groupIds) {
        return outsideReadingDao.loadOutsideReadingByClazzGroupIds(groupIds);
    }

    @Override
    public MapMessage fetchGoldenWordsList(Long userId, String labelParam, Integer pageNum, Integer pageSize) {
        Map<String, OutsideReadingCollection> readingCollectionMap = outsideReadingCollectionDao.loadOutsideReadingCollectionByStudentId(userId);
        Set<String> bookIds = readingCollectionMap.values().stream().map(OutsideReadingCollection::getBookId).collect(Collectors.toSet());
        LinkedHashMap<String, ReadingOTOBook> otoBookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIds);

        List<GoldenWordsResp> goldenWordsList = new LinkedList<>();
        for (OutsideReadingCollection collection : readingCollectionMap.values()) {
            ReadingOTOBook readingOTOBook = otoBookMap.get(collection.getBookId());
            if (readingOTOBook != null && collection.getLabels().contains(labelParam)) {
                goldenWordsList.add(new GoldenWordsResp(collection.getId(), collection.getGoldenWordsContent(), readingOTOBook.getBookName(), collection.getCreateAt()));
            }
        }

        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        Page<GoldenWordsResp> bookPage = PageableUtils.listToPage(goldenWordsList, pageable);
        return MapMessage.successMessage()
                .add("bookList", bookPage.getContent())
                .add("totalPages", bookPage.getTotalPages())
                .add("elementSize", bookPage.getTotalElements());
    }

    @Override
    public MapMessage crmLoadOutsideReadingsByGroupId(Long groupId) {
        List<OutsideReading> outsideReadingList = loadOutsideReadingByGroupId(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(outsideReadingList)) {
            Set<String> bookIdSet = outsideReadingList.stream().map(OutsideReading::findBookId).collect(Collectors.toSet());
            Map<String, ReadingOTOBook> bookMap = stoneDataLoaderClient.loadReadingOTOBookByIds(bookIdSet);
            outsideReadingList.forEach(
                    outsideReading -> {
                        ReadingOTOBook book = bookMap.get(outsideReading.findBookId());
                        Map<String, Object> readingMapper = MapUtils.m(
                                "id", outsideReading.getId(),
                                "bookId", outsideReading.findBookId(),
                                "bookName", book != null ? book.getBookName() : "",
                                "createAt", outsideReading.getCreateAt(),
                                "endTime", outsideReading.getEndTime()
                        );
                        result.add(readingMapper);
                    }
            );
        }
        return MapMessage.successMessage().add("list", result);
    }

    public NiceExpression fetchNiceExpressionByMissionId(String missionId) {
        LinkedHashMap<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(Collections.singleton(missionId));
        ReadingOTOMission readingOTOMission = missionMap.get(missionId);
        if (readingOTOMission == null) {
            return null;
        }
        return readingOTOMission.getNiceExpressions();
    }


    public List<OutsideReading> loadOutsideReadingByGroupId(Long groupId) {
        Map<Long, List<OutsideReading>> outsideReadingMap = outsideReadingDao.loadOutsideReadingByClazzGroupIds(Collections.singleton(groupId));
        return outsideReadingMap.get(groupId);
    }

}
