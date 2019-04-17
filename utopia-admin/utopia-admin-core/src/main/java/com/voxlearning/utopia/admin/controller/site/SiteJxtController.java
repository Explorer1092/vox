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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.vendor.api.entity.JxtExtTab;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016/4/12
 */
@Controller
@RequestMapping(value = "/site/jxt")
@Slf4j
public class SiteJxtController extends SiteAbstractController {

    @Inject
    private JxtLoaderClient jxtLoaderClient;

    @Inject
    private JxtServiceClient jxtServiceClient;

    @Inject
    protected CrmImageUploader crmImageUploader;


    @RequestMapping(value = "getjxtexttablist.vpage", method = RequestMethod.GET)
    public String getJxtExtTabList(Model model) {
        List<JxtExtTab> extTabList = jxtLoaderClient.getAllJxtExtTabList();
        model.addAttribute("extTabList", extTabList);
        return "site/jxt/jxtexttablist";

    }

    /**
     * 进入tab编辑页面
     */
    @RequestMapping(value = "jxtexttabedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String tabEdit(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            JxtExtTab extTab = jxtLoaderClient.getJxtExtTab(id);
            if (extTab != null) {
                model.addAttribute("extTab", extTab).addAttribute("url", combineImgUrl(extTab.getImg()));
            }
        }
        return "site/jxt/jxttabedit";
    }

    /**
     * 保存tab
     */
    @RequestMapping(value = "jxtexttabsave.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tabEditSave() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String desc = getRequestString("desc");
        Long tabType = getRequestLong("tabType");
        String link = getRequestString("link");
        Integer linkType = getRequestInt("linkType");
        String img = getRequestString("img");
        String mainFunctionName = getRequestString("mainFunctionName");
        String subFunctionName = getRequestString("subFunctionName");
        Integer rank = getRequestInt("rank");
        Boolean showMessageCount = getRequestBool("showMessageCount");
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        Date startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd HH:mm");
        Date endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd HH:mm");

        JxtExtTab jxtexttab;
        jxtexttab = jxtLoaderClient.getJxtExtTab(id);
        if (jxtexttab == null) {
            //判断填的tabType是否与库里已有的tab的tabType相同。相同则报错
            List<JxtExtTab> allJxtExtTabList = jxtLoaderClient.getAllJxtExtTabList();
            if (CollectionUtils.isNotEmpty(allJxtExtTabList) && allJxtExtTabList.stream().anyMatch(p -> p.getTabType().equals(tabType))) {
                return MapMessage.errorMessage("tab类型不能重复");
            }
            jxtexttab = new JxtExtTab();
            jxtexttab.setOnline(Boolean.FALSE);
        }
        jxtexttab.setName(name);
        jxtexttab.setDesc(desc);
        jxtexttab.setTabType(tabType);
        jxtexttab.setLink(link);
        jxtexttab.setLinkType(linkType);

        jxtexttab.setImg(img);
        jxtexttab.setMainFunctionName(mainFunctionName);
        jxtexttab.setSubFunctionName(subFunctionName);
        jxtexttab.setRank(rank);
        jxtexttab.setShowMessageCount(showMessageCount);
        jxtexttab.setStartDate(startDate);
        jxtexttab.setEndDate(endDate);
        return jxtServiceClient.saveJxtExtTab(jxtexttab);
    }

    /**
     * 上线tab
     */
    @RequestMapping(value = "onlinejxttab.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage onlineTab() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }
        JxtExtTab jxtExtTab = jxtLoaderClient.getJxtExtTab(id);
        if (jxtExtTab == null) {
            return MapMessage.errorMessage("id为{}的tab不存在", id);
        }
        return jxtServiceClient.onlineJxtExtTab(id);
    }


    /**
     * 下线tab
     */
    @RequestMapping(value = "offlinejxttab.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage offlineTab() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }
        JxtExtTab jxtExtTab = jxtLoaderClient.getJxtExtTab(id);
        if (jxtExtTab == null) {
            return MapMessage.errorMessage("id为{}的tab不存在", id);
        }
        return jxtServiceClient.offlineJxtExtTab(id);
    }


    /**
     * 编辑时上传图片
     */
    @RequestMapping(value = "edituploadimage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage editUploadImage(MultipartFile imgFile) {
        //上传图片
        //返回预览的完整路径和图片的相对路径
        MapMessage mapMessage = new MapMessage();
        if (imgFile.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = imgFile.getOriginalFilename();

        String prefix = "tabicon-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
        try {
            @Cleanup InputStream inStream = imgFile.getInputStream();
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
            mapMessage.add("url", combineImgUrl(filename));
            mapMessage.add("fileName", filename);
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            log.error("上传icon图片异常： " + ex.getMessage());
        }
        return mapMessage;
    }

    @RequestMapping(value = "zhaoying.vpage", method = RequestMethod.GET)
    public String zhaoYing() {
        //家长端测试接口
        return "site/jxt/zhaoying";
    }

    @RequestMapping(value = "zhaoying_test.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage zhaoYingTest() {
        String url = StringUtils.trim(getRequestString("url"));
        Integer isPost = getRequestInt("post");
        String app = getRequestString("app");
        Integer paramCount = getRequestInt("paramCount");
        if (StringUtils.isBlank(url)) {
            return MapMessage.errorMessage("url不能为空");
        }
        Map<String, String> paramMap = new HashMap<>();
        if (paramCount > 0) {
            for (Integer i = 1; i <= paramCount; i++) {
                String param = getRequestString("param_" + i.toString());
                String value = getRequestString("value_" + i.toString());
                if (StringUtils.isNotBlank(param)) {
                    paramMap.put(param, value);
                } else {
                    return MapMessage.errorMessage("参数名不能为空");
                }
            }
        }
        String secretKey;
        if ("17Parent".equals(app)) {
            secretKey = "iMMrxI3XMQtd";
        } else if ("17Student".equals(app)) {
            secretKey = "kuLwGZMJBcQj";
        } else if ("17Teacher".equals(app)) {
            secretKey = "gvUKQN1EFXKp";
        } else {
            return MapMessage.errorMessage("App选择错误");
        }
        paramMap.put("app_key", app);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        String apiURL = "http://www.test.17zuoye.net/" + url;
        apiURL = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response;
        if (isPost == 1) {
            response = HttpRequestExecutor.defaultInstance().post(apiURL).execute();
        } else {
            response = HttpRequestExecutor.defaultInstance().get(apiURL).execute();
        }
        System.out.println(response.getResponseString());
        return MapMessage.successMessage().add("resStr", response.getResponseString());

    }

    //生成预览url
    private String combineImgUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        //使用cdn地址而不是主站地址
        String prePath = "https://" + ProductConfig.getCdnDomainAvatar();
        return prePath + "/gridfs/" + url;
    }
}
