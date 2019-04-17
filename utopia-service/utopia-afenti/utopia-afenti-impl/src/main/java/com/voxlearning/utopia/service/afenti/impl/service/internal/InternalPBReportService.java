package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankInfo;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.PicBookCacheSystem;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.StudentPicBookRankCacheManager;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.StudentPicBookSchoolRankCacheManager;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.afenti.cache.UserPicBookCache;
import com.voxlearning.utopia.service.afenti.impl.service.UserPicBookServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.convert.SafeConverter.toDouble;
import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress.Attr.*;
import static java.util.Collections.emptySet;

@Named
public class InternalPBReportService extends SpringContainerSupport {

    private static final int FULL_MARKS = 100;
    private static final int PART1_MARKS = 30;
    private static final int PART2_MARKS = 30;
    private static final int MAX_LEARN_TIME = 5;

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private UserPicBookServiceImpl userPicBookSrv;
    @Inject private PictureBookLoaderClient picBookLoader;
    @Inject private PicBookCalScoreService calScoreSrv;
    @Inject private PicBookCacheSystem picBookCacheSystem;
    @Inject private StudentPicBookRankCacheManager studentPicBookRankCacheManager;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private StudentPicBookSchoolRankCacheManager studentPicBookSchoolRankCacheManager;
    @Inject private SchoolLoaderClient schoolLoaderClient;


    private UserPicBookCache cache;

    @AlpsPubsubPublisher(topic = "utopia.picbook.finish.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;


    @Override
    public void afterPropertiesSet() {
        cache = new UserPicBookCache(AfentiCache.getPersistent());
    }


    public MapMessage process(List<UserPicBookResult> records) {
        if (records == null)
            return MapMessage.errorMessage();

        boolean containFinish = records.stream().anyMatch(rd -> SafeConverter.toBoolean(rd.getFinish()));
        MapMessage resultMsg;
        int module = records.get(0).getModule();

        // 第3模块需要记录每个题的情况，供前端展示进度
        // 1模块只用一个标记即可
        // 4模块要算每道题的分，所以也得逐条处理
        if (module == 3 || module == 4) {
            MapMessage tmpMsg = MapMessage.successMessage();
            for (UserPicBookResult record : records) {
                tmpMsg = processReportData(record);
                // 这个不地方就不阻断，万一有一半正确一半错误
                //if(!tmpMsg.isSuccess())
                //    return tmpMsg;
            }

            resultMsg = tmpMsg;
        } else {
            // 最后一条记录，但是要汇总所有记录的duration
            int sumDuration = records.stream()
                    .mapToInt(r -> toInt(r.getDuration()))
                    .sum();

            UserPicBookResult lastResult = records.get(records.size() - 1);
            lastResult.setDuration(sumDuration);

            resultMsg = processReportData(lastResult);
        }

        // 只有遇到模块末尾结束的状态，才返回进度数据让前台刷新
        if (!containFinish) resultMsg.remove("detail");

        return resultMsg;
    }

    private MapMessage processReportData(UserPicBookResult result) {
        Long userId = result.getUserId();
        String bookId = result.getBookId();
        int module = toInt(result.getModule());

        if (userId == null || StringUtils.isEmpty(bookId) || module <= 0) {
            return MapMessage.errorMessage("非法参数");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        String schoolShortName = "";
        if (studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null) {
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(studentDetail.getClazz().getSchoolId()).getUninterruptibly();
            if (school != null) {
                schoolShortName = school.getShortName();
            }
        }

        UserPicBook userPicBook = userPicBookSrv.loadUserPicBook(userId, bookId);
        PictureBookPlus picBook = pictureBookPlusServiceClient.loadById(bookId);
        if (picBook == null)
            return MapMessage.errorMessage("绘本数据不存在");

        boolean finish = SafeConverter.toBoolean(result.getFinish());
        int inputScore = toInt(result.getScore());
        String questionId = result.getQuestionId();

        int totalScore = 0;// 总分
        BigDecimal score = BigDecimal.ZERO;// 当前提交的分数
        int last2Marks = FULL_MARKS - PART1_MARKS - PART2_MARKS; // 后面两个模块的总分
        BigDecimal qScore = calScoreSrv.getSingleQuestionScore(last2Marks, picBook);// 第3、4模块单题的分数
        Long now = new Date().getTime();
        WeekRange thisWeek = WeekRange.current();

        boolean triggerCalScore = false;// 触发算分的标志
        boolean addToMyBooks = true;
        int allModuleNum = CollectionUtils.isEmpty(picBook.getPracticeQuestions()) ? 3 : 4;

        // 用户的进度
        UserPicBookProgress progress = cache.loadProgress(userId, Collections.singletonList(bookId)).get(bookId);
        UserPicBookAchieve achieve = cache.loadAchieve(userId);// 成就

        // 问题ID列表汇总分数的Func
        Function<List<String>, BigDecimal> sumScoreFunc = qIds -> qIds.stream()
                .map(qId -> BigDecimal.valueOf(progress.getQuestionScore(qId)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 绘本是否免费
        boolean isFree = Optional.ofNullable(picBook.getFreeMap())
                .map(m -> m.getOrDefault(ApplyToType.SELF, 0) == 1)
                .orElse(false);
        if (module == 1) {
            // 无论完没完成，都需要记录页码的状态
            String pageId = result.getPageId();
            Set<String> readPages = progress.getReadPages();
            if (readPages == null)
                readPages = new HashSet<>();

            readPages.add(pageId);
            progress.setReadPages(readPages);

            if (finish) {
                score = BigDecimal.valueOf(PART1_MARKS);
                result.setPageId("1");// 完成后，从头再开始

                Set<String> readBookIds = Optional.ofNullable(achieve.getReadBookIds()).orElse(emptySet());
                readBookIds.add(bookId);
                achieve.setReadingNum(readBookIds.size());

                // 处理排行榜
                Long rank = studentPicBookRankCacheManager.updateStudentRank(userId, readBookIds.size(), PicBookRankCategory.READ, thisWeek.getWeekOfYear());
                if (rank != null && rank < 150) {
                    // 补全信息
                    PicBookRankInfo rankInfo = PicBookRankInfo.newInstanceForRank(userId, readBookIds.size(), SafeConverter.toInt(rank), PicBookRankCategory.READ);
                    rankInfo.setClassName(studentDetail.getClazz() != null ? studentDetail.getClazz().formalizeClazzName() : "");
                    rankInfo.setSchoolName(schoolShortName);
                    rankInfo.setImg(studentDetail.fetchImageUrl());
                    rankInfo.setStudentName(studentDetail.fetchRealname());
                    picBookCacheSystem.getStudentPicBookTopRankCacheManager().updateStudentToRank(PicBookRankCategory.READ,
                            rankInfo, WeekRange.current().getWeekOfYear());
                }
                if (studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null) {
                    Long schoolRank = studentPicBookSchoolRankCacheManager.updateStudentRank(userId,
                            readBookIds.size(), PicBookRankCategory.READ, studentDetail.getClazz().getSchoolId(), thisWeek.getWeekOfYear());
                    if (schoolRank != null && schoolRank < 20) {
                        // 补全信息
                        PicBookRankInfo rankInfo = PicBookRankInfo.newInstanceForRank(userId, readBookIds.size(), SafeConverter.toInt(schoolRank), PicBookRankCategory.READ);
                        rankInfo.setClassName(studentDetail.getClazz().formalizeClazzName());
                        rankInfo.setSchoolName(schoolShortName);
                        rankInfo.setImg(studentDetail.fetchImageUrl());
                        rankInfo.setStudentName(studentDetail.fetchRealname());
                        picBookCacheSystem.getStudentPicBookTopSchoolRankCacheManager().updateStudentToRank(studentDetail.getClazz().getSchoolId(),
                                PicBookRankCategory.READ, rankInfo, WeekRange.current().getWeekOfYear());
                    }
                }

            }

            // 此标志是因为霖锐偷懒，传到前端被当做是否把绘本提前到我的绘本头部使用。
            // 只有免费并且未读满两页的情况，不移到头部。
            // 否则但凡是report过的，都算操作过，都要提前
            if (isFree && readPages.size() < 1) {
                addToMyBooks = false;
            }

        } else if (module == 2 && finish) {
            triggerCalScore = true;
            score = BigDecimal.valueOf(PART2_MARKS);

            Set<String> newWords = Optional.ofNullable(achieve.getNewWords()).orElse(emptySet());
            List<String> words = picBook.allNewWords()
                    .stream()
                    .map(w -> w.getEntext())
                    .collect(Collectors.toList());

            newWords.addAll(words);
            achieve.setNewWordsNum(newWords.size());

            // 处理排行榜
            Long rank = studentPicBookRankCacheManager.updateStudentRank(userId, newWords.size(), PicBookRankCategory.WORD, thisWeek.getWeekOfYear());
            if (rank != null && rank < 150) {
                // 补全信息
                PicBookRankInfo rankInfo = PicBookRankInfo.newInstanceForRank(userId, newWords.size(), SafeConverter.toInt(rank), PicBookRankCategory.WORD);
                rankInfo.setClassName(studentDetail.getClazz() != null ? studentDetail.getClazz().formalizeClazzName() : "");
                rankInfo.setSchoolName(schoolShortName);
                rankInfo.setImg(studentDetail.fetchImageUrl());
                rankInfo.setStudentName(studentDetail.fetchRealname());
                picBookCacheSystem.getStudentPicBookTopRankCacheManager().updateStudentToRank(PicBookRankCategory.WORD,
                        rankInfo, WeekRange.current().getWeekOfYear());
            }
            if (studentDetail.getClazz() != null && studentDetail.getClazz().getSchoolId() != null) {
                Long schoolRank = studentPicBookSchoolRankCacheManager.updateStudentRank(userId,
                        newWords.size(), PicBookRankCategory.WORD, studentDetail.getClazz().getSchoolId(), thisWeek.getWeekOfYear());
                if (schoolRank != null && schoolRank < 20) {
                    // 补全信息
                    PicBookRankInfo rankInfo = PicBookRankInfo.newInstanceForRank(userId, newWords.size(), SafeConverter.toInt(schoolRank), PicBookRankCategory.WORD);
                    rankInfo.setClassName(studentDetail.getClazz().formalizeClazzName());
                    rankInfo.setSchoolName(schoolShortName);
                    rankInfo.setImg(studentDetail.fetchImageUrl());
                    rankInfo.setStudentName(studentDetail.fetchRealname());
                    picBookCacheSystem.getStudentPicBookTopSchoolRankCacheManager().updateStudentToRank(studentDetail.getClazz().getSchoolId(),
                            PicBookRankCategory.WORD, rankInfo, WeekRange.current().getWeekOfYear());
                }
            }

        } else if (module == 3) {
            if (StringUtils.isBlank(questionId))
                return MapMessage.errorMessage("问题ID为空!");

            score = calScoreSrv.convertVoiceScore(inputScore, qScore);
            progress.recordScore(questionId, score.doubleValue());

            List<String> oralQIds = picBook.getOralQuestions();
            if (finish) {
                triggerCalScore = true;
                score = sumScoreFunc.apply(picBook.getOralQuestions());
            } else {
                int orgIndex = oralQIds.indexOf(questionId);
                // 如果某一道题做完了，进度记的是下一道题
                questionId = oralQIds.get(Math.min(orgIndex + 1, oralQIds.size() - 1));
            }

        } else if (module == 4) {
            if (StringUtils.isBlank(questionId))
                return MapMessage.errorMessage("问题ID为空!");

            List<List<String>> inputAnswer = result.parseAnswer();
            score = calScoreSrv.calPractiseScore(questionId, inputAnswer, qScore);
            progress.recordScore(questionId, score.doubleValue());

            List<String> practiseQIds = picBook.getPracticeQuestions();
            // 结束的时候算每个模块部分
            if (finish) {
                questionId = practiseQIds.get(0);
                triggerCalScore = true;

                score = sumScoreFunc.apply(picBook.getPracticeQuestions());
            } else {
                int orgIndex = practiseQIds.indexOf(questionId);
                // 如果某一道题做完了，进度记的是下一道题
                questionId = practiseQIds.get(Math.min(orgIndex + 1, practiseQIds.size() - 1));
            }
        }

        // 记录进度
        progress.recordProgress(module, result.getPageId(), questionId, finish, result.getScore(), score.doubleValue(), result.getAudioUrl());

        List<Map<String, Object>> moduleDetail = progress.getModuleDetail();
        if (triggerCalScore && moduleDetail != null) {
            AtomicInteger finisInThisWeekCount = new AtomicInteger(0);// 这周完成数量
            AtomicInteger finishCount = new AtomicInteger(0);// 完成模块数量

            // 统计这周完成的模块个数，以及总完成个数
            Consumer<Map<String, Object>> statTime = md -> {
                Long finishTime = MapUtils.getLong(md, FINISH_TIME.getName());
                if (finishTime != null && thisWeek.contains(finishTime)) {
                    finisInThisWeekCount.incrementAndGet();
                }

                if (SafeConverter.toBoolean(md.get(FINISH.getName())))
                    finishCount.incrementAndGet();
            };

            totalScore = moduleDetail.stream()
                    .peek(statTime)
                    .map(md -> BigDecimal.valueOf(toDouble(md.get(SCORE.getName()))))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(FULL_MARKS))
                    .intValue();

            // 判断第一次完成 ： 完成了所有模块， 并且userPicBook没有总分 为第一次完成该绘本
            if (finishCount.get() >= allModuleNum && userPicBook != null
                    && userPicBook.getScore() == null && !isFree) {
                Map<String, Object> event = new HashMap<>();
                event.put("type", "app_practice");
                event.put("studentId", userId);
                Map<String, Object> attr = new HashMap<>();
                attr.put("appKey", OrderProductServiceType.ELevelReading.name());
                attr.put("score", totalScore);
                event.put("attributes", attr);
                messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(event)));
            }
            
            // 完成了所有模块，并且没有分或者是有分更高则记录
            if (finishCount.get() >= allModuleNum
                    && userPicBook != null
                    && (userPicBook.getScore() == null || userPicBook.getScore() < totalScore)) {
                userPicBook.setScore(totalScore);
            }

            if (finisInThisWeekCount.get() >= allModuleNum) {
                // 记录本周各个绘本获得的分数
                Map<String, Integer> scoreMap = Optional.ofNullable(achieve.getScoreMap()).orElse(new HashMap<>());
                scoreMap.put(bookId, totalScore);

                int sumScore = scoreMap.values()
                        .stream()
                        .mapToInt(s -> s)
                        .sum();

                // 成就平均分要四舍五入取整
                int newAvgScore = BigDecimal.valueOf((double) sumScore / scoreMap.size())
                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                        .intValue();

                achieve.setAverageScore(newAvgScore);
                achieve.setScoreMap(scoreMap);
            }
        }

        // 计算学习时长，最长5分钟
        Long learnTime = achieve.getLearnTime();
        learnTime += Math.min(Math.max(toInt(result.getDuration()), 0), TimeUnit.MINUTES.toMillis(MAX_LEARN_TIME));
        achieve.setLearnTime(learnTime);

        // 记录最近一次阅读时间
        progress.setReadTime(now);

        cache.modifyAchieveCache(userId, achieve);
        cache.modifyProgressCache(userId, bookId, progress);

        // 根据模块数，填充数据
        progress.fill(allModuleNum);

        return MapMessage.successMessage()
                .add("detail", progress)
                .add("achieve", achieve)
                .add("score", Optional.ofNullable(userPicBook)
                        .map(pb -> SafeConverter.toDouble(pb.getScore()))
                        .orElse(0d))
                .add("addToMyBooks", addToMyBooks);
    }

}
