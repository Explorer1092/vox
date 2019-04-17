/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.utopia.admin.util.HssfUtils;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微信消息审核
 * Created by Shuai Huan on 2015/3/26.
 */
@Controller
@RequestMapping("/site/wechatmessage")
public class SiteWechatMessageController extends SiteAbstractController {

    @Inject private WechatServiceClient wechatServiceClient;

    @RequestMapping(value = "messagehome.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String messageHome() {
        return "site/wechatmessage/messagehome";
    }

    @RequestMapping(value = "messagelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage messageList() {
        Integer type = getRequestInt("type");
        List<Map<String, Object>> notices = wechatServiceClient.loadNoticeByMessageTypeForCrm(type);
        return MapMessage.successMessage().add("notices", notices);
    }

    @RequestMapping(value = "update.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateMessageState() {
        Integer type = getRequestInt("type");
        wechatServiceClient.updateMessageStateByType(type);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteMessageState() {
        Integer type = getRequestInt("type");
        wechatServiceClient.deleteMessageStateByType(type);
        return MapMessage.successMessage();
    }

    //-------------------------------------------------------------------------------------------------
    //----------------------------       发送微信消息 By Wyc 2016-06-15          -----------------------
    //-------------------------------------------------------------------------------------------------
    @RequestMapping(value = "sendwechatmsg.vpage", method = RequestMethod.GET)
    public String sendMsgIndex(Model model) {
        return "site/wechatmessage/sendwechatmsg";
    }

    @RequestMapping(value = "sendwechatmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendWechatMsg(Model model) {
        int wt = getRequestInt("wechatType", -1);
        WechatType wechatType = WechatType.of(wt);
        String firstInfo = getRequestString("first");
        String keyword1 = getRequestString("k1");
        String keyword2 = getRequestString("k2");
        String remark = getRequestString("remark");
        String url = getRequestString("url");
        String sendTime = getRequestString("sendTime");
        String isUstalk = getRequestString("isUstalk");
        String isWkt = getRequestString("isWkt");
        String userIdsFrom = getRequestString("userIdsFrom");
        List<Map<String, Object>> usersToSend = new ArrayList<>();
        if (StringUtils.equals(userIdsFrom, "excel")) {
            //todo:读取excel文件，并解析
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            try {
                MultipartFile inputFile = multipartRequest.getFile("userIds");
                if (!inputFile.isEmpty()) {
                    // 获取文件类型
                    String originalFileName = inputFile.getOriginalFilename();
                    String ext = StringUtils.substringAfterLast(originalFileName, ".");
                    ext = StringUtils.defaultString(ext).trim().toLowerCase();
                    // 根据后缀名来判断文件类型

                    if (StringUtils.equals("xls", ext)) {
                        HSSFWorkbook workbook = new HSSFWorkbook(inputFile.getInputStream());
                        HSSFSheet sheet = workbook.getSheetAt(0);
                        int rowIndex = 0;
                        while (true) {
                            HSSFRow row = sheet.getRow(rowIndex);
                            if (row == null) {
                                break;
                            }
                            Long uid = HssfUtils.getLongCellValue(row.getCell(0));
                            Date st = row.getCell(1).getDateCellValue();
                            if (uid == null || st == null) break;
                            usersToSend.add(MiscUtils.m("uid", uid, "sendTime", st));
                            rowIndex++;
                        }
                    } else {
                        XSSFWorkbook workbook = new XSSFWorkbook(inputFile.getInputStream());
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        int rowIndex = 0;
                        while (true) {
                            XSSFRow row = sheet.getRow(rowIndex);
                            if (row == null) {
                                break;
                            }
                            Long uid = XssfUtils.getLongCellValue(row.getCell(0));
                            Date st = row.getCell(1).getDateCellValue();
                            if (uid == null || st == null) break;
                            usersToSend.add(MiscUtils.m("uid", uid, "sendTime", st));
                            rowIndex++;
                        }
                    }
                }
            } catch (Exception ex) {
                return MapMessage.errorMessage("解析excel失败");
            }
        } else {
            String userId = getRequestString("userId").replaceAll("\r", "");
            Set<Long> userIdList = Arrays.asList(userId.split("\n"))
                    .stream()
                    .map(SafeConverter::toLong)
                    .filter(id -> id > 0L)
                    .collect(Collectors.toSet());
            Date send = new Date();
            if (StringUtils.isNotBlank(sendTime)) {
                send = DateUtils.stringToDate(sendTime);
            }
            for (Long u : userIdList) {
                usersToSend.add(MiscUtils.m("uid", u, "sendTime", send));
            }
        }

        StringBuilder errMsg = new StringBuilder();
        if (wechatType == null || (wechatType != WechatType.PARENT && wechatType != WechatType.TEACHER)) {
            errMsg.append("<br>请选择正确的消息发送端");
        }
//        if (StringUtils.isBlank(firstInfo)) {
//            errMsg.append("<br>请填写【消息主题】");
//        }
        if (StringUtils.isBlank(keyword1) && StringUtils.isBlank(keyword2)) {
            errMsg.append("<br>Keyword请至少填写一项");
        }
//        if (StringUtils.isBlank(remark)) {
//            errMsg.append("<br>请填写【备注】");
//        }
        if (CollectionUtils.isEmpty(usersToSend)) {
            errMsg.append("<br>请填写正确的【用户ID】");
        }
        if (errMsg.length() > 0) {
            model.addAttribute("error", errMsg.toString());
            return MapMessage.errorMessage(errMsg.toString());
        }

        WechatNoticeProcessorType noticeType;
        if (wechatType == WechatType.PARENT) {
            noticeType = WechatNoticeProcessorType.ParentOperationalNotice;
        } else if (wechatType == WechatType.TEACHER) {
            noticeType = WechatNoticeProcessorType.TeacherOperationNotice;
        } else {
            model.addAttribute("error", "请选择正确的消息发送端");
            return MapMessage.errorMessage("请选择正确的消息发送端！");
        }
        Map<String, Object> extensionInfo = MiscUtils.m(
                "first", firstInfo,
                "keyword1", keyword1,
                "keyword2", keyword2,
                "remark", remark,
                "url", url,
                "isWkt", isWkt
        );

        // generate batch no
        String batchNo = RandomStringUtils.randomAlphabetic(10);
        batchSendPromotWechatMessage(usersToSend, extensionInfo, url, noticeType, wechatType, isUstalk, batchNo);
        return MapMessage.successMessage().add("batchNo", batchNo);
    }

    //TODO:上传excel


    private void batchSendPromotWechatMessage(List<Map<String, Object>> usersToSend, Map<String, Object> extensionInfo, String url, WechatNoticeProcessorType noticeType, WechatType wechatType, String isUstalk, String batchNo) {
        AlpsThreadPool.getInstance().submit(() -> {
            if (StringUtils.equals(isUstalk, "ustalk")) {
                for (Map<String, Object> userToSend : usersToSend) {
                    // 处理URL中的userId参数
                    Long uid = SafeConverter.toLong(userToSend.get("uid"));
                    // 处理URL中的userId参数
                    if (StringUtils.isNoneBlank(url) && url.contains("#userId#")) {
                        extensionInfo.put("url", url.replace("#userId#", String.valueOf(uid)));
                    }
                    // bugme:ustalk为了提高用户微信消息的打开率，需要在文案上加上孩子的名字，并给某些字段加色，着重显示
                    List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(uid);
                    long studentId;
                    String studentName;
                    if (studentParentRefs.size() > 0) {
                        // 有孩子，注入到extensionInfo中去
                        studentId = studentParentRefs.get(0).getStudentId();
                        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                        studentName = studentDetail.getProfile().getRealname();

                    } else {
                        studentId = 0;
                        studentName = "";
                    }
                    extensionInfo.put("studentName", studentName);
                    extensionInfo.put("studentId", studentId);
                    extensionInfo.put("type", "ustalkpromot");
                    // 文言中替换studentName
                    extensionInfo.put("first", SafeConverter.toString(extensionInfo.get("first")).replace("#studentName#", studentName));
                    extensionInfo.put("keyword1", SafeConverter.toString(extensionInfo.get("keyword1")).replace("#studentName#", studentName));
                    extensionInfo.put("keyword2", SafeConverter.toString(extensionInfo.get("keyword2")).replace("#studentName#", studentName));
                    extensionInfo.put("remark", SafeConverter.toString(extensionInfo.get("remark")).replace("#studentName#", studentName));
                    extensionInfo.put("sendTime", userToSend.get("sendTime"));
                    wechatServiceClient.processWechatNotice(noticeType, uid, extensionInfo, wechatType);
                }
            } else {
                for (Map<String, Object> userToSend : usersToSend) {
                    // 处理URL中的userId参数
                    Long uid = SafeConverter.toLong(userToSend.get("uid"));
                    if (StringUtils.isNoneBlank(url) && url.contains("#userId#")) {
                        extensionInfo.put("url", url.replace("#userId#", String.valueOf(uid)));
                    }
                    extensionInfo.put("sendTime", userToSend.get("sendTime"));
                    wechatServiceClient.processWechatNotice(noticeType, uid, extensionInfo, wechatType);
                }
            }
        });
    }
}
