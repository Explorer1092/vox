package com.voxlearning.utopia.mizar.controller.order;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.partner.api.entity.ThirdPartyUserInfoRef;
import com.voxlearning.galaxy.service.partner.api.mapper.ThirdPartyConfigMapper;
import com.voxlearning.galaxy.service.partner.api.service.ThirdPartyService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.XssfUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/6/7
 */
@Controller
@RequestMapping(value = "thirdParty/userInfo")
public class ThirdPartyUserInfoController extends AbstractMizarController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;


    private String thirdPartyInfoKey;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Mode current = com.voxlearning.alps.runtime.RuntimeMode.current();
        if (current == Mode.DEVELOPMENT)
            current = Mode.TEST;
        thirdPartyInfoKey = "thirdPartyInfoList_" + current.name();
    }

    /**
     * 查询购买第三方产品的用户信息
     */
    @RequestMapping(value = "infoList.vpage", method = RequestMethod.GET)
    public String findPicOrderCount(Model model) {
        String accountName = getCurrentUser().getAccountName();
        ThirdPartyConfigMapper partyConfigMapper = getThirdTypeIdByUserName(accountName);
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1)) - 1;
        if (partyConfigMapper == null) {
            model.addAttribute("error", "用户信息错误");
            return "/order/thirdparty/thirdpartyuserinfo";
        }
        if (partyConfigMapper.getThirdPartyId() == null) {
            model.addAttribute("error", "未找到对应的第三方信息");
            return "/order/thirdparty/thirdpartyuserinfo";
        }
        model.addAttribute("thirdPartyName", partyConfigMapper.getDesc());
        Pageable pageable = new PageRequest(pageIndex, 100);
        Page<ThirdPartyUserInfoRef> thirdPartyListByDate = thirdPartyService.getThirdPartyListByPage(partyConfigMapper.getThirdPartyId(), pageable);
        model.addAttribute("returnList", generateReturnList(thirdPartyListByDate.getContent(), pageIndex, accountName));
        model.addAttribute("pageIndex", pageIndex + 1);
        model.addAttribute("totalPages", thirdPartyListByDate.getTotalPages());
        return "/order/thirdparty/thirdpartyuserinfo";
    }


    /**
     * 下载当天的订单数据
     */
    @RequestMapping(value = "downloadData.vpage", method = RequestMethod.GET)
    public void downloadOrderDetail() {
        String accountName = getCurrentUser().getAccountName();
        ThirdPartyConfigMapper partyConfigMapper = getThirdTypeIdByUserName(accountName);
        if (partyConfigMapper == null) {
            logger.warn("下载第三方数据失败。检查pageBlock的配置");
            return;
        }
        String fileName = partyConfigMapper.getDesc() + "数据详情-" + DateUtils.dateToString(new Date()) + ".xlsx";
        List<List<String>> dataList = generateDownLoadData(partyConfigMapper.getThirdPartyId(), accountName);
        XSSFWorkbook xssfWorkbook = convertDateDataToHSSfWorkbook(dataList);
        fileName = XssfUtils.generateFilename(fileName);
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException e) {
            logger.error("download third party data error!", e);
        }

    }


    private ThirdPartyConfigMapper getThirdTypeIdByUserName(String accountName) {
        List<PageBlockContent> parentTabConfigList = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("thirdPartyInfo");
        if (CollectionUtils.isEmpty(parentTabConfigList)) {
            return null;
        }
        PageBlockContent thirdPartyConfig = parentTabConfigList.stream().filter(t -> thirdPartyInfoKey.equals(t.getBlockName())).findFirst().orElse(null);
        if (thirdPartyConfig == null) {
            return null;
        }
        String tabConfigContent = thirdPartyConfig.getContent();
        if (StringUtils.isBlank(tabConfigContent)) {
            return null;
        }
        List<ThirdPartyConfigMapper> thirdPartyConfigMappers = new ArrayList<>();
        try {
            thirdPartyConfigMappers = JsonUtils.fromJsonToList(tabConfigContent.replaceAll("\n|\r|\t", "").trim(), ThirdPartyConfigMapper.class);
            if (CollectionUtils.isEmpty(thirdPartyConfigMappers)) {
                return null;
            }
        } catch (Exception e) {
            logger.warn("thirdPartyConfigMappers json error!!", e);
            return null;
        }

        ThirdPartyConfigMapper thirdPartyConfigMapper = thirdPartyConfigMappers.stream().filter(e -> StringUtils.equals(e.getAccountName(), accountName)).findFirst().orElse(null);
        if (thirdPartyConfigMapper == null) {
            return null;
        }
        return thirdPartyConfigMapper;
    }


    private List<Map<String, Object>> generateReturnList(List<ThirdPartyUserInfoRef> thirdPartyListByDate, int pageIndex, String accountName) {
        if (CollectionUtils.isEmpty(thirdPartyListByDate)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        int index = 1;
        for (ThirdPartyUserInfoRef e : thirdPartyListByDate) {
            Map<String, Object> map = new HashMap<>();
            map.put("index", pageIndex * 100 + index);
            map.put("childName", e.getChildName());
            map.put("childAge", SafeConverter.toInt(e.getChildAge()) != 0 ? e.getChildAge() : "");
            map.put("clazzLevel", SafeConverter.toInt(e.getClazzLevel()) != 0 ? e.getClazzLevel() : "");
            String mobile = "";
            if (e.getMobile() != null) {
                mobile = sensitiveUserDataServiceClient.loadThirdPartyUserMobile(e.getId(), "ThirdPartyUserInfoController/findPicOrderCount", accountName);
            }
            map.put("mobile", mobile);
            map.put("date", DateUtils.dateToString(e.getCreateDatetime()));
            if (e.getRegionCode() != null && e.getRegionCode() != 0) {
                ExRegion exRegion = raikouSystem.loadRegion(e.getRegionCode());
                if (exRegion != null) {
                    map.put("region", exRegion.getProvinceName());
                }
            } else {
                map.put("region", e.getRegionName());
            }
            map.put("device", e.getDevice());
            returnList.add(map);
            index++;
        }
        return returnList;
    }

    private List<List<String>> generateDownLoadData(Integer thirdPartyId, String accountName) {
        List<ThirdPartyUserInfoRef> thirdPartyUserInfos = thirdPartyService.exportThirdPartyList(thirdPartyId);
        if (CollectionUtils.isEmpty(thirdPartyUserInfos)) {
            return Collections.emptyList();
        }
        List<List<String>> dataList = new ArrayList<>();
        thirdPartyUserInfos = thirdPartyUserInfos.stream().sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).collect(Collectors.toList());
        int index = 1;
        for (ThirdPartyUserInfoRef userInfo : thirdPartyUserInfos) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(index));
            String mobile = sensitiveUserDataServiceClient.loadThirdPartyUserMobile(userInfo.getId(), "ThirdPartyUserInfoController/downloadOrderDetail", accountName);
            list.add(mobile);
            list.add(userInfo.getChildName());
            list.add(SafeConverter.toInt(userInfo.getChildAge()) != 0 ? SafeConverter.toString(userInfo.getChildAge()) : "");
            list.add(SafeConverter.toInt(userInfo.getClazzLevel()) != 0 ? SafeConverter.toString(userInfo.getClazzLevel()) : "");
            if (userInfo.getRegionCode() != null && userInfo.getRegionCode() != 0) {
                ExRegion exRegion = raikouSystem.loadRegion(userInfo.getRegionCode());
                if (exRegion != null) {
                    list.add(exRegion.getProvinceName());
                }
            } else {
                list.add(userInfo.getRegionName());
            }
            list.add(DateUtils.dateToString(userInfo.getCreateDatetime()));
            list.add(userInfo.getDevice());
            dataList.add(list);
            index++;
        }
        return dataList;
    }

    private XSSFWorkbook convertDateDataToHSSfWorkbook(List<List<String>> dataList) {
        String[] dateDataTitle = new String[]{
                "序号", "手机号", "学生姓名", "学生年龄", "学生年级", "学生地区", "报名时间", "学习设备"
        };
        int[] dateDataWidth = new int[]{
                5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000
        };
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {
            xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList);
        } catch (Exception e) {
            logger.error("generate order detail xlsx error!", e);
        }
        return xssfWorkbook;
    }

}
