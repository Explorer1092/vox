package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.luffy.controller.AbstractXcxController;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.consumer.NewEnglishContentLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.luffy.controller.ApiConstants.*;
import static com.voxlearning.luffy.controller.ContentApiConstants.*;

/**
 * Created by Summer on 2018/6/28
 */

@Controller
@Slf4j
@RequestMapping(value = "/xcx/open")
public class ParentXcxOpenController extends AbstractXcxController {

    @Inject private NewEnglishContentLoaderClient newEnglishContentLoaderClient;
    private static List<String> filterLesson = Arrays.asList("语感练习", "听力练习", "语法练习");


    /**
     * 点读专用
     * 单元的点读内容
     *
     * @return
     */
    @RequestMapping(value = "/unit/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage unitPicListen() {
        String unitId = getRequestString(REQ_UNIT_ID);
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null || (!BookCatalogType.UNIT.name().equals(unit.getNodeType()) && !BookCatalogType.LESSON.name().equals(unit.getNodeType()))) {
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        }
        NewBookCatalogAncestor bookCatalogAncestor = unit.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);

        List<PicListen> allPicListen;
        if (unit.getNodeType().equals(BookCatalogType.LESSON.name())) {
            allPicListen = questionLoaderClient.loadPicListenByNewLessonId(unitId);
        } else {
            allPicListen = questionLoaderClient.loadPicListenByNewUnitId(unitId);
        }

        Map<String, Object> unitInfoMap = new LinkedHashMap<>();

        unitInfoMap.put(RES_UNIT_ID, unit.getId());
        unitInfoMap.put(RES_UNIT_CNAME, unit.getName());
        unitInfoMap.put(RES_TOTAL_INDEX, allPicListen.size());

        List<Map<String, Object>> picListenMapList = new ArrayList<>();

        int index = 1;
        for (PicListen t : allPicListen) {
            Map<String, Object> picListenMap = new LinkedHashMap<>();
            addIntoMap(picListenMap, RES_PICLISTEN_ID, t.getId());
            addIntoMap(picListenMap, RES_PIC_URL, t.getImgUrl());
            addIntoMap(picListenMap, RES_PIC_FILENAME, t.getImgFilename());
            addIntoMap(picListenMap, RES_INDEX, index);
            addIntoMap(picListenMap, RES_BLOCK_DATA, t.getContent());
            addIntoMap(picListenMap, RES_BLOCK_SENTENCES, t.getBlocks());
            picListenMapList.add(picListenMap);
            index++;
        }
        unitInfoMap.put(RES_PICLISTEN_LIST, picListenMapList);
        return MapMessage.successMessage().add(RES_UNIT_INFO, unitInfoMap);
    }


    /**
     * 除人教外点读教材获取单元详情
     */
    @RequestMapping(value = "/pay/book/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookDetailForPay() {
        String bookId = getRequestString(REQ_BOOK_ID);
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null)
            return MapMessage.errorMessage("教材不存在");

        Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile);
        Map<String, Object> bookMap = generateBookDetailMapForPicListen(newBookProfile, bookNeedPay, true);

        return MapMessage.successMessage().add(RES_USER_BOOK, bookMap)
                .add(RES_PURCHASE_TEXT, "您即将进入点读机收费内容，如需体验完整内容，请购买该点读教材。");
    }


    /**
     * 随声听专用
     * 返回这个学生的课本信息
     */
    @RequestMapping(value = "/walkman/selfloadbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage selfloadbook() {
        String requestBookId = getRequestString(REQ_BOOK_ID);
        User parent = currentUser();
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(requestBookId);
        if (newBookProfile == null) {
            return MapMessage.errorMessage("教材不存在");
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog);

        Map<String, List<NewBookCatalog>> bookId2ModuleMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.MODULE);
        List<NewBookCatalog> moduleList = bookId2ModuleMap == null ? new ArrayList<>() : bookId2ModuleMap.get(newBookProfile.getId());
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        //随身听付费
        Boolean walkManNeedPay = textBookManagementLoaderClient.walkManNeedPay(requestBookId);
        Map<String, DayRange> dayRangeMap = parent == null ? null : picListenCommonService.parentBuyWalkManLastDayMap(parent.getId(), false);
        boolean walkManHasPurchase = dayRangeMap != null && (dayRangeMap.get(requestBookId) != null && new Date().before(dayRangeMap.get(requestBookId).getEndDate()));
        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (walkManNeedPay) {
            bookMap.put(RES_PURCHASE_TEXT, "您即将进入随身听收费内容，如需体验完整内容，请购买该教材。");
        }
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();
            Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient
                    .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
            List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
            if (!CollectionUtils.isEmpty(unitList)) {
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitMap = new LinkedHashMap<>();
                    unitMap.put(RES_UNIT_ID, unit.getId());
                    unitMap.put(RES_RANK, unit.getRank());
                    unitMap.put(RES_UNIT_CNAME, unit.getName());
                    unitMap.put(RES_UNIT_ENAME, unit.getAlias());
                    unitMap.put(RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, unitList.indexOf(unit) == 0));
                    unitMapList.add(unitMap);
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }
        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, BookCatalogType.UNIT);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }

            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();
                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(moduleId);
                if (unitList == null)
                    unitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getName());
                    addIntoMap(unitGroupInfo, RES_UNIT_ENAME, unit.getAlias());
                    addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                    addIntoMap(unitGroupInfo, RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, unitList.indexOf(unit) == 0 && moduleList.indexOf(module) == 0));
                    groupUnitList.add(unitGroupInfo);
                }

                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_GROUP_ENAME, module.getAlias());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupInfo.put(RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, moduleList.indexOf(module) == 0));
                groupList.add(groupInfo);

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }
        return MapMessage.successMessage().add(RES_USER_BOOK, bookMap);

    }

    /**
     * 随声听专用
     * 返回课本的单元信息
     */
    @RequestMapping(value = "/walkman/selfloadurl.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getUnitInfo() {
        Subject subject = Subject.safeParse(getRequestString(REQ_SUBJECT));
        String unitId = getRequestString(REQ_UNIT_ID);
        if (subject == null || subject != Subject.ENGLISH) {
            return MapMessage.errorMessage("不支持的学科");
        }

        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())) {
            return MapMessage.errorMessage();
        }

        Map<String, List<NewBookCatalog>> unti2LessonListMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
        if (MapUtils.isEmpty(unti2LessonListMap)) {
            return MapMessage.errorMessage();
        }

        List<NewBookCatalog> lessonList = unti2LessonListMap.get(unitId);
        if (CollectionUtils.isEmpty(lessonList)) {
            return MapMessage.errorMessage();
        }
        Set<String> lessonIds = lessonList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());

        Map<String, List<Sentence>> lesson2SentenceListMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIds);

        if (MapUtils.isEmpty(lesson2SentenceListMap)) {
            return MapMessage.errorMessage();
        }
        Map<String, Object> unitInfo = new LinkedHashMap<>();
        List<Map<String, Object>> lessonInfo = new LinkedList<>();

        for (NewBookCatalog lesson : lessonList) {
            List<Map> sentenceInfo = new LinkedList<>();

            if (!lesson2SentenceListMap.containsKey(lesson.getId())) {
                continue;
            }
            List<Sentence> sentencesList = lesson2SentenceListMap.get(lesson.getId());
            for (Sentence sentence : sentencesList) {
                if (StringUtils.isNotBlank(sentence.getWaveUri())) {
                    Map<String, Object> sentenceInfoMap = new LinkedHashMap<>();
                    sentenceInfoMap.put(RES_SENTENCE_ID, sentence.getId());
                    sentenceInfoMap.put(RES_SENTENCE_CNTEXT, sentence.getCnText());
                    sentenceInfoMap.put(RES_SENTENCE_ENTEXT, sentence.getEnText());
                    String url = getCdnBaseUrlStaticSharedWithSep() + sentence.getWaveUri();
                    sentenceInfoMap.put(RES_SENTENCE_WAVE_URI, url);
                    sentenceInfo.add(sentenceInfoMap);
                }
            }
            if (sentenceInfo.size() > 0) {
                if (containsNeedFilterLessons(lesson.getName())) {
                    continue;
                }
                Map<String, Object> lessonInfoMap = new LinkedHashMap<>();
                lessonInfoMap.put(RES_LESSON_CNAME, lesson.getName());
                lessonInfoMap.put(RES_LESSON_ENAME, lesson.getAlias());
                lessonInfoMap.put(RES_LESSON_ID, lesson.getId());
                lessonInfoMap.put(RES_SENTENCE_LIST, sentenceInfo);

                lessonInfo.add(lessonInfoMap);
            }
        }

        unitInfo.put(RES_UNIT_CNAME, unit.getName());
        unitInfo.put(RES_UNIT_ENAME, unit.getAlias());
        unitInfo.put(RES_RANK, unit.getRank());
        unitInfo.put(RES_LESSON_LIST, lessonInfo);

        return MapMessage.successMessage().add(RES_UNIT, unitInfo);
    }

    /**
     * 过滤几种（根据filterLesson定义）lesson
     */
    private boolean containsNeedFilterLessons(String lessonName) {
        if (StringUtils.isBlank(lessonName)) {
            return false;
        }
        for (String name : filterLesson) {
            if (lessonName.contains(name)) {
                return true;
            }
        }
        return false;
    }


    private Map<String, Object> generateBookDetailMapForPicListen(NewBookProfile newBookProfile, Boolean bookNeedPay, Boolean withUnit) {
        Boolean userPayedBook;
        DayRange lastDayRange = null; // 当前教材的有效期截止时间
        User currentUser = currentUser();
        if (bookNeedPay) {
            PicListenBookPayInfo picListenBookPayInfo = currentUser == null ? null :
                    picListenCommonService.userBuyBookPicListenLastDayMap(currentUser, false).get(newBookProfile.getId());
            lastDayRange = picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null;
            userPayedBook = lastDayRange != null && lastDayRange.getEndDate().after(new Date());
        } else
            userPayedBook = true; //免费的教材就当作是已付费了


        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog, true, lastDayRange);
        if (withUnit) {
            addBookStructure(bookMap, bookNeedPay, userPayedBook, newBookProfile);
        }
        return bookMap;
    }

    /**
     * 语文教材和英语教材的结构不同,但返回的给壳的结构都是unitGroup->unit
     * 英语教材取的结构是 module(对应返回unitGroup)->unit(对应返回unit)
     * 语文教材取的结构是 unit(对应返回unitGroup)->lesson(对应返回unit)
     * 注意
     * 1. 取zip包,两个学科方法貌似不一样
     * 2. 取点读内容的方法也不一样。。。
     * 3. 语文是 生词表 英语是单词表。。。
     *
     * @param bookMap
     * @param bookNeedPay
     * @param userPayedBook
     * @param newBookProfile
     */
    private void addBookStructure(Map<String, Object> bookMap, Boolean bookNeedPay, Boolean userPayedBook, NewBookProfile newBookProfile) {

        boolean isChinese = newBookProfile.getSubjectId() == Subject.CHINESE.getId();
        BookCatalogType unitGroupNode = isChinese ? BookCatalogType.UNIT : BookCatalogType.MODULE;
        BookCatalogType unitNode = isChinese ? BookCatalogType.LESSON : BookCatalogType.UNIT;

        String bookId = newBookProfile.getId();
        List<NewBookCatalog> moduleList = newContentLoaderClient.loadChildrenSingle(bookId, unitGroupNode);
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        List<NewBookCatalog> allUnitList = newContentLoaderClient.loadChildrenSingle(bookId, unitNode);
        List<String> unitIdList = allUnitList.stream().map(NewBookCatalog::getId).collect(Collectors.toList());
        Map<String, List<PicListen>> unitId2PicListMap;
        if (CollectionUtils.isNotEmpty(unitIdList)) {
            unitId2PicListMap = isChinese ? questionLoaderClient.loadPicListenByNewLessonIds(unitIdList) : questionLoaderClient.loadPicListenByNewUnitIds(unitIdList);
        } else {
            unitId2PicListMap = new HashMap<>();
        }


        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();

            if (!CollectionUtils.isEmpty(allUnitList)) {
                int i = 0;
                for (NewBookCatalog unit : allUnitList) {
                    List<PicListen> picListens = unitId2PicListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(picListens))
                        continue;
                    unitMapList.add(picUnitMap(unit, unitStatus(bookNeedPay, userPayedBook, i == 0)));
                    i++;
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }

        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, unitNode);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }
            int i = 0;
            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();

                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(moduleId);
                if (unitList == null)
                    unitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                int j = 0;
                for (NewBookCatalog unit : unitList) {
                    List<PicListen> picListens = unitId2PicListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(picListens))
                        continue;
                    groupUnitList.add(picUnitMap(unit, unitStatus(bookNeedPay, userPayedBook, i == 0 && j == 0)));
                    j++;
                }
                if (CollectionUtils.isEmpty(groupUnitList))
                    continue;
                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_MODULE_GROUP_ID, module.getId());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupList.add(groupInfo);
                i++;

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }
        bookMap.put(RES_HAS_WORD_LIST, textBookManagementLoaderClient.englishWordListShow(bookId));
        bookMap.put(RES_HAS_CHINESE_WORD_LIST, textBookManagementLoaderClient.chineseWordListShow(bookId));
    }

    private Map<String, Object> picUnitMap(NewBookCatalog unit, String unitStatus) {
        Map<String, Object> unitMap = new LinkedHashMap<>();
        addIntoMap(unitMap, RES_UNIT_ID, unit.getId());
        addIntoMap(unitMap, RES_UNIT_CNAME, unit.getName());
        addIntoMap(unitMap, RES_RANK, unit.getRank());
        addIntoMap(unitMap, RES_PAY_STATUS, unitStatus);
        return unitMap;
    }

    /**
     * 三种状态
     *
     * @param bookNeedPay   教材是否需要付费
     * @param userPayedBook 用户是否付费并且有效
     * @param isFirst       是不是第一个单元
     * @return
     */
    private String unitStatus(Boolean bookNeedPay, Boolean userPayedBook, Boolean isFirst) {
        if (!bookNeedPay) //不用付费的教材随便打开
            return RES_PAY_STATUS_FREE;
        else {
            if (userPayedBook)
                return RES_PAY_STATUS_FREE; //付费的用户随便打开
            else {
                if (isFirst)
                    return RES_PAY_STATUS_EXP; //未付费的第一单元体验
                else
                    return RES_PAY_STATUS_PAY; //其余单元需要付费
            }
        }
    }
}
