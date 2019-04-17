package com.voxlearning.utopia.admin.controller.ailesson;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.admin.util.ChipsWechatShareUtil;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.api.ChipsUserVideoLoader;
import com.voxlearning.utopia.service.ai.client.AiServiceClient;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chips/user/video")
public class AiUserVideoController extends AbstractAdminSystemController {

    public static Set<String> tryOutUnit = new HashSet<>();

    static {
        tryOutUnit.add("SD_10300001152317");//(单元 试用单元)
        tryOutUnit.add("SD_10300001569109");//(单元 入门版试用单元)
    }

    @Inject
    private AiServiceClient aiServiceClient;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsUserVideoLoader.class)
    private ChipsUserVideoLoader chipsUserVideoLoader;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @RequestMapping(value = "examine/list.vpage", method = RequestMethod.GET)
    public String fetchExaminList(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<AIUserVideo> res;
        Long userId = getRequestLong("userId", 0L);
        String userVideoId = getRequestString("userVideoId");
        String userName = getRequestString("userName");
        String book = getRequestParameter("book", "");
        String unit = getRequestString("unit");
        String status = getRequestParameter("examineStatus", "");
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        boolean showTip = getRequestBool("showTip");
        Date start = StringUtils.isNotBlank(startDate) ? DateUtils.stringToDate(startDate, "yyyy-MM-dd") : null;
        Date end = StringUtils.isNotBlank(endDate) ? DateUtils.stringToDate(endDate, "yyyy-MM-dd") : null;
        String tab = getRequestString("tab");
        if (StringUtils.isBlank(tab)) {
            tab = "1";
        }
        List<StoneUnitData> stoneUnitDataList = new ArrayList<>();
        boolean filterMatch = true;
        if (StringUtils.isNotBlank(book)) {
            stoneUnitDataList = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(book)))
                    .map(e -> e.get(book))
                    .map(StoneBookData::newInstance)
                    .map(StoneBookData::getJsonData)
                    .map(StoneBookData.Book::getChildren)
                    .map(e -> e.stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toList()))
                    .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(e))
                    .map(e -> e.values().stream().map(StoneUnitData::newInstance).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }
        if (StringUtils.isBlank(userVideoId) && Long.compare(userId, 0L) <= 0 && StringUtils.isNoneBlank(unit, status)) { //单元，status
            AIUserVideo.ExamineStatus examineStatus = AIUserVideo.ExamineStatus.safeOf(status);
            res = chipsUserVideoLoader.loadByUnitId(unit, examineStatus);
            res = filterByTab(res, tab).stream()
                    .filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
                    .filter(e -> start == null || (e.getCreateTime() != null && e.getCreateTime().after(start)))
                    .filter(e -> end == null || (e.getCreateTime() != null && e.getCreateTime().before(end)))
                    .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                    .collect(Collectors.toList());
        } else if (Long.compare(userId, 0L) > 0) { //用户id
            res = chipsUserVideoLoader.loadByUserId(userId);
            res = filterByTab(res, tab).parallelStream()
                    .filter(e -> StringUtils.isBlank(userVideoId) || userVideoId.equals(e.getId()))
                    .filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
                    .filter(e -> StringUtils.isBlank(book) || book.equals(e.getBookId()))
                    .filter(e -> StringUtils.isBlank(unit) || unit.equals(e.getUnitId()))
                    .filter(e -> StringUtils.isBlank(status) || status.equals(e.getStatus().name()))
                    .filter(e -> start == null || (e.getCreateTime() != null && e.getCreateTime().after(start)))
                    .filter(e -> end == null || (e.getCreateTime() != null && e.getCreateTime().before(end)))
                    .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                    .collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(userVideoId)) { //视频id
            AIUserVideo aiUserVideo = chipsUserVideoLoader.loadById(userVideoId);
            res = aiUserVideo != null ? filterByTab(Arrays.asList(aiUserVideo), tab).stream().filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
                    .filter(e -> StringUtils.isBlank(book) || book.equals(e.getBookId()))
                    .filter(e -> StringUtils.isBlank(unit) || unit.equals(e.getUnitId()))
                    .filter(e -> StringUtils.isBlank(status) || status.equals(e.getStatus().name()))
                    .filter(e -> start == null || (e.getCreateTime() != null && e.getCreateTime().after(start)))
                    .filter(e -> end == null || (e.getCreateTime() != null && e.getCreateTime().before(end)))
                    .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                    .collect(Collectors.toList()) : Collections.emptyList();
        } else if (start != null && end != null && DateUtils.dayDiff(end, start) > 0 && DateUtils.dayDiff(end, start) < 8) {
            String bookType = getRequestString("bookType");
            Set<String> bookSet;
            if (StringUtils.isBlank(book)) {
                if ("long".equals(bookType)) {
                    bookSet = fetchBooksByGrade(false).stream().map(m -> (String) m.get("id")).collect(Collectors.toSet());
                } else if ("short".equals(bookType)) {
                    bookSet = fetchBooksByGrade(true).stream().map(m -> (String) m.get("id")).collect(Collectors.toSet());
                } else if ("all".equals(bookType)) {
                    bookSet = fetchBooks(true).stream().map(m -> (String) m.get("id")).collect(Collectors.toSet());
                } else {
                    bookSet = Collections.emptySet();
                }
            } else {
                bookSet = Collections.singleton(book);
            }
            res = filterByTab(chipsUserVideoLoader.loadByDateRange(start, end), tab).stream()
                    .filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
//                    .filter(e -> StringUtils.isBlank(book) || book.equals(e.getBookId()))
                    .filter(e -> CollectionUtils.isEmpty(bookSet) || bookSet.contains(e.getBookId()))
                    .filter(e -> StringUtils.isBlank(unit) || unit.equals(e.getUnitId()))
                    .filter(e -> StringUtils.isBlank(status) || status.equals(e.getStatus().name()))
                    .collect(Collectors.toList());
        } else {
            res = Collections.emptyList();
            filterMatch = !showTip;
        }
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 10);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(converUserVideoList(res), pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", res != null ? res.size() : 0);
        model.addAttribute("userVideoId", userVideoId);
        model.addAttribute("userName", userName);
        model.addAttribute("book", book);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("filterMatch", filterMatch);
        model.addAttribute("showTip", showTip);
        model.addAttribute("userId", userId != null && !userId.equals(0L) ? userId : 0);
        String bookType = getRequestString("bookType");
        if (StringUtils.isNotBlank(bookType) && ("long".equals(bookType) || "short".equals(bookType))) {
            model.addAttribute("books", fetchBooksByGrade("short".equalsIgnoreCase(bookType)));
        } else {
            model.addAttribute("books", fetchBooks(true));
        }
        model.addAttribute("unit", unit);
        model.addAttribute("units", convertStoneUnit(stoneUnitDataList, true));
        model.addAttribute("categoryList", AIUserVideo.Category.values());
        model.addAttribute("examineStatus", status);
        model.addAttribute("examineStatusList", AIUserVideo.ExamineStatus.values());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("bookType", bookType);
        model.addAttribute("tab", tab);
        return "ailesson/examinevideo_indexV2";
    }

    private List<AIUserVideo> filterByTab(List<AIUserVideo> videoList, String tab) {
        if (CollectionUtils.isEmpty(videoList)) {
            return Collections.emptyList();
        }
        if ("1".equals(tab)) {
            return videoList.stream().filter(e -> e.getForShare() != null && e.getForShare()).collect(Collectors.toList());
        }
        if ("2".equals(tab)) {
            return videoList.stream().filter(e -> e.getForRemark() != null && e.getForRemark()).collect(Collectors.toList());
        }
        if ("3".equals(tab)) {//每个用户 每个单元或者lesson最新的；由于后续宋涛入的数据是按照每个unit存储，不细化到lesson丽都，此处取每个用户每个unit的最新数据
            Map<String, List<AIUserVideo>> userUnitMap = videoList.stream().filter(e -> StringUtils.isNotBlank(e.getUnitId()) && !tryOutUnit.contains(e.getUnitId())).collect(Collectors.groupingBy(e -> e.getUserId() + "-" + e.getUnitId()));
            List<AIUserVideo> result = new ArrayList<>();
            for (List<AIUserVideo> userUnitList : userUnitMap.values()) {
                userUnitList.sort(Comparator.comparing(AIUserVideo::getUpdateTime).reversed());
                if (CollectionUtils.isNotEmpty(userUnitList)) {
                    result.add(userUnitList.get(0));
                }
            }
            return result;
        }
        if ("4".equals(tab)) {
            List<AIUserVideo> tryOutVideoList = videoList.stream().filter(e -> StringUtils.isNotBlank(e.getUnitId()) && tryOutUnit.contains(e.getUnitId())).collect(Collectors.toList());
            List<AIUserVideo> exceptTryOutVideoList = videoList.stream().filter(e -> StringUtils.isNotBlank(e.getUnitId()) && !tryOutUnit.contains(e.getUnitId())).collect(Collectors.toList());
            List<AIUserVideo> notShareNotRemarkList = exceptTryOutVideoList.stream()
                    .filter(e -> (e.getForRemark() != null && !e.getForRemark()) && (e.getForShare() == null || !e.getForShare()))
                    .collect(Collectors.toList());
            Map<String, List<AIUserVideo>> userUnitMap = notShareNotRemarkList.stream().collect(Collectors.groupingBy(e -> e.getUserId() + "-" + e.getUnitId()));
            List<AIUserVideo> result = new ArrayList<>();
            for (List<AIUserVideo> userUnitList : userUnitMap.values()) {
                userUnitList.sort(Comparator.comparing(AIUserVideo::getUpdateTime).reversed());
                if (CollectionUtils.isNotEmpty(userUnitList)) {
                    for (int i = 1; i < userUnitList.size(); i++) {
                        result.add(userUnitList.get(i));
                    }
                }
            }
            result.addAll(tryOutVideoList);
            return result;
        }
        return videoList;
    }

    private MapMessage fetchBooksByType(String bookType) {
        MapMessage message = MapMessage.successMessage();
        if (bookType.equals("all")) {
            message.add("books", fetchBooks(true));
        } else if (bookType.equals("long")) {
            message.add("books", fetchBooksByGrade(false));
        } else if (bookType.equals("short")) {
            message.add("books", fetchBooksByGrade(true));
        }
        return message;
    }


    private List<Map<String, Object>> fetchBooksByGrade(boolean isShort) {

        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.ChipsEnglish)
                .stream().filter(e -> Boolean.FALSE.equals(e.getDisabled()) && StringUtils.isNotBlank(e.getAppItemId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(itemList)) {
            return Collections.emptyList();
        }
        List<String> productIdList = productList.stream().map(OrderProduct::getId).collect(Collectors.toList());
        Map<String, List<OrderProductItem>> productToItemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIdList);
        Map<String, Boolean> bookShortMap = new HashMap<>();
        for (OrderProduct product : productList) {
            List<OrderProductItem> itemTempList = productToItemMap.get(product.getId());
            List<String> bookList = itemTempList.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
            if (isShortProduct(product)) {//短期课
                for (String book : bookList) {
                    bookShortMap.put(book, true);
                }
            } else {
                for (String book : bookList) {
                    bookShortMap.put(book, false);
                }
            }
        }

        LinkedList<Map<String, Object>> list = new LinkedList<>();
        itemList.forEach(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getAppItemId());
            map.put("name", item.getName());
            Boolean flag = bookShortMap.get(item.getAppItemId());
            if (flag == null) {
                return;
            }
            if (isShort == flag) {
                list.add(map);
            }
        });
        Map<String, Object> map = new HashMap<>();
        map.put("id", "");
        map.put("name", "全部");
        list.addFirst(map);
        return list;
    }


    @RequestMapping(value = "examine/bookUnit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fetchBookUnitList(Model model) {
        String bookType = getRequestString("bookType");
        if (bookType.equals("all") || bookType.equals("long") || bookType.equals("short")) {
            return fetchBooksByType(bookType);
        }
        String bookId = getRequestString("bookId");
        boolean all = getRequestBool("all");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.successMessage().add("unitList", new ArrayList<>());
        }
        List<StoneUnitData> unitList = fetchStoneUnitDataList(bookId);
        if (CollectionUtils.isEmpty(unitList)) {
            return MapMessage.successMessage().add("unitList", new ArrayList<>());
        }
        List<Map<String, Object>> units = new ArrayList<>();
        if (all) {
            Map<String, Object> allMap = new HashMap<>();
            allMap.put("id", "");
            allMap.put("name", "全部");
            units.add(allMap);
        }
        unitList.forEach(unit -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", unit.getId());
            map.put("name", Optional.ofNullable(unit.getJsonData()).map(u -> u.getName()).orElse(""));
            units.add(map);
        });
        return MapMessage.successMessage().add("unitList", units);
    }

    private List<Map<String, Object>> convertStoneUnit(List<StoneUnitData> unitList, boolean includeAll) {
        if (CollectionUtils.isEmpty(unitList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> units = new ArrayList<>();
        if (includeAll) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "");
            map.put("name", "全部");
            units.add(map);
        }
        unitList.forEach(unit -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", unit.getId());
            map.put("name", Optional.ofNullable(unit.getJsonData()).map(u -> u.getName()).orElse(""));
            units.add(map);
        });
        return units;
    }

    private List<StoneUnitData> fetchStoneUnitDataList(String bookId) {
        StoneData bookStoneData = Optional.ofNullable(bookId).map(e -> stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(e)))
                .map(m -> m.get(bookId)).orElse(null);
        List<StoneBookData.Node> children = Optional.ofNullable(bookStoneData).map(e -> StoneBookData.newInstance(e))
                .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                .map(e -> e.getJsonData().getChildren()).orElse(null);
        if (CollectionUtils.isEmpty(children)) {
            return Collections.emptyList();
        }
        Set<String> unitIdSet = children.stream().map(e -> e.getStone_data_id()).collect(Collectors.toSet());
        Map<String, StoneData> unitDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(unitIdSet);
        if (MapUtils.isEmpty(unitDataMap)) {
            return Collections.emptyList();
        }
        return children.stream().filter(e -> e != null && StringUtils.isNotBlank(e.getStone_data_id()) && !StringUtils.equals(e.getStone_data_id(), BooKConst.TRAVEL_ENGLISH_TRIAL_UNIT) && unitDataMap.get(e.getStone_data_id()) != null)
                .map(e -> StoneUnitData.newInstance(unitDataMap.get(e.getStone_data_id()))).collect(Collectors.toList());
    }

    private List<Map<String, Object>> fetchBooks(boolean includeAll) {
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.ChipsEnglish);
        if (CollectionUtils.isEmpty(itemList)) {
            return new ArrayList<>();
        }
        LinkedList<Map<String, Object>> list = new LinkedList<>();
        itemList.forEach(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getAppItemId());
            map.put("name", item.getName());
            list.add(map);
        });
        if (includeAll) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "");
            map.put("name", "全部");
            list.addFirst(map);
        }
        return list;
    }

    private boolean isShortProduct(OrderProduct product) {
        if (product == null || StringUtils.isBlank(product.getAttributes())) {
            return false;
        }
        Map<String, Object> map = JsonUtils.fromJson(product.getAttributes());
        if (MapUtils.isEmpty(map)) {
            return false;
        }
        return Boolean.TRUE.equals(SafeConverter.toBoolean(map.get("short")));
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage worksDetail() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("no login");
        }

        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("paramter is null");
        }
        AIUserVideo aiUserVideo = chipsUserVideoLoader.loadById(id);
        if (aiUserVideo == null) {
            return MapMessage.errorMessage("no data");
        }
        Map<String, Object> map = JsonUtils.safeConvertObjectToMap(aiUserVideo);
        map.put("statusName", aiUserVideo.getStatus().getDescription());
        return MapMessage.successMessage().add("data", map);
    }

    @RequestMapping(value = "examine.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage examinWorks() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("没有登录");
        }
        String id = getRequestString("id");
        String status = getRequestString("status");
        String description = getRequestString("description");
        String category = getRequestString("category");
        String fromStatus = getRequestString("formStatus");
        if (StringUtils.isAnyBlank(id, status)) {
            return MapMessage.errorMessage("参数异常");
        }
        AIUserVideo.ExamineStatus examineStatus = AIUserVideo.ExamineStatus.safeOf(status);
        AIUserVideo.ExamineStatus fromExamineStatus = AIUserVideo.ExamineStatus.safeOf(fromStatus);
        if (status == null) {
            return MapMessage.errorMessage("参数异常");
        }
        AIUserVideo.Category cat = AIUserVideo.Category.safeOf(category);
        return aiServiceClient.examineUserVideo(id, fromExamineStatus, examineStatus, adminUser.getAdminUserName(), examineStatus == AIUserVideo.ExamineStatus.Passed ? (cat == null ? AIUserVideo.Category.Common : cat) : null, description);
    }

    @RequestMapping(value = "comment/list.vpage", method = RequestMethod.GET)
    public String fetchCommentWorksList(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<AIUserVideo> res;
        String userVideoId = getRequestString("userVideoId");
        String[] labels = getRequest().getParameterValues("label");
        List<AIUserVideo.Label> labelList = new ArrayList<>();
        if (labels != null && labels.length > 1) {
            for (String s : labels) {
                AIUserVideo.Label label = AIUserVideo.Label.safeOf(s);
                if (label != null) {
                    labelList.add(label);
                }
            }
        }
        List<ChipsEnglishClass> allClazz = chipsEnglishClazzService.selectAllChipsEnglishClass();
        Long userId = getRequestLong("userId");
        Long clazz = getRequestLong("clazz");
        String book = getRequestParameter("book", "BK_10300003451674");
        String category = getRequestParameter("category", "");
        String comment = getRequestParameter("comment", "");
        String userName = getRequestString("userName");
        String unit = getRequestString("unit");
        int hasComment = getRequestInt("hasComment", 0);
        ;
        List<StoneUnitData> unitDataList = fetchStoneUnitDataList(book);
        if (StringUtils.isBlank(userVideoId) && Long.compare(userId, 0L) <= 0) {
            if (StringUtils.isBlank(unit)) {
                unit = unitDataList.stream().findFirst().map(StoneUnitData::getId).orElse("");
            }
            Set<Long> clazzUsers = allClazz.stream()
                    .filter(e -> clazz != null && clazz.equals(e.getId()))
                    .findFirst()
                    .map(e -> chipsEnglishClazzService.selectAllUserByClazzId(e.getId()).stream().collect(Collectors.toSet()))
                    .orElse(Collections.emptySet());
            res = chipsUserVideoLoader.loadByUnitId(unit, AIUserVideo.ExamineStatus.Passed).stream()
                    .filter(e -> userId == null || userId.equals(0L) || e.getUserId().equals(userId))
                    .filter(e -> AIUserVideo.Category.safeOf(category) == null || AIUserVideo.Category.safeOf(category) == e.getCategory())
                    .filter(e -> CollectionUtils.isEmpty(clazzUsers) || clazzUsers.contains(e.getUserId()))
                    .filter(e -> StringUtils.isBlank(comment) || (StringUtils.isNotBlank(e.getComment()) && e.getComment().contains(comment)))
                    .filter(e -> {
                        if (CollectionUtils.isEmpty(labelList)) {
                            return true;
                        }
                        if (CollectionUtils.isEmpty(e.getLabels())) {
                            return false;
                        }
                        for (AIUserVideo.Label label : labelList) {
                            if (labelList.contains(label)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
                    .filter(e -> hasComment == 1 ? StringUtils.isNotBlank(e.getComment()) : hasComment == 2 ? StringUtils.isBlank(e.getComment()) : true)
                    .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                    .collect(Collectors.toList());
        } else if (StringUtils.isBlank(userVideoId) && Long.compare(userId, 0L) > 0) {
            res = chipsUserVideoLoader.loadByUserId(userId).stream()
                    .filter(e -> e.getStatus() == AIUserVideo.ExamineStatus.Passed)
                    .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                    .collect(Collectors.toList());
        } else {
            AIUserVideo aiUserVideo = chipsUserVideoLoader.loadById(userVideoId);
            res = aiUserVideo != null ? Arrays.asList(aiUserVideo) : Collections.emptyList();
        }

        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(converUserVideoList(res), pageable);
        model.addAttribute("hasComment", hasComment);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", res != null ? res.size() : 0);
        model.addAttribute("userVideoId", userVideoId);
        model.addAttribute("userName", userName);
        model.addAttribute("comment", comment);
        model.addAttribute("book", book);
        model.addAttribute("userId", userId != null && !userId.equals(0L) ? userId : 0);
        model.addAttribute("books", fetchBooks(false));
        model.addAttribute("unit", unit);
        model.addAttribute("clazz", clazz);
        model.addAttribute("clazzList", allClazz);
        model.addAttribute("category", category);
        model.addAttribute("units", convertStoneUnit(unitDataList, false));
        model.addAttribute("categoryList", AIUserVideo.Category.values());
        model.addAttribute("selectedLabels", labelList);
        model.addAttribute("labels", AIUserVideo.Label.values());
        model.addAttribute("pageNumber", pageNumber);

        return "ailesson/commentvideo_index";
    }

    @RequestMapping(value = "comment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage commentWorks(@RequestParam(value = "labels", required = false) String[] labels) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("没有登录");
        }
        String id = getRequestString("id");
        String comment = getRequestString("comment");
        String commentAudio = getRequestString("commentAudio");
        String category = getRequestString("category");

        if (StringUtils.isAnyBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        List<AIUserVideo.Label> labelList = new ArrayList<>();

        if (labels != null && labels.length > 0) {
            for (String s : labels) {
                AIUserVideo.Label label = AIUserVideo.Label.safeOf(s);
                if (label != null) {
                    labelList.add(label);
                }
            }
        }
        aiServiceClient.updateUserVideoComment(id, adminUser.getAdminUserName(), comment, commentAudio, labelList, AIUserVideo.Category.safeOf(category));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "comment/audioupload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage commentUploadAudio() {
        MapMessage mapMessage = MapMessage.successMessage();
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = multipartRequest.getFile("file");
            if (file.isEmpty()) {
                mapMessage.setSuccess(false);
                mapMessage.setInfo("没有文件上传");
            } else {
                try {
                    String filename = file.getOriginalFilename();
                    String path = AdminOssManageUtils.upload(file, "chips/comment/audio");
                    mapMessage.add("url", path)
                            .add("filename", filename);
                    mapMessage.setSuccess(true);
                } catch (Exception e) {
                    mapMessage.setSuccess(false);
                    mapMessage.setInfo("文件上传异常");
                }
            }
        } catch (Exception e) {
            mapMessage.setSuccess(false);
            mapMessage.setInfo("文件上传异常");
        }

        return mapMessage;
    }

    private List<Map<String, Object>> convertUnit(List<NewBookCatalog> unitList) {
        if (CollectionUtils.isEmpty(unitList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> units = new ArrayList<>();
        unitList.forEach(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("name", Optional.ofNullable(o)
                    .filter(e -> MapUtils.isNotEmpty(e.getExtras()))
                    .filter(e -> e.getExtras().get("ai_teacher") != null)
                    .map(e -> JsonUtils.fromJson(SafeConverter.toString(e.getExtras().get("ai_teacher"))))
                    .filter(e -> MapUtils.isNotEmpty(e) && e.get("pageSubTitle") != null)
                    .map(e -> SafeConverter.toString(e.get("pageSubTitle")))
                    .orElse(""));
            units.add(map);
        });
        return units;
    }

    private List<Map<String, Object>> converUserVideoList(List<AIUserVideo> res) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(res)) {
            res.sort(Comparator.comparing(AIUserVideo::getCreateTime));
            res.forEach(e -> {
                Map<String, Object> bean = new HashMap<>();
                bean.put("id", e.getId());
                bean.put("user", e.getUserName() + "(" + e.getUserId() + ")");
                bean.put("lesson", e.getLessonName());
                bean.put("commented", StringUtils.isNotEmpty(e.getComment()));
                bean.put("status", e.getStatus() != null ? e.getStatus().getDescription() : "");
                bean.put("category", e.getCategory() != null ? e.getCategory().getDescription() : "");
                bean.put("categoryName", e.getCategory());
                bean.put("statusName", e.getStatus());
                bean.put("url", ChipsWechatShareUtil.shareVideoUrl(e.getId()));
                bean.put("createDate", DateUtils.dateToString(e.getCreateTime()));
                List<String> videos = e.fetchUserVideos();
                if (CollectionUtils.isEmpty(videos)) {
                    videos = Collections.singletonList(e.getVideo());
                }
                bean.put("video", videos.get(0));
                bean.put("videos", StringUtils.join(videos, ","));
                list.add(bean);
            });
        }
        return list;
    }


    @RequestMapping(value = "export.vpage", method = RequestMethod.GET)
    public void exportData() {
        Long userId = getRequestLong("user");
        List<AIUserVideo> res = chipsUserVideoLoader.loadByUserId(userId).stream()
                .sorted(Comparator.comparing(AIUserVideo::getCreateTime))
                .collect(Collectors.toList());
        String fileName = userId + "-video.xls";
        HSSFWorkbook hssfWorkbook = createBookExportResult(res);
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    private HSSFWorkbook createBookExportResult(List<AIUserVideo> records) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("视频");
        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFRow firstRow = createRow(hssfSheet, 0, 5, borderStyle);
        setCellValue(firstRow, 0, borderStyle, "教材id");
        setCellValue(firstRow, 1, borderStyle, "单元id");
        setCellValue(firstRow, 2, borderStyle, "课程名称");
        setCellValue(firstRow, 3, borderStyle, "用户视频");
        setCellValue(firstRow, 4, borderStyle, "合成前视频");
        setCellValue(firstRow, 5, borderStyle, "合成后视频");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(records)) {
            for (AIUserVideo record : records) {
                HSSFRow row = createRow(hssfSheet, rowNum++, 5, borderStyle);
                setCellValue(row, 0, borderStyle, SafeConverter.toString(record.getBookId(), ""));
                setCellValue(row, 1, borderStyle, SafeConverter.toString(record.getUnitId(), ""));
                setCellValue(row, 2, borderStyle, SafeConverter.toString(record.getLessonName(), ""));
                setCellValue(row, 3, borderStyle, CollectionUtils.isEmpty(record.fetchUserVideos()) ? "" : StringUtils.join(record.fetchUserVideos(), ","));
                setCellValue(row, 4, borderStyle, CollectionUtils.isEmpty(record.getOriginalVideos()) ? "" : StringUtils.join(record.getOriginalVideos(), ","));
                setCellValue(row, 5, borderStyle, SafeConverter.toString(record.getVideo(), ""));
            }
        }
        return hssfWorkbook;
    }

    private void setCellValue(HSSFRow row, int column, CellStyle style, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private HSSFRow createRow(HSSFSheet sheet, int rowNum, int column, CellStyle style) {
        HSSFRow row = sheet.createRow(rowNum);
        for (int i = 0; i <= column; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
        return row;
    }
}