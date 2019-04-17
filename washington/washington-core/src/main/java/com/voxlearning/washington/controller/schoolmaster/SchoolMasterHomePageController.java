package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.rstaff.consumer.SchoolMasterServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * describe:
 *
 * @author yong.liu
 * @date 2019/02/27
 */
@Controller
@RequestMapping("/schoolmasterHomepage")
public class SchoolMasterHomePageController extends SchoolMasterBaseController{


    @Inject private SchoolMasterServiceClient schoolMasterServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @RequestMapping(value = "loadCurrentInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadCurrentInfo(){
        Date curDate = new Date();
        int day = curDate.getDate();
        Calendar cal = Calendar.getInstance();
        if(day <= 3){
            cal.add(Calendar.MONTH, -1);
        }
        String currentDateStr = DateUtils.dateToString(cal.getTime(),"yyyy年MM月");
        String currentTerm = (String)getCurrentTermMap().get("name");
        return MapMessage.successMessage().set("currentDate",currentDateStr).set("currentTerm",currentTerm);
    }

    private Map<String,Object> getCurrentTermMap() {
        Date curDate = new Date();
        List<Map<String,Object>> terms = getTerms();
        Map<String,Object> currentTermMap = null;
        for(Map<String,Object> temp : terms){
            String startDateStr = (String) temp.get("startDate");
            Date startDate = DateUtils.stringToDate(startDateStr+" 00:00:00","yyyyMMdd HH:mm:ss");
            String endDateStr = (String) temp.get("endDate");
            Date endDate = DateUtils.stringToDate(endDateStr+" 23:59:59","yyyyMMdd HH:mm:ss");
            if(curDate.before(endDate) && curDate.after(startDate)){
                currentTermMap = temp;
                break;
            }
        }
        return currentTermMap;
    }

    @RequestMapping(value = "loadBaseData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBaseData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        String dateStr = getCurrentMonth();
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        Map<String,Object> data = schoolMasterServiceClient.loadBaseData(SafeConverter.toInt(dateStr),schoolId);
        if(Objects.isNull(data)){
            result.add("result", false);
            result.add("info", "没有数据");
            return result;
        }

        result.add("result", true);
        data.put("schoolName",school.getShortName());
        result.add("data",data);
        return result;
    }

    private String getCurrentMonth() {
        Date curDate = new Date();
        int day = curDate.getDate();
        Calendar cal = Calendar.getInstance();
        if(day <= 3){
            cal.add(Calendar.MONTH, -1);
        }
        return DateUtils.dateToString(cal.getTime(),"yyyyMM");
    }

    @RequestMapping(value = "loadHomeWorkData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadHomeWorkData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        String dateStr = getCurrentMonth();
        List<Map<String,Object>> data = schoolMasterServiceClient.loadHomeWorkData(SafeConverter.toInt(dateStr),schoolId);
        if(Objects.isNull(data)){
            result.add("result", false);
            result.add("info", "没有数据");
            return result;
        }

        result.add("result", true);
        result.add("data",data);
        return result;
    }

    @RequestMapping(value = "loadExamData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadExamData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        String dateStr = getCurrentMonth();
        List<Map<String,Object>> data = schoolMasterServiceClient.loadExamData(SafeConverter.toInt(dateStr),schoolId);
        if(Objects.isNull(data)){
            result.add("result", false);
            result.add("info", "没有数据");
            return result;
        }

        result.add("result", true);
        result.add("data",data);
        return result;
    }

    @RequestMapping(value = "loadActivityData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadActivityData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        String dateStr = getCurrentMonth();
        List<Map<String,Object>> data = schoolMasterServiceClient.loadActivityData(SafeConverter.toInt(dateStr),schoolId);
        if(Objects.isNull(data)){
            result.add("result", false);
            result.add("info", "没有数据");
            return result;
        }

        result.add("result", true);
        result.add("data",data);
        return result;
    }

    @RequestMapping(value = "loadTeachResourceData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeachResourceData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        String dateStr = getCurrentMonth();
        List<Map<String,Object>> data = schoolMasterServiceClient.loadTeachResourceData(SafeConverter.toInt(dateStr),schoolId);
        if(Objects.isNull(data)){
            result.add("result", false);
            result.add("info", "没有数据");
            return result;
        }

        result.add("result", true);
        result.add("data",data);
        return result;
    }

    @RequestMapping(value = "loadLearningSkillsData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadLearningSkillsData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        String schoolYearTerm = (String)getCurrentTermMap().get("value");
        String grade = getRequestString("grade");
        String schoolYear = schoolYearTerm.split("-")[0];
        String term = schoolYearTerm.split("-")[1];
        Map<String,Object> englishData = schoolMasterServiceClient.loadLearningSkillsData(schoolId,school.getRegionCode(),"ENGLISH",grade,schoolYear,term);
        Map<String,Object> mathData = schoolMasterServiceClient.loadLearningSkillsData(schoolId,school.getRegionCode(),"MATH",grade,schoolYear,term);
        Map<String,Object> chineseData = schoolMasterServiceClient.loadLearningSkillsData(schoolId,school.getRegionCode(),"CHINESE",grade,schoolYear,term);
        List<Map<String,Object>> data = new LinkedList<>();
        Map<String,Object> engRes = new LinkedHashMap<>();
        engRes.put("subject","ENGLISH");
        if(Objects.nonNull(englishData)){
            engRes.putAll(englishData);
        }

        Map<String,Object> maRes = new LinkedHashMap<>();
        maRes.put("subject","MATH");
        if(Objects.nonNull(mathData)){
            maRes.putAll(mathData);
        }

        Map<String,Object> chRes = new LinkedHashMap<>();
        chRes.put("subject","CHINESE");
        if(Objects.nonNull(chineseData)){
            chRes.putAll(chineseData);
        }
        data.add(engRes);
        data.add(maRes);
        data.add(chRes);

        result.add("result", true);
        result.add("data",data);
        result.add("currentTerm",getCurrentTermMap().get("name"));
        return result;
    }

    @RequestMapping(value = "loadKnowledgeModuleData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadKnowledgeModuleData(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        String schoolYearTerm = (String)getCurrentTermMap().get("value");
        String grade = getRequestString("grade");
        String schoolYear = schoolYearTerm.split("-")[0];
        String term = schoolYearTerm.split("-")[1];
        Map<String,Object> englishData = schoolMasterServiceClient.loadKnowledgeModuleData(schoolId,school.getRegionCode(),"ENGLISH",grade,schoolYear,term);
        Map<String,Object> mathData = schoolMasterServiceClient.loadKnowledgeModuleData(schoolId,school.getRegionCode(),"MATH",grade,schoolYear,term);

        List<Map<String,Object>> data = new LinkedList<>();
        Map<String,Object> engRes = new LinkedHashMap<>();
        engRes.put("subject","ENGLISH");
        if(Objects.nonNull(englishData)){
            engRes.putAll(englishData);
        }

        Map<String,Object> maRes = new LinkedHashMap<>();
        maRes.put("subject","MATH");
        if(Objects.nonNull(mathData)){
            maRes.putAll(mathData);
        }

        data.add(engRes);
        data.add(maRes);

        result.add("result", true);
        result.add("data",data);
        result.add("currentTerm",getCurrentTermMap().get("name"));
        return result;
    }

    @RequestMapping(value = "loadMsgList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadMsgList(){
        MapMessage result = new MapMessage();
        ResearchStaff schoolMaster = currentResearchStaff();
        if (Objects.isNull(schoolMaster)) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long schoolId = schoolMaster.getManagedRegion().getSchoolIds().iterator().next();
        if(RuntimeMode.current().le(Mode.TEST)){
            schoolId = 75456L;
        }
        UtopiaCache cache = CacheSystem.CBS.getCache("storage");
        Object msgListObj = cache.load("SCHOOLMASTER_HOMEPAGE_MSGLIST_"+schoolId);
        LinkedList<String> msgList = null;
        if(Objects.isNull(msgListObj)){
            msgList = new LinkedList<>();
        }else{
            msgList = (LinkedList<String>)msgListObj;
        }

        result.add("result", true);
        result.add("data",msgList);
        return result;
    }



    /**
     * AI智能教育数据监管平台
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String dataPlatform() {
        ResearchStaff researchStaff = currentResearchStaff();
        if(researchStaff == null){
            return "redirect:"+getBaseDomain();
        }
        return "adminteacher/dataplatform/index";
    }

}
