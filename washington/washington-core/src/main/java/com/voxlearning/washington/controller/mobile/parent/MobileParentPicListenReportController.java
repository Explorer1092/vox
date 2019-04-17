package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.UnisoundScoreLevel;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadSentenceResult;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenReportConfig;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenReportDayResult;
import com.voxlearning.utopia.service.vendor.api.entity.StudentFollowReadReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_CREATE_COLLECTION_FAIL;

/**
 * 点读机报告
 *
 * @author jiangpengparen
 * @since 2017-03-17 下午3:43
 **/
@Controller
@RequestMapping(value = "/parentMobile/selfstudy/piclisten/report")
@Slf4j
public class MobileParentPicListenReportController extends AbstractMobileParentSelfStudyController {






    @RequestMapping(value = "/generate_collection.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage generateCollection() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;

        Long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return noLoginResult;
        String unitId = getRequestString("unit_id");
        if (StringUtils.isBlank(unitId))
            return MapMessage.errorMessage("unitId is required");


        StudentFollowReadReport studentFollowReadReport = parentSelfStudyService.loadStudentFollowReadReportData(studentId);
        if (studentFollowReadReport == null)
            return MapMessage.errorMessage("没有对应的跟读详情");

        StudentFollowReadReport.UnitResult unitResult = studentFollowReadReport.getUnitResultList().stream().filter(t -> t.getUnitId().equals(unitId)).findFirst().orElse(null);
        if (unitResult == null)
            return MapMessage.errorMessage("没有对应的跟读详情");

        List<String> scoreIdList = unitResult.getLastReadSentenceResultIds();

        if (CollectionUtils.isEmpty(scoreIdList))
            return MapMessage.errorMessage("没有对应的跟读详情");

        MapMessage mapMessage = parentSelfStudyService.generateFollowReadCollection(studentId, unitId, scoreIdList);
        if ( !mapMessage.isSuccess())
            return MapMessage.errorMessage(RES_RESULT_CREATE_COLLECTION_FAIL);


        Boolean  overLimit = SafeConverter.toBoolean(mapMessage.get("is_over_limit"));
        String collectionId = SafeConverter.toString(mapMessage.get("collection_id"));

        return MapMessage.successMessage().add("over_limit", overLimit).add("content_id", collectionId);
    }


    @RequestMapping(value = "/follow_detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage followReadDetail() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;

        Long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return noLoginResult;
        String unitId = getRequestString("unit_id");
        if (StringUtils.isBlank(unitId))
            return MapMessage.errorMessage("unitId is required");

        StudentFollowReadReport studentFollowReadReport = parentSelfStudyService.loadStudentFollowReadReportData(studentId);
        if (studentFollowReadReport == null)
            return MapMessage.errorMessage("没有对应的跟读详情");

        StudentFollowReadReport.UnitResult unitResult = studentFollowReadReport.getUnitResultList().stream().filter(t -> t.getUnitId().equals(unitId)).findFirst().orElse(null);
        if (unitResult == null)
            return MapMessage.errorMessage("没有对应的跟读详情");
        List<String> lastReadSentenceResultIds = unitResult.getLastReadSentenceResultIds();
        Map<String, FollowReadSentenceResult> readSentenceResultMap = parentSelfStudyService.loadFollowReadSentenceResults(lastReadSentenceResultIds);
        Map<String, Object> unitDetailMap = new LinkedHashMap<>();
        unitDetailMap.put("title", unitResult.getUnitName());
        unitDetailMap.put("average_score", unitResult.getAverageScore());

        Boolean followReadIsPayed = picListenCommonService.parentHasBuyScore(parent.getId());

        Map<String, Object> readSentenceMap = new LinkedHashMap<>();
        readSentenceMap.put("total_sentence_count", unitResult.getTotalSentenceCount());
        readSentenceMap.put("read_sentence_count", unitResult.getReadSentenceCount());
        List<Map<String, Object>> readSentenceMapList = new ArrayList<>();
        List<FollowReadSentenceResult.ScoreResult> redSentenceResultList = new ArrayList<>();  //染红的句子结果
        for (Map.Entry<String, FollowReadSentenceResult> entry : readSentenceResultMap.entrySet()) {
            FollowReadSentenceResult sentenceResult = entry.getValue();
            Map<String, Object> sentenceMap = new LinkedHashMap<>();
            sentenceMap.put("sentence_id", sentenceResult.getSentenceId());
            sentenceMap.put("text", sentenceResult.getScoreResult().getSample());
            if (followReadIsPayed) {
                sentenceMap.put("score", sentenceResult.fetchSentenceScore() == null ? 0 :sentenceResult.fetchSentenceScore().intValue());
            }
            sentenceMap.put("audio_url", sentenceResult.getAudioUrl());
            readSentenceMapList.add(sentenceMap);
            if (sentenceResult.getScoreResult().getWords().stream()
                    .filter(t -> CollectionUtils.isNotEmpty(t.getSubwords()))
                    .anyMatch(t -> SafeConverter.toDouble(t.getScore())*10 <= UnisoundScoreLevel.C.getMaxScore())){
                redSentenceResultList.add(sentenceResult.getScoreResult());
            }

        }
        readSentenceMap.put("sentence_list", readSentenceMapList);

        unitDetailMap.put("read_sentence", readSentenceMap);

        List<List<Map<String, Object>>> redSentenceList = new ArrayList<>();
        List<FollowReadSentenceResult.ScoreResult> wordResultList = new ArrayList<>();
        for (FollowReadSentenceResult.ScoreResult scoreResult : redSentenceResultList) {
            if (scoreResult.getWords().size() == 1) { //如果是单词
                wordResultList.add(scoreResult);
                continue;
            }
            List<Map<String, Object>> sentenceWordMapList = new ArrayList<>();
            scoreResult.getWords().forEach(t -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("word_text", t.getText());
                map.put("is_red", CollectionUtils.isNotEmpty(t.getSubwords()) &&
                        SafeConverter.toDouble(t.getScore())*10 <= UnisoundScoreLevel.C.getMaxScore());
                sentenceWordMapList.add(map);
            });
            redSentenceList.add(sentenceWordMapList);
        }
        //单词的话,放在一句里
        List<Map<String, Object>> redWordMapList = new ArrayList<>();
        for (FollowReadSentenceResult.ScoreResult scoreResult : wordResultList) {
            FollowReadSentenceResult.Word word = scoreResult.getWords().get(0);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("word_text", word.getText());
            map.put("is_red", CollectionUtils.isNotEmpty(word.getSubwords()) &&
                    SafeConverter.toDouble(word.getScore())*10 <= UnisoundScoreLevel.C.getMaxScore());
            redWordMapList.add(map);
        }
        List<List<Map<String, Object>>> redContentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(redWordMapList))
            redContentList.add(redWordMapList);
        if (CollectionUtils.isNotEmpty(redSentenceList))
            redContentList.addAll(redSentenceList);
        unitDetailMap.put("unreach_standard_sentence_list", redContentList);
        Map<String, String> poemTodoMap = followReadDetailPoem(CollectionUtils.isNotEmpty(redContentList), unitResult.getAverageScore());
        unitDetailMap.put("poem", poemTodoMap.get("poem"));
        unitDetailMap.put("todo", poemTodoMap.get("todo"));
        unitDetailMap.put("book_id", unitResult.getBookId());
        unitDetailMap.put("follow_read_payed", followReadIsPayed);
        return MapMessage.successMessage().add("unit_detail", unitDetailMap);

    }

    private Map<String,String> followReadDetailPoem(boolean hasRed, Integer averageScore) {
        Map<String, String> map = new HashMap<>();
        if (hasRed){
            map.put("poem", "跟读句子中以下染红的内容未达标");
            map.put("todo", "重读未达标句子");
            return map;
        }else {
            String poem;
            map.put("todo", "继续本课跟读练习");
            if (averageScore <= 60){
                poem = randomPickOne("谁说万事开头难？我读英语从不烦。只要敢说第一句，万水千山只等闲。",
                        "Never, never, never, never give up. ----永远不要、不要、不要、不要放弃。");
            }else if (averageScore <= 80){
                poem = randomPickOne("宝贝的天赋还需要更多的勤奋和努力来挖掘哦！相信你不会轻言放弃！",
                        "Better late than never. ----只要开始，虽晚不迟。");
            }else if (averageScore <= 85){
                poem = randomPickOne("把每一件简单的事做好，就不简单；把每一件平凡的事完成，就不平凡！",
                        "Action is the proper fruit of knowledge. ----行动是知识之佳果。");
            }else if (averageScore <= 90){
                poem = randomPickOne("宝贝的上进心家长和老师都看得见！加油，每天进步一点点，成功就在你眼前！",
                        "There is no short cut in learning English. ----学习英语没有捷径可走。");
            }else if (averageScore <=95){
                poem = randomPickOne("据说顶尖选手每天都给自己提出更高的要求~~ 看好你呦！",
                        "Well begun is half done. ----好的开始是成功的一半。");
            }else {
                poem = randomPickOne("长风破浪会有时，直挂云帆济沧海。本课读得这么棒，何不分享一下～",
                        "Complacency is the enemy of study. ----自我满足是学习的敌人。");
            }
            map.put("poem", poem);
            return map;
        }
    }
    private <T> T randomPickOne(T... args){
        if (args == null || args.length == 0)
            return null;
        return RandomUtils.pickRandomElementFromList(Arrays.asList(args));
    }


    @RequestMapping(value = "/follow_read_thirsty.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage shareContent() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;

        Long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return noLoginResult;


        AlpsFuture<Boolean> reportHasFollowReadFuture = parentSelfStudyService.reportHasFollowRead(studentId);

        StudentFollowReadReport studentFollowReadReport = parentSelfStudyService.loadStudentFollowReadReportData(studentId);
        Boolean reportHasFollow = reportHasFollowReadFuture.getUninterruptibly();
        if (studentFollowReadReport == null)
            return MapMessage.successMessage().add("follow_read_book_list", new ArrayList<>()).add("has_follow_read", reportHasFollow);
        List<StudentFollowReadReport.UnitResult> unitResultList = studentFollowReadReport.getUnitResultList();
        Map<String, List<StudentFollowReadReport.UnitResult>> book2UnitResultListMap = unitResultList.stream().collect(Collectors.groupingBy(StudentFollowReadReport.UnitResult::getBookId));
        List<Map<String, Object>> followReadBookMapList = new ArrayList<>(book2UnitResultListMap.size());
        for (Map.Entry<String, List<StudentFollowReadReport.UnitResult>> entry : book2UnitResultListMap.entrySet()) {
            String bookId = entry.getKey();
            List<StudentFollowReadReport.UnitResult> unitResults = entry.getValue();
            if (CollectionUtils.isEmpty(unitResults))
                continue;
            Map<String, Object> followReadBookMap = new LinkedHashMap<>();
            followReadBookMap.put("book_id", bookId);
            followReadBookMap.put("book_name", unitResults.get(0).getBookName());
            Boolean hasGroup = StringUtils.isNotBlank(unitResults.get(0).getModuleId());
            followReadBookMap.put("has_unit_group", hasGroup);
            if (hasGroup){
                List<Map<String, Object>> groupMapList = new ArrayList<>();
                List<String> sortedModuleIdList = unitResults.stream().sorted((o1, o2) -> o1.getModuleRank().compareTo(o2.getModuleRank())).map(StudentFollowReadReport.UnitResult::getModuleId).distinct().collect(Collectors.toList());
                Map<String, List<StudentFollowReadReport.UnitResult>> moduleId2UnitResultList = unitResults.stream().collect(Collectors.groupingBy(StudentFollowReadReport.UnitResult::getModuleId));
                for (String moduleId : sortedModuleIdList) {
                    List<StudentFollowReadReport.UnitResult> resultList = moduleId2UnitResultList.get(moduleId);
                    if (CollectionUtils.isEmpty(resultList))
                        continue;
                    Map<String, Object> groupMap = new LinkedHashMap<>();
                    groupMap.put("group_id", moduleId);
                    groupMap.put("group_name", resultList.get(0).getModuleName());
                    List<Map<String, Object>> unitMapList = convert2UnitMapList(resultList);
                    groupMap.put("unit_list", unitMapList);
                    groupMapList.add(groupMap);
                }
                followReadBookMap.put("group_list", groupMapList);
            }else {
                followReadBookMap.put("unit_list", convert2UnitMapList(unitResults));
            }
            followReadBookMapList.add(followReadBookMap);
        }
        return MapMessage.successMessage().add("follow_read_book_list", followReadBookMapList).add("has_follow_read", reportHasFollow);

    }

    private  List<Map<String, Object>> convert2UnitMapList(List<StudentFollowReadReport.UnitResult> unitResultList){
        List<StudentFollowReadReport.UnitResult> sortedUnitResultList = unitResultList.stream().sorted((o1, o2) -> o1.getUnitRank().compareTo(o2.getUnitRank())).collect(Collectors.toList());
        return sortedUnitResultList.stream().map(result -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("unit_id", result.getUnitId());
            map.put("unit_name", result.getUnitName());
            map.put("total_sentence_count", result.getTotalSentenceCount());
            map.put("read_sentence_count", result.getReadSentenceCount());
            map.put("average_score", result.getAverageScore());
            return map;
        }).collect(Collectors.toList());
    }


    @RequestMapping(value = "/score_seven.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage reportScore() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;

        Long studentId = getRequestLong("sid");
        if (studentId == 0 )
            return noLoginResult;
        DayRange currentDay = DayRange.current();
        AlpsFuture<PicListenReportConfig> reportConfigFuture = parentSelfStudyService.loadPicListenReportConfig();
        AlpsFuture<PicListenReportDayResult> dayResultAlpsFuture = parentSelfStudyService.loadReportDayResult(studentId, currentDay);
        AlpsFuture<List<PicListenReportDayResult.DayScoreMapper>> sevenDayScoreListFuture = parentSelfStudyService.loadSevenDayScore(studentId, currentDay);

        //今日学习得分开始
        Map<String, Object> yesterdayResultMap = new LinkedHashMap<>();

        Map<String, Object> comprehensiveScoreMap = new LinkedHashMap<>();
        Map<String, Object> learnTimeResultMap = new LinkedHashMap<>();
        Map<String, Object> playSentenceResultMap = new LinkedHashMap<>();
        Map<String, Object> todaySuggestMap = new LinkedHashMap<>();

        yesterdayResultMap.put("comprehensive_score", comprehensiveScoreMap);
        yesterdayResultMap.put("learn_time_result", learnTimeResultMap);
        yesterdayResultMap.put("play_sentence_result", playSentenceResultMap);
        yesterdayResultMap.put("today_suggest", todaySuggestMap);

        PicListenReportDayResult reportDayResult = dayResultAlpsFuture.getUninterruptibly();
        PicListenReportConfig reportConfig = reportConfigFuture.getUninterruptibly();
        if (reportDayResult == null || reportConfig == null)
            return MapMessage.errorMessage("数据错误");
        //综合得分
        Long reportScore = reportDayResult.getReportScore();
        comprehensiveScoreMap.put("score", reportScore);
        comprehensiveScoreMap.put("comment", generateComment(reportDayResult.getReportScore(), reportConfig.getReportScoreParam(), false));

        //学习时长开
        learnTimeResultMap.put("time", covert2Minutes(reportDayResult.getLearnTime()));
        learnTimeResultMap.put("comment", generateComment(reportDayResult.getLearnTime(), reportConfig.getLearnTimeScoreParam(), true));
        learnTimeResultMap.put("max", covert2Minutes(reportConfig.getLearnTimeScoreParam().getMax()));
        learnTimeResultMap.put("standard", covert2Minutes(reportConfig.getLearnTimeScoreParam().getStandard()));

        //播放句子数
        playSentenceResultMap.put("count", reportDayResult.getPlaySentenceCount());
        playSentenceResultMap.put("comment", generateComment(reportDayResult.getPlaySentenceCount(), reportConfig.getPlaySentenceCountScoreParam(), false));
        playSentenceResultMap.put("max", reportConfig.getPlaySentenceCountScoreParam().getMax());
        playSentenceResultMap.put("standard", reportConfig.getPlaySentenceCountScoreParam().getStandard());

        //跟读句子数
        if (reportDayResult.ifHasFollowRead()){
            Map<String, Object> followReadSentenceResultMap = new LinkedHashMap<>();
            yesterdayResultMap.put("follow_read_sentence_result", followReadSentenceResultMap);
            followReadSentenceResultMap.put("count", reportDayResult.getFollowReadSentenceCount());
            followReadSentenceResultMap
                    .put("comment", generateComment(reportDayResult.getFollowReadSentenceCount(), reportConfig.getFollowReadSentenceCountScoreParam(), false));
            followReadSentenceResultMap.put("max", reportConfig.getFollowReadSentenceCountScoreParam().getMax());
            followReadSentenceResultMap.put("standard", reportConfig.getFollowReadSentenceCountScoreParam().getStandard());
        }

        //今日建议
        Map<String, String> suggestTodo = generateSuggestTodo(reportDayResult, reportConfig);
        todaySuggestMap.put("text", suggestTodo.get("text"));
        todaySuggestMap.put("todo", suggestTodo.get("todo"));
        //今日学习得分结束


        // 近七日分数
        List<PicListenReportDayResult.DayScoreMapper> scoreMapperList = sevenDayScoreListFuture.getUninterruptibly();
        List<Map<String, Object>> scoreMapList = new ArrayList<>(scoreMapperList.size());
        scoreMapperList.forEach(scoreMapper -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("day", scoreMapper.getDay().getMonth() + "/" + scoreMapper.getDay().getDay());
            map.put("score", scoreMapper.getScore());
            scoreMapList.add(map);
        });
        //加上今日分数，是实时算出来的
        Map<String, Object> todayMap = new LinkedHashMap<>();
        todayMap.put("day", currentDay.getMonth() + "/" + currentDay.getDay());
        todayMap.put("score", reportScore);
        scoreMapList.add(todayMap);


        return MapMessage.successMessage().add("yesterday_result", yesterdayResultMap).add("seven_day_score", scoreMapList);
    }

    private Long covert2Minutes(Long learnTime) {
        if (learnTime <= 0)
            return 0L;
        long t = learnTime % 60000;
        if (t > 30000)
            return learnTime/60000 + 1;
        else
            return learnTime/60000;
    }

    private Map<String, String> generateSuggestTodo(PicListenReportDayResult reportDayResult, PicListenReportConfig reportConfig) {
        Map<String, String> map = new HashMap<>();
        Boolean learnTimeReachStandard = isReachStandard(reportDayResult.getLearnTime(), reportConfig.getLearnTimeScoreParam(), true);
        if (!learnTimeReachStandard) {
            map.put("text", "今日学习时长未达标，我们建议宝贝每天至少在点读机上学习10分钟哦。英语学习需要大量输入才能慢慢输出，请家长鼓励宝贝每天坚持英语学习，循序渐进养成良好习惯～");
            map.put("todo", "打开课文，点读学习");
            return map;
        }
        Boolean playSentenceReachStandard = isReachStandard(reportDayResult.getPlaySentenceCount(), reportConfig.getPlaySentenceCountScoreParam(), false);
        if (!playSentenceReachStandard){
            map.put("text", "今日播放句子数量较少，我们建议宝贝每天至少听读10个不同句子～英语学习需要语感培养，每天结合预习+复习听读一定数量的句子，有助于打牢英语基础哦！");
            map.put("todo", "点读课文，培养语感");
            return map;
        }
        if (reportDayResult.ifHasFollowRead() && !isReachStandard(reportDayResult.getFollowReadSentenceCount(), reportConfig.getFollowReadSentenceCountScoreParam(), false)){
            map.put("text", "今日跟读句子数量不足，有待提升哦。宝贝在听力输入的基础上，需要大量模仿才能练就纯正语音。请家长鼓励宝贝多跟读模仿，口语定会进步显著！");
            map.put("todo", "跟读句子，巩固发音");
            return map;
        }
        map.put("text", "不错哦，今日表现堪称一流！坚持每天用点读机保质保量学英语，不懈努力终将有所回报哦～");
        map.put("todo", "坚持学习，继续点读");
        return map;
    }

    private Boolean isReachStandard(Long value, PicListenReportConfig.ScoreParam scoreParam, Boolean isTime){
        if (!isTime)
            return value >= scoreParam.getStandard();
        return covert2Minutes(value) >= covert2Minutes(scoreParam.getStandard());
    }

    private String generateComment(Long value, PicListenReportConfig.ScoreParam scoreParam, Boolean isTime){
        if (!isTime) {
            if (value < scoreParam.getStandard())
                return "偏低";
            if (value < scoreParam.getGood())
                return "良好";
            return "优秀";
        }
        if (covert2Minutes(value) < covert2Minutes(scoreParam.getStandard()))
            return "偏低";
        if (covert2Minutes(value) < covert2Minutes(scoreParam.getGood()))
            return "良好";
        return "优秀";
    }





}
