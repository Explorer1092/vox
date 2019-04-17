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

package com.voxlearning.utopia.admin.controller.textbook;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
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
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.EntityUtils;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.email.api.EmailService;
import com.voxlearning.utopia.service.email.api.client.PlainEmailCreator;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.CRMTextBookManagementService;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.TextBookManagementLoader;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSourceType;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/4/6.
 */
@Controller
@RequestMapping("/textbook")
@Slf4j
public class TextBookManagementController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CRMTextBookManagementService.class)
    private CRMTextBookManagementService crmTextBookManagementService;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @ImportService(interfaceClass = TextBookManagementLoader.class)
    private TextBookManagementLoader textBookManagementLoader;
    @ImportService(interfaceClass = EmailService.class)
    private EmailService emailService;

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private static final String PICLISTENBOOK_EDIT_EMAIL_LIST = "PICLISTENBOOK_EDIT_EMAIL_LIST";

    /**
     * 新建或者编辑教材
     */
    @RequestMapping(value = "/upsertTextBookData.vpage", method = RequestMethod.GET)
    public String upsertTextBookData(Model model) {

        String bookId = getRequestString("bookId");
        model.addAttribute("bookId", bookId);
        if (StringUtils.isNotEmpty(bookId)) {
            TextBookManagement textBookManagement = crmTextBookManagementService.$loadByBookId(bookId);
            if (textBookManagement != null) {
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                model.addAttribute("name", newBookProfile.getName());
                model.addAttribute("publisher", newBookProfile.getPublisher());
                model.addAttribute("shortPublisher", newBookProfile.getShortPublisher());
                model.addAttribute("shortName", newBookProfile.getShortName());
                model.addAttribute("listName", textBookManagement.getBookListName());
                model.addAttribute("clazzLevel", newBookProfile.getClazzLevel());
                model.addAttribute("termType", newBookProfile.getTermType());
                model.addAttribute("picListenConfig", textBookManagement.getPicListenConfig());
                if (textBookManagement.getPicListenConfig() != null && textBookManagement.getPicListenConfig().getSdkInfo() != null
                        && textBookManagement.getPicListenConfig().getSdkInfo().getSdkType() != null) {
                    model.addAttribute("picListenConfigSdkType", textBookManagement.getPicListenConfig().getSdkInfo().getSdkType().name());
                } else {
                    model.addAttribute("picListenConfigSdkType", "");
                }
                model.addAttribute("walkManConfig", textBookManagement.getWalkManConfig());
                model.addAttribute("textReadConfig", textBookManagement.getTextReadConfig());
                model.addAttribute("isFollowRead", textBookManagement.getIsFollowRead());
                model.addAttribute("hasWordList", textBookManagement.getHasWordList());
                model.addAttribute("chineseWordSupport", textBookManagement.getChineseWordSupport());
                model.addAttribute("comment", textBookManagement.getComment());
                model.addAttribute("sourceType", textBookManagement.getSourceType().name());
                model.addAttribute("refBookId", textBookManagement.getRefBookId());
                model.addAttribute("subjectId", textBookManagement.getSubjectId());
            }
        }
        return "textbook/textbookdetail";
    }

    /**
     * 保存教材信息
     */
    @RequestMapping(value = "/saveTextBookData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTextBookData() {
        String bookId = getRequestString("bookId");
        Boolean isNew = getRequestBool("isNew");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null) {
            return MapMessage.errorMessage("BookId输入有误");
        }
        if (isNew) {
            TextBookManagement newBook = crmTextBookManagementService.$loadByBookId(bookId);
            if (newBook != null) {
                return MapMessage.errorMessage("该教材已经被添加，请勿重复添加");
            }
        }

        String listName = getRequestString("listName");
        if (StringUtils.isBlank(listName)){
            return MapMessage.errorMessage("教材列表页名称不能为Null");
        }
        String sourceType = getRequestString("book_source");
        String sdkBookId = getRequestString("picListenSdkBookId");
        String sdkBookIdV2 = getRequestString("picListenSdkBookIdV2");
        String sdkTypeStr = getRequestString("picListenSdkType");
        Boolean isPicListenAndroidOnline = getRequestBool("picListenAndroidOnline");
        Boolean isPicListenIOSOnline = getRequestBool("picListenIOSOnline");
        Boolean isPicMiniProgramOnline = getRequestBool("picMiniProgramOnline");
        Boolean isPicListenAuthUserOnline = getRequestBool("picListenAuthUserOnline");
        Boolean isPicListenFree = getRequestBool("picListenFree");
        Boolean isPicListenPreview = getRequestBool("picListenPreview");
        Boolean isWalkManAndroidOnline = getRequestBool("walkManAndroidOnline");
        Boolean isWalkManIOSOnline = getRequestBool("walkManIOSOnline");
        Boolean isWalkManMiniProgramOnline = getRequestBool("walkManMiniProgramOnline");
        //随身听付费相关
        Boolean isWalkManFree = getRequestBool("walkManFree");
        String walkManLeastVersion = getRequestString("walkManLeastVersion");
        Boolean isTextReadAndroidOnline = getRequestBool("textReadAndroidOnline");
        Boolean isTextReadIOSOnline = getRequestBool("textReadIOSOnline");
        Boolean isFollowRead = getRequestBool("followRead");
        Boolean chineseWordSupport = getRequestBool("chineseWordSupport");
        Boolean hasWordList = getRequestBool("wordList");
        String comment = getRequestString("comment");

        TextBookManagement textBookManagement = new TextBookManagement();
        textBookManagement.setBookId(bookId);
        textBookManagement.setBookListName(listName);
        textBookManagement.setSourceType(TextBookSourceType.parse(sourceType));
        TextBookManagement.PicListenConfig picListenConfig = new TextBookManagement.PicListenConfig();
        picListenConfig.setIsAndroidOnline(isPicListenAndroidOnline);
        picListenConfig.setIsIOSOnline(isPicListenIOSOnline);
        picListenConfig.setIsMiniProgramOnline(isPicMiniProgramOnline);
        picListenConfig.setIsAuthUserOnline(isPicListenAuthUserOnline);
        picListenConfig.setIsFree(isPicListenFree);
        picListenConfig.setSdkBookId(sdkBookId);
        TextBookManagement.SdkInfo sdkInfo = new TextBookManagement.SdkInfo();
        TextBookSdkType sdkType = TextBookSdkType.valueOf(sdkTypeStr);
        if (sdkType == null)
            sdkType = TextBookSdkType.none;
        sdkInfo.setSdkBookId(sdkBookId);
        if (sdkType == TextBookSdkType.renjiao) {
            sdkInfo.setRenjiaoNewSdkBookId(sdkBookIdV2);
        }
        sdkInfo.setSdkType(sdkType);
        picListenConfig.setSdkInfo(sdkInfo);
        picListenConfig.setIsPreview(isPicListenPreview);
        textBookManagement.setPicListenConfig(picListenConfig);
        TextBookManagement.WalkManConfig walkManConfig = new TextBookManagement.WalkManConfig();
        walkManConfig.setIsAndroidOnline(isWalkManAndroidOnline);
        walkManConfig.setIsIOSOnline(isWalkManIOSOnline);
        walkManConfig.setIsMiniProgramOnline(isWalkManMiniProgramOnline);
        //随身听付费相关
        walkManConfig.setIsFree(isWalkManFree);
        walkManConfig.setLeastVersion(walkManLeastVersion);
        textBookManagement.setWalkManConfig(walkManConfig);
        TextBookManagement.TextReadConfig textReadConfig = new TextBookManagement.TextReadConfig();
        textReadConfig.setIsAndroidOnline(isTextReadAndroidOnline);
        textReadConfig.setIsIOSOnline(isTextReadIOSOnline);
        textBookManagement.setTextReadConfig(textReadConfig);
        textBookManagement.setIsFollowRead(isFollowRead);
        textBookManagement.setHasWordList(hasWordList);
        textBookManagement.setChineseWordSupport(chineseWordSupport);
        textBookManagement.setComment(comment);
        textBookManagement.setOperateUser(getCurrentAdminUser().getAdminUserName());

        TextBookManagement textBookManagementOld = crmTextBookManagementService.$loadByBookId(textBookManagement.getBookId());

        TextBookManagement textBook = crmTextBookManagementService.$upsertTextBook(textBookManagement);
        if (textBookManagementOld != null && textBook != null) {
            //记录修改内容并发送邮件
            String changeLog = "";
            try {
                changeLog = EntityUtils.getInstance().compareAndGetResult("", textBookManagementOld, textBook, "createTime", "updateTime", "subjectId");
                addAdminLog("操作修改点读机教材配置", textBook.getBookId(), "修改教材配置", changeLog);
            } catch (Exception e) {
                logger.warn("compareAndGetResult error", e);
            }
            if (StringUtils.isNotBlank(changeLog)) {
                generateAndSendEmail(textBook.getBookId(), newBookProfile.getName(), newBookProfile.getPublisher(), DateUtils.dateToString(textBook.getUpdateTime()), getCurrentAdminUser().getAdminUserName(), changeLog);
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }


    /**
     * 教材管理列表页
     */
    @RequestMapping(value = "/exportAll.vpage", method = RequestMethod.GET)
    public void exportAllBook(HttpServletResponse response) {
        List<TextBookManagement> textBookManagements = crmTextBookManagementService.$loadTextBooks();
        Map<String, TextBookManagement> textBookManagementMap = textBookManagements.stream().collect(Collectors.toMap(TextBookManagement::getBookId, Function.identity()));
        List<String> bookIdList = textBookManagements.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet sheet = xssfWorkbook.createSheet();
        //表头
        XSSFRow head = sheet.createRow(0);
        head.createCell(0).setCellValue("教材id");
        head.createCell(1).setCellValue("教材名称");
        head.createCell(2).setCellValue("出版社简称");
        head.createCell(3).setCellValue("年级");
        head.createCell(4).setCellValue("学期");
        head.createCell(5).setCellValue("学科");
        head.createCell(6).setCellValue("点读机ios上线");
        head.createCell(7).setCellValue("点读机android上线");
        head.createCell(8).setCellValue("点读机认证用户上线");
        head.createCell(9).setCellValue("点读机是否需要付费");
        head.createCell(10).setCellValue("随身听ios上线");
        head.createCell(11).setCellValue("随身听android上线");
        head.createCell(12).setCellValue("语文朗读ios上线");
        head.createCell(13).setCellValue("语文朗读android上线");
        head.createCell(14).setCellValue("是否支持跟读");
        head.createCell(15).setCellValue("是否支持语文生字表");
        head.createCell(16).setCellValue("是否支持英语生词表");
        head.createCell(17).setCellValue("教材类型");
        head.createCell(18).setCellValue("关联教材id");
        head.createCell(19).setCellValue("sdkBookId");

        for (int i = 0; i < bookIdList.size(); i++) {
            String bookId = bookIdList.get(i);
            XSSFRow row = sheet.createRow(i + 1);
            TextBookManagement textBookManagement = textBookManagementMap.get(bookId);
            if (textBookManagement == null)
                continue;
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            row.createCell(0).setCellValue(bookId);
            row.createCell(1).setCellValue(newBookProfile.getShortName());
            row.createCell(2).setCellValue(newBookProfile.getShortPublisher());
            row.createCell(3).setCellValue(newBookProfile.getClazzLevel());
            row.createCell(4).setCellValue(Term.of(newBookProfile.getTermType()).name());
            row.createCell(5).setCellValue(Subject.fromSubjectId(newBookProfile.getSubjectId()).getValue());
            if (textBookManagement.getPicListenConfig() != null) {
                row.createCell(6).setCellValue(fromBoolean(textBookManagement.getPicListenConfig().getIsAndroidOnline()));
                row.createCell(7).setCellValue(fromBoolean(textBookManagement.getPicListenConfig().getIsAndroidOnline()));
                row.createCell(8).setCellValue(fromBoolean(textBookManagement.getPicListenConfig().getIsAuthUserOnline()));
                row.createCell(9).setCellValue(fromBoolean(!textBookManagement.getPicListenConfig().getIsFree()));
            } else {
                row.createCell(6).setCellValue(fromBoolean(false));
                row.createCell(7).setCellValue(fromBoolean(false));
                row.createCell(8).setCellValue(fromBoolean(false));
                row.createCell(9).setCellValue(fromBoolean(false));
            }
            if (textBookManagement.getWalkManConfig() != null) {
                row.createCell(10).setCellValue(fromBoolean(textBookManagement.getWalkManConfig().getIsIOSOnline()));
                row.createCell(11).setCellValue(fromBoolean(textBookManagement.getWalkManConfig().getIsAndroidOnline()));
            } else {
                row.createCell(10).setCellValue(fromBoolean(false));
                row.createCell(11).setCellValue(fromBoolean(false));
            }
            if (textBookManagement.getTextReadConfig() != null) {
                row.createCell(12).setCellValue(fromBoolean(textBookManagement.getTextReadConfig().getIsIOSOnline()));
                row.createCell(13).setCellValue(fromBoolean(textBookManagement.getTextReadConfig().getIsAndroidOnline()));
            } else {
                row.createCell(12).setCellValue(fromBoolean(false));
                row.createCell(13).setCellValue(fromBoolean(false));
            }
            row.createCell(14).setCellValue(fromBoolean(textBookManagement.getIsFollowRead()));
            row.createCell(15).setCellValue(fromBoolean(textBookManagement.getChineseWordSupport()));
            row.createCell(16).setCellValue(fromBoolean(textBookManagement.getHasWordList()));
            row.createCell(17).setCellValue(fromType(textBookManagement.getSourceType()));
            row.createCell(18).setCellValue(textBookManagement.getRefBookId());
            if (textBookManagement.getPicListenConfig() != null && textBookManagement.getPicListenConfig().getSdkInfo() != null) {
                row.createCell(19).setCellValue(SafeConverter.toString(textBookManagement.getPicListenConfig().getSdkInfo().getSdkBookIdV2()));
            }
        }

        try {
            String filename = "导出教材-";
            filename += DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (IOException e) {
            log.error("下载失败!", e);
        }
    }

    private String fromType(TextBookSourceType sourceType) {
        if (sourceType == TextBookSourceType.SELF_DEVELOP)
            return "A类";
        if (sourceType == TextBookSourceType.NAMI)
            return "B类";
        return "C类";
    }

    private String fromBoolean(Boolean b) {
        if (b == null)
            return "false";
        return b.toString();
    }

    /**
     * 教材管理列表页
     */
    @RequestMapping(value = "/textbooklist.vpage", method = RequestMethod.GET)
    public String textBookList(Model model) {

        int pageIndex = SafeConverter.toInt(getRequestParameter("page", "1"));
        String searchBookId = getRequestParameter("searchBookId", "");
        String searchSourceType = getRequestParameter("searchSourceType", "");
        Integer searchSubject = SafeConverter.toInt(getRequestParameter("searchSubject", ""));
        String shortPublisher = getRequestParameter("shortPublisher", "");
        List<TextBookMapper> publisherList = textBookManagementLoader.getPublisherList();
        //给前端传过去
        List<String> shortPublisherList = publisherList.stream().map(TextBookMapper::getPublisherShortName).collect(Collectors.toList());
        List<TextBookManagement> textBookManagementList = crmTextBookManagementService.$loadTextBooks();
        Set<String> publisherBookIds = new HashSet<>();
        if (StringUtils.isNotBlank(shortPublisher)) {
            Set<String> allBookIds = textBookManagementList.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(allBookIds)) {
                publisherBookIds = newContentLoaderClient.loadBooks(allBookIds).values().stream().filter(e -> StringUtils.equals(e.getShortPublisher(), shortPublisher)).map(NewBookProfile::getId).collect(Collectors.toSet());
            }
        }
        Set<String> finalPublisherBookIds = publisherBookIds;
        textBookManagementList = textBookManagementList.stream()
                .filter(e -> StringUtils.isBlank(searchBookId) || StringUtils.isNotBlank(searchBookId) && StringUtils.equals(searchBookId, e.getBookId()))
                .filter(e -> StringUtils.isBlank(searchSourceType) || StringUtils.isNotBlank(searchSourceType) && StringUtils.equals(searchSourceType, e.getSourceType().name()))
                .filter(e -> searchSubject == 0 || Objects.equals(searchSubject, e.getSubjectId()))
                .filter(e -> CollectionUtils.isEmpty(finalPublisherBookIds) || finalPublisherBookIds.contains(e.getBookId()))
                .collect(Collectors.toList());
        Pageable pageRequest = new PageRequest(pageIndex - 1, 10);
        Page<TextBookManagement> textBookManagementPage = PageableUtils.listToPage(textBookManagementList, pageRequest);
        textBookManagementList = new ArrayList<>(textBookManagementPage.getContent());
        List<Map<String, Object>> mapList = generateTextBookDetailList(textBookManagementList);
        model.addAttribute("textBookList", mapList);
        model.addAttribute("currentPage", pageIndex);
        model.addAttribute("totalPage", textBookManagementPage.getTotalPages());
        model.addAttribute("hasPrev", textBookManagementPage.hasPrevious());
        model.addAttribute("hasNext", textBookManagementPage.hasNext());
        model.addAttribute("searchSourceType", searchSourceType);
        model.addAttribute("searchBookId", searchBookId);
        model.addAttribute("searchSubject", searchSubject);
        model.addAttribute("shortPublisherList", shortPublisherList);
        model.addAttribute("shortPublisher", shortPublisher);
        return "textbook/textbooklist";
    }


    /**
     * 进入导入教材数据页面
     */
    @RequestMapping(value = "/importBookPage.vpage", method = RequestMethod.GET)
    public String importDataPage(Model model) {
        return "textbook/importtextbook";
    }


    /**
     * 通过id导入教材数据
     */
    @RequestMapping(value = "/importBook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importDataByBookId() {
        String bookIds = getRequestString("bookIds");
        if (StringUtils.isBlank(bookIds)) {
            return MapMessage.errorMessage();
        }
        bookIds = bookIds.replaceAll("\n|\r|\t", "").trim();
        String[] bookIdsArray = bookIds.split(",");
        List<String> bookIdsList = Arrays.asList(bookIdsArray);
        List<TextBookManagement> textBooks = generateDataListByIdList(bookIdsList);
        if (textBooks.size() < bookIdsList.size()) {
            return MapMessage.errorMessage("导入的数据中有错误的id，请检查");
        }
        if (CollectionUtils.isNotEmpty(textBooks)) {
            textBooks.stream().forEach(e -> {
                if (e != null) {
                    crmTextBookManagementService.$upsertTextBook(e);
                }
            });
        }
        return MapMessage.successMessage();
    }

    /**
     * 获取教材信息
     */
    @RequestMapping(value = "/getBookProfile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getBookProfile() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        Map<String, Object> map = new HashMap<>();
        if (newBookProfile != null) {
            map.put("bookName", newBookProfile.getName());
            map.put("publisher", newBookProfile.getPublisher());
            map.put("shortPublisher", newBookProfile.getShortPublisher());
            map.put("shortName", newBookProfile.getShortName());
            map.put("clazzLevel", newBookProfile.getClazzLevel());
            map.put("termType", newBookProfile.getTermType());
            map.put("subjectId", newBookProfile.getSubjectId());
            return MapMessage.successMessage().add("bookMap", map);
        }
        return MapMessage.errorMessage("BookId输入有误");
    }

    @RequestMapping(value = "/uploadTextBookData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadTextBookData() {
        XSSFWorkbook workbook = readExcel("source_file");
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        MapMessage mapMessage = generateTextBookManagementDetail(workbook);
        if (!mapMessage.isSuccess()) {
            int errorRow = SafeConverter.toInt(mapMessage.get("errorRow"));
            if (errorRow != 0) {
                return MapMessage.errorMessage(mapMessage.getInfo() + "错误行号" + errorRow);
            } else {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
        }
        return MapMessage.successMessage("导入成功:成功" + SafeConverter.toInt(mapMessage.get("successCount")) + "条");
    }

    /**
     * 删除教材
     */
    @RequestMapping(value = "/deleteTextBook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTextBook() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        if (crmTextBookManagementService.removeBook(bookId)) {
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            addAdminLog("操作修改点读机教材配置", bookId, "删除教材", "删除教材");
            generateAndSendEmail(bookId, newBookProfile.getName(), newBookProfile.getPublisher(), DateUtils.dateToString(new Date()), getCurrentAdminUser().getAdminUserName(), "删除教材");
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    /**
     * 检查付费教材是否关联付费产品
     */
    @RequestMapping(value = "/loadPayBookProductByBookId.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadPayBookProductByBookId() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("bookId不能为空");
        }
        Map<String, List<OrderProduct>> productByAppItemIds = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singleton(bookId));
        if (MapUtils.isNotEmpty(productByAppItemIds) && CollectionUtils.isNotEmpty(productByAppItemIds.get(bookId))) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }


    private List<Map<String, Object>> generateTextBookDetailList(List<TextBookManagement> textBookManagementList) {
        if (CollectionUtils.isEmpty(textBookManagementList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<String> bookIds = textBookManagementList.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> newBookProfileMap = newContentLoaderClient.loadBooks(bookIds);
        for (TextBookManagement book : textBookManagementList) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookId", book.getBookId());
            map.put("bookName", newBookProfileMap.get(book.getBookId()).getName());
            map.put("bookSourceType", book.getSourceType().getDesc());
            map.put("bookPublisher", newBookProfileMap.get(book.getBookId()).getPublisher());
            dataList.add(map);
        }
        return dataList;
    }

    private MapMessage generateTextBookManagementDetail(XSSFWorkbook workbook) {
        if (workbook == null) {
            return MapMessage.errorMessage("文件错误");
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("获取文件sheet页错误");
        }
        //取标题下面的一行做为起始
        Integer rowIndex = 1;
        Set<String> bookIds = new HashSet<>();
        Set<String> refBookIds = new HashSet<>();
        while (sheet.getRow(rowIndex) != null) {
            XSSFRow row = sheet.getRow(rowIndex);
            //自研bookId
            String bookId = SafeConverter.toString(XssfUtils.getStringCellValue(row.getCell(2)));
            if (StringUtils.isNotBlank(bookId)) {
                bookIds.add(bookId);
            }
            //namiBookId
            String refBookId = SafeConverter.toString(XssfUtils.getStringCellValue(row.getCell(13)));
            if (StringUtils.isNotBlank(refBookId)) {
                refBookIds.add(refBookId);
            }
            rowIndex++;
        }
        List<TextBookManagement> dataList = firstGenerateTextBookManagementList(bookIds, refBookIds);
        if (CollectionUtils.isNotEmpty(dataList)) {
            dataList.stream().forEach(e -> {
                if (e != null) {
                    crmTextBookManagementService.$upsertTextBook(e);
                }
            });
        }
        return MapMessage.successMessage().add("dataCount", dataList.size());
    }


    private XSSFWorkbook readExcel(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    /**
     * 获取pageBlock的配置
     */
    private String innerGetPicListenPageBlockConfig(String key) {
        if (StringUtils.isBlank(key))
            return StringUtils.EMPTY;
        List<PageBlockContent> selfStudyAdConfigPageContentList = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("picListenConfig");
        if (CollectionUtils.isEmpty(selfStudyAdConfigPageContentList))
            return StringUtils.EMPTY;
        PageBlockContent configPageBlockContent = selfStudyAdConfigPageContentList.stream().filter(p ->
                key.equals(p.getBlockName())
        ).findFirst().orElse(null);
        return configPageBlockContent == null ? StringUtils.EMPTY : configPageBlockContent.getContent();
    }


    private Set<String> generateAuthUserOnlineBookIds() {
        String configContent = innerGetPicListenPageBlockConfig("picListenOffline");
        Map<String, Object> configMap = JsonUtils.convertJsonObjectToMap(configContent);
        if (MapUtils.isEmpty(configMap))
            return Collections.emptySet();
        Object bookIdListObj = configMap.get("bookIdList");
        if (bookIdListObj == null || !(bookIdListObj instanceof List))
            return Collections.emptySet();
        List<String> bookIdList = (List<String>) bookIdListObj;
        if (CollectionUtils.isEmpty(bookIdList))
            return Collections.emptySet();
        return new HashSet<>(bookIdList);
    }

    private Set<String> generateFollowReadBookIds() {
        String configStr = innerGetPicListenPageBlockConfig("followReadBook");
        if (StringUtils.isBlank(configStr))
            return Collections.emptySet();
        List<String> bookIdList = JsonUtils.fromJsonToList(configStr, String.class);
        return CollectionUtils.isEmpty(bookIdList) ? Collections.emptySet() : new HashSet<>(bookIdList);
    }


    private List<TextBookManagement> firstGenerateTextBookManagementList(Collection<String> bookIds, Collection<String> refBookIds) {
        if (CollectionUtils.isEmpty(bookIds)) {
            Collections.emptyList();
        }
        List<TextBookManagement> dataList = new ArrayList<>();
        Set<String> allBookIds = new HashSet<>();
        allBookIds.addAll(bookIds);
        allBookIds.addAll(refBookIds);
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookIds);
        Set<String> authOnlineUserBookIds = generateAuthUserOnlineBookIds();
        Set<String> followReadBookIds = generateFollowReadBookIds();
        for (Map.Entry<String, NewBookProfile> book : bookProfileMap.entrySet()) {
            //自研对象
            TextBookManagement textBookManagement = new TextBookManagement();
            textBookManagement.setBookId(book.getKey());
            Map<String, Object> extras = book.getValue().getExtras();
            if (bookIds.contains(book.getKey())) {
                textBookManagement.setSourceType(TextBookSourceType.SELF_DEVELOP);
                textBookManagement.setSubjectId(book.getValue().getSubjectId());
                if (StringUtils.isNotBlank(SafeConverter.toString(extras.get("ref_book_id")))) {
                    textBookManagement.setRefBookId(SafeConverter.toString(extras.get("ref_book_id")));
                }
                //点读机
                TextBookManagement.PicListenConfig picListenConfig = new TextBookManagement.PicListenConfig();
                picListenConfig.setIsAndroidOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_android")));
                picListenConfig.setIsIOSOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_ios")));
                picListenConfig.setIsFree(!SafeConverter.toBoolean(extras.get("has_pic_listen_pay")));
                if (authOnlineUserBookIds.contains(book.getKey())) {
                    picListenConfig.setIsAuthUserOnline(Boolean.TRUE);
                } else {
                    picListenConfig.setIsAuthUserOnline(Boolean.FALSE);
                }
                picListenConfig.setSdkBookId(extras.get("pic_listen_sdk_book_id") != null ? SafeConverter.toString(extras.get("pic_listen_sdk_book_id")) : "");
                picListenConfig.setIsPreview(SafeConverter.toBoolean(extras.get("pic_listen_preview")));
                textBookManagement.setPicListenConfig(picListenConfig);
                //随身听
                TextBookManagement.WalkManConfig walkManConfig = new TextBookManagement.WalkManConfig();
                if (extras.get("has_listen") != null && SafeConverter.toBoolean(extras.get("has_listen"))) {
                    walkManConfig.setIsIOSOnline(Boolean.TRUE);
                    walkManConfig.setIsAndroidOnline(Boolean.TRUE);
                } else {
                    walkManConfig.setIsIOSOnline(Boolean.FALSE);
                    walkManConfig.setIsAndroidOnline(Boolean.FALSE);
                }
                textBookManagement.setWalkManConfig(walkManConfig);
                //语文朗读
                if ((book.getValue().getSubjectId() == Subject.CHINESE.getId())) {
                    TextBookManagement.TextReadConfig textReadConfig = new TextBookManagement.TextReadConfig();
                    if (SafeConverter.toBoolean(extras.get("reading")) && SafeConverter.toInt(extras.get("nami")) != 1) {
                        textReadConfig.setIsAndroidOnline(Boolean.TRUE);
                        textReadConfig.setIsIOSOnline(Boolean.TRUE);
                    } else {
                        textReadConfig.setIsAndroidOnline(Boolean.FALSE);
                        textReadConfig.setIsIOSOnline(Boolean.FALSE);
                    }
                    textBookManagement.setTextReadConfig(textReadConfig);
                }
                if (followReadBookIds.contains(book.getKey())) {
                    textBookManagement.setIsFollowRead(Boolean.TRUE);
                } else {
                    textBookManagement.setIsFollowRead(Boolean.FALSE);
                }
                if (book.getValue().getSubjectId() == Subject.ENGLISH.getId()) {
                    textBookManagement.setHasWordList(Boolean.TRUE);
                } else {
                    textBookManagement.setHasWordList(Boolean.FALSE);
                }
                if (book.getValue().getSubjectId() == Subject.CHINESE.getId() && SafeConverter.toInt(extras.get("nami")) != 1) {
                    textBookManagement.setChineseWordSupport(Boolean.TRUE);
                } else {
                    textBookManagement.setChineseWordSupport(Boolean.FALSE);
                }
                textBookManagement.setOperateUser(getCurrentAdminUser().getAdminUserName());
            } else if (refBookIds.contains(book.getKey())) {
                textBookManagement.setSourceType(TextBookSourceType.NAMI);
                textBookManagement.setSubjectId(book.getValue().getSubjectId());
                if (StringUtils.isNotBlank(SafeConverter.toString(extras.get("ref_book_id")))) {
                    textBookManagement.setRefBookId(SafeConverter.toString(extras.get("ref_book_id")));
                }
                //点读机
                TextBookManagement.PicListenConfig picListenConfig = new TextBookManagement.PicListenConfig();
                picListenConfig.setIsAndroidOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_android")));
                picListenConfig.setIsIOSOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_ios")));
                picListenConfig.setIsFree(!SafeConverter.toBoolean(extras.get("has_pic_listen_pay")));
                if (authOnlineUserBookIds.contains(book.getKey())) {
                    picListenConfig.setIsAuthUserOnline(Boolean.TRUE);
                } else {
                    picListenConfig.setIsAuthUserOnline(Boolean.FALSE);
                }
                picListenConfig.setSdkBookId(extras.get("pic_listen_sdk_book_id") != null ? SafeConverter.toString(extras.get("pic_listen_sdk_book_id")) : "");
                picListenConfig.setIsPreview(SafeConverter.toBoolean(extras.get("pic_listen_preview")));
                textBookManagement.setPicListenConfig(picListenConfig);
                //随身听
                TextBookManagement.WalkManConfig walkManConfig = new TextBookManagement.WalkManConfig();
                if (extras.get("has_listen") != null && SafeConverter.toBoolean(extras.get("has_listen"))) {
                    walkManConfig.setIsIOSOnline(Boolean.TRUE);
                    walkManConfig.setIsAndroidOnline(Boolean.TRUE);
                } else {
                    walkManConfig.setIsIOSOnline(Boolean.FALSE);
                    walkManConfig.setIsAndroidOnline(Boolean.FALSE);
                }
                textBookManagement.setWalkManConfig(walkManConfig);
                //语文朗读
                if (book.getValue().getSubjectId() == Subject.CHINESE.getId()) {
                    TextBookManagement.TextReadConfig textReadConfig = new TextBookManagement.TextReadConfig();
                    if (SafeConverter.toBoolean(extras.get("reading")) && SafeConverter.toInt(extras.get("nami")) != 1) {
                        textReadConfig.setIsAndroidOnline(Boolean.TRUE);
                        textReadConfig.setIsIOSOnline(Boolean.TRUE);
                    } else {
                        textReadConfig.setIsAndroidOnline(Boolean.FALSE);
                        textReadConfig.setIsIOSOnline(Boolean.FALSE);
                    }
                    textBookManagement.setTextReadConfig(textReadConfig);
                }
                if (followReadBookIds.contains(book.getKey())) {
                    textBookManagement.setIsFollowRead(Boolean.TRUE);
                } else {
                    textBookManagement.setIsFollowRead(Boolean.FALSE);
                }
                if (book.getValue().getSubjectId() == Subject.ENGLISH.getId()) {
                    textBookManagement.setHasWordList(Boolean.TRUE);
                } else {
                    textBookManagement.setHasWordList(Boolean.FALSE);
                }
                if (book.getValue().getSubjectId() == Subject.CHINESE.getId() && SafeConverter.toInt(extras.get("nami")) != 1) {
                    textBookManagement.setChineseWordSupport(Boolean.TRUE);
                } else {
                    textBookManagement.setChineseWordSupport(Boolean.FALSE);
                }
                textBookManagement.setOperateUser(getCurrentAdminUser().getAdminUserName());
            }
            dataList.add(textBookManagement);
        }
        return dataList;
    }

    private List<TextBookManagement> generateDataListByIdList(List<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds)) {
            Collections.emptyList();
        }
        List<TextBookManagement> dataList = new ArrayList<>();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIds);
        Set<String> authOnlineUserBookIds = generateAuthUserOnlineBookIds();
        Set<String> followReadBookIds = generateFollowReadBookIds();
        for (Map.Entry<String, NewBookProfile> book : bookProfileMap.entrySet()) {
            TextBookManagement textBookManagement = new TextBookManagement();
            textBookManagement.setBookId(book.getKey());
            Map<String, Object> extras = book.getValue().getExtras();
            if (SafeConverter.toInt(extras.get("nami")) == 1) {
                textBookManagement.setSourceType(TextBookSourceType.NAMI);
            } else {
                textBookManagement.setSourceType(TextBookSourceType.SELF_DEVELOP);
            }
            textBookManagement.setSubjectId(book.getValue().getSubjectId());
            if (StringUtils.isNotBlank(SafeConverter.toString(extras.get("ref_book_id")))) {
                textBookManagement.setRefBookId(SafeConverter.toString(extras.get("ref_book_id")));
            }
            //点读机
            TextBookManagement.PicListenConfig picListenConfig = new TextBookManagement.PicListenConfig();
            picListenConfig.setIsAndroidOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_android")));
            picListenConfig.setIsIOSOnline(SafeConverter.toBoolean(extras.get("has_pic_listen_ios")));
            picListenConfig.setIsFree(!SafeConverter.toBoolean(extras.get("has_pic_listen_pay")));
            if (authOnlineUserBookIds.contains(book.getKey())) {
                picListenConfig.setIsAuthUserOnline(Boolean.TRUE);
            } else {
                picListenConfig.setIsAuthUserOnline(Boolean.FALSE);
            }
            picListenConfig.setSdkBookId(extras.get("pic_listen_sdk_book_id") != null ? SafeConverter.toString(extras.get("pic_listen_sdk_book_id")) : "");
            picListenConfig.setIsPreview(SafeConverter.toBoolean(extras.get("pic_listen_preview")));
            textBookManagement.setPicListenConfig(picListenConfig);
            //随身听
            TextBookManagement.WalkManConfig walkManConfig = new TextBookManagement.WalkManConfig();
            if (extras.get("has_listen") != null && SafeConverter.toBoolean(extras.get("has_listen"))) {
                walkManConfig.setIsIOSOnline(Boolean.TRUE);
                walkManConfig.setIsAndroidOnline(Boolean.TRUE);
            } else {
                walkManConfig.setIsIOSOnline(Boolean.FALSE);
                walkManConfig.setIsAndroidOnline(Boolean.FALSE);
            }
            textBookManagement.setWalkManConfig(walkManConfig);
            //语文朗读
            if ((book.getValue().getSubjectId() == Subject.CHINESE.getId())) {
                TextBookManagement.TextReadConfig textReadConfig = new TextBookManagement.TextReadConfig();
                if (SafeConverter.toBoolean(extras.get("reading")) && SafeConverter.toInt(extras.get("nami")) != 1) {
                    textReadConfig.setIsAndroidOnline(Boolean.TRUE);
                    textReadConfig.setIsIOSOnline(Boolean.TRUE);
                } else {
                    textReadConfig.setIsAndroidOnline(Boolean.FALSE);
                    textReadConfig.setIsIOSOnline(Boolean.FALSE);
                }
                textBookManagement.setTextReadConfig(textReadConfig);
            }
            if (followReadBookIds.contains(book.getKey())) {
                textBookManagement.setIsFollowRead(Boolean.TRUE);
            } else {
                textBookManagement.setIsFollowRead(Boolean.FALSE);
            }
            if (book.getValue().getSubjectId() == Subject.ENGLISH.getId()) {
                textBookManagement.setHasWordList(Boolean.TRUE);
            } else {
                textBookManagement.setHasWordList(Boolean.FALSE);
            }
            if (book.getValue().getSubjectId() == Subject.CHINESE.getId() && SafeConverter.toInt(extras.get("nami")) != 1) {
                textBookManagement.setChineseWordSupport(Boolean.TRUE);
            } else {
                textBookManagement.setChineseWordSupport(Boolean.FALSE);
            }
            textBookManagement.setOperateUser(getCurrentAdminUser().getAdminUserName());
            dataList.add(textBookManagement);
        }
        return dataList;
    }

    private void generateAndSendEmail(String bookId, String bookName, String publishName, String updateTime, String userName, String content) {
        if (!RuntimeMode.isProduction())
            return;
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(updateTime)) {
            return;
        }

        String emailConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), PICLISTENBOOK_EDIT_EMAIL_LIST);
        if (StringUtils.isEmpty(emailConfig)) {
            return;
        }

        String[] emailArray = StringUtils.split(emailConfig, ",");
        List<String> emailList = Arrays.asList(emailArray);
        if (CollectionUtils.isEmpty(emailList)) {
            return;
        }
        PlainEmailCreator plainEmailCreator = new PlainEmailCreator(emailService);
        String emailBodyPattern = "点读机教材操作通知：\n教材ID:{0}\n教材名称:{1}\n出版社名称:{2}\n操作内容:{3}\n操作时间:{4}\n操作人:{5}";
        String emailBody = MessageFormat.format(emailBodyPattern, bookId, bookName, publishName, content, updateTime, userName);
        plainEmailCreator.body(emailBody);
        plainEmailCreator.subject("点读机教材操作通知");
        emailList.forEach(e -> {
            plainEmailCreator.to(e + "@17zuoye.com");
            plainEmailCreator.send();
        });

    }
}
