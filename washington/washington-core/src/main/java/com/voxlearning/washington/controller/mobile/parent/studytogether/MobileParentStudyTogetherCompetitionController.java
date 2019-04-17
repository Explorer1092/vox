package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.utopia.service.parent.api.StudyTogetherCompetitionService;
import com.voxlearning.utopia.service.parent.api.activity.StudyTogetherVideoCompetitionActivity;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentCompetitionCacheResult;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudentCompetitionVideo;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.StudyTogetherRankMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 一起学大赛活动
 *
 * @author jiangpeng
 * @since 2018-05-05 下午1:16
 **/
@Controller
@RequestMapping(value = "/parentMobile/study_together/competition")
public class MobileParentStudyTogetherCompetitionController extends AbstractMobileParentController {

    @ImportService(interfaceClass = StudyTogetherCompetitionService.class)
    private StudyTogetherCompetitionService competitionService;

    /**
     * 查询是否参与活动
     *
     * @return 未参与活动返回 successMessage
     */
    @ResponseBody
    @RequestMapping(value = "/checkjoin.vpage", method = RequestMethod.POST)
    public MapMessage checkJoin() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        long studentId = getRequestLong("sid");
        String lessonId = getRequestString("lesson_id");
        if (studentId == 0L || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return competitionService.checkVideo(studentId, lessonId);
    }

    /**
     * 保存上传结果
     *
     * @return 上传成功返回 successMessage
     */
    @ResponseBody
    @RequestMapping(value = "/saveUploadRecord.vpage", method = RequestMethod.POST)
    public MapMessage uploadVideo() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }

        long studentId = getRequestLong("sid");
        String lessonId = getRequestString("lesson_id");
        String desc = getRequestString("desc");
        String relativePath = getRequestString("video_url");
        if (studentId == 0L || StringUtils.isBlank(lessonId) || StringUtils.isBlank(desc) || StringUtils.isBlank(relativePath)) {
            return MapMessage.errorMessage("参数错误");
        }

        Student student = studentLoaderClient.loadStudent(studentId);
        String studentName = student.fetchRealnameIfBlankId();
        try {
            AtomicLockManager.getInstance().wrapAtomic(competitionService)
                    .keyPrefix("saveUploadRecord")
                    .keys(studentId, lessonId)
                    .proxy()
                    .saveStudentCompetitionVideo(studentId, lessonId, desc, relativePath, studentName);
        } catch (Exception e) {
            return MapMessage.errorMessage("正在保存上传结果，请稍后");
        }

        return MapMessage.successMessage("上传成功");
    }


    /**
     * 上传视频成功分享页(need to login in app)
     *
     * @return 参赛视频详情
     */
    @ResponseBody
    @RequestMapping(value = "/successdetails.vpage", method = RequestMethod.POST)
    public MapMessage successDetails() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }

        long studentId = getRequestLong("sid");
        String lessonId = getRequestString("lesson_id");
        if (studentId == 0L || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String id = StudentCompetitionVideo.generateId(studentId, lessonId);
        StudentCompetitionVideo togetherRef = null;
        Map<String, StudentCompetitionVideo> uploadInfoMap = competitionService.findUploadInfo(Collections.singleton(id));
        if (MapUtils.isNotEmpty(uploadInfoMap)) {
            togetherRef = uploadInfoMap.get(id);
        }
        if (togetherRef == null) {
            return MapMessage.errorMessage("没有此用户的视频信息");
        }

        Integer currentLikeNum = competitionService.loadLikeNum(studentId, lessonId);
        Integer diffLikeNum = 0;
        Integer selfRank = 0;
        StudentCompetitionCacheResult studentCompetitionCacheResult = competitionService.getStudentLessonAllRankingInfo(studentId, lessonId);
//        StudentCompetitionCacheResult studentCompetitionCacheResult = JsonUtils.fromJson(rankInfo, StudentCompetitionCacheResult.class);
        if (studentCompetitionCacheResult != null) {
            diffLikeNum = studentCompetitionCacheResult.getPreviousDifference();
            selfRank = studentCompetitionCacheResult.getRank();
        }
        User user = userLoaderClient.loadUser(studentId, UserType.STUDENT);

        return MapMessage.successMessage()
                .add("student_name", getStudentName(user))
                .add("user_img_url", getUserAvatarImgUrl(user))
                .add("desc", togetherRef.getDesc())
                .add("snapshot_url", generateSnapShotUrl(togetherRef.getVideoUrl()))
                .add("video_url", generateVideoUrl(togetherRef.getVideoUrl()))
                .add("status", togetherRef.getStatus())
                .add("current_like_num", currentLikeNum == null ? 0 : currentLikeNum)
                .add("diff_like_num", diffLikeNum)
                .add("self_rank", selfRank);
    }


    /**
     * 点赞数加1
     *
     * @return 成功返回 true
     */
    @ResponseBody
    @RequestMapping(value = "/likenumadd.vpage", method = RequestMethod.POST)
    public MapMessage incrLikeNumTotal() {
        long studentId = getRequestLong("student_id");
        String lessonId = getRequestString("lesson_id");
        if (studentId == 0L || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return competitionService.incrLikeNum(studentId, lessonId);
        } catch (Exception e) {
            logger.error("系统异常");
            return MapMessage.errorMessage();
        }
    }


    /**
     * 微信H5参赛详情页信息(no need to login in app)
     *
     * @return 视频详情
     */

    @RequestMapping(value = "/wechatdetails.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getDetailsInWechat() {
        long studentId = getRequestLong("student_id");
        String lessonId = getRequestString("lesson_id");
        if (studentId == 0L || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("参数错误");
        }
        String id = StudentCompetitionVideo.generateId(studentId, lessonId);
        StudentCompetitionVideo togetherRef = null;
        Map<String, StudentCompetitionVideo> uploadInfoMap = competitionService.findUploadInfo(Collections.singleton(id));
        if (MapUtils.isNotEmpty(uploadInfoMap)) {
            togetherRef = uploadInfoMap.get(id);
        }
        if (togetherRef == null) {
            return MapMessage.errorMessage("没有此用户的视频信息");
        }

        Integer currentLikeNum = competitionService.loadLikeNum(studentId, lessonId);
        Integer diffLikeNum = 0;
        StudentCompetitionCacheResult studentCompetitionCacheResult = competitionService.getStudentLessonAllRankingInfo(studentId, lessonId);
//        StudentCompetitionCacheResult studentCompetitionCacheResult = JsonUtils.fromJson(rankInfo, StudentCompetitionCacheResult.class);
        if (studentCompetitionCacheResult != null) {
            diffLikeNum = studentCompetitionCacheResult.getPreviousDifference();
        }

        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return MapMessage.errorMessage("无此用户信息");
        }

        student.fetchRealnameIfBlankId();
        return MapMessage.successMessage()
                .add("student_name", student.fetchRealnameIfBlankId())
                .add("user_img_url", getUserAvatarImgUrl(student))
                .add("desc", togetherRef.getDesc())
                .add("snapshot_url", generateSnapShotUrl(togetherRef.getVideoUrl()))
                .add("video_url", generateVideoUrl(togetherRef.getVideoUrl()))
                .add("status", togetherRef.getStatus())
                .add("current_like_num", currentLikeNum == null ? 0 : currentLikeNum)
                .add("diff_like_num", diffLikeNum);
    }


    /**
     * 查看排行榜：日榜和总榜
     *
     * @return
     */
    @RequestMapping(value = "/lookRankingList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lookRankingList() {
        boolean isGlobal = getRequestBool("is_global");
        String lessonId = getRequestString("lesson_id");
        long sid = getRequestLong("student_id");
        if (StringUtils.isBlank(lessonId) || sid == 0L) {
            return MapMessage.errorMessage("参数不正确");
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        Map<String, Object> returnMap = new HashMap<>();
        List<StudyTogetherRankMapper.StudyTogetherRank> rankList;
        StudentCompetitionCacheResult studentCompetitionCacheResult;
        Student student = studentLoaderClient.loadStudent(sid);
        //先把自己的上传信息取一下。
        String selfId = StudentCompetitionVideo.generateId(sid, lessonId);
        Map<String, StudentCompetitionVideo> selfInfo = competitionService.findUploadInfo(Collections.singleton(selfId));
        if (isGlobal) {
            rankList = competitionService.getLessonAllRankingInfo(lessonId);
            studentCompetitionCacheResult = competitionService.getStudentLessonAllRankingInfo(sid, lessonId);
        } else {
            rankList = competitionService.getLessonDayRangeRankingInfo(lessonId, DayRange.current());
            studentCompetitionCacheResult = competitionService.getStudentLessonDayRangeRankingInfo(sid, lessonId, DayRange.current());
        }
        if (CollectionUtils.isNotEmpty(rankList)) {
            List<String> ids = rankList.stream().map(e -> StudentCompetitionVideo.generateId(e.getSid(), lessonId)).collect(Collectors.toList());
            //用于批量获取学生信息
            List<Long> sids = rankList.stream().map(StudyTogetherRankMapper.StudyTogetherRank::getSid).collect(Collectors.toList());
            Map<String, StudentCompetitionVideo> uploadInfoMap = competitionService.findUploadInfo(ids);
            Map<Long, Student> studentMap = studentLoaderClient.loadStudents(sids);

            int rankIndex = 1;
            for (StudyTogetherRankMapper.StudyTogetherRank rankInfo : rankList) {
                String id = StudentCompetitionVideo.generateId(rankInfo.getSid(), lessonId);
                Map<String, Object> map = new HashMap<>();
                map.put("rank", rankIndex);
                map.put("sid", rankInfo.getSid());
                map.put("name", MapUtils.isNotEmpty(studentMap) && studentMap.get(rankInfo.getSid()) != null ? studentMap.get(rankInfo.getSid()).fetchRealnameIfBlankId() : "");
                map.put("img", MapUtils.isNotEmpty(studentMap) && studentMap.get(rankInfo.getSid()) != null ? getUserAvatarImgUrl(studentMap.get(rankInfo.getSid())) : "");
                map.put("snapshot_url", MapUtils.isNotEmpty(uploadInfoMap) && uploadInfoMap.get(id) != null ? generateSnapShotUrl(uploadInfoMap.get(id).getVideoUrl()) : "");
                map.put("video_url", MapUtils.isNotEmpty(uploadInfoMap) && uploadInfoMap.get(id) != null ? generateVideoUrl(uploadInfoMap.get(id).getVideoUrl()) : "");
                map.put("like_count", (!isGlobal && rankInfo.getLikeCount().intValue() > 1000) ? 1000 : rankInfo.getLikeCount().intValue());
                returnList.add(map);
                rankIndex++;
            }

        }
        returnMap.put("sid", student != null ? student.getId() : "");
        returnMap.put("name", student != null ? student.fetchRealnameIfBlankId() : "");
        returnMap.put("img", student != null ? getUserAvatarImgUrl(student) : "");
        if (studentCompetitionCacheResult != null) {
            returnMap.put("rank", studentCompetitionCacheResult.getRank());
            returnMap.put("like_count", studentCompetitionCacheResult.getLikeCount());
        }
        if (selfInfo.get(selfId) != null && selfInfo.get(selfId).getCreateDate().before(DayRange.current().getEndDate()) && selfInfo.get(selfId).getCreateDate().after(DayRange.current().getStartDate())) {
            returnMap.put("is_current_day", Boolean.TRUE);
        } else {
            returnMap.put("is_current_day", Boolean.FALSE);
        }

        return MapMessage.successMessage().add("rank_list", returnList).add("self_info", returnMap);
    }


    /**
     * 获奖名单：日榜和总榜
     *
     * @return
     */
    @RequestMapping(value = "/lookRewardList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lookRewardList() {
        boolean isGlobal = getRequestBool("is_global");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程ID不正确");
        }
        DayRange startDay = StudyTogetherVideoCompetitionActivity.startDay;
        DayRange endDay = StudyTogetherVideoCompetitionActivity.endDay;
        DayRange finishDay = StudyTogetherVideoCompetitionActivity.finishDay;
        List<Map<String, Object>> returnList = new ArrayList<>();
        if (isGlobal) {
            if (DayRange.current().getEndDate().compareTo(endDay.getEndDate()) < 0 && DayRange.current().getEndDate().compareTo(startDay.getEndDate()) > 0) {
                return MapMessage.errorMessage("活动还在进行中~");
            }
            List<StudyTogetherRankMapper.StudyTogetherRank> rankList = competitionService.getLessonAllRankingInfo(lessonId);
            int rankIndex = 1;
            List<Long> sids = rankList.stream().map(StudyTogetherRankMapper.StudyTogetherRank::getSid).collect(Collectors.toList());
            Map<Long, Student> studentMap = studentLoaderClient.loadStudents(sids);
            for (StudyTogetherRankMapper.StudyTogetherRank studyTogetherRank : rankList) {
                Map<String, Object> map = new HashMap<>();
                map.put("sid", studyTogetherRank.getSid());
                String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(studyTogetherRank.getSid());
                map.put("mobile", mobile);
                map.put("like_count", studyTogetherRank.getLikeCount().intValue());
                map.put("name", studentMap.get(studyTogetherRank.getSid()) != null ? studentMap.get(studyTogetherRank.getSid()).fetchRealnameIfBlankId() : "");
                map.put("img", studentMap.get(studyTogetherRank.getSid()) != null ? getUserAvatarImgUrl(studentMap.get(studyTogetherRank.getSid())) : "");
                map.put("rank", rankIndex);
                returnList.add(map);
                rankIndex++;
            }

        } else {
            DayRange index = DayRange.current().previous();
            while (index.getEndDate().compareTo(startDay.getEndDate()) >= 0 && index.getEndDate().compareTo(finishDay.getEndDate()) < 0) {
                List<StudyTogetherRankMapper.StudyTogetherRank> list = competitionService.getLessonDayRangeReward(lessonId, index);
                Map<String, Object> map = new HashMap<>();
                try {
                    if (CollectionUtils.isNotEmpty(list)) {
                        map.put("date", DateUtils.dateToString(index.getEndDate(), "MM月dd日"));
                        List<Long> sids = list.stream().map(StudyTogetherRankMapper.StudyTogetherRank::getSid).collect(Collectors.toList());
                        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(sids);
                        List<Map<String, Object>> rankList = new ArrayList<>();
                        for (StudyTogetherRankMapper.StudyTogetherRank rankInfo : list) {
                            if (rankInfo != null) {
                                Map<String, Object> rankMap = new HashMap<>();
                                rankMap.put("sid", rankInfo.getSid());
                                String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(rankInfo.getSid());
                                rankMap.put("mobile", mobile);
                                rankMap.put("like_count", rankInfo.getLikeCount().intValue() > 1000 ? 1000 : rankInfo.getLikeCount().intValue());
                                rankMap.put("name", studentMap.get(rankInfo.getSid()) != null ? studentMap.get(rankInfo.getSid()).fetchRealnameIfBlankId() : "");
                                rankMap.put("img", studentMap.get(rankInfo.getSid()) != null ? getUserAvatarImgUrl(studentMap.get(rankInfo.getSid())) : "");
                                rankList.add(rankMap);
                            }
                        }
                        map.put("data", rankList);
                        returnList.add(map);
                    }

                } catch (Exception e) {
                    logger.warn("competition json error!!", e);
                }
                index = index.previous();
            }
        }

        return MapMessage.successMessage().add("reward_list", returnList);
    }

    //获取视频截帧地址
    private String generateSnapShotUrl(String relativePath) {
        if (StringUtils.isBlank(relativePath)) {
            return "";
        }
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("news-video-content");
        Objects.requireNonNull(config);
        return "https://" + StringUtils.defaultString(config.getBucket())
                + "."
                + StringUtils.defaultString(config.getPublicEndpoint())
                + "/"
                + relativePath;
    }

    //获取视频播放地址
    private String generateVideoUrl(String relativePath) {
        if (StringUtils.isBlank(relativePath)) {
            return "";
        }
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("news-video-content");
        Objects.requireNonNull(config);
        return "https://" + StringUtils.defaultString(config.getHost())
                + "/"
                + relativePath;
    }

}
