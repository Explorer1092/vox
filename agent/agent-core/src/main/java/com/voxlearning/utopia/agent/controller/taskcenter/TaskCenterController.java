package com.voxlearning.utopia.agent.controller.taskcenter;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentTaskType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskMain;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskSubIntoSchoolExportData;
import com.voxlearning.utopia.agent.persist.entity.task.AgentTaskSubOnlineExportData;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;


/**
 * @author deliang.che
 * @since 2018-05-25
 */
@Controller
@RequestMapping(value = "/taskcenter/maintainteacher")
public class TaskCenterController extends AbstractAgentController {
    private final static String IMPORT_TASK_CENTER_TEMPLATE = "/config/templates/import_task_center_template.xlsx";
    @Inject
    private AgentTaskCenterService agentTaskCenterService;
    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;

    @RequestMapping("task_list_page.vpage")
    @OperationCode("d41846e1714743cc")
    public String maintainTeacherPage(){
        return "taskcenter/maintain_teacher/list";
    }

    @RequestMapping("task_edit_page.vpage")
    public String maintainTeacherEditPage(){
        return "taskcenter/maintain_teacher/edit";
    }

    @RequestMapping("task_add_page.vpage")
    public String maintainTeacherAddPage(){
        return "taskcenter/maintain_teacher/add";
    }

    @RequestMapping("task_detail_page.vpage")
    public String maintainTeacherDetailPage(){
        return "taskcenter/maintain_teacher/detail";
    }

    @RequestMapping("task_change_page.vpage")
    public String maintainTeacherChangePage(){
        return "taskcenter/maintain_teacher/change";
    }

    /**
     * 任务列表
     * @return
     */
    @RequestMapping(value = "main_task_list.vpage")
    @ResponseBody
    public MapMessage taskList(){
        List<AgentTaskMain> agentTaskMainList = agentTaskCenterService.getAllMainTask();
        return MapMessage.successMessage().add("dataList",agentTaskCenterService.toMainTaskListMap(agentTaskMainList));
    }

    /**
     * 任务下载模板
     */
    @RequestMapping(value = "task_import_template.vpage")
    public void importTemplate(){
        try {
            baseExcelService.downloadTemplate(IMPORT_TASK_CENTER_TEMPLATE, "任务导入模版");
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    /**
     * 任务新增
     * @return
     */
    @RequestMapping(value = "task_add.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage taskAdd(){
        AuthCurrentUser currentUser = getCurrentUser();
        String title = requestString("title");
        AgentTaskType taskType = AgentTaskType.nameOf(requestString("taskType"));
        Date endTime = requestDate("endTime","yyyy-MM-dd HH:mm");
        String comment = requestString("comment");
        if (endTime.before(new Date())){
            return MapMessage.errorMessage("任务截止时间不得早于创建时间");
        }
        AgentTaskMain agentTaskMain = new AgentTaskMain();
        agentTaskMain.setTitle(title);
        agentTaskMain.setTaskType(taskType);
        agentTaskMain.setEndTime(endTime);
        agentTaskMain.setComment(comment);
        agentTaskMain.setUserId(currentUser.getUserId());
        agentTaskMain.setUserName(currentUser.getRealName());
        agentTaskMain.setDisabled(false);

        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage();
        }
        MapMessage mapMessage = agentTaskCenterService.importTask(workbook,taskType,agentTaskMain);
        if (!mapMessage.isSuccess()){
            return mapMessage;
        }

        return MapMessage.successMessage();
    }

    /**
     *主任务详情
     * @return
     */
    @RequestMapping(value = "main_task_detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage taskDetail(){
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(id);
        if (null == agentTaskMain){
            return MapMessage.errorMessage("该任务不存在");
        }
        return MapMessage.successMessage().add("data",agentTaskCenterService.toMainTaskMap(agentTaskMain));
    }

    /**
     * 主任务编辑
     * @return
     */
    @RequestMapping(value = "main_task_update.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage taskUpdate(){
        String id = requestString("id");
        String title = requestString("title");
        Date endTime = requestDate("endTime","yyyy-MM-dd HH:mm");
        String comment = requestString("comment");
        Date currentDate = new Date();
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(id);
        if (null == agentTaskMain){
            return MapMessage.errorMessage("该任务不存在");
        }
        //如果任务已经截止，不可修改截止时间
        if (agentTaskMain.getEndTime().before(currentDate)){
            return MapMessage.errorMessage("该任务已截止，不可修改截止时间");
        }
        //修改后的截止时间不得早于当前时间
        if (endTime.before(currentDate)){
            return MapMessage.errorMessage("截止时间不得早于当前时间");
        }
        agentTaskMain.setTitle(title);
        agentTaskMain.setEndTime(endTime);
        agentTaskMain.setComment(comment);
        agentTaskCenterService.updateMainTask(agentTaskMain);
        return MapMessage.successMessage();
    }

    /**
     * 删除任务
     * @return
     */
    @RequestMapping(value = "task_delete.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage taskDelete(){
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(id);
        if (null == agentTaskMain){
            return MapMessage.errorMessage("该任务不存在");
        }
        agentTaskCenterService.deleteTask(id);
        return MapMessage.successMessage();
    }

    /**
     * 搜索执行人
     * @return
     */
    @RequestMapping(value = "search_operator.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchOperator(){
        String userKey = getRequestString("userKey");
        if (StringUtils.isEmpty(userKey)){
            return MapMessage.errorMessage("检索词为空");
        }
        long userId = SafeConverter.toLong(userKey);
        List<AgentUser> userList = new ArrayList<>();
        if (userId > 0){
            AgentUser user = baseOrgService.getUser(userId);
            userList.add(user);
        }else {
            List<AgentUser> agentUserList = baseOrgService.findUserByRealName(userKey);
            userList.addAll(agentUserList);
        }
        List<Map<String,Object>> dataList = new ArrayList<>();
        userList.forEach(item -> {
            Map<String,Object> dataMap = new HashMap<>();
            if (null != item){
                dataMap.put("userId",item.getId());
                dataMap.put("userName",item.getRealName());
                AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(item.getId()).stream().findFirst().orElse(null);
                if (null != groupUser){
                    AgentGroup group = baseOrgService.getGroupById(groupUser.getGroupId());
                    if (null != group){
                        dataMap.put("groupName",group.getGroupName());
                    }
                }
            }
            dataList.add(dataMap);
        });
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 搜索子任务信息
     * @return
     */
    @RequestMapping(value = "search_sub_task.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchSubTask(){
        String mainTaskId = requestString("mainTaskId");
        Long operatorId = requestLong("operatorId");
        if (StringUtils.isBlank(mainTaskId)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(mainTaskId);
        if (null == agentTaskMain){
            return MapMessage.errorMessage("该任务不存在");
        }
        List<Map<String,Object>> dataList = new ArrayList<>();
        dataList = agentTaskCenterService.getTaskSubByCondition(mainTaskId,operatorId);
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 修改执行人
     * @return
     */
    @RequestMapping(value = "update_operator.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage updateOperator(){

        String mainTaskId = requestString("mainTaskId");
        Long operatorId = requestLong("operatorId");
        String selectIdSr = requestString("selectIdStr");
        Date currentDate = new Date();
        if (StringUtils.isBlank(mainTaskId)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        if (null == operatorId){
            return MapMessage.errorMessage("执行人ID不正确");
        }
        if (StringUtils.isBlank(selectIdSr)){
            return MapMessage.errorMessage("请选择原执行人");
        }
        AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(mainTaskId);
        if (null == agentTaskMain){
            return MapMessage.errorMessage("该任务不存在");
        }
        //如果任务已经截止，不可变更执行人
        if (agentTaskMain.getEndTime().before(currentDate)){
            return MapMessage.errorMessage("该任务已截止，不可变更执行人");
        }
        AgentUser agentUser = agentUserLoaderClient.load(operatorId);
        if (null == agentUser){
            return MapMessage.errorMessage("该执行人不存在");
        }
        List<String> selectIds = Arrays.asList(StringUtils.split(selectIdSr,","));

        agentTaskCenterService.updateOperator(mainTaskId,selectIds,operatorId,agentUser.getRealName());
        return MapMessage.successMessage();
    }


    /**
     * 任务完成情况
     * @return
     */
    @RequestMapping(value = "task_finish_info.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage taskFinishInfo(){
        String mainTaskId = requestString("mainTaskId");
        if (StringUtils.isBlank(mainTaskId)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        Map<String, Object> dataMap = agentTaskCenterService.taskFinishInfo(mainTaskId);
        return MapMessage.successMessage().add("dataMap",dataMap);
    }


    /**
     * 线上维护老师数据导出
     * @param workbook
     * @param dataList
     */
    private void generalTaskSubOnlineExportData(SXSSFWorkbook workbook, Collection<AgentTaskSubOnlineExportData> dataList){
        try {
            Sheet sheet = workbook.createSheet("线上维护老师");
            sheet.createFreezePane(0, 1, 0, 1);

            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            Row firstRow = sheet.createRow(0);

            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setFont(font);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            baseExcelService.setCellValue(firstRow, 0, firstRowStyle, "执行人");
            baseExcelService.setCellValue(firstRow, 1, firstRowStyle, "市场部");
            baseExcelService.setCellValue(firstRow, 2, firstRowStyle, "大区");
            baseExcelService.setCellValue(firstRow, 3, firstRowStyle, "区域");
            baseExcelService.setCellValue(firstRow, 4, firstRowStyle, "分区");
            baseExcelService.setCellValue(firstRow, 5, firstRowStyle, "学校ID");
            baseExcelService.setCellValue(firstRow, 6, firstRowStyle, "学校名称");
            baseExcelService.setCellValue(firstRow, 7, firstRowStyle, "老师ID");
            baseExcelService.setCellValue(firstRow, 8, firstRowStyle, "老师姓名");
            baseExcelService.setCellValue(firstRow, 9, firstRowStyle, "科目");
            baseExcelService.setCellValue(firstRow, 10, firstRowStyle, "省");
            baseExcelService.setCellValue(firstRow, 11, firstRowStyle, "市");
            baseExcelService.setCellValue(firstRow, 12, firstRowStyle, "行政区");
            baseExcelService.setCellValue(firstRow, 13, firstRowStyle, "维护时间");
            baseExcelService.setCellValue(firstRow, 14, firstRowStyle, "维护方式");
            baseExcelService.setCellValue(firstRow, 15, firstRowStyle, "结果");
            baseExcelService.setCellValue(firstRow, 16, firstRowStyle, "任务期间是否布置作业");


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (AgentTaskSubOnlineExportData data : dataList) {
                    Row row = sheet.createRow(index++);
                    List<Object> exportAbleData = data.getExportAbleData();
                    if (CollectionUtils.isNotEmpty(exportAbleData)){
                        for (int i = 0; i < exportAbleData.size(); i++) {
                            baseExcelService.setCellValue(row,i,cellStyle,exportAbleData.get(i));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ",ex);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+ex)
                    .subject("线上维护老师下载异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 进校维护老师数据导出
     * @param workbook
     * @param dataList
     */
    private void generalTaskSubIntoSchoolExportData(SXSSFWorkbook workbook, Collection<AgentTaskSubIntoSchoolExportData> dataList){
        try {
            Sheet sheet = workbook.createSheet("进校维护老师");
            sheet.createFreezePane(0, 1, 0, 1);

            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            Row firstRow = sheet.createRow(0);

            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setFont(font);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            baseExcelService.setCellValue(firstRow, 0, firstRowStyle, "执行人");
            baseExcelService.setCellValue(firstRow, 1, firstRowStyle, "市场部");
            baseExcelService.setCellValue(firstRow, 2, firstRowStyle, "大区");
            baseExcelService.setCellValue(firstRow, 3, firstRowStyle, "区域");
            baseExcelService.setCellValue(firstRow, 4, firstRowStyle, "分区");
            baseExcelService.setCellValue(firstRow, 5, firstRowStyle, "学校ID");
            baseExcelService.setCellValue(firstRow, 6, firstRowStyle, "学校名称");
            baseExcelService.setCellValue(firstRow, 7, firstRowStyle, "老师ID");
            baseExcelService.setCellValue(firstRow, 8, firstRowStyle, "老师姓名");
            baseExcelService.setCellValue(firstRow, 9, firstRowStyle, "科目");
            baseExcelService.setCellValue(firstRow, 10, firstRowStyle, "省");
            baseExcelService.setCellValue(firstRow, 11, firstRowStyle, "市");
            baseExcelService.setCellValue(firstRow, 12, firstRowStyle, "行政区");
            baseExcelService.setCellValue(firstRow, 13, firstRowStyle, "任务期间是否进校");
            baseExcelService.setCellValue(firstRow, 14, firstRowStyle, "任务期间是否进校拜访老师");
            baseExcelService.setCellValue(firstRow, 15, firstRowStyle, "任务期间是否布置作业");


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (AgentTaskSubIntoSchoolExportData data : dataList) {
                    Row row = sheet.createRow(index++);
                    List<Object> exportAbleData = data.getExportAbleData();
                    if (CollectionUtils.isNotEmpty(exportAbleData)){
                        for (int i = 0; i < exportAbleData.size(); i++) {
                            baseExcelService.setCellValue(row,i,cellStyle,exportAbleData.get(i));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ",ex);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+ex)
                    .subject("进校维护老师下载异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 任务下载明细
     */
    @RequestMapping(value = "task_download_detail.vpage",method = RequestMethod.GET)
    public void taskDownloadDetail(){
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);
            String fileName = "{}-"+"{}-"+ nowTime + ".xlsx";
            String mainTaskId = requestString("mainTaskId");
            AgentTaskMain agentTaskMain = agentTaskCenterService.getMainTaskById(mainTaskId);
            AgentTaskType taskType = agentTaskMain.getTaskType();
            //线上维护老师
            if (taskType == AgentTaskType.ONLINE_SERVICE_TEACHER){
                fileName = StringUtils.formatMessage(fileName,agentTaskMain.getTitle(),"线上维护老师明细");
                List<AgentTaskSubOnlineExportData> taskSubOnlineExportData = agentTaskCenterService.getTaskSubOnlineExportData(mainTaskId);
                generalTaskSubOnlineExportData(workbook,taskSubOnlineExportData);
                //进校维护老师
            }else if (taskType == AgentTaskType.INSCHOOL_SERVICE_TEACHER){

                fileName = StringUtils.formatMessage(fileName,agentTaskMain.getTitle(),"进校维护老师明细");
                List<AgentTaskSubIntoSchoolExportData> taskSubIntoSchoolExportData = agentTaskCenterService.getTaskSubIntoSchoolExportData(mainTaskId);
                generalTaskSubIntoSchoolExportData(workbook,taskSubIntoSchoolExportData);

            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception e) {
            logger.error("error info: ",e);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+e)
                    .subject("任务下载明细异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }
}