package com.voxlearning.utopia.mizar.controller.teachingresource;

import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceMapper;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
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

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isEmpty;
import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isNotEmpty;


@Controller
@RequestMapping("jiangxi/teachingresource")
public class CrmTeachingResourceController extends AbstractMizarController {
    @Inject
    private TeachingResourceLoaderClient teachingResLoader;
    @Inject
    private TeachingResourceServiceClient teachingResService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;

        String queryTitle = getRequestString("name");
        //String querySubject = getRequestString("subject");
        String queryCategory = getRequestString("category");
        String queryUserVisitType = requestString("userVisitType", "");
        String queryUserReceiveType = requestString("userReceiveType", "");
        String action = getRequestString("action");
        String queryGrades = getRequestString("grade");

        //if("query".equals(action))
        //    page = 1;

        String queryOnlineStr = requestString("online", "all");
        Boolean queryOnline = SafeConverter.toBoolean(queryOnlineStr);
        String queryPosition = getRequestString("position");

        Pageable pageable = new PageRequest(page - 1, 10);

        List<TeachingResourceMapper> allResources = teachingResLoader.loadAllResourcesRaw()
                .stream()
                .filter(r -> Objects.equals(r.getSource(), TeachingResource.SOURCE_JIANGXI))
                .filter(r -> isEmpty(queryTitle) || (!isEmpty(r.getName()) && r.getName().contains(queryTitle)))
                //.filter(r -> isEmpty(querySubject) || r.getSubject().contains(querySubject))
                .filter(r -> isEmpty(queryCategory) || Objects.equals(r.getCategory(), queryCategory))
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
                        (!"All".equals(queryUserReceiveType) && r.getReceiveLimited() != null && r.getReceiveLimited().name().equals(queryUserReceiveType)))
                .map(t -> {
                    TeachingResourceMapper mapper = new TeachingResourceMapper();
                    try {
                        BeanUtils.copyProperties(mapper, t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    mapper.setCategoryName(TeachingResource.Category.parse(t.getCategory()).getDesc());

                    if (isNotEmpty(t.getSubject())) {
                        String subjectNames = Arrays.stream(t.getSubject().split(","))
                                .map(s -> TeachingResource.Subject.parse(s).getDesc())
                                .reduce((acc, item) -> acc + "," + item)
                                .orElse(null);
                        mapper.setSubjectNames(subjectNames);
                    }

                    return mapper;
                })
                .sorted((t1, t2) -> t2.getUpdateAt().compareTo(t1.getUpdateAt()))
                .collect(Collectors.toList());

        Page<TeachingResourceMapper> resources = PageableUtils.listToPage(allResources, pageable);

        model.addAttribute("resourceList", resources.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", resources.getTotalPages());
        model.addAttribute("hasPrev", resources.hasPrevious());
        model.addAttribute("hasNext", resources.hasNext());

        model.addAttribute("searchName", queryTitle);
        //model.addAttribute("searchSubject", querySubject);
        model.addAttribute("searchCategory", queryCategory);
        model.addAttribute("searchOnline", queryOnlineStr);
        model.addAttribute("searchPosition", queryPosition);
        model.addAttribute("searchGrade", queryGrades);
        model.addAttribute("searchUserVisitType", queryUserVisitType);
        model.addAttribute("searchUseReceiveType", queryUserReceiveType);

        model.addAttribute("limitUserTypes", TeachingResourceUserType.values());

        return "opmanager/teachingres/resource_index";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String resourceInfo(Model model) {
        String resourceId = getRequestString("id");

        TeachingResource resource;
        // 新增的情况
        if (StringUtil.isNullOrEmpty(resourceId)) {
            resource = new TeachingResource();
        } else {
            resource = teachingResLoader.loadResource(resourceId);
        }

        model.addAttribute("resource", resource);
        model.addAttribute("limitUserTypes", TeachingResourceUserType.values());
        model.addAttribute("forbidEditTask", resource.getOnlineAt() != null);
        TeachingResource.Category[] categories = {
                TeachingResource.Category.IMPORTANT_CASE,
                TeachingResource.Category.GROW_UP,
                TeachingResource.Category.ACTIVITY_NOTICE,
                TeachingResource.Category.OTHER_STONE
        };
        model.addAttribute("categories", categories);
        model.addAttribute("labels", TeachingResource.Label.values());
        model.addAttribute("workType", TeachingResource.WorkType.values());
        Set<TeachingResourceTask> tasksSet = new HashSet<>();
        tasksSet.add(TeachingResourceTask.FREE);
        tasksSet.add(TeachingResourceTask.NONE);
        model.addAttribute("tasks", tasksSet);
        return "opmanager/teachingres/resource_info";
    }

    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResource(@RequestBody TeachingResource resource) {
        if (resource == null)
            return MapMessage.errorMessage("参数为空!");

        if (isEmpty(resource.getId())) {
            resource.setId(null);
            resource.setOnline(false);
        } else {
            TeachingResource existResource = teachingResLoader.loadResource(resource.getId());
            // 无论上下线反复操作多少次，上线时间始终为第一次上线的时间点
            resource.setOnlineAt(existResource.getOnlineAt());
        }
        resource.setSource(TeachingResource.SOURCE_JIANGXI);
        resource.setSubject("CHINESE,MATH,ENGLISH"); // 江西版不包括学科设置, 直接全保存
        resource.setValidityPeriod(365); // 任务有效期都是免费的,随便设置一年吧

        return teachingResService.upsertTeachingResource(resource);
    }

    @RequestMapping(value = "online.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getOnLine() {
        String resourceId = getRequestString("resourceId");

        TeachingResource resource = teachingResLoader.loadResource(resourceId);
        if (resource == null)
            return MapMessage.errorMessage("资源不存在!");
        else if (resource.getOnlineAt() == null) {
            // 只在第一次上线，置上上线时间字段
            resource.setOnlineAt(new Date());
        }

        resource.setOnline(true);
        return teachingResService.upsertTeachingResource(resource);
    }

    @RequestMapping(value = "offline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offLine() {
        String resourceId = getRequestString("resourceId");

        TeachingResource resource = teachingResLoader.loadResource(resourceId);
        if (resource == null)
            return MapMessage.errorMessage("资源不存在!");

        resource.setOnline(false);
        return teachingResService.upsertTeachingResource(resource);
    }

}
