package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.utopia.service.campaign.api.constant.EvaluationParam;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCoursewareComment;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareEvaluationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.data.errorCode.coursewareErrorCode;
import com.voxlearning.washington.data.requestParam.EvaluationRequestParam;
import com.voxlearning.washington.data.view.LabelView;
import com.voxlearning.washington.data.view.TeacherCoursewarEvaluationView;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 课件评价信息接口
 *
 * @Author: peng.zhang
 * @Date: 2018/10/11
 */
@Controller
@RequestMapping("/courseware/evaluation")
public class TeacherCourseEvaluationController extends AbstractController {

    @Inject
    private TeacherCoursewareEvaluationServiceClient evaluationServiceClient;

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    public static final Integer COMMENT_NUM_WHICH_SHOW_TOTAL_SCORE = 3;

    /**
     * 根据课件 id 获取总评价信息
     * @return 课件综合评价信息
     */
    @RequestMapping(value = "evaluations.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchEvaluation(){
        String coursewareId = getRequestString("coursewareId");
        TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.loadTeacherCoursewareById(coursewareId);

        Map<String,Integer> labelInfo = new HashedMap<>();
        Integer commentNum = 0;
        Integer totalScore = 0;
        if ( null != teacherCourseware ){
            labelInfo = teacherCourseware.getLabelInfo();
            commentNum = teacherCourseware.getCommentNum();
            totalScore = teacherCourseware.getTotalScore();
        }
        List<LabelView> labelViewList = new ArrayList<>();
        if ( null != labelInfo && !labelInfo.isEmpty()){
            labelViewList = LabelView.Builder.build(labelInfo);
        }
        return MapMessage.successMessage().set("labelInfo", labelViewList).
                set("totalScore",commentNum < COMMENT_NUM_WHICH_SHOW_TOTAL_SCORE ?
                         0 : totalScore).
                set("commentNum",commentNum);
    }

    /**
     * 获取个人评价信息接口,以及是否评价过
     * @return 课件的个人评价信息
     */
    @RequestMapping(value = "personageEvaluations.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchPersonageEvaluation(){
        // 检查是否登录
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return MapMessage.errorMessage().setInfo("请先登录").setErrorCode(coursewareErrorCode.NOTEXIST_ERROR);
        }

        TeacherDetail teacherDetail = currentTeacherDetail();
        Long teacherId = teacherDetail.getId();
        String coursewareId = getRequestString("coursewareId");
        List<TeacherCoursewareComment> teacherCoursewareCommentList = evaluationServiceClient.fetchEvaluationByTeacherId(teacherId,coursewareId);
        TeacherCoursewareComment teacherCoursewareComment = new TeacherCoursewareComment();
        if (CollectionUtils.isNotEmpty(teacherCoursewareCommentList)){
            teacherCoursewareComment = teacherCoursewareCommentList.get(0);
        }
        if (teacherCoursewareComment.getId() == null){
            return MapMessage.successMessage().set("everEvaluation", false);
        } else {
            return MapMessage.successMessage().set("data",
                    TeacherCoursewarEvaluationView.Builder.build(teacherCoursewareComment)).
                    set("everEvaluation",true);
        }
    }

    /**
     * 创建评价信息接口
     * @param requestParam
     * @return
     */
    @RequestMapping(value = "createEvaluations.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createEvaluation(@RequestBody EvaluationRequestParam requestParam){
        // 检查是否登录
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }

        EvaluationParam evaluationParam = new EvaluationParam();
        try {
            BeanUtils.copyProperties(evaluationParam, requestParam);
            evaluationParam.setTeacherId(teacherDetail.getId());

            List<TeacherCoursewareComment> teacherCoursewareCommentList = evaluationServiceClient.fetchEvaluationByTeacherId(teacherDetail.getId(), evaluationParam.getCoursewareId());
            if (CollectionUtils.isNotEmpty(teacherCoursewareCommentList)) {
                return MapMessage.errorMessage("您已经评价过了!");
            }

            String isAuthentication = teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS ? "Y" : "N";
            evaluationParam.setIsAuthentication(isAuthentication);
            MapMessage message = evaluationServiceClient.createEvaluation(evaluationParam);
            if (message.isSuccess()){
                // 评价成功更新课件总评论表
                TeacherCourseware teacherCourseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(requestParam.getCoursewareId());
                teacherCoursewareContestServiceClient.updateCommentNum(requestParam.getCoursewareId(), teacherCourseware.getCommentNum() + 1);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage().setInfo(message.getInfo());
            }
        } catch (Exception e) {
            return MapMessage.errorMessage().set("error",e);
        }
    }

    /**
     * 判断是否可以评价接口
     * @return 可以评价返回 true ,不可以返回 false
     */
    @RequestMapping(value = "couldEvaluate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage couldEvaluateInfo(){
        TeacherDetail teacherDetail = currentTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("请先登录");
        }
        Long teacherId = teacherDetail.getId();
        Boolean result = false;
        // 判断是否是认证用户,如果是的话返回 true
        // 非认证用户,已评论个数小于 5 ,返回 true ,否则 false
        result = teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS ? true : evaluationServiceClient.couldEvaluate(teacherId);
        return MapMessage.successMessage().set("couldEvaluate",result).
                set("isAuthentication",teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS);
    }
}
