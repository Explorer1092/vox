package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.action.api.document.UserGrowth;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.utopia.service.userlevel.api.constant.StudentActivationLevelEnum;
import com.voxlearning.utopia.service.userlevel.api.constant.UserActivationActionEnum;
import com.voxlearning.utopia.service.userlevel.api.constant.UserActivationHomeLevelEnum;
import com.voxlearning.utopia.service.userlevel.api.entity.GrowthSettlementLog;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationLog;
import com.voxlearning.utopia.service.userlevel.api.mapper.*;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.mapper.UserActivationHomeRankMapper;
import com.voxlearning.washington.mapper.UserActivationLogMapper;
import com.voxlearning.washington.mapper.UserActivationRankMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 1/22/18
 */
@RequestMapping(value = "/studentMobile/userlevel")
@Controller
public class MobileStudentUserLevelController extends AbstractMobileController {
    @ImportService(interfaceClass = UserLevelLoader.class)
    private UserLevelLoader userLevelLoader;
    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;
    @Inject
    private ActionLoaderClient actionLoaderClient;

    @RequestMapping(value = "/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage info() {
        Long studentId = currentUserId();
        if (null == studentId) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            UserActivationLevel studentActivationLevel = userLevelLoader.getStudentLevel(studentId);
            if (null == studentActivationLevel) {
                return MapMessage.errorMessage("未查询到等级信息");
            }

            Long lastLevel = userLevelService.updateUserActivationLastLevel(studentId, Long.valueOf(studentActivationLevel.getLevel()));
            if (null == lastLevel) {
                lastLevel = 1L;
            }

            List<UserActivationRankMapper> ranks = getStudentLevelRank(studentId);
            if (CollectionUtils.isEmpty(ranks)) {
                return MapMessage.errorMessage("未查询到学生等级排名");
            }

            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
            UserActivationHomeLevelMapper studentHomeLevel = getStudentHomeLevel(currentStudent(), studentParentRefs);

            Map<Long, Boolean> inUserBlackList = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(currentUser()));

            //结算成长值
            boolean growthSettlement = false;
            GrowthSettlementLog growthSettlementLog = userLevelLoader.getGrowthSettlementLog(studentId);
            if (null == growthSettlementLog) {
                //还没结算过，这次会结算，要弹提示
                growthSettlement = true;
                UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(studentId);
                if (null != userGrowth) {
                    userLevelService.settlementGrowthValue(studentId, Long.valueOf(userGrowth.getGrowthValue()));
                } else {
                    //结算完得到的活跃值是0，不弹提示
                    growthSettlement = false;
                    userLevelService.settlementGrowthValue(studentId, 0L);
                }
            }

            //2018-8-10 0点以后家庭等级下线
            boolean shutDown = userLevelLoader.homeLevelShutDown();

            MapMessage message = MapMessage.successMessage()
                    .add("level", studentActivationLevel.getLevel())
                    .add("levelName", studentActivationLevel.getName())
                    .add("lastLevel", lastLevel)
                    .add("homeLevel", null == studentHomeLevel ? 0 : studentHomeLevel.getLevel())
                    .add("homeLevelShutDown", shutDown)
                    .add("activation", studentActivationLevel.getValue())
                    .add("maxActivation", studentActivationLevel.getLevelEndValue() + 1)
                    .add("minActivation", studentActivationLevel.getLevelStartValue())
                    .add("ranks", ranks)
                    .add("inBlackList", inUserBlackList.getOrDefault(studentId, false))
                    .add("growthSettlement", growthSettlement);

            return message;
        } catch (Exception ex) {
            logger.error("uid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage history() {
        Long studentId = currentUserId();
        if (null == studentId) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            List<UserActivationLog> logs = userLevelLoader.getUserActivationLogIn7Days(studentId);
            if (CollectionUtils.isEmpty(logs)) {
                return MapMessage.successMessage();
            }

            SortedMap<String, List<UserActivationLogMapper>> dateMap = new TreeMap<>(Comparator.reverseOrder());
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (UserActivationLog log : logs) {
                UserActivationActionEnum action = UserActivationActionEnum.of(log.getAction());
                boolean hide = null == action || (action == UserActivationActionEnum.STUDENT_GROWTH_SETTLEMENT && log.getValue() == 0);
                if (hide) {
                    continue;
                }

                LocalDateTime localDateTime = LocalDateTime.ofInstant(log.getCreateDatetime().toInstant(), ZoneId.systemDefault());
                String dateStr = localDateTime.format(dateFormatter);

                if (!dateMap.containsKey(dateStr)) {
                    dateMap.put(dateStr, new ArrayList<>());
                }

                UserActivationLogMapper mapper = new UserActivationLogMapper();
                mapper.setTime(localDateTime.format(timeFormatter));
                mapper.setValue(log.getValue());
                if (MapUtils.isEmpty(log.getExt())) {
                    mapper.setTitle(action.getTitle());
                } else {
                    switch (action) {
                        case STUDENT_FINISH_HOMEWORK:
                            generateFinishHomeworkTitle(log, mapper);
                            break;
                        case STUDENT_REPAIR_HOMEWORK:
                            generateRepairHomeworkTitle(log, mapper);
                            break;
                        case STUDENT_GROWTHWORLD_SUBJECT:
                            generateGrowthWorldSubjectTitle(log, mapper);
                            break;
                        case STUDENT_GOLDEN_MISSION:
                            generateGoldenMissionTitle(log, mapper);
                            break;
                        case STUDENT_PLATINUM_MISSION:
                            generatePlatinumMission(log, mapper);
                            break;
                        default:
                            mapper.setTitle(action.getTitle());
                    }
                }

                dateMap.get(dateStr).add(mapper);
            }
            for (Map.Entry<String, List<UserActivationLogMapper>> entry : dateMap.entrySet()) {
                List<UserActivationLogMapper> sortedMappers = entry.getValue().stream().sorted((m1, m2) -> m2.getTime().compareTo(m1.getTime())).collect(Collectors.toList());
                dateMap.put(entry.getKey(), sortedMappers);
            }
            return MapMessage.successMessage().add("logs", dateMap);
        } catch (Exception ex) {
            logger.error("sid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rank() {
        Long studentId = currentUserId();
        if (null == studentId || 0 == studentId) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            List<UserActivationRankMapper> ranks = getStudentLevelRank(studentId);

            return MapMessage.successMessage()
                    .add("rank", ranks);
        } catch (Exception ex) {
            logger.error("sid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/home/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeInfo() {
        User student = currentUser();
        if (null == student) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(student.getId());

            UserActivationHomeLevelMapper homeLevel = getStudentHomeLevel(student, studentParentRefs);
            if (null == homeLevel) {
                return MapMessage.errorMessage("未查询到家庭等级信息");
            }

            MapMessage message = MapMessage.successMessage();
            Long lastHomeLevel = userLevelService.updateUserActivationLastHomeLevel(student.getId(), Long.valueOf(homeLevel.getLevel()));
            if (null == lastHomeLevel) {
                lastHomeLevel = 1L;
            }

            if (null != homeLevel.getLevel()) {
                message.add("homeLevel", homeLevel.getLevel());
                message.add("homeName", homeLevel.getName());
                message.add("homeLastLevel", lastHomeLevel);
            }

            if (null != homeLevel.getStudentLevel()) {
                message.add("studentName", student.fetchRealname());
                message.add("studentAvatar", getUserAvatarImgUrl(student));
                message.add("studentLevel", homeLevel.getStudentLevel().getLevel());
                message.add("studentActivation", homeLevel.getStudentLevel().getValue());
                message.add("studentMaxActivation", homeLevel.getStudentLevel().getLevelEndValue() + 1);
                message.add("studentMinActivation", homeLevel.getStudentLevel().getLevelStartValue());
            }

            UserActivationLevel parentLevel = null;
            int parentMaxLevel = 0;
            User levelParent = null;
            String parentCallName = "";

            if (MapUtils.isNotEmpty(homeLevel.getParentLevels())) {
                Map<Long, User> parentMap = userLoaderClient.loadUsers(homeLevel.getParentLevels().keySet());
                List<Map<String, Object>> parentLevels = new ArrayList<>();
                for (Map.Entry<Long, UserActivationLevel> entry : homeLevel.getParentLevels().entrySet()) {
                    if (!parentMap.containsKey(entry.getKey())) {
                        continue;
                    }

                    String callName = "";
                    Long parentId = entry.getKey();
                    UserActivationLevel level = entry.getValue();

                    StudentParentRef studentParentRef = studentParentRefs.stream().filter(ref -> ref.getParentId().equals(parentId)).findFirst().orElse(null);
                    if (null == studentParentRef) {
                        continue;
                    }

                    Map<String, Object> info = new HashMap<>();
                    User parent = parentMap.get(parentId);
                    info.put("avatar", getUserAvatarImgUrl(parent));
                    info.put("level", level.getLevel());
                    info.put("activation", level.getValue());
                    callName = StringUtils.isBlank(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName();
                    info.put("callName", callName);
                    parentLevels.add(info);

                    if (level.getLevel() > parentMaxLevel) {
                        parentMaxLevel = level.getLevel();
                        parentLevel = level;
                        levelParent = parent;
                        parentCallName = callName;
                    }
                }

                parentLevels = parentLevels.stream().sorted(new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        int ret = ((Integer) o2.get("level")).compareTo((Integer) o1.get("level"));
                        if (ret == 0) {
                            ret = ((Long) o2.get("activation")).compareTo((Long) o1.get("activation"));
                        }
                        return ret;
                    }
                }).collect(Collectors.toList());

                message.add("parentLevels", parentLevels);
            }

            if (null != parentLevel && null != levelParent) {
                message.add("parentCallName", parentCallName);
                message.add("parentAvatar", getUserAvatarImgUrl(levelParent.fetchImageUrl()));
                message.add("parentLevel", parentLevel.getLevel());
                message.add("parentActivation", parentLevel.getValue());
                message.add("parentMaxActivation", parentLevel.getLevelEndValue() + 1);
                message.add("parentMinActivation", parentLevel.getLevelStartValue());
            }

            Map<Long, Boolean> inUserBlackList = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(currentUser()));
            message.add("studentInBlackList", inUserBlackList.getOrDefault(currentUserId(), false));

            List<UserActivationHomeRankMapper> ranks = getStudentHomeLevelRanks(student.getId());
            if (CollectionUtils.isNotEmpty(ranks)) {
                message.put("ranks", ranks);
            }

            Integer year = currentUser().getProfile().getYear();
            if (null != year && LocalDate.now().getYear() - year > 0) {
                message.add("studentAge", LocalDate.now().getYear() - year);
            }

            //2018-8-10 0点以后家庭等级下线
            boolean shutDown = userLevelLoader.homeLevelShutDown();
            boolean firstPrompt = true;
            if (!shutDown) {
                //要给第一次进入的弹窗提示
                String cacheKeyPrefix = "HOME_LEVEL_SHUT_DOWN_PROMPT_";
                CacheObject<Object> cacheObject = washingtonCacheSystem.CBS.persistence.get(cacheKeyPrefix + currentUserId());
                if (null != cacheObject && null != cacheObject.getValue()) {
                    firstPrompt = false;
                } else {
                    washingtonCacheSystem.CBS.persistence.set(cacheKeyPrefix + currentUserId(), 30 * 24 * 60 * 60, 1);
                }
            }
            message.add("homeLevelShutDown", shutDown)
                    .add("homeLevelShutDownFirstPrompt", firstPrompt);


            return message;
        } catch (Exception ex) {
            logger.error("sid:{}", student.getId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/home/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeRank() {
        Long studentId = currentUserId();
        if (null == studentId || 0 == studentId) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            List<UserActivationHomeRankMapper> ranks = getStudentHomeLevelRanks(studentId);

            if (CollectionUtils.isEmpty(ranks)) {
                return MapMessage.errorMessage("未查询到家庭等级排名");
            }
            return MapMessage.successMessage().add("ranks", ranks);
        } catch (Exception ex) {
            logger.error("sid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    private void generatePlatinumMission(UserActivationLog log, UserActivationLogMapper mapper) {
        if (!log.getExt().containsKey("sbj")) {
            return;
        }
        Subject subject = Subject.of(log.getExt().get("sbj").toString());
        mapper.setTitle("完成一次白金任务-" + subject.getValue() + "的学习");
    }

    private void generateGoldenMissionTitle(UserActivationLog log, UserActivationLogMapper mapper) {
        if (!log.getExt().containsKey("sbj")) {
            return;
        }

        Subject subject = Subject.of(log.getExt().get("sbj").toString());
        mapper.setTitle("完成一次黄金任务-" + subject.getValue() + "的学习");
    }

    private void generateGrowthWorldSubjectTitle(UserActivationLog log, UserActivationLogMapper mapper) {
        if (!log.getExt().containsKey("sbj")) {
            return;
        }
        if (log.getExt().get("sbj").equals("INTELLIGENCE")) {
            mapper.setTitle("完成一个成长世界智慧岛任务");
        } else {
            Subject subject = Subject.of(log.getExt().get("sbj").toString());
            mapper.setTitle("完成一个成长世界" + subject.getValue() + "岛任务");
        }
    }

    private void generateRepairHomeworkTitle(UserActivationLog log, UserActivationLogMapper mapper) {
        if (!log.getExt().containsKey("hid")) {
            return;
        }
        NewHomework newHomework = newHomeworkLoaderClient.load(log.getExt().get("hid").toString());
        if (null != newHomework) {
            mapper.setTitle("补做" + newHomework.getSubject().getValue() + "作业");
        }
    }

    private void generateFinishHomeworkTitle(UserActivationLog log, UserActivationLogMapper mapper) {
        if (!log.getExt().containsKey("hid")) {
            return;
        }
        NewHomework newHomework = newHomeworkLoaderClient.load(log.getExt().get("hid").toString());
        if (null != newHomework) {
            mapper.setTitle("按时完成" + newHomework.getSubject().getValue() + "作业");
        }
    }

    private List<RankContext> getRankContext(List<User> classmates) {
        List<RankContext> infos = new ArrayList<>();
        for (User user : classmates) {
            if (user.fetchRealname().equalsIgnoreCase("体验账号")) {
                continue;
            }
            RankContext context = new RankContext();
            context.setUserId(user.getId());
            context.setRealName(user.fetchRealname());
            infos.add(context);
        }
        return infos;
    }

    private List<UserActivationRankMapper> getStudentLevelRank(Long studentId) {
        List<UserActivationRankMapper> mappers = new ArrayList<>();

        List<UserActivationRank> ranks = userLevelLoader.getStudentLevelRank(studentId, null);
        if (CollectionUtils.isEmpty(ranks)) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (null == studentDetail || null == studentDetail.getClazzId()) {
//                return ranks;
                return mappers;
            }
            List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(studentDetail.getClazzId(), studentId);
            if (CollectionUtils.isEmpty(users)) {
                users = new ArrayList<>();
            }

            List<RankContext> contexts = getRankContext(users);
            ranks = userLevelLoader.getStudentLevelRank(studentId, contexts);
        }
        if (CollectionUtils.isNotEmpty(ranks)) {
            Set<Long> userIds = ranks.stream().map(UserActivationRank::getUserId).collect(Collectors.toSet());
            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

            UserActivationLevel studentLevel = userLevelLoader.getStudentLevel(currentUserId());
            for (UserActivationRank rank : ranks) {
                if (!userMap.containsKey(rank.getUserId())) {
                    continue;
                }
                StudentActivationLevelEnum level = StudentActivationLevelEnum.ofLevel(rank.getLevel());
                if (null == level) {
                    continue;
                }

                UserActivationRankMapper mapper = new UserActivationRankMapper();
                mapper.setUserId(rank.getUserId());
                mapper.setLevel(rank.getLevel());
                mapper.setRank(rank.getRank());
                mapper.setName(userMap.get(rank.getUserId()).fetchRealname());
                mapper.setAvatar(getUserAvatarImgUrl(userMap.get(rank.getUserId())));
                mapper.setLevelName(level.getName());
                mapper.setValue(rank.getValue());
                if (rank.getUserId().equals(currentUserId()) && null != studentLevel) {
                    mapper.setValue(studentLevel.getValue());
                }
                mappers.add(mapper);

               /* rank.setAvatar(getUserAvatarImgUrl(userMap.get(rank.getUserId()).fetchImageUrl()));
                rank.setName(userMap.get(rank.getUserId()).fetchRealname());
                rank.setLevelName(level.getName());

                if (rank.getUserId().equals(currentUserId())) {
                    //更新缓存里当前用户的活跃值
                    rank.setValue(studentLevel.getValue());
                }*/
            }
        }
//        return ranks;
        return mappers;
    }

    private List<UserActivationHomeRankMapper> getStudentHomeLevelRanks(Long studentId) {
        List<UserActivationHomeRankMapper> mappers = new ArrayList<>();

        List<UserActivationHomeRank> ranks = userLevelLoader.getStudentHomeLevelRank(studentId, null);
        if (CollectionUtils.isEmpty(ranks)) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (null == studentDetail || null == studentDetail.getClazzId()) {
                return mappers;
            }
            List<User> users = userAggregationLoaderClient.loadLinkedStudentsByClazzId(studentDetail.getClazzId(), studentId);
            if (CollectionUtils.isEmpty(users)) {
                users = new ArrayList<>();
            }
            List<RankContext> contexts = getRankContext(users);
            ranks = userLevelLoader.getStudentHomeLevelRank(studentId, contexts);
        }

        if (CollectionUtils.isNotEmpty(ranks)) {
            Set<Long> userIds = ranks.stream().map(UserActivationHomeRank::getUserId).collect(Collectors.toSet());
            Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
            for (UserActivationHomeRank rank : ranks) {
                if (!userMap.containsKey(rank.getUserId())) {
                    continue;
                }
                UserActivationHomeLevelEnum homeLevel = UserActivationHomeLevelEnum.ofLevel(rank.getLevel());
                if (null == homeLevel) {
                    continue;
                }

                UserActivationHomeRankMapper mapper = new UserActivationHomeRankMapper();
                mapper.setUserId(rank.getUserId());
                mapper.setLevel(rank.getLevel());
                mapper.setRank(rank.getRank());
                mapper.setAvatar(getUserAvatarImgUrl(userMap.get(rank.getUserId())));
                mapper.setName(userMap.get(rank.getUserId()).fetchRealname());
                mapper.setLevelName(homeLevel.getName());
                mappers.add(mapper);
                /*
                rank.setAvatar(getUserAvatarImgUrl(userMap.get(rank.getUserId()).fetchImageUrl()));
                rank.setName(userMap.get(rank.getUserId()).fetchRealname());
                rank.setLevelName(homeLevel.getName());
*/
            }
        }
        return mappers;
    }

    private UserActivationHomeLevelMapper getStudentHomeLevel(User student, List<StudentParentRef> studentParentRefs) {
        UserActivationHomeLevelMapper homeLevel = userLevelLoader.getStudentHomeLevelStatistics(student.getId(), null);
        if (null == homeLevel) {
            Set<Long> parentIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
            }
            homeLevel = userLevelLoader.getStudentHomeLevelStatistics(student.getId(), parentIds);
        }
        return homeLevel;
    }

}
