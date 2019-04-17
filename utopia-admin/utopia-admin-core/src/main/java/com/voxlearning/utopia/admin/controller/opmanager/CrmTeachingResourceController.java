package com.voxlearning.utopia.admin.controller.opmanager;

import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
// import com.voxlearning.utopia.service.user.api.mappers.TeachingResourceMapper;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.StringUtils.isEmpty;
import static com.voxlearning.alps.core.util.StringUtils.isNotEmpty;

/**
 * CRM - 教学资源
 *
 * @author haitian.gan
 */
@Controller
@RequestMapping("/opmanager/teachingresource")
public class CrmTeachingResourceController extends AbstractAdminController {

    @Inject private TeachingResourceLoaderClient teachingResLoader;
    @Inject private TeachingResourceServiceClient teachingResService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;

        String queryTitle = getRequestString("name");
        String querySubject = getRequestString("subject");
        String queryCategory = getRequestString("category");
        String queryUserVisitType = getRequestParameter("userVisitType","");
        String queryUserReceiveType = getRequestParameter("userReceiveType","");
        String action = getRequestString("action");
        String queryGrades = getRequestString("grade");

        //if("query".equals(action))
        //    page = 1;

        String queryOnlineStr = getRequestParameter("online","all");
        Boolean queryOnline = SafeConverter.toBoolean(queryOnlineStr);
        String queryPosition = getRequestString("position");

        Pageable pageable = new PageRequest(page - 1, 10);

        List<TeachingResourceMapper> allResources = teachingResLoader.loadAllResourcesRaw()
                .stream()
                .filter(r -> r.getSource() == null || Objects.equals(r.getSource(), TeachingResource.SOURCE_PLATFORM))
                .filter(r -> isEmpty(queryTitle) || (!isEmpty(r.getName()) && r.getName().contains(queryTitle)))
                .filter(r -> isEmpty(querySubject) || r.getSubject().contains(querySubject))
                .filter(r -> isEmpty(queryCategory) || Objects.equals(r.getCategory(),queryCategory))
                .filter(r -> "all".equals(queryOnlineStr) || queryOnline.equals(r.getOnline()))
                .filter(r -> isEmpty(queryPosition)
                        || "all".equals(queryPosition)
                        || ("featuring".equals(queryPosition) && SafeConverter.toBoolean(r.getFeaturing())))
                .filter(r -> isEmpty(queryGrades) || r.getGrade().contains(queryGrades))
                .filter(r -> StringUtils.isEmpty(queryUserVisitType) ||
                        ("All".equals(queryUserVisitType) && (r.getVisitLimited() == null || r.getVisitLimited() == TeachingResourceUserType.All)) ||
                        (!"All".equals(queryUserVisitType) && r.getVisitLimited() != null && r.getVisitLimited().name().equals(queryUserVisitType)))
                .filter(r -> StringUtils.isEmpty(queryUserReceiveType) ||
                        ("All".equals(queryUserReceiveType) && (r.getReceiveLimited() == null || r.getReceiveLimited() == TeachingResourceUserType.All)) ||
                        (!"All".equals(queryUserReceiveType) && r.getReceiveLimited() !=null && r.getReceiveLimited().name().equals(queryUserReceiveType)))
                .map(t -> {
                    TeachingResourceMapper mapper = new TeachingResourceMapper();
                    try {
                        BeanUtils.copyProperties(mapper,t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    mapper.setCategoryName(TeachingResource.Category.parse(t.getCategory()).getDesc());

                    if(isNotEmpty(t.getSubject())){
                        String subjectNames = Arrays.stream(t.getSubject().split(","))
                                .map(s -> TeachingResource.Subject.parse(s).getDesc())
                                .reduce((acc, item) -> acc + "," + item)
                                .orElse(null);
                        mapper.setSubjectNames(subjectNames);
                    }

                    return mapper;
                })
                .sorted((t1,t2) -> t2.getUpdateAt().compareTo(t1.getUpdateAt()))
                .collect(Collectors.toList());

        Page<TeachingResourceMapper> resources = PageableUtils.listToPage(allResources, pageable);

        model.addAttribute("resourceList", resources.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", resources.getTotalPages());
        model.addAttribute("hasPrev", resources.hasPrevious());
        model.addAttribute("hasNext", resources.hasNext());

        model.addAttribute("searchName", queryTitle);
        model.addAttribute("searchSubject", querySubject);
        model.addAttribute("searchCategory",queryCategory);
        model.addAttribute("searchOnline",queryOnlineStr);
        model.addAttribute("searchPosition",queryPosition);
        model.addAttribute("searchGrade",queryGrades);
        model.addAttribute("searchUserVisitType",queryUserVisitType);
        model.addAttribute("searchUseReceiveType",queryUserReceiveType);

        model.addAttribute("limitUserTypes", TeachingResourceUserType.values());

        return "opmanager/teachingres/resource_index";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String resourceInfo(Model model) {
        String resourceId = getRequestString("id");

        TeachingResource resource;
        // 新增的情况
        if(StringUtil.isNullOrEmpty(resourceId)){
            resource = new TeachingResource();
        }else {
            resource = teachingResLoader.loadResource(resourceId);
        }

        model.addAttribute("resource", resource);
        model.addAttribute("limitUserTypes", TeachingResourceUserType.values());
        model.addAttribute("forbidEditTask",resource.getOnlineAt() != null);
        TeachingResource.Category[] categories = {
                TeachingResource.Category.WEEK_WELFARE,
                TeachingResource.Category.SYNC_COURSEWARE,
                TeachingResource.Category.TEACHING_SPECIAL,
                TeachingResource.Category.COURSE_WARE,
                TeachingResource.Category.OUTSIDE_READING,
                TeachingResource.Category.TEST_PAPER,
                TeachingResource.Category.LECTURE,
        };
        model.addAttribute("categories", categories);
        model.addAttribute("labels", TeachingResource.Label.values());
        TeachingResource.WorkType[] workTypes = new TeachingResource.WorkType[]{
                TeachingResource.WorkType.无,
                TeachingResource.WorkType.布置作业,
                TeachingResource.WorkType.布置趣配音,
                TeachingResource.WorkType.布置自然拼读,
                TeachingResource.WorkType.布置绘本,
                TeachingResource.WorkType.布置假期作业,
                TeachingResource.WorkType.布置期末复习,
                TeachingResource.WorkType.推荐练习,
                TeachingResource.WorkType.推荐趣配音,
                TeachingResource.WorkType.推荐自然拼读,
                TeachingResource.WorkType.推荐绘本,
        };
        model.addAttribute("workType", workTypes);
        Set<TeachingResourceTask> tasksSet = new HashSet<>(Arrays.asList(TeachingResourceTask.values()));
        tasksSet.remove(TeachingResourceTask.PRO_SURVIVAL_6);
        tasksSet.remove(TeachingResourceTask.PRO_SURVIVAL_7);
        tasksSet.remove(TeachingResourceTask.RECRUIT_NEW);

        List<TeachingResourceTask> tasksList = new ArrayList<>(tasksSet);
        tasksList.sort((e1,e2) -> {
            return e2.getConfigDesc().compareTo(e1.getConfigDesc());
        });
        model.addAttribute("tasks", tasksSet);

        return "opmanager/teachingres/resource_info";
    }

    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResource(@RequestBody TeachingResource resource) {
        if (resource == null)
            return MapMessage.errorMessage("参数为空!");

        if(isEmpty(resource.getId())) {
            resource.setId(null);
            resource.setOnline(false);
        } else {
            TeachingResource existResource = teachingResLoader.loadResource(resource.getId());
            // 无论上下线反复操作多少次，上线时间始终为第一次上线的时间点
            resource.setOnlineAt(existResource.getOnlineAt());
        }
        resource.setSource(TeachingResource.SOURCE_PLATFORM);

        return teachingResService.upsertTeachingResource(resource);
    }

    @RequestMapping(value = "online.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOnLine(){
        String resourceId = getRequestString("resourceId");

        TeachingResource resource = teachingResLoader.loadResource(resourceId);
        if(resource == null)
            return MapMessage.errorMessage("资源不存在!");
        else if(resource.getOnlineAt() == null){
            // 只在第一次上线，置上上线时间字段
            resource.setOnlineAt(new Date());
        }

        resource.setOnline(true);
        return teachingResService.upsertTeachingResource(resource);
    }

    @RequestMapping(value = "offline.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offLine(){
        String resourceId = getRequestString("resourceId");

        TeachingResource resource = teachingResLoader.loadResource(resourceId);
        if(resource == null)
            return MapMessage.errorMessage("资源不存在!");

        resource.setOnline(false);
        return teachingResService.upsertTeachingResource(resource);
    }

    @RequestMapping(value = "get_cache_preview.vpage")
    @ResponseBody
    public MapMessage getCachePreview(){
        Long teacherId = getRequestLong("teacherId");
        List<String> data = teachingResLoader.testForRedis(teacherId);
        return MapMessage.successMessage().add("result",data);
    }

    /**
     * 瞬间完成任务,修复数据用
     * @return
     */
    @RequestMapping(value = "finish_task.vpage")
    @ResponseBody
    public MapMessage finishTask(){
        try {
            String taskId = getRequestString("taskId");
            Validate.notEmpty(taskId, "参数错误!");

            return teachingResService.finishTask(taskId);
        }catch(Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "query_task.vpage")
    public String queryTeacherTask(Model model){
        try{
            Long teacherId = getRequestLong("teacherId");
            Validate.isTrue(teacherId != 0L,"参数错误!");

            int page = getRequestInt("page", 1);
            page = page <= 0 ? 1 : page;

            model.addAttribute("queryTeacherId",teacherId);
            Map<String,TeachingResourceRaw> resourceMap = teachingResLoader.loadResourceMap();

            List<Map<String,Object>> tasks = teachingResLoader.loadTeacherTasks(teacherId)
                    .stream()
                    // 倒序排列
                    .sorted((t1,t2) -> t2.getCreateAt().compareTo(t1.getCreateAt()))
                    .map(t -> MapUtils.m(
                            "id",t.getId(),
                            "name",resourceMap.get(t.getResourceId()).getName(),
                            "task",t.getTask(),
                            "status",t.getStatus(),
                            "createAt",t.getCreateAt()))
                    .collect(Collectors.toList());

            Pageable pageable = new PageRequest(page - 1, 10);
            Page<Map<String,Object>> tasksPage = PageableUtils.listToPage(tasks, pageable);

            model.addAttribute("taskList", tasksPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", tasksPage.getTotalPages());
            model.addAttribute("hasPrev", tasksPage.hasPrevious());
            model.addAttribute("hasNext", tasksPage.hasNext());

        }catch (Exception e){
            model.addAttribute("hasPrev", false);
            model.addAttribute("hasNext", false);
            model.addAttribute("totalPage",0);
            model.addAttribute("error",e.getMessage());
        }

        return "opmanager/teachingres/teacher_task";
    }
}
