package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.controller.view.*;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.ExamPaperService;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageResult;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPaperEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperOpenOptionParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperProcessStateNotify;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.mockexam.controller.ResourceCode.Operation.PAPER_OPEN;
import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_DATA;
import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_ERROR_DESC;

/**
 * 考卷rest服务
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Slf4j
@Controller
@RequestMapping("mockexam/paper")
public class ExamPaperController extends AbstractAgentController {

    @Inject
    private ExamPaperService examPaperService;

    /**
     * 跳转到查询页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "forquery.vpage")
    public String forQuery(Model model) {
        model.addAttribute("groupId", fetchUserGroupId(getCurrentUserId()));
        appendEnum(model);
        return "/mockexam/paper/list";
    }

    /**
     * 试卷分页查询
     *
     * @param params 参数
     * @return 结果
     */
    @ResponseBody
    @RequestMapping(value = "querypage.vpage", method = RequestMethod.POST)
    public MapMessage queryPage(@RequestBody ExamPaperPageQueryParams params) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(params.getPage() == null ? 1 : params.getPage());
        pageInfo.setSize(params.getSize());

        MapMessage message;
        List<Integer> userRegionCodes = new ArrayList<>();
        // 不是管理员的情况，获取用户所在部门负责的区域
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdmin(getCurrentUser().getRoleList());
        if(!isAdmin){
            Long groupId = fetchUserGroupId(userId);
            userRegionCodes.addAll(baseOrgService.getGroupRegionCodeList(groupId));
            // 用户所在的部门不负责任何区域
            if(CollectionUtils.isEmpty(userRegionCodes)){
                message = MapMessage.successMessage();
                message.set("size", pageInfo.getSize());
                message.set("page", pageInfo.getPage());
                message.set("totalSize", 0);
                message.set(KEY_DATA, new ArrayList<>());
                return message;
            }
        }

        PageResult<ExamPaperDto> result = examPaperService.queryPage(params, pageInfo);
        if (result.isSuccess()) {
            List<ExamPaperListView> views = result.getData().stream()
                    .filter(p -> {
                        if(isAdmin){
                            return true;
                        }
                        BooleanEnum isPublic = BooleanEnum.valueOf(p.getIsPublic());
                        if(isPublic == BooleanEnum.Y){
                            return true;
                        }else {
                            if(judgeRegionOverlap(userRegionCodes, p.getRegionCodes())){
                                return true;
                            }
                            return false;
                        }
                    })
                    .map(ExamPaperListView.Builder::build).collect(Collectors.toList());
            setPaperPreviewUrl(views);
            message = ViewBuilder.fetch(result);
            message.set("size", result.getSize());
            message.set("page", result.getPage());
            message.set("totalSize", result.getTotalSize());
            message.set(KEY_DATA, views);
        } else {
            message = new MapMessage();
            message.setErrorCode(result.getErrorCode());
            message.setInfo(result.getErrorMessage());
            message.set("size", result.getSize());
            message.set("page", result.getPage());
            message.set(KEY_ERROR_DESC, result.getErrorMessage());
        }
        return message;
    }



    /**
     * 改变试卷状态:是否公开
     *
     * @param params 参数
     * @return 结果
     */
    @OperationCode(PAPER_OPEN)
    @ResponseBody
    @RequestMapping(value = "openclose.vpage", method = RequestMethod.POST)
    public MapMessage openOrClose(@RequestBody ExamPaperOpenOptionParams params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getUserName());
        return ViewBuilder.fetch(examPaperService.openOrClose(params));
    }

    /**
     * 通知
     *
     * @param messageStr
     * @return
     */
    @RequestMapping(value = "notify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage notify(@RequestBody String messageStr) {
        ExamPaperProcessStateNotify message = JsonUtils.fromJson(messageStr, ExamPaperProcessStateNotify.class);
        if(message == null){
            throw new BusinessException(ErrorCode.PAPER_NOTIFY_STATE,
                    String.format("流程通知时发生错误，notify = %s", messageStr));
        }
        log.info(String.format("exam_paper_notify|message = %s", message));
        Result<Boolean> result = examPaperService.handleProcessNotify(message);
        return ViewBuilder.fetch(result);
    }

    /**
     * 查询详情
     *
     * @param paperId 试卷id
     * @param subject 学科
     * @return
     */
    @RequestMapping(value = "fordetail.vpage", method = RequestMethod.GET)
    public String forDetail(@RequestParam String paperId,
                            @RequestParam String subject,
                            Model model) {
        ExamPaperQueryParams params = new ExamPaperQueryParams();
        params.setPaperId(paperId);
        params.setSubject(subject);
        PageInfo pageInfo = new PageInfo();
        PageResult<ExamPaperDto> result = examPaperService.queryPage(params, pageInfo);
        if (result.isSuccess() && result.getData().size() > 0) {
            ExamPaperListView view = result.getData().stream()
                    .map(ExamPaperListView.Builder::build)
                    .findFirst().orElse(new ExamPaperListView());
            model.addAttribute("paper", view);
        } else {
            model.addAttribute("paper", new ExamPaperListView());
        }
        appendEnum(model);
        return "/mockexam/paper/detail";
    }

    static Model appendEnum(Model model) {

        //试卷状态leftmenu.ftl
        model.addAttribute("status", EnumListBuilder.build(ExamPaperEnums.Status.values(),
                (Describable<ExamPaperEnums.Status>) i -> i.desc));

        //试卷来源
        model.addAttribute("source", EnumListBuilder.build(ExamPaperEnums.Source.values(),
                (Describable<ExamPaperEnums.Source>) i -> i.desc));

        //学科
//        model.addAttribute("subject", EnumListBuilder.build(ExamPlanEnums.Subject.values(),
//                (Describable<ExamPlanEnums.Subject>) i -> i.desc));

        // 学科暂时只传数学,等可以预览就用上面的
        model.addAttribute("subject", EnumListBuilder.build(new ExamPlanEnums.Subject[]{ExamPlanEnums.Subject.MATH, ExamPlanEnums.Subject.CHINESE, ExamPlanEnums.Subject.ENGLISH},
                (Describable<ExamPlanEnums.Subject>) i -> i.desc));

        return model;
    }

    /**
     * 获取用户所在的部门
     * @param userId userID
     * @return 部门ID
     */
    private Long fetchUserGroupId(Long userId){
        Long groupId = null;
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(userId);
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            AgentGroupUser groupUser = groupUserList.get(0);
            if (groupUser != null) {
                groupId = groupUser.getGroupId();
            }
        }
        return groupId;
    }

    /**
     * 判断两个区域列表是否有重叠的部分
     * @param r1 区域列表
     * @param r2 区域列表
     * @return 判断结果
     */
    private boolean judgeRegionOverlap(List<Integer> r1, List<Integer> r2){
        if(CollectionUtils.isEmpty(r1) || CollectionUtils.isEmpty(r2)){
            return false;
        }
        if(r1.stream().anyMatch(r2::contains)){
            return true;
        }
        List<Integer> countyCodes1 = agentRegionService.getCountyCodes(r1);
        List<Integer> countyCodes2 = agentRegionService.getCountyCodes(r2);
        return countyCodes1.stream().anyMatch(countyCodes2::contains);
    }

    boolean isAdmin(List<Integer> roles) {
        return roles.contains(AgentRoleType.Admin.getId())
                || roles.contains(AgentRoleType.Country.getId())
                || roles.contains(AgentRoleType.MOCK_EXAM_MANAGER.getId());
    }

    /**
     * 设置试卷预览Url
     * @param views 列表
     */
    private void setPaperPreviewUrl(List<ExamPaperListView> views){
        if(CollectionUtils.isEmpty(views)){
            return;
        }
        final String secretKey;
        final String paperDomain;
        if(RuntimeMode.lt(Mode.STAGING)){
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY_TEST;
            paperDomain = "http://www.test.17zuoye.net";
        }else {
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY;
            if(RuntimeMode.current() == Mode.STAGING){
                paperDomain = "http://www.staging.17zuoye.net";
            }else {
                paperDomain = "https://www.17zuoye.com";
            }
        }
        views.forEach(p -> {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("paperIds", p.getPaperId());
            String sig = AgentApiAuth.generateAppKeySig(paramMap, secretKey);
            p.setPaperPreviewUrl(paperDomain + "/newexamv2/previewpaper.vpage?paperIds=" + p.getPaperId()
                    + "&sig=" + sig + "&app_key=" + AgentApiAuth.APP_KEY);
        });

    }
}
