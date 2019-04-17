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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.feedback.client.DislocationGroupServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroup;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroupDetail;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author fugui.chang
 * @since 2016-10-12 20:17
 * 提供给风控组的功能
 */

@Controller
@RequestMapping("/crm/riskcontrol")
public class CrmRiskControlController extends CrmAbstractController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private DislocationGroupServiceClient dislocationGroupServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    //异常学生标记
    @RequestMapping(value = "abnormalclazz.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String abnormalclazz(Model model) {
        return "crm/riskcontrol/abnormalclazz";
    }

    @RequestMapping(value = "abnormalclazzList.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String abnormalclazzList(Model model) {
        Long groupId = SafeConverter.toLong(getRequestParameter("groupId", "").replaceAll("\\s", ""));
        Long realSchoolId = SafeConverter.toLong(getRequestParameter("realSchoolId", "").replaceAll("\\s", ""));
        String beginTime = getRequestParameter("beginTime", "").trim();
        String endTime = getRequestParameter("endTime", "").trim();
        int currentPage = SafeConverter.toInt(getRequestParameter("currentPage", ""));

        List<DislocationGroup> dislocationGroupDetailList = new LinkedList<>();
        if (groupId != 0) {
            DislocationGroupDetail dislocationGroupDetail = dislocationGroupServiceClient.getDislocationGroupService()
                    .loadDislocationGroupDetailByGroupId(groupId)
                    .getUninterruptibly();
            if (dislocationGroupDetail != null) {
                dislocationGroupDetailList.add(dislocationGroupDetail);
            }
        } else if (realSchoolId != 0) {
            List<DislocationGroupDetail> dislocationGroupDetails = dislocationGroupServiceClient.getDislocationGroupService()
                    .loadDislocationGroupDetailsByRealSchoolId(realSchoolId)
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(dislocationGroupDetails)) {
                dislocationGroupDetailList.addAll(dislocationGroupDetails);
            }
        } else if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
            Date beginDate = null;
            Date endDate = null;
            try {
                beginDate = DateUtils.parseDate(beginTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
                endDate = DateUtils.parseDate(endTime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
            } catch (ParseException e) {
                //日期解析错误,提示日期格式的写法
            }
            List<DislocationGroupDetail> dislocationGroupDetails = dislocationGroupServiceClient.getDislocationGroupService()
                    .loadDislocationGroupDetailsByTime(beginDate, endDate)
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(dislocationGroupDetails)) {
                dislocationGroupDetailList.addAll(dislocationGroupDetails);
            }
        } else {
            model.addAttribute("nodata", true);
            return "crm/riskcontrol/abnormalclazz";
        }
        if (CollectionUtils.isNotEmpty(dislocationGroupDetailList)) {
            dislocationGroupDetailList.sort(((o1, o2) -> o1.getUpdateDatetime().before(o2.getUpdateDatetime()) ? 1 : -1));
        }

        if (currentPage == 0) {
            currentPage = 1;
        }

        int pageSize = 50;
        int beginIndex = pageSize * (currentPage - 1);
        int endIndex = pageSize * currentPage;
        if (endIndex > dislocationGroupDetailList.size()) {
            endIndex = dislocationGroupDetailList.size();
        }
        int totalPage;
        if (dislocationGroupDetailList.size() % pageSize == 0) {
            totalPage = dislocationGroupDetailList.size() / pageSize;
        } else {
            totalPage = dislocationGroupDetailList.size() / pageSize + 1;
        }

        model.addAttribute("dislocationGroupDetailList", dislocationGroupDetailList.subList(beginIndex, endIndex));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("dataIndex", beginIndex);

        Map<String, String> pars = new HashedMap<>();
        pars.put("groupId", groupId.toString());
        pars.put("realSchoolId", realSchoolId.toString());
        pars.put("beginTime", beginTime);
        pars.put("endTime", endTime);
        model.addAttribute("conditions", pars);
        if (CollectionUtils.isEmpty(dislocationGroupDetailList)) {
            model.addAttribute("nodata", true);
        }
        return "crm/riskcontrol/abnormalclazz";
    }

    @RequestMapping(value = "searchschool.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage searchSchool() { //根据groupId查询对应的学校信息 或者 根据schoolId查询学校信息;这两个条件不是同时使用
        Long groupId = getRequestLong("groupId");
        Long realSchoolId = getRequestLong("realSchoolId");
        if (groupId == 0 && realSchoolId == 0) {
            return MapMessage.errorMessage("输入参数有误");
        }

        Long schoolId = null;
        if (groupId != 0) {
            GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, false);
            if (groupMapper == null) {
                return MapMessage.errorMessage("GroupId:" + groupId + "不存在");
            }

            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(groupMapper.getClazzId());
            if (clazz == null) {
                return MapMessage.errorMessage("groupId:" + groupId + "没有对应的班级");
            }
            schoolId = clazz.getSchoolId();
        }

        if (realSchoolId != 0) {
            schoolId = realSchoolId;
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("groupId:" + groupId + "没有对应的学校");
        }

        return MapMessage.successMessage().add("schoolName", school.getCname()).add("schoolId", school.getId());
    }

    @RequestMapping(value = "adddislocationgroup.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addDislocationGroup() {
        Long groupId = getRequestLong("groupId");
        Long realSchoolId = getRequestLong("realSchoolId");
        String operationNotes = getRequestString("operationNotes").trim();
        if (groupId == 0 || realSchoolId == 0 || StringUtils.isBlank(operationNotes)) {
            return MapMessage.errorMessage("新增数据时参数有误");
        }
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setGroupId(groupId);
        dislocationGroup.setRealSchoolId(realSchoolId);
        dislocationGroup.setNotes(operationNotes);
        dislocationGroup.setLatestOperator(getCurrentAdminUser().getAdminUserName());
        MapMessage mapMessage = dislocationGroupServiceClient.getDislocationGroupService()
                .createDislocationGroup(dislocationGroup)
                .getUninterruptibly();
        if (mapMessage.isSuccess()) {
            //adminlog
            addAdminLog("新增异常班级标记", groupId, "User:" + getCurrentAdminUser().getAdminUserName() + ";operationNotes" + operationNotes);
        }
        return mapMessage;
    }

    @RequestMapping(value = "deletedislocationgroup.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteDislocationGroup() {
        Long groupId = getRequestLong("groupId");
        String operationNotes = getRequestString("operationNotes").trim();
        if (groupId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.isBlank(operationNotes)) {
            return MapMessage.errorMessage("请填写备注");
        }
        MapMessage mapMessage = dislocationGroupServiceClient.getDislocationGroupService()
                .disableDislocationGroupByGroupId(groupId, operationNotes, getCurrentAdminUser().getAdminUserName())
                .getUninterruptibly();
        if (mapMessage.isSuccess()) {
            addAdminLog("删除异常班级标记", groupId, "User:" + getCurrentAdminUser().getAdminUserName() + ";operationNotes" + operationNotes);
        }
        return mapMessage;
    }

    @RequestMapping(value = "updatedislocationgroup.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateDislocationGroup() {
        Long id = getRequestLong("id");
        Long groupId = getRequestLong("groupId");
        Long realSchoolId = getRequestLong("realSchoolId");
        String operationNotes = getRequestString("operationNotes").trim();
        if (id == 0 || groupId == 0 || realSchoolId == 0 || StringUtils.isBlank(operationNotes)) {
            return MapMessage.errorMessage("更新数据时参数有误");
        }
        DislocationGroup dislocationGroup = new DislocationGroup();
        dislocationGroup.setId(id);
        dislocationGroup.setGroupId(groupId);
        dislocationGroup.setRealSchoolId(realSchoolId);
        dislocationGroup.setNotes(operationNotes);
        dislocationGroup.setLatestOperator(getCurrentAdminUser().getAdminUserName());
        MapMessage mapMessage = dislocationGroupServiceClient.getDislocationGroupService()
                .updateDislocationGroup(dislocationGroup)
                .getUninterruptibly();
        if (mapMessage.isSuccess()) {
            addAdminLog("更新异常班级标记", groupId, "User:" + getCurrentAdminUser().getAdminUserName() + ";operationNotes:" + operationNotes + ";realSchoolId:" + realSchoolId);
        }
        return mapMessage;
    }

}
