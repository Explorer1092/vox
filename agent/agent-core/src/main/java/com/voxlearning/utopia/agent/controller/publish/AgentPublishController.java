package com.voxlearning.utopia.agent.controller.publish;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.dao.mongo.publish.AgentPublishControlDao;
import com.voxlearning.utopia.agent.dao.mongo.publish.AgentPublishDao;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublish;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublishData;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.publish.AgentPublishService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


/**
 * AgentPublishController
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Controller
@RequestMapping("/publish/data")
public class AgentPublishController extends AbstractAgentController {
    @Inject
    private AgentPublishService agentPublishService;
    @Inject
    private AgentPublishDao agentPublishDao;
    @Inject
    private AgentPublishControlDao agentPublishControlDao;
    @RequestMapping("publish_list_page.vpage")
    @OperationCode("0aa63f835e3244e9")
    public String publishListPage(){
        return "publish/publish_list";
    }
    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private EmailServiceClient emailServiceClient;

    // 新建或修改页面
    @RequestMapping("save_page.vpage")
    public String savePage(Model model){
        String publishId = getRequestString("publishId");
        if(!StringUtils.isBlank(publishId)){
            AgentPublish publish = agentPublishDao.load(publishId);
            if(publish != null){
                model.addAttribute("publishId", publishId);
                model.addAttribute("title", publish.getTitle());
                model.addAttribute("comment", publish.getComment());
                model.addAttribute("control", agentPublishControlDao.loadByPublishId(publishId));
            }
        }
        model.addAttribute("marketRoleTypeList", AgentRoleType.getMarketRoleList());
        return "publish/publish_info";
    }


    // 上传文件操作
    @RequestMapping(value = "upload_data.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadData(){
        String publishId = getRequestString("publishId");
        String title = getRequestString("title");
        if(StringUtils.isBlank(title)){
            return MapMessage.errorMessage("请填写标题");
        }

        Boolean isGrouped = requestBoolean("isGrouped");
        if(isGrouped == null){
            return MapMessage.errorMessage("请确定数据是否分组！");
        }

        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage();
        }
        return agentPublishService.uploadWorkBook(workbook, publishId, title, isGrouped);
    }

    // 保存功能
    @RequestMapping("save_data.vpage")
    @ResponseBody
    public MapMessage saveData(){
        String publishId = getRequestString("publishId");
        if(StringUtils.isBlank(publishId)){
            return MapMessage.errorMessage("publishId为空");
        }

        String title = getRequestString("title");
        if(StringUtils.isBlank(title)){
            return MapMessage.errorMessage("请填写标题");
        }

        Boolean allowViewSubordinateData = requestBoolean("allowViewSubordinateData");
        if(allowViewSubordinateData == null){
            return MapMessage.errorMessage("请确定数据是否分组！");
        }

        Boolean allowDownload = requestBoolean("allowDownload");
        if(allowDownload == null){
            return MapMessage.errorMessage("请确定数据是否分组！");
        }

        String comment = getRequestString("comment");

        Set<Integer> roleIds = requestIntegerSet("roleIds");
        Set<Long> groupIds = requestLongSet("groupIds");
        List<AgentRoleType> roleTypeList = roleIds.stream().map(AgentRoleType::of).filter(Objects::nonNull).collect(Collectors.toList());
        return agentPublishService.saveData(publishId, title, allowViewSubordinateData, allowDownload, roleTypeList, new ArrayList<>(groupIds), comment);
    }

    /**
     * 获取列表数据
     * @return
     */
    @RequestMapping("publish_list.vpage")
    @ResponseBody
    public MapMessage publishList(){
        MapMessage message = MapMessage.successMessage();
        List<AgentPublish> dataList = agentPublishService.getPublishList(getCurrentUserId());
        message.add("dataList",dataList);
        return message;
    }

    /**
     * 数据详情接口
     * @return
     */
    @RequestMapping(value = "publish_detail.vpage",method = RequestMethod.GET)
    public String publishDetail(Model model){
        String publishId = requestString("publishId");
        Map<String,Object> dataMap = new HashMap<>();
        if (StringUtils.isNotBlank(publishId)){
            dataMap = agentPublishService.getPublishDetail(publishId);
        }
        model.addAttribute("data", dataMap);
        return "publish/publish_detail";
    }

    /**
     * 数据预览接口|详情表格数据接口
     * @return
     */
    @RequestMapping(value = "publish_data_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage publishDataList(){
        MapMessage message = MapMessage.successMessage();
        AuthCurrentUser currentUser = getCurrentUser();
        String publishId = requestString("publishId");
        Map<String,Object> dataMap = new HashMap<>();
        List<List<Object>> dataList = new ArrayList<>();
        if (StringUtils.isNotBlank(publishId)){
            //设置表头列表
            dataMap.put("dataTitleList",agentPublishService.getPublishDataTitleList(publishId));
            //设置数据列表
            List<AgentPublishData> publishDataList = agentPublishService.getPublishDataList(currentUser.getUserId(), publishId);
            publishDataList.forEach(p -> {
                if (null != p){
                    dataList.add(p.getDataList());
                }
            });
            dataMap.put("dataList",dataList);
        }else {
            message = MapMessage.errorMessage();
            //设置表头列表
            dataMap.put("dataTitleList",new ArrayList<>());
            //设置数据列表
            dataMap.put("dataList",new ArrayList<>());
        }
        message.add("data",dataMap);
        return message;
    }

    /**
     * 上线（发布）|下线接口
     * @return
     */
    @RequestMapping(value = "publish_onOffLine.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage publishOnline(){
        String id = requestString("id");
        Integer status = requestInteger("status");
        if (StringUtils.isBlank(id) || null == status){
            return MapMessage.errorMessage("数据错误");
        }
        //发布
        if (Objects.equals(status, AgentPublish.STATUS_ONLINE)){
            agentPublishService.updatePublishStatus(id,status);
        //下线
        } else if (Objects.equals(status, AgentPublish.STATUS_OFFLINE)){
            agentPublishService.updatePublishStatus(id,status);
        }
        return MapMessage.successMessage();
    }

    /**
     * 删除
     * @return
     */
    @RequestMapping(value = "publish_delete.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage publishDelete(){
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("数据同步ID错误");
        }
        agentPublishService.deletePublish(id);
        return MapMessage.successMessage();
    }

    /**
     * 数据Excel导出
     */
    @RequestMapping(value = "publish_export.vpage", method = RequestMethod.GET)
    public void publishExport() {
        try {
            AuthCurrentUser currentUser = getCurrentUser();
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            List<List<Object>> dataList = new ArrayList<>();
            String id = requestString("id");
            String title = "信息分发";
            if (StringUtils.isNotBlank(id)){
                AgentPublish publish = agentPublishDao.load(id);
                if(publish != null){
                    title = StringUtils.isBlank(publish.getTitle()) ? "" : publish.getTitle();
                    //获取导出数据（表头数据）
                    List<String> dataTitleList = agentPublishService.getPublishDataTitleList(id);
                    //获取导出数据（数据部分）
                    List<AgentPublishData> publishDataList = agentPublishService.getPublishDataList(currentUser.getUserId(), id);
                    publishDataList.forEach(p->{
                        if (null != p){
                            dataList.add(p.getDataList());
                        }
                    });
                    generalPublishSheet(workbook,dataTitleList,dataList);
                }
            }
            title = title + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    title,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        } catch (Exception e) {
            logger.error("error info: ",e);
            emailServiceClient.createPlainEmail()
                    .body("功能：数据同步导出；接口：publish_export.vpage；error info: "+e)
                    .subject("查询数据异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }

    private void generalPublishSheet(SXSSFWorkbook workbook,List<String> dataTileList,List<List<Object>> dataList){
        try {
            Sheet sheet = workbook.createSheet("数据同步");
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
            //拼装表头
            for (int i=0;i<dataTileList.size();i++){
                baseExcelService.setCellValue(firstRow,i,firstRowStyle,dataTileList.get(i));
            }
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            //拼装数据
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (List<Object> data : dataList) {
                    Row row = sheet.createRow(index++);
                    if (CollectionUtils.isNotEmpty(data)){
                        for (int i = 0; i < data.size(); i++) {
                            baseExcelService.setCellValue(row,i,cellStyle,data.get(i));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ",ex);
            emailServiceClient.createPlainEmail()
                    .body("功能：数据同步导出；方法：generalPublishSheet；error info: "+ex)
                    .subject("数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }
}

