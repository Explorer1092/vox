package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.AgentTag;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.AgentTargetTagService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AgentTagController
 *
 * @author song.wang
 * @date 2017/6/2
 */
@Controller
@RequestMapping("/mobile/tag")
public class AgentTagController extends AbstractAgentController{

    @Inject
    private AgentTargetTagService agentTargetTagService;
    @Inject
    private TeacherResourceService teacherResourceService;

    @RequestMapping("teacher_tag_page.vpage")
    @ResponseBody
    public MapMessage teacherTagPage(){
        MapMessage mapMessage = MapMessage.successMessage();
        Long teacherId = getRequestLong("teacherId");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        List<AgentTag> positionTags = AgentTag.fetchTeacherPositionTags();

        if(teacher.isKLXTeacher()){ // 初中数学老师和高中数学老师没有"学科组长"标签
            positionTags.remove(AgentTag.SUBJECT_LEADER);
        }

        List<Map<String,Object>> allTags = new ArrayList<>();
        positionTags.forEach(p ->{

        });

        List<Map<String,Object>> selectTags = new ArrayList<>();
        List<AgentTag> teacherTags = agentTargetTagService.loadTeacherTags(teacherId);

        for(AgentTag tag : positionTags){
            if(CollectionUtils.isEmpty(teacherTags) ||(CollectionUtils.isNotEmpty(teacherTags) && !teacherTags.contains(tag))){
                Map<String,Object> map = new HashMap<>();
                map.put("code",tag.getCode());
                map.put("desc",tag.getDesc());
                allTags.add(map);
            }
        }

        teacherTags.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put("code",p.getCode());
            map.put("desc",p.getDesc());
            selectTags.add(map);
        });
    /*    List<Map<String, Object>> dataList = new ArrayList<>();
        for(AgentTag tag : positionTags){
            Map<String, Object> dataItem = new HashMap<>();
            dataItem.put("tag", tag);
            if(CollectionUtils.isNotEmpty(teacherTags) && teacherTags.contains(tag)){
                dataItem.put("isSelected", true);
            }else {
                dataItem.put("isSelected", false);
            }
            dataList.add(dataItem);
        }*/

        mapMessage.put("allTags",allTags);
        mapMessage.put("selectTags",selectTags);
        return mapMessage;
    }

    @RequestMapping(value = "save_teacher_tags.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacherTags(){
        Long teacherId = getRequestLong("teacherId");

        String[] tagStrList = getRequestString("tags").split(",");
        List<AgentTag> tagList = new ArrayList<>();
        for(String tagStr : tagStrList){
            if(StringUtils.isNotBlank(tagStr) && AgentTag.codeOf(Integer.valueOf(tagStr)) != null){
                tagList.add(AgentTag.codeOf(Integer.valueOf(tagStr)));
            }
        }
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(getCurrentUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        boolean result = agentTargetTagService.saveTeacherTags(teacherId, tagList);
        MapMessage message = new MapMessage();
        message.setSuccess(result);
        if(!result){
            message.setInfo("操作失败！");
        }
        return message;
    }
}
