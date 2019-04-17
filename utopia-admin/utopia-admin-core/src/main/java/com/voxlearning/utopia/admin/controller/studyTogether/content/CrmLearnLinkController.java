package com.voxlearning.utopia.admin.controller.studyTogether.content;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmLearnLinkLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmLearnLinkService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.component.content.*;
import com.voxlearning.galaxy.service.studycourse.api.enums.LinkEnum;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.prometheus.service.data.api.client.IntelDiagnosisCourseServiceClient;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author xuerui.zhang
 * @since 2019/3/5 下午5:08
 */
@Slf4j
@Controller
@RequestMapping(value = "opmanager/studyTogether/link/")
public class CrmLearnLinkController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmLearnLinkLoader.class)
    private CrmLearnLinkLoader learnLinkLoader;

    @ImportService(interfaceClass = CrmLearnLinkService.class)
    private CrmLearnLinkService learnLinkService;

    @Inject
    private IntelDiagnosisClient intelDiagnosisCourseServiceClient;

    /**
     * 学习环节列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String loadAllLearnLin(Model model) {
        return "/opmanager/studyTogether/temlate/component/learnlinklist";
    }


    @RequestMapping(value = "index_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadLearnLinkData() {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Long linkId = getRequestLong("linkId", 0L);
        String name = getRequestString("likeName");
        String type = getRequestString("type");
        String selectType = getRequestString("selectType");
        String createUser = getRequestString("createUser");

        List<LearnLink> learnLinks = learnLinkLoader.loadAllLearnLink();
        if (CollectionUtils.isEmpty(learnLinks)) {
            learnLinks = new ArrayList<>();
        }
        if (CollectionUtils.isNotEmpty(learnLinks)) {
            if (0L != linkId) {
                learnLinks = learnLinks.stream().filter(e -> e.getId().equals(linkId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(name)) {
                learnLinks = learnLinks.stream().filter(e -> e.getName().contains(name.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                learnLinks = learnLinks.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(type)) {
                learnLinks = learnLinks.stream()
                        .filter(p -> p.getType() != null)
                        .filter(p -> p.getType() == LinkEnum.valueOf(type))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(selectType)) {
                int innerType = SafeConverter.toInt(selectType, -1);
                if (innerType != -1) {
                    List<LinkEnum> linkEnums = Stream.of(LinkEnum.values()).filter(e -> e.getSelectId() == innerType).collect(Collectors.toList());
                    learnLinks = learnLinks.stream()
                            .filter(p -> p.getType() != null)
                            .filter(p -> linkEnums.contains(p.getType()))
                            .collect(Collectors.toList());
                }
            }
        }

        Page<LearnLink> resultList;
        if (CollectionUtils.isEmpty(learnLinks)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            learnLinks = learnLinks.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(learnLinks, pageRequest);
        }


        return MapMessage.successMessage()
                .add("currentPage", pageNum)
                .add("content", resultList.getContent())
                .add("totalPage", resultList.getTotalPages())
                .add("totalCount", resultList.getTotalElements())
                .add("hasPrev", resultList.hasPrevious())
                .add("hasNext", resultList.hasNext())
                .add("linkId", linkId)
                .add("name", name)
                .add("createUser", createUser);
    }

//    /**
//     * 修改/添加环节
//     */
//    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
//    public String linkDetails(Model model) {
//        Long linkId = getRequestLong("linkId", 0L);
//        if (linkId > 0L) {
//            LearnLink learnLink = learnLinkLoader.loadLearnLinkById(linkId);
//            if (learnLink != null) {
//                model.addAttribute("content", learnLink);
//            } else {
//                model.addAttribute("content", new LearnLink());
//            }
//        } else {
//            model.addAttribute("content", new LearnLink());
//        }
//        model.addAttribute("linkId", linkId);
//        model.addAttribute("createUser", getCurrentAdminUser().getAdminUserName());
//        return "/opmanager/studyTogether/temlate/component/learnlinkinfo";
//    }

    /**
     * 环节详情
     */
    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        return "/opmanager/studyTogether/temlate/component/learnlinkinfo";
    }

    /**
     * 环节详情
     */
    @RequestMapping(value = "info_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage courseInfoData() {
        Long linkId = getRequestLong("linkId", 0L);
        MapMessage mapMessage = new MapMessage();
        mapMessage.add("linkId", linkId);
        if (linkId > 0L) {
            LearnLink learnLink = learnLinkLoader.loadLearnLinkById(linkId);
            if (learnLink != null) {
                mapMessage.add("content", learnLink);
            } else {
                learnLink = new LearnLink();
                learnLink.setCreateUser(getCurrentAdminUser().getAdminUserName());
                mapMessage.add("content", learnLink);
            }
        } else {
            LearnLink learnLink = new LearnLink();
            learnLink.setCreateUser(getCurrentAdminUser().getAdminUserName());
            mapMessage.add("content", learnLink);
        }
        mapMessage.add("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));
        return mapMessage;
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long linkId = getRequestLong("linkId", 0L);
        if (0L == linkId) {
            return null;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(linkId.toString(), ChangeLogType.LEARN_LINK, pageRequest);

//        Page<ContentChangeLog> resultList;
//        if (CollectionUtils.isEmpty(changeLogList.getContent())) {
//            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
//        } else {
//            resultList = PageableUtils.listToPage(changeLogList.getContent(), pageRequest);
//        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("linkId", linkId);
        return null;
    }

    /**
     * 保存环节信息
     */
    @ResponseBody
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage saveLearnLink() {
        Long linkId = getRequestLong("linkId", 0L);
        String name = getRequestString("name");
        String type = getRequestString("type");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        //优等生公用字段
        String title = getRequestString("title");
        String pic = getRequestString("img");
        //音频视频公用字段
        Integer duration = getRequestInt("duration");
        //直接上传视频
        String videoUrl = getRequestString("videoUrl");
        //视频轻交互
        String videoId = getRequestString("videoId");
        //直接上传音频
        String subTitle = getRequestString("subTitle");
        String author = getRequestString("author");
        String image = getRequestString("background");
        String audioUrl = getRequestString("audioUrl");
        String content = getRequestString("content");
        String lrcContent = getRequestString("lrcContent");
        //选择题
        String questionId = getRequestString("questionId");
        boolean needScore = getRequestInt("needScore") == 1;
        boolean needResult = getRequestInt("needResult") == 1;

        try {
            LearnLink bean;
            LearnLink newObj;
            LearnLink oldObj = new LearnLink();
            if (linkId <= 0L) {
                bean = new LearnLink();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(learnLinkLoader).keyPrefix("LINK_INCR_ID").keys(linkId).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = learnLinkLoader.loadLearnLinkById(linkId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                BeanUtils.copyProperties(oldObj, bean);
                bean.setId(linkId);
            }
            bean.setName(name);
            bean.setRemark(remark);
            if (0L == linkId) {
                bean.setCreateUser(createUser);
            }

            LinkEnum linkEnum = LinkEnum.valueOf(type);
            if (linkEnum == LinkEnum.UNKNOWN) {
                return MapMessage.errorMessage("类型错误");
            }
            bean.setType(linkEnum);
            ComponentContent componentContent = new ComponentContent();
            switch (linkEnum) {
                case DIRECT_VIDEO:
                    DirectVideoContent directVideoContent = new DirectVideoContent();
                    directVideoContent.setPic(pic);
                    directVideoContent.setTitle(title);
                    directVideoContent.setVideoUrl(videoUrl);
                    directVideoContent.setDuration(duration);
                    componentContent.setDirectVideoContent(directVideoContent);
                    bean.setContent(componentContent);
                    break;

                case INDIRECT_VIDEO:
                    IndirectVideoContent indirectVideoContent = new IndirectVideoContent();
                    indirectVideoContent.setPic(pic);
                    indirectVideoContent.setTitle(title);
                    indirectVideoContent.setVideoId(videoId);
                    List<IntelDiagnosisCourse> diagnosisCourses = intelDiagnosisCourseServiceClient.loadDiagnosisCoursesByIds(Collections.singleton(videoId));
                    if (CollectionUtils.isEmpty(diagnosisCourses)) {
                        return MapMessage.errorMessage("轻交互视频ID错误");
                    }
                    componentContent.setIndirectVideoContent(indirectVideoContent);
                    bean.setContent(componentContent);
                    break;

                case DIRECT_AUDIO:
                    DirectAudioContent audioContent = new DirectAudioContent();
                    audioContent.setPic(pic);
                    audioContent.setTitle(title);
                    audioContent.setSubTitle(subTitle);
                    audioContent.setAuthor(author);
                    audioContent.setImage(image);
                    audioContent.setAudioUrl(audioUrl);
                    audioContent.setDuration(duration);
                    audioContent.setContent(content);
                    audioContent.setLrcContent(lrcContent);
                    componentContent.setDirectAudioContent(audioContent);
                    bean.setContent(componentContent);
                    break;
                case ONE_QUESTION_MORE_ASK:
                case SINGLE_WITHOUT_RESOLUTION:
                    ChooseContent chooseContent = new ChooseContent();
                    if (StringUtils.isBlank(questionId)) {
                        return MapMessage.errorMessage("选择类型题目填写错误");
                    }

                    List<String> questionIds = Arrays.asList(StringUtils.split(questionId, "#"));
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(questionIds);
                    Set<String> funMissedQuestionIds = questionIds.stream().filter(p -> questionMap.get(p) == null)
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
                        return MapMessage.errorMessage("题目ID错误: " + JsonUtils.toJson(funMissedQuestionIds));
                    }
                    chooseContent.setQuestionId(questionIds);
                    chooseContent.setNeedScore(needScore);
                    chooseContent.setNeedResult(needResult);
                    chooseContent.setPic(pic);
                    chooseContent.setTitle(title);
                    componentContent.setChooseContent(chooseContent);
                    bean.setContent(componentContent);
                    break;
                case SINGLE_HAS_RESOLUTION:
                    ChooseContent chooseContent1 = new ChooseContent();
                    if (StringUtils.isBlank(questionId)) {
                        return MapMessage.errorMessage("选择类型题目填写错误");
                    }
                    List<String> questionIdList = Arrays.asList(StringUtils.split(questionId, "#"));
                    Map<String, NewQuestion> questionContentMap = questionLoaderClient.loadLatestQuestionByDocIds(questionIdList);
                    Set<String> missedQuestionIds = questionIdList.stream().filter(p -> questionContentMap.get(p) == null)
                            .collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(missedQuestionIds)) {
                        return MapMessage.errorMessage("题目ID错误: " + JsonUtils.toJson(missedQuestionIds));
                    }
                    List<NewQuestion> hasQuestionAnalysis = questionContentMap.values().stream().filter(e -> e.getContent().getSubContents().stream().anyMatch(p -> StringUtils.isBlank(p.getAnalysis()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(hasQuestionAnalysis)) {
                        return MapMessage.errorMessage("题目ID错误: " + JsonUtils.toJson(hasQuestionAnalysis.stream().map(NewQuestion::getId).collect(Collectors.toList())) + "没有解析");
                    }
                    chooseContent1.setQuestionId(questionIdList);
                    chooseContent1.setNeedScore(needScore);
                    chooseContent1.setNeedResult(needResult);
                    chooseContent1.setPic(pic);
                    chooseContent1.setTitle(title);
                    componentContent.setChooseContent(chooseContent1);
                    bean.setContent(componentContent);
                    break;
            }
            String userName = getCurrentAdminUser().getAdminUserName();
            newObj = learnLinkService.save(bean);
            if (0L == linkId) {
                studyCourseBlackWidowServiceClient.justAddChangeLog("环节", userName,
                        ChangeLogType.LEARN_LINK, newObj.getId().toString(), "新增学习环节");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.LEARN_LINK, newObj.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "get_link_id_by_name.vpage", method = RequestMethod.GET)
    public MapMessage getLinkIdByName() {
        String name = getRequestString("name");
        List<LearnLink> linkList = learnLinkLoader.loadLearnLinkByName(name);
        List<Map<String, Object>> returnList = new ArrayList<>();
        for (LearnLink learnLink : linkList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", learnLink.getId());
            map.put("name", learnLink.getName());
            returnList.add(map);
        }
        return MapMessage.successMessage().add("return_list", returnList);
    }

    @ResponseBody
    @RequestMapping(value = "get_link_by_id.vpage", method = RequestMethod.GET)
    public MapMessage checkLinkById() {
        long id = getRequestLong("id");
        LearnLink learnLink = learnLinkLoader.loadLearnLinkById(id);
        if (learnLink != null) {
            return MapMessage.successMessage().add("linkInfo", learnLink);
        }
        return MapMessage.errorMessage();
    }

}
