package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.coin.api.CoinTypeBufferLoaderClient;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseCoinRewardLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseCoinRewardService;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSpuLoader;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseCoinReward;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseCoinRewardMapper;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>连续学习金币奖励
 * @author xuerui.zhang
 * @since 2018/9/19 下午2:28
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/coinreward/")
public class CrmCourseCoinRewardController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseCoinRewardLoader.class)
    private CrmCourseCoinRewardLoader courseCoinRewardLoader;

    @ImportService(interfaceClass = CrmCourseCoinRewardService.class)
    private CrmCourseCoinRewardService courseCoinRewardService;

    @ImportService(interfaceClass = CrmCourseStructSpuLoader.class)
    private CrmCourseStructSpuLoader courseStructSpuLoader;

    @Inject
    private CoinTypeBufferLoaderClient coinTypeBufferLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;

    /**
     * 连续学习金币奖励列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        String cscrId = getRequestString("cscrId");
        Integer coinTypeId = getRequestInt("coinTypeId");
        String createUser = getRequestString("createUser");
        Integer weekCount = getRequestInt("weekCount");

        List<CourseCoinReward> rewardList = courseCoinRewardLoader.loadAllCourseCoinReward();
        if (CollectionUtils.isNotEmpty(rewardList)) {
            if (StringUtils.isNotBlank(cscrId)) {
                rewardList = rewardList.stream().filter(e -> e.getId().equals(cscrId.trim())).collect(Collectors.toList());
            }
            if (coinTypeId > 0) {
                rewardList = rewardList.stream().filter(e -> e.getCoinTypeId().equals(coinTypeId)).collect(Collectors.toList());
            }
            if (weekCount > 0) {
                rewardList = rewardList.stream().filter(e -> e.getWeekCount().equals(weekCount)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                rewardList = rewardList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }
        if (CollectionUtils.isNotEmpty(rewardList)) {
            rewardList = rewardList.stream().sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).collect(Collectors.toList());
        }
        Page<CourseCoinRewardMapper> resultList = getPage(rewardList, pageRequest);

        if(coinTypeId > 0) {
            model.addAttribute("coinTypeId", coinTypeId);
        }
        if(weekCount > 0) {
            model.addAttribute("weekCount", weekCount);
        }
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("cscrId", cscrId);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/coinreward/index";
    }

    /**
     * 学习币奖励-修改/添加页面
     */
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String cscrId = getRequestString("cscrId");
        String username = null;
        if (StringUtils.isNotBlank(cscrId)) {
            CourseCoinReward courseCoinReward = courseCoinRewardLoader.loadCourseCoinRewardById(cscrId);
            if (courseCoinReward != null) {
                username = courseCoinReward.getCreateUser();
                model.addAttribute("content", courseCoinReward);
            }
        } else {
            model.addAttribute("content", new CourseCoinReward());
        }
        model.addAttribute("cscrId", cscrId);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/coinreward/details";
    }

    /**
     * 添加或修改章节
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        String cscrId = getRequestString("cscrId");
        Integer weekCount = getRequestInt("weekCount");
        Integer dayCount = getRequestInt("dayCount");
        String desc = getRequestString("desc");
        Integer coinTypeId = getRequestInt("coinTypeId");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        String rewardType = getRequestString("rewardType");

        try {
            CourseCoinReward bean;
            CourseCoinReward newObj;
            CourseCoinReward oldObj = new CourseCoinReward();

            if (StringUtils.isBlank(cscrId)) {
                bean = new CourseCoinReward();
                bean.setId(CourseCoinReward.generatingId(weekCount, dayCount));
            } else {
                bean = courseCoinRewardLoader.loadCourseCoinRewardById(cscrId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                BeanUtils.copyProperties(oldObj, bean);
            }

            bean.setCoinTypeId(coinTypeId);
            bean.setWeekCount(weekCount);
            bean.setDayCount(dayCount);
            if (StringUtils.isNotBlank(desc)) bean.setDesc(desc);
            bean.setRemark(remark);
            bean.setRewardType(rewardType);
            String userName = getCurrentAdminUser().getAdminUserName();

            if (StringUtils.isBlank(cscrId)) {
                bean.setCreateUser(createUser);
                CourseCoinReward save = courseCoinRewardService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("学习奖励", userName,
                        ChangeLogType.CourseCoinReward, save.getId(), "新增学习奖励信息");
            } else {
                bean.setId(cscrId);
                newObj = courseCoinRewardService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseCoinReward, cscrId);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        String cscrId = getRequestString("cscrId");
        if (StringUtils.isBlank(cscrId))
            return CourseConstMapper.CSCR_REDIRECT;

        CourseCoinReward courseCoinReward = courseCoinRewardLoader.loadCourseCoinRewardById(cscrId);
        if (courseCoinReward == null)
            return CourseConstMapper.CSCR_REDIRECT;

        model.addAttribute("cscrId", cscrId);
        model.addAttribute("content", courseCoinReward);
        return "opmanager/studyTogether/coinreward/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        String cscrId = getRequestString("cscrId");
        if (StringUtils.isBlank(cscrId))  {
            return CourseConstMapper.CSCR_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(cscrId, ChangeLogType.CourseCoinReward, pageRequest);

//        Page<ContentChangeLog> resultList;
//        if (CollectionUtils.isEmpty(changeLogList)) {
//            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
//        } else {
//            resultList = PageableUtils.listToPage(changeLogList, pageRequest);
//        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("cscrId", cscrId);
        return "opmanager/studyTogether/coinreward/logs";
    }

    @ResponseBody
    @RequestMapping(value = "check_id.vpage", method = RequestMethod.GET)
    public MapMessage checkId() {
        Integer weekCount = getRequestInt("weekCount");
        Integer dayCount = getRequestInt("dayCount");
        if (weekCount <= 0 && dayCount <= 0) {
            return MapMessage.errorMessage();
        }
        String id = CourseCoinReward.generatingId(weekCount, dayCount);
        CourseCoinReward bean = courseCoinRewardLoader.loadCourseCoinRewardById(id);
        return bean == null ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    /**
     * 获取sku名称
     */
    @ResponseBody
    @RequestMapping(value = "spuname.vpage", method = RequestMethod.GET)
    public MapMessage getSkuName() {
        Long spuId = getRequestLong("spuId");
        if (0L == spuId) return MapMessage.errorMessage();
        CourseStructSpu courseStructSpu = courseStructSpuLoader.loadCourseStructSpuById(spuId);
        return courseStructSpu == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("spuName", courseStructSpu.getName());
    }

    private Page<CourseCoinRewardMapper> getPage(List<CourseCoinReward> list, PageRequest pageRequest) {
        if (CollectionUtils.isEmpty(list)) {
            return PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        }
        List<CourseCoinRewardMapper> mapperList = new ArrayList<>();
        Page<CourseCoinReward> pageList = PageableUtils.listToPage(list, pageRequest);
        pageList.getContent().forEach(e -> mapperList.add(wrapperBean(e)));
        return new PageImpl<>(mapperList, pageRequest, list.size());
    }

    private CourseCoinRewardMapper wrapperBean(CourseCoinReward coinReward) {
        CourseCoinRewardMapper bean = new CourseCoinRewardMapper();
        bean.setId(coinReward.getId());
        bean.setWeekCount(coinReward.getWeekCount());
        bean.setDayCount(coinReward.getDayCount());
        bean.setCreateUser(coinReward.getCreateUser());
        bean.setCoinTypeId(coinReward.getCoinTypeId());
        CoinType coinType = coinTypeBufferLoaderClient.getCoinType(coinReward.getCoinTypeId());
        bean.setCoinCount(coinType.getCount());
        String rewardType = coinReward.getRewardType();
        if (StringUtils.isNotBlank(rewardType)) {
            bean.setRewardType(rewardType);
            ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(rewardType);
            bean.setRewardCount(null == item ? 0 : item.getCount());
        } else {
            bean.setRewardType("");
        }
        return bean;
    }
}
