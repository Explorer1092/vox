package com.voxlearning.utopia.admin.controller.equator.sapling;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.equator.service.configuration.api.entity.sapling.TreeConfig;
import com.voxlearning.equator.service.configuration.client.ResourceExcelTableServiceClient;
import com.voxlearning.equator.service.configuration.client.SaplingConfigInfoClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.annotation.ResourceDownloadType;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableDigest;
import com.voxlearning.equator.service.rubik.api.client.SaplingInSchoolsLoaderClient;
import com.voxlearning.equator.service.rubik.api.xmlvo.sapling.vo.SaplingClassmateVo;
import com.voxlearning.equator.service.sapling.api.client.SaplingLoaderClient;
import com.voxlearning.equator.service.sapling.api.client.SaplingServiceClient;
import com.voxlearning.equator.service.sapling.api.constants.sapling.SaplingStatus;
import com.voxlearning.equator.service.sapling.api.entity.UserSaplingLetterInfo;
import com.voxlearning.equator.service.sapling.api.entity.UserTreeMaturityInfo;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author xiaoying.han
 * @CreateDate 2018/11/27
 */
@Controller
@RequestMapping(value = "equator/newwonderland/sapling")
public class UserSaplingManagerController extends AbstractEquatorController {
    @Inject
    private SaplingLoaderClient saplingLoaderClient;
    @Inject
    private SaplingServiceClient saplingServiceClient;
    @Inject
    private SaplingConfigInfoClient saplingConfigInfoClient;
    @Inject
    private SaplingInSchoolsLoaderClient saplingInSchoolsLoaderClient;

    @Inject
    private ResourceExcelTableServiceClient resourceExcelTableServiceClient;

    private static String DEFAULT_USER_IMAGE_NAME = "https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png";

    private static String TEST_HOST = "http://cdn-portrait.test.17zuoye.net/gridfs/";

    private static String ONLINE_HOST = "https://cdn-portrait.17zuoye.cn/gridfs/";

    @RequestMapping(value = "saplingInfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getSaplingInfo(Model model) {
        String responseString = "equator/sapling/index";
        Long studentId = getRequestLong("studentId");
        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        if (studentId == 0) {
            return responseString;
        }

        MapMessage mapMessage = saplingLoaderClient.getRemoteReference().getUserSaplingInfo(studentId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", "数据不正确");
            return responseString;
        }

        model.addAttribute("treeData", mapMessage);
        if (StringUtils.equals(SafeConverter.toString(mapMessage.get("status")), SaplingStatus.growing.name())) {
            //获取树的配置信息
            TreeConfig treeConfig = saplingConfigInfoClient.getTreeConfigFromBuffer()
                    .stream()
                    .filter(s -> StringUtils.equals(SafeConverter.toString(mapMessage.get("treeId")), s.getId()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("treeConfig", treeConfig);
            model.addAttribute("treeStage", treeConfig.fetchStageHeights());
        }
        //已收集的树
        UserTreeMaturityInfo userTreeMaturityInfo = saplingLoaderClient.getRemoteReference().loadUserTreeMaturityInfo(studentId).getUninterruptibly();
        if (userTreeMaturityInfo != null && !userTreeMaturityInfo.getMaturitySaplings().isEmpty()) {
            Map<String, UserTreeMaturityInfo.TreeMaturityInfo> maturityInfoMap = userTreeMaturityInfo.getMaturitySaplings();
            model.addAttribute("treeMaturity", maturityInfoMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        }

        return responseString;
    }

    /**
     * 新增阳光
     *
     * @return
     */
    @RequestMapping(value = "acquiresubjectsuns.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSun() {
        Long studentId = getRequestLong("studentId");
        String sunType = getRequestString("sunType");
        int sunNum = getRequestInt("sunNum");
        Map<String, Object> sunData = new HashMap<>();
        sunData.put("sunType", sunType);
        sunData.put("sunNum", sunNum);
        try {
            return saplingServiceClient.getRemoteReference().acquireSubjectSuns(studentId, sunData, getCurrentAdminUser().getAdminUserName());
//            return saplingServiceClient.getRemoteReference().acquireSubjectSuns(studentId, typeSun);
        } catch (Exception e) {
            e.printStackTrace();
            return MapMessage.errorMessage("添加阳光失败，请联系管理员。");
        }
    }

    @RequestMapping(value = "classmatecircle.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String classmateCircle(Model model) {
        String responseString = "equator/sapling/classmatecircle";
        Long studentId = getRequestLong("studentId");
        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        if (studentId == 0) {
            return responseString;
        }

        MapMessage mapMessage = saplingInSchoolsLoaderClient.getSaplingInSchoolsLoader().classmateCircleList(studentId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", "数据不正确");
            return responseString;
        }

        List<Map<String, Object>> mapList = ((List<SaplingClassmateVo>) mapMessage.getOrDefault("clazzCircleList", Collections.EMPTY_LIST)).stream().map(classmateVo -> {
            Map<String, Object> currentObject = JsonUtils.safeConvertObjectToMap(classmateVo);
            currentObject.put("imgUrl", fetchImageUrlByDefault((String) currentObject.getOrDefault("imgUrl", "")));
            return currentObject;
        }).collect(Collectors.toList());
        model.addAttribute("clazzCircleList", mapList);

        return responseString;
    }

    @RequestMapping(value = "letterproanswer.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String letterProAnswer(Model model) {
        String responseString = "/equator/sapling/letteranswer";
        Long studentId = getRequestLong("studentId");
        String saplingCommId = getRequestString("saplingCommId");
        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        model.addAttribute("saplingCommId", StringUtils.isBlank(saplingCommId) ? "" : saplingCommId);
        if (studentId == 0 || StringUtils.isBlank(saplingCommId)) {
            return responseString;
        }
        MapMessage mapMessage = saplingLoaderClient.getRemoteReference().fetchLetterAnswer(studentId, saplingCommId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", "数据不正确");
            return responseString;
        }
        List<UserSaplingLetterInfo.UserLetter> letterList = (List<UserSaplingLetterInfo.UserLetter>) mapMessage.getOrDefault("letterAnswer", Collections.emptyList());
        //获取青苗答题内容-CDN资源
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService()
                .loadResourceTableDigestListNotDisabledFromDb().getUninterruptibly();
        String category = "Sapling";
        String tableName = "TreeQuestion";
        ResourceTableDigest resourceTableDigest = resourceTableDigestList.stream().filter(s -> StringUtils.equals(s.getCategory(), category)).filter(s -> StringUtils.equals(s.getTableName(), tableName)).findFirst().orElse(null);
        if (resourceTableDigest == null || resourceTableDigest.fetchResourceType() != ResourceDownloadType.CDN) {
            model.addAttribute("error", "青苗答题配置不正确");
            return responseString;
        }
        String httpResult = HttpRequestExecutor.defaultInstance()
                .get(resourceTableDigest.getUrl())
                .execute()
                .getResponseString();
        List<Map> listData = JsonUtils.fromJsonToList(httpResult, Map.class);
        Map<String, String> contentMap = listData.stream().collect(HashMap::new, (m, v) ->
                m.put(SafeConverter.toString(v.get("id")), SafeConverter.toString(v.get("content"))), HashMap::putAll);
        Map<String, String> answerMap = listData.stream().collect(HashMap::new, (m, v) ->
                m.put(SafeConverter.toString(v.get("id")), SafeConverter.toString(v.get("answer"))), HashMap::putAll);
        List letterProAnswer = letterList.stream().filter(t -> StringUtils.isNotEmpty(t.getProId())).map(t -> {
            Map<String, Object> proAnswer = new HashMap<>();
            proAnswer.put("letterId", t.getLetterId());
            proAnswer.put("stage", t.getStage());
            proAnswer.put("proId", t.getProId());
            proAnswer.put("proContent", contentMap.getOrDefault(t.getProId(), ""));
            proAnswer.put("answerContent", t.getAnswerContent());
            proAnswer.put("randomFlag", t.getRandomFlag());
            proAnswer.put("randomAnswer", getRandomAnswer(answerMap.getOrDefault(t.getProId(), ""), t.getAnswerFlag()));
            return proAnswer;
        }).collect(Collectors.toList());
        model.addAttribute("letterProAnswer", letterProAnswer);
        return responseString;
    }


    // 头像拼接
    private String fetchImageUrlByDefault(String imgUrl) {
        if (StringUtils.isNotEmpty(imgUrl) && !imgUrl.contains("avatar_normal")) {
            return RuntimeMode.le(Mode.TEST) ? TEST_HOST + imgUrl.replace("gridfs/", "") : ONLINE_HOST + imgUrl.replace("gridfs/", "");
        } else {
            return DEFAULT_USER_IMAGE_NAME;
        }
    }

    private String getRandomAnswer(String answerOption, String answerFlag) {
        String[] answerArr = answerOption.split("#");
        if (StringUtils.equals("A", answerFlag)) {
            return answerArr[0];
        } else if (StringUtils.equals("B", answerFlag)) {
            return answerArr[1];
        } else if (StringUtils.equals("C", answerFlag)) {
            return answerArr[2];
        }
        return "";
    }
}
