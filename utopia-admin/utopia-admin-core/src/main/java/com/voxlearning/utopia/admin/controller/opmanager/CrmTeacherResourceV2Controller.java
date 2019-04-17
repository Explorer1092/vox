package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.admin.constant.UploadFileType;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.mapper.CrmLessionView;
import com.voxlearning.utopia.admin.util.UploadOssManageUtils;
import com.voxlearning.utopia.service.business.api.CrmNewTeacherResourceService;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/opmanager/teacher_resource/")
public class CrmTeacherResourceV2Controller extends AbstractAdminController {

    @ImportService(interfaceClass = CrmNewTeacherResourceService.class)
    private CrmNewTeacherResourceService crmNewTeacherResourceService;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    /**
     * 后台页面路由
     */
    @RequestMapping(value = "{filePath}/{ftlPath}.vpage", method = RequestMethod.GET)
    public String frontFtl(@PathVariable(value = "filePath") String filePath, @PathVariable(value = "ftlPath") String ftlPath, Model model) {
        Set<TeachingResourceTask> exclude = new HashSet<>(Arrays.asList(
                TeachingResourceTask.NONE,
                TeachingResourceTask.PRO_SURVIVAL_6,
                TeachingResourceTask.PRO_SURVIVAL_7
        ));
        List<ExLinkedHashMap<Object, Object>> taskList = Arrays.stream(TeachingResourceTask.values())
                .filter(i -> !exclude.contains(i))
                .map(i -> MapUtils.map("name", i.getConfigDesc(), "value", i.name()))
                .collect(Collectors.toList());

        List<ExLinkedHashMap<Object, Object>> workTypeList = Stream.of(
                TeachingResource.WorkType.无,
                TeachingResource.WorkType.布置作业,
                TeachingResource.WorkType.布置趣配音,
                TeachingResource.WorkType.布置自然拼读,
                TeachingResource.WorkType.布置绘本,
                TeachingResource.WorkType.布置假期作业,
                TeachingResource.WorkType.布置期末复习
        ).map(i -> MapUtils.map("name", i.name(), "value", i.name())).collect(Collectors.toList());

        model.addAttribute("taskList", taskList);
        model.addAttribute("workTypeList", workTypeList);

        return "opmanager/teachingres/" + filePath + "/" + ftlPath;
    }

    @ResponseBody
    @RequestMapping("list.vpage")
    public MapMessage list() {
        String id = getRequestString("id");
        String title = getRequestString("title");
        String subject = getRequestString("subject");
        String clazzLevel = getRequestString("clazz_level");
        String levelTerm = getRequestString("level_term");
        String category = getRequestString("category");
        String onlineStatus = getRequestString("online_status");
        String receiveLimit = getRequestString("receive_limit");
        String bookId = getRequestString("book_id");
        Integer page = getRequestInt("page", 1);
        Integer pageSize = getRequestInt("page_size", 10);
        String source = getRequestString("source");

        List<NewTeacherResource> newTeacherResources = crmNewTeacherResourceService.loadAll();
        Stream<NewTeacherResource> stream = newTeacherResources.stream();
        if (StringUtils.isNotBlank(id)) {
            stream = stream.filter(i -> Objects.equals(i.getId(), id));
        } else {
            if (StringUtils.isNotBlank(title)) {
                stream = stream.filter(i -> i.getTitle().contains(title));
            }
            if (StringUtils.isNotBlank(subject)) {
                Subject subjectEnum = Subject.ofWithUnknown(subject);
                stream = stream.filter(i -> Objects.equals(i.getSubject(), subjectEnum));
            }
            if (StringUtils.isNotBlank(clazzLevel)) {
                int clazzLevelInt = SafeConverter.toInt(clazzLevel);
                stream = stream.filter(i -> Objects.equals(i.getClazzLevel(), clazzLevelInt));
            }
            if (StringUtils.isNotBlank(levelTerm)) {
                int levelTermInt = SafeConverter.toInt(levelTerm);
                stream = stream.filter(i -> Objects.equals(i.getTermType(), levelTermInt));
            }
            if (StringUtils.isNotBlank(category)) {
                stream = stream.filter(i -> Objects.equals(i.getCategory(), category));
            }
            if (StringUtils.isNotBlank(onlineStatus)) {
                boolean onlineBoolean = SafeConverter.toBoolean(onlineStatus);
                stream = stream.filter(i -> Objects.equals(i.getOnline(), onlineBoolean));
            }
            if (StringUtils.isNotBlank(receiveLimit)) {
                boolean receiveLimitBool = SafeConverter.toBoolean(receiveLimit, false);
                stream = stream.filter(i -> Objects.equals(i.getReceiveLimit(), receiveLimitBool));
            }
            if (StringUtils.isNotBlank(bookId)) {
                stream = stream.filter(i -> Objects.equals(i.getBookId(), bookId));
            }
            if (StringUtils.isNotBlank(source)) {
                int sourceInt = SafeConverter.toInt(source);
                stream = stream.filter(i -> Objects.equals(i.getSource(), sourceInt));
            }
        }
        List<NewTeacherResource> collect = stream.sorted((o1, o2) -> o2.getUpdateAt().compareTo(o1.getUpdateAt())).collect(Collectors.toList());

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<NewTeacherResource> resources = PageableUtils.listToPage(collect, pageable);
        List<NewTeacherResource> content = resources.getContent();

        return MapMessage.successMessage().add("data", content)
                .add("hasNext", resources.hasNext())
                .add("totalPages", resources.getTotalPages())
                .add("totalElements", resources.getTotalElements());
    }

    @ResponseBody
    @RequestMapping("detail.vpage")
    public MapMessage detail() {
        String id = requestString("id");
        NewTeacherResource load = crmNewTeacherResourceService.load(id);
        return MapMessage.successMessage().add("data", load);
    }

    @ResponseBody
    @RequestMapping(value = "upsert.vpage", method = RequestMethod.POST)
    public MapMessage upsert(@RequestBody NewTeacherResource newTeacherResource) {
        return crmNewTeacherResourceService.upsert(newTeacherResource);
    }

    @ResponseBody
    @RequestMapping(value = "online_offline.vpage")
    public MapMessage onlineOffline() {
        String id = getRequestString("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        crmNewTeacherResourceService.onlineOffline(id);
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "book_list.vpage", method = RequestMethod.GET)
    public MapMessage bookList() {
        int term = getRequestInt("term");
        int clazz = getRequestInt("clazzLevel");
        String subjectString = getRequestString("subject");
        Subject subject = Subject.of(subjectString);

        if (subject == null || term <= 0 || clazz <= 0 || term > 2 || clazz > 6) {
            MapMessage.successMessage().set("books", Collections.emptyList());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<NewBookProfile> bookList = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesIdAndBookType(subject, ClazzLevel.parse(clazz), Term.of(term), null, null);

        if (CollectionUtils.isNotEmpty(bookList)) {
            Map<String, List<NewBookCatalog>> unitListMap = newContentLoaderClient.loadChildren(bookList.stream().map(NewBookProfile::getId).collect(Collectors.toList()), BookCatalogType.UNIT);

            for (NewBookProfile bookItem : bookList) {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("id", bookItem.getId());
                bookMap.put("name", bookItem.getName());

                List<NewBookCatalog> unitList = MapUtils.isNotEmpty(unitListMap) && CollectionUtils.isNotEmpty(unitListMap.get(bookItem.getId()))
                        ? unitListMap.get(bookItem.getId()) : Collections.emptyList();

                List<Map<String, String>> unit = new ArrayList<>();
                unitList.forEach(e -> {
                    Map<String, String> unitMap = new HashMap<>();
                    unitMap.put("unitId", e.getId());
                    unitMap.put("unitName", e.getName());
                    unit.add(unitMap);
                });
                bookMap.put("unitList", unit);
                result.add(bookMap);
            }
        }

        return MapMessage.successMessage().set("books", result);
    }

    @RequestMapping(value = "lessions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchLessionList(@RequestParam(name = "unitId") String unitId) {
        try {
            Map<String, List<NewBookCatalog>> lessionListMap = newContentLoaderClient.loadChildren(Collections.singletonList(unitId), BookCatalogType.LESSON);
            List<NewBookCatalog> lessionlist = lessionListMap.get(unitId);
            List<CrmLessionView> crmLessionViews = CrmLessionView.Builder.build(lessionlist);
            return MapMessage.successMessage().set("data", crmLessionViews);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "getsignature.vpage")
    @ResponseBody
    public MapMessage getSignature(HttpServletRequest request) {
        String ext = getRequestString("ext");
        UploadFileType uploadFileType;
        if (ext != null) {
            uploadFileType = UploadFileType.of(ext);
            if (uploadFileType.equals(UploadFileType.unsupported)) {
                return MapMessage.errorMessage("不支持的数据类型");
            }
        } else uploadFileType = UploadFileType.unsupported;

        MapMessage signatureResult = UploadOssManageUtils.getSignature(uploadFileType, "teacher_resource", getResponse());
        if (signatureResult != null)
            return MapMessage.successMessage().add("data", signatureResult);
        return MapMessage.errorMessage();
    }
}


