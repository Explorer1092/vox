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

package com.voxlearning.washington.controller.nekketsu.adventure;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.BookStatus;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AchievementType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.AdventureConstants;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.*;
import com.voxlearning.utopia.service.nekketsu.cache.NekketsuCache;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureLoaderClient;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureServiceClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.psr.entity.PsrPrimaryAppEnContent;
import com.voxlearning.utopia.service.psr.entity.PsrPrimaryAppEnItem;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 沃克大冒险
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/21 12:10
 */
@Controller
@RequestMapping("/student/nekketsu/adventure")
public class AdventureController extends AbstractController {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    // ========================================================================
    // ADVENTURE
    // ========================================================================
    @Inject protected AdventureServiceClient adventureServiceClient;
    @Inject protected AdventureLoaderClient adventureLoaderClient;

    /**
     * 登录
     *
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/login.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage login() {
        boolean isPay = isPay(false);
        MapMessage response = adventureLoaderClient.login(currentUserId());
        if (!response.isSuccess()) {
           NewBookProfile newBookProfile = newClazzBookLoaderClient.fetchUserBook(currentStudentDetail(), Subject.ENGLISH);
            if (null == newBookProfile || newBookProfile.getOldId() == null) {
                return MapMessage.errorMessage().setErrorCode("101006").setInfo("没有找到默认教材");
            }
            Long bookId = newBookProfile.getOldId();
            Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
            response = adventureServiceClient.createUserAdventure(currentUserId(), bookId, getWords(bookId), classLevel);
        }
        response.add("isPay", isPay);
        return response;
    }

    /**
     * 未找到用户默认教材，需要根据用户选择的教材创建大冒险信息
     *
     * @param data flash数据，json
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/loginwithbookid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loginWithBookId(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        Long bookId = Long.valueOf((String) map.get("bookId"));
        if (null == bookId) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
        return adventureServiceClient.createUserAdventure(currentUserId(), bookId, getWords(bookId), classLevel);
    }

    /**
     * 获取未领取的礼物
     *
     * @return Gift数组
     */
    @RequestMapping(value = "/getstageungrantgifts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getStageUngrantGifts() {
        return adventureLoaderClient.getStageUngrantGifts(currentUserId());
    }

    @RequestMapping(value = "/canplay.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage canPlay() {
        boolean canPlay = true;
        String reason = "";

        if (RuntimeMode.ge(Mode.STAGING)) {
            String flag = washingtonCacheSystem.CBS.unflushable.load(AdventureConstants.NEW_STAGE_CACHE_KEY_PREFIX + currentUserId());
            if (StringUtils.isNotEmpty(flag)) {
                //bookId_stageOrder 与当前比较，若不同，canPlay = false;
                BookStages bookStages = adventureLoaderClient.getBookStagesByUserId(currentUserId());//取得当前
                if (null == bookStages || !Objects.equals(flag, bookStages.getBookId() + "_" + bookStages.getCurrentStage())) {
                    canPlay = false;
                    reason = "daily";
                }
            }
        }

        if (canPlay && !isPay(true)) {
            UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(currentUserId());
            if (null != userAdventure) {
                canPlay = userAdventure.getTrialCount() < AdventureConstants.TRIAL_COUNT;
                if (!canPlay) {
                    reason = "trial";
                }
            }
        }
        return MapMessage.successMessage().add("canPlay", canPlay).add("reason", reason);
    }


    /**
     * 提交抽奖
     *
     * @param data
     * @return
     */
    @RequestMapping(value = "/receiveFreeBeans.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage receiveFreeBeans(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        if (map.containsKey("bookId") == false || map.containsKey("stageOrder") == false) {
            return MapMessage.errorMessage().setInfo("参数不足");
        }
        Long bookId = Long.valueOf((String) map.get("bookId"));
        Integer stageOrderId = (Integer) map.get("stageOrder");
        Long userId = currentUserId();
        return this.adventureServiceClient.receiveFreeBeans(userId, bookId, stageOrderId);
    }

    /**
     * 获取问题
     *
     * @param data flash数据，json
     * @return 完整问题
     */
    @RequestMapping(value = "/getquestion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getQuestion(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }

        Integer stageOrder;
        String fileName;
        Integer stageAppOrder;
        try {
            Map<String, Object> map = JsonUtils.fromJson(data);
            stageOrder = (Integer) map.get("stageOrder");
            fileName = (String) map.get("fileName");
            stageAppOrder = (Integer) map.get("stageAppOrder");
        } catch (Exception e) {
            logger.error("invalid param:{}", data);
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        if (checkStageOrder(stageOrder) || checkStageAppOrder(stageAppOrder) || StringUtils.isEmpty(fileName)) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }

        //FIXME 前端传还是去服务端取
        BookStages bookStages = adventureLoaderClient.getBookStagesByUserId(currentUserId());
        if (null == bookStages) {
            return MapMessage.errorMessage().setErrorCode("101015").setInfo("没有找到教材与关卡对应信息");
        }


        if (!bookStages.getStages().containsKey(stageOrder)) {
            return MapMessage.errorMessage().setErrorCode("101000")
                    .setInfo("CurrentStage is : " + bookStages.getCurrentStage());
        }
        Stage stage = bookStages.getStages().get(stageOrder);
        StageApp stageApp = stage.getApps().get(stageAppOrder);
        if (!Objects.equals(fileName, stageApp.getFileName())) {
            return MapMessage.errorMessage().setErrorCode("101010")
                    .setInfo("小游戏序号“" + stageAppOrder + "”与文件名“" + fileName + "”不匹配");
        }
        Long bookId = bookStages.getBookId();

        List<String> wordList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(bookStages.getStages().get(stageOrder).getWords())) {
            wordList.addAll(stage.getWords());
        } else {
            wordList.addAll(getWords(bookId));
            adventureServiceClient.addStageWords(currentUserId(), bookId, stageOrder, wordList);
        }

        List<String> wordRelation = new LinkedList<>();
        List<Sentence> psrSentence = this.loadSentenceFromWordListAndBook(wordList, bookId, wordRelation);
        Collections.shuffle(psrSentence);

        PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(fileName);
        MapMessage mapMessage = flashGameServiceClient.loadDataFromSentenceList(currentUserId(), getCdnBaseUrlStaticSharedWithSep(), psrSentence, englishPractice, Ktwelve.PRIMARY_SCHOOL, null, false);
        String gameData = JsonUtils.toJson(mapMessage.get("gameData"));
        mapMessage.remove("gameData");
        mapMessage.add("gameData", gameData);
        gameFlashLoaderConfigManager.setupFlashV1Url(mapMessage, getRequest(), fileName, fileName);
        gameFlashLoaderConfigManager.setupFlashV1ComponentUrl(mapMessage, getRequest(), fileName);
        mapMessage.add("wordRelation", wordRelation);
        return mapMessage;
    }


    @RequestMapping(value = "adventure.vpage", method = RequestMethod.GET)
    public String books(Model model) {
        UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(currentUserId());
        Book book = null;
        Long bookId = null;
        if (null != userAdventure) {
            bookId = userAdventure.getBookId();
            book = englishContentLoaderClient.loadEnglishBook(bookId);
        }
        if (null != book) {
            model.addAttribute("adventureBookName", book.getCname());
            model.addAttribute("adventureBookId", bookId);
        }
        return "/studentv3/book/adventure";
    }

    /**
     * 获取可选教材
     *
     * @return Book数组
     */
    @RequestMapping(value = "/getavailablebooks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAvailableBooks(Integer clazzLevel) {
        List<Book> adventureBooks = getAdventureBooksWithCache(clazzLevel);
        engPaintedSkin(adventureBooks);
        return MapMessage.successMessage().add("books", adventureBooks);
    }

    @RequestMapping(value = "/gethistorybooks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHistoryBooks() {
        UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(currentUserId());
        if (null == userAdventure) {
            return MapMessage.successMessage().add("books", new ArrayList<>());
        }
        List<Long> bookIds = userAdventure.getBookIds();
        if (CollectionUtils.isEmpty(bookIds)) {
            return MapMessage.errorMessage();
        }
        Map<Long, Book> bookMap = englishContentLoaderClient.loadEnglishBooks(bookIds);
        List<Book> bookList = new LinkedList<>(bookMap.values());
        engPaintedSkin(bookList);
        return MapMessage.successMessage().add("books", bookList);
    }

    @RequestMapping(value = "/getnextbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getNextBook() {
        Long userId = currentUserId();
        UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(userId);

        final Book currentBook = englishContentLoaderClient.loadEnglishBook(userAdventure.getBookId());
        final Integer currentBookRank = 2 * (currentBook.getClassLevel() - 1) + currentBook.getTermType();
        int bookClassLevel = currentBook.getClassLevel();
        //后一本教材需要根据学期判断，如果TermType是2，则取classLevel加1；特殊情况，当六年级时，不做处理
        if (null != currentBook.getTermType() && 2 == currentBook.getTermType() && bookClassLevel != 6) {
            bookClassLevel += 1;
        }
        List<Book> bookList = getAdventureBooksWithCache(bookClassLevel).stream().filter(source ->
                Objects.equals(currentBook.getPress(), source.getPress()) &&
                        (2 * (source.getClassLevel() - 1) + source.getTermType() - 1) == currentBookRank
        ).collect(Collectors.toList());

        if (bookList.size() >= 1) {
            Long bookId = bookList.get(0).getId();
            Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
            return adventureServiceClient.changeBook(userId, bookId, getWords(bookId), classLevel);
        }
        return MapMessage.errorMessage().setErrorCode("101012").setInfo("没有找到后一本教材");
    }

    @RequestMapping(value = "/getprebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public MapMessage getPreBook() {
        Long userId = currentUserId();
        UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(userId);

        final Book currentBook = englishContentLoaderClient.loadEnglishBook(userAdventure.getBookId());
        final Integer currentBookRank = 2 * (currentBook.getClassLevel() - 1) + currentBook.getTermType();


        List<Book> bookList = getAdventureBooks().stream().filter(source ->
                Objects.equals(currentBook.getPress(), source.getPress()) &&
                        (2 * (source.getClassLevel() - 1) + source.getTermType() + 1) == currentBookRank
        ).collect(Collectors.toList());

        if (bookList.size() >= 1) {
            Long bookId = bookList.get(0).getId();
            Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
            return adventureServiceClient.changeBook(userId, bookId, getWords(bookId), classLevel);
        }
        return MapMessage.errorMessage().setErrorCode("101013").setInfo("没有找到前一本教材");
    }

    // FIXME: 先临时解决吧。GAME_TIME数据太大了，可能把缓存冲爆了。
    // FIXME: 赶紧优化这里的业务
    protected List<Book> getAdventureBooksWithCache(Integer clazzLevel) {
        return washingtonCacheSystem.CBS.flushable
                .wrapCache(this)
                .keys(clazzLevel)
                .expiration(CachedObjectExpirationPolicy.today)
                .proxy()
                .getAdventureBooks(clazzLevel);
    }

    protected List<Book> getAdventureBooks(final Integer clazzLevel) {

        Set<Long> islandPracticeIds = practiceLoaderClient.loadSubjectPractices(Subject.ENGLISH).stream()
                .filter(PracticeType::isIslandPractice)
                .map(PracticeType::getId)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Collection<Integer> practices = new LinkedHashSet<>();
        for (Long practiceId : islandPracticeIds) {
            practices.add(practiceId.intValue());
        }

        Map<Long, Book> bookMap = englishContentLoaderClient.getExtension().loadEnglishBooksByPracticeTypes(practices);

        List<Book> books = bookMap.values().stream()
                .filter(t -> BookStatus.safeParse(t.getStatus()) == BookStatus.ONLINE)
                .filter(t -> t.getClassLevel() != null)
                .filter(t -> Objects.equals(t.getClassLevel(), clazzLevel))
                .sorted((o1, o2) -> Long.compare(o1.getId(), o2.getId()))
                .collect(Collectors.toList());

        for (Book book : books) {
            if (!StringUtils.contains(book.getImgUrl(), "catalog_new")) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                if (bookPress != null) {
                    book.setViewContent(bookPress.getViewContent());
                    book.setColor(bookPress.getColor());
                }
                book.setImgUrl(StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
            }
        }

        return books.stream()
                .sorted((o1, o2) -> {
                    String c1 = StringUtils.defaultString(o1.getCname());
                    String c2 = StringUtils.defaultString(o2.getCname());
                    int ret = c1.compareTo(c2);
                    if (ret != 0) return ret;
                    int t1 = SafeConverter.toInt(o1.getTermType());
                    int t2 = SafeConverter.toInt(o2.getTermType());
                    return Integer.compare(t1, t2);
                })
                .collect(Collectors.toList());
    }

    private List<Book> getAdventureBooks() {
        int clazzLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
        return getAdventureBooksWithCache(clazzLevel);
    }

    /**
     * 更换教材
     */
    @RequestMapping(value = "/changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook() {
        Long bookId = getRequestLong("bookId");
        Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
        UserAdventure adventure = adventureLoaderClient.getUserAdventureByUserId(currentUserId());
        if (null == adventure) {
            return adventureServiceClient.createUserAdventure(currentUserId(), bookId, getWords(bookId), classLevel);
        }

        //FIXME 需要增加判断是否要传入List<String> words
        return adventureServiceClient.changeBook(currentUserId(), bookId, getWords(bookId), classLevel);
    }

    /**
     * 小游戏成绩提交
     *
     * @param data flash数据，json
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/stagemisson.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage stageMisson(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map map = JsonUtils.fromJson(data);
        if (null == map) {
            logger.error("Illegal JSON: {}", data);
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Long bookId = Long.valueOf((String) map.get("bookId"));
        Integer stageOrder = Integer.valueOf(String.valueOf(map.get("stageOrder")));
        Integer stageAppOrder = Integer.valueOf(String.valueOf(map.get("appOrder")));
        Integer count = Integer.valueOf(String.valueOf(map.get("count")));
        String fileName = (String) map.get("fileName");
        String result = (String) map.get("result");
        List<String> wordRelation = (List<String>) map.get("wordRelation");
        if (null == bookId || checkStageOrder(stageOrder) || checkStageAppOrder(stageAppOrder)
                || null == count || count < 0 || count > 3
                || StringUtils.isEmpty(fileName)) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }

        if (currentStudentDetail() == null) {
            return MapMessage.errorMessage().setInfo("请登录后再使用!");
        }

        if (currentStudentDetail().getClazz() == null) {
            return MapMessage.errorMessage().setInfo("未加入班级!");
        }

        Integer classLevel = Integer.valueOf(currentStudentDetail().getClazz().getClassLevel());
        MapMessage response = adventureServiceClient.stageMisson(currentUserId(), bookId, stageOrder,
                stageAppOrder, fileName, count, classLevel, !isPay(true));
        if (response.isSuccess() && StringUtils.isNotEmpty(result) && CollectionUtils.isNotEmpty(wordRelation)) {
            try {
                GameResult gameResult = JsonUtils.fromJson(result, GameResult.class);
//                processGameResult(currentUserId(), bookId, currentStudentDetail().getClazzId(), gameResult,
//                        wordRelation, (Integer) response.get("appId"));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        return response;
    }

    @RequestMapping(value = "/getbeyondclassmates.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getBeyondClassmates(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map map = JsonUtils.fromJson(data);
        if (map == null) {
            logger.error("Illegal JSON: {}", data);
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }

        Integer stageOrder = Integer.valueOf(String.valueOf(map.get("stageOrder")));
        if (checkStageOrder(stageOrder)) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        MapMessage response = adventureLoaderClient.getBeyondClassmates(currentUserId(),
                currentStudentDetail().getClazzId(), stageOrder);
        return response;
    }

    /**
     * 关卡皇冠奖励、分享
     *
     * @param data flash数据，json
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/stagecrownreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage stageCrownReward(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        Integer stageOrder = (Integer) map.get("stageOrder");
        Boolean shared = (Boolean) map.get("shared");
        if (checkStageOrder(stageOrder) || null == shared) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        if (shared) {
            share(AdventureClazzJournal.Type.CROWN,
                    "恭喜" + currentUser().getProfile().getRealname() + "获得沃克单词冒险之奇幻探险第" + stageOrder + "关皇冠",
                    null, null);
        }
        return adventureServiceClient.stageCrownReward(currentUserId(), stageOrder, shared);
    }

    /**
     * 班级超越分享、更改当前开启的关卡
     *
     * @param data flash数据，json
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/openstage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage openStage(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        if (null == map) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Integer stageOrder = (Integer) map.get("stageOrder");
        Boolean shared = (Boolean) map.get("shared");
        if (checkStageOrder(stageOrder) || null == shared) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        //FIXME 班级超越是否分享，处理图片
        MapMessage response = adventureServiceClient.openStage(currentUserId(), stageOrder, shared);
        if (shared && response.isSuccess()) {
            List<Map> beyondClassmates = (List<Map>) map.get("beyondClassmates");
            if (CollectionUtils.isNotEmpty(beyondClassmates)) {
                List<BeyondClassmate> list = new LinkedList<>();
                for (Map classmate : beyondClassmates) {
                    try {
                        Long userId = Long.valueOf((String) classmate.get("userId"));
                        String name = (String) classmate.get("name");
                        String img = (String) classmate.get("img");
                        BeyondClassmate beyondClassmate = BeyondClassmate.newInstance(userId, name, img);
                        if (null != beyondClassmate) {
                            list.add(beyondClassmate);
                        }
                    } catch (Exception e) {
                    }
                }
                if (null != list && 0 != list.size()) {
                    share(AdventureClazzJournal.Type.BEYOND,
                            "恭喜" + currentUser().getProfile().getRealname() + "开启沃克单词冒险之奇幻探险第" + stageOrder + "关，进度超越以下同学：",
                            null, list);
                }
            }
        }
        return response;
    }

    /**
     * 领取礼物
     *
     * @param data flash数据，json
     * @return GiIft数组
     */
    @RequestMapping(value = "/grantgift.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage grantGift(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        String id = (String) map.get("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }
        MapMessage response = adventureServiceClient.grantGift(id);
        if (response.isSuccess()) {
            Gift gift = (Gift) response.get("gift");
//            processGift(gift);
            response.remove("gift");
        }
        return response;
    }

    /**
     * 领取成就
     *
     * @param data flash数据，json
     * @return UserAdventure
     */
    @RequestMapping(value = "/grantachievement.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage grantAchievement(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        String achievementTypeStr = (String) map.get("achievementType");
        if (StringUtils.isEmpty(achievementTypeStr)) {
            return MapMessage.errorMessage().setInfo("参数不完整");
        }

        AchievementType achievementType = AchievementType.valueOf(achievementTypeStr);

        MapMessage response = adventureServiceClient.grantAchievement(currentUserId(), achievementType);
        if (response.isSuccess()) {
            Gift gift = (Gift) response.get("gift");
//            processGift(gift);
            response.remove("gift");
        }
        return response;
    }

//    private void processGift(Gift gift) {
//        if (gift.getPkVitalityNum() > 0) {
//            pkServiceClient.addVitality(currentUser(), gift.getPkVitalityNum(), VitalityType.沃克大冒险奇幻探险获得活力值);
//        }
//    }

    /**
     * 查询PK武器
     *
     * @return PkEquipment数组
     */
    @RequestMapping(value = "/getpkequipment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getPkEquipment() {
        return MapMessage.errorMessage().setErrorCode("101001").setInfo("PK馆已经下线了!");
//        Role role = pkLoaderClient.loadRole(currentUserId());
//        if (null == role) {
//            return MapMessage.errorMessage().setErrorCode("101001").setInfo("PK角色不存在 : " + currentUserId());
//        }
//        List<EquipmentConfig> equipmentConfigs = pkConfigLoaderClient.equipmentConfigCache()
//                .loadAll().stream()
//                .filter(t -> t.getCategory() == SourceCategory.ADVENTURE)
//                .collect(Collectors.toList());
//        List<PkEquipment> pkEquipments = new LinkedList<>();
//        if (CollectionUtils.isEmpty(equipmentConfigs)) {
//            return MapMessage.successMessage().add("pkEquipments", pkEquipments);
//        }
//        PkEquipment pkEquipment;
//        for (EquipmentConfig initial : equipmentConfigs) {
//            if (null == initial.getCareer() || initial.getCareer() == role.getCareer()) {
//                pkEquipment = new PkEquipment();
//                pkEquipment.setId(initial.getId());
//                pkEquipment.setName(initial.getName());
//                pkEquipment.setImg(initial.getImg());
//                pkEquipment.setDesc(initial.getDesc());
//                equipmentValue(initial.getViolet().getBaseAttribute(), initial.getViolet().getSecondaryAttribute(), pkEquipment);
//                pkEquipments.add(pkEquipment);
//            }
//        }
//        return MapMessage.successMessage().add("pkEquipments", pkEquipments);
    }

//    private void equipmentValue(EquipmentAttribute baseAttribute, EquipmentAttribute secondaryAttribute, PkEquipment pkEquipment) {
//        if (null == baseAttribute || null == secondaryAttribute) {
//            return;
//        }
//        pkEquipment.setBaseAttribute(baseAttribute);
//        pkEquipment.setSecondaryAttribute(secondaryAttribute);
//        switch (secondaryAttribute.getType()) {
//            case CRIT: {
//                pkEquipment.getSecondaryAttribute().setValue(pkEquipment.getSecondaryAttribute().getValue() * 20);
//                break;
//            }
//            case AGI: {
//                pkEquipment.getSecondaryAttribute().setValue(pkEquipment.getSecondaryAttribute().getValue() * 20);
//                break;
//            }
//            case HIT: {
//                pkEquipment.getSecondaryAttribute().setValue(pkEquipment.getSecondaryAttribute().getValue() * 20);
//                break;
//            }
//            case RES: {
//                pkEquipment.getSecondaryAttribute().setValue(pkEquipment.getSecondaryAttribute().getValue() * 20);
//                break;
//            }
//            default:
//                break;
//        }
//    }

    /**
     * 兑换PK武器
     *
     * @param data flash数据，json
     * @return 成功/失败
     */
    @RequestMapping(value = "/exchangepkequipment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangePkEquipment(String data) {
        return MapMessage.errorMessage().setInfo("PK馆已经下线了!");
//        if (StringUtils.isEmpty(data)) {
//            return MapMessage.errorMessage().setInfo("没有输入参数");
//        }
//        if (null == pkLoaderClient.loadRole(currentUserId())) {
//            return MapMessage.errorMessage().setErrorCode("101001").setInfo("PK角色不存在 : " + currentUserId());
//        }
//
//        int bagSpaceLeft = pkLoaderClient.getBagEquipmentSpaceLeft(currentUserId());
//        if (bagSpaceLeft <= 0) {
//            return MapMessage.errorMessage().setErrorCode("101002").setInfo("PK角色背包已满 : " + currentUserId());
//        }
//
//        Map<String, Object> map = JsonUtils.fromJson(data);
//        String pkEquipmentId = (String) map.get("id");
//        if (StringUtils.isEmpty(pkEquipmentId)) {
//            return MapMessage.errorMessage().setInfo("参数不完整");
//        }
//
//        //判断水晶数
//        UserAdventure userAdventure = adventureLoaderClient.getUserAdventureByUserId(currentUserId());
//
//        if (null == userAdventure || userAdventure.getCurrentDiamond() < PkEquipment.getPrice(pkEquipmentId)) {
//            return MapMessage.errorMessage().setErrorCode("101003")
//                    .setInfo(currentUserId() + "钻石数量不够兑换PK装备: " + pkEquipmentId);
//        }
//        MapMessage response = pkServiceClient.addEquipment(currentUserId(), pkEquipmentId + "_violet");
//        if (response.isSuccess()) {
//            response = adventureServiceClient.exchangePkEquipment(currentUserId(),
//                    PkEquipment.getPrice(pkEquipmentId), pkEquipmentId);
//            if (response.isSuccess() && response.containsKey("userAdventure")) {
//                userAdventure = (UserAdventure) response.get("userAdventure");
//            }
//        }
//        return MapMessage.successMessage().add("userAdventure", userAdventure).add("id", pkEquipmentId);
    }

    @RequestMapping(value = "/shareexchangepkequipment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shareExchangePkEquipment(String data) {
        if (StringUtils.isEmpty(data)) {
            return MapMessage.errorMessage().setInfo("没有输入参数");
        }
        Map<String, Object> map = JsonUtils.fromJson(data);
        String name = (String) map.get("name");
        String imgUrl = (String) map.get("imgUrl");
        share(AdventureClazzJournal.Type.EXCHANGE,
                "恭喜" + currentUser().getProfile().getRealname() + "在沃克单词冒险之奇幻探险中兑换" + name + "。",
                imgUrl, null);
        return MapMessage.successMessage();
    }


    /**
     * 开启下一组关卡（15关）
     *
     * @return UserAdventure和BookStages
     */
    @RequestMapping(value = "/opennextstagegroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage openNextStageGroup() {
        return adventureServiceClient.openNextStageGroup(currentUserId());
    }

    private void share(AdventureClazzJournal.Type type, String content, String img, List<BeyondClassmate> classmates) {
        AdventureClazzJournal adventureClazzJournal = new AdventureClazzJournal();
        adventureClazzJournal.setContent(content);
        adventureClazzJournal.setImg(img);
        adventureClazzJournal.setType(type);
        adventureClazzJournal.setClassmates(classmates);

        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        if (!clazz.isSystemClazz()) {// 非系统自建班级
            zoneQueueServiceClient.createClazzJournal(clazz.getId())
                    .withUser(student.getId())
                    .withUser(student.fetchUserType())
                    .withClazzJournalType(ClazzJournalType.WALKER_ADVENTURE)
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                    .withJournalJson(JsonUtils.toJson(adventureClazzJournal))
                    .commit();
        } else {// 系统自建班级
            List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(currentUserId(), false);
            // FIXME：取第一个组，这样也很蛋疼。。。
            GroupMapper group = MiscUtils.firstElement(groups);
            if (group != null) {
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withUser(student.getId())
                        .withUser(student.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.WALKER_ADVENTURE)
                        .withClazzJournalCategory(ClazzJournalCategory.APPLICATION)
                        .withJournalJson(JsonUtils.toJson(adventureClazzJournal))
                        .withGroup(group.getId())
                        .commit();
            }
        }
    }

    private List<String> getWords(Long bookId) {
        List<String> wordList = new LinkedList<>();
        PsrPrimaryAppEnContent psrPrimaryAppEnContent = utopiaPsrServiceClient.getPsrPrimaryAppEn("adventure", currentUserId(),
                currentStudentDetail().getCityCode(), bookId, 0L, AdventureConstants.STAGE_WORDS_COUNT, "");
        if (psrPrimaryAppEnContent.getErrorContent().equals("success")) {
            for (PsrPrimaryAppEnItem enItem : psrPrimaryAppEnContent.getAppEnList()) {
                wordList.add(enItem.getEid());
            }
        }
        return wordList;
    }

    private List<Sentence> loadSentenceFromWordListAndBook(List<String> wordList, Long book, List<String> wordRelation) {
        List<Unit> unitList = englishContentLoaderClient.loadEnglishBookUnits(book);
        Collection<Long> unitIdCollection = unitList.stream()
                .map(Unit::getId)
                .filter(t -> t != null)
                .collect(Collectors.toList());
        Map<Long, List<Lesson>> lessonListMap = englishContentLoaderClient.loadEnglishUnitLessons(unitIdCollection);
        List<Long> lessonIdList = new ArrayList<>();

        Map<Long, Lesson> lessonMap = new LinkedHashMap<>();
        if (null != lessonListMap && !lessonListMap.isEmpty()) {
            for (Map.Entry<Long, List<Lesson>> entry : lessonListMap.entrySet()) {

                List<Lesson> lessonList = entry.getValue();
                if (lessonList == null) {
                    lessonList = Collections.emptyList();
                }
                lessonList.stream()
                        .filter(t -> t != null)
                        .filter(t -> t.getId() != null)
                        .forEach(t -> lessonMap.put(t.getId(), t));

                lessonIdList.addAll(lessonList.stream()
                        .filter(t -> t != null)
                        .filter(t -> t.getId() != null)
                        .map(Lesson::getId)
                        .collect(Collectors.toList()));
            }
        }
        Map<String, Sentence> sentenceMap = new LinkedHashMap<>();
        Map<String, String> wordRelationMap = new LinkedHashMap<>();
        Map<Long, List<Sentence>> sentenceListMap = englishContentLoaderClient.loadEnglishLessonSentences(lessonIdList);
        if (null != sentenceListMap && !sentenceListMap.isEmpty()) {
            for (Map.Entry<Long, List<Sentence>> entry : sentenceListMap.entrySet()) {
                for (Sentence sentence : entry.getValue()) {
                    sentenceMap.put("word#" + sentence.getEnText(), sentence);
                    long lessonId = entry.getKey();
                    long unitId = lessonMap.get(lessonId).getUnitId();
                    wordRelationMap.put("word#" + sentence.getEnText(), unitId + "$" + lessonId + "$" + sentence.getEnText());
                }
            }
        }

        List<Sentence> matchSentence = new ArrayList<>();
        for (String word : wordList) {
            Sentence sentence = sentenceMap.get(word);
            if (sentence != null) {
                matchSentence.add(sentenceMap.get(word));
            }
            wordRelation.add(wordRelationMap.get(word));
        }
        return matchSentence;
    }

    private boolean checkStageOrder(Integer stageOrder) {
        return (null == stageOrder || stageOrder < 1 || stageOrder > 60);
    }

    private boolean checkStageAppOrder(Integer stageAppOrder) {
        return (null == stageAppOrder || stageAppOrder < 1 || stageAppOrder > 5);
    }

    public boolean isPay(boolean userCache) {
        Boolean isPay = null;
        if (userCache) {
            isPay = NekketsuCache.getNekketsuCache().load(AdventureConstants.USER_PAID_KEY_PREFIX + currentUserId());
        }
        if (null == isPay) {
            AppPayMapper map = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.Walker.name(), currentUserId());
            if (null != map) {
                isPay = map.isActive();
            }

            NekketsuCache.getNekketsuCache().delete(AdventureConstants.USER_PAID_KEY_PREFIX + currentUserId());
            NekketsuCache.getNekketsuCache().add(AdventureConstants.USER_PAID_KEY_PREFIX + currentUserId(), 30 * 60, isPay);
        }
        return Boolean.TRUE.equals(isPay);
    }


}
