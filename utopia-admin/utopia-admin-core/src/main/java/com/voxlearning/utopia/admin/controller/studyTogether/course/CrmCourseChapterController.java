package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseChapterLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseChapterService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseChapter;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>章节
 * @author xuerui.zhang
 * @since 2018/9/7 下午3:38
 */
@Slf4j
@Controller
@RequestMapping(value = "opmanager/studytogether/chapter/")
public class CrmCourseChapterController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseChapterService.class)
    private CrmCourseChapterService courseChapterService;

    @ImportService(interfaceClass = CrmCourseChapterLoader.class)
    private CrmCourseChapterLoader courseChapterLoader;

    /**
     * 章节列表
     */
    @RequestMapping(value = "chindex.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Long chapterId = getRequestLong("chapterId");
        Long skuId = getRequestLong("skuId");
        String chapterName = getRequestString("chapterName");
        Integer envLevel = getRequestInt("envLevel");
        String createUser = getRequestString("createUser");

        List<CourseChapter> cchList = courseChapterLoader.loadAllCourseChapter();
        if (CollectionUtils.isNotEmpty(cchList)) {
            if (0L != chapterId) {
                cchList = cchList.stream().filter(e -> e.getId().equals(chapterId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(chapterName)) {
                cchList = cchList.stream().filter(e -> (StringUtils.isNotBlank(e.getChapterName()) && e.getChapterName()
                        .contains(chapterName.trim()))).collect(Collectors.toList());
            }
            if (0L != skuId) {
                cchList = cchList.stream().filter(e -> e.getSkuId().equals(skuId)).collect(Collectors.toList());
            }
            if (0 != envLevel && -1 != envLevel) {
                cchList = cchList.stream().filter(e -> e.getEnvLevel().equals(envLevel)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                cchList = cchList.stream()
                        .filter(e -> StringUtils.isNotBlank(e.getCreateUser()) && e.getCreateUser().contains(createUser.trim()))
                        .collect(Collectors.toList());
            }
        }

        Page<CourseChapter> resultList;
        if (CollectionUtils.isEmpty(cchList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            cchList = cchList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(cchList, pageRequest);
        }

        if(0L != chapterId) model.addAttribute("chapterId", chapterId);
        if(0L != skuId) model.addAttribute("skuId", skuId);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("chapterName", chapterName);
        model.addAttribute("envLevel", envLevel);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/chapter/chindex";
    }

    /**
     * 章节修改/添加页面
     */
    @RequestMapping(value = "chdetails.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long chapterId = getRequestLong("chapterId", 0L);
        String username = null;
        if (0L != chapterId) {
            CourseChapter courseChapter = courseChapterLoader.loadCourseChapter(chapterId);
            if (courseChapter != null) {
                username = courseChapter.getCreateUser();
                model.addAttribute("content", courseChapter);
            }
        } else {
            model.addAttribute("content", new CourseChapter());
        }
        model.addAttribute("chapterId", chapterId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/chapter/chdetails";
    }

    /**
     * 添加或修改章节
     */
    @ResponseBody
    @RequestMapping(value = "chsave.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long chapterId = getRequestLong("chapterId");
        Long skuId = getRequestLong("skuId");
        String chapterName = getRequestString("chapterName");
        String chapterDesc = getRequestString("chapterDesc");
        Integer seq = getRequestInt("seq");
        String openDate = getRequestString("openDate");
        String singleRewardIds = getRequestString("sids");
        String doubleRewardIds = getRequestString("dids");
        Integer envLevel = getRequestInt("envLevel");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");

        List<Integer> sidLists = JsonUtils.fromJsonToList(singleRewardIds, Integer.class);
        List<Integer> didLists = JsonUtils.fromJsonToList(doubleRewardIds, Integer.class);
        if (sidLists == null){
            sidLists = new ArrayList<>();
        }
        if (didLists == null){
            didLists = new ArrayList<>();
        }

        try {
            CourseChapter bean;
            CourseChapter oldObj = new CourseChapter();

            if (chapterId <= 0L) {
                bean = new CourseChapter();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseChapterLoader)
                            .keyPrefix("CHAPTER_INCR_ID").keys(skuId, seq).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseChapterLoader.loadCourseChapter(chapterId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                bean.setId(chapterId);
                BeanUtils.copyProperties(oldObj, bean);
            }
            bean.setSkuId(skuId);
            bean.setChapterName(chapterName);
            bean.setChapterDesc(chapterDesc);
            bean.setSeq(seq);
            if (StringUtils.isNotBlank(openDate)) {
                bean.setOpenDate(CrmCourseCommonController.safeConvertDate(openDate));
            } else {
                bean.setOpenDate(null);
            }
            bean.setEnvLevel(envLevel);
            bean.setRemark(remark);
            bean.setSingleRewardIds(sidLists);
            bean.setDoubleRewardIds(didLists);
            String userName = getCurrentAdminUser().getAdminUserName();
            if (0L == chapterId) {
                bean.setCreateUser(createUser);
                courseChapterService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("章节", userName,
                        ChangeLogType.CourseChapter, bean.getId().toString(), "新增章节信息");
            } else {
                CourseChapter newObj = courseChapterService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseChapter, bean.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "chinfo.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long chapterId = getRequestLong("chapterId", 0L);
        if (0L == chapterId) {
            return CourseConstMapper.CHAPTER_REDIRECT;
        }
        CourseChapter courseChapter = courseChapterLoader.loadCourseChapter(chapterId);
        if (courseChapter == null) {
            return CourseConstMapper.CHAPTER_REDIRECT;
        }
        model.addAttribute("content", courseChapter);
        return "opmanager/studyTogether/chapter/chinfo";
    }

    @RequestMapping(value = "chlogs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long chapterId = getRequestLong("chapterId", 0L);
        if (0L == chapterId)  {
            return CourseConstMapper.CHAPTER_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(chapterId.toString(), ChangeLogType.CourseChapter, pageRequest);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("chapterId", chapterId);
        return "opmanager/studyTogether/chapter/chlogs";
    }

    @ResponseBody
    @RequestMapping(value = "check_seq.vpage", method = RequestMethod.POST)
    public MapMessage checkSeq() {
        Long skuId = getRequestLong("skuId");
        Integer seq = getRequestInt("seq");
        List<CourseChapter> courseSubjects = courseChapterLoader.loadAllCourseChapter();
        List<Integer> seqs = courseSubjects
                .stream().filter(e -> e.getSkuId().equals(skuId)).map(CourseChapter::getSeq).collect(Collectors.toList());
        if (!seqs.contains(seq)) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

}