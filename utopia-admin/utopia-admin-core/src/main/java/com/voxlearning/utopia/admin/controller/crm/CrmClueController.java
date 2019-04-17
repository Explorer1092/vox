package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherClueService;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CrmClueController
 *
 * @author song.wang
 * @date 2016/8/6
 */
@Controller
@RequestMapping("/crm/clue")
public class CrmClueController extends CrmAbstractController{

    @Inject private CrmTeacherClueService crmTeacherClueService;


    @RequestMapping(value = "add_clue.vpage")
    @ResponseBody
    public MapMessage addClue() {

        String targetType = requestString("targetType");
        if(StringUtils.isBlank(targetType)){
            return MapMessage.errorMessage();
        }

        if("teacher".equals(targetType)){
            return addTeacherClue();
        }

        return MapMessage.successMessage();
    }

    private MapMessage addTeacherClue() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        Set<Long> teacherIds = requestLongSet("targetIds");
        CrmClueType clueType = CrmClueType.nameOf(requestString("clueType"));
        if(clueType == null || CollectionUtils.isEmpty(teacherIds)){
            return MapMessage.errorMessage();
        }
        return crmTeacherClueService.addTeacherClue(user.getAdminUserName(), clueType, teacherIds);
    }

    @RequestMapping(value = "clue_list.vpage")
    public String clueList(Model model){

        Date createStart = requestDate("createStart");
        Date createEnd = requestDate("createEnd");
        CrmClueType clueType = CrmClueType.nameOf(requestString("type"));


        if(createEnd == null){
            createEnd = new Date();
        }
        if(createStart == null){
            createStart = DateUtils.calculateDateDay(createEnd, -7);
        }
        if(clueType == null){
            clueType = CrmClueType.values()[0];
        }


        List<CrmTeacherClue> teacherClueList = new ArrayList<>();
        if(CrmClueType.核实老师认证 == clueType){
            teacherClueList = crmTeacherClueService.findByType(clueType, createStart, createEnd);
        }

        model.addAttribute("clueList", convertToDataList(teacherClueList));
        model.addAttribute("createStart", formatDate(createStart));
        model.addAttribute("createEnd", formatDate(createEnd));
        model.addAttribute("type", clueType);
        model.addAttribute("clueTypes", CrmClueType.values());
        return "crm/clue/clue_list";

    }

    private List<Map<String, Object>> convertToDataList(List<CrmTeacherClue> teacherClueList){
        if(CollectionUtils.isEmpty(teacherClueList)){
            return Collections.emptyList();
        }
        List<Long> teacherIdList = teacherClueList.stream().map(CrmTeacherClue::getTeacherId).collect(Collectors.toList());
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIdList);
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> itemMap = null;
        for(CrmTeacherClue teacherClue : teacherClueList){
            itemMap = new HashMap<>();
            itemMap.put("type", teacherClue.getType());
            itemMap.put("schoolId", teacherClue.getSchoolId());
            itemMap.put("schoolName", teacherClue.getSchoolName());
            itemMap.put("teacherId", teacherClue.getTeacherId());
            itemMap.put("teacherName", teacherClue.getTeacherName());
            itemMap.put("subject", teacherClue.getSubject());
            itemMap.put("creator", teacherClue.getCreator());
            itemMap.put("receiver", teacherClue.getReceiver());
            itemMap.put("createTime", teacherClue.getCreateTime() == null ? "" : DateUtils.dateToString(teacherClue.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
            itemMap.put("updateTime", teacherClue.getUpdateTime() == null ? "" : DateUtils.dateToString(teacherClue.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            Teacher teacher = teacherMap.get(teacherClue.getTeacherId());
            if(teacher != null){
                itemMap.put("authState", teacher.fetchCertificationState());
            }
            retList.add(itemMap);
        }
        return retList;
    }
}
