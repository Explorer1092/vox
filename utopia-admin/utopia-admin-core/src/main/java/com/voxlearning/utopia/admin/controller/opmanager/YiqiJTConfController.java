package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.mapper.yiqijt.*;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTChoiceNote;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseCatalog;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseOuterchain;
import com.voxlearning.utopia.service.campaign.client.YiqiJTServiceClient;
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
import java.util.stream.Stream;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * 一起新讲堂配置
 */
@Controller
@RequestMapping("/opmanager/17JTConf")
public class YiqiJTConfController extends AbstractAdminController {
    @Inject private YiqiJTServiceClient yiqiJTServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;

        String queryTitle = getRequestString("name");
        int querySubject = getRequestInt("subject");
        int queryGrades = getRequestInt("grade");

        //if("query".equals(action))
        //    page = 1;

        int queryOnline = getRequestInt("status",0);

        Pageable pageable = new PageRequest(page - 1, 10);

        Map<Long, Set<Integer>> gradeMap = yiqiJTServiceClient.getAllGradeMap();
        Map<Long, Set<Integer>> subjectMap = yiqiJTServiceClient.getAllSubjectMap();
        List<YiqiJTConfCourseListMapper> allCourses = yiqiJTServiceClient.load17JTCourseList()
                .stream()
                .filter(r -> Objects.equals(r.getSource(), TeachingResource.SOURCE_PLATFORM))
                .filter(r -> isEmpty(queryTitle) || (!isEmpty(r.getTitle()) && r.getTitle().contains(queryTitle)))
                .filter(r ->  {
                    if (querySubject == 0) {
                        return true;
                    }
                    return Optional.ofNullable(subjectMap).isPresent()
                            && subjectMap.containsKey(r.getId())
                            && subjectMap.get(r.getId()) != null
                            && subjectMap.get(r.getId()).contains(querySubject);
                })
                .filter(r -> queryOnline==0 || queryOnline==r.getStatus())
                .filter(r -> {
                    if(queryGrades == 0) {
                        return true;
                    }
                    return Optional.ofNullable(gradeMap).isPresent()
                            && gradeMap.containsKey(r.getId())
                            && gradeMap.get(r.getId()) != null
                            && gradeMap.get(r.getId()).contains(queryGrades);
                })
                .map(t -> {
                    YiqiJTConfCourseListMapper mapper = new YiqiJTConfCourseListMapper();
                    try {
                        BeanUtils.copyProperties(mapper,t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    if (Optional.ofNullable(gradeMap).isPresent() && gradeMap.containsKey(t.getId())) {
                        mapper.setGradeNames(gradeMap.get(t.getId())
                                .stream()
                                .map(s -> ClazzLevel.getDescription(s))
                                .reduce((names, item) -> names + ", " + item)
                                .get());
                    }

                    if (Optional.ofNullable(subjectMap).isPresent() && subjectMap.containsKey(t.getId())) {
                        mapper.setSubjectNames(subjectMap.get(t.getId())
                                .stream()
                                .map(s -> Subject.fromSubjectId(s).getValue())
                                .reduce((names, item) -> names + ", " + item)
                                .get());
                    }

                    return mapper;
                })
                .collect(Collectors.toList());

        Page<YiqiJTConfCourseListMapper> resources = PageableUtils.listToPage(allCourses, pageable);

        model.addAttribute("resourceList", resources.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", resources.getTotalPages());
        model.addAttribute("hasPrev", resources.hasPrevious());
        model.addAttribute("hasNext", resources.hasNext());

        model.addAttribute("searchName", queryTitle);
        model.addAttribute("searchSubject", querySubject);
        model.addAttribute("searchOnline",queryOnline);
        model.addAttribute("searchGrade",queryGrades);

        model.addAttribute("limitUserTypes", TeachingResourceUserType.values());

        return "opmanager/17jt/course_index";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        long courseId = NumberUtils.toLong( getRequestString("id"));

        YiqiJTConfCourseMapper course = new YiqiJTConfCourseMapper();
        // 新增的情况
        if (courseId != 0) {
            YiqiJTCourse jtCourse = yiqiJTServiceClient.loadCourseById(courseId);
            try {
                BeanUtils.copyProperties(course, jtCourse);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            course.setCataloglist(yiqiJTServiceClient.getCourseCatalogsByCourseId(courseId)
                    .stream()
                    .map(t -> {
                        YiqiJTConfCourseCatalogMapper mapper = new YiqiJTConfCourseCatalogMapper();
                        try {
                            BeanUtils.copyProperties(mapper, t);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return mapper;
                    })
                    .sorted(Comparator.comparing(YiqiJTConfCourseCatalogMapper::getTimeNodeSec))
                    .collect(Collectors.toList()));
            course.setChoiceNoteList(yiqiJTServiceClient.getCourseNotesByCourseId(courseId)
                    .stream()
                    .map(t -> {
                        YiqiJTConfChoiceNoteMapper mapper = new YiqiJTConfChoiceNoteMapper();
                        try {
                            BeanUtils.copyProperties(mapper, t);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return mapper;
                    })
                    .collect(Collectors.toList()));
            course.setGrade(yiqiJTServiceClient.getGradeIdsByCourseId(courseId)
                    .stream()
                    .map(s -> String.valueOf(s))
                    .reduce((ids, id) -> ids + "," + id)
                    .orElse(null));
            course.setSubject(yiqiJTServiceClient.getSubjectIdsByCourseId(courseId)
                    .stream().map(s -> String.valueOf(s))
                    .reduce((ids, id) -> ids + "," + id)
                    .orElse(null));
            course.setOuterchainList(yiqiJTServiceClient.getCourseOuterchainsByCourseId(courseId)
                    .stream()
                    .map(s -> {
                        YiqiJTConfCourseOuterchainMapper outerchainMapper = new YiqiJTConfCourseOuterchainMapper();
                        try {
                            BeanUtils.copyProperties(outerchainMapper, s);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return outerchainMapper;

                    })
                    .collect(Collectors.toList()));

        }
        model.addAttribute("course", course);
        model.addAttribute("categories", YiqiJTConfCourseMapper.Price.values());
        model.addAttribute("labels", TeachingResource.Label.values());
        return "opmanager/17jt/course_info";
    }

    @RequestMapping(value = "saveCourse.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResource(@RequestBody YiqiJTConfCourseMapper courseMapper) {
        if (courseMapper == null) {
            return MapMessage.errorMessage("参数为空!");
        }
        MapMessage result;
        try {
            courseMapper.setSource(TeachingResource.SOURCE_PLATFORM);
            courseMapper.setCategory(YiqiJTCourse.Category.YIQI_JIANGTANG.name());

            YiqiJTCourse course = null;
            if (courseMapper.getId() != null) {
                course = yiqiJTServiceClient.loadCourseById(courseMapper.getId());
            }
            if (course == null) {
                course = new YiqiJTCourse();
                course.setOpenTime(new Date());
                course.setActiveTime(2592000);
                //新添加的课程默认不上线
                course.setStatus(2);
            }
            BeanUtils.copyProperties(course, courseMapper);
            List<Integer> gradeList = Arrays.asList(courseMapper.getGrade().split(","))
                    .stream()
                    .map(s -> NumberUtils.toInt(s))
                    .collect(Collectors.toList());

            List<Integer> subjectList = Stream.of(courseMapper.getSubject().split(","))
                    .map(s -> NumberUtils.toInt(s))
                    .collect(Collectors.toList());
            course = yiqiJTServiceClient.upsertCourse(course);

            yiqiJTServiceClient.upsertCourseSubject(course.getId(), subjectList);
            yiqiJTServiceClient.upsertCourseGeade(course.getId(), gradeList);
            result = MapMessage.successMessage();
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "addCatalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCatalog(@RequestBody YiqiJTConfCourseCatalogMapper catalogMapper) {
        if (catalogMapper == null) {
            return MapMessage.errorMessage("参数为空!");
        }
        if (catalogMapper.getCourseId() == null) {
            return MapMessage.errorMessage("课程id为空，请先保存课程再添加!");
        }
        MapMessage result = MapMessage.successMessage();
        try {
            YiqiJTCourseCatalog course = new YiqiJTCourseCatalog();
            BeanUtils.copyProperties(course, catalogMapper);

            yiqiJTServiceClient.addCourseCatalog(course);
            List<YiqiJTCourseCatalog> catalogList = yiqiJTServiceClient.getCourseCatalogsByCourseId(course.getCourseId());
            List<YiqiJTConfCourseCatalogMapper> mappers = cvCatalog2Mapper(catalogList);
            result.add("catalogList", mappers);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    private  List<YiqiJTConfCourseCatalogMapper> cvCatalog2Mapper(List<YiqiJTCourseCatalog> catalogList) {
        if (catalogList == null && catalogList.isEmpty()) {
            return null;
        }
        return catalogList
                .stream()
                .map(s -> {
                    YiqiJTConfCourseCatalogMapper mapper = new YiqiJTConfCourseCatalogMapper();
                    try {
                        BeanUtils.copyProperties(mapper, s);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return mapper;
                })
                .sorted(Comparator.comparing(YiqiJTConfCourseCatalogMapper::getTimeNodeSec))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "delCatalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delCatalog(@RequestBody long id) {
        if (id == 0) {
            return MapMessage.errorMessage("参数为空!");
        }
        MapMessage result = MapMessage.successMessage();
        try {
            YiqiJTCourseCatalog catalog = yiqiJTServiceClient.getCourseCatalogById(id);
            if (catalog == null) {
                return MapMessage.errorMessage("课程目录已删除!");
            }
            yiqiJTServiceClient.delCourseCatalog(id);
            List<YiqiJTCourseCatalog> catalogList = yiqiJTServiceClient.getCourseCatalogsByCourseId(catalog.getCourseId());
            List<YiqiJTConfCourseCatalogMapper> mappers = cvCatalog2Mapper(catalogList);
            result.add("catalogList", mappers);
        } catch (Exception e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "upsertChoiceNote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addChoiceNote(@RequestBody YiqiJTConfChoiceNoteMapper choiceNoteMapper) {
        if (choiceNoteMapper == null) {
            return MapMessage.errorMessage("参数为空!");
        }
        if (choiceNoteMapper.getCourseId() == null) {
            return MapMessage.errorMessage("课程id为空，请先保存课程再添加!");
        }
        MapMessage result = MapMessage.successMessage();
        try {
            YiqiJTChoiceNote course = new YiqiJTChoiceNote();
            BeanUtils.copyProperties(course, choiceNoteMapper);

            yiqiJTServiceClient.upsertCourseChoiceNote(course);
            List<YiqiJTChoiceNote> choiceNoteList = yiqiJTServiceClient.getCourseNotesByCourseId(course.getCourseId());
            result.add("choiceNoteList", choiceNoteList);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "delChoiceNote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delChoiceNote(@RequestBody long id) {
        if (id == 0) {
            return MapMessage.errorMessage("参数为空!");
        }
        MapMessage result = MapMessage.successMessage();
        try {
            YiqiJTChoiceNote noteById = yiqiJTServiceClient.getCourseNoteById(id);
            if (noteById == null) {
                return MapMessage.errorMessage("该精选笔记已删除!");
            }
            yiqiJTServiceClient.delCourseChoiceNote(id);
            List<YiqiJTChoiceNote> choiceNoteList = yiqiJTServiceClient.getCourseNotesByCourseId(noteById.getCourseId());
            result.add("choiceNoteList", choiceNoteList);
        } catch (Exception e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "addOuterchain.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addOuterchain(@RequestBody YiqiJTConfCourseOuterchainMapper outerchainMapper) {
        if (outerchainMapper == null) {
            return MapMessage.errorMessage("参数为空!");
        }
        if (outerchainMapper.getCourseId() == null) {
            return MapMessage.errorMessage("课程id为空，请先保存课程再添加!");
        }
        MapMessage  result = MapMessage.successMessage();
        try {
            YiqiJTCourseOuterchain course = new YiqiJTCourseOuterchain();
            BeanUtils.copyProperties(course, outerchainMapper);

            yiqiJTServiceClient.addCourseOuterchain(course);
            List<YiqiJTCourseOuterchain> outerchainList = yiqiJTServiceClient.getCourseOuterchainsByCourseId(course.getCourseId());
            List<YiqiJTConfCourseOuterchainMapper> mappers = cvOuterchain2Mapper(outerchainList);
            result.add("outerchainList", mappers);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    private List<YiqiJTConfCourseOuterchainMapper> cvOuterchain2Mapper(List<YiqiJTCourseOuterchain> outerchainList) {
        if (outerchainList ==null || outerchainList.isEmpty()) {
            return null;
        }
        List<YiqiJTConfCourseOuterchainMapper> mappers = outerchainList
                .stream()
                .map(s -> {
                    YiqiJTConfCourseOuterchainMapper mapper = new YiqiJTConfCourseOuterchainMapper();
                    try {
                        BeanUtils.copyProperties(mapper, s);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return mapper;
                })
                .collect(Collectors.toList());
        return mappers;
    }

    @RequestMapping(value = "delOuterchain.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delOuterchain(@RequestBody long id) {
        if (id == 0) {
            return MapMessage.errorMessage("参数为空!");
        }

        MapMessage  result = MapMessage.successMessage();
        try {
            YiqiJTCourseOuterchain outerchain = yiqiJTServiceClient.getCourseOuterchainById(id);
            if (outerchain == null) {
                return MapMessage.errorMessage("外链已删除!");
            }
            yiqiJTServiceClient.delCourseOuterchain(id);
            List<YiqiJTCourseOuterchain> outerchainList = yiqiJTServiceClient.getCourseOuterchainsByCourseId(outerchain.getCourseId());
            List<YiqiJTConfCourseOuterchainMapper> mappers = cvOuterchain2Mapper(outerchainList);
            result.add("outerchainList", mappers);
        } catch (Exception e) {
            e.printStackTrace();
            result = MapMessage.errorMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "offline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offline() {
        MapMessage msg = null;
        long coueseId = getRequestLong("courseId");
        try {
            yiqiJTServiceClient.updateCourseStatus(coueseId, 2);
            msg = MapMessage.successMessage();
        } catch (Exception e) {
            msg = MapMessage.errorMessage(e.getMessage());
        }
        return msg;
    }

    @RequestMapping(value = "online.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage online() {
        MapMessage msg = null;
        long coueseId = getRequestLong("courseId");
        List<YiqiJTCourseCatalog> catalogList = yiqiJTServiceClient.getCourseCatalogsByCourseId(coueseId);
        if (catalogList == null || catalogList.isEmpty()) {
            return MapMessage.errorMessage("上线失败！必须设置课程目录才可上线！");
        }
        try {
            yiqiJTServiceClient.updateCourseStatus(coueseId, 1);
            msg = MapMessage.successMessage();
        } catch (Exception e) {
            msg = MapMessage.errorMessage(e.getMessage());
        }
        return msg;
    }
}
