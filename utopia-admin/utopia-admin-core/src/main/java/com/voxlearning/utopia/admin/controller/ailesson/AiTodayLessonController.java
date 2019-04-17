package com.voxlearning.utopia.admin.controller.ailesson;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.constant.UploadFileType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.SelectOption;
import com.voxlearning.utopia.admin.util.ChipsUploadOssManageUtils;
import com.voxlearning.utopia.admin.util.ChipsWechatShareUtil;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.client.AiTodayLessonClient;
import com.voxlearning.utopia.service.ai.constant.ActiveServiceQuestionTemplateType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.AIActiveServiceTemplate;
import com.voxlearning.utopia.service.ai.entity.AITodayLesson;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceQuestionTemplate;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceUserQuestionTemplate;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chips/ai/todaylesson")
public class AiTodayLessonController extends AbstractAdminSystemController {

    @Inject
    private AiTodayLessonClient aiTodayLessonClient;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @RequestMapping(value = "/index.vpage")
    public String index() {
        return "ailesson/today/index";
    }

    /**
     * 主动服务模板管理入口页面
     */
    @RequestMapping(value = "/activeServiceIndex.vpage")
    public String activeServiceIndex(Model model) {
        model.addAttribute("qid", getRequestString("qid"));
        model.addAttribute("bookId", getRequestString("bookId"));
        model.addAttribute("unitType", getRequestString("unitType"));
        model.addAttribute("lessonId", getRequestString("lessonId"));
        model.addAttribute("method", getRequestString("method"));
        model.addAttribute("unitId", getRequestString("unitId"));
        return "ailesson/today/activeServiceIndex";
    }

    /**
     * 轻运营班主任新增页面
     */
    @RequestMapping(value = "/add.vpage", method = RequestMethod.GET)
    public String add(Model model) {
        String type = getRequestString("type");
        String id = getRequestString("id");
        model.addAttribute("id", id);
        if (type.equals("official")) {
            return "ailesson/today/addOfficial";
        }
        return "ailesson/today/add";
    }


    /**
     * 轻运营班主任后台短期课详情查询
     */
    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage item() {
        String id = getRequestString("id");
        return wrapper(mm -> {

            AITodayLesson po = aiTodayLessonClient.getRemoteReference().load(id);
            if (po == null) {
                mm.setSuccess(false).setInfo("记录不存在");
            } else {
                mm.add("data", po);
            }
        });
    }

    /**
     * 轻运营班主任后台短期课详情保存
     */
    @RequestMapping(value = "/detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addItem() {
        String dataJson = getRequestString("data");
        return wrapper(mm -> {
            AITodayLesson po = JSONObject.parseObject(dataJson, AITodayLesson.class);
            mm.putAll(aiTodayLessonClient.getRemoteReference().save(po));
        });
    }

    /**
     * 根据单元id获取单元信息
     */
    private StoneUnitData getUnit(String unitId) {
        if (StringUtils.isBlank(unitId)) {
            return null;
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitId));
        if (MapUtils.isEmpty(stoneDataMap) || CollectionUtils.isEmpty(stoneDataMap.values())) {
            return null;
        }
        return stoneDataMap.values().stream().filter(e -> e != null).map(StoneUnitData::newInstance).findFirst().orElse(null);
    }

    /**
     * 获取薯条英语所有的教材
     */
    @RequestMapping(value = "/getBooks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getBooks() {
        return wrapper(mm -> {
            mm.add("data", buildProductSelectOptionList(null, false));
        });
    }

    /**
     * 获取某个教材下的所有的单元类型UnitType
     */
    @RequestMapping(value = "/getUnitTypes.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUnitTypes() {
        String bookId = getRequestString("bookId");
        return wrapper(mm -> {
            mm.add("data", chipsEnglishContentLoader.getAllChipsUnitType(bookId).stream().map(e -> new SelectOption(e.name(), e.getDesc())).collect(Collectors.toList()));
        });
    }

    /**
     * 获取教材下指定单元类型的Unit
     */
    @RequestMapping(value = "/getUnits.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUnits() {
        String bookId = getRequestString("bookId");
        String unitType = getRequestString("unitType");
        return wrapper(mm -> {
            mm.add("data", chipsEnglishContentLoader.getUnitByChipsUnitType(bookId, ChipsUnitType.safeOf(unitType)));
        });
    }

    /**
     * 获取指定教材指定单元下的所有lesson
     */
    @RequestMapping(value = "/getLessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLessons() {
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return wrapper(mm -> {
            mm.add("data", chipsEnglishContentLoader.loadLessonByUnitId(bookId, unitId));
        });
    }

    /**
     * 获取轻运营班主任后台列表数据
     */
    @RequestMapping(value = "/getResults.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getResults() {
        return wrapper(mm -> {
            String bookId = getRequestString("bookId");
            String unitId = getRequestString("unitId");
            List<Map<String, Object>> list = aiTodayLessonClient.getRemoteReference().findByBookIdUnitId(bookId, unitId);
            mm.add("data", list);
        });
    }

    /**
     *轻运营班主任后台长期课详情查询
     */
    @RequestMapping(value = "/officialDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage officialItem() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return wrapper(mm -> {
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("bookList", buildProductSelectOptionList(null, true));
                mm.add("dataList", jsonMap);
            });
        }
        return wrapper(mm -> {

            AIActiveServiceTemplate template = aiTodayLessonClient.getRemoteReference().loadAIActiveServiceTemplate(id);
            if (template == null) {
                mm.setSuccess(false).setInfo("记录不存在");
            } else {
                String json = template.getJson();
                mm.add("dataJson", JsonUtils.fromJson(json));
                Map<String, Object> dataList = new HashMap<>();
                //教材下拉框数据
                dataList.put("bookList", buildProductSelectOptionList(template.getBookId(), true));
                String bookId = template.getBookId();
                //类型下拉框数据
                dataList.put("unitTypeList", chipsEnglishContentLoader.getAllChipsUnitType(bookId).stream().map(e -> new SelectOption(e.name(), e.getDesc())).collect(Collectors.toList()));
                ChipsUnitType chipsUnitType = Optional.of(template.getUnitId()).map(e -> getUnit(e)).map(u -> u.getJsonData()).map(j -> j.getUnit_type()).orElse(null);
                //单元下拉框数据
                dataList.put("unitList", chipsEnglishContentLoader.getUnitByChipsUnitType(bookId, chipsUnitType));
                if (chipsUnitType != null) {
                    dataList.put("unitType", chipsUnitType);
                }
                mm.add("dataList", dataList);
                //属性回填使用
                Map<String, Object> dataPrimary = new HashMap<>();
                dataPrimary.put("unitId", template.getUnitId());
                dataPrimary.put("id", id);
                dataPrimary.put("bookId", template.getBookId());
                mm.add("dataPrimary", dataPrimary);
            }
        });
    }

    private String getBookName(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(bookId));
        if (MapUtils.isEmpty(stoneDataMap)) {
            return null;
        }
        return Optional.ofNullable(stoneDataMap.get(bookId)).map(StoneBookData::newInstance).filter(b -> b.getJsonData() != null).map(b -> b.getJsonData().getName()).orElse(null);
    }

    /**
     * 轻运营班主任后台长期课详情入库保存
     */
    @RequestMapping(value = "/officialDetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addOfficialItems() {
        String dataJson = getRequestString("dataJson");
        String id = getRequestString("id");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return wrapper(mm -> {
            AIActiveServiceTemplate template = new AIActiveServiceTemplate();
            if (StringUtils.isNotBlank(id.trim())) {
                template.setId(id);
            }
            if (StringUtils.isNotBlank(bookId)) {
                template.setBookId(bookId);
                template.setBookName(getBookName(bookId));
            }
            if (StringUtils.isNotBlank(unitId)) {
                template.setUnitId(unitId);
                StoneUnitData unit = getUnit(unitId);
                template.setTitle(Optional.ofNullable(unit).map(u -> u.getCustomName()).orElse(null));
            }
            template.setJson(dataJson);
            mm.putAll(aiTodayLessonClient.getRemoteReference().saveAIActiveServiceTemplate(template));
        });
    }

    /**
     * 轻运营班主任删除
     */
    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (type.equals("official")) {
            return wrapper(mm -> {
                aiTodayLessonClient.getRemoteReference().removeAIActiveServiceTemplate(id);
            });
        } else {
            return wrapper(mm -> {
                aiTodayLessonClient.getRemoteReference().remove(id);
            });
        }
    }

    private MapMessage wrapper(Consumer<MapMessage> wrapper) {

        // Auth check?
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }

        MapMessage mm = MapMessage.successMessage();
        try {
            wrapper.accept(mm);
        } catch (Exception e) {
            mm = MapMessage.errorMessage(e.getMessage());
            logger.error(e.getMessage());

        }
        return mm;
    }

    /**
     * 构建教材下拉框
     * @param filterShortBook 返回结果是否排除短期课
     * @return
     */
    public List<SelectOption> buildProductSelectOptionList(String selectedValue, boolean filterShortBook) {
        List<StoneBookData> bookList = chipsEnglishContentLoader.loadAllChipsEnglishBooks();
        if (filterShortBook) {
            bookList = bookList.stream().filter(b -> b.getId().equals(BooKConst.CHIPS_ENGLISH_BOOK_ID)).collect(Collectors.toList());
        }
        return bookList.stream().map(p -> {
            SelectOption op = new SelectOption();
//            op.setDesc(p.getJsonData() == null ? null : p.getJsonData().getName());
            op.setDesc(p.getCustomName());
            op.setValue(p.getId());
            op.setSelected(StringUtils.isBlank(selectedValue) ? false : p.getId().equals(selectedValue));
            return op;
        }).collect(Collectors.toList());
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

    /**
     * 主动服务通用模板新增/编辑入口页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/activeServiceAdd.vpage")
    public String activieServiceAdd(Model model) {
        String qid = getRequestString("qid");
        model.addAttribute("qid", qid);
        String bookId = getRequestString("bookId");
        String unitType = getRequestString("unitType");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        //页面属性回填时使用，代表是根据questionId进行查询还是根据lesson进行查询
        String method = getRequestString("method");
        if (StringUtils.isNotBlank(method)) {
            model.addAttribute("method", method);
            model.addAttribute("bookId", bookId);
            model.addAttribute("unitType", unitType);
            model.addAttribute("unitId", unitId);
            model.addAttribute("lessonId", lessonId);
        }
        return "ailesson/today/activeServiceAdd";
    }

    /**
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=44054703
     * wiki中要求页面展示的18个元素
     */
    private List<AIActiveServiceTemplateItem> build() {
        List<AIActiveServiceTemplateItem> list = new ArrayList<>();
        int i = 0;
        list.add(new AIActiveServiceTemplateItem("发音点评1", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("发音点评1音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("发音点评2", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("发音点评2音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("发音点评3", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("发音点评3音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解1", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解1音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解2", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解2音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解3", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("语法点讲解3音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站1", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站1音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站2", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站2音频", "", "url", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站3", "", "text", i++));
        list.add(new AIActiveServiceTemplateItem("知识加油站3音频", "", "url", i++));
        return list;
    }

    @RequestMapping(value = "/activeServiceSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage activeServiceSave(Model model) {
        String qid = getRequestString("qid");
        model.addAttribute("qid", qid);
        ActiveServiceQuestionTemplate template = new ActiveServiceQuestionTemplate();
        if (StringUtils.isBlank(qid)) {
            return MapMessage.errorMessage("no question id");
        }
        template.setId(qid);
        String dataJson = getRequestString("dataJson");
        template.setJson(dataJson);
        return aiTodayLessonClient.getRemoteReference().saveActiveServiceQuestionTemplate(template);
    }

    /**
     * 主动服务通用模板数据详情
     * @return
     */
    @RequestMapping(value = "/activieServiceQuery.vpage")
    @ResponseBody
    public MapMessage activieServiceQuery() {
        String qid = getRequestString("qid");
        if (StringUtils.isBlank(qid)) {
            return MapMessage.successMessage();
        }
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        ActiveServiceQuestionTemplate template = aiTodayLessonClient.getRemoteReference().loadActiveServiceQuestionTemplateById(qid);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("qid", qid);
        mapMessage.add("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneData::getCustomName).orElse(""));
        if (template == null || StringUtils.isBlank(template.getJson())) {
            mapMessage.add("itemList", build());
        } else {
            mapMessage.add("itemList", parseFromJson(template.getJson()));
        }
        return mapMessage;
    }

    /**
     * 解析通用服务模板json
     */
    private List<AIActiveServiceTemplateItem> parseFromJson(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        List<AIActiveServiceTemplateItem> result = new ArrayList<>();
        List list = JsonUtils.fromJson(json, List.class);
        if (list == null) {
            return Collections.emptyList();
        }
        for (Object obj : list) {
            Map<String, Object> map = (Map<String, Object>) obj;
            AIActiveServiceTemplateItem item = new AIActiveServiceTemplateItem((String) map.get("name"), (String) map.get("value"), (String) map.get("type"), (Integer) map.get("index"));
            result.add(item);
        }
        return result;
    }

    /**
     * 解析主动服务用户模板json
     * @param fromGeneral 为true时表示 json是通用模板中取的，false表示从用户模板中取的
     * @return
     */
    private List<AIActiveServiceUserTemplateItem> parseAIActiveServiceUserTemplateItemFromJson(String json, boolean fromGeneral) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        List<AIActiveServiceUserTemplateItem> result = new ArrayList<>();
        List list = JsonUtils.fromJson(json, List.class);
        if (list == null) {
            return Collections.emptyList();
        }
        for (Object obj : list) {
            Map<String, Object> map = (Map<String, Object>) obj;
            if(fromGeneral){
                if (StringUtils.isNotBlank((String) map.get("value"))) {
                    AIActiveServiceUserTemplateItem item = new AIActiveServiceUserTemplateItem((String) map.get("name"),
                            (String) map.get("value"), (String) map.get("type"), (Integer) map.get("index"),
                            true);
                    result.add(item);
                }
            } else {
                AIActiveServiceUserTemplateItem item = new AIActiveServiceUserTemplateItem((String) map.get("name"),
                        (String) map.get("value"), (String) map.get("type"), (Integer) map.get("index"),
                        (Boolean) map.get("checkBox"));
                result.add(item);
            }
        }
        return result;
    }

    /**
     *  该question是否已经添加主动服务模板
     */
    @RequestMapping(value = "/haveQuestionTemplate.vpage")
    @ResponseBody
    public MapMessage haveQuestionTemplate() {
        String qid = getRequestString("qid");
        if (StringUtils.isBlank(qid)) {
            return MapMessage.errorMessage("没有question id");
        }
        ActiveServiceQuestionTemplate template = aiTodayLessonClient.getRemoteReference().loadActiveServiceQuestionTemplateById(qid);
        if (template == null) {
            return MapMessage.errorMessage("本题未添加主动服务模板,请先去添加模板");
        }
        return MapMessage.successMessage();
    }

    /**
     * 主动服务用户模板入口页面
     */
    @RequestMapping(value = "/userTemplateIndex.vpage")
    public String userQuestionTemplateIndex(Model model) {
        String userId = getRequestString("userId");
        String qid = getRequestString("qid");
        String aid = getRequestString("aid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        model.addAttribute("aid", aid);
        model.addAttribute("bookId", getRequestString("bookId"));
        model.addAttribute("unitId", getRequestString("unitId"));
        model.addAttribute("lessonId", getRequestString("lessonId"));

        return "ailesson/today/activeServiceUserAdd";
    }

    /**
     * 获取主动服务用户模板数据
     */
    @RequestMapping(value = "/userTemplateQuery.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userQuestionTemplateQuery(Model model) {
        Long userId = getRequestLong("userId");
        String qid = getRequestString("qid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        ActiveServiceUserQuestionTemplate userTemplate = aiTodayLessonClient.getRemoteReference().queryActiveServiceUserQuestionTemplateByUserIdQid(userId, qid);
        MapMessage message = MapMessage.successMessage();
        Map<String, StoneData> qMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        message.add("name", Optional.ofNullable(qMap).map(m -> m.get(qid)).map(StoneData::getCustomName).orElse(""));
        List<AIActiveServiceUserTemplateItem> itemList;
        if (userTemplate == null) {
            ActiveServiceQuestionTemplate template = aiTodayLessonClient.getRemoteReference().loadActiveServiceQuestionTemplateById(qid);
            itemList = parseAIActiveServiceUserTemplateItemFromJson(template.getJson(), true);
        } else {
            itemList = parseAIActiveServiceUserTemplateItemFromJson(userTemplate.getJson(), false);
        }
        message.add("itemList", itemList);

        AIActiveServiceUserTemplateItem item = handleUserAnswer(getRequestString("unitId"), getRequestString("lessonId"), qid, getRequestString("aid"));
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
        return chipsEnglishUserLoader.buildUserAnswer(unitId, lessonId, qid, aid,"用户回答");
    }

    /**
     * 删除主动服务用户模板
     */
    @RequestMapping(value = "/userTemplateDelete.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage userQuestionTemplateDelete(Model model) {
        String userId = getRequestString("userId");
        String qid = getRequestString("qid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        aiTodayLessonClient.getRemoteReference().deleteActiveServiceUserQuestionTemplate(userId, qid);
        MapMessage message = MapMessage.successMessage();
        return message;
    }
    /**
     * 主动服务用户模板入库保存
     */
    @RequestMapping(value = "/userTemplateSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage userQuestionTemplateSave(Model model) {
        String userId = getRequestString("userId");
        String qid = getRequestString("qid");
        model.addAttribute("userId", userId);
        model.addAttribute("qid", qid);
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        String aid = getRequestString("aid");
        MapMessage message = MapMessage.successMessage();
        String dataJson = getRequestString("dataJson");
        List<AIActiveServiceUserTemplateItem> jsonList = parseAIActiveServiceUserTemplateItemFromJson(dataJson, false);
        jsonList.forEach(item -> {
            if (!item.isCheckBox()) {
                item.setValue(null);
            }
        });

        String url = ChipsWechatShareUtil.getWechatDomain() + "/chips/center/activeServicePreview.vpage?qid=" + qid + "&userId=" + userId
                + "&bookId=" + bookId + "&unitId=" + unitId + "&aid=" + aid + "&lessonId=" + lessonId + "&t=" + System.currentTimeMillis();
        String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");

        if (StringUtils.isNotBlank(shortUrl)) {
            url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
        }

        ActiveServiceUserQuestionTemplate userQuestionTemplate = new ActiveServiceUserQuestionTemplate();
        userQuestionTemplate.setId(userId + "-" + qid);
        userQuestionTemplate.setUserId(userId);
        userQuestionTemplate.setQid(qid);
        userQuestionTemplate.setTemplateType(ActiveServiceQuestionTemplateType.normal);
        userQuestionTemplate.setJson(JsonUtils.toJson(jsonList));
        userQuestionTemplate.setBookId(bookId);
        userQuestionTemplate.setUnitId(unitId);
        aiTodayLessonClient.getRemoteReference().saveActiveServiceUserQuestionTemplate(userQuestionTemplate);
        return message.set("url", url);
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

    /**
     * 查询qidList对应的主动服务模板数据
     */
    private MapMessage buildActiveServiceQuestionTemplate(List<String> qidList) {
        Map<String, StoneData> allMap = chipsEnglishContentLoader.loadQuestionStoneData(qidList);
        Map<String, ActiveServiceQuestionTemplate> templateMap = aiTodayLessonClient.getRemoteReference().loadActiveServiceQuestionTemplateByIds(allMap.keySet());
        List<Map<String, Object>> list = new ArrayList<>();
        allMap.forEach((k, v) -> {
            if (k == null || v == null) {
                return;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", k);
            m.put("name", v.getCustomName());
            ActiveServiceQuestionTemplate template = templateMap.get(k);
            m.put("flag", template == null ? false : true);
            m.put("updateTime", template == null ? "" : DateUtils.dateToString(template.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
            list.add(m);
        });
        return MapMessage.successMessage().add("data", list);
    }

    @RequestMapping(value = "getSignature.vpage")
    @ResponseBody
    public MapMessage getSignature() {
        String ext = getRequestString("ext");
        UploadFileType uploadFileType;
        if (ext != null) {
            uploadFileType = UploadFileType.of(ext);
            if (uploadFileType.equals(UploadFileType.unsupported)) {
                return MapMessage.errorMessage("不支持的数据类型");
            }
        } else {
            uploadFileType = UploadFileType.unsupported;
        }

        MapMessage signatureResult = ChipsUploadOssManageUtils.getSignature(uploadFileType, "chips", getResponse());
        if (signatureResult != null) {
            return MapMessage.successMessage().add("data", signatureResult);
        }
        return MapMessage.errorMessage();
    }

}
