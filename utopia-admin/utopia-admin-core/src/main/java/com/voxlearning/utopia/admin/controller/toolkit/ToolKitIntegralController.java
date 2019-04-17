/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.constants.BusinessType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Cleanup;
import lombok.Getter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午1:52,13-11-14.
 */
@Controller
@RequestMapping("/toolkit/integral")
public class ToolKitIntegralController extends ToolKitAbstractController {

    //按用户id导入积分数据
    public static final int IMPORT_DATA_BY_USER_ID = 4;
    //按手机号导入积分数据
    public static final int IMPORT_DATA_BY_USER_PHONE = 5;

//    public static final  String INTEGRAL_ERROR_RECORD_PATH = "/config/integral_error_record.xlsx";

    @Getter List<StringBuffer> lstFailed = new ArrayList<>();

    //跳向批量添加积分页面
    @RequestMapping(value = "tobatchaddintegral.vpage", method = RequestMethod.GET)
    public String batchGenAmbassador() {
        return "site/batch/batchaddintegral";
    }

    /**
     * 后台管理批量导入添加积分的名单
     */
    @RequestMapping(value = "batchaddintegral.vpage", method = RequestMethod.POST)
    public String batchAddIntegral(Model model) {
        String content = getRequestString("batchAddIntegralContext");
        if (StringUtils.isBlank(content)) {
            getAlertMessageManager().addMessageError("请输入添加积分的名单");
            return "/site/batch/batchaddintegral";
        }
        lstFailed.clear();
        String[] contents = content.split("\\r\\n");

        List<String> lstSuccess = new ArrayList<>();

        int totalRecord = contents.length;

        for (String m : contents) {

            String[] contextArray = m.trim().split("[\\s]+");
            int contextArrayLen = contextArray.length;
            if (contextArrayLen < IMPORT_DATA_BY_USER_ID || contextArrayLen > IMPORT_DATA_BY_USER_PHONE) {
                StringBuffer errorMessage = new StringBuffer(m).append(" 存在错误数据或不完整数据");
                lstFailed.add(errorMessage);
                continue;
            }

            //按用户ID导入数据
            if (contextArrayLen == IMPORT_DATA_BY_USER_ID) {
                if (importIntegralDataByUserId(lstFailed, m, contextArray)) continue;
            } else if (contextArrayLen == IMPORT_DATA_BY_USER_PHONE) {
                //按手机号导入用户数据
                if (importIntegralDataByUserphone(lstFailed, m, contextArray)) continue;
            }

            final IntegralHistory integralHistory = new IntegralHistory(
                    Long.valueOf(contextArray[0]),
                    Integer.valueOf(StringUtils.trim(contextArray[2])),
                    Integer.valueOf(StringUtils.trim(contextArray[1]))
            );

            Long adminUserId = getCurrentAdminUser().getFakeUserId();
            integralHistory.setAddIntegralUserId(adminUserId);
            integralHistory.setComment(contextArray[3]);
            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
            if (!msg.isSuccess()) {
                StringBuffer errorMessage = new StringBuffer(m).append(" 存在错误数据或不完整数据");
                lstFailed.add(errorMessage);
            } else {
                lstSuccess.add(m);
            }
            asyncFootprintServiceClient.createBusinessLog(BusinessType.SERVICE, adminUserId, "成功导入{}条积分明细", contextArray.length / 4);
        }


        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        model.addAttribute("totalRecord", totalRecord);
        return "/site/batch/batchaddintegral";
    }

    @RequestMapping(value = "exportErrorData.vpage")
    public void exportErrorData() {

        XSSFWorkbook workbook = getXssfSheets();

        try {
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("errorMessage.xlsx", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("TaskRecord export Excp : {};", e);
        }

    }

    private XSSFWorkbook getXssfSheets() {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = sheet.createRow(0);

        firstRow.createCell(0).setCellValue("用户id/用户手机号");
        firstRow.createCell(1).setCellValue("积分数量");
        firstRow.createCell(2).setCellValue("积分类型");
        firstRow.createCell(3).setCellValue("备注");
        firstRow.createCell(4).setCellValue("用户类型");
        firstRow.createCell(5).setCellValue("错误信息");

        int rowNum = 1;


        for (StringBuffer errorMessage : lstFailed) {

            String[] errors = errorMessage.toString().split("\\s");
            XSSFRow row = sheet.createRow(rowNum++);

            int size = errors.length;
            for (int i = 0; i < size; i++) {
                row.createCell(i).setCellValue(errors[i]);
            }
        }
        return workbook;
    }


    //按手机号导入
    private boolean importIntegralDataByUserphone(List<StringBuffer> lstFailed, String m, String[] contextArray) {
        //用户类型
        String userMobile = StringUtils.deleteWhitespace(contextArray[0]);
        if (!MobileRule.isMobile(userMobile)) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 手机号错误，请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (!NumberUtils.isNumber(StringUtils.trim(contextArray[1]))) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 积分数量错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (!NumberUtils.isNumber(StringUtils.trim(contextArray[2]))) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 积分类型错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        String userType = StringUtils.deleteWhitespace(contextArray[4]);
        if (!NumberUtils.isNumber(userType)) {
            StringBuffer errorMessage = new StringBuffer(m).append(" 用户类型错误，请核实");
            lstFailed.add(errorMessage);
            return true;
        } else {
            // 手机号查询
            List<User> userList = userLoaderClient.loadUsers(userMobile, UserType.of(Integer.valueOf(userType)));
            if (CollectionUtils.isEmpty(userList)) {
                StringBuffer errorMessage = new StringBuffer(m).append(" 不存在该手机号的用户或手机号错误，请核实");
                lstFailed.add(errorMessage);
                return true;
            } else {
                contextArray[0] = String.valueOf(userList.stream().map(User::getId).collect(Collectors.toList()).get(0));
            }
        }
        return false;
    }

    //按照用户ID导入积分数据
    private boolean importIntegralDataByUserId(List<StringBuffer> lstFailed, String m, String[] contextArray) {
        if (!NumberUtils.isNumber(contextArray[0])) {
            StringBuffer errorMessage = new StringBuffer(m).append("  该用户id不正确,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (!NumberUtils.isNumber(StringUtils.trim(contextArray[1]))) {
            StringBuffer errorMessage = new StringBuffer(m).append("  积分数量错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        if (!NumberUtils.isNumber(StringUtils.trim(contextArray[2]))) {
            StringBuffer errorMessage = new StringBuffer(m).append("  积分类型错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        User user = userLoaderClient.loadUser(Long.valueOf(contextArray[0]));
        if (user == null) {
            StringBuffer errorMessage = new StringBuffer(m).append("  不存在此ID的用户或提交信息错误,请核实");
            lstFailed.add(errorMessage);
            return true;
        }
        return false;
    }
}
