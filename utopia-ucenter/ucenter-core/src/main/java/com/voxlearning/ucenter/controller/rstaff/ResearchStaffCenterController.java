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

package com.voxlearning.ucenter.controller.rstaff;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.ArgMapper;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.LogisticType;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.mappers.UserShippingAddressMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * research staff center controller
 *
 * @author Longlong Yu
 * @since 下午7:24,13-9-17.
 */
@Controller
@RequestMapping("/rstaff/center")
public class ResearchStaffCenterController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * NEW 教研员个人中心
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        Long rstaffId = currentUserId();
        ResearchStaff researchStaff = currentResearchStaff();
        model.addAttribute("ktwelve", researchStaff.getKtwelve());
        MapMessage message = userServiceClient.generateUserShippingAddress(rstaffId);
        model.addAttribute("userShippingAddressMapper", message.get("address"));
        model.addAttribute("mobile", message.get("mobile"));

        return "rstaffv3/center/index";
    }

    /**
     * NEW 教研员个人信息编辑
     */
    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    String edit(Model model) {

        Long rstaffId = currentUserId();

        MapMessage message = userServiceClient.generateUserShippingAddress(rstaffId);
        model.addAttribute("userShippingAddressMapper", message.get("address"));
        model.addAttribute("mobile", message.get("mobile"));

        // 收货信息
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        List<ArgMapper> provinces = new ArrayList<>(regionList.size());
        for (ExRegion region : regionList) {
            ArgMapper mapper = new ArgMapper();
            mapper.setKey(region.getCode());
            mapper.setValue(region.getName());
            provinces.add(mapper);
        }
        if (!provinces.isEmpty()) {
            model.addAttribute("provinces", provinces);
        }
        return "rstaffv3/center/edit";
    }

    /**
     * NEW 教研员重置密码
     */
    @RequestMapping(value = "editPassword.vpage", method = RequestMethod.GET)
    String editPassword() {
        return "rstaffv3/center/editpassword";
    }

    /**
     * NEW 教研员提交个人信息
     */
    @RequestMapping(value = "submitrstaffinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRStaffInfo(@RequestBody Map<String, Object> paramsMap) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return MapMessage.errorMessage();
        }
        UserShippingAddress userShippingAddress = userLoaderClient.loadUserShippingAddress(researchStaff.getId());
        if (userShippingAddress == null)
            userShippingAddress = new UserShippingAddress();

        try {
            validate(paramsMap, userShippingAddress);
        } catch (IllegalArgumentException iaEx) {
            return MapMessage.errorMessage(iaEx.getMessage());
        } catch (Exception e) {
            return MapMessage.errorMessage("用户信息不合规范");
        }

        //修改姓名
        String newName = paramsMap.get("userName").toString();
        if (researchStaff.getProfile() != null
                && !researchStaff.getProfile().getRealname().equals(newName)) {
            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
            if (!userServiceClient.changeName(researchStaff.getId(), newName).isSuccess()) {

                com.voxlearning.alps.spi.bootstrap.LogCollector.info("backend-general", MiscUtils.map("usertoken", researchStaff.getId(),
                        "usertype", researchStaff.getUserType(),
                        "platform", "pc",
                        "version", "",
                        "op", "change user name",
                        "mod1", researchStaff.fetchRealname(),
                        "mod2", newName,
                        "mod3", researchStaff.getAuthenticationState()));

                return MapMessage.errorMessage("修改用户名失败");
            }
        }
        // 更新收货地址
        userShippingAddress.setUserId(researchStaff.getId());
        userShippingAddress.setDisabled(false);
        MapMessage message = userServiceClient.updateUserShippingAddress(userShippingAddress);
        if (message.isSuccess()) {
            return message.add("userName", newName);
        } else {
            return message.setInfo("保存用户信息失败！");
        }
    }

    /**
     * NEW 教研员
     * 保存提交个人信息
     * 用于商城--提交保存收货地址表单
     *
     * @param command 表单参数
     * @return 返回地址
     */
    @RequestMapping(value = "address.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage address(@RequestBody UserShippingAddressMapper command) {
        Long userId = currentUserId();
        Long provinceCode = command.getProvinceCode();
        String provinceName = command.getProvinceName();
        Long cityCode = command.getCityCode();
        String cityName = command.getCityName();
        Long countyCode = command.getCountyCode();
        String countyName = command.getCountyName();
        String detailAddress = command.getDetailAddress();
        String phone = command.getPhone();
        String postCode = command.getPostCode();
        Long id = command.getId();
        String logisticType = command.getLogisticType();

        if (StringUtils.isBlank(provinceName)) {
            return MapMessage.errorMessage("请选择省份！");
        }
        if (StringUtils.isBlank(cityName)) {
            return MapMessage.errorMessage("请选择城市！");
        }
        if (StringUtils.isBlank(countyName)) {
            return MapMessage.errorMessage("请选择区县！");
        }
        if (StringUtils.isBlank(detailAddress)) {
            return MapMessage.errorMessage("请填写详细地址！");
        }
        if (StringUtils.isBlank(phone)) {
            return MapMessage.errorMessage("请填写联系电话！");
        }
        if (StringUtils.isBlank(postCode)) {
            return MapMessage.errorMessage("请填写邮编！");
        }

        UserShippingAddress userShippingAddress = new UserShippingAddress();
        userShippingAddress.setId(id);
        userShippingAddress.setUserId(userId);
        userShippingAddress.setProvinceCode(provinceCode);
        userShippingAddress.setProvinceName(provinceName);
        userShippingAddress.setCityCode(cityCode);
        userShippingAddress.setCityName(cityName);
        userShippingAddress.setCountyCode(countyCode);
        userShippingAddress.setCountyName(countyName);
        userShippingAddress.setDetailAddress(detailAddress);
        userShippingAddress.setPostCode(postCode);
        userShippingAddress.setSensitivePhone(phone);
        userShippingAddress.setLogisticType(LogisticType.valueOf(logisticType));
        MapMessage message = userServiceClient.updateUserShippingAddress(userShippingAddress);
        if (!message.isSuccess()) {
            return message.setInfo("您的收货地址信息保存失败！");
        } else {
            return message.setInfo("您的收货地址信息保存成功！");
        }
    }

    //--------------------------private method-------------------------------------------//

    private void validate(Map<String, Object> paramsMap, UserShippingAddress userShippingAddress) {

        String userName = paramsMap.get("userName").toString().replaceAll("\\s", "");
        Validate.notEmpty(userName, "请填写姓名");

        ResearchStaff researchStaff = researchStaffLoaderClient.loadResearchStaff(currentUserId());
        if (researchStaff.getSubject() != Subject.MATH && (researchStaff.isResearchStaffForCounty() || researchStaff.isResearchStaffForCity())) {
            String provinceName = paramsMap.get("provinceName").toString();
            String cityName = paramsMap.get("cityName").toString();
            String countyName = paramsMap.get("countyName").toString();
            String detailAddress = paramsMap.get("detailAddress").toString().replaceAll("\\s", "");
            String postCode = paramsMap.get("postCode").toString().replaceAll("\\s", "");
            String phone = paramsMap.get("phone").toString().replaceAll("\\s", "");
            String logisticType = paramsMap.get("logisticType").toString();

            Validate.notEmpty(provinceName, "请选择省份");
            Validate.notEmpty(cityName, "请选择城市");
            Validate.notEmpty(countyName, "请选择区县");
            Validate.notEmpty(detailAddress, "请填写详细地址");
            Validate.notEmpty(postCode, "请填写邮政编码");
            Validate.notEmpty(phone, "请填写联系电话");
            Validate.notEmpty(logisticType, "请选择配送方式");

            userShippingAddress.setProvinceCode(Long.parseLong(paramsMap.get("provinceCode").toString()));
            userShippingAddress.setProvinceName(provinceName);
            userShippingAddress.setCityCode(Long.parseLong(paramsMap.get("cityCode").toString()));
            userShippingAddress.setCityName(cityName);
            userShippingAddress.setCountyCode(Long.parseLong(paramsMap.get("countyCode").toString()));
            userShippingAddress.setCountyName(countyName);
            userShippingAddress.setDetailAddress(detailAddress);
            userShippingAddress.setPostCode(postCode);
            userShippingAddress.setSensitivePhone(phone);
            userShippingAddress.setLogisticType(LogisticType.valueOf(logisticType));
        }

        paramsMap.put("userName", userName);
    }
}
