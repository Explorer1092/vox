/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.mobile;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端、家长端通用的接口
 *
 * @author shiwe.liao
 * @since 2016/4/26
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/")
public class MobileUserController extends AbstractMobileJxtController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    /**
     * 通知详情 老师端、家长端通用
     */
    @RequestMapping(value = "/jxt/notice/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNoticeInfo() {
        String noticeId = getRequestString("notice_id");
        Long groupId = getRequestLong("group_id");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        JxtNotice jxtNotice = jxtLoaderClient.getJxtNoticeById(noticeId);
        if (jxtNotice == null) {
            return MapMessage.errorMessage("您要查看的通知不存在");
        }
        if (!jxtNotice.getGroupIds().contains(groupId)) {
            return MapMessage.errorMessage("通知{}并不包含组{}", noticeId, groupId);
        }
        Map<String, Object> map = generateNoticeInfoMap(jxtNotice, groupId, user);
        return MapMessage.successMessage().add("notice_info", map);
    }

    /**
     * 作业分享详情页 老师端、家长端通用
     */
    @RequestMapping(value = "/share/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserShareInfo() {
        String shareId = getRequestString("user_share_id");
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        JxtUserShare userShare = jxtLoaderClient.getUserShareById(shareId);
        if (userShare == null) {
            return MapMessage.errorMessage("需要查看的分享不存在");
        }
        return generateUserShareMap(userShare, user.getId());
    }

    /**
     * 作业分享点赞 老师端、家长端通用
     */
    @RequestMapping(value = "/jxt/vote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voteUserShare() {
        Long studentId = getRequestLong("sid");
        String voteTypeName = getRequestString("vote_type");
        String typeId = getRequestString("type_id");
        String ver = getRequestString("ver");
        String voteLevelName = getRequestString("vote_level");

        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请登录账号");
        }
        JxtVoteType voteType = JxtVoteType.parse(voteTypeName);
        JxtVoteLevel voteLevel = JxtVoteLevel.parse(voteLevelName);
        if (JxtVoteType.UNKNOWN == voteType) {
            return MapMessage.errorMessage("点赞类型错误");
        }
        Long ownerId = 0L;
        //被点赞人名字
        String votedUserName = "";
        if (JxtVoteType.HOMEWORK_SHARE == voteType) {
            JxtUserShare userShare = jxtLoaderClient.getUserShareById(typeId);
            if (userShare == null) {
                return MapMessage.errorMessage("需要查看的分享不存在");
            }
            ownerId = userShare.getUserId();
            votedUserName = userShare.getUserName();
        } else if (JxtVoteType.VOICE_RECOMMEND == voteType) {
            //语音推荐的id和homeworkId是一样的
            List<VoiceRecommend> voiceRecommendList = voiceRecommendLoaderClient.loadExcludeNoRecommend(Collections.singleton(typeId));
            if (CollectionUtils.isEmpty(voiceRecommendList)) {
                return MapMessage.errorMessage("需要查看语音推荐不存在");
            }
            ownerId = voiceRecommendList.get(0).getTeacherId();
            Teacher ownerTeacher = teacherLoaderClient.loadTeacher(ownerId);
            String subjectName = ownerTeacher != null && ownerTeacher.getSubject() != null ? ownerTeacher.getSubject().getValue() : "";
            votedUserName = ownerTeacher == null ? "" : subjectName + ownerTeacher.fetchRealname() + "老师";
        } else if (JxtVoteType.JXT_NEWS_COMMENT == voteType) {
            //获取资讯评论的userName
            JxtNewsComment jxtNewsComment = jxtNewsLoaderClient.getCommentById(typeId);
            votedUserName = jxtNewsComment.getUserName();
        }
        //不能给自己点赞
        if (ownerId.equals(user.getId())) {
            return MapMessage.errorMessage("不能点赞自己的线下作业分享");
        }

        String typeAndId = JxtUserVoteRecord.generateTypeAndId(voteType, typeId);
        List<JxtUserVoteRecord> voteRecords = jxtLoaderClient.getVoteRecordByUserId(user.getId());
        if (CollectionUtils.isNotEmpty(voteRecords) && voteRecords.stream().anyMatch(p -> typeAndId.equals(p.getTypeAndId()))) {
            //已经点过赞了
            String info = "您已经对该次" + voteType.getName() + "点赞过了";
            return MapMessage.errorMessage(info);
        }

        String userName = "";
        if (UserType.PARENT == user.fetchUserType()) {
            if (JxtVoteType.JXT_NEWS_COMMENT == voteType || JxtVoteType.JXT_NEWS == voteType || JxtVoteType.USER_MISSION_DETAIL == voteType) {
                userName = generateUserName(user);
            } else {
                //从聊天组里面取最准确的名字
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                if (CollectionUtils.isEmpty(studentParentRefs) || !studentParentRefs.stream().anyMatch(p -> p.getParentId().equals(user.getId()))) {
                    return MapMessage.errorMessage("此学生与当前家长无关联");
                }
                StudentParentRef studentParentRef = studentParentRefs.stream().filter(p -> p.getParentId().equals(user.getId())).findFirst().orElse(null);
                Student student = studentLoaderClient.loadStudent(studentId);
                userName = student.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());
            }
        } else if (UserType.TEACHER == user.fetchUserType()) {
            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
            userName = subjectName + user.fetchRealname() + "老师";
        }

        JxtUserVoteRecord voteRecord = new JxtUserVoteRecord();
        voteRecord.setTypeId(typeId);
        voteRecord.setVoteType(voteType);
        voteRecord.setUserId(user.getId());
        voteRecord.setUserName(userName);
        voteRecord.setStudentId(studentId);
        voteRecord.setVoteLevel(voteLevel);

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(jxtServiceClient)
                    .keyPrefix("jxt_vote")
                    .keys(user.getId(), voteType.name(), typeId, voteLevel.name())
                    .proxy()
                    .saveUserVoteRecord(voteRecord, ownerId);
            if (mapMessage.isSuccess()) {
                jxtServiceClient.updateCacheWithSaveVoteRecord(voteRecord, ownerId);
                if (UserType.PARENT == user.fetchUserType() && VersionUtil.compareVersion(ver, "1.5.1") < 0) {
                    //返回被点赞人的名称
                    mapMessage.add("vote_user", votedUserName);
                } else if (UserType.TEACHER == user.fetchUserType() && VersionUtil.compareVersion(ver, "1.2.0") < 0) {
                    //返回被点赞人的名称
                    mapMessage.add("vote_user", votedUserName);
                } else {
                    //新版本要返回点赞结果的全部文案
                    String content = userName + "给" + votedUserName + "的" + voteType.getName() + "点赞";
                    if (JxtVoteType.JXT_NEWS == voteType) {
                        content = "";
                    }
                    mapMessage.add("vote_user", content);
                }

            }
            return mapMessage;
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在点赞，请稍后");
        } catch (Exception e) {
            return MapMessage.errorMessage("服务器错误，请稍后重试");
        }
    }


    @RequestMapping(value = "/voice_recommend_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getVoiceRecommendInfo() {
        String id = getRequestString("recommend_id");
        Long sid = getRequestLong("sid");

        User user = currentUser();
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("语音推荐Id不能为空");
        }
        if (user == null) {
            return MapMessage.errorMessage("请登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<VoiceRecommend> voiceRecommendList = voiceRecommendLoaderClient.loadExcludeNoRecommend(Collections.singleton(id));
        if (CollectionUtils.isEmpty(voiceRecommendList)) {
            return MapMessage.errorMessage("语音推荐不存在");
        }
        VoiceRecommend voiceRecommend = voiceRecommendList.get(0);
        return MapMessage.successMessage().add("voice_recommend_info", generateVoiceRecommendMap(voiceRecommend, user, sid));
    }

    @RequestMapping(value = "/dubbing_recommend_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDubbingVoiceRecommendInfo() {
        String id = getRequestString("recommend_id");
        Long sid = getRequestLong("sid");
        User user = currentUser();
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("语音推荐Id不能为空");
        }
        if (user == null) {
            return MapMessage.errorMessage("请登录账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<DubbingRecommend> dubbingRecommends = dubbingScoreRecommendLoaderClient.loadExcludeNoRecommend(Collections.singleton(id));
        if (CollectionUtils.isEmpty(dubbingRecommends)) {
            return MapMessage.errorMessage("趣味配音推荐不存在");
        }
        DubbingRecommend recommend = dubbingRecommends.get(0);
        return MapMessage.successMessage().add("dubbing_recommend_info", generateDubbingRecommendMap(recommend, user, sid));
    }

    private Map<String, Object> generateDubbingRecommendMap(DubbingRecommend recommend, User user, Long sid) {
        Map<String, Object> result = new HashMap<>();
        if (recommend == null || user == null || CollectionUtils.isEmpty(recommend.getExcellentDubbingStu())) {
            return result;
        }
        UserType userType = user.fetchUserType();
        result.put("comment", recommend.getRecommendComment());
        List<Map<String, Object>> excellentDubbingStu = Lists.newLinkedList();
        for (BaseVoiceRecommend.DubbingWithScore dubbingWithScore : recommend.getExcellentDubbingStu()) {
            //学生访问时只返回学生自己的语音
            if (UserType.STUDENT == userType && !dubbingWithScore.getUserId().equals(user.getId())) {
                continue;
            }
            Map<String, Object> voiceMap = new HashMap<>();
            voiceMap.put("videoName", dubbingWithScore.getVideoName());
            voiceMap.put("dubbingId", dubbingWithScore.getDubbingId());
            voiceMap.put("userId", dubbingWithScore.getUserId());
            voiceMap.put("userName", dubbingWithScore.getUserName());
            voiceMap.put("score", dubbingWithScore.getScore());
            voiceMap.put("duration", dubbingWithScore.getDuration());
            voiceMap.put("studentVideoUrl", dubbingWithScore.getStudentVideoUrl());
            voiceMap.put("coverUrl", dubbingWithScore.getCoverUrl());
            voiceMap.put("practiseName", ObjectiveConfigType.DUBBING_WITH_SCORE.getValue());
            excellentDubbingStu.add(voiceMap);
        }
        result.put("excellentDubbingStu", excellentDubbingStu);//学生配音
        //生成时间
        result.put("createTime", DateUtils.dateToString(recommend.getCreateTime(), "yyyy年MM月dd日"));
        //发布人
        Teacher teacher = teacherLoaderClient.loadTeacher(recommend.getTeacherId());
        String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
        result.put("teacherName", teacher == null ? "" : subjectName + teacher.fetchRealname() + "老师");
        result.put("userType", user.fetchUserType().getType());
        result.put("buttonUrl", UrlUtils.buildUrlQuery("/view/mobile/parent/homework/report_detail", MapUtils.m("tab", "clazz", "sid", sid, "hid", recommend.getHomeworkId())));
        Student student = studentLoaderClient.loadStudent(sid);
        result.put("userName", student == null ? "" : student.fetchRealname());
        return result;
    }

    //生成返回前端的消息列表
    private Map<String, Object> generateNoticeInfoMap(JxtNotice jxtNotice, Long groupId, User user) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNotice == null || groupId == null || user == null || jxtNotice.getExpireTime() == null) {
            return map;
        }
        if (JxtNoticeType.UNKNOWN == JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType())) {
            return map;
        }
        GroupMapper groupMapper = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                .loadGroupDetail(groupId, true)
                .firstOrNull();
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        //通知的反馈
        Map<String, List<JxtFeedBack>> feedBackListByNoticeIds = jxtLoaderClient.getFeedBackListByNoticeIds(Collections.singleton(jxtNotice.getId()));
        List<JxtFeedBack> feedBackList = feedBackListByNoticeIds.containsKey(jxtNotice.getId()) ? feedBackListByNoticeIds.get(jxtNotice.getId()) : new ArrayList<>();
        //环信聊天组
        map.put("notice_id", jxtNotice.getId());
        //通知类型
        map.put("notice_type", JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType()).getName());
        //通知时间
        map.put("start_date", DateUtils.dateToString(jxtNotice.getCreateTime(), "M月dd日 HH:mm"));
        map.put("end_date", DateUtils.dateToString(jxtNotice.getExpireTime(), "M月dd日 HH:mm"));
        map.put("is_expire", jxtNotice.getExpireTime().before(new Date()));
        map.put("page_is_expire", DateUtils.dayDiff(new Date(), jxtNotice.getExpireTime()) > 30);
        //通知文本内容
        String content = SafeConverter.toString(jxtNotice.getContent(), "");
        map.put("notice_content", content.replaceAll("作业", "练习").replaceAll("布置", "推荐").replaceAll("督促", "提醒"));
        //图片
        List<String> imgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jxtNotice.getImgUrl())) {
            jxtNotice.getImgUrl().stream().forEach(p -> imgList.add(OSS_IMAGE_HOST + p));
        }
        map.put("notice_img_list", imgList);
        //语音
        map.put("notice_voice", StringUtils.isBlank(jxtNotice.getVoiceUrl()) ? "" : OSS_HOST + jxtNotice.getVoiceUrl());
        //发布人
        Teacher teacher = teacherLoaderClient.loadTeacher(jxtNotice.getTeacherId());
        if (jxtNotice.getTeacherId().equals(user.getId())) {
            map.put("notice_author", "我");
            map.put("is_self_open", Boolean.TRUE);
        } else {
            if (teacher == null) {
                map.put("notice_author", "老师");
            } else {
                if (CollectionUtils.isEmpty(teacher.getSubjects())) {
                    map.put("notice_author", (StringUtils.isBlank(teacher.fetchRealname()) ? "" : teacher.fetchRealname().substring(0, 1)) + "老师");
                } else if (teacher.getSubjects().size() > 1) {  //老师包班制多个学科
                    //包班制老师如果在这个班有多个学科,则姓名不显示学科
                    List<Subject> subjectList = teacherLoaderClient.findTeacherAllSubjectInClazz(groupMapper.getClazzId(), teacher.getId());
                    if (CollectionUtils.isNotEmpty(subjectList) && subjectList.size() > 1)
                        map.put("notice_author", (StringUtils.isBlank(teacher.fetchRealname()) ? "" : teacher.fetchRealname().substring(0, 1)) + "老师");
                    else
                        map.put("notice_author", teacher.getSubject().getValue() + (StringUtils.isBlank(teacher.fetchRealname()) ? "" : teacher.fetchRealname().substring(0, 1)) + "老师");
                } else // 老师只有一个学科
                    map.put("notice_author", teacher.getSubject().getValue() + (StringUtils.isBlank(teacher.fetchRealname()) ? "" : teacher.fetchRealname().substring(0, 1)) + "老师");

            }
            map.put("is_self_open", Boolean.FALSE);
        }
        //通知是否需要反馈
        map.put("need_feed_back", jxtNotice.getNeedFeedBack());
        //班级名称
        map.put("clazz_name", clazz.formalizeClazzName());


        //反馈人名称列表
        List<JxtFeedBack> noticeFeedBackList = feedBackList.stream().filter(p -> p.getGroupId().equals(groupId)).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        //已反馈的ParentId
        Set<Long> hadFeedBackParentIds = noticeFeedBackList.stream().map(JxtFeedBack::getParentId).collect(Collectors.toSet());
        //已反馈的学生Id
        Set<Long> hadFeedBackStudentIds = noticeFeedBackList.stream().map(JxtFeedBack::getStudentId).collect(Collectors.toSet());
        //未反馈人列表
        List<String> notFeedBackStudentNames = groupMapper.getStudents().stream().filter(p -> !hadFeedBackStudentIds.contains(p.getId())).map(GroupMapper.GroupUser::getName).collect(Collectors.toList());
        map.put("user_type", user.fetchUserType().getType());
        if (UserType.PARENT == user.fetchUserType()) {
            //当是家长时要返回当前家长是否已反馈
            map.put("had_feed_back", hadFeedBackParentIds.contains(user.getId()));
            //已反馈学生数
            map.put("feed_back_user_count", hadFeedBackStudentIds.size());
            //未反馈学生数
            map.put("not_feed_back_user_count", notFeedBackStudentNames.size());
        } else if (UserType.TEACHER == user.fetchUserType()) {
            //当是老师时需要返回是否已发送一键提醒
            map.put("had_teacher_remind", jxtNotice.getHadTeacherRemindMap().get(groupId));
            //已反馈学生名称列表
            //因为一个孩子可以有多个家长确认。所以先收集为set再转list
            map.put("feed_back_user_list", new ArrayList<>(noticeFeedBackList.stream().map(JxtFeedBack::getStudentName).collect(Collectors.toSet())));
            //未反馈学生名称列表
            map.put("not_feed_back_user_list", notFeedBackStudentNames);
        }
        return map;
    }

    //用户分享详情
    private MapMessage generateUserShareMap(JxtUserShare userShare, Long currentUserId) {
        MapMessage message = MapMessage.successMessage();
        if (userShare == null) {
            return message;
        }

        List<JxtUserVoteRecord> voteRecordList = jxtLoaderClient.getVoteRecordByTypeAndId(JxtVoteType.HOMEWORK_SHARE, userShare.getId());
        message.add("user_share_id", userShare.getId());
        message.add("user_share_content", userShare.getContent());
        if (CollectionUtils.isNotEmpty(userShare.getImgList())) {
            List<String> imgList = new ArrayList<>();
            userShare.getImgList().forEach(p -> imgList.add(OSS_IMAGE_HOST + p));
            message.add("user_share_img_list", imgList);
        }
        message.add("user_share_date", DateUtils.dateToString(userShare.getCreateTime(), "MM月dd日 HH:mm"));
        message.add("user_share_user_id", userShare.getUserId());
        message.add("user_share_user_name", userShare.getUserName());
        message.add("is_self_open", userShare.getUserId().equals(currentUserId));
        if (CollectionUtils.isNotEmpty(voteRecordList)) {
            voteRecordList = voteRecordList.stream().sorted((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime())).collect(Collectors.toList());
            message.add("user_share_vote_list", voteRecordList.stream().map(JxtUserVoteRecord::getUserName).collect(Collectors.toList()));
            message.add("user_share_self_vote", voteRecordList.stream().anyMatch(p -> p.getUserId().equals(currentUserId)));
        } else {
            message.add("user_share_vote_list", new ArrayList<>());
            message.add("user_share_self_vote", Boolean.FALSE);
        }
        return message;
    }

    //语音推荐详情
    private Map<String, Object> generateVoiceRecommendMap(VoiceRecommend voiceRecommend, User user, Long sid) {
        Map<String, Object> map = new HashMap<>();
        if (voiceRecommend == null || user == null || CollectionUtils.isEmpty(voiceRecommend.getRecommendVoiceList())) {
            return map;
        }
        UserType userType = user.fetchUserType();
        //评语
        map.put("comment", voiceRecommend.getRecommendComment());
        //语音列表
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (VoiceRecommend.RecommendVoice voice : voiceRecommend.getRecommendVoiceList()) {
            //学生访问时只返回学生自己的语音
            if (UserType.STUDENT == userType && !voice.getStudentId().equals(user.getId())) {
                continue;
            }
            Map<String, Object> voiceMap = new HashMap<>();
            voiceMap.put("student_name", voice.getStudentName());
            voiceMap.put("practice_name", voice.getCategoryName());
            voiceMap.put("voice_list", voice.getVoiceList());
            mapList.add(voiceMap);
        }
        map.put("voice_detail", mapList);
        //生成时间
        map.put("create_time", DateUtils.dateToString(voiceRecommend.getCreateTime(), "yyyy年MM月dd日"));
        //发布人
        Teacher teacher = teacherLoaderClient.loadTeacher(voiceRecommend.getTeacherId());
        String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
        map.put("teacher_name", teacher == null ? "" : subjectName + teacher.fetchRealname() + "老师");
        //点赞列表
        List<JxtUserVoteRecord> voteRecordList = jxtLoaderClient.getVoteRecordByTypeAndId(JxtVoteType.VOICE_RECOMMEND, voiceRecommend.getId());
        if (CollectionUtils.isEmpty(voteRecordList)) {
            map.put("vote_count", 0);
            map.put("vote_user", "");
            map.put("vote_able", Boolean.TRUE);
        }
        voteRecordList = voteRecordList.stream().sorted((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime())).collect(Collectors.toList());
        //点赞数量
        map.put("vote_count", voteRecordList.size());
        //点在用户名称
        map.put("vote_user", StringUtils.join(voteRecordList.stream().map(JxtUserVoteRecord::getUserName).collect(Collectors.toList()), ","));
        //能否点赞
        map.put("vote_able", UserType.PARENT == user.fetchUserType() && !voteRecordList.stream().anyMatch(p -> p.getUserId().equals(user.getId())));
        map.put("user_type", user.fetchUserType().getType());

        map.put("buttonUrl", UrlUtils.buildUrlQuery("/view/mobile/parent/homework/report_detail", MapUtils.m("tab", "clazz", "sid", sid, "hid", voiceRecommend.getId())));
        Student student = studentLoaderClient.loadStudent(sid);
        map.put("user_name", student == null ? "" : student.fetchRealname());
        return map;
    }


    /**
     * 分享优秀录音推荐
     */
    @RequestMapping(value = "share/voicerecommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getShareVoiceRecommend() {
        String id = getRequestString("homeworkId");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("语音推荐Id不能为空");
        }

        try {
            List<VoiceRecommend> voiceRecommendList = voiceRecommendLoaderClient.loadExcludeNoRecommend(Collections.singleton(id));
            if (CollectionUtils.isEmpty(voiceRecommendList)) {
                return MapMessage.errorMessage("语音推荐不存在");
            }
            VoiceRecommend voiceRecommend = voiceRecommendList.get(0);
            return MapMessage.successMessage().add("voice_recommend_info", shareVoiceRecommend(voiceRecommend));
        } catch (Exception ex) {
            logger.error("Failed to share voice recommend, error {}", ex.getMessage());
            return MapMessage.errorMessage("Failed to share voice recommend" + ex.getMessage());
        }
    }

    /**
     * 分享优秀语音推荐详情
     */
    private Map<String, Object> shareVoiceRecommend(VoiceRecommend voiceRecommend) {
        Map<String, Object> map = new HashMap<>();
        if (voiceRecommend == null || CollectionUtils.isEmpty(voiceRecommend.getRecommendVoiceList())) {
            return map;
        }

        //语音列表
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (VoiceRecommend.RecommendVoice voice : voiceRecommend.getRecommendVoiceList()) {
            Map<String, Object> voiceMap = new HashMap<>();
            voiceMap.put("student_name", voice.getStudentName());
            voiceMap.put("practice_name", voice.getCategoryName());
            voiceMap.put("voice_list", voice.getVoiceList());
            mapList.add(voiceMap);
        }
        map.put("voice_detail", mapList);
        //生成时间
        map.put("create_time", DateUtils.dateToString(voiceRecommend.getCreateTime(), "yyyy年MM月dd日"));
        //发布人
        Teacher teacher = teacherLoaderClient.loadTeacher(voiceRecommend.getTeacherId());
        String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
        map.put("teacher_name", teacher == null ? "" : subjectName + teacher.fetchRealname() + "老师");
        //点赞列表
        List<JxtUserVoteRecord> voteRecordList = jxtLoaderClient.getVoteRecordByTypeAndId(JxtVoteType.VOICE_RECOMMEND, voiceRecommend.getId());
        if (CollectionUtils.isEmpty(voteRecordList)) {
            map.put("vote_count", 0);
            map.put("vote_user", "");
            map.put("vote_able", Boolean.TRUE);
        }
        voteRecordList = voteRecordList.stream()
                .sorted(Comparator.comparing(JxtUserVoteRecord::getCreateTime))
                .collect(Collectors.toList());
        //点赞数量
        map.put("vote_count", voteRecordList.size());
        //点赞用户名称
        map.put("vote_user", StringUtils.join(voteRecordList.stream()
                .map(JxtUserVoteRecord::getUserName)
                .collect(Collectors.toList()), ","));
        //评语
        map.put("comment", voiceRecommend.getRecommendComment());
        return map;
    }

    @RequestMapping(value = "filtersensitiveusername.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage filterSensitiveUsername() {
        String userName = getRequestParameter("childName", "").trim();
        if (badWordCheckerClient.containsUserNameBadWord(userName)) {
            return MapMessage.errorMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "brandingpinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String getbrandingCurrentUserInfo() {

        String callbackFunction = getRequestString("jsoncallback");

        User user = currentUser();
        if (user == null) {
            return callbackFunction + "(" + JsonUtils.toJson("请重新登陆") + ")";
        }

        MapMessage result = MapMessage.successMessage();
        result.add("userId", user.getId());
        result.add("userName", user.fetchRealname());
        String retStr = JsonUtils.toJson(result);

        return callbackFunction + "(" + retStr + ")";
    }

    @RequestMapping(value = "pinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCurrentUserInfo() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登陆");
        }

        MapMessage result = MapMessage.successMessage();
        result.add("userId", user.getId());
        result.add("userName", user.fetchRealname());
        result.add("userType", user.fetchUserType().name());
        result.add("userAvatar", getUserAvatarImgUrl(user));
        result.add("headWear", getHeadWear(user.getId()));

        Integer classLevel = null; // 年级字段，默认使用null
        if (user.isStudent()) {
            StudentDetail student = studentLoaderClient.loadStudentDetail(user.getId());
            classLevel = student == null ? null : student.getClazzLevelAsInteger();
        } else if (user.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            result.add("subject", teacherDetail.getSubject());
            result.add("subjects", teacherDetail.getSubjects());
        }
        result.add("classLevel", classLevel);
//        if (user.isStudent()) {
//
//
//        } else if (user.isTeacher()) {
//
//        }pinfo.vpage
        return result;
    }

    /**
     * 获取头饰
     */
    private String getHeadWear(Long userId) {
        if (userId == null || userId <= 0L) return null;

        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(userId);
        if (studentInfo == null) return null;

        Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
        return (headWearPrivilege != null) ? headWearPrivilege.getImg() : null;
    }

    @RequestMapping(value = "/jxtnews/had_recommend.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHadRecommendJxtNewsList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_USER_ERROR_MSG).setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long teacherId;
        Integer clazzLevel = getRequestInt("clazz_level");
        if (user.fetchUserType() == UserType.PARENT) {
            Long groupId = getRequestLong("group_id");
            List<GroupTeacherTuple> tuples = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByGroupId(groupId);
            if (CollectionUtils.isEmpty(tuples)) {
                return MapMessage.errorMessage("您要查看的班级暂无老师").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            teacherId = tuples.get(0).getTeacherId();
            //把子账号转化为主帐号
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if (mainTeacherId != null) {
                teacherId = mainTeacherId;
            }
        } else if (user.fetchUserType() == UserType.TEACHER) {
            teacherId = user.getId();
        } else {
            return MapMessage.errorMessage("暂不支持的用户类型").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        User teacher = raikouSystem.loadUser(teacherId);
        List<JxtNewsTeacherRecommend> teacherRecommends = jxtNewsLoaderClient.getTeacherRecommendListByTeacherIdAndClazzLevel(teacherId, clazzLevel, SchoolYear.newInstance().year()).getUninterruptibly();
        List<String> newsIdList = teacherRecommends.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).map(JxtNewsTeacherRecommend::getRecommendId).collect(Collectors.toList());
        List<JxtNews> jxtNewsList = jxtNewsLoaderClient.getJxtNewsByNewsIds(newsIdList)
                .values()
                .stream()
                .filter(p -> p.getOnline() != null && p.getOnline())
                .sorted((o1, o2) -> newsIdList.indexOf(o1.getId()) - newsIdList.indexOf(o2.getId()))
                .distinct()
                .collect(Collectors.toList());
//        Map<String, Long> readCountMap = asyncNewsCacheService.JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIdList).getUninterruptibly();
        List<Map<String, Object>> mapList = new ArrayList<>();
        jxtNewsList.forEach(news -> {
            Map<String, Object> map = new HashMap<>();
            //阅读数
//            Long readCount = SafeConverter.toLong(readCountMap.get(news.getId()));
            map.put("news_id", news.getId());
            map.put("title", news.getTitle());
            map.put("img_list", news.getCoverImgList());
            map.put("jxt_news_type", news.getJxtNewsType());
            //文章的内容类型
            if (news.getJxtNewsContentType() != null) {
                map.put("jxt_news_content_type", news.getJxtNewsContentType());
            }
            if (news.getJxtNewsType() == JxtNewsType.TEXT) {
                map.put("digest", news.getDigest());
            }
            // 内容样式
            map.put("jxt_news_style_type", news.getJxtNewsStyleType() == null ? JxtNewsStyleType.NEWS : news.getJxtNewsStyleType());
//            map.put("read_count", JxtNewsUtil.countFormat(readCount));
            //更新时间。用作cdn的时间戳
            map.put("update_time", news.getUpdateTime().getTime());
            mapList.add(map);
        });
        return MapMessage.successMessage().add("jxt_news_list", mapList).add("teacher_name", teacher == null ? "" : teacher.fetchRealname());
    }

    @RequestMapping(value = "/jxtnews/had_recommend_clazz_level.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getHadRecommendClazzLevel() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_USER_ERROR_MSG).setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        MapMessage mapMessage = MapMessage.successMessage();
        Set<Long> clazzIds = new HashSet<>();
        if (user.fetchUserType() == UserType.PARENT) {
            //TODO 家长端入口被下掉了。
            //先返回不报错
            Set<Long> sharedGroupIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(sharedGroupIds)) {
                List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadGroups(sharedGroupIds, false).values().stream().sorted((o1, o2) -> o1.getSubject().getKey() - o2.getSubject().getKey()).collect(Collectors.toList());
                Map<Long, GroupMapper> groupMapperMap = groupMappers.stream().collect(Collectors.toMap(GroupMapper::getId, e -> e));
                clazzIds = groupMappers.stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
                //所有老师id
                Map<Long, Long> groupTeacherIdMap = new HashMap<>();
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .findByGroupIds(groupMapperMap.keySet())
                        .forEach(p -> {
                            if (!groupTeacherIdMap.containsKey(p.getGroupId())) {
                                groupTeacherIdMap.put(p.getGroupId(), p.getTeacherId());
                            }
                        });
                //所有账号对应的主帐号
                Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(groupTeacherIdMap.values());
                List<Map<String, Object>> subjectList = new ArrayList<>();
                groupTeacherIdMap.keySet().forEach(groupId -> {
                    Long teacherId = groupTeacherIdMap.get(groupId);
                    GroupMapper groupMapper = groupMapperMap.get(groupId);
                    Long mainTeacherId = mainTeacherIds.get(teacherId);
                    //1、本身是主帐号
                    //2、主帐号不在这个班级里面。这两种情况都要返回
                    if (mainTeacherId == null || Objects.equals(mainTeacherId, teacherId) || !groupTeacherIdMap.values().contains(mainTeacherId)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("subject_name", groupMapper.getSubject().getValue());
                        map.put("group_id", groupMapper.getId());
                        subjectList.add(map);
                    }
                });
                mapMessage.add("subject_list", subjectList);
            }
        } else if (user.fetchUserType() == UserType.TEACHER) {
            //都要支持包班制 老师子账号也要查出来。。。。
            Set<Long> relTeacherIdSet = teacherLoaderClient.loadRelTeacherIds(user.getId());
            clazzIds = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIdSet, false).values().stream().flatMap(Collection::stream).map(GroupMapper::getClazzId).collect(Collectors.toSet());
        } else {
            return MapMessage.errorMessage("暂不支持的用户类型").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        Map<ClazzLevel, List<Clazz>> clazzLevelListMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(p -> !p.isTerminalClazz())
                .collect(Collectors.groupingBy(Clazz::getClazzLevel));
        List<ClazzLevel> clazzLevelList = clazzLevelListMap.keySet().stream().sorted((o1, o2) -> o1.getLevel() - o2.getLevel()).collect(Collectors.toList());
        List<Map<String, Object>> mapList = new ArrayList<>();
        clazzLevelList.forEach(clazzLevel -> {
            Map<String, Object> map = new HashMap<>();
            map.put("clazz_level", clazzLevel.getLevel());
            map.put("level_name", clazzLevel.getDescription());
            mapList.add(map);
        });
        return mapMessage.add("clazz_level_list", mapList);
    }

    @RequestMapping(value = "scholar.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryUserName() {
        Long userId = getRequestLong("scholar");
        User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
        String name = student == null ? "" : student.fetchRealname();
        if (StringUtils.isBlank(name)) name = null;
        return MapMessage.successMessage().add("name", name);

    }

}
