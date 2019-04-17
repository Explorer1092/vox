package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.campaign.api.CampaignService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadSentenceResult;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * 点读机跟读
 *
 * @author jiangpeng
 * @since 2017-03-09 上午11:21
 **/
@Controller
@RequestMapping(value = "/v1/parent/selfstudy/piclisten/followread")
@Slf4j
public class ParentSelfStudyFollowReadApiController extends AbstractSelfStudyApiController {


    @ImportService(interfaceClass = CampaignService.class)
    private CampaignService campaignService;


    static final MapMessage NO_FOLLOW_READ_CONTENT_RESULT =
            successMessage().add(RES_HAS_READ_CONTENT, false).add(RES_HAS_NO_READ_CONTENT_TEXT, "本单元没有可以跟读的内容~ 换一个单元试试吧");

    /**
     *  生成作品
     * @return
     */
    @RequestMapping(value = "/collection.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createCollect() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_UNIT_ID, "单元id");
            validateRequiredNumber(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_SCORE_IDS, "打分结果");
            validateRequest(REQ_UNIT_ID, REQ_STUDENT_ID, REQ_SCORE_IDS);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0)
            return failMessage(RES_RESULT_NO_STUDENT_FOUND_MSG);
        String unitId = getRequestString(REQ_UNIT_ID);

        NewBookCatalog unitNode = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(unitId)).get(unitId);
        if (unitNode == null || !unitNode.getNodeType().equals(BookCatalogType.UNIT.name()))
            return failMessage(RES_RESULT_UNIT_ERROR_MSG);
        NewBookCatalogAncestor bookNode = unitNode.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (bookNode == null)
            return failMessage(RES_RESULT_UNIT_ERROR_MSG);
        List<String> scoreIdList = JsonUtils.fromJsonToList(getRequestString(REQ_SCORE_IDS), String.class);
        if (CollectionUtils.isEmpty(scoreIdList))
            return failMessage(RES_RESULT_SCORE_IDS_ERROR);
        MapMessage mapMessage = parentSelfStudyService.generateFollowReadCollection(studentId, unitId, scoreIdList);
        if ( !mapMessage.isSuccess())
            return failMessage(RES_RESULT_CREATE_COLLECTION_FAIL);

        Boolean  overLimit = SafeConverter.toBoolean(mapMessage.get("is_over_limit"));
        String collectionId = SafeConverter.toString(mapMessage.get("collection_id"));
        if (overLimit)
            return successMessage().add(RES_IS_OVER_LIMIT, true).add(RES_WARNING_TEXT, "今天生成录音作品次数太多啦~明天再来试试吧！");
        else
            return successMessage().add(RES_SHARE_URL, fetchMainsiteUrlByCurrentSchema() + "/view/wx/parent/reading/repeat?content_id=" + collectionId + "&book_id=" + bookNode.getId() + "&unit_id=" + unitId )
                    .add(RES_IS_OVER_LIMIT, false);
    }


    /**
     * 上报打分结果
     * @return
     */
    @RequestMapping(value = "/report.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage reportScore() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_UNIT_ID, "单元id");
            validateRequiredNumber(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_PICLISTEN_ID, "点读内容id");
            validateRequired(REQ_SENTENCE_ID, "句子ID");
            validateRequired(REQ_SCORE, "打分结果");
            validateRequired(REQ_RECORE_URL, "录音url");
            validateRequired(REQ_RECORD_TIME, "录音时长");
            validateRequest(REQ_UNIT_ID, REQ_STUDENT_ID, REQ_PICLISTEN_ID, REQ_SENTENCE_ID, REQ_SCORE, REQ_RECORE_URL, REQ_RECORD_TIME);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }


        String unitId = getRequestString(REQ_UNIT_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String picListenId = getRequestString(REQ_PICLISTEN_ID);
        Long sentenceId = getRequestLong(REQ_SENTENCE_ID);
        String scoreJsonStr = getRequestString(REQ_SCORE);
        String audioUrl = getRequestString(REQ_RECORE_URL);
        Integer recordTime = getRequestInt(REQ_RECORD_TIME);

        if (studentId == 0)
            return failMessage(RES_RESULT_NO_STUDENT_FOUND_MSG);

        Boolean isOverLimit = parentSelfStudyService.loadIsFollowReadOverLimit(studentId, picListenId, sentenceId).getUninterruptibly();
        if (isOverLimit) {
            return successMessage().add(RES_OVER_LIMIT, true).add(RES_WARNING_TEXT, "今日读太多次了，换一句读一下吧");
        }

        FollowReadSentenceResult.UselessWrapper uselessWrapper = JsonUtils.fromJson(scoreJsonStr, FollowReadSentenceResult.UselessWrapper.class);
        if (uselessWrapper == null)
            return failMessage(RES_RESULT_SCORE_ERROR);
        List<FollowReadSentenceResult.ScoreResult> lines = uselessWrapper.getLines();
        if (CollectionUtils.isEmpty(lines))
            return failMessage(RES_RESULT_SCORE_ERROR);
        FollowReadSentenceResult.ScoreResult scoreResult = lines.get(0);
        if (scoreResult == null)
            return failMessage(RES_RESULT_SCORE_ERROR);
        if (scoreResult.getBegin() == null || scoreResult.getEnd() == null || scoreResult.getFluency() == null
                || scoreResult.getIntegrity() == null || scoreResult.getPronunciation() == null || scoreResult.getUsertext() == null
                || scoreResult.getSample() == null || scoreResult.getScore() == null || scoreResult.getWords() == null
                || recordTime == 0 )
            return failMessage(RES_RESULT_SCORE_ERROR);


        FollowReadSentenceResult result = new FollowReadSentenceResult();
        result.setStudentId(studentId);
        result.setUnitId(unitId);
        result.setPicListenId(picListenId);
        result.setSentenceId(sentenceId);
        result.setAudioUrl(audioUrl);
        result.setScoreResult(scoreResult);
        result.setRecordTime(recordTime);

        MapMessage mapMessage = parentSelfStudyService.processFollowResult(result);
        if (!mapMessage.isSuccess())
            return failMessage(mapMessage.getInfo());
        campaignService.addLotteryFreeChance(CampaignType.UNICORN_ACTIVITY_LOTTERY, studentId, 1);
        return successMessage().add(RES_SCORE, mapMessage.get("level")).add(RES_SCORE_RESULT_ID, mapMessage.get("result_id")).add(RES_OVER_LIMIT, false);
    }


    /**
     * 跟读内容
     * @return
     */
    @RequestMapping(value = "/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage followReadList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_UNIT_ID, "单元id");
            validateRequiredNumber(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_UNIT_ID, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        if (studentId == 0)
            return failMessage(RES_RESULT_NO_STUDENT_FOUND_MSG);
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())) {
            return NO_FOLLOW_READ_CONTENT_RESULT;
        }
        NewBookCatalogAncestor newBookCatalogAncestor = unit.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (newBookCatalogAncestor == null)
            return failMessage(RES_RESULT_UNIT_ERROR_MSG);
        //65804:跟读打分功能，免费开放

        List<PicListen> allPicListen = questionLoaderClient.loadPicListenByNewUnitId(unitId);



        if (CollectionUtils.isEmpty(allPicListen))
            return NO_FOLLOW_READ_CONTENT_RESULT;

        Set<Long> allSentenceIdSet = new HashSet<>();
        Map<String, AlpsFuture<Boolean>> sentenceReadOverLimitFutureMap = new HashMap<>();
        for (PicListen picListen : allPicListen) {
            List<Long> allSentenceIdList = picListen.getAllSentenceIdList();
            if (CollectionUtils.isEmpty(allSentenceIdList))
                continue;
            boolean hasSentence = false;
            for (Long sentenceId : allSentenceIdList) {
                if (sentenceId == 0 )
                    continue;
                String key = picListenSentenceKey(picListen.getId(), sentenceId);
                AlpsFuture<Boolean> readLimitFuture = parentSelfStudyService.loadIsFollowReadOverLimit(studentId, picListen.getId(), sentenceId);
                sentenceReadOverLimitFutureMap.put(key, readLimitFuture);
                hasSentence = true;
            }
            if (!hasSentence)
                continue;
            allSentenceIdSet.addAll(allSentenceIdList);
        }
        if (CollectionUtils.isEmpty(allSentenceIdSet))
            return NO_FOLLOW_READ_CONTENT_RESULT;

        MapMessage mapMessage = successMessage();
        mapMessage.add(RES_READ_INTERVAL, 1000);
        mapMessage.add(RES_OVER_LIMIT_TEXT, "今日读太多次了，换一句读一下吧"); // TODO: 2017/3/9 更新文案
        mapMessage.add(RES_HAS_READ_CONTENT, true);

        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(allSentenceIdSet);

        String cdnByEnv ;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest())
            cdnByEnv = "https://cdn.17zuoye.com";
        else
            cdnByEnv = getCdnBaseUrlStaticSharedWithSep();

        List<Map<String, Object>> contentMapList = new ArrayList<>();
        int picListenIndex = 1;
        for (PicListen picListen : allPicListen) {
            Map<String, Object> picListenMap = new LinkedHashMap<>();
            picListenMap.put(RES_PICLISTEN_ID, picListen.getId());
            List<Long> sentenceIds = picListen.getAllSentenceIdList();
            if (CollectionUtils.isEmpty(sentenceIds)) {
                picListenMap.put(RES_SENTENCE_LIST, new ArrayList<>());
                contentMapList.add(picListenMap);
                continue;
            }
            if (MapUtils.isEmpty(sentenceMap)) {
                picListenMap.put(RES_SENTENCE_LIST, new ArrayList<>());
                contentMapList.add(picListenMap);
                continue;
            }
            List<Map<String, Object>> sentenceMapList = new ArrayList<>();
            int i = 1;
            for (Long sentenceId : sentenceIds) {
                if (sentenceId == 0 )
                    continue;
                Sentence sentence = sentenceMap.get(sentenceId);
                if (sentence == null)
                    continue;
                Map<String, Object> map = new HashMap<>();
                map.put(RES_SENTENCE_ID, sentence.getId());
                map.put(RES_TEXT, sentence.getEnText());
                map.put(RES_AUDIO_URL, cdnByEnv + sentence.getWaveUri());
                map.put(RES_AUDIO_TIME, sentence.getWavePlayTime());
                map.put(RES_RECORD_LIMIT_TIME, calculateRecodeLimitTime(sentence.getWavePlayTime()));
                map.put(RES_INDEX, i++);
                map.put(RES_OVER_LIMIT, getAlpsFutureResult(sentenceReadOverLimitFutureMap.get(picListenSentenceKey(picListen.getId(), sentenceId)), false));
                map.put(RES_SCORE_STATUS, "show");
                sentenceMapList.add(map);
            }
            if (CollectionUtils.isEmpty(sentenceMapList)) {
                picListenMap.put(RES_SENTENCE_LIST, new ArrayList<>());
                contentMapList.add(picListenMap);
                continue;
            }
            picListenIndex++;
            picListenMap.put(RES_SENTENCE_LIST, sentenceMapList);
            contentMapList.add(picListenMap);
        }
        if (CollectionUtils.isEmpty(contentMapList))
            return NO_FOLLOW_READ_CONTENT_RESULT;
        return mapMessage.add(RES_UNIT_CONTENT_LIST, contentMapList);

    }


    private String picListenSentenceKey(String picListenId, Long sentenceId) {
        return picListenId + "_" + sentenceId;
    }

    private double calculateRecodeLimitTime(Integer audioTime) {
        if (audioTime == null)
            return 5000;
        return audioTime * 1.2 + 2000;
    }
}
