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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNoticeType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtFeedBack;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNotice;
import com.voxlearning.washington.controller.mobile.AbstractMobileJxtController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 老师端发布家校通消息相关
 * Created by Shuai Huan on 2016/4/21.
 */
@Controller
@RequestMapping(value = "/teacherMobile/jxt/notice/")
@Slf4j
public class MobileTeacherJxtNoticeController extends AbstractMobileJxtController {

    @Inject private RaikouSDK raikouSDK;

    /**
     * 通知入口页。返回班级列表
     */
    @RequestMapping(value = "publish.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage publishNotice() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        return generateTeacherGroups(teacher.getId());
    }

    /**
     * 保存通知
     */
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNotice() {
        String groupIdStr = getRequestString("group_ids");
        String content = getRequestString("content");
        String imgListStr = getRequestString("img_url_list");
        String voiceURL = getRequestString("voice_url");
        String endTimeStr = getRequestString("end_time");
        Boolean autoRemind = getRequestBool("auto_remind");
        Boolean needFeedBack = getRequestBool("need_feed_back");
        // 过滤敏感词
        if (StringUtils.isNoneBlank(content) && badWordCheckerClient.containsConversationBadWord(content)) {
            return MapMessage.errorMessage("您可能输入了不合适内容，请修改提交~");
        }
        MapMessage validateMessage = validateNotice(content, groupIdStr, imgListStr, voiceURL, endTimeStr);
        if (!validateMessage.isSuccess()) {
            return MapMessage.errorMessage(validateMessage.getInfo());
        }
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        List<Long> groupIdList = JsonUtils.fromJsonToList(groupIdStr, Long.class);
        Set<Long> groupIds = new HashSet<>(groupIdList);
        //处理包班制
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Set<Long> teacherGroupIds = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIds, false).values()
                .stream()
                .flatMap(Collection::stream)
                .filter(p -> {
                    for (Long tid : relTeacherIds) {
                        if (p.isTeacherGroupRefStatusValid(tid))
                            return true;
                    }
                    return false;
                })
                .map(GroupTeacherMapper::getId)
                .collect(Collectors.toSet());
        groupIds = groupIds.stream().filter(teacherGroupIds::contains).collect(Collectors.toSet());
        //全部组都默认处理为未发送过一键提醒
        Map<Long, Boolean> teacherRemindMap = new HashMap<>();
        groupIds.forEach(p -> teacherRemindMap.put(p, Boolean.FALSE));
        //把图片地址处理成相对地址
        List<String> imgList = new ArrayList<>();
        if (StringUtils.isNotBlank(imgListStr)) {
            JsonUtils.fromJsonToList(imgListStr, String.class).forEach(p -> imgList.add(p.substring(OSS_IMAGE_HOST.length())));
        }
        JxtNotice notice = new JxtNotice();
        notice.setTeacherId(teacher.getId());
        notice.setNoticeType(JxtNoticeType.ClAZZ_AFFAIR.getType());
        notice.setGroupIds(groupIds);
        notice.setContent(content);
        notice.setImgUrl(imgList);
        notice.setVoiceUrl(StringUtils.isBlank(voiceURL) ? "" : voiceURL.substring(OSS_HOST.length()));
        notice.setAutoRemind(autoRemind);
        notice.setHadTeacherRemindMap(teacherRemindMap);
        notice.setNeedFeedBack(needFeedBack);
        notice.setExpireTime(DateUtils.stringToDate(endTimeStr, "yyyy-MM-dd HH"));
        MapMessage message = jxtServiceClient.saveJxtNotice(notice);
        if (message.isSuccess()) {
            //这里才是取所有的学科
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
            String noticeId = SafeConverter.toString(message.get("notice_id"));
            String pushContent = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + SafeConverter.toString(message.get("notice_content"));
            String url = "/view/mobile/common/notice_detail?notice_id=" + noticeId + "&group_id=";
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("s", ParentAppPushType.JXT_NOTICE.name());
            //新的极光push
            groupIds.forEach(p -> {
                jpushExtInfo.put("url", url + p);
                appMessageServiceClient.sendAppJpushMessageByTags(pushContent,
                        AppMessageSource.PARENT,
                        Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))),
                        null,
                        jpushExtInfo);
            });
        }

        return message;
    }

    /**
     * 通知列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherNoticeList() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        //包班制要查所有子账号的通知
        Set<Long> relTeacherIdSet = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        List<JxtNotice> noticeList = jxtLoaderClient.getJxtNoticeListByTeacherIds(relTeacherIdSet).values().stream().flatMap(Collection::stream).filter(p -> p.getExpireTime() != null).collect(Collectors.toList());
        List<Map<String, Object>> mapList = generateNoticeList(noticeList, teacher.getId());
        return MapMessage.successMessage().add("notice_list", mapList);
    }


    /**
     * 老师发送一键提醒
     */
    @RequestMapping(value = "remind.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherRemind() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String noticeId = getRequestString("notice_id");
        String groupIdStr = getRequestString("group_ids");
        JxtNotice jxtNotice = jxtLoaderClient.getJxtNoticeById(noticeId);
        if (jxtNotice == null) {
            return MapMessage.errorMessage("您要查看的通知不存在");
        }
        if (StringUtils.isBlank(groupIdStr)) {
            return MapMessage.errorMessage("提醒班级不能为空");
        }
        if (!jxtNotice.getTeacherId().equals(teacher.getId())) {
            return MapMessage.errorMessage("该通知不属于您，不能发送一键提醒");
        }
        Set<Long> needRemindGroupIds = new HashSet<>(JsonUtils.fromJsonToList(groupIdStr, Long.class));
        if (!needRemindGroupIds.stream().anyMatch(p -> jxtNotice.getHadTeacherRemindMap().get(p) == Boolean.FALSE)) {
            return MapMessage.errorMessage("已向该班级发送过一键提醒");
        }
        //这里得过滤掉needRemindGroupIds中由于换班引起的已经不在当前老师明显的group
        //老师名下的group
        List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacher.getId());
        if (CollectionUtils.isEmpty(groupTeacherRefs)) {
            return MapMessage.errorMessage("该老师名下已经没有任何班级");
        }
        Set<Long> teacherGroupIds = groupTeacherRefs.stream().map(GroupTeacherTuple::getGroupId).collect(Collectors.toSet());
        //过滤出目前老师名下的需要发提醒的班级
        needRemindGroupIds = needRemindGroupIds.stream().filter(teacherGroupIds::contains).collect(Collectors.toSet());
        MapMessage message = jxtServiceClient.updateJxtNoticeRemindTrue(jxtNotice, needRemindGroupIds);
        if (!message.isSuccess()) {
            return message;
        }
        //新的极光push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("s", ParentAppPushType.JXT_NOTICE.name());
        jpushExtInfo.put("url", "");
        List<String> groupTags = new LinkedList<>();
        needRemindGroupIds.forEach(p -> groupTags.add(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(p))));
        Long teacherId = teacher.getId();
        //这里只是取发送人的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
        String push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + "提醒家长们查看最新通知！";
        appMessageServiceClient.sendAppJpushMessageByTags(push_title,
                AppMessageSource.PARENT,
                groupTags,
                null,
                jpushExtInfo);
        return message;
    }

    @RequestMapping(value = "re_edit.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNoticeInfoForReSave() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String noticeId = getRequestString("notice_id");
        JxtNotice jxtNotice = jxtLoaderClient.getJxtNoticeById(noticeId);
        if (jxtNotice == null) {
            return MapMessage.errorMessage("您要查看的通知不存在");
        }
        if (jxtNotice.getNoticeType() != JxtNoticeType.ClAZZ_AFFAIR.getType()) {
            return MapMessage.errorMessage("作业单不能重发");
        }
        Map<String, Object> map = generateNoticeInfoForReSave(jxtNotice);
        return MapMessage.successMessage().add("notice_info", map);
    }


    //生成老师班级信息
    private MapMessage generateTeacherGroups(Long teacherId) {
        //取老师所有主副账号的group
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<GroupTeacherMapper> teacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIds, false).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //clazz-group对应map
        //支持包班制
        Map<Long, List<GroupTeacherMapper>> clazzGroupListMap = teacherMappers.stream()
                .filter(p -> {
                    for (Long tid : relTeacherIds) {
                        if (p.isTeacherGroupRefStatusValid(tid))
                            return true;
                    }
                    return false;
                })
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.groupingBy(GroupTeacherMapper::getClazzId));
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzGroupListMap.keySet())
                .stream()
                .filter(p -> !p.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (MapUtils.isEmpty(clazzGroupListMap) || CollectionUtils.isEmpty(clazzList)) {
            return MapMessage.errorMessage("老师班级为空!");
        }
        Boolean hasSlave = teacherHasSlave(teacherId);
        Map<Long, List<Subject>> clazzId2SubjectListMap = hasSlave ? teacherLoaderClient.findTeacherAllSubjectInClazzs(clazzList.stream().map(Clazz::getId).collect(Collectors.toList()), teacherId) : Collections.emptyMap();
        clazzList.forEach(clazz -> {
            Long clazzId = clazz.getId();
            Long groupId = clazzGroupListMap.get(clazzId) == null ? null : clazzGroupListMap.get(clazzId).get(0).getId();
            if (groupId != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("group_id", groupId);
                map.put("clazz_name", clazz.formalizeClazzName() + toSubjectListStr(clazzId2SubjectListMap.get(clazzId)));
                mapList.add(map);
            }
        });
        return MapMessage.successMessage().add("clazz_list", mapList);
    }


    //校验通知属性
    private MapMessage validateNotice(String content, String groupIdStr, String imgListStr, String voiceUrl, String endTimeStr) {
        List<Long> groupIds = JsonUtils.fromJsonToList(groupIdStr, Long.class);
        if (CollectionUtils.isEmpty(groupIds)) {
            return MapMessage.errorMessage("请选择通知发送的班级");
        }
        if (StringUtils.isBlank(content) && StringUtils.isBlank(voiceUrl)) {
            List<String> imgList = JsonUtils.fromJsonToList(imgListStr, String.class);
            if (CollectionUtils.isEmpty(imgList)) {
                return MapMessage.errorMessage("请输入通知内容");
            }

        }
        if (StringUtils.isBlank(endTimeStr)) {
            return MapMessage.errorMessage("请输入截止时间");
        }
        return MapMessage.successMessage();
    }

    //生成通知列表
    private List<Map<String, Object>> generateNoticeList(List<JxtNotice> noticeList, Long teacherId) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(noticeList)) {
            return mapList;
        }
        noticeList = noticeList.stream().filter(p -> p.getExpireTime() != null && p.getNoticeType() != null).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        //分成两部分通知处理
        //班务通知和作业单通知
        List<JxtNotice> offlineHomeworkNoticeList = noticeList.stream().filter(p -> JxtNoticeType.OFFLINE_HOMEWORK.getType() == p.getNoticeType()).collect(Collectors.toList());
        List<JxtNotice> clazzAffairNoticeList = noticeList.stream().filter(p -> JxtNoticeType.ClAZZ_AFFAIR.getType() == p.getNoticeType()).collect(Collectors.toList());
        //班务通知
        List<Map<String, Object>> infoForClazzAffair = generateNoticeInfoForClazzAffair(clazzAffairNoticeList, teacherId);
        if (CollectionUtils.isNotEmpty(infoForClazzAffair)) {
            mapList.addAll(infoForClazzAffair);
        }
        //作业单
        List<Map<String, Object>> infoForOfflineHomework = generateNoticeInfoForOfflineHomework(offlineHomeworkNoticeList, teacherId);
        if (CollectionUtils.isNotEmpty(infoForOfflineHomework)) {
            mapList.addAll(infoForOfflineHomework);
        }
        //排序
        mapList.sort(((o1, o2) -> Long.compare(SafeConverter.toLong(o2.get("create_time")), SafeConverter.toLong(o1.get("create_time")))));
        return mapList;
    }

    private static Map<Subject, String> subjectSimpleNameMap = new HashMap<>();

    static {
        subjectSimpleNameMap.put(Subject.CHINESE, "语");
        subjectSimpleNameMap.put(Subject.MATH, "数");
        subjectSimpleNameMap.put(Subject.ENGLISH, "英");
    }

    private String toSubjectListStr(List<Subject> subjects) {
        if (CollectionUtils.isEmpty(subjects))
            return "";
        subjects = subjects.stream().sorted((o1, o2) -> Integer.compare(o2.getKey(), o1.getKey())).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        subjects.forEach(subject -> {
            sb.append(" ");
            sb.append(subjectSimpleNameMap.get(subject));
        });
        return sb.deleteCharAt(0).insert(0, "(").append(")").toString();
    }


    //专门针对重新发送时使用的
    private Map<String, Object> generateNoticeInfoForReSave(JxtNotice jxtNotice) {
        Map<String, Object> map = new HashMap<>();
        //过滤过期时间为空的异常数据
        if (jxtNotice == null || jxtNotice.getExpireTime() == null) {
            return map;
        }
        if (JxtNoticeType.UNKNOWN == JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType())) {
            return map;
        }
        map.put("selected_clazz", jxtNotice.getGroupIds());
        map.put("end_time", jxtNotice.getExpireTime().getTime());
        //通知文本内容
        map.put("content", jxtNotice.getContent());
        //图片
        List<String> imgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jxtNotice.getImgUrl())) {
            jxtNotice.getImgUrl().forEach(p -> imgList.add(OSS_IMAGE_HOST + p));
        }
        map.put("pictures", imgList);
        //语音
        map.put("records", StringUtils.isBlank(jxtNotice.getVoiceUrl()) ? new ArrayList<>() : Collections.singletonList(OSS_HOST + jxtNotice.getVoiceUrl()));
        //通知是否需要反馈
        map.put("need_feed_back", jxtNotice.getNeedFeedBack());
        //家长提醒
        map.put("auto_remind", jxtNotice.getAutoRemind());
        //标识为重发====前端在通知文本前加上“[更新]”
        map.put("is_edit", true);
        //要改这个"[更新]"的文案一定要去ParentJxtApiController修改top_notice.vpage接口的返回值RES_RESULT_TOP_NOTICE_CONTENT的。
        map.put("append_content", "[更新]");
        return map;
    }

    //将班务通知处理成前台数据
    private List<Map<String, Object>> generateNoticeInfoForClazzAffair(List<JxtNotice> clazzAffairNoticeList, Long teacherId) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(clazzAffairNoticeList)) {
            return mapList;
        }
        Set<Long> groupIds = new HashSet<>();
        clazzAffairNoticeList.forEach(p -> groupIds.addAll(p.getGroupIds()));
        //组map
        Map<Long, GroupMapper> groupMaps = deprecatedGroupLoaderClient.loadGroups(groupIds, true);
        //班级map
        Map<Long, Clazz> clazzMaps = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupMaps.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        //包班制老师的列表里的班级名要加上学科
        Boolean hasSlave = teacherHasSlave(teacherId);
        Map<Long, List<Subject>> clazz2SubjectListMap = hasSlave ? teacherLoaderClient.findTeacherAllSubjectInClazzs(clazzMaps.keySet(), teacherId) : Collections.emptyMap();
        //通知的反馈map
        Map<String, List<JxtFeedBack>> feedBackMaps = jxtLoaderClient.getFeedBackListByNoticeIds(clazzAffairNoticeList.stream().map(JxtNotice::getId).collect(Collectors.toSet()));
        for (JxtNotice jxtNotice : clazzAffairNoticeList) {
            Map<String, Object> map = new HashMap<>();
            if (JxtNoticeType.ClAZZ_AFFAIR == JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType())) {
                map.put("notice_id", jxtNotice.getId());
                //通知类型
                map.put("notice_type", JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType()).getName());
                //通知创建时间
                map.put("notice_create_date", DateUtils.dateToString(jxtNotice.getCreateTime(), "M月dd日 HH:mm"));
                //通知截止时间
                map.put("notice_end_date", DateUtils.dateToString(jxtNotice.getExpireTime(), "M月dd日 HH:mm"));
                //是否已过期
                map.put("notice_date_status", jxtNotice.getExpireTime().before(new Date()) ? "已过期" : "");
                //通知文本内容
                String img = CollectionUtils.isEmpty(jxtNotice.getImgUrl()) ? "" : "[图片]";
                String voice = StringUtils.isBlank(jxtNotice.getVoiceUrl()) ? "" : "[语音]";
                map.put("notice_content", img + voice + jxtNotice.getContent());
                //通知是否需要反馈
                map.put("need_feed_back", jxtNotice.getNeedFeedBack());

                List<Map<String, Object>> clazzFeedBackList = new ArrayList<>();
                Set<Long> noticeGroupIds = jxtNotice.getGroupIds();
                //key是clazzId value是groupId
                Map<Long, Long> clazzGroupMap = groupMaps.values()
                        .stream()
                        .filter(p -> noticeGroupIds.contains(p.getId()))
                        .collect(Collectors.toMap(GroupMapper::getClazzId, GroupMapper::getId));
                //排序完的通知对应的班级列表
                List<Clazz> noticeClazzList = clazzMaps.values()
                        .stream()
                        .filter(p -> clazzGroupMap.keySet().contains(p.getId()))
                        .sorted(new Clazz.ClazzLevelAndNameComparator())
                        .collect(Collectors.toList());
                //统计该条通知所有group的未反馈总数
                Long totalNotFeedBackCount = 0L;
                for (Clazz clazz : noticeClazzList) {
                    Map<String, Object> feedBack = new HashMap<>();
                    //班级名称
                    feedBack.put("clazz_name", clazz.formalizeClazzName() + toSubjectListStr(clazz2SubjectListMap.get(clazz.getId())));
                    feedBack.put("group_id", clazzGroupMap.get(clazz.getId()));
                    //已反馈人数/聊天组总人数
                    int feedBackStudentCount = feedBackMaps.containsKey(jxtNotice.getId()) ? feedBackMaps.get(jxtNotice.getId()).stream().filter(p -> p.getGroupId().equals(clazzGroupMap.get(clazz.getId()))).map(JxtFeedBack::getStudentId).collect(Collectors.toSet()).size() : 0;
                    int studentTotalCount = groupMaps.get(clazzGroupMap.get(clazz.getId())).getStudents().size();
                    totalNotFeedBackCount += studentTotalCount - feedBackStudentCount;
                    if (feedBackStudentCount <= studentTotalCount) {
                        feedBack.put("feed_back_count", feedBackStudentCount + "/" + studentTotalCount);
                    } else {
                        feedBack.put("feed_back_count", feedBackStudentCount);
                    }
                    clazzFeedBackList.add(feedBack);
                }
                //班级反馈统计列表
                map.put("notice_feed_back_clazz_list", clazzFeedBackList);
                //通知到期天数和未反馈家长总数==通知列表页才需要
                Long expire = DateUtils.dayDiff(jxtNotice.getExpireTime(), new Date());
                //未过期，且过期时间在24小时内
                if (jxtNotice.getExpireTime().after(new Date()) && expire == 0 && totalNotFeedBackCount > 0) {
                    map.put("notice_date_expire", String.format("1天内到期，还有%d位家长未响应", totalNotFeedBackCount));
                    map.put("can_send_remind", jxtNotice.getHadTeacherRemindMap().values().stream().anyMatch(p -> Boolean.FALSE == p));
                }
                //放个时间的参数。两个list合并后做排序
                map.put("create_time", jxtNotice.getCreateTime().getTime());
                mapList.add(map);
            }
        }
        return mapList;
    }

    private List<Map<String, Object>> generateNoticeInfoForOfflineHomework(List<JxtNotice> offlineHomeworkNoticeList, Long teacherId) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        offlineHomeworkNoticeList = offlineHomeworkNoticeList.stream().filter(p -> MapUtils.isNotEmpty(p.getGroupOfflineHomeworkIdMap())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(offlineHomeworkNoticeList)) {
            return mapList;
        }
        Set<Long> groupIds = new HashSet<>();
        offlineHomeworkNoticeList.forEach(p -> groupIds.addAll(p.getGroupOfflineHomeworkIdMap().keySet()));
        //组map
        Map<Long, GroupMapper> groupMaps = deprecatedGroupLoaderClient.loadGroups(groupIds, false);
        //班级map
        Map<Long, Clazz> clazzMaps = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupMaps.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        //包班制老师的列表里的班级名要加上学科
        Boolean hasSlave = teacherHasSlave(teacherId);
        Map<Long, List<Subject>> clazz2SubjectListMap = hasSlave ? teacherLoaderClient.findTeacherAllSubjectInClazzs(clazzMaps.keySet(), teacherId) : Collections.emptyMap();
        for (JxtNotice jxtNotice : offlineHomeworkNoticeList) {
            Map<String, Object> map = new HashMap<>();
            if (JxtNoticeType.OFFLINE_HOMEWORK == JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType())) {
                map.put("notice_id", jxtNotice.getId());
                //通知类型
                map.put("notice_type", JxtNoticeType.ofWithUnKnow(jxtNotice.getNoticeType()).getName());
                //通知创建时间
                map.put("notice_create_date", DateUtils.dateToString(jxtNotice.getCreateTime(), "M月dd日 HH:mm"));
                //通知截止时间
                map.put("notice_end_date", DateUtils.dateToString(jxtNotice.getExpireTime(), "M月dd日 HH:mm"));
                //是否已过期
                map.put("notice_date_status", jxtNotice.getExpireTime().before(new Date()) ? "已过期" : "");
                //通知文本内容
                String img = CollectionUtils.isEmpty(jxtNotice.getImgUrl()) ? "" : "[图片]";
                String voice = StringUtils.isBlank(jxtNotice.getVoiceUrl()) ? "" : "[语音]";
                map.put("notice_content", img + voice + jxtNotice.getContent());

                //这里的groupId 取groupOfflineHomeworkIdMap的Key
                Set<Long> noticeGroupIds = jxtNotice.getGroupOfflineHomeworkIdMap().keySet();
                //key是clazzId value是groupId
                Map<Long, Long> clazzGroupMap = groupMaps.values()
                        .stream()
                        .filter(p -> noticeGroupIds.contains(p.getId()))
                        .collect(Collectors.toMap(GroupMapper::getClazzId, GroupMapper::getId));
                //排序完的通知对应的班级列表
                List<Clazz> noticeClazzList = clazzMaps.values()
                        .stream()
                        .filter(p -> clazzGroupMap.keySet().contains(p.getId()))
                        .sorted(new Clazz.ClazzLevelAndNameComparator())
                        .collect(Collectors.toList());
                //通知关联的班级列表
                List<Map<String, Object>> clazzFeedBackList = new ArrayList<>();
                for (Clazz clazz : noticeClazzList) {
                    Map<String, Object> feedBack = new HashMap<>();
                    //班级名称
                    feedBack.put("clazz_name", clazz.formalizeClazzName() + toSubjectListStr(clazz2SubjectListMap.get(clazz.getId())));
                    feedBack.put("group_id", clazzGroupMap.get(clazz.getId()));
                    feedBack.put("offline_homework_id", jxtNotice.getGroupOfflineHomeworkIdMap().get(clazzGroupMap.get(clazz.getId())));
                    clazzFeedBackList.add(feedBack);
                }
                //作业单班级列表
                map.put("notice_feed_back_clazz_list", clazzFeedBackList);
                //放个时间的参数。两个list合并后做排序
                map.put("create_time", jxtNotice.getCreateTime().getTime());
                mapList.add(map);
            }
        }
        return mapList;
    }
}
