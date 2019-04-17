package com.voxlearning.washington.controller;

import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.vendor.api.DPVendorService;
import com.voxlearning.washington.net.message.exam.GetQuestionByIdsRequest;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 试卷预览controller, 用于一起测市场试卷预览
 * @author majianxin
 * @version V1.0
 * @date 2018/9/7
 */
@Controller
@RequestMapping("newexam/marketing")
public class PreviewPaperController extends AbstractController {

    @ImportService(interfaceClass = DPVendorService.class)
    private DPVendorService dpVendorService;

    /**
     * 页面信息
     */
    @RequestMapping(value = "newpaperinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperInfo() {
        MapMessage message;
        String paperIds = getRequestString("paperIds");
        String sig = getRequestString("sig");
        String appKey = getRequestString("appKey");

        if (StringUtils.isBlank(paperIds) || StringUtils.isBlank(sig) || StringUtils.isBlank(appKey)) {
            return MapMessage.errorMessage("请提供正确的参数");
        }
        //验证请求合法性
        Map<String, String> request = Maps.newHashMap();
        request.put("paperIds", paperIds);
        MapMessage mapMessage = dpVendorService.isValidRequest(appKey, null, request, sig);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("请求非法");
        }
        try {
            message = newExamReportLoaderClient.fetchPaperInfo(Arrays.asList(paperIds.split(",")));
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    /**
     * 试卷题信息
     */
    @RequestMapping(value = "newpaper/questioninfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage paperQuestionInfo() {
        MapMessage message;
        String paperId = getRequestString("paperId");
        String paperIds = getRequestString("paperIds");
        String sig = getRequestString("sig");
        String appKey = getRequestString("appKey");

        if (StringUtils.isBlank(paperId) || StringUtils.isBlank(paperIds) || StringUtils.isBlank(sig) || StringUtils.isBlank(appKey) || !Arrays.asList(paperIds.split(",")).contains(paperId)) {
            return MapMessage.errorMessage("请提供正确的参数");
        }
        //验证请求合法性
        Map<String, String> request = Maps.newHashMap();
        request.put("paperIds", paperIds);
        MapMessage mapMessage = dpVendorService.isValidRequest(appKey, null, request, sig);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("请求非法");
        }
        try {
            message = newExamReportLoaderClient.fetchPaperQuestionInfo(paperId);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
        return message;
    }

    // 通过试题ID列表查询试题列表
    @RequestMapping(value = "load/newquestion/byids.vpage")
    @ResponseBody
    public MapMessage loadQuestionByIds() {
        String paperIds = getRequestString("paperIds");
        String sig = getRequestString("sig");
        String appKey = getRequestString("appKey");

        if (StringUtils.isBlank(paperIds) || StringUtils.isBlank(sig) || StringUtils.isBlank(appKey)) {
            return MapMessage.errorMessage("请提供正确的参数");
        }
        //验证请求合法性
        Map<String, String> request = Maps.newHashMap();
        request.put("paperIds", paperIds);
        MapMessage mapMessage = dpVendorService.isValidRequest(appKey, null, request, sig);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage("请求非法");
        }

        GetQuestionByIdsRequest flashReq = getRequestObject(GetQuestionByIdsRequest.class);
        if (flashReq == null) {
            logger.error("flashReq is null, input: ", JsonUtils.toJson(getRequest().getParameterMap()));
            return MapMessage.errorMessage("json error");
        }
        return tikuStrategy.loadQuestionFilterAnswersByIds(flashReq.ids, flashReq.containsAnswer);
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("app_key", "17Agent");
        params.put("paperIds", "P_10200021980893");
        String reqSig = DigestSignUtils.signMd5(params, "YtfaPhAO0#95");
        System.out.println(reqSig);
    }
}
