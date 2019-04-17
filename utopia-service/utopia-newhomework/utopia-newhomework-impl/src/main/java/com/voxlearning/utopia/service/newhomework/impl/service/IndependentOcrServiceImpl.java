package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.core.helper.ClassifyImageUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.helper.classify.images.ClassifyImagesReponseBody;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.classifyimages.OcrClassifyImages;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.IndependentOcrProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.OcrStudentWorkbook;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.SaveOcrRecognitionRequest;
import com.voxlearning.utopia.service.newhomework.api.service.IndependentOcrService;
import com.voxlearning.utopia.service.newhomework.api.util.WeightRandom;
import com.voxlearning.utopia.service.newhomework.consumer.cache.IndependentOcrRewardCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages.OcrClassifyImagesPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.ocr.IndependentOcrProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.ocr.OcrStudentWorkbookDao;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.IndependentOcrPublisher;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionAnswer;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.*;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
@Named
@Service(interfaceClass = IndependentOcrService.class)
@ExposeService(interfaceClass = IndependentOcrService.class)
public class IndependentOcrServiceImpl implements IndependentOcrService {

    private static final Logger logger = LoggerFactory.getLogger(IndependentOcrServiceImpl.class);

    @Inject private IndependentOcrProcessResultDao independentOcrProcessResultDao;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private OcrClassifyImagesPersistence ocrClassifyImagesPersistence;
    @Inject private IntelDiagnosisClient intelDiagnosisClient;
    @Inject private OcrStudentWorkbookDao ocrStudentWorkbookDao;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private IndependentOcrPublisher independentOcrPublisher;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;

    @Override
    public Map<String, Object> batchProcessOcrResult(Long userId, SaveOcrRecognitionRequest request) {
        List<IndependentOcrProcessResult> independentOcrProcessResults = Lists.newLinkedList();
        Date currentDate = new Date();
        for (OcrMentalImageDetail ocrMentalImageDetail : request.getOcrMentalImageDetails()) {
            IndependentOcrProcessResult processResult = new IndependentOcrProcessResult();
            IndependentOcrProcessResult.ID id = new IndependentOcrProcessResult.ID(currentDate, request.getStudentId());
            processResult.setId(id.toString());
            processResult.setUserId(userId);
            processResult.setStudentId(request.getStudentId());
            processResult.setCreateAt(currentDate);
            processResult.setUpdateAt(currentDate);
            processResult.setDisabled(Boolean.FALSE);
            processResult.setClientType(request.getClientType());
            processResult.setClientName(request.getClientName());
            processResult.setOcrMentalImageDetail(ocrMentalImageDetail);
            independentOcrProcessResults.add(processResult);
        }
        independentOcrProcessResultDao.inserts(independentOcrProcessResults);

        // 用户上传图片处理
        AlpsThreadPool
                .getInstance()
                .submit(() -> this.classifyImage(String.valueOf(currentDate.getTime()), userId, independentOcrProcessResults));
        // 去发奖励
        if (("17Student".equals(request.getClientName()) && VersionUtil.compareVersion(request.getVersion(), "3.1.9") > 0) || "17Parent".equals(request.getClientName())) {
            return reward(request.getStudentId(), request.getOcrMentalImageDetails());
        }
        return Collections.emptyMap();
    }

    @NotNull
    private Map<String, Object> reward(Long studentId, List<OcrMentalImageDetail> ocrMentalImageDetails) {
        Map<String, Object> resultMap = new HashMap<>();
        long notRightNum = ocrMentalImageDetails.stream().flatMap(ocr -> ocr.getForms().stream())
                .flatMap(form -> form.getAnswers().stream())
                .filter(form -> form.getJudge() != 2)
                .filter(form -> form.getJudge() != 1).count();

        String rewardType = "HAVE_WRONG"; //有错题
        Integer rewardBean = 0;
        // 忽略无法批改, 并且不为1的个数为0, 则去激励
        if (notRightNum == 0) {
            IndependentOcrRewardCacheManager ocrRewardCacheManager = newHomeworkCacheService.getIndependentOcrRewardCacheManager();
            // 本周已有学豆奖励->点赞, 本周未领取学豆奖励, 走发奖励逻辑
            if (ocrRewardCacheManager.exist(studentId)) {
                rewardType = "REWARDED"; //已奖励
            } else {
                rewardType = "NOT_REWARD"; //未奖励
                // AB test, 灰度地区发学豆, 非灰度地区只点赞
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                boolean isWhiteList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                        "IndependentOcrReward", "region");
                if (isWhiteList) {
                    rewardBean = weightRandom0To3();
                    if (rewardBean > 0) {
                        ocrRewardCacheManager.add(ocrRewardCacheManager.getCacheKey(studentId), 1);
                        // 触发家长奖励
                        String parentRewardItemKey = rewardBean == 1 ? "MATH_photocorrecting_100" : rewardBean == 2 ? "MATH_photocorrecting_1001" : "MATH_photocorrecting_1000";
                        parentRewardService.generateParentReward(studentId, parentRewardItemKey, null);
                    }
                }
            }
        }
        resultMap.put("rewardBean", rewardBean);
        resultMap.put("rewardType", rewardType);
        return resultMap;
    }

    /**
     * 获取0-3权重随机数
     */
    private Integer weightRandom0To3() {
        ArrayList<Pair<Integer, Integer>> atomList = new ArrayList<>();
        atomList.add(new Pair<>(3, 10));
        atomList.add(new Pair<>(2, 20));
        atomList.add(new Pair<>(1, 30));
        atomList.add(new Pair<>(0, 40));
        WeightRandom<Integer, Integer> weightRandom = new WeightRandom<>(atomList);
        return weightRandom.random();
    }

    @Override
    public boolean batchProcessOcrResult(Collection<IndependentOcrProcessResult> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return false;
        }
        try {
            independentOcrProcessResultDao.inserts(documents);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void classifyImage(String createAtMillis, Long userId, Collection<IndependentOcrProcessResult> independentOcrProcessResults) {
        if (CollectionUtils.isEmpty(independentOcrProcessResults)) {
            return;
        }

        //没有识别图片不会提交, 只对有识别结果图片进行处理
        //调用金山云服务如果返回图片有问题我们就替换图片。并把学生原始图片计入到被替换历史表供以后可查。
        //图片被识别出来公式内容等，需要对每一张进行鉴黄处理
        List<String> imageUrls = independentOcrProcessResults.stream()
                .filter(p -> p.getOcrMentalImageDetail() != null
                        && StringUtils.isNotEmpty(p.getOcrMentalImageDetail().getImg_url())
                        && SafeConverter.toInt(p.getOcrMentalImageDetail().getNumber()) > 0)
                .map(p -> p.getOcrMentalImageDetail().getImg_url()).collect(Collectors.toList());

        ClassifyImagesReponseBody checkResult = classifyImages(imageUrls);
        if (checkResult == null) {
            return;
        }
        Map<String, String> imageKeyProcessIdMap = independentOcrProcessResults.stream()
                .filter(p -> p.getOcrMentalImageDetail() != null && StringUtils.isNotEmpty(p.getOcrMentalImageDetail().getImg_url()))
                .collect(Collectors.toMap(k -> k.getOcrMentalImageDetail().getImg_url(), IndependentOcrProcessResult::getId));

        for (ClassifyImagesReponseBody.ClassifyImagesItemBody body : checkResult.getBody()) {
            if (body.getErrorNo() == 400) {
                logger.error("IndependentOcrServiceImpl image problem : errorNo: {} ,errorMsg : {} ", body.getErrorNo(), body.getErrorMsg());
                continue;
            }
            List<ClassifyImagesReponseBody.ClassifyImagesItemBody.ImageResult> imageResults = body.getResults();
            if (CollectionUtils.isEmpty(imageResults)) {
                continue;
            }
            ClassifyImagesReponseBody.ClassifyImagesItemBody.ImageResult image = imageResults.get(0);
            if (SafeConverter.toInt(image.getLabel()) == 1) {  //  1正常，2低俗，3色情
                continue;
            }
            //其他情况为低俗后者是涉黄，需要处理
            this.repairOcrPractiseImage(createAtMillis, userId, imageKeyProcessIdMap.get(body.getImageUrl()));
        }
    }

    @Nullable
    private ClassifyImagesReponseBody classifyImages(List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return null;
        }

        ClassifyImagesReponseBody checkResult;
        try {
            checkResult = ClassifyImageUtils.checkImage(imageUrls);
        } catch (Exception e) {
            logger.error("IndependentOcrServiceImpl classifyImages error. imageUrls : {} , exception : {}", imageUrls, e);
            return null;
        }
        if (checkResult == null) {
            logger.error("IndependentOcrServiceImpl invoke jinshan method fail ,can't get return msg . imageUrls : {} ", imageUrls);
            return null;
        }
        if (checkResult.getHeader() == null) {
            logger.error("IndependentOcrServiceImpl invoke jinshan method fail ,can't get header msg .imageUrls : {} ", imageUrls);
            return null;
        }
        if (checkResult.getHeader() != null && checkResult.getHeader().getErrorNo() != 200) {
            logger.error("IndependentOcrServiceImpl invoke jinshan method fail : errorNo: {} ,errorMsg : {} ", checkResult.getHeader().getErrorNo(), checkResult.getHeader().getErrorMsg());
            return null;
        }
        return checkResult;
    }


    private boolean repairOcrPractiseImage(String createAtMillis, Long userId, String processId) {
        if (StringUtils.isEmpty(createAtMillis) && userId < 1L && StringUtils.isEmpty(processId)) {
            return false;
        }
        IndependentOcrProcessResult ocrProcessResult = independentOcrProcessResultDao.load(processId);
        if (ocrProcessResult == null || ocrProcessResult.getOcrMentalImageDetail() == null) {
            return false;
        }
        OcrMentalImageDetail ocrMentalImageDetail = ocrProcessResult.getOcrMentalImageDetail();
        String oralImageUrl = ocrMentalImageDetail.getImg_url();
        //替换为默认的图片
        ocrProcessResult.getOcrMentalImageDetail().setImg_url(NewHomeworkConstants.INDEPENDENT_OCR_DEFAULT_IMG);
        ocrProcessResult.setDisabled(Boolean.TRUE);
        independentOcrProcessResultDao.upsert(ocrProcessResult);
        //记录替换
        OcrClassifyImages ocrClassifyImages = new OcrClassifyImages();
        ocrClassifyImages.setHomeworkId(createAtMillis);
        ocrClassifyImages.setUserId(userId);
        ocrClassifyImages.setProcessId(processId);
        ocrClassifyImages.setOriginalImageUrl(oralImageUrl);
        ocrClassifyImagesPersistence.insert(ocrClassifyImages);

        LogCollector.info("backend-general", MapUtils.map(
                "create_millis", createAtMillis,
                "usertoken", userId,
                "imageId", ocrMentalImageDetail.getImg_id(),
                "imgUrl", oralImageUrl,
                "imgWidth", ocrMentalImageDetail.getImg_width(),
                "imgHeight", ocrMentalImageDetail.getImg_height(),
                "form", ocrMentalImageDetail.getForms(),
                "ip", "1",   //代表后端
                "op", "aiImageLogs",
                "env", RuntimeMode.current().getStageMode()
        ));

        return true;
    }

    @Override
    public MapMessage reportError(Long studentId, String imgUrl, String coordinate, ExLinkedHashMap<String, String> kibanaMap) {
        IndependentOcrProcessResult ocrProcessResult = independentOcrProcessResultDao.loadByImageUrl(imgUrl, studentId);
        if (ocrProcessResult == null) {
            return MapMessage.errorMessage("检查记录不存在或已删除");
        }
        List<OcrMentalImageDetail.Coordinate> coordinates = JsonUtils.fromJsonToList(coordinate, OcrMentalImageDetail.Coordinate.class);
        Date currentDate = new Date();

        OcrMentalImageDetail ocrMentalImageDetail = ocrProcessResult.getOcrMentalImageDetail();
        if (imgUrl.equals(ocrMentalImageDetail.getImg_url())) {
            List<OcrMentalImageDetail.Form> forms = ocrMentalImageDetail.getForms();
            for (OcrMentalImageDetail.Form form : forms) {
                List<OcrMentalImageDetail.Coordinate> sourceCoordinates = form.getCoordinate();
                if (CollectionUtils.isNotEmpty(sourceCoordinates) && sourceCoordinates.containsAll(coordinates)) {
                    ocrProcessResult.setUpdateAt(currentDate);
                    form.setCorrectAt(currentDate);
                    independentOcrProcessResultDao.upsert(ocrProcessResult);

                    // kibana
                    ExLinkedHashMap<String, String> map = MapUtils.map(
                            "processCreateTime", ocrProcessResult.getCreateAt(),
                            "processId", ocrProcessResult.getId(),
                            "studentId", studentId,
                            "imageId", ocrMentalImageDetail.getImg_id(),
                            "imgUrl", imgUrl,
                            "imgWidth", ocrMentalImageDetail.getImg_width(),
                            "imgHeight", ocrMentalImageDetail.getImg_height(),
                            "form", form);
                    map.putAll(kibanaMap);
                    LogCollector.info("backend-general", map);
                    return MapMessage.successMessage("操作成功");
                }
            }
        }
        return MapMessage.errorMessage("操作失败");
    }

    @Override
    public MapMessage fetchOcrResultList(Long studentId, Integer pageNum, Integer pageSize) {
        List<IndependentOcrProcessResult> ocrProcessResults = independentOcrProcessResultDao.loadByStudentId(studentId);
        List<IndependentOcrProcessResult> sortedProcessResult = ocrProcessResults
                .stream()
                .sorted((pr1, pr2) -> pr2.getCreateAt().compareTo(pr1.getCreateAt()))
                .collect(Collectors.toList());

        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        Page<IndependentOcrProcessResult> processResultPage = PageableUtils.listToPage(sortedProcessResult, pageable);

        MapMessage message = new MapMessage();
        return message.add("result", "success")
                .add("processList", processResultPage.getContent())
                .add("totalPages", processResultPage.getTotalPages())
                .add("elementSize", processResultPage.getTotalElements());
    }

    @Override
    public MapMessage deleteResult(List<String> processIdList, Long studentId) {
        List<String> deleteImgUrls = independentOcrProcessResultDao.deleteProcessResults(processIdList, studentId);

        // 发kafka到家长通同步删除数据
        if (CollectionUtils.isNotEmpty(deleteImgUrls)) {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", studentId);
            map.put("imgUrls", deleteImgUrls);
            independentOcrPublisher.getIndependentOcrPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
        return MapMessage.successMessage("操作成功");
    }

    @Override
    public MapMessage fetchAnswerAndAnalysis(List<String> questionIdList) {
        //是否展示答案解析
        String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "SHOW_OCR_ANSWER_AND_ANALYSIS");
        boolean showAnswerAndAnalysis = ConversionUtils.toBool(config);

        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        if (showAnswerAndAnalysis) {
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(questionIdList);
            for (NewQuestion newQuestion : newQuestionMap.values()) {
                List<List<String>> answers = Lists.newLinkedList();
                List<String> analysis = Lists.newLinkedList();
                for (NewQuestionsSubContents subContent : newQuestion.getContent().getSubContents()) {
                    List<String> subStandardAnswers = subContent.getAnswers().stream().map(NewQuestionAnswer::getAnswer).collect(Collectors.toList());
                    answers.add(subStandardAnswers);
                    analysis.add(subContent.getAnalysis());
                }
                resultMap.put(newQuestion.getDocId(), MapUtils.m("answers", answers, "analysis", analysis));
            }
        }
        MapMessage message = new MapMessage();
        return message.add("result", "success").add("questionMap", resultMap).add("showAnswerAndAnalysis", showAnswerAndAnalysis);
    }

    @Override
    public MapMessage mentalSymptomAnalysis(String imgUrl, List<String> textList, Long studentId) {
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omadMap = processOcrMentalArithmeticDiagnosis(imgUrl, textList, studentId);
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalysisMap = processOcrMentalArithmeticSymptomAnalysis(imgUrl, textList, studentId);
        Map<String, OcrMentalImageDetail.Point> textPointMap = new HashMap<>();
        Set<String> courseIds = new LinkedHashSet<>();
        if (MapUtils.isNotEmpty(omadMap)) {
            OcrMentalImageDetail.OcrMentalArithmeticDiagnosis diagnosis = omadMap.get(imgUrl);
            if (diagnosis != null) {
                if (CollectionUtils.isNotEmpty(diagnosis.getItemPoints())) {
                    for (OcrMentalImageDetail.ItemPoint itemPoint : diagnosis.getItemPoints()) {
                        if (CollectionUtils.isNotEmpty(itemPoint.getPoints())) {
                            OcrMentalImageDetail.Point point = itemPoint.getPoints().get(0);
                            textPointMap.put(itemPoint.getItemContent(), point);
                            if (StringUtils.isNotEmpty(point.getCourseId())) {
                                courseIds.add(point.getCourseId());
                            }
                        }
                    }
                }
            }
        }
        Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
        Map<String, Map<String, Object>> symptomAnalysises = new LinkedHashMap<>();
        for (String text : textList) {
            Map<String, Object> symptomAnalysis = new LinkedHashMap<>();
            if (textPointMap.get(text) != null) {
                OcrMentalImageDetail.Point point = textPointMap.get(text);
                symptomAnalysis.put("pointId", point.getPointId());
                symptomAnalysis.put("pointName", point.getPointName());
                symptomAnalysis.put("errorCause", point.getErrorCause());
                symptomAnalysis.put("courseId", point.getCourseId());
                symptomAnalysis.put("courseName", point.getCourseName());
                if (StringUtils.isNotEmpty(point.getCourseId())) {
                    IntelDiagnosisCourse intelDiagnosisCourse = intelDiagnosisCourseMap.get(point.getCourseId());
                    if (intelDiagnosisCourse != null) {
                        symptomAnalysis.put("courseBackgroundImage", intelDiagnosisCourse.getBackgroundImage());
                    }
                }
            }
            if (symptomAnalysisMap.get(text) != null) {
                OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis analysis = symptomAnalysisMap.get(text);
                symptomAnalysis.put("symptom", analysis.getSymptom());
                symptomAnalysis.put("analysis", analysis.getAnalysis());
            }
            symptomAnalysises.put(text, symptomAnalysis);
        }
        return MapMessage.successMessage()
                .add("symptomAnalysisMap", symptomAnalysises);
    }

    private Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> processOcrMentalArithmeticDiagnosis(String imgUrl, List<String> textList, Long studentId) {
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omadMap = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> itemSubs = new ArrayList<>();
        for (String text : textList) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", text);
            item.put("correct", false);
            itemSubs.add(item);
        }
        items.add(MapUtils.m("imgUrl", imgUrl, "contents", itemSubs));
        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_TEST : OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL;
        if (RuntimeMode.current().equals(Mode.STAGING)) {
            requestUrl = OCR_MENTAL_ARITHMETIC_DIAGNOSIS_URL_STAGING;
        }
        Map<String, Object> httpParams = MapUtils.m("items", items, "userId", studentId);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(requestUrl)
                    .json(httpParams)
                    .contentType("application/json").socketTimeout(1000)
                    .execute();
            if (response == null || response.getStatusCode() != 200) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", studentId,
                        "mod2", requestUrl,
                        "mod3", httpParams,
                        "op", "hr homework ocr_mental_arithmetic"
                ));
                logger.error("调用:{}失败, httpParams:{}, response: {}",
                        requestUrl,
                        httpParams,
                        response != null ? response.getResponseString() : "");
            }
            if (response != null) {
                Map resp = JsonUtils.fromJson(response.getResponseString(), Map.class);
                if (resp.get("resultCode").equals(200)) {
                    Map resultInfo = JsonUtils.fromJson(JsonUtils.toJson(resp.get("resultInfo")));
                    if (resultInfo != null) {
                        String itemsJson = JsonUtils.toJson(resultInfo.get("items"));
                        List<OcrMentalImageDetail.OcrMentalArithmeticDiagnosis> omads = JsonUtils.fromJsonToList(itemsJson, OcrMentalImageDetail.OcrMentalArithmeticDiagnosis.class);
                        if (CollectionUtils.isNotEmpty(omads)) {
                            for (OcrMentalImageDetail.OcrMentalArithmeticDiagnosis omad : omads) {
                                omadMap.put(omad.getImgUrl(), omad);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取纸质口算诊断数据失败requestUrl:{}, Exception: {}", requestUrl, e);
        }
        return omadMap;
    }

    private Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> processOcrMentalArithmeticSymptomAnalysis(String imgUrl, List<String> textList, Long studentId) {
        Map<String, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalysisMap = new LinkedHashMap<>();
        Set<String> errorTextSet = new LinkedHashSet<>(textList);
        String requestUrl = RuntimeMode.current().le(Mode.TEST) ? OCR_FORMULA_SYMPTOM_TEST_URL : OCR_FORMULA_SYMPTOM_PRODUCT_RUL;
        if (RuntimeMode.current().equals(Mode.STAGING)) {
            requestUrl = OCR_FORMULA_SYMPTOM_STAGING_RUL;
        }
        if (CollectionUtils.isEmpty(errorTextSet)) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = MapUtils.m("uid", studentId, "equations", errorTextSet);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(requestUrl)
                    .json(params)
                    .contentType("application/json")
                    .socketTimeout(1000)
                    .execute();
            if (response != null && response.getStatusCode() == 200) {
                Map resp = JsonUtils.fromJson(response.getResponseString(), Map.class);
                if (MapUtils.isEmpty(resp)) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", studentId,
                            "mod2", requestUrl,
                            "mod3", params,
                            "response", response.getResponseString(),
                            "op", "OcrMentalArithmeticSymptomAnalysis response is empty"
                    ));
                } else {
                    String resultJson = JsonUtils.toJson(resp.get("results"));
                    List<OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis> symptomAnalyses = JsonUtils.fromJsonToList(resultJson, OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis.class);
                    if (CollectionUtils.isNotEmpty(symptomAnalyses)) {
                        int errorTextSize = errorTextSet.size();
                        int symptomAnalysesSize = symptomAnalyses.size();
                        List<String> errorTextList = new ArrayList<>(errorTextSet);
                        if (errorTextSize == symptomAnalysesSize) {
                            for (int index = 0; index < errorTextSize; index++) {
                                String errorText = errorTextList.get(index);
                                OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis symptomAnalysis = symptomAnalyses.get(index);
                                if (symptomAnalysis != null && StringUtils.isNotEmpty(symptomAnalysis.getSymptom())) {
                                    symptomAnalysisMap.put(errorText, symptomAnalysis);
                                }
                            }
                        }
                    }
                }
            } else {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", studentId,
                        "mod2", requestUrl,
                        "mod3", params,
                        "status", response == null ? "" : response.getStatusCode(),
                        "op", "OcrMentalArithmeticSymptomAnalysis status error"
                ));
                logger.error("调用:{}失败, httpParams:{}, response: {}",
                        requestUrl,
                        params,
                        response != null ? response.getResponseString() : "");
            }
        } catch (Exception e) {
            logger.error("获取纸质口算错因分析数据失败requestUrl:{}, Exception: {}", requestUrl, e);
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod2", requestUrl,
                    "mod3", params,
                    "op", "OcrMentalArithmeticSymptomAnalysis Exception"
            ));
        }
        return symptomAnalysisMap;
    }

    @Override
    public List<Map<String, Object>> saveOcrStudentWorkbook(List<String> backCoverImgUrlList, Long studentId) {
        Date currentTime = new Date();
        List<OcrStudentWorkbook> ocrStudentWorkbooks = new ArrayList<>();
        for (String backCoverImgUrl : backCoverImgUrlList) {
            OcrStudentWorkbook workbook = new OcrStudentWorkbook();
            String id = StringUtils.join("OWB_", RandomUtils.nextObjectId());
            workbook.setId(id);
            workbook.setBackCoverImgUrl(backCoverImgUrl);
            workbook.setCreateTime(currentTime);
            workbook.setUpdateTime(currentTime);
            workbook.setStudentId(studentId);
            workbook.setDisabled(Boolean.FALSE);
            ocrStudentWorkbooks.add(workbook);
        }
        ocrStudentWorkbookDao.inserts(ocrStudentWorkbooks);

        // 用户上传图片处理
        Map<String, Integer> workBookIdCodeMap = classifyImage(studentId, ocrStudentWorkbooks);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (OcrStudentWorkbook workbook : ocrStudentWorkbooks) {
            resultList.add(MapUtils.m("myWorkbookId", workbook.getId(),
                    "backCoverImgUrl", workbook.getBackCoverImgUrl(),
                    "code", workBookIdCodeMap.get(workbook.getId()) == null ? 1 : workBookIdCodeMap.get(workbook.getId())));// 1正常，2低俗，3色情
        }
        return resultList;
    }

    private Map<String, Integer> classifyImage(Long studentId, Collection<OcrStudentWorkbook> ocrStudentWorkbooks) {
        //没有识别图片不会提交, 只对有识别结果图片进行处理
        //调用金山云服务如果返回图片有问题我们就替换图片。并把学生原始图片计入到被替换历史表供以后可查。
        //图片被识别出来公式内容等，需要对每一张进行鉴黄处理
        List<String> imageUrls = ocrStudentWorkbooks.stream().map(OcrStudentWorkbook::getBackCoverImgUrl).collect(Collectors.toList());
        ClassifyImagesReponseBody checkResult = classifyImages(imageUrls);
        if (checkResult == null) {
            return Collections.emptyMap();
        }
        Map<String, String> imageKeyWorkBookIdMap = ocrStudentWorkbooks.stream()
                .collect(Collectors.toMap(OcrStudentWorkbook::getBackCoverImgUrl, OcrStudentWorkbook::getId));

        Map<String, Integer> classifyImageMap = new HashMap<>();
        for (ClassifyImagesReponseBody.ClassifyImagesItemBody body : checkResult.getBody()) {
            if (body.getErrorNo() == 400) {
                logger.error("IndependentOcrServiceImpl image problem : errorNo: {} ,errorMsg : {} ", body.getErrorNo(), body.getErrorMsg());
                continue;
            }
            List<ClassifyImagesReponseBody.ClassifyImagesItemBody.ImageResult> imageResults = body.getResults();
            if (CollectionUtils.isEmpty(imageResults)) {
                continue;
            }
            ClassifyImagesReponseBody.ClassifyImagesItemBody.ImageResult image = imageResults.get(0);
            int label = SafeConverter.toInt(image.getLabel());
            classifyImageMap.put(imageKeyWorkBookIdMap.get(body.getImageUrl()), label);
            if (label != 1) {  //  1正常，2低俗，3色情
                this.repairOcrStudentWorkbookImage(studentId, imageKeyWorkBookIdMap.get(body.getImageUrl()));
            }
        }
        return classifyImageMap;
    }

    private boolean repairOcrStudentWorkbookImage(Long studentId, String myWorkbookId) {
        if (studentId < 1L && StringUtils.isEmpty(myWorkbookId)) {
            return false;
        }
        OcrStudentWorkbook workbook = ocrStudentWorkbookDao.load(myWorkbookId);
        if (workbook == null) {
            return false;
        }
        workbook.setDisabled(true);
        ocrStudentWorkbookDao.upsert(workbook);
        //记录替换
        OcrClassifyImages ocrClassifyImages = new OcrClassifyImages();
        ocrClassifyImages.setHomeworkId(myWorkbookId);
        ocrClassifyImages.setUserId(studentId);
        ocrClassifyImages.setProcessId(myWorkbookId);
        ocrClassifyImages.setOriginalImageUrl(workbook.getBackCoverImgUrl());
        ocrClassifyImagesPersistence.insert(ocrClassifyImages);
        return true;
    }

    @Override
    public MapMessage fetchOcrStudentWorkbook(Long studentId) {
        List<OcrStudentWorkbook> ocrStudentWorkbooks = ocrStudentWorkbookDao.loadStudentBooks(studentId);
        List<Map<String, Object>> results = new ArrayList<>();
        for (OcrStudentWorkbook ocrStudentWorkbook : ocrStudentWorkbooks) {
            results.add(MapUtils.m("myWorkbookId", ocrStudentWorkbook.getId(),
                    "backCoverImgUrl", ocrStudentWorkbook.getBackCoverImgUrl()));
        }
        return MapMessage.successMessage().add("results", results);
    }

    @Override
    public MapMessage removeOcrStudentWorkbook(Long studentId, String myWorkbookId) {
        OcrStudentWorkbook workbook = ocrStudentWorkbookDao.load(myWorkbookId);
        if (workbook == null) {
            return MapMessage.successMessage("练习册不存在");
        }
        if (!Objects.equals(workbook.getStudentId(), studentId)) {
            return MapMessage.successMessage("学生和练习册不匹配");
        }
        workbook.setDisabled(Boolean.TRUE);
        workbook.setUpdateTime(new Date());
        ocrStudentWorkbookDao.upsert(workbook);
        return MapMessage.successMessage("删除成功");
    }

    /**
     * 根据imgUrls删除检查记录
     * @param studentId 学生ID
     * @param imgUrls 图片地址
     * @return 删除数量
     */
    public int deleteByImgUrls(Long studentId, List<String> imgUrls) {
        return independentOcrProcessResultDao.deleteByImageUrls(studentId, imgUrls);
    }
}
