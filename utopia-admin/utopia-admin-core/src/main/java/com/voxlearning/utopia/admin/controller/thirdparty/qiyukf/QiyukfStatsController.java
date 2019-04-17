package com.voxlearning.utopia.admin.controller.thirdparty.qiyukf;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.data.ExcelExportData;
import com.voxlearning.utopia.admin.support.WorkbookUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.crm.QiYuMobileRecord;
import com.voxlearning.utopia.entity.crm.QiYuSessionRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.crm.client.QiYuMobileRecordServiceClient;
import com.voxlearning.utopia.service.crm.client.QiYuSessionRecordServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/site/qiyukf/stats")
public class QiyukfStatsController extends CrmAbstractController {

    @Inject
    private QiYuSessionRecordServiceClient qiYuSessionRecordServiceClient;
    @Inject
    private QiYuMobileRecordServiceClient qiYuMobileRecordServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;


    @RequestMapping("/query.vpage")
    public String query(Model model) {
        long id = getRequestLong("sessionId");
        int page = getRequestInt("page", 0);
        int tab = getRequestInt("tab", 1);
        boolean isExport = getRequestBool("isExport"); // 是否导出操作
        if (tab == 1) {
            querySessionMessage(model, id, page, isExport);
        } else if (tab == 2) {
            queryMobileMessage(model, id, page, isExport);
        }

        model.addAttribute("tab", tab);
        return "/site/qiyukf/qiyukf_stats_list";
    }

    @RequestMapping("/queryMobile.vpage")
    @ResponseBody
    public MapMessage queryMobile() {
        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.successMessage();
        }
        return MapMessage.successMessage().add("mobile", SensitiveLib.decodeMobile(mobile));
    }

    private void querySessionMessage(Model model, Long id, int page, boolean isExport) {
        List<QiYuSessionRecord> records = new ArrayList<>();
        if (id > 0) {
            model.addAttribute("sessionId", id);
            QiYuSessionRecord record = qiYuSessionRecordServiceClient.getQiYuSessionRecordService().load(id);
            if (record != null) {
                records.add(record);
            }
        } else {
            String sessionTimeStart = getRequestString("sessionTimeStart");
            String sessionTimeEnd = getRequestString("sessionTimeEnd");
            boolean search = true;
            // 默认查前一天的
            Date date = DateUtils.stringToDate(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), DateUtils.FORMAT_SQL_DATE);
            Date endTime = DateUtils.addSeconds(date, -1);
            Date startTime = DateUtils.addDays(date, -1);
            if (StringUtils.isNoneBlank(sessionTimeStart, sessionTimeEnd)) {
                startTime = DateUtils.stringToDate(sessionTimeStart, DateUtils.FORMAT_SQL_DATE);
                endTime = DateUtils.stringToDate(sessionTimeEnd, DateUtils.FORMAT_SQL_DATE);
                if (startTime == null || endTime == null || startTime.after(endTime)) {
                    model.addAttribute("error", "查询时间格式错误");
                    search = false;
                } else {
                    endTime = DateUtils.addSeconds(DateUtils.addDays(endTime, 1), -1);
                }
            } else if (!(StringUtils.isBlank(sessionTimeStart) && StringUtils.isBlank(sessionTimeEnd))) {
                model.addAttribute("error", "查询时间格式错误");
                search = false;
            }
            if (search) {
                int pageSize = 20;
                if (isExport) {
                    if (DateUtils.dayDiff(endTime, startTime) > 31) {
                        model.addAttribute("error", "最多导出31天的数据");
                        return;
                    }
                    pageSize = 100000;
                    List<ExcelExportData> data = new ArrayList<>();
                    List<Field> fields = Arrays.stream(QiYuSessionRecord.class.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && sessionTitleMap.containsKey(f.getName())).collect(Collectors.toList());
                    String[] title = fields.stream().map(f -> sessionTitleMap.get(f.getName())).collect(Collectors.toList()).toArray(new String[]{});
                    while (startTime.before(endTime)) {
                        Date tmpEndTime = DateUtils.addDays(startTime, 15);
                        if (tmpEndTime.after(endTime)) {
                            tmpEndTime = endTime;
                        }
                        Page<QiYuSessionRecord> recordsPage = qiYuSessionRecordServiceClient.getQiYuSessionRecordService().query(startTime, tmpEndTime, 0, pageSize);
                        List<QiYuSessionRecord> content = recordsPage.getContent();
                        List<List<String>> dataList = content.stream()
                                .map(c -> fields.stream().map(f -> {
                                    try {
                                        f.setAccessible(true);
                                        Object value = f.get(c);
                                        if ((f.getName().equals("startTime") && SafeConverter.toLong(value) > 0)
                                                || (f.getName().equals("endTime") && SafeConverter.toLong(value) > 0)
                                                || (f.getName().equals("inQueueTime") && SafeConverter.toLong(value) > 0)) {
                                            return DateUtils.dateToString(new Date(SafeConverter.toLong(value)));
                                        }
                                        else if ((f.getName().equals("queueTime") && SafeConverter.toLong(c.getInQueueTime()) > 0)) {
                                            return SafeConverter.toString((SafeConverter.toLong(c.getStartTime()) - SafeConverter.toLong(c.getInQueueTime()))/1000, "");
                                        }
                                        // 手机号 导出的时候加密
                                        else if (f.getName().equals("mobile")) {
                                            String mobile = SensitiveLib.decodeMobile(SafeConverter.toString(value));
                                            if (StringUtils.isNotBlank(mobile)) {
                                                return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                                            }
                                            return mobile;
                                        } else if (SafeConverter.toLong(value) > 0 && f.getName().equals("visitRange")) {
                                            return SafeConverter.toString(SafeConverter.toLong(value) / 1000);
                                        } else if (f.getName().equals("evaluation") && value != null) {
                                            return ObjectUtils.get(() -> evaluationMap.get(SafeConverter.toInt(c.getEvaluationType())).get(SafeConverter.toInt(value)), "");
                                        } else if (f.getName().equals("relatedType") && value != null) {
                                            return relatedTypeMap.get(SafeConverter.toInt(value));
                                        } else if (f.getName().equals("interaction") && value != null) {
                                            return interactionMap.get(SafeConverter.toInt(value));
                                        } else if (f.getName().equals("closeReason") && value != null) {
                                            return closeReasonMap.get(SafeConverter.toInt(value));
                                        } else if (f.getName().equals("sType") && value != null) {
                                            return sTypeMap.get(SafeConverter.toInt(value));
                                        }
                                        return SafeConverter.toString(value, "");
                                    } catch (IllegalAccessException ignored) {
                                        return "";
                                    }
                                }).collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        startTime = tmpEndTime;
                        data.add(new ExcelExportData("在线数据", title, null, dataList, title.length));
                    }
                    export("session" + "-" + DateUtils.dateToString(startTime, DateUtils.FORMAT_SQL_DATE) + "-" + DateUtils.dateToString(endTime, DateUtils.FORMAT_SQL_DATE), data,
                            "");

                } else {
                    Page<QiYuSessionRecord> recordsPage = qiYuSessionRecordServiceClient.getQiYuSessionRecordService().query(startTime, endTime, page, pageSize);
                    records.addAll(recordsPage.getContent());
                    model.addAttribute("showCount", recordsPage.getTotalElements());
                    model.addAttribute("currentPage", recordsPage.getNumber());
                    model.addAttribute("totalPage", recordsPage.getTotalPages());
                    model.addAttribute("hasPrev", recordsPage.hasPrevious());
                    model.addAttribute("hasNext", recordsPage.hasNext());
                    model.addAttribute("sessionTimeStart", sessionTimeStart);
                    model.addAttribute("sessionTimeEnd", sessionTimeEnd);
                }
            }
        }
        model.addAttribute("records", records);
    }

    private void queryMobileMessage(Model model, Long id, int page, boolean isExport) {
        List<QiYuMobileRecord> records = new ArrayList<>();
        if (id > 0) {
            model.addAttribute("sessionId", id);
            QiYuMobileRecord record = qiYuMobileRecordServiceClient.getQiYuMobileRecordService().load(id);
            if (record != null) {
                records.add(record);
            }
        } else {
            String sessionTimeStart = getRequestString("sessionTimeStart");
            String sessionTimeEnd = getRequestString("sessionTimeEnd");
            String callOutNum = getRequestString("callOutNum");
            String callInNum = getRequestString("callInNum");
            boolean search = true;
            // 默认显示当天的内容
            Date endTime = new Date();
            Date startTime = DateUtils.stringToDate(DateUtils.dateToString(endTime, DateUtils.FORMAT_SQL_DATE), DateUtils.FORMAT_SQL_DATE);
            if (StringUtils.isNoneBlank(sessionTimeStart, sessionTimeEnd)) {
                startTime = DateUtils.stringToDate(sessionTimeStart, DateUtils.FORMAT_SQL_DATE);
                endTime = DateUtils.stringToDate(sessionTimeEnd, DateUtils.FORMAT_SQL_DATE);
                if (startTime == null || endTime == null || startTime.after(endTime)) {
                    model.addAttribute("error", "查询时间格式错误");
                    search = false;
                } else {
                    endTime = DateUtils.addSeconds(DateUtils.addDays(endTime, 1), -1);
                }
            } else if (!(StringUtils.isBlank(sessionTimeStart) && StringUtils.isBlank(sessionTimeEnd))) {
                model.addAttribute("error", "查询时间格式错误");
                search = false;
            }

            if (search) {
                int pageSize = 20;
                // 如果是导出 最多一次导出100000条
                if (isExport) {
                    if (DateUtils.dayDiff(endTime, startTime) > 31) {
                        model.addAttribute("error", "最多导出31天的数据");
                        return;
                    }
                    pageSize = 100000;
                    List<ExcelExportData> data = new ArrayList<>();
                    List<Field> fields = Arrays.stream(QiYuMobileRecord.class.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && mobileTitleMap.containsKey(f.getName())).collect(Collectors.toList());
                    String[] title = fields.stream().map(f -> mobileTitleMap.get(f.getName())).collect(Collectors.toList()).toArray(new String[]{});
                    while (startTime.before(endTime)) {
                        Date tmpEndTime = DateUtils.addDays(startTime, 15);
                        if (tmpEndTime.after(endTime)) {
                            tmpEndTime = endTime;
                        }
                        Page<QiYuMobileRecord> recordsPage = qiYuMobileRecordServiceClient.getQiYuMobileRecordService().query(startTime, tmpEndTime, callOutNum, callInNum, 0, pageSize);
                        List<QiYuMobileRecord> content = recordsPage.getContent();
                        startTime = tmpEndTime;
                        List<List<String>> dataList = content.stream()
                                .map(c -> fields.stream().map(f -> {
                                    try {
                                        f.setAccessible(true);
                                        Object value = f.get(c);
                                        if ((f.getName().equals("startTime") && SafeConverter.toLong(value) > 0)
                                                || (f.getName().equals("connectionStartTime") && SafeConverter.toLong(value) > 0)) {
                                            return DateUtils.dateToString(new Date(SafeConverter.toLong(value)));
                                        } else if (f.getName().equals("endTime")) {
                                            long duration = SafeConverter.toLong(c.getCallDuration());
                                            return DateUtils.dateToString(new Date(SafeConverter.toLong(c.getStartTime()) + (duration < 0 ? 0 : duration)));
                                        }
                                        // 手机号 导出的时候加密
                                        else if (f.getName().equals("callOutNum") || f.getName().equals("callInNum")) {
                                            String mobile = SensitiveLib.decodeMobile(SafeConverter.toString(value));
                                            if (StringUtils.isNotBlank(mobile)) {
                                                return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                                            }
                                            return mobile;
                                        } else if (StringUtils.containsAny(f.getName(), "waitingDuration", "callDuration")) {
                                            // 这个两个时间精确到秒
                                            long time = SafeConverter.toLong(value) / 1000;
                                            // 兼容负数
                                            return SafeConverter.toString(time < 0 ? 0 : time);
                                        } else if (f.getName().equals("status") && value != null) {
                                            return statusMap.get(SafeConverter.toInt(value));
                                        } else if (f.getName().equals("direction") && value != null) {
                                            return directionMap.get(SafeConverter.toInt(value));
                                        }
                                        return SafeConverter.toString(value, "");
                                    } catch (IllegalAccessException ignored) {
                                        return "";
                                    }
                                }).collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        data.add(new ExcelExportData("通话记录", title, null, dataList, title.length));
                    }
                    export("mobile" + "-" + DateUtils.dateToString(startTime, DateUtils.FORMAT_SQL_DATE) + "-" + DateUtils.dateToString(endTime, DateUtils.FORMAT_SQL_DATE), data,
                            "");
                } else {
                    Page<QiYuMobileRecord> recordsPage = qiYuMobileRecordServiceClient.getQiYuMobileRecordService().query(startTime, endTime, callOutNum, callInNum, page, pageSize);
                    records.addAll(recordsPage.getContent());
                    model.addAttribute("showCount", recordsPage.getTotalElements());
                    model.addAttribute("currentPage", recordsPage.getNumber());
                    model.addAttribute("totalPage", recordsPage.getTotalPages());
                    model.addAttribute("hasPrev", recordsPage.hasPrevious());
                    model.addAttribute("hasNext", recordsPage.hasNext());
                    model.addAttribute("sessionTimeStart", sessionTimeStart);
                    model.addAttribute("sessionTimeEnd", sessionTimeEnd);
                    model.addAttribute("callOutNum", callOutNum);
                    model.addAttribute("callInNum", callInNum);
                }
            }
        }
        model.addAttribute("records", records);
    }

    /**
     * 只导出一个sheet页
     * @param fileName
     * @param excelExportData
     * @param notice
     */
    private void export(String fileName, List<ExcelExportData> excelExportData, String notice) {
        try {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            int rowNum = 0;
            HSSFSheet sheet = null;
            for (ExcelExportData exportData : excelExportData) {
                int columns = exportData.getColumns();
                if (rowNum == 0 && sheet == null) {
                    sheet = hssfWorkbook.createSheet(exportData.getSheet());
                    sheet.setActive(true);
                    Row title = WorkbookUtils.createRow(sheet, rowNum++, columns, null);
                    for (int i = 0; i < columns; ++i) {
                        WorkbookUtils.setCellValue(title, i, null, exportData.getTitle()[i]);
                    }
                    // 说明
                    if (StringUtils.isNotBlank(notice)) {
                        HSSFCellStyle style = hssfWorkbook.createCellStyle();
                        HSSFFont font = hssfWorkbook.createFont();
                        font.setBold(true);
                        style.setFont(font);
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        style.setWrapText(true);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, columns));
                        Row noticeRow = WorkbookUtils.createRow(sheet, rowNum++, 0, style);
                        noticeRow.setHeightInPoints((short) 60);
                        WorkbookUtils.setCellValue(noticeRow, 0, style, notice);
                    }
                }
                // 数据处理
                for (List<String> line : exportData.getData()) {
                    Row row = WorkbookUtils.createRow(sheet, rowNum++, columns, null);
                    for (int i = 0; i < line.size(); ++i) {
                        WorkbookUtils.setCellValue(row, i, null, line.get(i));
                    }
                }
            }

            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            hssfWorkbook.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName + ".xls", "application/vnd.ms-excel", out.toByteArray());
        } catch (IOException e) {
            logger.error("Failed download {}", e);
        }
    }

    /**
     * 需要导出的字段
     */
    private static Map<String, String> sessionTitleMap = MapUtils.map(
            "id", "回话ID",
            "startTime", "会话开始时间",
            "endTime", "会话结束时间",
            "sType", "会话类型",
            "category", "会话分类",
            "evaluation", "满意度值",
            "evaluationType", "满意度类型",
            "evaluationRemark", "满意度评价内容",
            "relatedType", "关联会话类型",
            "relatedId", "被关联的会话",
            "interaction", "会话交互类型",
            "closeReason", "会话被关闭原因",
            "fromGroup", "会话来自分流组名",
            "fromStaff", "会话来自哪个客服",
            "inQueueTime", "排队开始时间点",
            "queueTime", "排队时长",
            "visitRange", "与上一次来访的时间差（单位-秒）",
            "vipLevel", "vip级别",
            "staffId", "客服",
            "staffName", "客服名字",
            "userId", "访客",
            "fromIp", "访客来源",
            "fromPage", "来源页",
            "fromTitle", "来源页标题",
            "fromType", "来源类型",
            "foreignId", "17id",
            "mobile", "手机号",
            "staffMessageNum", "客服发送消息的总数量",
            "userMessageNum", "访客发送消息的总数量",
            "replayAvgTime", "平均会话响应时间"
    );

    /**
     * 需要导出的字段
     */
    private static Map<String, String> mobileTitleMap = MapUtils.map(
            "sessionId", "会话ID",
            "startTime", "通话开始时间",
            "endTime", "通话结束时间",
            "connectionStartTime", "通话接通时间",
            "waitingDuration", "通话等待时长",
            "callDuration", "通话时长",
            "direction", "呼叫方向",
            "callOutNum", "被叫号码",
            "callInNum", "来电呼入号码",
            "status", "电话状态",
            "evaluation", "服务评价",
            "recordUrl", "录音地址",
            "staffId", "客服ID",
            "staffName", "客服姓名",
            "staffNum", "客服坐席号码"
    );

    /**
     * 满意度mapping表
     */
    private static Map<Integer, Map<Integer, String>> evaluationMap = MapUtils.map(
            2, MapUtils.map(100, "满意", 1, "不满意"),
            3, MapUtils.map(100, "满意", 50, "一般", 1, "不满意"),
            5, MapUtils.map(100, "非常满意", 75, "满意", 50, "一般", 25, "不满意", 1, "非常不满意"),
            0, MapUtils.map()
    );

    /**
     * 关联会话类型mapping
     * 0=无关联  1=从机器人转接过来   2=机器人会话转接人工   3=历史会话发起   4=客服间转接    5=被接管
     */
    private static Map<Integer, String> relatedTypeMap = MapUtils.map(
            0, "无关联", 1, "从机器人转接过来", 2, "机器人会话转接人工", 3, "历史会话发起", 4, "客服间转接", 5, "被接管"
    );

    /**
     * 会话交互类型mapping
     * 0=客服正常会话  1=机器人会话  2=呼叫中心会话   3=推送消息
     */
    private static Map<Integer, String> interactionMap = MapUtils.map(
            0, "客服正常会话", 1, "机器人会话", 2, "呼叫中心会话", 3, "推送消息"
    );

    /**
     * 会话关闭原因mapping
     * 0= 客服关闭  1=用户离开  2=用户不说话 自动关闭了 3=机器人会话转接到人工 4=客服离开 5=客服主动将会话转出  6=管理员强势接管会话，或访客再次申请其他客服   7=访客关闭
     */
    private static Map<Integer, String> closeReasonMap = MapUtils.map(
            0, "客服关闭", 1, "用户离开", 2, "用户不说话 自动关闭了", 3, "推送消息"
    );

    /**
     * 会话类型mapping
     * 0=正常会话.1(2)=离线  留言，3=排队超时
     */
    private static Map<Integer, String> sTypeMap = MapUtils.map(
            0, "正常会话", 1, "离线", 2, "留言", 3, "排队超时"
    );

    /**
     * 会话类型mapping
     * 0-客服未接听，1-接通，2-外呼未接通（占线），3-外呼未接通（无法接通），4-外呼未接通（无人应答），5-访客 IVR 中放弃，6-访客队列中放弃，7-访客排队超时，8-客服不在服务时间，
     * 9-无客服在线，10-电话转接成功
     */
    private static Map<Integer, String> statusMap = MapUtils.map(
            0, "客服未接听", 1, "接通", 2, "外呼未接通（占线）",
            3, "外呼未接通（无法接通）", 4 , "外呼未接通（无人应答）", 5, "访客IVR中放弃",
            6, "访客队列中放弃", 7 ,"访客排队超时" , 8, "客服不在服务时间", 9, "无客服在线" , 10, "电话转接成功"
    );
    /**
     * 呼叫方向 1-呼入（客服为被叫），2-呼出（客服为主叫）
     */
    private static Map<Integer, String> directionMap = MapUtils.map(
            1, "呼入（客服为被叫）", 2, "呼出（客服为主叫）"
    );
}
