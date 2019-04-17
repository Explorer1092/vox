package com.voxlearning.utopia.agent.controller.exam;


import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.exam.AgentExamContractPaybackReportData;
import com.voxlearning.utopia.agent.bean.exam.AgentExamContractVO;
import com.voxlearning.utopia.agent.constants.AgentLargeExamContractType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractExtend;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractPayback;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContractSplitSetting;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.exam.AgentLargeExamService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceRange;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大考合同
 *
 * @author chunlin.yu
 * @create 2018-03-13 22:06
 **/

@Controller
@RequestMapping("/exam/contractmanage")
public class ContractController extends AbstractAgentController {

    @Inject
    SchoolLoaderClient schoolLoaderClient;

    @Inject
    AgentDictSchoolService agentDictSchoolService;

    @Inject
    AgentLargeExamService agentLargeExamService;

    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;

    @RequestMapping(value = "manage.vpage",method = RequestMethod.GET)
    @OperationCode("2a836acaf1504254")
    public String manage(Model model){
        return "exam/contract_manage";

    }
    @RequestMapping(value = "changeManage.vpage",method = RequestMethod.GET)
    public String changeManage(Model model){
        String type = requestString("type");
        List<Map<String,Object>> serviceRangeList = new ArrayList<>();
        Arrays.stream(AgentServiceRange.values()).forEach(p -> {
            Map<String,Object> serviceTypeMap = new HashMap<>();
            serviceTypeMap.put("sr_key",p.name());
            serviceTypeMap.put("sr_value",p.getDesc());
            serviceRangeList.add(serviceTypeMap);
        });
        model.addAttribute("serviceRangeList",serviceRangeList);
        model.addAttribute("type",type);
        return "exam/changeManage";

    }

    @RequestMapping(value = "detail.vpage",method = RequestMethod.GET)
    public String detail(Model model){
        return "exam/contract_detail";

    }

    /**
     * 学校校验
     * @return
     */
    @RequestMapping(value = "verify_school.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage verifySchool(){
        Long schoolId = requestLong("schoolId");
        if (null == schoolId){
            return MapMessage.errorMessage("学校ID错误");
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if(school == null){
            return MapMessage.errorMessage("此学校不存在，无法添加");
        }
        if(!school.isMiddleSchool() && !school.isSeniorSchool()){
            return MapMessage.errorMessage("此学校不是初高中，无法添加");
        }
        Map<String,Object> dataMap = new HashMap<>();
        //获取学校主干名

        dataMap.put("cmainName",school.getCmainName());

        //获取学校等级
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool != null && agentDictSchool.getSchoolPopularity() != null){
            dataMap.put("schoolPopularityType",agentDictSchool.getSchoolPopularity().getLevel());
        }
        // 获取负责学校的专员
        AgentUser agentUser = baseOrgService.getSchoolManager(schoolId).stream().findFirst().orElse(null);
        if (null != agentUser){
            dataMap.put("userName",agentUser.getRealName());
        }
        return MapMessage.successMessage().add("data",dataMap);
    }

    /**
     * 获取学校的合同
     * @return
     */
    @RequestMapping(value = "school_contract.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolContract(){
        //合同ID
        Long id = requestLong("id");
        if (null == id){
            return MapMessage.errorMessage("合同ID错误");
        }
        AgentExamContractVO agentExamContract = agentLargeExamService.getAgentExamContract(id);
        agentExamContract = agentLargeExamService.getExamContractExtend(agentExamContract, id);
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("agentExamContract",agentExamContract);
        //获取合同服务范围
        String serviceRange = agentExamContract.getServiceRange();
        List<AgentServiceRange> serviceRangeList = AgentServiceRange.toList(serviceRange);
        List<Map<String,Object>> serviceRangeMapList = new ArrayList<>();
        Arrays.stream(AgentServiceRange.values()).forEach( p-> {
            Map<String,Object> serviceRangeMap = new HashMap<>();
            serviceRangeMap.put("sr_key",p.name());
            serviceRangeMap.put("sr_value",p.getDesc());
            if (serviceRangeList.contains(p)){
                serviceRangeMap.put("sr_show",true);
            }else {
                serviceRangeMap.put("sr_show",false);
            }
            serviceRangeMapList.add(serviceRangeMap);
        });
        dataMap.put("serviceRangeList",serviceRangeMapList);
        return MapMessage.successMessage().add("data",dataMap);
    }

    /**
     * 删除合同
     * @return
     */
    @RequestMapping(value = "delete_contract.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteContract(){
        Long contractId = requestLong("contractId");
        if (null == contractId){
            return MapMessage.errorMessage("学校合同ID错误");
        }
        List<AgentExamContractPayback> contractPaybackList = agentLargeExamService.getPaybackByContractId(contractId);
        if (CollectionUtils.isNotEmpty(contractPaybackList)){
            return MapMessage.errorMessage("该合同下尚有回款记录，不可删除");
        }
        agentLargeExamService.deleteExamContract(contractId);
        return MapMessage.successMessage();
    }

    /**
     * 根据学校ID获取学校的有效合同
     * @return
     */
    @RequestMapping(value = "school_valid_contract.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolValidContract(){
        //学校ID
        Long schoolId = requestLong("schoolId");
        if (null == schoolId){
            return MapMessage.errorMessage("学校ID错误");
        }
        AgentExamContractVO agentExamContract = agentLargeExamService.getAgentExamContractBySchoolId(schoolId);
        return MapMessage.successMessage().add("agentExamContract",agentExamContract);
    }


    /**
     * 插入或者更新合同
     * @return
     */
    @RequestMapping(value = "upsert_contract.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertContract(){
        //主键ID，若ID存在，则为更新，id不存在，则为插入
        Long id = requestLong("id");
        Long schoolId = requestLong("schoolId");
        AgentLargeExamContractType contractType = AgentLargeExamContractType.nameOf(requestString("contractType"));
        Integer contractAmount = requestInteger("contractAmount");
        Date beginDate = requestDate("beginDate");
        Date endDate = requestDate("endDate");
        Date contractDate = requestDate("contractDate");
        Integer hardwareCost = requestInteger("hardwareCost");
        Integer machinesNum = requestInteger("machinesNum");
        String machinesType = requestString("machinesType");
        String remark = requestString("remark");
        String splitSettingJsonStr = requestString("splitSettingList");         //分成设置
        String imageUrlJsonStr = requestString("imageUrlList");                 //合同图片URL

        Integer thirdPartyProductCost = requestInteger("thirdPartyProductCost");//第三方产品成本
        String serviceRangeStr = getRequestString("serviceRangeStr");//服务范围

        if (null == schoolId){
            return MapMessage.errorMessage("学校ID错误");
        }
        if (null == contractType){
            return MapMessage.errorMessage("合同类型不正确");
        }
        if (null == contractAmount){
            return MapMessage.errorMessage("合同金额不正确");
        }
        if (null == beginDate){
            return MapMessage.errorMessage("服务开始日期不正确");
        }
        if (null == endDate){
            return MapMessage.errorMessage("服务结束日期不正确");
        }
        if (endDate.before(beginDate)){
            return MapMessage.errorMessage("服务结束日期不能早于开始日期");
        }
        if (null == contractDate){
            return MapMessage.errorMessage("签约日期不正确");
        }
        if (StringUtils.isBlank(serviceRangeStr)){
            return MapMessage.errorMessage("服务范围至少选择一项");
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if(school == null){
            return MapMessage.errorMessage("此学校不存在，无法添加");
        }
        if(!school.isMiddleSchool() && !school.isSeniorSchool()){
            return MapMessage.errorMessage("此学校不是初高中，无法添加");
        }

        AgentExamContractVO contractVO = new AgentExamContractVO();
        contractVO.setId(id);
        contractVO.setSchoolId(schoolId);
        contractVO.setContractType(contractType);
        contractVO.setContractAmount(contractAmount);
        contractVO.setBeginDate(beginDate);
        contractVO.setEndDate(endDate);
        contractVO.setContractDate(contractDate);

        contractVO.setHardwareCost(null != hardwareCost ? hardwareCost : 0);
        contractVO.setMachinesNum(null != machinesNum ? machinesNum : 0);
        contractVO.setMachinesType(null != machinesType ? machinesType : "");
        contractVO.setRemark(null != remark ? remark : "");
        contractVO.setThirdPartyProductCost(null != thirdPartyProductCost ? thirdPartyProductCost : 0);
        List<AgentServiceRange> serviceRangeList = AgentServiceRange.toList(serviceRangeStr);
        if (CollectionUtils.isNotEmpty(serviceRangeList)){
            contractVO.setServiceRange(StringUtils.join(serviceRangeList.stream().map(AgentServiceRange::name).collect(Collectors.toList()), ","));
        }
        //分成设置
        AgentExamContractExtend agentExamContractExtend = new AgentExamContractExtend();
        List<AgentExamContractSplitSetting> splitSettingList = new ArrayList<>();
        if (StringUtils.isNotBlank(splitSettingJsonStr)){
            splitSettingList = JsonUtils.fromJsonToList(splitSettingJsonStr, AgentExamContractSplitSetting.class);
        }
        agentExamContractExtend.setSplitSettingList(splitSettingList);
        //保存合同基础信息
        Long contractId = agentLargeExamService.addOrUpdateLargeExamContract(contractVO);
        //合同图片URL
        List<String> imageUrlList = new ArrayList<>();
        if (StringUtils.isNotBlank(imageUrlJsonStr)){
            String[] imageUrlArray = imageUrlJsonStr.split(",");
            imageUrlList = Arrays.stream(imageUrlArray).collect(Collectors.toList());
        }
        agentExamContractExtend.setImageUrlList(imageUrlList);
        //保存合同扩展信息
        if (null != contractId){
            agentExamContractExtend.setContractId(contractId);
            agentExamContractExtend.setDisabled(false);
            agentLargeExamService.addOrUpdateExamContractExtend(agentExamContractExtend);
        }
        return MapMessage.successMessage();
    }


    /**
     * 合同检索
     * @return
     */
    @RequestMapping(value = "search_contract.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchContract(){
        Long id = requestLong("id");
        Long schoolId = requestLong("schoolId");
        Long contractorId = requestLong("contractorId");
        AgentLargeExamContractType contractType = AgentLargeExamContractType.nameOf(requestString("contractType"));
        Date beginDate = requestDate("beginDate");
        Date endDate = requestDate("endDate");
        if (null == id && schoolId == null && contractorId == null && beginDate == null && endDate == null){
            MonthRange current = MonthRange.current();
            Date startDate = current.getStartDate();
            beginDate = DateUtils.addMonths(startDate,-5);
            endDate = current.getEndDate();
        } else if (null == id && schoolId == null && contractorId == null){
            if (null == beginDate || null == endDate){
                return MapMessage.errorMessage("请选择开始日期和结束日期");
            }
            if (beginDate.before(DateUtils.addDays(endDate, -31))) {
                return MapMessage.errorMessage("查询时间范围不能大于31天");
            }
        }
        List<AgentExamContractVO> dataList = agentLargeExamService.searchContract(id,schoolId,null,contractType,beginDate,endDate);
        if (null != contractorId){
            dataList = dataList.stream().filter(item -> null != item && Objects.equals(item.getContractorId(), contractorId)).collect(Collectors.toList());
        }
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 学校检索
     * @return
     */
    @RequestMapping(value = "search_school.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchSchool(){
        String schoolKey = getRequestString("schoolKey");
        if (StringUtils.isEmpty(schoolKey)){
            return MapMessage.errorMessage("检索词为空");
        }
        Collection<School> schools = agentLargeExamService.searchSchool(getCurrentUserId(), schoolKey);
        return MapMessage.successMessage().add("dataList",schools);
    }

    /**
     * 用户检索
     * @return
     */
    @RequestMapping(value = "search_user.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchUser(){
        String userKey = getRequestString("userKey");
        if (StringUtils.isEmpty(userKey)){
            return MapMessage.errorMessage("检索词为空");
        }
        long userId = SafeConverter.toLong(userKey);
        List<AgentUser> dataList = new ArrayList<>();
        if (userId > 0){
            AgentUser user = baseOrgService.getUser(userId);
            dataList.add(user);
        }else {
            List<AgentUser> agentUserList = baseOrgService.findUserByRealName(userKey);
            dataList.addAll(agentUserList);
        }
        return MapMessage.successMessage().add("dataList",dataList);
    }

    /**
     * 合同回款记录信息
     * @return
     */
    @RequestMapping(value = "contract_payback_info.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage contractPaybackInfo(){
        //合同ID
        Long contractId = requestLong("contractId");
        if (null == contractId){
            return MapMessage.errorMessage("合同ID错误");
        }
        return MapMessage.successMessage().add("data",agentLargeExamService.getContractPaybackInfo(contractId));
    }

    /**
     * 新建合同回款记录
     * @return
     */
    @RequestMapping(value = "add_contract_payback.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addContractPayback(){
        Long currentUserId = getCurrentUserId();
        Long contractId = requestLong("contractId");
        Integer waitPaybackAmount = requestInteger("waitPaybackAmount");
        Date paybackDate = requestDate("paybackDate");
        Integer paybackAmount = requestInteger("paybackAmount");
        if (null == contractId){
            return MapMessage.errorMessage("合同ID不正确");
        }
        if (null == paybackDate){
            return MapMessage.errorMessage("回款日期不正确");
        }
        if (null == paybackAmount){
            return MapMessage.errorMessage("回款金额不正确");
        }
        //合同金额
        AgentExamContractVO agentExamContract = agentLargeExamService.getAgentExamContract(contractId);
        Integer contractAmount = 0;
        if (null != agentExamContract){
            contractAmount = agentExamContract.getContractAmount();
        }
        //已回款金额
        Integer havaPaybackAmount = agentLargeExamService.getContractHavaPaybackAmount(contractId);
        //待回款金额
        waitPaybackAmount = contractAmount - havaPaybackAmount;
        if (paybackAmount > waitPaybackAmount){
            return MapMessage.errorMessage("回款金额需小于等于待回款金额");
        }
        //退款金额
        if (MathUtils.doubleAdd(havaPaybackAmount,paybackAmount) < 0){
            return MapMessage.errorMessage("退款金额需小于等于已回款金额");
        }


        AgentExamContractPayback agentExamContractPayback = new AgentExamContractPayback();
        agentExamContractPayback.setContractId(contractId);
        agentExamContractPayback.setPaybackDate(paybackDate);
        agentExamContractPayback.setPaybackAmount(paybackAmount);
        agentExamContractPayback.setOperatorId(currentUserId);
        agentLargeExamService.addContractPayback(agentExamContractPayback);
        return MapMessage.successMessage();
    }

    /**
     * 合同回款明细Excel导出
     */
    @RequestMapping(value = "contract_payback_export.vpage", method = RequestMethod.GET)
    public void contractPaybackExport() {
        try{
            Date beginDate = requestDate("beginDate");
            Date endDate = requestDate("endDate");
            SXSSFWorkbook workbook = new SXSSFWorkbook();

            List<AgentExamContractPaybackReportData> contractPaybackReportData = agentLargeExamService.getContractPaybackReportData(beginDate, endDate);
            generalContractPaybackReportData(workbook,contractPaybackReportData);
            String title = "合同回款明细";
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
        }catch (Exception e){
            logger.error("error info: ",e);
            emailServiceClient.createPlainEmail()
                    .body("功能：合同回款明细Excel导出；接口：contract_payback_export.vpage；error info: "+e)
                    .subject("查询数据异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }

    }


    private void generalContractPaybackReportData(SXSSFWorkbook workbook,Collection<AgentExamContractPaybackReportData> dataList){
        try {
            Sheet sheet = workbook.createSheet("合同回款明细");
            sheet.createFreezePane(0, 1, 0, 1);
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setFont(font);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            Row firstRow = sheet.createRow(0);
            baseExcelService.setCellValue(firstRow, 0, firstRowStyle, "合同编号");
            baseExcelService.setCellValue(firstRow, 1, firstRowStyle, "回款编号");
            baseExcelService.setCellValue(firstRow, 2, firstRowStyle, "学校ID");
            baseExcelService.setCellValue(firstRow, 3, firstRowStyle, "学校名称");
            baseExcelService.setCellValue(firstRow, 4, firstRowStyle, "等级");
            baseExcelService.setCellValue(firstRow, 5, firstRowStyle, "合同类型");
            baseExcelService.setCellValue(firstRow, 6, firstRowStyle, "主签约人");
            baseExcelService.setCellValue(firstRow, 7, firstRowStyle, "合同总金额");
            baseExcelService.setCellValue(firstRow, 8, firstRowStyle, "硬件成本");
            baseExcelService.setCellValue(firstRow, 9, firstRowStyle, "回款日期");
            baseExcelService.setCellValue(firstRow, 10, firstRowStyle, "回款金额");
            baseExcelService.setCellValue(firstRow, 11, firstRowStyle, "本期前已回款金额");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (AgentExamContractPaybackReportData data : dataList) {
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
                    .subject("合同回款明细数据下载异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 合同操作日志
     * @return
     */
    @RequestMapping(value = "contract_opetation_record.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage contractOperationRecord(){
        Long contractId = requestLong("contractId");
        return MapMessage.successMessage().add("contractOperationRecordList",agentLargeExamService.getContractOperationRecord(contractId));
    }
}
