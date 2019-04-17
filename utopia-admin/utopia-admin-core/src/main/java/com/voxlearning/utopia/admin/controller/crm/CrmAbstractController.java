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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.ExcelExportData;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherService;
import com.voxlearning.utopia.admin.service.crm.CrmUserService;
import com.voxlearning.utopia.admin.support.SessionUtils;
import com.voxlearning.utopia.admin.support.WorkbookUtils;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.UserFeedbackForEmail;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkCrmService;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.ResearchStaffManagedRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.service.SeiueSyncDataService;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Longlong Yu
 * @since 下午12:41,13-6-8.
 */
abstract public class CrmAbstractController extends AbstractAdminSystemController {

    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject protected SchoolLoaderClient schoolLoaderClient;
    @Inject protected SchoolExtServiceClient schoolExtServiceClient;
    @Inject protected SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject protected NewKuailexueServiceClient newKuailexueServiceClient;
    @ImportService(interfaceClass = SeiueSyncDataService.class)
    protected SeiueSyncDataService seiueSyncDataService;
    @ImportService(interfaceClass = NewHomeworkCrmService.class)
    protected NewHomeworkCrmService newHomeworkCrmService;
    @Inject
    protected CrmImageUploader crmImageUploader;

    /**
     * services
     */
    @Resource protected CrmTeacherService crmTeacherService;
    @Resource protected CrmUserService crmUserService;

    public static List<UserFeedbackForEmail> transform(List<UserFeedback> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.emptyList();
        }
        List<UserFeedbackForEmail> target = new LinkedList<>();
        for (UserFeedback src : source) {
            if (src == null) {
                continue;
            }
            UserFeedbackForEmail tgt = new UserFeedbackForEmail();
            tgt.setId(src.getId());
            tgt.setContent(src.getContent());
            tgt.setUserType(src.getUserType());
            tgt.setRealName(src.getRealName());
            tgt.setUserId(src.getUserId());
            tgt.setAddress(src.getAddress());
            tgt.setContactQq(src.getContactSensitiveQq());
            tgt.setContactPhone(src.getContactSensitivePhone());
            tgt.setTag(src.getTag());
            target.add(tgt);
        }
        return target;
    }

    protected Map<String, String> passwordChangeTrackMap(Long userId, String pos) {
        Map<String, String> map = new HashMap<>();
        map.put("user", SafeConverter.toString(userId));
        map.put("operator", getCurrentAdminUser().getAdminUserName());
        map.put("date", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
        map.put("pos", pos);
        map.put("env", RuntimeMode.current().name());
        return map;
    }

    protected String juniorCrmAdminUrlBase() {
        String host = "zx-admin.17zuoye.net";
        if (RuntimeMode.isProduction()) {
            host = "zx-admin.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            host = "zx-admin.staging.17zuoye.net";
        } else if (RuntimeMode.isTest()) {
            host = "zx-admin.test.17zuoye.net";
        } else if (RuntimeMode.isDevelopment()) {
            host = "local.zx-admin.17zuoye.net";
        }

        return "http://" + host;
    }

    protected String getPrePath() {
        String prePath = "http://www.test.17zuoye.net" + "/gridfs/";
        if (RuntimeMode.isProduction()) {
            prePath = "http://www.17zuoye.com" + "/gridfs/";
        } else if (RuntimeMode.isStaging()) {
            prePath = "http://www.staging.17zuoye.net" + "/gridfs/";
        }

        return prePath;
    }

    protected void updateUserAppSessionKey(Long uid, String appKey) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef(appKey, uid);
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    appKey,
                    uid,
                    SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), uid));
        }
    }

    /**
     * 通过 CommonConfig 获取学校的映射关系
     * 测试 : 陈经纶中学(高中部)(414008)
     * 线上 : 陈经纶中学(高中部)(405492)
     */
    boolean isCJLSchool(Long schoolId) {
        if (schoolId == null) {
            return false;
        }
        try {
            String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                    ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
            );

            Map<String, Long> schoolIdMap = new HashMap<>();
            Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
                String[] split = pair.split(":");
                schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
            });
            return !schoolIdMap.isEmpty() && schoolIdMap.containsValue(schoolId);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 获取上传的Excel文件
     *
     * @param name 上传文件名
     */
    Workbook getRequestWorkbook(String name) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            logger.error("getRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest request = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile file = request.getFile(name);
            if (file == null || file.isEmpty()) {
                logger.error("getRequestWorkbook - Empty MultipartFile with name['{}']", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("getRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return WorkbookFactory.create(in);
        } catch (Exception e) {
            logger.error("SpecialTeacherController getRequestWorkbook - Excp : {}", e);
            return null;
        }
    }


    /**
     * 初步检查EXCEL
     *
     * @param workbook excel文件
     * @param maximum  最大行数，默认100
     */
    MapMessage checkWorkbook(Workbook workbook, int maximum) {
        if (workbook == null) {
            return MapMessage.errorMessage("文档内容为空");
        }
        // 默认取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("文档内容为空");
        }
        // 默认第一行都是title
        if (sheet.getLastRowNum() < 1) {
            return MapMessage.errorMessage("文档内容为空");
        }
        if (sheet.getLastRowNum() > Integer.max(100, maximum)) {
            return MapMessage.errorMessage("文档人数异常，最多不超过" + maximum + "人");
        }
        return MapMessage.successMessage();
    }

    /**
     * 根据下载内容生成要下载的Excel文件，仅支持 xls
     */
    protected HSSFWorkbook createXlsExcelExportData(List<ExcelExportData> excelExportData) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        // 设置单元格边框样式
        borderStyle.setBorderBottom(CellStyle.BORDER_THIN);
        borderStyle.setBorderTop(CellStyle.BORDER_THIN);
        borderStyle.setBorderLeft(CellStyle.BORDER_THIN);
        borderStyle.setBorderRight(CellStyle.BORDER_THIN);

        for (ExcelExportData exportData : excelExportData) {
            HSSFSheet sheet = hssfWorkbook.createSheet(exportData.getSheet());
            sheet.setActive(true);

            int rowNum = 0;
            int columns = exportData.getColumns();
            // 表头处理
            Row title = WorkbookUtils.createRow(sheet, rowNum++, columns, borderStyle);
            for (int i = 0; i < columns; ++i) {
                sheet.setColumnWidth(i, exportData.getWidth()[i]);
                if (exportData.highlight(i)) {
                    HSSFCellStyle newStyle = hssfWorkbook.createCellStyle();
                    // 设置单元格边框样式
                    newStyle.setBorderBottom(CellStyle.BORDER_THIN);
                    newStyle.setBorderTop(CellStyle.BORDER_THIN);
                    newStyle.setBorderLeft(CellStyle.BORDER_THIN);
                    newStyle.setBorderRight(CellStyle.BORDER_THIN);
                    newStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                    newStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    WorkbookUtils.setCellValue(title, i, newStyle, exportData.getTitle()[i]);
                } else {
                    WorkbookUtils.setCellValue(title, i, borderStyle, exportData.getTitle()[i]);
                }
            }

            // 数据处理
            for (List<String> line : exportData.getData()) {
                Row row = WorkbookUtils.createRow(sheet, rowNum++, columns, borderStyle);
                for (int i = 0; i < columns; ++i) {
                    WorkbookUtils.setCellValue(row, i, borderStyle, line.get(i));
                }
            }
        }
        return hssfWorkbook;
    }

    protected void write(InputStream in, OutputStream out) throws Exception {
        int BUFFER_SIZE = 1024 * 8;
        byte[] buffer = new byte[BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    /**
     * 记录 UserServiceRecord
     */
    void logUserServiceRecord(Long userId, String operation, UserServiceRecordOperationType operationType, String content) {
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setComments(operation);
        userServiceRecord.setOperationContent(content);
        userServiceRecord.setOperationType(operationType.name());

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }

    /**
     * 希悦平台学校
     */
    boolean isSeiueSchool(Long schoolId) {
        return Objects.equals(seiueSyncDataService.loadSchoolId(), schoolId);
    }
}
