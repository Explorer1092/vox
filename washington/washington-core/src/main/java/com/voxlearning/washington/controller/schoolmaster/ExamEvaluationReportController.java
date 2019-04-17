package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.rstaff.api.constans.EvaluationModule;
import com.voxlearning.utopia.service.rstaff.api.constans.ItemAnalysisModule;
import com.voxlearning.utopia.service.rstaff.consumer.SchoolMasterServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 测评报告
 */
@Controller
@RequestMapping("/exam/evaluationReport")
public class ExamEvaluationReportController extends SchoolMasterBaseController{

    @Inject
    private SchoolMasterServiceClient schoolMasterServiceClient;

    @Inject private EmailServiceClient emailServiceClient;

    private final static String SECRET = "ob3ZkwDyJ02AsNvV";

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/testport_yiqi/rstaff.vpage";
    }

    /*
    * 教研员
    * */
    @RequestMapping(value = "rstaff.vpage", method = RequestMethod.GET)
    public String rstaffReport(Model model) {
        return "adminteacherreport/rstaff";
    }

    /*
    * 校长
    * */
    @RequestMapping(value = "schoolmaster.vpage", method = RequestMethod.GET)
    public String schoolmasterReport(Model model) {
        return "adminteacherreport/schoolmaster";
    }

    /**
     * marketing查看测评报告接口
     * @param model
     * @return
     */
    @RequestMapping(value = "marketingtestreport.vpage", method = RequestMethod.GET)
    public String marketingTestReport(Model model) {
        String examId = getRequestString("examId");
        String regionLevel = getRequestString("regionLevel");
        String regionCode = getRequestString("regionCode");
        String sig = getRequestString("sig");
        Map<String, String> params = new LinkedHashMap<>();
        params.put("examId",examId);
        params.put("regionLevel",regionLevel);
        params.put("regionCode",regionCode);
        String mySig = DigestSignUtils.signMd5(params,SECRET);
        if(Objects.equals(sig,mySig)){
            return "adminteacherreport/rstaff";
        }else{
            return "common/mobileerrorinfo";
        }
    }



    /*
    * 题目质量分析
    * */
    @RequestMapping(value = "subjectquality.vpage", method = RequestMethod.GET)
    public String subjectQualityReport(Model model) {
        String paperId = getRequestString("paperId");
        model.addAttribute("paperId",paperId);
        String sig = getRequestString("sig");
        Map<String, String> params = new LinkedHashMap<>();
        params.put("paperId",paperId);
        String mySig = DigestSignUtils.signMd5(params,SECRET);
        if(Objects.equals(sig,mySig)){
            return "adminteacherreport/subjectquality";
        }else{
            return "common/mobileerrorinfo";
        }
    }


    /**
     * 获得测评报告的数据，根据模块的名称
     * @param model
     * @return
     */
    @RequestMapping(value = "loadEvaluationReportByMoudleName.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadEvaluationReportByMoudleName(Model model) {
        MapMessage result = new MapMessage();
        try{
            String regionLevel =getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");
            String examId = getRequestString("examId");
            String moduleName = getRequestString("moduleName");

            EvaluationModule module = EvaluationModule.valueOf(moduleName);
            switch (module){
                case EvaluationStruct:
                    moduleName = EvaluationModule.EvaluationStruct.moduleFunc;
                    break;
                case EvaluationSurvey:
                    moduleName = EvaluationModule.EvaluationSurvey.moduleFunc;
                    break;
                case ProjectImplSituation:
                    moduleName = EvaluationModule.ProjectImplSituation.moduleFunc;
                    break;
                case StudentWholeSituation:
                    moduleName = EvaluationModule.StudentWholeSituation.moduleFunc;
                    break;
                case StudentSubjectSituation:
                    moduleName = EvaluationModule.StudentSubjectSituation.moduleFunc;
                    break;
                case Suggest:
                    moduleName = EvaluationModule.Suggest.moduleFunc;
                    break;
                case AttachedList:
                    moduleName = EvaluationModule.AttachedList.moduleFunc;
                    break;
                default:
                    break;
            }

            String apiURL =  "http://10.7.4.240:8116/api/v1/assessmentReport";
            if (RuntimeMode.ge(Mode.STAGING)) {
                apiURL = "http://yqc.17zuoye.net/api/v1/assessmentReport";
            }
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("regionLevel",regionLevel);
            paramMap.put("regionCode", regionCode);
            paramMap.put("examId",examId);
            paramMap.put("moduleName",moduleName);
            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
            String retStr = response.getResponseString();
            Map dataMap = JsonUtils.fromJson(retStr);

            result.add("result",dataMap.get("success"));
            result.add("info",dataMap.get("msg"));
            result.add("dataMap",dataMap.get("dataMap"));
        }catch(Exception e){
            logger.error("获得测评报告数据失败",e);
            result.add("result",false);
            result.add("info","暂无数据");
        }
        return result;
    }

    /**
     * 获得题目分析报告的数据，根据模块的名称
     * @param model
     * @return
     */
    @RequestMapping(value = "loadItemAnalysisReportByMoudleName.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadItemAnalysisReportByMoudleName(Model model) {
        MapMessage result = new MapMessage();
        try{
            String paperId = getRequestString("paperId");
            String moduleName = getRequestString("moduleName");

            ItemAnalysisModule module = ItemAnalysisModule.valueOf(moduleName);
            switch (module){
                case ItemAnalysisStruct:
                    moduleName = ItemAnalysisModule.ItemAnalysisStruct.moduleFunc;
                    break;
                case ExamPaperReliability:
                    moduleName = ItemAnalysisModule.ExamPaperReliability.moduleFunc;
                    break;
                case ItemAnalysisResult:
                    moduleName = ItemAnalysisModule.ItemAnalysisResult.moduleFunc;
                    break;
                case NotGoodItemAnalysisResult:
                    moduleName = ItemAnalysisModule.NotGoodItemAnalysisResult.moduleFunc;
                    break;
                case SetAQuestionSuggest:
                    moduleName = ItemAnalysisModule.SetAQuestionSuggest.moduleFunc;
                    break;
                default:
                    break;
            }

            String apiURL =  "http://10.7.4.240:8116/api/v1/itemReport";
            if (RuntimeMode.ge(Mode.STAGING)) {
                apiURL = "http://yqc.17zuoye.net/api/v1/itemReport";
            }
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("paperId",paperId);
            paramMap.put("moduleName",moduleName);
            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
            String retStr = response.getResponseString();
            Map dataMap = JsonUtils.fromJson(retStr);

            result.add("result",dataMap.get("success"));
            result.add("info",dataMap.get("msg"));
            result.add("dataMap",dataMap.get("dataMap"));
        }catch(Exception e){
            logger.error("获得题目分析报告失败",e);
            result.add("result",false);
            result.add("info","暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "checkReportData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkReportData(){
        MapMessage result = new MapMessage();
        try{
            String reportType = getRequestString("reportType");
            String paperId = getRequestString("paperId");
            String examId = getRequestString("examId");
            String regionLevel = getRequestString("regionLevel");
            String regionCode = getRequestString("regionCode");

            String apiURL =  "http://10.7.4.240:8116/api/v1/itemReport";
            if (RuntimeMode.ge(Mode.STAGING)) {
                apiURL = "http://yqc.17zuoye.net/api/v1/itemReport";
            }
            Map<String, String> paramMap = new HashMap<>();
            if(Objects.equals(reportType,"itemReport")){
                paramMap.put("moduleName","loadItemAnalysisStruct");
                paramMap.put("paperId",paperId);
                paramMap.put("reportType","itemReport");
            }else{
                apiURL =  "http://10.7.4.240:8116/api/v1/assessmentReport";
                if (RuntimeMode.ge(Mode.STAGING)) {
                    apiURL = "http://yqc.17zuoye.net/api/v1/assessmentReport";
                }
                paramMap.put("moduleName","loadEvaluationStruct");
                paramMap.put("examId",examId);
                paramMap.put("regionLevel",regionLevel);
                paramMap.put("regionCode",regionCode);
                paramMap.put("reportType","assessmentReport");
            }
            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
            String retStr = response.getResponseString();
            Map dataMap = JsonUtils.fromJson(retStr);

            result.add("result",dataMap.get("success"));
            result.add("info",dataMap.get("msg"));
        }catch(Exception e){
            logger.error("调用checkReportData失败",e);
            result.add("result",false);
            result.add("info","请求失败");
        }
        return result;
    }


    @RequestMapping(value = "sendEmail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendEmail(){
        MapMessage result = new MapMessage();
        try{
            String title = getRequestString("title");
            String content = getRequestString("content");
            emailServiceClient.createPlainEmail()
                    .to("yqcfeedback@17zuoye.com")
                    .subject(title)
                    .body(content)
                    .send();
            result.add("result",true);
            result.add("info","邮件发送成功");
        }catch(Exception e){
            logger.error("邮件发送失败",e);
            result.add("result",false);
            result.add("info","邮件发送失败");
        }
        return result;
    }

}
