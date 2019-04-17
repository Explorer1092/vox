package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 测评计划编辑视图，用于向前端提供可变性属性封装
 *
 * @author xiaolei.li
 * @version 2018/9/3
 */
@Data
public class ExamPlanEditView implements Serializable {
    private boolean name;
    private boolean subject;
    private boolean type;
    private boolean grade;
    private boolean form;
    private boolean book;
    private boolean paper;
    private boolean paperDoc;
    private boolean distributeType;
    private boolean scene;
    private boolean region;
    private boolean totalScore;

    public static class Builder {
        public static ExamPlanEditView build(ExamPlanDto plan, AuthCurrentUser user) {
            ExamPlanEditView editView;
            ExamPlanEnums.Status status = plan.getStatus();
            if (isAdmin(user.getRoleList())) {
                editView = buildAllTrue();
                switch (status) {
                    case PLAN_REJECT:
                    case PLAN_WITHDRAW:
                    case PAPER_REJECT:
                        break;
                    case PAPER_CHECKING:
                        editView.subject = false;
//                        editView.type = false;
                        editView.paper = false;
                        editView.paperDoc = false;
                        break;
                    case PLAN_AUDITING:
                    case PAPER_PROCESSING:
                    case PAPER_READY:
                    case EXAM_PUBLISHED:
                    case EXAM_OFFLINE:
                        editView.subject = false;
//                        editView.type = false;
                        editView.paper = false;
                        editView.paperDoc = false;
                        editView.totalScore = false;
                        break;
                    default:
                        throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                String.format("尚未处理的测评状态[id=%s][status=%s]", plan.getId(), status));
                }
            } else {
                switch (status) {
                    case PLAN_REJECT:
                    case PLAN_WITHDRAW:
                    case PAPER_REJECT:
                        editView = buildAllTrue();
                        break;
                    case PLAN_AUDITING:
                    case PAPER_CHECKING:
                    case PAPER_PROCESSING:
                    case PAPER_READY:
                    case EXAM_PUBLISHED:
                    case EXAM_OFFLINE:
                        editView = buildAllFalse();
                        break;
                    default:
                        throw new BusinessException(ErrorCode.PLAN_CONSTRAINT,
                                String.format("尚未处理的测评状态[id=%s][status=%s]", plan.getId(), status));
                }
            }
            return editView;
        }

        public static ExamPlanEditView buildAllTrue() {
            ExamPlanEditView view = new ExamPlanEditView();
            view.name = true;
            view.subject = true;
            view.type = true;
            view.grade = true;
            view.form = true;
            view.book = true;
            view.paper = true;
            view.paperDoc = true;
            view.distributeType = true;
            view.scene = true;
            view.region = true;
            view.totalScore = true;
            return view;
        }

        public static ExamPlanEditView buildAllFalse() {
            ExamPlanEditView view = new ExamPlanEditView();
            view.name = false;
            view.subject = false;
            view.type = false;
            view.grade = false;
            view.form = false;
            view.book = false;
            view.paper = false;
            view.paperDoc = false;
            view.distributeType = false;
            view.scene = false;
            view.region = false;
            view.totalScore = false;
            return view;
        }
    }

    /**
     * 判断是否为管理员
     *
     * @param roles 角色列表
     * @return 是否为管理员
     */
    static boolean isAdmin(List<Integer> roles) {
        return roles.contains(AgentRoleType.Admin.getId())
                || roles.contains(AgentRoleType.Country.getId())
                || roles.contains(AgentRoleType.MOCK_EXAM_MANAGER.getId());
    }
}
