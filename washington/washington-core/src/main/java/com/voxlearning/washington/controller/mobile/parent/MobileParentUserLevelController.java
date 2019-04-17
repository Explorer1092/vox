package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.utopia.service.userlevel.api.constant.UserActivationActionEnum;
import com.voxlearning.utopia.service.userlevel.api.constant.UserActivationHomeLevelEnum;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationLog;
import com.voxlearning.utopia.service.userlevel.api.mapper.*;
import com.voxlearning.washington.mapper.UserActivationHomeRankMapper;
import com.voxlearning.washington.mapper.UserActivationLogMapper;
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
 * @since 12/15/17
 */
@Controller
@RequestMapping(value = "/parentMobile/userlevel")
public class MobileParentUserLevelController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = UserLevelLoader.class)
    private UserLevelLoader userLevelLoader;
    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;

    @RequestMapping(value = "/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage info() {
        Long studentId = getRequestLong("sid");
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        User parent = currentParent();
        if (null == parent) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            UserActivationLevel parentActivationLevel = userLevelLoader.getParentLevel(parent.getId());
            if (null == parentActivationLevel) {
                return MapMessage.errorMessage("未查询到家长等级信息");
            }

            List<User> students = studentLoaderClient.loadStudentClassmates(studentId);
            int count = 0;
            Set<String> parentCallNames = new HashSet<>();

            if (CollectionUtils.isNotEmpty(students)) {
                Set<Long> classmatesIds = students.stream().map(User::getId).collect(Collectors.toSet());

                UserLevelClassStatistics parentActivationStatistics = userLevelLoader.getClassStatisticsForParent(parent.getId(), studentId, parentActivationLevel.getLevel(), null);
                if (null == parentActivationStatistics) {
                    //没有统计信息的缓存，构造一份
                    Map<Long, List<StudentParent>> parentMap = parentLoaderClient.loadStudentParents(classmatesIds);
                    if (MapUtils.isNotEmpty(parentMap)) {
                        Set<Long> parentIds = new HashSet<>();
                        parentMap.values().forEach(lst -> lst.forEach(sp -> {
                            if (!Objects.equals(sp.getParentUser().getId(), currentUserId())) {
                                parentIds.add(sp.getParentUser().getId());
                            }
                        }));

                        if (CollectionUtils.isNotEmpty(parentIds)) {
                            parentActivationStatistics = userLevelLoader.getClassStatisticsForParent(parent.getId(), studentId, parentActivationLevel.getLevel(), parentIds);
                        }
                    }
                }

                if (null != parentActivationStatistics) {
                    count = parentActivationStatistics.getCount();

                    //最多取两位家长显示
                    Set<Long> pids = new HashSet<>();
                    for (int i = parentActivationStatistics.getUserIds().size() - 1; i >= 0; i--) {
                        pids.add(parentActivationStatistics.getUserIds().get(i));

                        if (pids.size() >= 2) {
                            break;
                        }
                    }

                    //补充家长的callName显示
                    if (pids.size() > 0) {
                        Map<Long, List<StudentParentRef>> refMap = parentLoaderClient.loadParentStudentRefs(pids);
                        if (MapUtils.isNotEmpty(refMap)) {
                            for (Map.Entry<Long, List<StudentParentRef>> entry : refMap.entrySet()) {
                                if (CollectionUtils.isEmpty(entry.getValue())) {
                                    continue;
                                }
                                for (StudentParentRef ref : entry.getValue()) {
                                    if (classmatesIds.contains(ref.getStudentId())) {
                                        User user = raikouSystem.loadUser(ref.getStudentId());
                                        if (null != user) {
                                            parentCallNames.add(user.fetchRealnameIfBlankId() + (StringUtils.isBlank(ref.getCallName()) ? "家长" : ref.getCallName()));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Long lastLevel = userLevelService.updateUserActivationLastLevel(parent.getId(), Long.valueOf(parentActivationLevel.getLevel()));
            if (null == lastLevel) {
                lastLevel = 1L;
            }

            MapMessage message = MapMessage.successMessage()
                    .add("level", parentActivationLevel.getLevel())
                    .add("lastLevel", lastLevel)
                    .add("maxActivation", parentActivationLevel.getLevelEndValue() + 1)
                    .add("minActivation", parentActivationLevel.getLevelStartValue())
                    .add("activation", parentActivationLevel.getValue())
                    .add("count", count)
                    .add("callNames", parentCallNames);

            return message;
        } catch (Exception ex) {
            logger.error("uid:{}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage history() {
        User parent = currentParent();
        if (null == parent) {
            return MapMessage.errorMessage("未登录");
        }

        try {
            List<UserActivationLog> logs = userLevelLoader.getUserActivationLogIn7Days(parent.getId());
            if (CollectionUtils.isEmpty(logs)) {
                return MapMessage.successMessage();
            }

            SortedMap<String, List<UserActivationLogMapper>> dateMap = new TreeMap<>(Comparator.reverseOrder());
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (UserActivationLog log : logs) {
                UserActivationActionEnum action = UserActivationActionEnum.of(log.getAction());
                if (null == action) {
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
                mapper.setCreateDatetime(log.getCreateDatetime());
                if (action == UserActivationActionEnum.PARENT_PAYMENT) {
                    if (MapUtils.isNotEmpty(log.getExt()) && log.getExt().containsKey("oid")) {
                        UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(log.getExt().get("oid").toString());
                        if (null != userOrder) {
                            mapper.setTitle("为孩子开通" + userOrder.getProductName());
                        }
                    }
                } else if (action == UserActivationActionEnum.PARENT_PAYMENT_REFUND) {
                    if (MapUtils.isNotEmpty(log.getExt()) && log.getExt().containsKey("oid")) {
                        UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(log.getExt().get("oid").toString());
                        if (null != userOrder) {
                            mapper.setTitle(userOrder.getProductName() + "退款");
                        }
                    }
                }
                if (StringUtils.isBlank(mapper.getTitle())) {
                    mapper.setTitle(action.getTitle());
                }
                if (StringUtils.isNotBlank(mapper.getTitle())) {
                    mapper.setTitle(mapper.getTitle());
                }
                dateMap.get(dateStr).add(mapper);
            }
            for (Map.Entry<String, List<UserActivationLogMapper>> entry : dateMap.entrySet()) {
                List<UserActivationLogMapper> sortedMappers = entry.getValue().stream().sorted((m1, m2) -> m2.getCreateDatetime().compareTo(m1.getCreateDatetime())).collect(Collectors.toList());
                dateMap.put(entry.getKey(), sortedMappers);
            }
            return MapMessage.successMessage().add("logs", dateMap);
        } catch (Exception ex) {
            logger.error("pid:{}", currentUserId(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }


    @RequestMapping(value = "/home/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeInfo() {
        Long studentId = getRequestLong("sid");
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            User student = raikouSystem.loadUser(studentId);
            if (null == student) {
                return MapMessage.errorMessage("未查询到学生信息");
            }

            List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(student.getId());

            UserActivationHomeLevelMapper homeLevel = userLevelLoader.getStudentHomeLevelStatistics(student.getId(), null);
            if (null == homeLevel) {
                Set<Long> parentIds = new HashSet<>();
                if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                    parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
                }
                homeLevel = userLevelLoader.getStudentHomeLevelStatistics(student.getId(), parentIds);
            }
            if (null == homeLevel) {
                return MapMessage.errorMessage("未查询到家庭等级信息");
            }

            MapMessage message = MapMessage.successMessage();
            Long lastHomeLevel = userLevelService.updateUserActivationLastHomeLevel(currentUserId(), student.getId(), Long.valueOf(homeLevel.getLevel()));
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

            int parentMaxLevel = 0;
            String currentParentCallName = "";
            UserActivationLevel currentParentLevel = null;

            if (MapUtils.isNotEmpty(homeLevel.getParentLevels())) {
                List<Map<String, Object>> parentLevels = new ArrayList<>();
                Map<String, Object> currentUserLevel = new HashMap<>();
                for (Map.Entry<Long, UserActivationLevel> entry : homeLevel.getParentLevels().entrySet()) {
                    String callName = "";
                    Long parentId = entry.getKey();
                    UserActivationLevel level = entry.getValue();

                    StudentParentRef studentParentRef = studentParentRefs.stream().filter(ref -> ref.getParentId().equals(parentId)).findFirst().orElse(null);
                    if (null == studentParentRef) {
                        continue;
                    }

                    Map<String, Object> info = new HashMap<>();
                    info.put("level", level.getLevel());
                    info.put("activation", level.getValue());
                    callName = StringUtils.isBlank(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName();
                    info.put("callName", callName);
                    if (parentId.equals(currentUserId())) {
                        currentParentCallName = callName;
                        currentParentLevel = level;
                    }

                    User parent = raikouSystem.loadUser(parentId);
                    if (null != parent) {
                        info.put("avatar", getUserAvatarImgUrl(parent));
                    }

                    if (parentId.equals(currentUserId())) {
                        currentUserLevel = info;
                    } else {
                        parentLevels.add(info);
                    }

                    if (level.getLevel() > parentMaxLevel) {
                        parentMaxLevel = level.getLevel();
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
                parentLevels.add(0, currentUserLevel);

                message.add("parentLevels", parentLevels);
            }

            if (null != currentParentLevel) {
                message.add("parentAvatar", getUserAvatarImgUrl(currentParent().fetchImageUrl()));
                message.add("parentCallName", currentParentCallName);
                message.add("parentLevel", currentParentLevel.getLevel());
                message.add("parentActivation", currentParentLevel.getValue());
                message.add("parentMaxActivation", currentParentLevel.getLevelEndValue() + 1);
                message.add("parentMinActivation", currentParentLevel.getLevelStartValue());
            }

            Map<Long, Boolean> inUserBlackList = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(student));
            message.add("studentInBlackList", inUserBlackList.getOrDefault(studentId, false));

            List<UserActivationHomeRankMapper> ranks = getStudentHomeLevelRank(studentId);
            if (CollectionUtils.isNotEmpty(ranks)) {
                message.add("ranks", ranks);
            }

            if (null != student) {
                Integer year = student.getProfile().getYear();
                if (null != year && LocalDate.now().getYear() - year > 0) {
                    message.add("studentAge", LocalDate.now().getYear() - year);
                }
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
            logger.error("sid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/home/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeRank() {
        Long studentId = getRequestLong("sid");
        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<UserActivationHomeRankMapper> ranks = getStudentHomeLevelRank(studentId);
            if (CollectionUtils.isEmpty(ranks)) {
                return MapMessage.errorMessage("未查询到家庭等级排名");
            }

            return MapMessage.successMessage().add("ranks", ranks);
        } catch (Exception ex) {
            logger.error("sid:{}", studentId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private List<RankContext> getRankContext(List<User> classmates) {
        List<RankContext> infos = new ArrayList<>();
        for (User user : classmates) {
            RankContext context = new RankContext();
            context.setUserId(user.getId());
            context.setRealName(user.fetchRealname());
            context.setAvatar(user.fetchImageUrl());
            infos.add(context);
        }
        return infos;
    }

    private List<UserActivationHomeRankMapper> getStudentHomeLevelRank(Long studentId) {
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
            User student = raikouSystem.loadUser(studentId);
            if (null != student) {
                users.add(student);
            }

            List<RankContext> contexts = getRankContext(users);
            ranks = userLevelLoader.getStudentHomeLevelRank(studentId, contexts);
        }

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
        }
        return mappers;
    }
}
