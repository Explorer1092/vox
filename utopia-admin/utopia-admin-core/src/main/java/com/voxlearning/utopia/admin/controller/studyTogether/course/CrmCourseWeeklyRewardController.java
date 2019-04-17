package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseWeeklyRewardLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseWeeklyRewardService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseWeeklyReward;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * <P>周奖励
 *
 * @author xuerui.zhang
 * @since 2018/9/10 上午11:01
 */
@Slf4j
@Controller
@RequestMapping(value = "opmanager/studytogether/weeklyreward/")
public class CrmCourseWeeklyRewardController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseWeeklyRewardLoader.class)
    private CrmCourseWeeklyRewardLoader courseWeeklyRewardLoader;

    @ImportService(interfaceClass = CrmCourseWeeklyRewardService.class)
    private CrmCourseWeeklyRewardService courseWeeklyRewardService;

    private final static List<Integer> TYPE = Arrays.asList(2, 3, 4, 5, 6);

    /**
     * 奖励列表
     */
    @RequestMapping(value = "wrindex.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Long rewardId = getRequestLong("rewardId");
        String rewardName = getRequestString("rewardName");
        Integer rewardType = getRequestInt("rewardType");
        String createUser = getRequestString("createUser");

        List<CourseWeeklyReward> rewardList = courseWeeklyRewardLoader.loadAllCourseWeeklyReward()
                .stream().filter(e -> e.getType() != 1).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(rewardList)) {
            if (0L != rewardId) {
                rewardList = rewardList.stream().filter(e -> e.getId().equals(rewardId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(rewardName)) {
                rewardList = rewardList.stream().filter(e -> e.getName().contains(rewardName.trim())).collect(Collectors.toList());
            }
            if (0 != rewardType && -1 != rewardType) {
                rewardList = rewardList.stream().filter(e -> e.getType().equals(rewardType)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                rewardList = rewardList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }

        Page<CourseWeeklyReward> resultList;
        if (CollectionUtils.isEmpty(rewardList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            rewardList = rewardList.stream()
                    .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                    .collect(Collectors.toList());
            resultList = PageableUtils.listToPage(rewardList, pageRequest);
        }

        if (0L != rewardId) model.addAttribute("rewardId", rewardId);
        if (0 != rewardType) model.addAttribute("rewardType", rewardType);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("rewardName", rewardName);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/weeklyreward/wrindex";
    }

    /**
     * 进入奖励修改/添加页面
     */
    @RequestMapping(value = "wrdetails.vpage", method = RequestMethod.GET)
    public String rewardDetail(Model model) {
        Long rewardId = getRequestLong("rewardId");
        String username = null;
        if (0L != rewardId) {
            CourseWeeklyReward weeklyReward = courseWeeklyRewardLoader.loadCourseWeeklyReward(rewardId);
            if (weeklyReward != null) {
                model.addAttribute("content", weeklyReward);
                model.addAttribute("obj", weeklyReward.getContent());
                username = weeklyReward.getCreateUser();
            }
        } else {
            model.addAttribute("content", new CourseWeeklyReward());
            model.addAttribute("obj", new Object());
        }
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("types", TYPE);
        model.addAttribute("rewardId", rewardId);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/weeklyreward/wrdetails";
    }

    /**
     * 添加或修改奖励
     */
    @ResponseBody
    @RequestMapping(value = "wrsave.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long rewardId = getRequestLong("rewardId");
        String name = getRequestString("name");
        String iconUrl = getRequestString("iconUrl");
        Integer type = getRequestInt("type");
        Integer coinType = getRequestInt("coinType");
        String videoUrl = getRequestString("videoUrl");
        String ebookId = getRequestString("ebookId");
        String rewardType = getRequestString("rewardType");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        Integer seq = getRequestInt("seq");
        String title = getRequestString("title");
        String desc = getRequestString("desc");

        try {
            CourseWeeklyReward bean;
            CourseWeeklyReward newObj;
            CourseWeeklyReward oldObj = new CourseWeeklyReward();

            if (rewardId <= 0L) {
                bean = new CourseWeeklyReward();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseWeeklyRewardLoader)
                            .keyPrefix("COURSE_WEEKLY_REWARD_INCR_ID").keys(type).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseWeeklyRewardLoader.loadCourseWeeklyReward(rewardId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                bean.setId(rewardId);
                BeanUtils.copyProperties(oldObj, bean);
            }

            bean.setName(name);
            bean.setIconUrl(iconUrl);
            bean.setType(type);
            if (type == 2) {
                CourseWeeklyReward.Video video = new CourseWeeklyReward.Video();
                video.setVideoUrl(videoUrl);
                bean.setContent(video);
            } else if (type == 3) {
                CourseWeeklyReward.Coin coin = new CourseWeeklyReward.Coin();
                coin.setCoinType(coinType);
                bean.setContent(coin);
            } else if (type == 4) {
                CourseWeeklyReward.Ebook ebook = new CourseWeeklyReward.Ebook();
                ebook.setEbookId(ebookId);
                bean.setContent(ebook);
            } else if (type == 5) {
                CourseWeeklyReward.WeeklyReport report = new CourseWeeklyReward.WeeklyReport();
                report.setSeq(seq);
                report.setTitle(title);
                report.setDesc(desc);
                bean.setContent(report);
            } else if (type == 6) {
                CourseWeeklyReward.ParentReward reward = new CourseWeeklyReward.ParentReward();
                reward.setType(rewardType);
                bean.setContent(reward);
            }
            bean.setRemark(remark);
            String userName = getCurrentAdminUser().getAdminUserName();
            if (0L == rewardId) bean.setCreateUser(createUser);

            newObj = courseWeeklyRewardService.save(bean);
            if (0L == rewardId) {
                studyCourseBlackWidowServiceClient.justAddChangeLog("周奖励", userName,
                        ChangeLogType.CourseWeeklyReward, newObj.getId().toString(), "新增奖励信息");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseWeeklyReward, bean.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 奖励Info
     */
    @RequestMapping(value = "wrinfo.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long rewardId = getRequestLong("rewardId", 0L);
        if (0L == rewardId) {
            return CourseConstMapper.WEEKLY_REDIRECT;
        }
        CourseWeeklyReward weeklyReward = courseWeeklyRewardLoader.loadCourseWeeklyReward(rewardId);
        if (weeklyReward == null) {
            return CourseConstMapper.WEEKLY_REDIRECT;
        }
        model.addAttribute("types", TYPE);
        model.addAttribute("content", weeklyReward);
        model.addAttribute("obj", weeklyReward.getContent());
        model.addAttribute("createUser", getCurrentAdminUser().getAdminUserName());
        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST)));
        model.addAttribute("rewardId", rewardId);
        return "opmanager/studyTogether/weeklyreward/wrinfo";
    }

    @RequestMapping(value = "wrlogs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long rewardId = getRequestLong("rewardId", 0L);
        if (0L == rewardId) {
            return CourseConstMapper.WEEKLY_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(rewardId.toString(), ChangeLogType.CourseWeeklyReward, pageRequest);

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
        model.addAttribute("rewardId", rewardId);
        return "opmanager/studyTogether/weeklyreward/wrlogs";
    }

}
