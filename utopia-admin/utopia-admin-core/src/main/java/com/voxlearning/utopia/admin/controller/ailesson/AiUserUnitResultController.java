package com.voxlearning.utopia.admin.controller.ailesson;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.SelectOption;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsUserPageViewLogService;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

@Controller
@RequestMapping("/chips/user/unit")
public class AiUserUnitResultController extends AbstractAdminSystemController {

    @Inject
    private AiLoaderClient aiLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @ImportService(interfaceClass = ChipsUserPageViewLogService.class)
    private ChipsUserPageViewLogService chipsUserPageViewLogService;

    private List<SelectOption> buildClazzOption(Long selectedValue) {
        List<ChipsEnglishClass> allClazzList = chipsEnglishClazzService.selectAllChipsEnglishClass();
        return buildClazzOption(allClazzList, selectedValue);
    }

    private List<SelectOption> buildClazzOption(List<ChipsEnglishClass> clazzList, Long selectedValue) {
        if (CollectionUtils.isEmpty(clazzList)) {
            return new ArrayList<>();
        }
        return clazzList.stream().filter(c -> c.getId() != null && c.getId() != 0l).map(c -> {
            SelectOption option = new SelectOption();
            option.setValue(c.getId());
            option.setDesc(c.getName());
            option.setSelected(selectedValue == null ? false : c.getId().equals(selectedValue));
            return option;
        }).collect(Collectors.toList());
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @Deprecated
    public String fetchExaminList(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<NewBookCatalog> unitList = getNewBookCatalogs();
        Long userId = getRequestLong("userId");
        String userName = getRequestString("userName");
        Long clazz = getRequestLong("clazz");
        List<SelectOption> clazzOptionList = buildClazzOption(clazz);
        List<Map<String, Object>> resList;
        if (userId == null || userId.compareTo(0L) <= 0) {
            resList = selectByClazz(clazz, userName, unitList);
        } else {
            resList = selectByUserId(userId, unitList);
        }
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 100);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(resList, pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", resList != null ? resList.size() : 0);
        model.addAttribute("userName", userName);
        model.addAttribute("userId", userId != null && !userId.equals(0L) ? userId : 0);
        model.addAttribute("clazz", clazz);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("clazzOptionList", clazzOptionList);
        return "ailesson/userunitres_index";
    }

    private List<NewBookCatalog> getNewBookCatalogs() {
        return newContentLoaderClient.loadChildrenSingle("BK_10300003451674", BookCatalogType.UNIT)
                .stream()
                .filter(e -> !"BKC_10300231943369".equals(e.getId()))
                .sorted(Comparator.comparing(NewBookCatalog::getRank)).collect(Collectors.toList());
    }

    private String getClazzName(Long userId) {
        return Optional.ofNullable(chipsEnglishClazzService.loadMyDefaultClass(userId)).filter(c -> c != null).map(ChipsEnglishClass::getName).orElse("");
    }

    private List<Map<String, Object>> selectByUserId(Long userId, List<NewBookCatalog> unitList) {
        List<Map<String, Object>> resList = new ArrayList<>();
        User user = userLoaderClient.loadUser(userId);
        String uName = getNickName(user, "");
        Map<String, Object> res = new HashMap<>();
        res.put("user", uName + "(" + userId + ")");
        res.put("userId", userId);
        res.put("clazz", getClazzName(userId));
        List<AIUserUnitResultPlan> userUnitRes = aiLoaderClient.getRemoteReference().loadUnitStudyPlan(userId);
        int size = CollectionUtils.isNotEmpty(userUnitRes) ? userUnitRes.size() : 0;
        res.put("finish", size + "/" + unitList.size());
        for (int i = 0; i < unitList.size(); i++) {
            res.put("c" + (i + 1), getScore(unitList.get(i).getId(), userUnitRes));
        }
        resList.add(res);
        return resList;
    }

    private String getNickName(User user, String defName) {
        return Optional.ofNullable(user)
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .filter(o -> StringUtils.isNotBlank(o))
                .orElse(defName);
    }

    private List<Long> filterUserName(List<Long> userIdSet, Map<Long, User> userInfoMap, String userName) {
        return userIdSet.stream().filter(e -> StringUtils.isBlank(userName) ||
                (userInfoMap.get(e) != null && userInfoMap.get(e).getProfile() != null
                        && StringUtils.isNotBlank(userInfoMap.get(e).getProfile().getNickName()) &&
                        userInfoMap.get(e).getProfile().getNickName().contains(userName))).collect(Collectors.toList());
    }

    private String getScore(String unitId, List<AIUserUnitResultPlan> userUnitResultPlanList) {
        AIUserUnitResultPlan aiUserUnitResultPlan = Optional.ofNullable(userUnitResultPlanList)
                .map(e -> e.stream().filter(e1 -> e1.getUnitId().equals(unitId)).findFirst().orElse(null))
                .orElse(null);
        return aiUserUnitResultPlan != null ? (aiUserUnitResultPlan.getScore() + "") : "--";
    }

    private List<Map<String, Object>> selectByClazz(Long clazzId, String userName, List<NewBookCatalog> unitList) {
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId);
        if (clazz == null || CollectionUtils.isEmpty(userRefList)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = userRefList.stream().map(ref -> ref.getUserId()).collect(Collectors.toList());
        Map<Long, User> userInfoMap = userLoaderClient.loadUsers(userIdList);
        userIdList = filterUserName(userIdList, userInfoMap, userName);
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<Long, List<AIUserUnitResultPlan>> userUnitResultPlanMap = getUserUnitResultPlan(userIdList);
        for (Long userId : userIdList) {
            Map<String, Object> res = new HashMap<>();
            String nickName = getNickName(Optional.ofNullable(userInfoMap).map(m -> m.get(userId)).get(), "");
            res.put("user", nickName + "(" + userId + ")");
            res.put("clazz", clazz.getName());
            List<AIUserUnitResultPlan> userUnitResultPlanList = userUnitResultPlanMap.get(userId);
            int size = userUnitResultPlanList != null ? userUnitResultPlanList.size() : 0;
            res.put("finish", size + "/" + unitList.size());
            for (int i = 0; i < unitList.size(); i++) {
                String unitId = unitList.get(i).getId();
                res.put("c" + (i + 1), getScore(unitId, userUnitResultPlanList));
            }
            resList.add(res);
        }
        return resList;
    }

    private List<Map<String, Object>> exportByClazz(ChipsEnglishClass clazz, List<NewBookCatalog> unitList) {
        if (clazz == null || clazz.getId() == null) {
            return new ArrayList<>();
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazz.getId());
        if (clazz == null || CollectionUtils.isEmpty(userRefList)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = userRefList.stream().map(ref -> ref.getUserId()).collect(Collectors.toList());
        Map<Long, User> userInfoMap = userLoaderClient.loadUsers(userIdList);
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<Long, List<AIUserUnitResultPlan>> userUnitResultPlanMap = getUserUnitResultPlan(userIdList);
        for (Long userId : userIdList) {
            Map<String, Object> res = new HashMap<>();
            String nickName = getNickName(Optional.ofNullable(userInfoMap).map(m -> m.get(userId)).get(), "");
            res.put("userName", nickName);
            res.put("userId", userId);
            res.put("clazz", clazz.getName());
            List<AIUserUnitResultPlan> userUnitResultPlanList = userUnitResultPlanMap.get(userId);
            int size = userUnitResultPlanList != null ? userUnitResultPlanList.size() : 0;
            res.put("finish", size + "/" + unitList.size());
            for (int i = 0; i < unitList.size(); i++) {
                String unitId = unitList.get(i).getId();
                res.put("c" + (i + 1), getScore(unitId, userUnitResultPlanList));
            }
            resList.add(res);
        }
        return resList;
    }

    private Map<Long, List<AIUserUnitResultPlan>> getUserUnitResultPlan(List<Long> userIdList) {
        Map<Long, List<AIUserUnitResultPlan>> unitResMap = new HashMap<>();
        for (int i = 0; i < userIdList.size(); i += 100) {
            Map<Long, List<AIUserUnitResultPlan>> resMap = aiLoaderClient.getRemoteReference().loadUnitStudyPlan(userIdList.subList(i, Math.min(i + 100, userIdList.size())));
            if (MapUtils.isNotEmpty(resMap)) {
                unitResMap.putAll(resMap);
            }
        }
        return unitResMap;
    }

    @RequestMapping(value = "bookres/preview.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public void bookresult() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return;
        }
        ChipsEnglishClass clazz = Optional.ofNullable(chipsEnglishClazzService.selectChipsEnglishClassById(getRequestLong("clazz"))).orElse(null);
        if (clazz == null) {
            return;
        }
        List<Map<String, Object>> resList = previewByClazz(clazz);
        HSSFWorkbook hssfWorkbook = createBookExportResult(resList);
        String filename = clazz.getName() + dateToString(new Date(), FORMAT_SQL_DATE) + "定级报告.xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    private List<Map<String, Object>> previewByClazz(ChipsEnglishClass clazz) {
        if (clazz == null || clazz.getId() == null) {
            return new ArrayList<>();
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazz.getId());
        if (clazz == null || CollectionUtils.isEmpty(userRefList)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = userRefList.stream().map(ref -> ref.getUserId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> resList = new ArrayList<>();
        String bookId = userOrderLoaderClient.loadProductItemsByProductId(clazz.getProductId()).stream().findFirst().map(OrderProductItem::getAppItemId).orElse("");
        Map<Long, AIUserBookResult> resultMap = chipsEnglishContentLoader.loadPreviewUserBookResult(userIdList, bookId);
        Map<Long, User> userInfoMap = userLoaderClient.loadUsers(userIdList);

        Map<Long, Boolean> renewMap = isRenew(userIdList, clazz);//c
        Map<String, ChipsUserPageViewLog> viewReportMap = isViewReportPage(userIdList, bookId);//a
        Map<String, ChipsUserPageViewLog> viewRenewV2PageMap = isViewRenewV2Page(userIdList, clazz.getId());//b
        Map<String, ChipsUserPageViewLog> viewRenewPageMap = isViewRenewPage(userIdList);//续费页；广告页
        userIdList.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", e);
            String uName = getNickName(Optional.ofNullable(userInfoMap).map(m -> m.get(e)).orElse(null), "--");
            map.put("userName", uName);
            Optional<AIUserBookResult> bookResult = Optional.ofNullable(resultMap).map(rm -> rm.get(e));
            map.put("userScore", bookResult.map(br -> String.valueOf(br.getScore())).orElse("--"));
            map.put("userLevel", bookResult.filter(br -> br.getLevel() != null).map(br -> br.getLevel().name()).orElse("--"));
            map.put("en", bookResult.map(br -> br.getEnSummary()).orElse("--"));
            map.put("cn", bookResult.map(br -> br.getCnSummary()).orElse("--"));
            map.put("renewStatus", buildRenewStatus(e, renewMap, viewRenewV2PageMap, viewReportMap, bookId, clazz.getId()));
            map.put("viewGroupBuyPage",buildViewGroupBuyPageStatus(e, viewRenewPageMap));
            resList.add(map);
        });
        return resList;
    }

    private String buildViewGroupBuyPageStatus(Long userId, Map<String, ChipsUserPageViewLog> viewRenewPageMap) {
        if (viewRenewPageMap.get(ChipsUserPageViewLog.genId(userId, "GROUP_BUY")) != null) {
            return "是";
        }
        return "否";
    }

    private String buildRenewStatus(Long userId, Map<Long, Boolean> renewMap, Map<String, ChipsUserPageViewLog> viewRenewV2PageMap
            , Map<String, ChipsUserPageViewLog> viewReportMap, String bookId, Long clazzId) {
        if (renewMap.get(userId) != null && renewMap.get(userId)) {
            return "已续费";
        }
        if (viewRenewV2PageMap.get(ChipsUserPageViewLog.genId(userId, "v2-" + userId + "-" + clazzId)) != null) {
            return "已查看优惠";
        }
        if (viewReportMap.get(ChipsUserPageViewLog.genId(userId, bookId + "-" +userId)) != null) {
            return "已查看定级";
        }
        return "无";
    }

    /**
     * 是否续费
     */
    private Map<Long, Boolean> isRenew(List<Long> userIdList, ChipsEnglishClass clazz) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> map = new HashMap<>();
        userIdList.forEach(uid -> {
            List<ChipsUserCourse> courseList = chipsEnglishClazzService.loadChipsUserCourseByUserId(uid);
            map.put(uid, isRenewUser(courseList, clazz));
        });
        return map;
    }

    private boolean isRenewUser(List<ChipsUserCourse> courseList, ChipsEnglishClass clazz) {
        Date createTime = courseList.stream().filter(e -> Boolean.TRUE.equals(e.getActive())).filter(e -> StringUtils.isNotBlank(e.getProductId()))
                .filter(e -> e.getProductId().equals(clazz.getProductId())).map(e -> e.getCreateTime()).findFirst().orElse(null);
        if (createTime == null) {
            return false;
        }
        ChipsUserCourse ct = courseList.stream() .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .filter(e -> StringUtils.isNotBlank(e.getProductId())).filter(e -> e.getCreateTime().after(createTime)).findFirst().orElse(null);
        return ct != null;
    }

    /**
     * 续费提醒中的续费提醒V2已点击查看
     * @param userIdList
     * @return
     */
    private Map<String, ChipsUserPageViewLog> isViewRenewV2Page(List<Long> userIdList, long clazzId) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        Set<String> logIdSet = userIdList.stream().map(uid -> ChipsUserPageViewLog.genId(uid, "v2-" + uid + "-" + clazzId)).collect(Collectors.toSet());
        return chipsUserPageViewLogService.loadChipsUserPageViewLogByIds(logIdSet);
    }

    /**
     * 续费提醒中的定级报告链接已点击查看。
     * @param userIdList
     * @return
     */
    private Map<String, ChipsUserPageViewLog> isViewReportPage(List<Long> userIdList, String bookId) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        Set<String> logIdSet = userIdList.stream().map(uid ->  ChipsUserPageViewLog.genId(uid , bookId + "-" + uid)).collect(Collectors.toSet());
        return chipsUserPageViewLogService.loadChipsUserPageViewLogByIds(logIdSet);
    }

    /**
     * 是否查看续费页；广告页
     * @param userIdList
     * @return
     */
    private Map<String, ChipsUserPageViewLog> isViewRenewPage(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        Set<String> logIdSet = userIdList.stream().map(uid -> ChipsUserPageViewLog.genId(uid, PageViewType.GROUP_BUY.name())).collect(Collectors.toSet());
        return chipsUserPageViewLogService.loadChipsUserPageViewLogByIds(logIdSet);
    }

    @RequestMapping(value = "/data/export.vpage", method = RequestMethod.GET)
    @Deprecated
    public void exportData() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return;
        }
        ChipsEnglishClass clazz = Optional.ofNullable(chipsEnglishClazzService.selectChipsEnglishClassById(getRequestLong("clazz"))).orElse(null);
        if (clazz == null) {
            return;
        }
        List<NewBookCatalog> unitList = getNewBookCatalogs();
        List<Map<String, Object>> resList = exportByClazz(clazz, unitList);
        HSSFWorkbook hssfWorkbook = createUnitExportResult(resList);
        String filename = clazz.getName() + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            hssfWorkbook.write(outStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception e) {
            logger.error("exportData error.", e);
        }
    }

    private HSSFWorkbook createBookExportResult(List<Map<String, Object>> records) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成绩");

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFRow firstRow = createRow(hssfSheet, 0, 5, borderStyle);
        setCellValue(firstRow, 0, borderStyle, "用户ID");
        setCellValue(firstRow, 1, borderStyle, "用户姓名");
        setCellValue(firstRow, 2, borderStyle, "成绩");
        setCellValue(firstRow, 3, borderStyle, "等级");
        setCellValue(firstRow, 4, borderStyle, "续费状态");
        setCellValue(firstRow, 5, borderStyle, "查看续费页");
        setCellValue(firstRow, 6, borderStyle, "英文评论");
        setCellValue(firstRow, 7, borderStyle, "中文评论");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(records)) {
            for (Map<String, Object> record : records) {
                HSSFRow row = createRow(hssfSheet, rowNum++, 5, borderStyle);
                setCellValue(row, 0, borderStyle, SafeConverter.toString(record.get("userId"), ""));
                setCellValue(row, 1, borderStyle, SafeConverter.toString(record.get("userName"), ""));
                setCellValue(row, 2, borderStyle, SafeConverter.toString(record.get("userScore"), ""));
                setCellValue(row, 3, borderStyle, SafeConverter.toString(record.get("userLevel"), ""));
                setCellValue(row, 4, borderStyle, SafeConverter.toString(record.get("renewStatus"), ""));
                setCellValue(row, 5, borderStyle, SafeConverter.toString(record.get("viewGroupBuyPage"), ""));
                setCellValue(row, 6, borderStyle, SafeConverter.toString(record.get("en"), ""));
                setCellValue(row, 7, borderStyle, SafeConverter.toString(record.get("cn"), ""));
            }
        }
        return hssfWorkbook;
    }

    private HSSFWorkbook createUnitExportResult(List<Map<String, Object>> records) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFSheet hssfSheet = hssfWorkbook.createSheet("成绩");

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        HSSFRow firstRow = createRow(hssfSheet, 0, 13, borderStyle);
        setCellValue(firstRow, 0, borderStyle, "班級");
        setCellValue(firstRow, 1, borderStyle, "用户ID");
        setCellValue(firstRow, 2, borderStyle, "用户姓名");
        setCellValue(firstRow, 3, borderStyle, "完成率");
        setCellValue(firstRow, 4, borderStyle, "第一课");
        setCellValue(firstRow, 5, borderStyle, "第二课");
        setCellValue(firstRow, 6, borderStyle, "第三课");
        setCellValue(firstRow, 7, borderStyle, "第四课");
        setCellValue(firstRow, 8, borderStyle, "第五课");
        setCellValue(firstRow, 9, borderStyle, "第六课");
        setCellValue(firstRow, 10, borderStyle, "第七课");
        setCellValue(firstRow, 11, borderStyle, "第八课");
        setCellValue(firstRow, 12, borderStyle, "第九课");
        setCellValue(firstRow, 13, borderStyle, "第十课");
        int rowNum = 1;
        if (CollectionUtils.isNotEmpty(records)) {
            for (Map<String, Object> record : records) {
                HSSFRow row = createRow(hssfSheet, rowNum++, 13, borderStyle);
                setCellValue(row, 0, borderStyle, SafeConverter.toString(record.get("clazz"), ""));
                setCellValue(row, 1, borderStyle, SafeConverter.toString(record.get("userId"), ""));
                setCellValue(row, 2, borderStyle, SafeConverter.toString(record.get("userName"), ""));
                setCellValue(row, 3, borderStyle, SafeConverter.toString(record.get("finish"), ""));
                for (int i = 1; i < 11; i++) {
                    setCellValue(row, 3 + i, borderStyle, SafeConverter.toString(record.get("c" + i), ""));
                }
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
