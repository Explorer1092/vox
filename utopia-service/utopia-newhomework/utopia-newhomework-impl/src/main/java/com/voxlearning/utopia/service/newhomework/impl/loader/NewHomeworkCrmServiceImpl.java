package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmService;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.entity.classifyimages.OcrClassifyImages;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.RepairHomeworkDataParam;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.DubbingSyntheticHistoryDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkBlackWhiteListDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkFinishRewardInParentAppDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages.OcrClassifyImagesPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkProcessResultShardDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.queue.DubbingSyntheticQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.SelfStudyHomeworkGenerateServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/8
 */
@Named
@ExposeService(interfaceClass = NewHomeworkCrmService.class)
public class NewHomeworkCrmServiceImpl implements NewHomeworkCrmService {

    @Inject
    private SubHomeworkDao subHomeworkDao;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;
    @Inject
    private NewHomeworkCrmLoaderImpl newHomeworkCrmLoader;
    @Inject
    private NewHomeworkFinishRewardInParentAppDao newHomeworkFinishRewardInParentAppDao;
    @Inject
    private DubbingSyntheticQueueProducer dubbingSyntheticQueueProducer;
    @Inject
    private DubbingSyntheticHistoryDao dubbingSyntheticHistoryDao;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SubHomeworkProcessResultShardDao subHomeworkProcessResultShardDao;
    @Inject
    private SubHomeworkResultDao subHomeworkResultDao;
    @Inject
    private SelfStudyHomeworkGenerateServiceImpl selfStudyHomeworkGenerateService;
    @Inject
    private ShardHomeworkDao shardHomeworkDao;
    @Inject
    private OcrClassifyImagesPersistence ocrClassifyImagesPersistence;
    @Inject
    private BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject private HomeworkBlackWhiteListDao homeworkBlackWhiteListDao;


    @Override
    public MapMessage changeHomeworkEndTime(Date searchStartDate, Date searchEndDate, Date endTime) {
        List<NewHomework.Location> locationList = newHomeworkCrmLoader.findHomeworkByEndTime(searchStartDate, searchEndDate);
        List<List<NewHomework.Location>> lists = NewHomeworkUtils.splitList(locationList, 1000);
        int successCount = 0;
        int failedCount = 0;
        for (List<NewHomework.Location> tempList : lists) {
            for (NewHomework.Location location : tempList) {
                String id = location.getId();
                boolean b = NewHomeworkUtils.isShardHomework(id) ? shardHomeworkDao.changeHomeworkEndTime(id, endTime) : subHomeworkDao.changeHomeworkEndTime(id, endTime);
                if (b) {
                    successCount = successCount + 1;
                } else {
                    failedCount = failedCount + 1;
                }
            }
            ThreadUtils.sleepCurrentThread(2000);
        }
        return MapMessage.successMessage().add("allCount", locationList.size()).add("failedCount", failedCount).add("successCount", successCount);
    }

    @Override
    public MapMessage changeHomeworkEndTime(String homeworkId, Date endTime) {

        boolean b;
        if (NewHomeworkUtils.isSubHomework(homeworkId)) {
            b = subHomeworkDao.changeHomeworkEndTime(homeworkId, endTime);
        } else if (NewHomeworkUtils.isShardHomework(homeworkId)) {
            b = shardHomeworkDao.changeHomeworkEndTime(homeworkId, endTime);
        } else {
            b = false;
        }
        if (b) {
            return MapMessage.successMessage().add("allCount", 1).add("failedCount", 0).add("successCount", 1);
        } else {
            return MapMessage.successMessage().add("allCount", 1).add("failedCount", 1).add("successCount", 0);
        }
    }

    @Override
    public MapMessage addHomeworkRewardInParentApp(Long userId, String homeworkId, Long groupId, Integer integralCount, Date expire) {
        return newHomeworkFinishRewardInParentAppDao.addRewardInteger(userId, homeworkId, groupId, integralCount, expire);
    }

    @Override
    public MapMessage repairSelfStudyCorrectHomework(String homeworkId, Long studentId) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null
                || !NewHomeworkConstants.GenerateHomeworkTypes.contains(homework.getType())
                || !NewHomeworkConstants.GenerateHomeworkTags.contains(homework.getHomeworkTag())
                || !NewHomeworkConstants.NeedSelfStudyHomeworkSubjects.contains(homework.getSubject())) {
            return MapMessage.errorMessage("作业不存、作业学科错误、作业类型错误");
        }
        if (!NewHomeworkConstants.showWrongQuestionInfo(homework.getCreateAt(), RuntimeMode.getCurrentStage())) {
            return MapMessage.errorMessage("本次作业创建时间在错题订正上线之前，不允许生成");
        }

        HomeworkSelfStudyRef.ID ID = new HomeworkSelfStudyRef.ID(homeworkId, studentId);
        HomeworkSelfStudyRef ref = homeworkSelfStudyRefDao.load(ID.toString());
        if (ref != null) {
            return MapMessage.errorMessage("自学订正作业已存在");
        }

        NewHomeworkResult homeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(homework.toLocation(), studentId, true);
        if (homeworkResult == null || !homeworkResult.isFinished() || homeworkResult.getPractices() == null) {
            return MapMessage.errorMessage("作业未开始、作业未完成");
        }
        //去生成订正任务
        selfStudyHomeworkGenerateService.generateSelfStudyHomework(homework, homeworkResult);
        return MapMessage.successMessage("自学订正任务应该布置成功了~");
    }

    @Override
    public MapMessage resumeNewHomework(String homeworkId) {
        if (!NewHomeworkUtils.isSubHomework(homeworkId) && !NewHomeworkUtils.isShardHomework(homeworkId)) {
            return MapMessage.errorMessage("不是新作业ID");
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!SafeConverter.toBoolean(newHomework.getDisabled())) {
            return MapMessage.errorMessage("作业未被删除");
        }
        if (NewHomeworkUtils.isShardHomework(homeworkId)) {
            shardHomeworkDao.crmUpdateDisabledTrue(homeworkId);
        } else {
            subHomeworkDao.crmUpdateDisabledTrue(homeworkId);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage crmResendDubbingSynthetic(Collection<String> ids) {
        Map<String, DubbingSyntheticHistory> dshMap = dubbingSyntheticHistoryDao.loads(ids);
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("homework");
        Objects.requireNonNull(config);
        String endpoint = config.getEndpoint();
        String bucket = config.getBucket();
        List<String> successIds = new ArrayList<>();
        for (DubbingSyntheticHistory syh : dshMap.values()) {
            Map<String, Object> message = new LinkedHashMap<>();
            String id = syh.getId();
            message.put("id", id);
            message.put("audioUrl", syh.getAudioUrl());
            message.put("videoUrl", syh.getVideoUrl());
            message.put("bucket", bucket);
            message.put("endpoint", endpoint);
            message.put("path", syh.getPath());
            message.put("fileName", id + ".mp4");
            String json = JsonUtils.toJson(message);
            Message msg = Message.newMessage().writeBinaryBody(json.getBytes(ICharset.DEFAULT_CHARSET));
            dubbingSyntheticQueueProducer.getProducer().produce(msg);
            successIds.add(id);
        }
        return MapMessage.successMessage().add("successIds", successIds);
    }

    @Override
    public MapMessage repairHomeworkData(RepairHomeworkDataParam param) {
        //如果是趣配音
        if(ObjectiveConfigType.DUBBING_WITH_SCORE.equals(param.getType())
                || ObjectiveConfigType.BASIC_APP.equals(param.getType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(param.getType())
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(param.getType())
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(param.getType())
                || ObjectiveConfigType.LEVEL_READINGS.equals(param.getType())){
            return repairHomeworkAppData(param);
        }
        if (param.getUserAnswers() == null || param.getFormerAnswer() == null){
            return MapMessage.errorMessage("参数错误");
        }
        NewHomework newHomework = newHomeworkLoader.load(param.getHid());
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(param.getType());
        if (target == null) {
            return MapMessage.errorMessage("type is error");
        }

        List<Long> userIds = new LinkedList<>();
        //没有userId 的时候
        if (param.getUserId() == null) {
            userIds.addAll(
                    studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                            .stream()
                            .map(LongIdEntity::getId)
                            .collect(Collectors.toList()));
        } else {
            userIds.add(param.getUserId());
        }
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userIds, true);

        List<String> pids = newHomeworkResultMap.values()
                .stream()
                .filter(BaseHomeworkResult::isFinished)
                .filter(o -> o.getPractices().containsKey(param.getType()))
                .map(o -> o.getPractices().get(param.getType()))
                .filter(o -> o.processAnswers().containsKey(param.getQid()))
                .map(o -> o.processAnswers().get(param.getQid()))
                .collect(Collectors.toList());
        List<SubHomeworkProcessResult> subHomeworkProcessResults = subHomeworkProcessResultShardDao.loads(pids)
                .values()
                .stream()
                .filter(o -> o.getUserAnswers() != null)
                .filter(o -> Objects.equals(JsonUtils.toJson(o.getUserAnswers()), param.getFormerAnswer()))
                .collect(Collectors.toList());

        List<String> resultIds = subHomeworkProcessResults
                .stream()
                .filter(o -> newHomeworkResultMap.containsKey(o.getUserId()))
                .map(o -> newHomeworkResultMap.get(o.getUserId()).getId())
                .collect(Collectors.toList());

        Map<Long, SubHomeworkResult> subHomeworkResultMap = subHomeworkResultDao.loads(resultIds)
                .values()
                .stream()
                .collect(Collectors.toMap(BaseHomeworkResultLight::getUserId, Function.identity()));
        for (SubHomeworkProcessResult p : subHomeworkProcessResults) {
            if (!subHomeworkResultMap.containsKey(p.getUserId()))
                continue;
            SubHomeworkResult subHomeworkResult = subHomeworkResultMap.get(p.getUserId());
            if (p.getUserAnswers() == null)
                continue;
            String s = JsonUtils.toJson(p.getUserAnswers());
            if (!s.equals(param.getFormerAnswer()))
                continue;
            repairData(p, subHomeworkResult, param);
        }
        return MapMessage.successMessage();
    }

    @Override
    public boolean repairOcrMentalPractiseImage(String homeworkId, Long userId, String processId) {
        if (StringUtils.isEmpty(homeworkId) && userId < 1L && StringUtils.isEmpty(processId)) {
            return false;
        }
        SubHomeworkProcessResult subHomeworkProcessResults = subHomeworkProcessResultShardDao.load(processId);
        if (subHomeworkProcessResults == null
                || subHomeworkProcessResults.getObjectiveConfigType() != ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
                || subHomeworkProcessResults.getOcrMentalImageDetail() == null) {
            return false;
        }
        OcrMentalImageDetail ocrMentalImageDetail = subHomeworkProcessResults.getOcrMentalImageDetail();
        String oralImageUrl = ocrMentalImageDetail.getImg_url();
        //替换为默认的图片
        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == subHomeworkProcessResults.getObjectiveConfigType()) {
            subHomeworkProcessResults.getOcrMentalImageDetail().setImg_url(NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_DEFAULT_IMG);
        } else if (ObjectiveConfigType.OCR_DICTATION == subHomeworkProcessResults.getObjectiveConfigType()) {
            subHomeworkProcessResults.getOcrMentalImageDetail().setImg_url(NewHomeworkConstants.OCR_DICTATION_DEFAULT_IMG);
        }
        subHomeworkProcessResultShardDao.upsert(subHomeworkProcessResults);
        //记录替换
        OcrClassifyImages ocrClassifyImages = new OcrClassifyImages();
        ocrClassifyImages.setHomeworkId(homeworkId);
        ocrClassifyImages.setUserId(userId);
        ocrClassifyImages.setProcessId(processId);
        ocrClassifyImages.setOriginalImageUrl(oralImageUrl);
        ocrClassifyImagesPersistence.insert(ocrClassifyImages);

        LogCollector.info("backend-general", MapUtils.map(
                "hid", homeworkId,
                "usertoken", userId,
                "imageId", ocrMentalImageDetail.getImg_id(),
                "imgUrl", oralImageUrl,
                "imgWidth", ocrMentalImageDetail.getImg_width(),
                "imgHeight", ocrMentalImageDetail.getImg_height(),
                "form", ocrMentalImageDetail.getForms(),
                "ip", "1",   //代表后端
                "op", "aiImageLogs",
                "env", "production"
        ));

        return true;
    }

    @Override
    public boolean repairOcrDictationPracticeImage(String homeworkId, Long userId, String processId) {
        if (StringUtils.isEmpty(homeworkId) && userId < 1L && StringUtils.isEmpty(processId)) {
            return false;
        }
        SubHomeworkProcessResult subHomeworkProcessResult = subHomeworkProcessResultShardDao.load(processId);
        if (subHomeworkProcessResult == null
                || subHomeworkProcessResult.getObjectiveConfigType() != ObjectiveConfigType.OCR_DICTATION
                || subHomeworkProcessResult.getOcrDictationImageDetail() == null) {
            return false;
        }
        OcrMentalImageDetail ocrMentalImageDetail = subHomeworkProcessResult.getOcrDictationImageDetail();
        String imageUrl = ocrMentalImageDetail.getImg_url();
        //替换为默认的图片
        subHomeworkProcessResult.getOcrDictationImageDetail().setImg_url(NewHomeworkConstants.OCR_DICTATION_DEFAULT_IMG);
        subHomeworkProcessResultShardDao.upsert(subHomeworkProcessResult);
        //记录替换
        OcrClassifyImages ocrClassifyImages = new OcrClassifyImages();
        ocrClassifyImages.setHomeworkId(homeworkId);
        ocrClassifyImages.setUserId(userId);
        ocrClassifyImages.setProcessId(processId);
        ocrClassifyImages.setOriginalImageUrl(imageUrl);
        ocrClassifyImagesPersistence.insert(ocrClassifyImages);

        LogCollector.info("backend-general", MapUtils.map(
                "hid", homeworkId,
                "usertoken", userId,
                "imageId", ocrMentalImageDetail.getImg_id(),
                "imgUrl", imageUrl,
                "imgWidth", ocrMentalImageDetail.getImg_width(),
                "imgHeight", ocrMentalImageDetail.getImg_height(),
                "form", ocrMentalImageDetail.getForms(),
                "ip", "1",   //代表后端
                "op", "aiImageLogs",
                "env", "production"
        ));
        return true;
    }

    private void repairData(SubHomeworkProcessResult p, SubHomeworkResult subHomeworkResult, RepairHomeworkDataParam param) {
        double beforeScore = SafeConverter.toDouble(p.getScore());
        p.setScore(param.getScore());
        p.setSubScore(param.getSubScore());
        p.setGrasp(param.getGrasp());
        p.setSubGrasp(param.getSubGrasp());
        p.setUserAnswers(param.getUserAnswers());
        subHomeworkProcessResultShardDao.upsert(p);
        BaseHomeworkResultAnswer baseHomeworkResultAnswer = subHomeworkResult.getPractices().get(param.getType());
        if (baseHomeworkResultAnswer != null) {
            baseHomeworkResultAnswer.setScore(SafeConverter.toDouble(baseHomeworkResultAnswer.getScore()) + SafeConverter.toDouble(param.getScore()) - beforeScore);
            subHomeworkResultDao.upsert(subHomeworkResult);
        }
    }


    private MapMessage repairHomeworkAppData(RepairHomeworkDataParam param) {
        NewHomework newHomework = newHomeworkLoader.load(param.getHid());
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(param.getType());
        if (target == null) {
            return MapMessage.errorMessage("type is error");
        }
        Long userId = param.getUserId();
        //没有userId 的时候
        if (userId == null) {
            return MapMessage.errorMessage("user is error");
        }
        String keyStr = param.getKeyStr();
        if (StringUtils.isBlank(keyStr)) {
            return MapMessage.errorMessage("keyStr is error");
        }
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), userId, true);
        List<String> pids = newHomeworkResult.findHomeworkProcessIdsForVideoQuestionByVideoId(keyStr, param.getType());
        if(CollectionUtils.isEmpty(pids)){
            return MapMessage.errorMessage("keyStr & type 没有对应的题结果");
        }
        Map<String, SubHomeworkProcessResult>  subHomeworkProcessResultMap = subHomeworkProcessResultShardDao.loads(pids);

        SubHomeworkResult subHomeworkResult = subHomeworkResultDao.load(newHomeworkResult.getId());
        Double score;
        Double totalScore = 0D;
        Boolean containQid = false;
        Integer standardNum = 0;
        for (SubHomeworkProcessResult p : subHomeworkProcessResultMap.values()) {
            if (p.getQuestionId().equals(param.getQid())){
                if (param.getScore() > p.getStandardScore()) {
                    p.setScore(p.getStandardScore());
                } else {
                    p.setScore(param.getScore());
                }
                p.setSubScore(param.getSubScore());
                p.setGrasp(param.getGrasp());
                p.setSubGrasp(param.getSubGrasp());
                if(StringUtils.isNotBlank(param.getOralScoreLevel())){
                    p.setAppOralScoreLevel(AppOralScoreLevel.valueOf(param.getOralScoreLevel()));
                }
                containQid = true;
                subHomeworkProcessResultShardDao.upsert(p);
            }
            totalScore += p.getScore() != null ? p.getScore() : 0;
            if (p.getGrasp() != null && Boolean.TRUE.equals(p.getGrasp())) {
                ++standardNum;
            }
        }
        if(containQid){
            if (ObjectiveConfigType.LEVEL_READINGS.equals(param.getType())) {
                score = 60 + totalScore;
                if (score > 100D) {
                    score = 100D;
                }
            } else {
                score = subHomeworkProcessResultMap.size() == 0 ? 0 : new BigDecimal(totalScore)
                        .divide(new BigDecimal(subHomeworkProcessResultMap.size()), 0, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
            }
            BaseHomeworkResultAnswer baseHomeworkResultAnswer = subHomeworkResult.getPractices().get(param.getType());
            if (baseHomeworkResultAnswer != null && baseHomeworkResultAnswer.getAppAnswers() != null) {
                BaseHomeworkResultAppAnswer braa = baseHomeworkResultAnswer.getAppAnswers().get(keyStr);
                if(ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(param.getType())
                        || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(param.getType())){
                    Integer appQuestionNum = 0;
                    List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkReadReciteQuestions(param.getType(), keyStr);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        appQuestionNum = newHomeworkQuestions.size();
                    }
                    score = new BigDecimal(standardNum).divide(new BigDecimal(appQuestionNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
                    braa.setStandardNum(standardNum);
                }
                braa.setScore(score);

                baseHomeworkResultAnswer.getAppAnswers().replace(keyStr, braa);
                double objTotalScore = 0d;
                for (BaseHomeworkResultAppAnswer nhra : baseHomeworkResultAnswer.getAppAnswers().values()) {
                    objTotalScore += nhra.getScore();
                }
                double avgScore = new BigDecimal(objTotalScore).divide(new BigDecimal(baseHomeworkResultAnswer.getAppAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                baseHomeworkResultAnswer.setScore(avgScore);
                subHomeworkResultDao.upsert(subHomeworkResult);
            }
            return MapMessage.successMessage();
        }else {
            return MapMessage.errorMessage("提交结果不包含此题");
        }
    }

    @Override
    public MapMessage resumeBasicReviewHomework(String packageId) {
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null) {
            return MapMessage.errorMessage("基础必过练习包不存在");
        }
        if (!basicReviewHomeworkPackage.isDisabledTrue()) {
            return MapMessage.errorMessage("不需要恢复");
        }
        basicReviewHomeworkPackage.setDisabled(false);
        basicReviewHomeworkPackageDao.upsert(basicReviewHomeworkPackage);
        List<String> homeworkIds = basicReviewHomeworkPackage.getStages().stream().map(BasicReviewStage::getHomeworkId).collect(Collectors.toList());
        Map<String, ShardHomework> shardHomeworkMap = shardHomeworkDao.loads(homeworkIds);
        for (ShardHomework shardHomework : shardHomeworkMap.values()) {
            shardHomework.setDisabled(false);
            shardHomeworkDao.upsert(shardHomework);
        }
        return MapMessage.successMessage("恢复成功");
    }

    @Override
    public boolean addNewHomeworkBlackWhiteList(String businessType, String idType, String blackWhiteId) {
        try {
            String id = HomeworkBlackWhiteList.generateId(businessType, idType, blackWhiteId);
            HomeworkBlackWhiteList blackWhiteList = homeworkBlackWhiteListDao.load(id);
            if (blackWhiteList != null) {
                blackWhiteList.setDisabled(false);
                homeworkBlackWhiteListDao.upsert(blackWhiteList);
            } else {
                HomeworkBlackWhiteList homeworkBlackWhiteList = new HomeworkBlackWhiteList();
                homeworkBlackWhiteList.setId(id);
                homeworkBlackWhiteList.setBusinessType(businessType);
                homeworkBlackWhiteList.setIdType(idType);
                homeworkBlackWhiteList.setBlackWhiteId(blackWhiteId);
                Date currentDate = new Date();
                homeworkBlackWhiteList.setCreateAt(currentDate);
                homeworkBlackWhiteList.setUpdateAt(currentDate);
                homeworkBlackWhiteList.setDisabled(false);
                homeworkBlackWhiteListDao.insert(homeworkBlackWhiteList);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteNewHomeworkBlackWhiteList(String id) {
        try {
            return homeworkBlackWhiteListDao.updateDisabledTrue(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
