package com.voxlearning.utopia.admin.controller.ailesson;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.ChipsWechatShareUtil;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;

@Controller
@RequestMapping("/chips/ai/active/service")
public class AiLessonActiveServiceController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;
    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;
    @ImportService(interfaceClass = ChipsUserVideoService.class)
    private ChipsUserVideoService chipsUserVideoService;

    @RequestMapping(value = "/index.vpage")
    public String index(Model model) {
        Long classId = getRequestLong("classId");
        model.addAttribute("classId", classId);
        String bookId = chipsActiveService.loadBookIdByClassId(classId);
        model.addAttribute("bookId", bookId);

        ChipsActiveServiceType serviceType;
        try {
            serviceType = ChipsActiveServiceType.valueOf(getRequestString("serviceType"));
        } catch (Exception e) {
            serviceType = null;
        }
        model.addAttribute("serviceType", serviceType == null ? "ALL" : serviceType.name());
        return "ailesson/active_service";
    }

    @RequestMapping(value = "/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage infoDetail() {
        Long classId = getRequestLong("classId");
        String level = getRequestString("level");
        // 状态 1 完成、 0 未完成, 2 全部, 3 service 时的未审核
        int status = getRequestInt("status");
        // 单元 id
        String unitId = getRequestString("unitId");
        // 完成时间
        Date createDate = null;
        String dateStr = getRequestString("createDate");
        if (StringUtils.isNotBlank(dateStr)) {
            createDate = DateUtils.stringToDate(dateStr);
        }
        Date updateBeginDate = null;
        String updateDateStr = getRequestString("updateBeginDate");
        if (StringUtils.isNotBlank(updateDateStr)) {
            updateBeginDate = DateUtils.stringToDate(updateDateStr);
        }
        //用户 id
        Long userId = getRequestLong("userId", -1L);
        ChipsActiveServiceType serviceType;
        try {
            serviceType = ChipsActiveServiceType.valueOf(getRequestString("serviceType"));
        } catch (Exception e) {
            serviceType = null;
        }
        // 分页
        int pageNum = getRequestInt("pageNum");

        return chipsActiveService.obtainActiveServiceInfos(serviceType, classId, status, unitId, createDate, userId, pageNum, updateBeginDate, level);
    }

    @RequestMapping(value = "/otherServiceUserTemplateSave.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage otherServiceUserTemplateSave() {
        String serviceType = getRequestString("serviceType");
        long userId = getRequestLong("userId");
        long clazzId = getRequestLong("clazzId");
        String unitId = getRequestString("unitId");
        String renewType = getRequestString("renewType");
        if (ChipsActiveServiceType.REMIND.equals(ChipsActiveServiceType.of(serviceType))) {
            chipsActiveService.updateToReminded(clazzId, unitId, userId);
        }

        String url = ChipsWechatShareUtil.getWechatDomain() + "/chips/center/otherServiceTypePreview.vpage?userId=" + userId + "&serviceType=" + serviceType
                + "&clazzId=" + clazzId + "&t=" + System.currentTimeMillis();
        String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");
        if (StringUtils.isNotBlank(shortUrl)) {
            url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
        }
        return chipsActiveService.saveOtherServiceTypeUserTemplate(serviceType, userId, clazzId, renewType).set("url", url);
    }

    /**
     * 更新到已提示
     *
     * @return
     */
    @RequestMapping(value = "/updateToReminded.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateToReminded() {
        String serviceType = getRequestString("serviceType");
        Long classId = getRequestLong("classId");
        // 单元 id
        String unitId = getRequestString("unitId");
        //用户 id
        Long userId = getRequestLong("userId", -1L);
        if ("service".equals(serviceType.toLowerCase())) {
            return chipsActiveService.updateServiced(classId, unitId, userId);
        }
        return chipsActiveService.updateToReminded(classId, unitId, userId);
    }

    /**
     * 主动服务模板管理V2入口页面
     */
    @RequestMapping(value = "/activeServiceIndex.vpage")
    public String activeServiceIndexV2() {
        return "ailesson/activeServiceIndexV2";
    }


    @RequestMapping(value = "/otherServiceTypeAdd.vpage")
    public String otherServiceTypeAdd() {
        return "ailesson/otherServiceTypeAdd";
    }

    /**
     * 续费提醒编辑页面
     *
     * @return
     */
    @RequestMapping(value = "/otherServiceTypeAddV2.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String otherServiceTypeAddV2() {
        String renewType = getRequestString("renewType");
//        if("first".equals(renewType)){//首次
//            return "ailesson/otherServiceRenewFirstEdit";
//        } else {//后续
//            return "ailesson/otherServiceRenewFollowUpEdit";
//        }
        if ("v1".equals(renewType)) {
            return "ailesson/otherServiceRenewV1Edit";
        } else {
            return "ailesson/otherServiceRenewV2Edit";
        }
//        if ("v2".equals(renewType)) {
//
//        }
//        if ("wp".equals(renewType)) {
//
//        }

    }

    private RenewFirstPojo parseRenewFirstPojoFromJson(String json) {
        return JsonUtils.fromJson(json, RenewFirstPojo.class);
    }

    private RenewFirstPojo buildEmptyRenewFirstPojo() {
        RenewFirstPojo pojo = new RenewFirstPojo();
        List<RenewFirstPojo.WeekPoint> weekPointList = new ArrayList<>();
        for (UGCWeekPointsEnum weekPoint : UGCWeekPointsEnum.values()) {
            RenewFirstPojo.WeekPoint wp = new RenewFirstPojo.WeekPoint();
            wp.setWeekPointName(weekPoint.name());
            wp.setWeekPointDesc(weekPoint.getDesc());
            wp.setWeekPointLevel(weekPoint.getLevel());
            weekPointList.add(wp);
        }
        pojo.setWeekPointList(weekPointList);
        return pojo;
    }

    private RenewFollowUpPojo parseRenewFollowUpPojoFromJson(String json) {
        return JsonUtils.fromJson(json, RenewFollowUpPojo.class);
    }

    private RenewFollowUpPojo buildEmptyRenewFollowUpPojo() {
        return new RenewFollowUpPojo();
    }

    @RequestMapping(value = "/queryOtherServiceType.vpage")
    @ResponseBody
    public MapMessage queryOtherServiceType() {
        String id = getRequestString("id");
        String serviceType = getRequestString("serviceType");
        String name = getRequestString("name");
        ChipsOtherServiceTemplate template = chipsActiveService.loadOtherServiceTypeTemplate(id);
        MapMessage message = MapMessage.successMessage();
        message.add("id", id);
        message.add("serviceType", serviceType);
        if (serviceType.equals(ChipsActiveServiceType.RENEWREMIND.name())) {
            String renewType = getRequestString("renewType");//first
            if ("v1".equals(renewType)) {
                if (template != null && StringUtils.isNotBlank(template.getJson())) {
//                    List<ActiveServiceItem> itemList = JsonUtils.fromJsonToList(template.getJson(), ActiveServiceItem.class);
//                    message.add("itemList", itemList);
                    RenewV1Pojo pojo = JsonUtils.fromJson(template.getJson(), RenewV1Pojo.class);
//                    message.add("topList", pojo.getTopItemList());
//                    message.add("bottomList", pojo.getBottomItemList());
//                    message.add("weekPointList", pojo.getWeekPointList());
                    message.add("pojo", pojo);
                } else {
                    List<ActiveServiceItem> itemList = ActiveServiceItem.buildRenewV1();
                    List<ActiveServiceItem> bottomItemList = RenewV1Pojo.buildRenewV1Bottom();
                    List<ActiveServiceItem> topItemList = RenewV1Pojo.buildRenewV1Top();
                    List<RenewV1Pojo.WeekPoint> weekPointList = new ArrayList<>();
                    for (UGCWeekPointsEnum weekPoint : UGCWeekPointsEnum.values()) {
                        RenewV1Pojo.WeekPoint wp = new RenewV1Pojo.WeekPoint();
                        wp.setWeekPointName(weekPoint.name());
                        wp.setWeekPointDesc(weekPoint.getDesc());
                        wp.setWeekPointLevel(weekPoint.getLevel());
                        wp.setWpItemList(RenewV1Pojo.buildRenewV1WeekPoint());
                        weekPointList.add(wp);
                    }
                    RenewV1Pojo pojo = new RenewV1Pojo();
                    pojo.setTopItemList(topItemList);
                    pojo.setBottomItemList(bottomItemList);
                    pojo.setWeekPointList(weekPointList);
//                    message.add("topList", topItemList);
//                    message.add("bottomList", bottomItemList);
//                    message.add("weekPointList", weekPointList);
                    message.add("pojo", pojo);
//                    message.add("itemList", itemList);
                }
            } else {
                if (template != null && StringUtils.isNotBlank(template.getJson())) {
                    List<ActiveServiceItem> itemList = JsonUtils.fromJsonToList(template.getJson(), ActiveServiceItem.class);
                    message.add("itemList", itemList);
                } else {
                    List<ActiveServiceItem> itemList = ActiveServiceItem.buildRenewV2();
                    message.add("itemList", itemList);
                }
            }
        } else {
            if (template != null && StringUtils.isNotBlank(template.getJson())) {
                List<Map> mapList = JsonUtils.fromJsonToList(template.getJson(), Map.class);
                message.add("templateList", mapList);
            } else {
                List<Map<String, String>> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("text", "");
                    map.put("image", "");
                    list.add(map);
                }
                message.add("templateList", list);
                message.add("name", StringUtils.isBlank(name) ? "" : name);
            }
        }
        return message;
    }

    //    @RequestMapping(value = "/queryOtherServiceType.vpage")
//    @ResponseBody
    public MapMessage queryOtherServiceTypeOld() {
        String id = getRequestString("id");
        String serviceType = getRequestString("serviceType");
        String name = getRequestString("name");
        ChipsOtherServiceTemplate template = chipsActiveService.loadOtherServiceTypeTemplate(id);
        MapMessage message = MapMessage.successMessage();
        message.add("id", id);
        message.add("serviceType", serviceType);
        if (serviceType.equals(ChipsActiveServiceType.RENEWREMIND.name())) {
            String renewType = getRequestString("renewType");//first
            if ("first".equals(renewType)) {
                if (template != null && StringUtils.isNotBlank(template.getJson())) {
                    RenewFirstPojo pojo = parseRenewFirstPojoFromJson(template.getJson());
                    if (pojo == null) {
                        pojo = buildEmptyRenewFirstPojo();
                    }
                    message.add("pojo", pojo);
//                    message.add("pojo", buildEmptyRenewFirstPojo());
                } else {
                    message.add("pojo", buildEmptyRenewFirstPojo());
                }
            } else {
                if (template != null && StringUtils.isNotBlank(template.getJson())) {
                    RenewFollowUpPojo pojo = parseRenewFollowUpPojoFromJson(template.getJson());
                    if (pojo == null) {
                        pojo = buildEmptyRenewFollowUpPojo();
                    }
                    message.add("pojo", pojo);
                } else {
                    message.add("pojo", buildEmptyRenewFollowUpPojo());
                }
            }
        } else {
            if (template != null && StringUtils.isNotBlank(template.getJson())) {
                List<Map> mapList = JsonUtils.fromJsonToList(template.getJson(), Map.class);
                message.add("templateList", mapList);
            } else {
                List<Map<String, String>> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("text", "");
                    map.put("image", "");
                    list.add(map);
                }
                message.add("templateList", list);
                message.add("name", StringUtils.isBlank(name) ? "" : name);
            }
        }
        return message;
    }

    @RequestMapping(value = "/otherServiceTypeSave.vpage")
    @ResponseBody
    public MapMessage otherServiceTypeSave() {
        String id = getRequestString("id");
        String json = getRequestString("json");
        String serviceType = getRequestString("serviceType");
        String name = getRequestString("name");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().add("info", "id is blank");
        }
        ChipsOtherServiceTemplate template = new ChipsOtherServiceTemplate();
        template.setId(id);
        template.setJson(json);
        template.setName(name);
        template.setServiceType(serviceType);
        return chipsActiveService.saveOtherServiceTypeTemplate(template);
    }


    @RequestMapping(value = "/activeServiceAdd.vpage")
    public String activieServiceAddV2() {
        return "ailesson/activeServiceAddV2";
    }


    @RequestMapping(value = "/commonTemplateDataQuery.vpage")
    @ResponseBody
    public MapMessage commonTemplateDataQuery() {
        String qid = getRequestString("qid");
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));

        ActiveServiceTemplate template = chipsActiveService.loadActiveServiceTemplateById(qid);
        MapMessage message = MapMessage.successMessage();
        List<ActiveServicePronunciation> pronList;
        List<ActiveServiceGrammar> gramList;
        List<ActiveServiceKnowledge> knowledgeList;
        if (template == null) {
            pronList = new ArrayList<>();
            pronList.add(new ActiveServicePronunciation());
            pronList.add(new ActiveServicePronunciation());
            gramList = new ArrayList<>();
            gramList.add(new ActiveServiceGrammar());
            gramList.add(new ActiveServiceGrammar());
            knowledgeList = ActiveServiceKnowledge.build();
        } else {
            pronList = template.getPronunciationList();
            gramList = template.getGrammarList();
            knowledgeList = template.getKnowledgeList();
        }
        message.add("pronList", pronList);
        message.add("gramList", gramList);
        message.add("knowledgeList", knowledgeList);
        message.add("summary", template == null || StringUtils.isBlank(template.getLearnSummary()) ? "" : template.getLearnSummary());
        message.add("defaultSummary", template == null || StringUtils.isBlank(template.getDefaultSummary()) ? "" : template.getDefaultSummary());
        message.add("qid", qid);
        message.add("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneData::getCustomName).orElse(""));
        return message;
    }

    @RequestMapping(value = "/commonTemplateSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage commonTemplateSave() {
        String qid = getRequestString("qid");
        String pronJson = getRequestString("pronJson");
        List<ActiveServicePronunciation> pronList = JsonUtils.fromJsonToList(pronJson, ActiveServicePronunciation.class);
        String gramJson = getRequestString("gramJson");
        List<ActiveServiceGrammar> gramList = JsonUtils.fromJsonToList(gramJson, ActiveServiceGrammar.class);
        String knowledgeJson = getRequestString("knowledgeJson");
        List<ActiveServiceKnowledge> knowledgeList = JsonUtils.fromJsonToList(knowledgeJson, ActiveServiceKnowledge.class);
        List<ActiveServiceKnowledge> knowList = new ArrayList<>();
        for (ActiveServiceKnowledge knowledge : knowledgeList) {
            if (StringUtils.isNotBlank(knowledge.getValue())) {
                knowList.add(knowledge);
            }
        }
        String summary = getRequestString("summary");
        String defaultSummary = getRequestString("defaultSummary");
        ActiveServiceTemplate template = new ActiveServiceTemplate();
        template.setId(qid);
        template.setGrammarList(gramList);
        template.setKnowledgeList(knowList);
        template.setPronunciationList(pronList);
        template.setLearnSummary(summary);
        template.setDefaultSummary(defaultSummary);
        MapMessage message = chipsActiveService.saveActiveServiceTemplate(template);
        return message;
    }

    @RequestMapping(value = "/userTemplateIndex.vpage")
    public String userTemplateIndex(Model model) {
        String questionName = getRequestString("questionName");
        model.addAttribute("questionName", questionName);
        model.addAttribute("lessonId", getRequestString("lessonId"));
        model.addAttribute("unitId", getRequestString("unitId"));
        model.addAttribute("aid", getRequestString("aid"));
        model.addAttribute("bookId", getRequestString("bookId"));
        return "ailesson/activeServiceUserAddV2";
    }


    @RequestMapping(value = "/queryUserTemplate.vpage")
    @ResponseBody
    public MapMessage queryUserAnswer() {
        long userId = getRequestLong("userId");
        String qid = getRequestString("qid");
        String aids = getRequestString("aids");
        String[] split = aids.split(",");
        String aid = getRequestString("aid");

//        ActiveServiceUserTemplate userTemplate = chipsActiveService.buildActiveServiceUserTemplate(userId, qid, Arrays.asList(split));
//        MapMessage message = MapMessage.successMessage();
//        message.add("userTemplate", userTemplate);
        MapMessage message = chipsActiveService.buildActiveServiceUserTemplateMapMessage(userId, qid, Arrays.asList(split), aid);
        AIActiveServiceUserTemplateItem item = handleUserAnswer(getRequestString("unitId"), getRequestString("lessonId"), qid, aid);
        if (item != null) {
            message.add("userAnswer", item);
        }
        return message;
    }

    /**
     * 主动服务用户模板中的"用户回答"数据
     */
    private AIActiveServiceUserTemplateItem handleUserAnswer(String unitId, String lessonId, String qid, String aid) {
        if (StringUtils.isBlank(aid) || StringUtils.isBlank(unitId) || StringUtils.isBlank(lessonId) || StringUtils.isBlank(qid)) {
            return null;
        }
        return chipsEnglishUserLoader.buildUserAnswer(unitId, lessonId, qid, aid, "用户回答");
    }

    @RequestMapping(value = "/haveQuestionTemplate.vpage")
    @ResponseBody
    public MapMessage haveQuestionTemplate() {
        String qid = getRequestString("qid");
        if (StringUtils.isBlank(qid)) {
            return MapMessage.errorMessage("没有question id");
        }
        ActiveServiceTemplate template = chipsActiveService.loadActiveServiceTemplateById(qid);
        if (template == null) {
            return MapMessage.errorMessage("本题未添加主动服务模板,请先去添加模板:" + qid);
        }
        return MapMessage.successMessage();
    }

    /**
     * 主动服务模板数据，qid不为null时代表是根据questionId 进行查询，否则是根据lesson查询
     */
    @RequestMapping(value = "/getQuestionResult.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActiveServiceQuestionResult() {
        String qid = getRequestString("qid");
        if (StringUtils.isNotBlank(qid)) {//根据questionId查询
            return wrapper(mm -> {
                MapMessage mapMessage = buildActiveServiceQuestionTemplate(Collections.singletonList(qid));
                mm.putAll(mapMessage);
            });
        }
        return wrapper(mm -> {//根据LessonId查询
            String lessonId = getRequestString("lessonId");
            MapMessage mapMessage = findActiveServiceQuestionTemplateByLessonId(lessonId);
            mm.putAll(mapMessage);
        });
    }

    @RequestMapping(value = "/userTemplateSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userTemplateSave() {
        long userId = getRequestLong("userId");
        String qid = getRequestString("qid");
        String pronJson = getRequestString("pronJson");
        String gramJson = getRequestString("gramJson");
        String knowledgeJson = getRequestString("knowledgeJson");
        String summary = getRequestString("summary");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String aid = getRequestString("aid");
        String lessonId = getRequestString("lessonId");
        String qName = getRequestString("qname");
        chipsActiveService.saveActiveServiceUserTemplate(buildActiveServiceUserTemplate(userId, qid, summary, pronJson, gramJson, knowledgeJson)
                , bookId, unitId);
        MapMessage message = MapMessage.successMessage();
        String url = ChipsWechatShareUtil.getWechatDomain() + "/chips/center/activeServicePreviewV2.vpage?qid=" + qid + "&userId=" + userId
                + "&bookId=" + bookId + "&unitId=" + unitId + "&aid=" + aid + "&lessonId=" + lessonId + "&questionName=" + qName + "&t=" + System.currentTimeMillis();
        String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");
        if (StringUtils.isNotBlank(shortUrl)) {
            url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
        }
        return message.set("url", url);
    }

    @RequestMapping(value = "/queryOtherServiceTypeTemplate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryOtherServiceTypeTemplate() {
        String otherServiceType = getRequestString("otherServiceType");
        ChipsActiveServiceType serviceType = ChipsActiveServiceType.of(otherServiceType);
        return chipsActiveService.loadOtherServiceTypeTemplateList(serviceType);
    }

    private ActiveServiceUserTemplate buildActiveServiceUserTemplate(Long userId, String qid, String summary, String pronJson, String gramJson, String knowledgeJson) {
        ActiveServiceUserTemplate template = new ActiveServiceUserTemplate();
        template.setId(userId + "-" + qid);
        template.setUserId(userId);
        template.setQid(qid);
        template.setLearnSummary(summary);
        if (StringUtils.isNotBlank(pronJson)) {
            template.setPronList(JsonUtils.fromJsonToList(pronJson, ActiveServicePronunciation.class));
        }
        if (StringUtils.isNotBlank(gramJson)) {
            template.setGrammarList(JsonUtils.fromJsonToList(gramJson, ActiveServiceGrammar.class));
        }
        if (StringUtils.isNotBlank(knowledgeJson)) {
            List<ActiveServiceKnowledge> knowledgeList = JsonUtils.fromJsonToList(knowledgeJson, ActiveServiceKnowledge.class);
            List<ActiveServiceKnowledge> kList = new ArrayList<>();
            for (ActiveServiceKnowledge knowledge : knowledgeList) {
                if (StringUtils.isNotBlank(knowledge.getValue())) {
                    kList.add(knowledge);
                }
            }
            template.setKnowledgeList(kList);
        }
        return template;
    }

    /**
     * 查询qidList对应的主动服务模板数据
     */
    private MapMessage buildActiveServiceQuestionTemplate(List<String> qidList) {
        Map<String, StoneData> allMap = chipsEnglishContentLoader.loadQuestionStoneData(qidList);
        Map<String, ActiveServiceTemplate> templateMap = chipsActiveService.loadActiveServiceTemplateByIds(allMap.keySet());
        List<Map<String, Object>> list = new ArrayList<>();
        allMap.forEach((k, v) -> {
            if (k == null || v == null) {
                return;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", k);
            m.put("name", v.getCustomName());
            ActiveServiceTemplate template = templateMap.get(k);
            m.put("flag", template == null ? false : true);
            m.put("updateTime", template == null ? "" : DateUtils.dateToString(template.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
            list.add(m);
        });
        return MapMessage.successMessage().add("data", list);
    }

    /**
     * 查询lesson下的所有question的主动服务模板数据
     */
    private MapMessage findActiveServiceQuestionTemplateByLessonId(String lessonId) {
        List<String> qidList = Optional.ofNullable(lessonId).map(lid -> chipsEnglishContentLoader.loadLessonById(lid))
                .map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getContent_ids).orElse(null);
        if (CollectionUtils.isEmpty(qidList)) {
            return MapMessage.successMessage().add("data", Collections.emptyList());
        }
        return buildActiveServiceQuestionTemplate(qidList);
    }

    private MapMessage wrapper(Consumer<MapMessage> wrapper) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        MapMessage mm;
        try {
            mm = MapMessage.successMessage();
            wrapper.accept(mm);
        } catch (Exception e) {
            mm = MapMessage.errorMessage(e.getMessage());
            logger.error(e.getMessage());
        }
        return mm;
    }


    /**
     * 续费提醒主动服务页
     *
     * @return
     */
    @RequestMapping(value = "/otherServiceRenewUserIndex.vpage", method = RequestMethod.GET)
    public String otherServiceRenewUserIndex() {
        String renewType = getRequestString("renewType");
        if ("first".equals(renewType)) {//首次
            return "ailesson/otherServiceRenewFirstUserDetail";
        } else {//后续
            return "ailesson/otherServiceRenewFollowUpUserDetail";
        }
//        return "ailesson/otherServiceTypeAddV2";
    }

    @RequestMapping(value = "/queryOtherServiceRenewUserTemplate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryOtherServiceRenewUserTemplate() {
        long userId = getRequestLong("userId");
        long clazzId = getRequestLong("clazzId");
        String bookId = getRequestString("bookId");
        String url = ChipsWechatShareUtil.getWechatDomain() + "/chips/center/reportV2.vpage?userId=" + userId + "&book=" + bookId + "&t=" + System.currentTimeMillis();
        String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");
        if (StringUtils.isNotBlank(shortUrl)) {
            url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
        }

        return chipsActiveService.loadOtherServiceRenewUserData(userId, clazzId).set("url", url);
    }

    @RequestMapping(value = "/genRemarkVideo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage genRemarkVideo() {
        Long userId = getRequestLong("userId");
        String unitId = getRequestString("unitId");
        return chipsUserVideoService.filterRemarkVideo(userId, unitId);
    }
}
