package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.core.helper.ClassifyImageUtils;
import com.voxlearning.utopia.core.helper.classify.images.ClassifyImagesReponseBody;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkProcessResultService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkCrmServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.HomeworkHBaseQueueServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@Named
@Service(interfaceClass = NewHomeworkProcessResultService.class)
@ExposeService(interfaceClass = NewHomeworkProcessResultService.class)
public class NewHomeworkProcessResultServiceImpl implements NewHomeworkProcessResultService {

    private static final Logger logger = LoggerFactory.getLogger(NewHomeworkProcessResultServiceImpl.class);

    @Inject
    private SubHomeworkProcessResultDao subHomeworkProcessResultDao;

    @Inject
    private HomeworkHBaseQueueServiceImpl homeworkHBaseQueueService;

    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Inject
    private NewHomeworkCrmServiceImpl newHomeworkCrmService;

    @Override
    public void inserts(String homeworkId, Collection<NewHomeworkProcessResult> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        if (NewHomeworkUtils.isSubHomework(homeworkId) || NewHomeworkUtils.isShardHomework(homeworkId)) {
            List<SubHomeworkProcessResult> subList = new ArrayList<>();
            entities.forEach(o -> {
                if (HomeworkTransform.NewHomeworkProcessResultToSub(o) != null) {
                    subList.add(HomeworkTransform.NewHomeworkProcessResultToSub(o));
                }
            });
            subHomeworkProcessResultDao.inserts(subList);
            homeworkHBaseQueueService.sendSubHomeworkProcessResult(subList);
        }
    }

    @Override
    public Boolean updateCorrection(String id,
                                    String hid,
                                    String qid,
                                    Long userId,
                                    Boolean review,
                                    CorrectType correctType,
                                    Correction correction,
                                    String teacherMark,
                                    Boolean isBatch) {
        SubHomeworkProcessResult processResult = subHomeworkProcessResultDao.updateCorrection(id, review, correctType, correction, teacherMark, isBatch);
        if (processResult != null) {
            homeworkHBaseQueueService.sendSubHomeworkProcessResult(Collections.singletonList(processResult));
        }
        return processResult != null;
    }

    @Override
    public void classifyImage(String homeworkId, Long userId, List<String> ocrMentalAnswerIds) {
        if (CollectionUtils.isEmpty(ocrMentalAnswerIds)) {
            return;
        }
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(ocrMentalAnswerIds);
        if (MapUtils.isEmpty(processResultMap)) {
            return;
        }
        //【第一层判断】
        //判断条件：number，值为0的时候，代表没有检测出任何公式。
        //处理方案：识别未能识别出公式，原图片被替换为新的图片，前端toast显示：未检测到可识别的口算类型（不会影响本次作业的分数）
        //【第二层判断】
        //调用金山云服务如果返回图片有问题我们就替换图片。并把学生原始图片计入到被替换历史表供以后可查。
        List<String> processIds = processResultMap.values().stream()
                .filter(p -> p.getOcrMentalImageDetail() != null && SafeConverter.toInt(p.getOcrMentalImageDetail().getNumber()) == 0)
                .map(SubHomeworkProcessResult::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(processIds)) {
            processIds.forEach(p -> newHomeworkCrmService.repairOcrMentalPractiseImage(homeworkId, userId, p));
        }
        //图片被识别出来公式内容等，需要对每一张进行鉴黄处理
        List<String> imageUrls = processResultMap.values().stream()
                .filter(p -> p.getOcrMentalImageDetail() != null
                        && StringUtils.isNotEmpty(p.getOcrMentalImageDetail().getImg_url())
                        && SafeConverter.toInt(p.getOcrMentalImageDetail().getNumber()) > 0)
                .map(p -> p.getOcrMentalImageDetail().getImg_url()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(imageUrls)) {
            return;
        }
        Map<String, String> imageKeyProcessIdMap = processResultMap.values().stream()
                .filter(p -> p.getOcrMentalImageDetail() != null && StringUtils.isNotEmpty(p.getOcrMentalImageDetail().getImg_url()))
                .collect(Collectors.toMap(k -> k.getOcrMentalImageDetail().getImg_url(), SubHomeworkProcessResult::getId));
        ClassifyImagesReponseBody checkResult;
        try {
            checkResult = ClassifyImageUtils.checkImage(imageUrls);
        } catch (Exception e) {
            logger.error("FH_ClassifyImage_execute processIds: {} ,imageUrls : {} ",
                    JSONObject.toJSONString(ocrMentalAnswerIds), JSONObject.toJSON(imageUrls), e);
            return;
        }
        if (checkResult == null) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail ,can't get return msg . processIds: {} ,imageUrls : {} "
                    , JSONObject.toJSONString(ocrMentalAnswerIds), JSONObject.toJSON(imageUrls));
            return;
        }
        if (checkResult.getHeader() == null) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail ,can't get header msg .processIds: {} ,imageUrls : {} "
                    , JSONObject.toJSONString(ocrMentalAnswerIds), JSONObject.toJSON(imageUrls));
            return;
        }
        if (checkResult.getHeader() != null && checkResult.getHeader().getErrorNo() != 200) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail : errorNo: {} ,errorMsg : {} ",
                    checkResult.getHeader().getErrorNo(), checkResult.getHeader().getErrorMsg());
            return;
        }

        for (ClassifyImagesReponseBody.ClassifyImagesItemBody body : checkResult.getBody()) {
            if (body.getErrorNo() == 400) {
                logger.error("FH_ClassifyImage_execute image problem : errorNo: {} ,errorMsg : {} ",
                        body.getErrorNo(), body.getErrorMsg());
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
            newHomeworkCrmService.repairOcrMentalPractiseImage(homeworkId, userId, imageKeyProcessIdMap.get(body.getImageUrl()));

        }
    }

    @Override
    public void classifyOcrDictationImage(String homeworkId, Long userId, List<String> ocrDictationAnswerIds) {
        if (CollectionUtils.isEmpty(ocrDictationAnswerIds)) {
            return;
        }
        Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(ocrDictationAnswerIds);
        if (MapUtils.isEmpty(processResultMap)) {
            return;
        }
        //【第一层判断】
        //判断条件：number，值为0的时候，代表没有检测出任何公式。
        //处理方案：识别未能识别出公式，原图片被替换为新的图片，前端toast显示：未检测到可识别的口算类型（不会影响本次作业的分数）
        //【第二层判断】
        //调用金山云服务如果返回图片有问题我们就替换图片。并把学生原始图片计入到被替换历史表供以后可查。
        List<String> processIds = processResultMap.values().stream()
                .filter(p -> p.getOcrDictationImageDetail() != null && SafeConverter.toInt(p.getOcrDictationImageDetail().getNumber()) == 0)
                .map(SubHomeworkProcessResult::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(processIds)) {
            processIds.forEach(p -> newHomeworkCrmService.repairOcrDictationPracticeImage(homeworkId, userId, p));
        }
        //图片被识别出来公式内容等，需要对每一张进行鉴黄处理
        List<String> imageUrls = processResultMap.values().stream()
                .filter(p -> p.getOcrDictationImageDetail() != null
                        && StringUtils.isNotEmpty(p.getOcrDictationImageDetail().getImg_url())
                        && SafeConverter.toInt(p.getOcrDictationImageDetail().getNumber()) > 0)
                .map(p -> p.getOcrDictationImageDetail().getImg_url()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(imageUrls)) {
            return;
        }
        Map<String, String> imageKeyProcessIdMap = processResultMap.values().stream()
                .filter(p -> p.getOcrDictationImageDetail() != null && StringUtils.isNotEmpty(p.getOcrDictationImageDetail().getImg_url()))
                .collect(Collectors.toMap(k -> k.getOcrDictationImageDetail().getImg_url(), SubHomeworkProcessResult::getId));
        ClassifyImagesReponseBody checkResult;
        try {
            checkResult = ClassifyImageUtils.checkImage(imageUrls);
        } catch (Exception e) {
            logger.error("FH_ClassifyImage_execute processIds: {} ,imageUrls : {} ",
                    JSONObject.toJSONString(ocrDictationAnswerIds), JSONObject.toJSON(imageUrls), e);
            return;
        }
        if (checkResult == null) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail ,can't get return msg . processIds: {} ,imageUrls : {} "
                    , JSONObject.toJSONString(ocrDictationAnswerIds), JSONObject.toJSON(imageUrls));
            return;
        }
        if (checkResult.getHeader() == null) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail ,can't get header msg .processIds: {} ,imageUrls : {} "
                    , JSONObject.toJSONString(ocrDictationAnswerIds), JSONObject.toJSON(imageUrls));
            return;
        }
        if (checkResult.getHeader() != null && checkResult.getHeader().getErrorNo() != 200) {
            logger.error("FH_ClassifyImage_execute invoke jinshan method fail : errorNo: {} ,errorMsg : {} ",
                    checkResult.getHeader().getErrorNo(), checkResult.getHeader().getErrorMsg());
            return;
        }

        for (ClassifyImagesReponseBody.ClassifyImagesItemBody body : checkResult.getBody()) {
            if (body.getErrorNo() == 400) {
                logger.error("FH_ClassifyImage_execute image problem : errorNo: {} ,errorMsg : {} ",
                        body.getErrorNo(), body.getErrorMsg());
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
            newHomeworkCrmService.repairOcrDictationPracticeImage(homeworkId, userId, imageKeyProcessIdMap.get(body.getImageUrl()));

        }
    }

    public void insertSubHomeworkProcessResults(Collection<SubHomeworkProcessResult> entities) {
        if (CollectionUtils.isNotEmpty(entities)) {
            subHomeworkProcessResultDao.inserts(entities);
            homeworkHBaseQueueService.sendSubHomeworkProcessResult(new ArrayList<>(entities));
        }
    }

    @Override
    public void upsert(SubHomeworkProcessResult entity) {
        if (entity != null) {
            subHomeworkProcessResultDao.upsert(entity);
            homeworkHBaseQueueService.sendSubHomeworkProcessResult(Collections.singletonList(entity));
        }
    }
}
