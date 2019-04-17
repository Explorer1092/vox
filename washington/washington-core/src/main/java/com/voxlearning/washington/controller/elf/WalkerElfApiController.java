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

package com.voxlearning.washington.controller.elf;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.WalkerElfProductType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.api.entity.ReadingDraft;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.nekketsu.consumer.ElfLoaderClient;
import com.voxlearning.utopia.service.nekketsu.consumer.ElfServiceClient;
import com.voxlearning.utopia.service.nekketsu.elf.entity.*;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.walker.elf.net.messages.*;
import com.voxlearning.utopia.service.walker.elf.net.types.GiftInfo;
import com.voxlearning.utopia.service.walker.elf.net.types.ReadingWord;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.support.AbstractGameSupportController;
import lombok.NoArgsConstructor;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_APP_KEY;
import static com.voxlearning.washington.controller.open.ApiConstants.REQ_SESSION_KEY;

/**
 * @author Sadi.Wan
 * @since 2014/6/12
 */
@Controller
@RequestMapping("/student/walker/elf")
@NoArgsConstructor
public class WalkerElfApiController extends AbstractGameSupportController {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject private ElfServiceClient elfServiceClient;
    @Inject private ElfLoaderClient elfLoaderClient;

    private static final boolean isVitalityTurnOn = false;       //not use vitality any more, so add this switch to turn off it

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        // log pv
        LogCollector.instance().info("a17zy_app_pv_logs",
                MiscUtils.map(
                        "app_key", OrderProductServiceType.WalkerElf.name(),
                        "user_id", currentUserId(),
                        "platform", "pc",
                        "env", RuntimeMode.getCurrentStage(),
                        "client_ip", getWebRequestContext().getRealRemoteAddr()
                ));

        return "studentv3/afenti/walker/elf/index";
    }

    private static final String OPEN_API_COLLECTION_NAME = "vendor_apps_logs";

    public void logApiCallInfo() {
        Map<String, String> loggingInfo = new HashMap<>();
        loggingInfo.put(REQ_APP_KEY, OrderProductServiceType.WalkerElf.name());
        loggingInfo.put(REQ_SESSION_KEY, getRequestString(REQ_SESSION_KEY));
        loggingInfo.put("user_id", String.valueOf(currentUserId()));

        String apiName = getWebRequestContext().getRequest().getRequestURI();
        if (apiName.indexOf(".") > 0) {
            apiName = apiName.substring(0, apiName.indexOf("."));
        }
        loggingInfo.put("api_name", apiName);
        if ("/v1/clazz/share".equals(apiName) || "/v1/clazz/sysshare".equals(apiName) || "/v1/user/integral/add".equals(apiName) || apiName.startsWith("/v1/user/wechat/")) {
            loggingInfo.put("op", "batch");
        }
        loggingInfo.put("app_client_ip", getWebRequestContext().getRealRemoteAddr());
        LogCollector.instance().info(OPEN_API_COLLECTION_NAME, loggingInfo);
    }

    @RequestMapping(value = "initInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage initInfo() {
        MapMessage rtn = elfLoaderClient.initInfo(currentUserId());
        if (rtn.isSuccess()) {
            if (isVitalityTurnOn) {
                rtn.add("vitality", GameVitalityType.WALKER_ELF.refreshVitality(currentUserId(),
                        washingtonCacheSystem.CBS.unflushable).getKey());
            }
            rtn.add("paidUser", isPaidUser());
            logApiCallInfo();
        }
        rtn.add("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
        return rtn;
    }

    public static final String SHARE_CACHE_KEY_PREFIX = "ELF_SEND_SHARE_FLAG:";

    @RequestMapping(value = "saveplant.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePlant(String data) {
        SavePlantRequest req = null;
        try {
            req = SavePlantRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        try {
            MapMessage rtn = elfServiceClient.getRemoteReference().savePlant(currentUserId(), req.levelId, req.plantId);
            String cacheKey = SHARE_CACHE_KEY_PREFIX + currentUserId();
            if (!rtn.isSuccess()) {
                rtn.put("showShare", false);
            } else {
                Object ob = washingtonCacheSystem.CBS.unflushable.load(cacheKey);
                rtn.put("showShare", null == ob);
                if (null == ob) {
                    washingtonCacheSystem.CBS.unflushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), "");
                }
            }
            return rtn;
        } catch (Exception e) {
            logger.error("savePlant:userid {},levelId:{},plantId:{}.e{}", currentUserId(), req.levelId, req.plantId, e);
        }
        return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
    }

    @RequestMapping(value = "composeplant.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage composePlant(String data) {
        ComposePlantRequest req = null;
        try {
            req = ComposePlantRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        try {
            MapMessage rtn = elfServiceClient.getRemoteReference().composePlant(currentUserId(), req.levelId, req.plantId);
            String cacheKey = SHARE_CACHE_KEY_PREFIX + currentUserId();
            if (!rtn.isSuccess()) {
                rtn.put("showShare", false);
            } else {
                Object ob = washingtonCacheSystem.CBS.unflushable.load(cacheKey);
                rtn.put("showShare", null == ob);
                if (null == ob) {
                    washingtonCacheSystem.CBS.unflushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), "");
                }
            }
            return rtn;
        } catch (Exception e) {
            logger.error("composePlant:userid {},levelId:{},plantId:{}.e{}", currentUserId(), req.levelId, req.plantId, e);
        }
        return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
    }

    @RequestMapping(value = "finishanimate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishAnimate() {
        ElfBubble myBubble = elfLoaderClient.loadBubble(currentUserId());
        if (null == myBubble) {//flash不用关心这个错误，吞掉
            return MapMessage.successMessage();
        }
        if (myBubble.isPlayAnimate()) {
            try {
                elfServiceClient.getRemoteReference().finishAnimate(currentUserId());
            } catch (Exception e) {
                logger.error("finishAnimate:userid {}.e{}", currentUserId(), e);
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "getlevelplant.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getLevelPlant(String data) {
        GetLevelPlantRequest req = null;
        try {
            req = GetLevelPlantRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        try {
            return elfLoaderClient.getRemoteReference().getLevelPlant(currentUserId());
        } catch (Exception e) {
            logger.error("getlevelplant FAILED:userid {} ,Excepiton e", currentUserId(), e);
        }
        return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法");
    }

    @RequestMapping(value = "clickleveltab.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clickLeveltab(String data) {
        ClickLevelTabRequest req = null;
        try {
            req = ClickLevelTabRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        ElfBubble myBubble = elfLoaderClient.loadBubble(currentUserId());
        if (null == myBubble || !myBubble.getNewPlant().containsKey(req.levelId)) {//flash不用关心这个错误，吞掉
            return MapMessage.successMessage().add("showBookNew", false);
        }
        boolean stillShowBookNew = false;//点掉这个levetab后其他的levetab还有新的,则“新拯救植物”new标签还需要亮
        if (myBubble.getNewPlant().containsKey(req.levelId) && myBubble.getNewPlant().get(req.levelId)) {
            try {
                elfServiceClient.getRemoteReference().unsetLevelTabNew(currentUserId(), req.levelId);
                for (Map.Entry<String, Boolean> entry : myBubble.getNewPlant().entrySet()) {
                    if (req.levelId.equals(entry.getKey())) {
                        continue;
                    }
                    if (entry.getValue()) {
                        stillShowBookNew = true;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("clickLeveltab FAILED:userid {},levelId:{},Excepiton {}", currentUserId(), req.levelId, e);
            }
        }
        return MapMessage.successMessage().add("showBookNew", stillShowBookNew);
    }

    @RequestMapping(value = "loadgiftlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadGiftList() {
        ElfMyGiftList myGiftList = elfLoaderClient.loadGiftList(currentUserId());
        if (null == myGiftList) {
            return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
        }
        try {
            ElfBubble elfBubble = elfLoaderClient.loadBubble(currentUserId());
            if (elfBubble.isNewGift()) {
                elfServiceClient.getRemoteReference().unsetGiftBubble(currentUserId());
            }
        } catch (Exception e) {
            logger.error("unsetGiftBubble FAILED:userid {} ,Excepiton e", currentUserId(), e);
        }
        return MapMessage.successMessage().add("giftList", buildFlashGiftList(myGiftList));
    }

    private List<GiftInfo> buildFlashGiftList(ElfMyGiftList myGiftList) {
        List<GiftInfo> giftInfoList = new ArrayList<>();
        for (Map.Entry<String, ElfMyGift> entry : myGiftList.getGiftMap().entrySet()) {
            GiftInfo giftInfo = new GiftInfo();
            giftInfo.fillFrom(entry.getValue());
            giftInfoList.add(giftInfo);
        }
        Collections.reverse(giftInfoList);
        return giftInfoList;
    }

    @RequestMapping(value = "exchangegift.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeGift(String data) {

        ExchangeGiftRequest req = null;
        try {
            req = ExchangeGiftRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }

        if (!isPaidUser()) {
            return MapMessage.errorMessage().setErrorCode("100024").setInfo("你还未开通英语动画绘本，无法领取礼物奖励！");
        }

        String lock = new StringBuilder("ELF:EXCHANGEGIFT_LOCK:").append(currentUserId()).append(":lock").toString();
        try {
            atomicLockManager.acquireLock(lock, 10);
        } catch (CannotAcquireLockException ex) {
            logger.warn("GET_ELF:EXCHANGEGIFT_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
        }
        try {
            ElfMyGiftList myGiftList = elfLoaderClient.loadGiftList(currentUserId());
            if (null == myGiftList) {
                return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
            }

            if (!myGiftList.getGiftMap().containsKey(req.giftId)) {
                return MapMessage.errorMessage().setErrorCode("100019").setInfo("不存在的礼物");
            }

            ElfMyGift gift = myGiftList.getGiftMap().get(req.giftId);
            switch (gift.getGiftType()) {
                case INTEGRAL:
                    IntegralHistory integralHistory = new IntegralHistory(currentUserId(), IntegralType.拯救精灵王礼物);
                    integralHistory.setIntegral(gift.getCount());
                    integralHistory.setComment(IntegralType.拯救精灵王礼物.getDescription());
                    MapMessage addIntegralRs = userIntegralService.changeIntegral(currentUser(), integralHistory);
                    if (!addIntegralRs.isSuccess()) {
                        return MapMessage.errorMessage().setErrorCode("100020").setInfo("领取学豆礼物失败");
                    }
                    break;
            }
            MapMessage rs = elfServiceClient.getRemoteReference().exchangeGift(currentUserId(), gift);
            if (!rs.isSuccess()) {
                return rs;
            }
            myGiftList = (ElfMyGiftList) rs.get("modified");
            return rs.add("giftList", buildFlashGiftList(myGiftList));
        } catch (Exception e) {
            logger.error("exchangeGift FAILED:userid {},req:{}Excepiton e {}", currentUserId(), data, e);
        } finally {
            atomicLockManager.releaseLock(lock);
        }
        return MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
    }

    @RequestMapping(value = "loadwordlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadWordList(String data) {
        LoadWordListRequest req;
        try {
            req = LoadWordListRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        if (StringUtils.trimToEmpty(req.bookId).length() < 2) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        List<ReadingWord> readingWordList = new ArrayList<>();


        MapMessage message = businessHomeworkServiceClient.getReadingDraftByReadingId(NumberUtils.toLong(req.bookId.substring(1)));

        if (null == message || !message.isSuccess()) {
            return MapMessage.errorMessage().setErrorCode("100003").setInfo("读本不存在");
        }
        List<Map<String, String>> cnEnText = new ArrayList<>();
        ReadingDraft rd = (ReadingDraft) message.get("readingDraft");
        List<Map<String, Object>> readingPages = (List<Map<String, Object>>) rd.getContent().get("readingPages");
        for (Map<String, Object> map : readingPages) {
            List<Map<String, String>> keyWords = (List<Map<String, String>>) map.get("keyWords");
            List<Map<String, String>> readingSentences = (List<Map<String, String>>) map.get("readingSentences");
            if (CollectionUtils.isNotEmpty(keyWords)) {
                for (Map<String, String> keywordMap : keyWords) {
                    ReadingWord readingWord = new ReadingWord();
                    readingWord.cnText = keywordMap.get("cntext");
                    readingWord.enText = keywordMap.get("entext");
                    readingWordList.add(readingWord);
                }
            }
            if (CollectionUtils.isNotEmpty(readingSentences)) {
                for (Map<String, String> sentenceMp : readingSentences) {
                    Map<String, String> cmap = new HashMap<>();
                    cmap.put("entext", sentenceMp.get("entext"));
                    cmap.put("cntext", sentenceMp.get("cntext"));
                    cnEnText.add(cmap);
                }
            }
        }
        return MapMessage.successMessage().add("wordList", readingWordList);
    }

    //FIXME DELETE THIS METHOD AFTER xml import!!
    @RequestMapping(value = "writexml.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage writeXml() throws Exception {
        if (!RuntimeMode.isDevelopment()) {
            return MapMessage.errorMessage();
        }
        File root = new File("E:\\wkbooks");
        File[] files = root.listFiles();
        int finishCount = 0;
        int bookCount = files.length;
        StringBuilder probableWrongText = new StringBuilder();
        for (File f : files) {
            try {
                long bookId = NumberUtils.toLong(StringUtils.substringAfter(f.getName(), "B"));
                KeyValuePair<String, ArrayList<Map<String, Object>>> kvp = loadBookStcEnCnList(bookId);
                List<Map<String, Object>> cnEnList = kvp.getValue();
                if (null == cnEnList) {
                    return MapMessage.errorMessage().setErrorCode("100003").setInfo("读本" + f.getName() + "不存在");
                }
                String xmlPath = f.getAbsolutePath() + "\\xml\\resource.xml";
                Document doc = new SAXReader().read(new File(xmlPath));
                List encap = doc.selectNodes("//resource/captions/caption[@lang='en']");
                List cncap = doc.selectNodes("//resource/captions/caption[@lang='cn']");
                Element enCap = (Element) encap.get(0);
                Element cnCap = (Element) cncap.get(0);
                List enLst = enCap.elements("text");
                List cnLst = cnCap.elements("text");
                if (enLst.size() - cnEnList.size() != 1) {
                    probableWrongText.append(bookId).append(" 从db中读到").append(cnEnList.size()).append("句话,但xml中有").append(enLst.size() - 1).append("句话\r\n");
                    continue;
                }
                for (int i = 0; i < enLst.size(); i++) {
                    if (i == 0) {
                        ((Element) cnLst.get(i)).addAttribute("txt", kvp.getKey());
                        continue;
                    }
                    Element enEle = (Element) enLst.get(i);
                    Element cnEle = (Element) cnLst.get(i);
                    Map<String, Object> cnEnMap = cnEnList.get(i - 1);
                    if (!cnEnMap.get("entext").toString().replaceAll("[^a-zA-Z-0-9]", "").equalsIgnoreCase(enEle.attributeValue("txt").replaceAll("[^a-zA-Z-0-9]", ""))) {
                        probableWrongText.append(bookId).append("(exp:").append(cnEnMap.get("entext")).append(",found:").append(enEle.attributeValue("txt")).append("\r\n");
                    }
                    cnEle.addAttribute("txt", cnEnMap.get("cntext").toString());
                }
                XMLWriter writer = new XMLWriter(new FileOutputStream(new File(xmlPath)));
                writer.write(doc);
                writer.close();
                finishCount++;
            } catch (Exception ex) {
                System.out.println("异常书本:" + f.getName());
                ex.printStackTrace();
                return MapMessage.errorMessage().setErrorCode("100003").setInfo("读本" + f.getName() + "写入错误，停止程序");
            }
        }
        String rs = "共" + bookCount + "本书，成功写入" + finishCount + "本,疑似内容不匹配:" + probableWrongText.toString();
        System.out.println(rs);
        return MapMessage.successMessage().add("rs", rs);
    }

    private KeyValuePair<String, ArrayList<Map<String, Object>>> loadBookStcEnCnList(long bookId) {
        MapMessage message = businessHomeworkServiceClient.getReadingDraftByReadingId(bookId);
        if (null == message || !message.isSuccess()) {
            return null;
        }
        ArrayList<Map<String, Object>> cnEnText = new ArrayList<>();
        ReadingDraft rd = (ReadingDraft) message.get("readingDraft");
        List<Map<String, Object>> readingPages = (List<Map<String, Object>>) rd.getContent().get("readingPages");
        for (Map<String, Object> map : readingPages) {
            List<Map<String, Object>> readingSentences = (List<Map<String, Object>>) map.get("readingSentences");
            if (CollectionUtils.isNotEmpty(readingSentences)) {
                for (Map<String, Object> sentenceMp : readingSentences) {
                    Map<String, Object> cmap = new HashMap<>();
                    cmap.put("entext", sentenceMp.get("entext"));
                    cmap.put("cntext", sentenceMp.get("cntext"));
                    cmap.put("rank", sentenceMp.get("rank"));
                    cnEnText.add(cmap);
                }
            }
        }
        Collections.sort(cnEnText, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Integer) o1.get("rank")).compareTo((Integer) o2.get("rank"));
            }
        });
        return new KeyValuePair<>(rd.getCname(), cnEnText);
    }

    @RequestMapping(value = "startreading.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage startReading(String data) {
        StartReadingRequest req = null;
        try {
            req = StartReadingRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        MapMessage rtn;
        try {
            rtn = elfServiceClient.getRemoteReference().startReading(currentUserId(), req.bookId, converValidBookId());
            if (isVitalityTurnOn) {
                int vitality = GameVitalityType.WALKER_ELF.refreshVitality(currentUserId(),
                        washingtonCacheSystem.CBS.unflushable).getKey();
                if (vitality <= 0) {
                    return MapMessage.errorMessage().setErrorCode("100023").setInfo("精力不足");
                }
                rtn.add("vitality", vitality);
            }
        } catch (Exception e) {
            logger.error("clickLeveltab FAILED:userid {},bookId:{},Excepiton {}", currentUserId(), req.bookId, e);
            rtn = MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
        }
        return rtn;
    }

    @RequestMapping(value = "finishreading.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishReading(String data) {
        FinishReadingRequest req = null;
        try {
            req = FinishReadingRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        ElfUserRecord elfUserRecord = elfLoaderClient.loadUserRecord(currentUserId());
        if (null == elfUserRecord) {
            return MapMessage.errorMessage().setErrorCode("100002").setInfo("获取用户记录失败");
        }

        if (StringUtils.isEmpty(req.bookId)
                || !elfUserRecord.getReadingTimers().containsKey(req.bookId)
                || null == elfUserRecord.getReadingTimers().get(req.bookId).getFnTime()) {
            return MapMessage.errorMessage().setErrorCode("100017").setInfo("没有正在阅读的读本");
        }

        Date now = new Date();
        if (elfUserRecord.getReadingTimers().get(req.bookId).getFnTime().after(now)) {//还没读完呢
            return MapMessage.errorMessage().setErrorCode("100018").setInfo("阅读尚未完成");
        }
        String lock = new StringBuilder("ELF:FINISH_READING_LOCK:").append(currentUserId()).append(":lock").toString();
        try {
            atomicLockManager.acquireLock(lock, 10);
        } catch (CannotAcquireLockException ex) {
            logger.warn("GET_ELF:FINISH_READING_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
        }
        MapMessage rtn;
        try {
            if (elfUserRecord.getBookRecordMap().containsKey(req.bookId) && elfUserRecord.getBookRecordMap().get(req.bookId).isSunGained()) {
                //已经读过的读本
                rtn = elfServiceClient.getRemoteReference().finishGainedReading(currentUserId(), elfUserRecord, req.bookId, now);
            } else {
                //初次完成阅读
                rtn = elfServiceClient.getRemoteReference().finishFirstReading(currentUserId(), elfUserRecord, req.bookId, now);
            }
            if (isVitalityTurnOn) {
                int vitality = GameVitalityType.WALKER_ELF.refreshVitality(currentUserId(), washingtonCacheSystem.CBS.unflushable).getKey();
                if (vitality <= 0) {
                    return MapMessage.errorMessage().setErrorCode("100023").setInfo("精力不足");
                }
                if (RuntimeMode.le(Mode.TEST)) {
                    rtn.add("vitality", GameVitalityType.WALKER_ELF.refreshVitality(currentUserId(),
                            washingtonCacheSystem.CBS.unflushable).getKey());
                } else {
                    rtn.add("vitality", GameVitalityType.WALKER_ELF.incVitality(currentUserId(),
                            washingtonCacheSystem.CBS.unflushable, -1).getKey());
                }

                if (RuntimeMode.lt(Mode.DEVELOPMENT)) {
                    GameVitalityType.WALKER_ELF.refreshVitality(currentUserId(),
                            washingtonCacheSystem.CBS.unflushable);
                }
            }
        } catch (Exception e) {
            logger.error("finishReading_DUBBLE FAILED:userid {},req:{},Excepiton {}", currentUserId(), data, e);
            rtn = MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
        } finally {
            atomicLockManager.releaseLock(lock);
        }
        return rtn;
    }

    @RequestMapping(value = "loadachievementlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadAchievementList() {
        try {
            return elfLoaderClient.getRemoteReference().loadAchvForDisplay(currentUserId());
        } catch (Exception e) {
            logger.error("loadAchievementList FAILED:userid {},Excepiton {}", currentUserId(), e);
        }
        return MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
    }

    @RequestMapping(value = "exchangeachv.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeAchv(String data) {
        ExchangeAchvRequest req = null;
        try {
            req = ExchangeAchvRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }

        if (!isPaidUser()) {
            return MapMessage.errorMessage().setErrorCode("100022").setInfo("你还未开通英语动画绘本，无法领取成就奖励！");
        }
        String lock = new StringBuilder("ELF:EXCHANGEACHV_LOCK:").append(currentUserId()).append(":lock").toString();
        try {
            atomicLockManager.acquireLock(lock, 10);
        } catch (CannotAcquireLockException ex) {
            logger.warn("GET_ELF:EXCHANGEACHV_LOCK_FAILED: DUPLICATED_OPERATION", currentUserId());
            return MapMessage.errorMessage().setErrorCode("100999").setInfo("安全检查失败。");
        }
        try {

            ElfAchievementType elfAchievementType = ElfAchievementType.valueOf(req.achvId);
            if (null == elfAchievementType) {
                return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法");
            }
            ElfMyAchievementMap elfMyAchievementMap = elfLoaderClient.loadAchv(currentUserId());
            if (null == elfMyAchievementMap) {
                return MapMessage.errorMessage().setErrorCode("100015").setInfo("读取数据失败");
            }

            if (!elfMyAchievementMap.getAchievementMap().get(elfAchievementType).isExchangable()) {
                return MapMessage.errorMessage().setErrorCode("100021").setInfo("成就不可兑换");
            }

            if (elfAchievementType.getIntegralCount() > 0) {
                IntegralHistory integralHistory = new IntegralHistory(currentUserId(), IntegralType.拯救精灵王礼物);
                integralHistory.setIntegral(elfAchievementType.getIntegralCount());
                integralHistory.setComment("领取英语动画绘本成就");
                MapMessage addIntegralRs = userIntegralService.changeIntegral(currentUser(), integralHistory);
                if (!addIntegralRs.isSuccess()) {
                    return MapMessage.errorMessage().setErrorCode("100020").setInfo("领取学豆礼物失败");
                }
            }
            return elfServiceClient.getRemoteReference().exchangeAchv(currentUserId(), elfAchievementType, elfMyAchievementMap);
        } catch (Exception e) {
            logger.error("exchangeGift FAILED:userid {},req:{}Excepiton e {}", currentUserId(), data, e);
        } finally {
            atomicLockManager.releaseLock(lock);
        }
        return MapMessage.errorMessage().setErrorCode("100013").setInfo("登录失败，连接出错");
    }

    @RequestMapping(value = "loadlevelbooklist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadLevelBookList(String data) {
        LoadLevelBookListRequest req;
        try {
            req = LoadLevelBookListRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        return elfLoaderClient.getRemoteReference().loadLevelBookWithRecord(currentUserId(), req.levelId, converValidBookId().contains(req.levelId));
    }

    @RequestMapping(value = "sendshare.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendShare(String data) {
        SendShareRequest req;
        try {
            req = SendShareRequest.parseRequest(data);
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }

        if (StringUtils.isEmpty(StringUtils.trimToEmpty(req.imgUrl)) || StringUtils.isEmpty(StringUtils.trimToEmpty(req.shareText))) {
            return MapMessage.errorMessage().setErrorCode("100014").setInfo("参数非法。");
        }
        StudentDetail student = currentStudentDetail();
        Clazz clazz = currentStudentDetail().getClazz();
        if (clazz == null || !clazz.isSystemClazz()) {
            zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                    .withUser(student.getId())
                    .withUser(student.fetchUserType())
                    .withClazzJournalType(ClazzJournalType.APP_SHARE)
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                    .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                            "app_name", "英语动画绘本",
                            "app_url", "/student/apps/index.vpage?app_key=WalkerElf",
                            "share_text", req.shareText,
                            "share_img", req.imgUrl)))
                    .withPolicy(JournalDuplicationPolicy.DAILY)
                    .commit();
        } else {
            List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);
            for (GroupMapper group : groups) {
                zoneQueueServiceClient.createClazzJournal(student.getClazzId())
                        .withUser(student.getId())
                        .withUser(student.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.APP_SHARE)
                        .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                                "app_name", "英语动画绘本",
                                "app_url", "/student/apps/index.vpage?app_key=WalkerElf",
                                "share_text", req.shareText,
                                "share_img", req.imgUrl)))
                        .withPolicy(JournalDuplicationPolicy.DAILY)
                        .withGroup(group.getId())
                        .commit();
            }
        }
        washingtonCacheSystem.CBS.unflushable.add(SHARE_CACHE_KEY_PREFIX + currentUserId(), DateUtils.getCurrentToDayEndSecond(), "");
        return MapMessage.successMessage();
    }

    private boolean isPaidUser() {
        AppPayMapper paidStatus = userOrderLoaderClient.getUserAppPaidStatus("WalkerElf", currentUserId());
        return paidStatus.isActive();
    }

    private Set<String> converValidBookId() {
        AppPayMapper paidStatus = userOrderLoaderClient.getUserAppPaidStatus("WalkerElf", currentUserId());
        List<String> validAppItems = paidStatus.getValidAppItems();
        if (CollectionUtils.isEmpty(validAppItems)) {
            return Collections.emptySet();
        }
        Set<String> rtn = new HashSet<>();
        for (String item : validAppItems) {
            switch (WalkerElfProductType.valueOf(item)) {
                case BookAll:
                    rtn.addAll(Arrays.asList("SAVE_PRINCE", "SAVE_QUEEN", "SAVE_KING"));
                    break;
                case BookOne:
                    rtn.add("SAVE_PRINCE");
                    break;
                case BookTwo:
                    rtn.add("SAVE_QUEEN");
                    break;
                case BookThree:
                    rtn.add("SAVE_KING");
                    break;
                default:
                    break;
            }
        }
        return rtn;
    }
}
