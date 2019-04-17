package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mockexam.controller.view.*;
import com.voxlearning.utopia.agent.mockexam.controller.view.ExamPlanQueryView.Actions;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.ExamPlanService;
import com.voxlearning.utopia.agent.mockexam.service.ExamReferenceService;
import com.voxlearning.utopia.agent.mockexam.service.dto.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.BatchExamPlanAuditParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPlanAuditParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReportQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.RegionDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportExistDto;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.mockexam.controller.ResourceCode.Operation.*;
import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_DATA;
import static com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder.KEY_ERROR_DESC;

/**
 * 考试计划rest服务
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Slf4j
@Controller
@RequestMapping("mockexam/plan")
public class ExamPlanController extends AbstractAgentController {

    @Resource
    ExamPlanService examPlanService;

    @Resource
    ExamReferenceService referenceService;


    /**
     * 跳转到新建页面
     *
     * @return 新页面地址
     */
    @OperationCode(ResourceCode.Operation.PLAN_FORCREATE)
    @RequestMapping(value = "forcreate.vpage")
    public String forCreate(Model model) {

        Long planId = getRequestLong("id");
        Result<ExamPlanDto> result;
        if(planId > 0){
            result = examPlanService.retrieve(planId);
            if (!result.isSuccess())
                result = examPlanService.getDefaultPlan();
        }else {
            result = examPlanService.getDefaultPlan();
        }

        ExamPlanView view = ExamPlanView.Builder.build(result.getData());
        model.addAttribute("plan", view);

        // 城市列表
        AuthCurrentUser user = getCurrentUser();
        Result<ArrayList<RegionDto>> provinceResult = referenceService.queryProvince(user.getUserId());
        List<RegionDto> provinces = provinceResult.getData();
        model.addAttribute("provinces", provinces);

        // 是否为管理员
        model.addAttribute("isAdmin", isAdmin(user.getRoleList()));

        // 构建其他枚举值列表
        appendEnums(model, isAdmin(user.getRoleList()));

        return "/mockexam/plan/create";
    }

    /**
     * 新建
     *
     * @param view 新建视图模型
     * @return 是否创建成功
     */
    @OperationCode(PLAN_CREATE)
    @ResponseBody
    @RequestMapping(value = "create.vpage", method = RequestMethod.POST)
    public MapMessage create(@RequestBody ExamPlanView view) {
        AuthCurrentUser user = getCurrentUser();
        view.setCreatorId(user.getUserId());
        view.setCreatorName(user.getUserName());
        ExamPlanDto planDto = ExamPlanView.Builder.build(view);
        Result<Boolean> result = examPlanService.create(planDto);
        return ViewBuilder.fetch(result);
    }

    /**
     * 跳转到修改页面
     *
     * @return 修改页面地址
     */
    @OperationCode(PLAN_FORUPDATE)
    @RequestMapping(value = "forupdate.vpage")
    public String forUpdate(@RequestParam(name = "id") Long id, Model model) {
        Result<ExamPlanDto> result = examPlanService.retrieve(id);
        if (!result.isSuccess())
            log.error("查询考试计划详单时发生异常,id=%s,结果 = %s", id, result);
        ExamPlanView view = ExamPlanView.Builder.build(result.getData());
        model.addAttribute("plan", view);

        // 城市列表
        AuthCurrentUser user = getCurrentUser();
        Result<ArrayList<RegionDto>> provinceResult = referenceService.queryProvince(user.getUserId());
        List<RegionDto> provinces = provinceResult.getData();
        model.addAttribute("provinces", provinces);

        // 是否为管理员
        model.addAttribute("isAdmin", isAdmin(user.getRoleList()));

        // 枚举值列表
        appendEnums(model, isAdmin(user.getRoleList()));

        // 基本信息是否可修改
        model.addAttribute("editable",
                ExamPlanEditView.Builder.build(result.getData(), user));

        return "/mockexam/plan/update";
    }

    /**
     * 修改
     */
    @OperationCode(PLAN_UPDATE)
    @ResponseBody
    @RequestMapping(value = "update.vpage", method = RequestMethod.POST)
    public MapMessage update(@RequestBody ExamPlanView view) {
        ExamPlanDto planDto = ExamPlanView.Builder.build(view);
        Result<Boolean> result = examPlanService.update(planDto);
        return ViewBuilder.fetch(result);
    }


    /**
     * 详单
     */
    @OperationCode(PLAN_QUERY)
    @RequestMapping(value = "fordetail.vpage", method = RequestMethod.GET)
    public String forDetail(@RequestParam Long id, Model model) {
        Result<ExamPlanDto> result = examPlanService.retrieve(id);
        if (!result.isSuccess())
            throw new BusinessException(ErrorCode.of(result.getErrorCode()));
        ExamPlanDto dto = result.getData();
        ExamPlanView view = ExamPlanView.Builder.build(dto);
        model.addAttribute("plan", view);
        AuthCurrentUser user = getCurrentUser();
        appendEnums(model, isAdmin(user.getRoleList()));
        return "/mockexam/plan/detail";
    }

    /**
     * 详单
     */
    @OperationCode(PLAN_QUERY)
    @ResponseBody
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public MapMessage detail(@RequestParam Long id) {
        Result<ExamPlanDto> result = examPlanService.retrieve(id);
        if (!result.isSuccess())
            throw new BusinessException(ErrorCode.of(result.getErrorCode()));
        ExamPlanView view = ExamPlanView.Builder.build(result.getData());
        return ViewBuilder.success(view);
    }

    /**
     * 审核
     */
    @OperationCode(PLAN_QUERY)
    @RequestMapping(value = "foraudit.vpage", method = RequestMethod.GET)
    public String forAudit(@RequestParam Long id, Model model) {
        Result<ExamPlanDto> result = examPlanService.retrieve(id);
        if (!result.isSuccess())
            throw new BusinessException(ErrorCode.of(result.getErrorCode()));
        ExamPlanDto dto = result.getData();
        ExamPlanView view = ExamPlanView.Builder.build(dto);
        model.addAttribute("plan", view);
        AuthCurrentUser user = getCurrentUser();
        appendEnums(model, isAdmin(user.getRoleList()));
        return "/mockexam/plan/audit";
    }
    /**
     * 提交修改
     */
    @OperationCode(PLAN_SUBMIT)
    @ResponseBody
    @RequestMapping(value = "submit.vpage", method = RequestMethod.POST)
    public MapMessage submit(@RequestBody ExamPlanView view) {
        AuthCurrentUser user = getCurrentUser();
        view.setCreatorId(user.getUserId());
        view.setCreatorName(user.getRealName());
        view.setOperatorId(user.getUserId());
        view.setOperatorName(user.getRealName());
        view.setOperatorRoles(user.getRoleList());
        ExamPlanDto planDto = ExamPlanView.Builder.build(view);
        Result<Boolean> result = examPlanService.submit(planDto);
        return ViewBuilder.fetch(result);
    }

    /**
     * 提交检查
     */
    @ResponseBody
    @RequestMapping(value = "book_check.vpage", method = RequestMethod.POST)
    public MapMessage submitCheck(@RequestBody ExamPlanView view) {
        AuthCurrentUser user = getCurrentUser();
        view.setCreatorId(user.getUserId());
        view.setCreatorName(user.getRealName());
        view.setOperatorId(user.getUserId());
        view.setOperatorName(user.getRealName());
        view.setOperatorRoles(user.getRoleList());
        ExamPlanDto planDto = ExamPlanView.Builder.build(view);
        Result<Boolean> result = examPlanService.submitBookCheck(planDto);
        return ViewBuilder.fetch(result);
    }

    /**
     * 撤销审核
     */
    @OperationCode(ResourceCode.Operation.PLAN_WITHDRAW)
    @ResponseBody
    @RequestMapping(value = "withdraw.vpage", method = RequestMethod.POST)
    public MapMessage withdraw(@RequestBody OperateRequest params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getRealName());
        params.setOperatorRoles(user.getRoleList());
        Result<Boolean> result = examPlanService.withdraw(params);
        return ViewBuilder.fetch(result);
    }


    /**
     * 审核
     *
     * @param params 审核参数
     * @return 审核结果
     */
    @OperationCode(PLAN_AUDIT)
    @RequestMapping(value = "audit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage audit(@RequestBody ExamPlanAuditParams params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getRealName());
        params.setOperatorRoles(user.getRoleList());
        return ViewBuilder.fetch(examPlanService.audit(params));
    }

    /**
     * 审核
     *
     * @param params 审核参数
     * @return 审核结果
     */
    @OperationCode(PLAN_AUDIT)
    @RequestMapping(value = "batchAudit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchAudit(@RequestBody BatchExamPlanAuditParams params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getRealName());
        params.setOperatorRoles(user.getRoleList());
        return ViewBuilder.fetch(examPlanService.batchAudit(params));
    }


    /**
     * 上线
     *
     * @param params 审核参数
     * @return 审核结果
     */
    @OperationCode(PLAN_ONLINE)
    @RequestMapping(value = "online.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage online(@RequestBody OperateRequest params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getRealName());
        Result<Boolean> result = examPlanService.online(params);
        return ViewBuilder.fetch(result);
    }

    /**
     * 下线
     *
     * @param params 审核参数
     * @return 审核结果
     */
    @OperationCode(PLAN_OFFLINE)
    @RequestMapping(value = "offline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offline(@RequestBody OperateRequest params) {
        AuthCurrentUser user = getCurrentUser();
        params.setOperatorId(user.getUserId());
        params.setOperatorName(user.getRealName());
        Result<Boolean> result = examPlanService.offline(params);
        return ViewBuilder.fetch(result);
    }

    /**
     * 列表
     */
    @OperationCode(PLAN_FORQUERY)
    @RequestMapping(value = "forquery.vpage")
    public String forQuery(Model model) {
        AuthCurrentUser user = getCurrentUser();
        appendEnums(model, isAdmin(user.getRoleList()));
        return "/mockexam/plan/list";
    }

    /**
     * 分页查询
     *
     * @param params 测评计划分页参数
     * @return 分页列表
     */
    @OperationCode(PLAN_QUERY)
    @ResponseBody
    @RequestMapping(value = "queryPage.vpage", method = RequestMethod.POST)
    public MapMessage queryPage(@RequestBody ExamPlanPageQueryParams params) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(params.getPage());
        pageInfo.setSize(params.getSize());
        AuthCurrentUser user = getCurrentUser();
        params.setCurrentUserId(user.getUserId());
        params.setCurrentUserRole(user.getRoleList());

        PageResult<ExamPlanDto> result = examPlanService.queryPage(params, pageInfo);

        MapMessage message;
        if (result.isSuccess()) {
            List<ExamPlanQueryView> views = result.getData().stream()
                    .map(ExamPlanQueryView.Builder::build).collect(Collectors.toList());
            views.stream().forEach(i -> route(i, user));
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
     * 根据名称模糊查找
     *
     * @param creator
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "creators.vpage", method = RequestMethod.GET)
    public MapMessage queryCreators(@RequestParam(name = "creator") String creator) {
        Result<ArrayList<String>> nameList = examPlanService.queryCreator(creator);
        return ViewBuilder.fetch(nameList);
    }

    /**
     * 根据测评 ID 查询该测评对应区域
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "regions.vpage", method = RequestMethod.GET)
    public MapMessage queryRegions(@RequestParam(name = "id") Long id) {
        Result<ExamPlanDto> examPlanDtoResult = examPlanService.queryRegions(id);
        if (!examPlanDtoResult.isSuccess()){
            throw new BusinessException(ErrorCode.of(examPlanDtoResult.getErrorCode()));
        }
        List<ExamPlanView.Builder.RegionView> viewList = ExamPlanView.Builder.
                buildRegionInfo(examPlanDtoResult.getData());
        return MapMessage.successMessage().set("data",viewList);
    }

    /**
     * 根据 考试ID 和 区域编码 查询报告是否存在
     * @param params 参数
     * @return
     */
    @RequestMapping(value = "queryReportIsExist.vpage" ,method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryWhetherReportExist(@RequestBody ExamReportQueryParams params){
        Result<ReportExistDto> reportExistDtoResult = examPlanService.queryReportExistInfo(params);
        if (!reportExistDtoResult.isSuccess()){
            throw new BusinessException(ErrorCode.of(reportExistDtoResult.getErrorCode()));
        }
        return MapMessage.successMessage().set("data",reportExistDtoResult.getData());
    }

    /**
     * 生成数字签名
     * @param params 参数
     * @return
     */
    @RequestMapping(value = "querySign.vpage" ,method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDigestSign(@RequestBody ExamReportQueryParams params){
        Map<String, String> map = new HashedMap<>();
        if (!StringUtils.isEmpty(params.getPaperId())){
            map.put("paperId",params.getPaperId());
        }
        if (!StringUtils.isEmpty(params.getExamId())){
            map.put("examId",params.getExamId());
        }
        if (!StringUtils.isEmpty(params.getRegionCode())){
            map.put("regionCode",params.getRegionCode());
        }
        if (!StringUtils.isEmpty(params.getRegionLevel())){
            map.put("regionLevel",params.getRegionLevel());
        }
        String secret = "ob3ZkwDyJ02AsNvV";
        String digestSign = DigestSignUtils.signMd5(map,secret);
        return MapMessage.successMessage().set("data",digestSign);
    }

    /**
     * 添加枚举值
     *
     * @param model
     * @return
     */
    static Model appendEnums(Model model, boolean isAdmin) {

        // 学科
        model.addAttribute("subject",
                EnumListBuilder.build(Subject.values(),
                        (Describable<Subject>) i -> i.desc));

        // 类型
        model.addAttribute("type",
                EnumListBuilder.build(Type.values(),
                        (Describable<Type>) i -> i.desc));

        // 年级
        model.addAttribute("grade",
                EnumListBuilder.build(Grade.values(),
                        (Describable<Grade>) i -> i.desc));

        // 形式
        model.addAttribute("form",
                EnumListBuilder.build(Form.values(),
                        (Describable<Form>) i -> i.desc));

        // 试卷来源
        model.addAttribute("paperType",
                EnumListBuilder.build(PaperType.values(),
                        (Describable<PaperType>) i -> i.desc));

        // 分发方式
        model.addAttribute("distributeType",
                EnumListBuilder.build(DistributeType.values(),
                        (Describable<DistributeType>) i -> i.desc));

        // 场景
        model.addAttribute("scene",
                EnumListBuilder.build(Scene.values(),
                        (Describable<Scene>) i -> i.desc));

        //模式
        model.addAttribute("pattern",
                EnumListBuilder.build(Pattern.values(),
                        (Describable<Pattern>) i -> i.desc));

        // 区域类型
        model.addAttribute("regionLevel",
                EnumListBuilder.build(isAdmin? RegionLevel.values() : new RegionLevel[]{RegionLevel.CITY, RegionLevel.COUNTY, RegionLevel.SCHOOL},
                        (Describable<RegionLevel>) i -> i.desc));

        // 布尔值
        model.addAttribute("booleanEnum",
                EnumListBuilder.build(BooleanEnum.values(),
                        (Describable<BooleanEnum>) i -> i.desc));

        // 口语算分逻辑
        model.addAttribute("spokenScoreType",
                EnumListBuilder.build(SpokenScoreType.values(),
                        (Describable<SpokenScoreType>) i -> i.desc));

        // 口语可答题次数
        model.addAttribute("spokenTimes",
                EnumListBuilder.build(SpokenAnswerTimes.values(),
                        (Describable<SpokenAnswerTimes>) i -> i.desc));

        // 成绩类型
        model.addAttribute("scoreRuleType",
                EnumListBuilder.build(ScoreRuleType.values(),
                        (Describable<ScoreRuleType>) i -> i.desc));

        // 状态
//        if (isAdmin) {
//            // 管理员去掉被拒绝
//            model.addAttribute("status", Arrays.stream(Status.values()).filter(i -> Status.PLAN_WITHDRAW != i).map(
//                    i -> {
//                        KeyValue<String, String> kv = new KeyValue<>();
//                        kv.setKey(i.name());
//                        kv.setValue(i.desc);
//                        return kv;
//                    }).collect(Collectors.toList()));
//        } else {
            model.addAttribute("status",
                    EnumListBuilder.build(Status.values(),
                            (Describable<Status>) i -> i.desc));
//        }
        model.addAttribute("isAdmin", isAdmin);

        return model;
    }

    /**
     * 基本信息是否可修改
     *
     * @param view 视图
     * @param user 当前登录用户
     * @return boolean值
     */
    public boolean isBasicInfoEditable(ExamPlanView view, AuthCurrentUser user) {
        if (null == view) {
            return false;
        } else {
            if (Arrays.asList(Status.PAPER_CHECKING, Status.PAPER_PROCESSING,
                    Status.PAPER_READY, Status.EXAM_PUBLISHED, Status.EXAM_OFFLINE)
                    .contains(Status.valueOf(view.getStatus())))
                return false;
            else
                return true;
        }
    }

    /**
     * 路由动作
     *
     * @param view 视图模型
     * @param user 用户
     * @return
     */
    public ExamPlanQueryView route(ExamPlanQueryView view, AuthCurrentUser user) {
        final Long id = view.getId();
        final Status status = Status.valueOf(view.getStatus());
        if (isAdmin(user.getRoleList())) {
            switch (status) {
                case PLAN_AUDITING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_AUDIT, Actions.PLAN_DOWNLOAD_ATTACHMENT));
                    break;
                case PLAN_WITHDRAW:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY));
                    break;
                case PLAN_REJECT:
                    view.setActions(Collections.singletonList(Actions.PLAN_DETAIL));
                    break;
                case PAPER_CHECKING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_WITHDRAW));
                    break;
                case PAPER_REJECT:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_WITHDRAW));
                    break;
                case PAPER_PROCESSING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_WITHDRAW));
                    break;
                case PAPER_READY:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_ONLINE, Actions.PLAN_WITHDRAW));
                    break;
                case EXAM_PUBLISHED:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_OFFLINE,
                            Actions.EXAM_REPLENISH, Actions.EXAM_MAKEUP, Actions.EXAM_SCORE,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(DateUtils.addDays(new Date(), -1)) ? Actions.EXAM_SCORE_DONWLOAD : null,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(new Date()) && Subject.MATH.desc.equals(view.get_subject()) ? Actions.EXAM_REPORT_DONWLOAD : null)
                            .stream().filter(Objects::nonNull).collect(Collectors.toList()));
                    break;
                case EXAM_OFFLINE:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_ONLINE,
                            Actions.EXAM_REPLENISH, Actions.EXAM_MAKEUP, Actions.EXAM_SCORE,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(DateUtils.addDays(new Date(), -1)) ? Actions.EXAM_SCORE_DONWLOAD : null,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(new Date()) && Subject.MATH.desc.equals(view.get_subject()) ? Actions.EXAM_REPORT_DONWLOAD : null)
                            .stream().filter(Objects::nonNull).collect(Collectors.toList()));
                    break;
                default:
                    throw new BusinessException(ErrorCode.PLAN_PAPER_UNKNOWN_STATUS,
                            String.format("错误的状态[id=%s][status=%s", id, status));
            }
        } else {
            switch (status) {
                case PLAN_AUDITING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_WITHDRAW, Actions.PLAN_COPY));
                    break;
                case PLAN_WITHDRAW:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY));
                    break;
                case PLAN_REJECT:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY));
                    break;
                case PAPER_CHECKING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL));
                    break;
                case PAPER_REJECT:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY));
                    break;
                case PAPER_PROCESSING:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL));
                    break;
                case PAPER_READY:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.PLAN_ONLINE));
                    break;
                case EXAM_PUBLISHED:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.PLAN_MODIFY, Actions.EXAM_SCORE,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(DateUtils.addMinutes(new Date(), -10)) ? Actions.EXAM_SCORE_DONWLOAD : null,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(new Date()) && Subject.MATH.desc.equals(view.get_subject()) ? Actions.EXAM_REPORT_DONWLOAD : null)
                            .stream().filter(Objects::nonNull).collect(Collectors.toList()));
                    break;
                case EXAM_OFFLINE:
                    view.setActions(Arrays.asList(Actions.PLAN_DETAIL, Actions.EXAM_SCORE,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(DateUtils.addMinutes(new Date(), -10)) ? Actions.EXAM_SCORE_DONWLOAD : null,
                            view.getScorePublishTime() != null && view.getScorePublishTime().before(new Date()) && Subject.MATH.desc.equals(view.get_subject()) ? Actions.EXAM_REPORT_DONWLOAD : null)
                            .stream().filter(Objects::nonNull).collect(Collectors.toList()));
                    break;
                default:
                    throw new BusinessException(ErrorCode.PLAN_PAPER_UNKNOWN_STATUS,
                            String.format("错误的状态[id=%s][status=%s", id, status));
            }
        }
        return view;
    }

    /**
     * 判断是否为管理员
     *
     * @param roles 角色列表
     * @return 是否为管理员
     */
    boolean isAdmin(List<Integer> roles) {
        return roles.contains(AgentRoleType.Admin.getId())
                || roles.contains(AgentRoleType.Country.getId())
                || roles.contains(AgentRoleType.MOCK_EXAM_MANAGER.getId());
    }
}
