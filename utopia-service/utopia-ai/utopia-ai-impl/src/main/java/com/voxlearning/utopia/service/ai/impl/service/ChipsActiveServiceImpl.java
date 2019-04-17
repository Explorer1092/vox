package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.support.ChipCourseSupport;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.WechatLoader;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named
@ExposeServices({
        @ExposeService(interfaceClass = ChipsActiveService.class, version = @ServiceVersion(version = "20190311")),
        @ExposeService(interfaceClass = ChipsActiveService.class, version = @ServiceVersion(version = "20190401"))
})
public class ChipsActiveServiceImpl implements ChipsActiveService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int PAGE_SIZE = 20;
    public static final String superKey = "Chips_Super_User";

    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;
    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;
    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private ChipCourseSupport chipCourseSupport;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;
    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;
    @Inject
    private ActiveServiceTemplateDao activeServiceTemplateDao;
    @Inject
    private ActiveServiceUserTemplateDao activeServiceUserTemplateDao;
    @Inject
    protected AIUserQuestionResultCollectionDao aiUserQuestionResultCollectionDao;
    @Inject
    private ChipsEnglishContentLoaderImpl chipsEnglishContentLoader;
    @ImportService(interfaceClass = WechatLoader.class)
    private WechatLoader wechatLoader;
    @Inject
    private ChipsEnglishClazzServiceImpl chipsEnglishClazzService;
    @Inject
    private ChipsOtherServiceUserTemplateDao chipsOtherServiceUserTemplateDao;
    @Inject
    private ChipsOtherServiceTemplateDao chipsOtherServiceTemplateDao;
    @Inject
    private ChipsEnglishUserLoaderImpl chipsEnglishUserLoader;
    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;
    @Inject
    private AIUserBookResultDao aiUserBookResultDao;
    @Inject
    private ChipsEnglishContentLoaderImpl chipsEnglishContentLoaderImpl;
    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    public static final String APP_KEY = "1691c8d7283446bb9707207782caf2e1";

    public static final String API_TEST = "http://39.106.96.141:8089/recognize";
    public static final String API_PRODUCTION = "http://vox_rec.17zuoye.com/recognize";

    private MapMessage handleBinding(Long classId, int status, Long userId, int pageNum, Date updateBeginDate) {
        MapMessage message = MapMessage.successMessage();
        List<Long> userList = getBindingUserList(classId, userId);
        userList = filterTestFefundNotAddWx(userList);
        if (userList.size() <= 0) {
            message.put("infoList", CollectionUtils.emptyCollection());
            return message;
        }
        // 总页数
//        long totalPage = userList.size() / PAGE_SIZE + 1;
        message.put("totalPage", Double.valueOf(Math.ceil(userList.size() * 1.0/PAGE_SIZE)).intValue());
        Map<Long, ChipsOtherServiceUserTemplate> userTemplateMap = chipsOtherServiceUserTemplateDao.query(userList, ChipsActiveServiceType.BINDING.name(), "all", updateBeginDate)
                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));
        List<ActiveServiceInfo> list = getActiveServiceInfos(ChipsActiveServiceType.BINDING, status, userList, userTemplateMap);
        message.put("infoList", page(list, pageNum));
        return message;
    }

    /**
     * 获取班级下没有绑定公众号的用户
     */
    @NotNull
    private List<Long> getBindingUserList(Long classId, Long userId) {
        List<Long> userIdList;
        if (userId != null && userId != 0 && userId != -1) {
            userIdList = Collections.singletonList(userId);
        } else {
            userIdList = chipsEnglishClazzService.selectAllUserByClazzId(classId);
        }
        Map<Long, Boolean> map = registeredInWeChatSubscription(userIdList);
        List<Long> userList = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!v) {
                userList.add(k);
            }
        });
        return userList;
    }

    private Map<Long, AIUserBookResult.Level> getRenewUserAndLevel(Long clazzId, Long userId, String level) {
        List<Long> userIdList;
        if (userId != null && userId != 0 && userId != -1) {
            userIdList = Collections.singletonList(userId);
        } else {
            userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return Collections.emptyMap();
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || load.getBeginDate() == null || load.getEndDate() == null) {
            return Collections.emptyMap();
        }
        Date today = DayRange.current().getStartDate();
        Date start = DateUtils.addDays(load.getBeginDate(), 5);
        Date end = DateUtils.addDays(load.getEndDate(), 2);
        if (!invalid(today, start, end)) {
            return Collections.emptyMap();
        }
        Map<Long, AIUserBookResult.Level> resultMap = new HashMap<>();
        Map<Long, AIUserBookResult.Level> userLevelMap = userLevel(userIdList, clazz);
        userIdList.forEach(uid -> {
            AIUserBookResult.Level l = userLevelMap.get(uid);
            if (l == null) {
                return;
            }
            if (StringUtils.isNotBlank(level)) {
                if (l != AIUserBookResult.Level.valueOf(level)) {
                    return;
                }
            }
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), uid);
            UserOrder userOrder = userOrderList.stream().filter(o -> invalid(o.getCreateDatetime(), start, end)).findFirst().orElse(null);
            if (userOrder != null) {
                return;
            }
            resultMap.put(uid, l);
        });
        return resultMap;
    }

    private List<Long> getUserInstructionUserList(Long classId, Long userId) {
        List<Long> userIdList;
        if (userId != null && userId != 0 && userId != -1) {
            userIdList = Collections.singletonList(userId);
        } else {
            userIdList = chipsEnglishClazzService.selectAllUserByClazzId(classId);
        }
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        return userExtSplitMap.values().stream().filter(s -> s.getServiceScore() == null || s.getServiceScore() == 0).map(ChipsEnglishUserExtSplit::getId).collect(Collectors.toList());
    }

    private Map<Long, AIUserBookResult.Level> tempRenewRemind(Long clazzId) {
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return Collections.emptyMap();
        }
        return userLevel(userIdList, clazz);
    }

    private MapMessage handleRenewRemind(Long classId, int status, Long userId, int pageNum, Date updateBeginDate, String level) {
        Map<Long, AIUserBookResult.Level> userLevelMap = getRenewUserAndLevel(classId, userId, level);
        if (RuntimeMode.lt(Mode.STAGING)) {
            userLevelMap = tempRenewRemind(classId);
        }
        MapMessage message = MapMessage.successMessage();
        if (MapUtils.isEmpty(userLevelMap)) {
            message.put("infoList", CollectionUtils.emptyCollection());
            return message;
        }
        // 总页数
        message.put("totalPage",  Double.valueOf(Math.ceil(userLevelMap.size() * 1.0/PAGE_SIZE)).intValue());
//        Map<Long, ChipsOtherServiceUserTemplate> userTemplateMap = chipsOtherServiceUserTemplateDao.query(userLevelMap.keySet(), ChipsActiveServiceType.RENEWREMIND.name(), "all", updateBeginDate)
//                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));
        Map<Long, ChipsOtherServiceUserTemplate> v1Map = chipsOtherServiceUserTemplateDao.query(userLevelMap.keySet(), ChipsActiveServiceType.RENEWREMIND.name() + "-v1", "all", updateBeginDate)
                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));
        Map<Long, ChipsOtherServiceUserTemplate> v2Map = chipsOtherServiceUserTemplateDao.query(userLevelMap.keySet(), ChipsActiveServiceType.RENEWREMIND.name() + "-v2", "all", updateBeginDate)
                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));
        Map<Long, ChipsOtherServiceUserTemplate> gradeMap = chipsOtherServiceUserTemplateDao.query(userLevelMap.keySet(), ChipsActiveServiceType.RENEWREMIND.name() + "-grade", "all", updateBeginDate)
                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));

        List<ActiveServiceInfo> list = getActiveServiceInfosV2(ChipsActiveServiceType.RENEWREMIND, status, userLevelMap.keySet(), v1Map, v2Map, gradeMap);
        message.put("infoList", page(list, pageNum));
        return message;
    }

    private MapMessage handleUseInstruction(Long classId, int status, Long userId, int pageNum, Date updateBeginDate) {
        MapMessage message = MapMessage.successMessage();
        List<Long> userList = getUserInstructionUserList(classId, userId);
        userList = filterTestFefundNotAddWx(userList);
        if (userList.size() <= 0) {
            message.put("infoList", CollectionUtils.emptyCollection());
            return message;
        }
        // 总页数
        message.put("totalPage", Double.valueOf(Math.ceil(userList.size() * 1.0/PAGE_SIZE)).intValue());
        Map<Long, ChipsOtherServiceUserTemplate> userTemplateMap = chipsOtherServiceUserTemplateDao.query(userList, ChipsActiveServiceType.USEINSTRUCTION.name(), "all", updateBeginDate)
                .stream().collect(Collectors.toMap(ChipsOtherServiceUserTemplate::getUserId, Function.identity()));
        List<ActiveServiceInfo> list = getActiveServiceInfos(ChipsActiveServiceType.USEINSTRUCTION, status, userList, userTemplateMap);
        message.put("infoList", page(list, pageNum));
        return message;
    }

    private List<ActiveServiceInfo> getActiveServiceInfosV2(ChipsActiveServiceType serviceType, int status, Collection<Long> userList,
                                                            Map<Long, ChipsOtherServiceUserTemplate> v1Map,Map<Long, ChipsOtherServiceUserTemplate> v2Map,
                                                            Map<Long, ChipsOtherServiceUserTemplate> gradeMap) {
        Map<Long, String> userNameMap = getUserNameMap(userList);
        List<ActiveServiceInfo> list = new ArrayList<>();
        userList.forEach(u -> {
            ChipsOtherServiceUserTemplate v1t = v1Map.get(u);
            ChipsOtherServiceUserTemplate v2t = v2Map.get(u);
            ChipsOtherServiceUserTemplate gradet = gradeMap.get(u);
            if (status == 1 && (v1t == null || v2t == null || gradet == null)) {
                return;
            }
            if (status == 0 && (v1t != null && v2t != null && gradet != null)) {
                return;
            }
            ActiveServiceInfo info = new ActiveServiceInfo();
            info.setServiceType(serviceType.name());
            info.setServiceName(serviceType.getDesc());
            info.setUserId(u);
            info.setUserName(userNameMap.get(u));
//            info.setStatus(t != null);
//            info.setCreateDate(t == null ? null : t.getUpdateDate());

            List<ActiveServiceInfo.RenewType> renewTypeList = new ArrayList<>();
            ActiveServiceInfo.RenewType grade = new ActiveServiceInfo.RenewType();
            grade.setStatus(gradet != null);
            grade.setType("grade");
            grade.setDesc(gradet != null ? "完成定级" : "定级报告");
            renewTypeList.add(grade);
            ActiveServiceInfo.RenewType v1 = new ActiveServiceInfo.RenewType();
            v1.setStatus(v1t != null);
            v1.setType("v1");
            v1.setDesc(v1t != null ? "完成续费v1" : "续费v1");
            renewTypeList.add(v1);

            ActiveServiceInfo.RenewType v2 = new ActiveServiceInfo.RenewType();
            v2.setStatus(v2t != null);
            v2.setType("v2");
            v2.setDesc(v2t != null? "完成续费v2" :"续费v2");
            renewTypeList.add(v2);

            info.setRenewList(renewTypeList);
            list.add(info);
        });
        return list;
    }

    private List<ActiveServiceInfo.RenewType> build( Map<String, ChipsOtherServiceUserTemplate> userTemplateMap) {
        List<ActiveServiceInfo.RenewType> list = new ArrayList<>();
        if (MapUtils.isEmpty(userTemplateMap)) {
            ActiveServiceInfo.RenewType grade = new ActiveServiceInfo.RenewType();
            grade.setStatus(false);
            grade.setType("grade");
            grade.setDesc("定级报告");
            list.add(grade);
            ActiveServiceInfo.RenewType v1 = new ActiveServiceInfo.RenewType();
            v1.setStatus(false);
            v1.setType("v1");
            v1.setDesc("续费v1");
            list.add(v1);
            ActiveServiceInfo.RenewType v2 = new ActiveServiceInfo.RenewType();
            v2.setStatus(false);
            v2.setType("v2");
            v2.setDesc("续费v2");
            list.add(v2);

        } else {
            ActiveServiceInfo.RenewType grade = new ActiveServiceInfo.RenewType();
            grade.setStatus(userTemplateMap.containsKey("grade"));
            grade.setType("grade");
            grade.setDesc(userTemplateMap.containsKey("grade") ? "完成定级" : "定级报告");
            list.add(grade);
            ActiveServiceInfo.RenewType v1 = new ActiveServiceInfo.RenewType();
            v1.setStatus(userTemplateMap.containsKey("v1"));
            v1.setType("v1");
            v1.setDesc(userTemplateMap.containsKey("v1") ? "完成续费v1" : "续费v1");
            list.add(v1);

            ActiveServiceInfo.RenewType v2 = new ActiveServiceInfo.RenewType();
            v2.setStatus(userTemplateMap.containsKey("v2"));
            v2.setType("v2");
            v2.setDesc(userTemplateMap.containsKey("v2") ? "完成续费v2" :"续费v2");
            list.add(v2);

        }
        return list;
    }

    @NotNull
    private List<ActiveServiceInfo> getActiveServiceInfos(ChipsActiveServiceType serviceType, int status, Collection<Long> userList, Map<Long, ChipsOtherServiceUserTemplate> userTemplateMap) {
        Map<Long, String> userNameMap = getUserNameMap(userList);
        List<ActiveServiceInfo> list = new ArrayList<>();
        userList.forEach(u -> {
            ChipsOtherServiceUserTemplate t = userTemplateMap.get(u);
            if (status == 1 && t == null) {
                return;
            }
            if (status == 0 && t != null) {
                return;
            }
            ActiveServiceInfo info = new ActiveServiceInfo();
            info.setServiceType(serviceType.name());
            info.setServiceName(serviceType.getDesc());
            info.setUserId(u);
            info.setUserName(userNameMap.get(u));
            info.setStatus(t != null ? "1" : "0");//serviceType != SERVICE
            info.setCreateDate(t == null ? null : t.getUpdateDate());
            list.add(info);
        });
        return list;
    }

    private List<ActiveServiceInfo> page(List<ActiveServiceInfo> allList, int page) {
        if (allList.size() < (page - 1) * PAGE_SIZE) {
            return Collections.emptyList();
        }
        if (allList.size() < page * PAGE_SIZE) {
            return allList.subList((page - 1) * PAGE_SIZE, allList.size());
        }
        return allList.subList((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
    }

    @Override
    public MapMessage obtainActiveServiceInfos(ChipsActiveServiceType serviceType, Long classId, int status, String unitId, Date date,
                                               Long userId, int pageNum, Date updateBeginDate, String level) {
        if (serviceType != null) {
            if (serviceType == ChipsActiveServiceType.BINDING) {
                return handleBinding(classId, status, userId, pageNum, updateBeginDate);
            }
            if (serviceType == ChipsActiveServiceType.RENEWREMIND) {
                return handleRenewRemind(classId, status, userId, pageNum, updateBeginDate, level);
            }
            if (serviceType == ChipsActiveServiceType.USEINSTRUCTION) {
                return handleUseInstruction(classId, status, userId, pageNum, updateBeginDate);
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();

        // 单元列表
        ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(classId);
        String productId = chipsEnglishClass.getProductId();
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            mapMessage.put("infoList", CollectionUtils.emptyCollection());
            return mapMessage;
        }
        String bookId = orderProductItemList.get(0).getAppItemId();
        List<StoneUnitData> unitList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
        mapMessage.put("unitList", unitList);

        long totalNum = chipsActiveServiceRecordDao.count(serviceType, classId, status, unitId, date, userId);
        if (totalNum <= 0) {
            mapMessage.put("infoList", CollectionUtils.emptyCollection());
            return mapMessage;
        }

        // 总页数
        mapMessage.put("totalPage", Double.valueOf(Math.ceil(totalNum * 1.0/PAGE_SIZE)).intValue());


        Map<String, StoneUnitData> unitDataMap = unitList.stream().collect(Collectors.toMap(StoneUnitData::getId, u -> u));

        // 主动服务列表
        List<ChipsActiveServiceRecord> infoList = chipsActiveServiceRecordDao.loadByClassIdFilter(serviceType, classId, status,
                unitId, date, userId, pageNum, PAGE_SIZE);
        if (CollectionUtils.isEmpty(infoList)) {
            mapMessage.put("infoList", CollectionUtils.emptyCollection());
            return mapMessage;
        }

        List<Long> userIds = infoList.stream().map(info -> info.getUserId()).collect(Collectors.toList());
        Map<Long, String> userNameMap = getUserNameMap(userIds);

        mapMessage.put("infoList", infoList.stream().map(info -> {
            StoneUnitData unitDate = unitDataMap.get(info.getUnitId());
            String userName = userNameMap.get(info.getUserId());
            return ActiveServiceInfo.valueOf(info, userName == null ? "" : userName, unitDate);
        }).collect(Collectors.toList()));

        return mapMessage;
    }

    @NotNull
    private Map<Long, String> getUserNameMap(Collection<Long> userIds) {
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, String> userNameMap = new HashMap<>();
        if (userMap != null) {
            userMap.forEach((uid, user) -> {
                UserProfile userProfile = user.getProfile();
                userNameMap.put(uid, userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "");
            });
        }
        return userNameMap;
    }

    public List<Long> filterTestFefundNotAddWx(List<Long> userIdList) {
        // 过滤微信
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishUserExtSplitDao.loads(userIdList);
        userIdList = userIdList.stream().filter(u -> userExtSplitMap.get(u) != null
                && userExtSplitMap.get(u).getWeAdd() != null && userExtSplitMap.get(u).getWeAdd()).collect(Collectors.toList());
        // 过滤测试账号
        ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName(superKey);
        if (config != null) {
            List<Long> superList = Arrays.stream(config.getValue().split(",")).map(SafeConverter::toLong).collect(Collectors.toList());
            userIdList = userIdList.stream().filter(u -> !superList.contains(u)).collect(Collectors.toList());
        }
        return userIdList;
    }

    /**
     * 续费提醒
     */
    private List<Long> renewUser(Long clazzId) {
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return Collections.emptyList();
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || load.getBeginDate() == null || load.getEndDate() == null) {
            return Collections.emptyList();
        }
        Date today = DayRange.current().getStartDate();
        Date start = DateUtils.addDays(load.getBeginDate(), 5);
        Date end = DateUtils.addDays(load.getEndDate(), 2);
        if (!invalid(today, start, end)) {
            return Collections.emptyList();
        }
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        Map<Long, AIUserBookResult.Level> userLevelMap = userLevel(userIdList, clazz);
        return userIdList.stream().filter(userId -> {
            if (userLevelMap.get(userId) == null) {
                return false;
            }
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), userId);
            UserOrder userOrder = userOrderList.stream().filter(o -> invalid(o.getCreateDatetime(), start, end)).findFirst().orElse(null);
            return userOrder == null;
        }).collect(Collectors.toList());
    }

    /**
     * 用户定级报告等级
     */
    private Map<Long, AIUserBookResult.Level> userLevel(List<Long> userIdList, ChipsEnglishClass clazz) {
        String bookId = getBookId(clazz);
        if (StringUtils.isBlank(bookId)) {
            return Collections.emptyMap();
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyMap();
        }
        List<String> bookResultIdList = userIdList.stream().map(u -> AIUserBookResult.generateId(u, bookId)).collect(Collectors.toList());
        Map<String, AIUserBookResult> bookResultMap = aiUserBookResultDao.loadByIds(bookResultIdList);
        return bookResultMap.values().stream().collect(Collectors.toMap(AIUserBookResult::getUserId, AIUserBookResult::getLevel));
    }

    private String getBookId(ChipsEnglishClass clazz) {
        return Optional.ofNullable(clazz).map(c -> c.getProductId()).filter(StringUtils::isNotBlank).map(userOrderLoaderClient::loadProductItemsByProductId)
                .map(l -> l.stream().findFirst().map(OrderProductItem::getAppItemId).orElse(null)).orElse(null);
    }

    /**
     * start 和end 之间 包含start，包含end
     * @param date
     * @param start
     * @param end
     * @return
     */
    private boolean invalid(Date date, Date start, Date end) {
        if (date.before(start) || date.after(end)) {
            return false;
        }
        return true;
    }

    public Map<Long, Boolean> registeredInWeChatSubscription(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return new HashMap<>();
        }
        Map<Long, List<UserWechatRef>> userMap = wechatLoader.loadUserWechatRefs(userIdList, WechatType.CHIPS);
        Map<Long, Boolean> map = new HashMap<>();
        for (Long userId : userIdList) {
            List<UserWechatRef> list = userMap.get(userId);
            if (CollectionUtils.isEmpty(list)) {
                map.put(userId, false);
            } else {
                map.put(userId, true);
            }
        }
        return map;
    }

    @Override
    public String loadBookIdByClassId(Long classId) {
        ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(classId);
        String productId = chipsEnglishClass.getProductId();
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            return "";
        }
        return orderProductItemList.get(0).getAppItemId();
    }

    public void genChipsRemindRecordByUserId(Long clazzId, Long userId,String productId, String unitId) {
        // 单元成绩 map （所有已经完成了此单元的用户 -> 成绩）
        AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao.load(userId, unitId);
        if (aiUserUnitResultHistory != null) { // 说明用户已经完成此单元，不用生成记录
            return;
        }
        Map<Long, Boolean> registerWxUser = isRegisterWxUser(Collections.singleton(userId));

        Map<Long, Boolean> testOrRefundUser = isTestOrRefundUser(Collections.singleton(userId), productId);
        if (!registerWxUser.get(userId) || testOrRefundUser.get(userId)) {
            return;
        }
        ChipsActiveServiceRecord record = ChipsActiveServiceRecord.valueOf(ChipsActiveServiceType.REMIND, clazzId, userId, unitId);
        ChipsActiveServiceRecord temp = chipsActiveServiceRecordDao.load(record.getId());
        if (temp != null) {
            return;
        }
        chipsActiveServiceRecordDao.upsert(record);
    }


    @Override
    public MapMessage updateToReminded(Long classId, String unitId, Long userId) {
        this.chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.REMIND, classId, userId, unitId);
        return MapMessage.successMessage();
    }

    /**
     * 点击视频下载时更新为已经服务
     * @param classId
     * @param unitId
     * @param userId
     * @return
     */
    public MapMessage updateServiced(Long classId, String unitId, Long userId) {
        this.chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.SERVICE, classId, userId, unitId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActiveServiceTemplate(ActiveServiceTemplate template) {
        if (template == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        activeServiceTemplateDao.upsert(template);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActiveServiceUserTemplate(ActiveServiceUserTemplate template, String bookId, String unitId) {
        if (template == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        activeServiceUserTemplateDao.upsert(template);
        Long userId = template.getUserId();
        Long classId = obtainClassId(userId, bookId);
        chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.SERVICE, classId, userId, unitId);
        return MapMessage.successMessage();
    }

    public MapMessage saveActiveServiceUserTemplate(ActiveServiceUserTemplate template) {
        if (template == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        activeServiceUserTemplateDao.upsert(template);
        return MapMessage.successMessage();
    }

    public Long obtainClassId(Long userId, String bookId) {
        return Optional.ofNullable(chipsEnglishClassUserRefPersistence.loadByUserId(userId))
                .filter(CollectionUtils::isNotEmpty)
                .map(refs -> refs.stream()
                        .map(ChipsEnglishClassUserRef::getChipsClassId)
                        .filter(cid -> {
                            ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(cid);
                            String productId = chipsEnglishClass.getProductId();
                            List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
                            if (bookId == null || CollectionUtils.isEmpty(orderProductItemList)) {
                                return false;
                            }
                            boolean matched = false;
                            for (OrderProductItem item : orderProductItemList) {
                                if (bookId.equals(item.getAppItemId())) {
                                    matched = true;
                                    break;
                                }
                            }
                            return matched;
                        })
                        .collect(Collectors.toList()))
                .map(list -> CollectionUtils.isEmpty(list) ? null : list.get(0))
                .orElse(null);
    }

    @Override
    public ActiveServiceTemplate loadActiveServiceTemplateById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return activeServiceTemplateDao.load(id);
    }

    @Override
    public Map<String, ActiveServiceTemplate> loadActiveServiceTemplateByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return activeServiceTemplateDao.loads(ids);
    }

    @Override
    public ActiveServiceUserTemplate buildActiveServiceUserTemplate(Long userId, String qid, Collection<String> aids) {
        ActiveServiceTemplate template = activeServiceTemplateDao.load(qid);
        if (template == null) {
            return null;
        }
        Map<String, AIUserQuestionResultCollection> answerMap = aiUserQuestionResultCollectionDao.loads(aids);
        Map<String, ActiveServicePronunciation> pronMap = handlePron(template.getPronunciationList());
        Map<String, ActiveServiceGrammar> gramMap = handleGram(template.getGrammarList());
        Set<String> keywordSet = extractPronKeyword(answerMap);
        Set<ActiveServicePronunciation> pronSet = extractPron(keywordSet, pronMap);
        Set<ActiveServiceGrammar> gramSet = extractGram(answerMap, gramMap);
        ActiveServiceUserTemplate userTemplate = new ActiveServiceUserTemplate();
        String userName = obtainUserName(userId, false);
        userTemplate.setUserId(userId);
        userTemplate.setQid(qid);
        if (CollectionUtils.isEmpty(pronSet) && CollectionUtils.isEmpty(gramSet)) {
            userTemplate.setKnowledgeList(template.getKnowledgeList());
            userTemplate.setLearnSummary(StringUtils.isBlank(template.getDefaultSummary()) ? "" : replaceSummary(template.getDefaultSummary(), userName, "", ""));
            return userTemplate;
        }
        String summary = replaceSummary(template.getLearnSummary(), userName, obtainQuestionName(qid), StringUtils.join(keywordSet, ","));
        userTemplate.setLearnSummary(StringUtils.isBlank(template.getLearnSummary()) ? "" : summary);
        if (CollectionUtils.isNotEmpty(pronSet)) {
            userTemplate.setPronList(new ArrayList<>(pronSet));
        }
        if (CollectionUtils.isNotEmpty(gramSet)) {
            userTemplate.setGrammarList(new ArrayList<>(gramSet));
        }
        return userTemplate;
    }

    @Override
    public MapMessage buildActiveServiceUserTemplateMapMessage(Long userId, String qid, Collection<String> aids, String aid) {
        AIUserQuestionResultCollection answer = loadAIUserQuestionResultCollection(aid);
        Set<String> keywordSet = extractKeyword(answer);
        //按照产品要求暂时去掉语音转文字功能 begin
//        String sentence = audioToSentence(answer == null ? null : answer.getUserAudio());
//        Set<String> sentenceToWords = sentenceToWords(sentence);
//        Set<String> filterKeywordSet = keywordSet.stream().filter(e -> !sentenceToWords.contains(e)).collect(Collectors.toSet());
        String sentence = "";
        Set<String> filterKeywordSet = Collections.emptySet();
        //按照产品要求暂时去掉语音转文字功能 end
        ActiveServiceUserTemplate userTemplate = buildActiveServiceUserTemplate(userId, qid, aids, filterKeywordSet);
        MapMessage message = MapMessage.successMessage();
        message.add("userTemplate", userTemplate);
        message.add("filterKeywordSet", filterKeywordSet);
        message.add("sentence", sentence);
        message.add("keywordSet", keywordSet);
        if (CollectionUtils.isNotEmpty(filterKeywordSet)) {
            message.add("filterFlag", true);
        } else {
            message.add("filterFlag", false);
        }
        return message;
    }

    public ActiveServiceUserTemplate buildActiveServiceUserTemplate(Long userId, String qid, Collection<String> aids, Set<String> filterKeywordSet) {
        ActiveServiceTemplate template = activeServiceTemplateDao.load(qid);
        if (template == null) {
            return null;
        }
        Map<String, AIUserQuestionResultCollection> answerMap = aiUserQuestionResultCollectionDao.loads(aids);
        Map<String, ActiveServicePronunciation> pronMap = handlePron(template.getPronunciationList());
        Map<String, ActiveServiceGrammar> gramMap = handleGram(template.getGrammarList());
        Set<String> keywordSet = extractPronKeyword(answerMap);
        keywordSet = filterKeyword(keywordSet, filterKeywordSet);
        Set<ActiveServicePronunciation> pronSet = extractPron(keywordSet, pronMap);
        Set<ActiveServiceGrammar> gramSet = extractGram(answerMap, gramMap);
        ActiveServiceUserTemplate userTemplate = new ActiveServiceUserTemplate();
        String userName = obtainUserName(userId, false);
        userTemplate.setUserId(userId);
        userTemplate.setQid(qid);
        if (CollectionUtils.isEmpty(pronSet) && CollectionUtils.isEmpty(gramSet)) {
            userTemplate.setKnowledgeList(template.getKnowledgeList());
            userTemplate.setPronList(Collections.emptyList());
            userTemplate.setGrammarList(Collections.emptyList());
            userTemplate.setLearnSummary(StringUtils.isBlank(template.getDefaultSummary()) ? "" : replaceSummary(template.getDefaultSummary(), userName, "", ""));
            return userTemplate;
        }
        String summary = replaceSummary(template.getLearnSummary(), userName, obtainQuestionName(qid), StringUtils.join(keywordSet, ","));
        userTemplate.setLearnSummary(StringUtils.isBlank(template.getLearnSummary()) ? "" : summary);
        if (CollectionUtils.isNotEmpty(pronSet)) {
            userTemplate.setPronList(new ArrayList<>(pronSet));
            userTemplate.setKnowledgeList(Collections.emptyList());
            userTemplate.setGrammarList(Collections.emptyList());
        }
        if (CollectionUtils.isNotEmpty(gramSet)) {
            userTemplate.setGrammarList(new ArrayList<>(gramSet));
            userTemplate.setKnowledgeList(Collections.emptyList());
            userTemplate.setPronList(Collections.emptyList());
        }
        return userTemplate;
    }

    private Set<String> filterKeyword(Set<String> allKeywordSet, Set<String> filterKeywordSet) {
        return allKeywordSet.stream().filter(e -> !filterKeywordSet.contains(e)).collect(Collectors.toSet());
    }

    /**
     * 题目字段
     * @param qid
     * @return
     */
    private String obtainQuestionName(String qid) {
        if (StringUtils.isBlank(qid)) {
            return "";
        }
        Map<String, StoneData> stoneDataMap = chipsEnglishContentLoader.loadQuestionStoneData(Collections.singletonList(qid));
        return Optional.ofNullable(stoneDataMap.get(qid)).map(StoneQuestionData::newInstance).map(StoneQuestionData::getJsonData).map(m -> m.get("translation")).orElse("").toString();
    }

    /**
     * 获取用户姓名
     *
     * @param isReal true: 获取真实姓名  false：获取nickName
     */
    private String obtainUserName(Long userId, boolean isReal) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return "孩子";
        }
        if (isReal) {
            return user.fetchRealname();
        } else {
            UserProfile userProfile = user.getProfile();
            return userProfile == null ? "孩子" : userProfile.getNickName() != null ? userProfile.getNickName() : "孩子";
        }
    }

    /**
     * {userName},{question},{keyword}
     */
    private String replaceSummary(String summary, String userName, String question, String keyword) {
        if (StringUtils.isBlank(summary)) {
            return summary;
        }
        summary = summary.replace("{userName}", userName);
        summary = summary.replace("{question}", question);
        summary = summary.replace("{keyword}", keyword);
        return summary;
    }

    private Set<String> extractPronKeyword(Map<String, AIUserQuestionResultCollection> answerMap) {
        Set<String> keywordSet = new HashSet<>();
        answerMap.forEach((k, v) -> {
            Set<String> ks = extractKeyword(v);
            if (CollectionUtils.isNotEmpty(ks)) {
                keywordSet.addAll(ks);
            }
        });
        return keywordSet;
    }

    private Set<ActiveServicePronunciation> extractPron(Set<String> keywordSet, Map<String, ActiveServicePronunciation> pronMap) {
        Set<ActiveServicePronunciation> pronSet = new HashSet<>();
        for (String k : keywordSet) {
            ActiveServicePronunciation pron = pronMap.get(k);
            if (pron == null) {
                continue;
            }
            pronSet.add(pron);
        }
        return pronSet;
    }

    private Set<ActiveServiceGrammar> extractGram(Map<String, AIUserQuestionResultCollection> answerMap, Map<String, ActiveServiceGrammar> gramMap) {
        Set<String> levelSet = new HashSet<>();
        answerMap.forEach((k, v) -> {
            String l = extractLevel(v);
            if (StringUtils.isNotBlank(l)) {
                levelSet.add(l);
            }
        });
        Set<ActiveServiceGrammar> gramSet = new HashSet<>();
        for (String l : levelSet) {
            ActiveServiceGrammar gram = gramMap.get(l);
            if (gram == null) {
                continue;
            }
            gramSet.add(gram);
        }
        return gramSet;
    }

    private Set<String> extractKeyword(AIUserQuestionResultCollection answer) {
        LessonType lessonType = answer.getLessonType();
        if (lessonType == null) {
            return Collections.emptySet();
        }
        if (lessonType != LessonType.warm_up && lessonType != LessonType.video_conversation) {
            return Collections.emptySet();
        }
        BigDecimal seven = new BigDecimal(7);
        BigDecimal zero = new BigDecimal(0);
        List<AIQuestionAppraisionRequest.Line> lines = Optional.ofNullable(answer.getVoiceEngineJson())
                .map(json -> JsonUtils.fromJson(json, AIQuestionAppraisionRequest.class))
                .map(AIQuestionAppraisionRequest::getLines).orElse(Collections.emptyList());
        Set<String> keywordSet = new HashSet<>();
        for (AIQuestionAppraisionRequest.Line line : lines) {
            List<AIQuestionAppraisionRequest.Word> words = line.getWords();
            if (CollectionUtils.isEmpty(words)) {
                continue;
            }
            for (AIQuestionAppraisionRequest.Word word : words) {
                if (word == null || StringUtils.isBlank(word.getText()) || word.getScore() == null || !word.getText().matches(ChipsEnglishUserLoaderImpl.REGEX)) {
                    continue;
                }
                int i = word.getScore().compareTo(seven);
                int j = word.getScore().compareTo(zero);
                if ((i == -1 || i == 0) && (j == 1 || j == 0)) {//小于等于7分 大于0
                    keywordSet.add(word.getText().trim().toLowerCase());
                }
            }
        }
        return keywordSet;
    }

    private String extractLevel(AIUserQuestionResultCollection answer) {
        LessonType lessonType = answer.getLessonType();
        if (lessonType == null) {
            return null;
        }
        if (lessonType == LessonType.video_conversation) {
            String level = answer.getLevel();
            if (StringUtils.isNotBlank(level)) {
                return level;
            }
        }
        return null;
    }

    private Map<String, ActiveServicePronunciation> handlePron(List<ActiveServicePronunciation> pronList) {
        Map<String, ActiveServicePronunciation> map = new HashMap<>();
        if (CollectionUtils.isEmpty(pronList)) {
            return Collections.emptyMap();
        }
        pronList.forEach(p -> {
            String keyword = p.getKeyword();
            if (StringUtils.isBlank(keyword)) {
                return;
            }
            String[] split = keyword.split(",");
            for (String kw : split) {
                map.put(kw.trim(), p);
            }
        });
        return map;
    }

    private Map<String, ActiveServiceGrammar> handleGram(List<ActiveServiceGrammar> gramList) {
        Map<String, ActiveServiceGrammar> map = new HashMap<>();
        if (CollectionUtils.isEmpty(gramList)) {
            return Collections.emptyMap();
        }
        gramList.forEach(g -> {
            String level = g.getLevel();
            if (StringUtils.isBlank(level)) {
                return;
            }
            String[] split = level.split(",");
            for (String l : split) {
                map.put(l.trim(), g);
            }
        });
        return map;
    }

    /**
     * 计算差几节课
     *
     * @param userId
     * @return
     */
    private String calRemindTemplateId(long userId, long clazzId) {
        String defaultId = ChipsActiveServiceType.REMIND + "-0";
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return defaultId;
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || CollectionUtils.isEmpty(load.getCourses())) {
            return defaultId;
        }
        Date startDate = DayRange.current().getStartDate();
        //在今天之前已经开课的单元
        Map<String, String> unitMap = load.getCourses().stream().filter(u -> u.getBeginDate().before(startDate)).map(ChipsEnglishProductTimetable.Course::getUnitId).collect(Collectors.toMap(Function.identity(), Function.identity(), (k1, k2) -> k1));
        int count = 0;
        for (String unitId : unitMap.keySet()) {
            AIUserUnitResultHistory history = aiUserUnitResultHistoryDao.load(userId, unitId);
            if (history == null || !history.getFinished()) {
                count++;
            }
        }
        if (count == 0) {
            return defaultId;
        }
        if (count == 1) {
            return ChipsActiveServiceType.REMIND + "-1";
        }
        if (count == 2) {
            return ChipsActiveServiceType.REMIND + "-2";
        }
        return ChipsActiveServiceType.REMIND + "-3";

    }

    private String calRenewTemplateIdV2(long userId, long clazzId, String renewType) {
        String defaultId = ChipsActiveServiceType.RENEWREMIND + "-def-v1";
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (userId == 0 || clazz == null) {
            logger.info("RenewTemplateId default userId: " + userId + ";clazzId:" + clazzId);
            return defaultId;
        }
        if ("grade".equals(renewType)) {
            return ChipsActiveServiceType.RENEWREMIND + "-grade";
        }
        AIUserBookResult.Level userLevel = userLevel(Collections.singletonList(userId), clazz).get(userId);
        if (userLevel == null) {
            logger.info("RenewTemplateId default userId: " + userId + ";clazzId:" + clazzId + ";level: " + userLevel);
            return defaultId;
        }
        if (userLevel == AIUserBookResult.Level.One) {
            if ("v1".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g1-v1";
            }
            if ("v2".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g1-v2";
            }
        }
        if (userLevel == AIUserBookResult.Level.Two) {
            if ("v1".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g2-v1";
            }
            if ("v2".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g2-v2";
            }
        }
        if (userLevel == AIUserBookResult.Level.Three) {
            if ("v1".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g3-v1";
            }
            if ("v2".equals(renewType)) {
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g3-v2";
            }
        }
        logger.info("RenewTemplateId default userId: " + userId + ";clazzId:" + clazzId + ";level: " + userLevel);
        return defaultId;
    }

        /**
         * 续费的用户模板id
         * serviceType.name() + "-g1-first"; serviceType.name() + "-g1-more";
         * serviceType.name() + "-g2-first"; serviceType.name() + "-g2-more";
         * serviceType.name() + "-g3-first"; serviceType.name() + "-g3-more";
         * serviceType.name() + "-default";
         */
        private String calRenewTemplateId ( long userId, long clazzId){
            String defaultId = ChipsActiveServiceType.RENEWREMIND + "-default";
            ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
            if (userId == 0 || clazz == null) {
                return defaultId;
            }
            AIUserBookResult.Level userLevel = userLevel(Collections.singletonList(userId), clazz).get(userId);
            if (userLevel == null) {
                return defaultId;
            }
            Date startDate = DayRange.current().getStartDate();
            if (userLevel == AIUserBookResult.Level.One) {
                ChipsOtherServiceUserTemplate load = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.RENEWREMIND.name() + "-" + userId);
                if (load == null || load.getCreateDate() == null || startDate.before(load.getCreateDate())) {
                    return ChipsActiveServiceType.RENEWREMIND.name() + "-g1-first";
                }
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g1-more";
            }
            if (userLevel == AIUserBookResult.Level.Two) {
                ChipsOtherServiceUserTemplate load = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.RENEWREMIND.name() + "-" + userId);
                if (load == null || load.getCreateDate() == null || startDate.before(load.getCreateDate())) {
                    return ChipsActiveServiceType.RENEWREMIND.name() + "-g2-first";
                }
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g2-more";
            }
            if (userLevel == AIUserBookResult.Level.Three) {
                ChipsOtherServiceUserTemplate load = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.RENEWREMIND.name() + "-" + userId);
                if (load == null || load.getCreateDate() == null || startDate.before(load.getCreateDate())) {
                    return ChipsActiveServiceType.RENEWREMIND.name() + "-g3-first";
                }
                return ChipsActiveServiceType.RENEWREMIND.name() + "-g3-more";
            }
            return defaultId;
        }

        @Override
        public MapMessage saveOtherServiceTypeUserTemplate (String serviceType,long userId, long clazzId, String renewType){
            MapMessage message = MapMessage.successMessage();
            ChipsActiveServiceType type = ChipsActiveServiceType.of(serviceType);
            ChipsOtherServiceUserTemplate template = new ChipsOtherServiceUserTemplate();
            if (type == ChipsActiveServiceType.BINDING) {
                //数据跟通用模板一致，不做单独存储
            }
            if (type == ChipsActiveServiceType.USEINSTRUCTION) {
                //数据跟通用模板一致，不做单独存储
            }
            if (type == ChipsActiveServiceType.REMIND) {
                //
                String templateId = calRemindTemplateId(userId, clazzId);
                template.setTemplateId(templateId);
                if (templateId.equals(ChipsActiveServiceType.REMIND + "-1")) {
                    String text = "{userName}的妈妈您好，老师看到孩子{unFinishedDays}的课程没有完成哦，怕您不知道怎么补课，所以给您发下具体的操作流程图哦。希望可以抽空给孩子补上哦～";
                    message.add("remindText", replaceRemindJson(text, userId, clazzId));
                } else if (templateId.equals(ChipsActiveServiceType.REMIND + "-2")) {
                    String text = "{userName}的妈妈您好，孩子已经完成了{finishedDaysCount}的课程，希望可以尽快和孩子一起把落下的{unFinishedDays}的课程补上哦，加油～";
                    message.add("remindText", replaceRemindJson(text, userId, clazzId));
                } else if (templateId.equals(ChipsActiveServiceType.REMIND + "-3")) {
                    String text = "{userName}的妈妈您好，孩子本期的课程中还有{unFinishedDaysCount}天的课程没有完成哦，希望可以尽快和孩子一起把落下的课程补上。";
                    message.add("remindText", replaceRemindJson(text, userId, clazzId));
                }
            }
            if (type == ChipsActiveServiceType.RENEWREMIND) {
                String templateId = calRenewTemplateIdV2(userId, clazzId, renewType);
                template.setTemplateId(templateId);
                message.add("templateId", templateId);
            }

            template.setUserId(userId);
            if (type == ChipsActiveServiceType.RENEWREMIND) {
                template.setServiceType(serviceType + "-" + renewType);
                template.setId(serviceType + "-" + renewType + "-" + userId);
            } else {
                template.setServiceType(serviceType);
                template.setId(serviceType + "-" + userId);
            }
            chipsOtherServiceUserTemplateDao.upsert(template);


            return message;
        }

        private List<Map<String, Object>> toMap (List < ChipsOtherServiceTemplate > templateList) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (ChipsOtherServiceTemplate template : templateList) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", template.getId());
                m.put("name", template.getName());
                m.put("flag", StringUtils.isBlank(template.getJson()) ? false : true);
                m.put("serviceType", template.getServiceType());
                m.put("updateTime", template.getUpdateDate() == null ? "" : DateUtils.dateToString(template.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
                list.add(m);
            }
            return list;
        }

        @Override
        public MapMessage loadOtherServiceTypeTemplateList (ChipsActiveServiceType serviceType){
            List<ChipsOtherServiceTemplate> templateList;
            MapMessage message = MapMessage.successMessage();
            if (serviceType == ChipsActiveServiceType.BINDING) {
                templateList = queryBinding();
                return message.add("data", toMap(templateList));
            }
            if (serviceType == ChipsActiveServiceType.USEINSTRUCTION) {
                templateList = queryUserInstruction();
                return message.add("data", toMap(templateList));
            }
            if (serviceType == ChipsActiveServiceType.RENEWREMIND) {
                templateList = queryRenewRemindV2();
                return message.add("data", toMap(templateList));
            }
            if (serviceType == ChipsActiveServiceType.REMIND) {
                templateList = queryRemind();
                return message.add("data", toMap(templateList));
            }
            return MapMessage.errorMessage().add("info", "serviceType error : " + serviceType.name());
        }

        @NotNull
        private List<ChipsOtherServiceTemplate> queryBinding () {
            ChipsActiveServiceType serviceType = ChipsActiveServiceType.BINDING;
            String id = serviceType.name() + "-1";
            return Collections.singletonList(buildChipsOtherServiceTemplate(id, serviceType.name(), ""));
        }

        private List<ChipsOtherServiceTemplate> queryUserInstruction () {
            ChipsActiveServiceType serviceType = ChipsActiveServiceType.USEINSTRUCTION;
            String id = serviceType.name() + "-1";
            return Collections.singletonList(buildChipsOtherServiceTemplate(id, serviceType.name(), ""));
        }

    private ChipsOtherServiceTemplate buildChipsOtherServiceTemplate(String id, String serviceType, String name) {
        ChipsOtherServiceTemplate template = chipsOtherServiceTemplateDao.load(id);
        if (template == null) {
            template = new ChipsOtherServiceTemplate();
            template.setId(id);
            if (name != null) {
                template.setName(name);
            }
            template.setServiceType(serviceType);
            template.setJson("");
        }
        if (template.getId().equals("RENEWREMIND-g3-v2") && template.getName().equals("G4-V2")) {
            template.setName("G3-V2");
        }
        return template;
    }

    private List<ChipsOtherServiceTemplate> queryRenewRemind() {
        ChipsActiveServiceType serviceType = ChipsActiveServiceType.RENEWREMIND;
        List<ChipsOtherServiceTemplate> list = new ArrayList<>();
        String g1FirstId = serviceType.name() + "-g1-first";
        String g1MoreId = serviceType.name() + "-g1-more";
        String g2FirstId = serviceType.name() + "-g2-first";
        String g2MoreId = serviceType.name() + "-g2-more";
        String g3FirstId = serviceType.name() + "-g3-first";
        String g3MoreId = serviceType.name() + "-g3-more";
        String defaultId = serviceType.name() + "-default";

        list.add(buildChipsOtherServiceTemplate(g1FirstId, serviceType.name(), "G1首次"));
        list.add(buildChipsOtherServiceTemplate(g2FirstId, serviceType.name(), "G2首次"));
        list.add(buildChipsOtherServiceTemplate(g3FirstId, serviceType.name(), "G3首次"));
        list.add(buildChipsOtherServiceTemplate(g1MoreId, serviceType.name(), "G1后续"));
        list.add(buildChipsOtherServiceTemplate(g2MoreId, serviceType.name(), "G2后续"));
        list.add(buildChipsOtherServiceTemplate(g3MoreId, serviceType.name(), "G3后续"));
        list.add(buildChipsOtherServiceTemplate(defaultId, serviceType.name(), "默认"));
        return list;
    }

    private List<ChipsOtherServiceTemplate> queryRenewRemindV2() {
        ChipsActiveServiceType serviceType = ChipsActiveServiceType.RENEWREMIND;
        List<ChipsOtherServiceTemplate> list = new ArrayList<>();
        String g1V1 = serviceType.name() + "-g1-v1";
        String g1V2 = serviceType.name() + "-g1-v2";
        String g2V1 = serviceType.name() + "-g2-v1";
        String g2V2 = serviceType.name() + "-g2-v2";
        String g3V1 = serviceType.name() + "-g3-v1";
        String g3V2 = serviceType.name() + "-g3-v2";
        String defV1 = serviceType.name() + "-def-v1";
        String defV2 = serviceType.name() + "-def-v2";
        list.add(buildChipsOtherServiceTemplate(g1V1, serviceType.name(), "G1-V1"));
        list.add(buildChipsOtherServiceTemplate(g1V2, serviceType.name(), "G1-V2"));
        list.add(buildChipsOtherServiceTemplate(g2V1, serviceType.name(), "G2-V1"));
        list.add(buildChipsOtherServiceTemplate(g2V2, serviceType.name(), "G2-V2"));
        list.add(buildChipsOtherServiceTemplate(g3V1, serviceType.name(), "G3-V1"));
        list.add(buildChipsOtherServiceTemplate(g3V2, serviceType.name(), "G3-V2"));
        list.add(buildChipsOtherServiceTemplate(defV1, serviceType.name(), "DEF-V1"));
        list.add(buildChipsOtherServiceTemplate(defV2, serviceType.name(), "DEF-V2"));
        return list;
    }

    @NotNull
    private List<ChipsOtherServiceTemplate> queryRemind() {
        ChipsActiveServiceType serviceType = ChipsActiveServiceType.REMIND;
        List<ChipsOtherServiceTemplate> list = new ArrayList<>();
        String id1 = serviceType.name() + "-1";
        String id2 = serviceType.name() + "-2";
        String id3 = serviceType.name() + "-3";
        list.add(buildChipsOtherServiceTemplate(id1, serviceType.name(), "缺1天"));
        list.add(buildChipsOtherServiceTemplate(id2, serviceType.name(), "缺2天"));
        list.add(buildChipsOtherServiceTemplate(id3, serviceType.name(), "缺3天及以上"));
        return list;
    }

    @Override
    public ChipsOtherServiceTemplate loadOtherServiceTypeTemplate(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return chipsOtherServiceTemplateDao.load(id);
    }

    @Override
    public MapMessage saveOtherServiceTypeTemplate(ChipsOtherServiceTemplate template) {
        chipsOtherServiceTemplateDao.upsert(template);
        return MapMessage.successMessage();
    }


    private long remainFinishDay(long clazzId) {
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return 0l;
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || load.getEndDate() == null) {
            return 0l;
        }
        Date startDate = DayRange.current().getStartDate();
        long l = DateUtils.dayDiff(load.getEndDate(), startDate);
        if (l < 0l) {
            return 0l;
        }
        return l;
    }

    /**
     * 已经开课的单元的用户成绩和班级平均成绩
     *
     * @param clazzId
     * @param userId
     * @return
     */
    private List<Map<String, String>> userScoreList(long clazzId, long userId) {
        //只处理产品下一个教材的
        Map<Long, List<AIUserInfoWithScore>> userIdToScoreMap = chipsEnglishUserLoader.loadClassSingleUserInfoWithScore(clazzId, Collections.singletonList(userId));
        Map<String, Integer> userUnitScoreMap = Optional.ofNullable(userIdToScoreMap.get(userId)).filter(CollectionUtils::isNotEmpty).map(l -> l.get(0)).map(AIUserInfoWithScore::getScoreLis)
                .map(l -> l.stream().collect(Collectors.toMap(AIUserUnitScore::getUnitId, AIUserUnitScore::getScore))).get();
        List<ChipsEnglishClassStatistics> classStatisticsList = chipsEnglishClazzService.selectChipsEnglishClassStatisticsByClazzId(clazzId);
        Map<String, ChipsEnglishClassStatistics> unitClazzMap = classStatisticsList.stream().filter(s -> s != null && StringUtils.isNotEmpty(s.getUnitId()))
                .collect(Collectors.toMap(ChipsEnglishClassStatistics::getUnitId, Function.identity()));
        List<String> beginUnitList = beginUnitList(clazzId);
        List<Map<String, String>> scoreList = new ArrayList<>();
        beginUnitList.forEach(u -> {
            Integer userScore = userUnitScoreMap.get(u);
            ChipsEnglishClassStatistics clazzStat = unitClazzMap.get(u);
            Map<String, String> map = new HashMap<>();
            map.put("userScore", userScore == null || userScore == -1 ? "/" : userScore + "");
            map.put("avgScore", clazzStat == null ? "0.00" : formatRate(clazzStat.getClassScore(), clazzStat.getClassFinishNum()));
            scoreList.add(map);
        });
        return scoreList;
    }

    private String formatRate(Integer numerator, Integer denominator) {
        if (numerator == null || numerator == 0 || denominator == null || denominator == 0) {
            return "0.00";
        }
        double val = new BigDecimal(numerator).divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return val + "";
    }

    /**
     * 已经开课的单元，今天开课的不在内，是按照单元开始顺序排序的
     *
     * @param clazzId
     * @return
     */
    private List<String> beginUnitList(long clazzId) {
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return Collections.emptyList();
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || CollectionUtils.isEmpty(load.getCourses())) {
            return Collections.emptyList();
        }
        Date startDate = DayRange.current().getStartDate();
        return load.getCourses().stream().filter(c -> c.getBeginDate().before(startDate)).map(c -> c.getUnitId()).collect(Collectors.toList());
    }

    public MapMessage loadPageShareTitle(String serviceType, long userId, long clazzId, String renewType) {
        ChipsActiveServiceType otherServiceType = ChipsActiveServiceType.of(serviceType);
        MapMessage message = MapMessage.successMessage();
        if (otherServiceType == ChipsActiveServiceType.BINDING) {
            message.add("sharePrimeTitle", "请绑定薯条英语公众号");
            message.add("shareSubTitle", "每天获得孩子的打卡奖励和学习报告哟！");
        }
        if (otherServiceType == ChipsActiveServiceType.USEINSTRUCTION) {
            message.add("sharePrimeTitle", "这是" + obtainUserName(userId, false) + "的开课指南");
            message.add("shareSubTitle", "点击查看本期薯条英语课程的开课指南");
        }
        if (otherServiceType == ChipsActiveServiceType.RENEWREMIND) {
//            message.add("sharePrimeTitle", obtainUserName(userId, false) + "的学习情况回顾");
//            long l = remainFinishDay(clazzId);
//            message.add("shareSubTitle", "距本期新课停更还有" + l + "天");
            if ("v1".equals(renewType)) {
                message.add("sharePrimeTitle", obtainUserName(userId, false) + "同学的本期学习情况分析");
                message.add("shareSubTitle", "根据孩子实际学习情况，给出的一份学习分析及学习规划建议");
                message.add("pageTitle", obtainUserName(userId, false) + "同学的本期学习情况分析");
            }
            if ("v2".equals(renewType)) {
                message.add("sharePrimeTitle", "薯条英语后续系统课程安排");
                message.add("shareSubTitle", "多项优惠可叠加， 大礼包数量有限，先到先得");
                message.add("pageTitle", "薯条英语后续系统课程安排");
            }
        }
        if (otherServiceType == ChipsActiveServiceType.REMIND) {
            message.add("sharePrimeTitle", obtainUserName(userId, false) + "的补课提醒");
            message.add("shareSubTitle", "请督促孩子尽快补上落下的课程");
        }
        return message;
    }

    @Override
    public MapMessage loadPreviewTemplate(String serviceType, long userId, String templateId, long clazzId, String renewType) {
        ChipsActiveServiceType otherServiceType = ChipsActiveServiceType.of(serviceType);
        MapMessage message = MapMessage.successMessage();

        AiChipsEnglishTeacher teacher = chipsEnglishClazzService.loadTeacherByUserIdAndClazzId(userId, clazzId);
        if (teacher == null) {//随便选一个
            teacher = chipsEnglishClazzService.loadAllChipsEnglishTeacher().stream().findFirst().orElse(null);
        }
        if (teacher != null) {
            message.put("teacherName", teacher.getName());
            message.put("headPortrait", teacher.getHeadPortrait());
        }
        if (otherServiceType == ChipsActiveServiceType.BINDING) {
            ChipsOtherServiceTemplate template = chipsOtherServiceTemplateDao.load(ChipsActiveServiceType.BINDING.name() + "-1");
            message.add("sharePrimeTitle", "请绑定薯条英语公众号");
            message.add("pageTitle", "如何绑定薯条英语公众号");
            if (template == null || StringUtils.isBlank(template.getJson())) {
                return MapMessage.errorMessage("模板为空，请检查该服务模板是否添加");
            }
            String json = template.getJson();
            if (StringUtils.isBlank(json)) {
                message.add("templateList", Collections.emptyList());
            } else {
                message.add("templateList", JsonUtils.fromJsonToList(json, Map.class));//key text, image
            }
            message.add("shareSubTitle", "每天获得孩子的打卡奖励和学习报告哟！");
            return message;
        }
        if (otherServiceType == ChipsActiveServiceType.USEINSTRUCTION) {
            ChipsOtherServiceTemplate template = chipsOtherServiceTemplateDao.load(ChipsActiveServiceType.BINDING.name() + "-1");
            message.add("sharePrimeTitle", "这是" + obtainUserName(userId, false) + "的开课指南");
            message.add("pageTitle", "薯条英语开课指南");
            if (template == null || StringUtils.isBlank(template.getJson())) {
                return MapMessage.errorMessage("模板为空，请检查该服务模板是否添加");
            }
            String json = template.getJson();
            if (StringUtils.isBlank(json)) {
                message.add("templateList", Collections.emptyList());
            } else {
                message.add("templateList", JsonUtils.fromJsonToList(json, Map.class));//key text, image
            }
            message.add("shareSubTitle", "点击查看本期薯条英语课程的开课指南");
            return message;
        }
        if (otherServiceType == ChipsActiveServiceType.RENEWREMIND) {
            ChipsOtherServiceTemplate template;
            if (StringUtils.isNotBlank(templateId)) {
                template = chipsOtherServiceTemplateDao.load(templateId);
            } else {
                ChipsOtherServiceUserTemplate userTemplate = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.RENEWREMIND.name() + "-" + renewType + "-" + userId);
                if (userTemplate != null && StringUtils.isNotBlank(userTemplate.getTemplateId())) {
                    template = chipsOtherServiceTemplateDao.load(userTemplate.getTemplateId());
//                    Set<String> templateIdSet = new HashSet<>();
//                    templateIdSet.add(ChipsActiveServiceType.RENEWREMIND.name() + "-g1-first");
//                    templateIdSet.add(ChipsActiveServiceType.RENEWREMIND.name() + "-g2-first");
//                    templateIdSet.add(ChipsActiveServiceType.RENEWREMIND.name() + "-g3-first");
//                    if (templateIdSet.contains(userTemplate.getTemplateId())) {
//                        message.add("userScoreList", userScoreList(clazzId, userId));
//                    }
                } else {
                    template = null;
                }
            }
//            message.add("sharePrimeTitle", obtainUserName(userId, false) + "的学习情况回顾");
//            message.add("pageTitle", obtainUserName(userId, false) + "同学的本期学习情况分析");
            if (template != null && StringUtils.isNotBlank(template.getJson())) {
                String json = replaceRenewRemindJson(template.getJson(), userId, clazzId);
                if ("v1".equals(renewType)) {
                    RenewV1Pojo pojo = JsonUtils.fromJson(json, RenewV1Pojo.class);
                    String bookId = getBookId(chipsEnglishClassPersistence.load(clazzId));
                    List<RenewV1Pojo.WeekPoint> weekPointList = handleRenewV1tPojoWeekPoint(pojo, userId, bookId);
                    weekPointList.stream().sorted(Comparator.comparing(RenewV1Pojo.WeekPoint::getWeekPointLevel));
                    if (CollectionUtils.isNotEmpty(weekPointList)) {
                        pojo.setWeekPointList(Collections.singletonList(weekPointList.get(0)));
                    } else {
                        pojo.setWeekPointList(Collections.emptyList());
                    }
                    message.add("pojo", pojo);
                    message.add("sharePrimeTitle", obtainUserName(userId, false) + "同学的本期学习情况分析");
                    message.add("shareSubTitle", "根据孩子实际学习情况，给出的一份学习分析及学习规划建议");
                    message.add("pageTitle", obtainUserName(userId, false) + "同学的本期学习情况分析");
                } else {
                    message.add("pojo", JsonUtils.fromJsonToList(json, ActiveServiceItem.class));
                    message.add("sharePrimeTitle", "薯条英语后续系统课程安排");
                    message.add("shareSubTitle", "多项优惠可叠加， 大礼包数量有限，先到先得");
                    message.add("pageTitle", "薯条英语后续系统课程安排");
                }
            } else {
                return MapMessage.errorMessage("模板为空，请检查该服务模板是否添加");
            }
            long l = remainFinishDay(clazzId);
//            message.add("shareSubTitle", "距本期新课停更还有" + l + "天");
            return message;

        }
        if (otherServiceType == ChipsActiveServiceType.REMIND) {
            ChipsOtherServiceTemplate template;
            if (StringUtils.isNotBlank(templateId)) {
                template = chipsOtherServiceTemplateDao.load(templateId);
            } else {
                ChipsOtherServiceUserTemplate userTemplate = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.REMIND.name() + "-" + userId);
                if (userTemplate != null && StringUtils.isNotBlank(userTemplate.getTemplateId())) {
                    template = chipsOtherServiceTemplateDao.load(userTemplate.getTemplateId());
                } else {
                    template = null;
                }
            }
            message.add("sharePrimeTitle", obtainUserName(userId, false) + "的补课提醒");
            message.add("shareSubTitle", "请督促孩子尽快补上落下的课程");
            if (template != null && StringUtils.isNotBlank(template.getJson())) {
                String json = replaceRemindJson(template.getJson(), userId, clazzId);
                message.add("templateList", JsonUtils.fromJsonToList(json, Map.class));//key text, image
            } else {
                return MapMessage.errorMessage("模板为空，请检查该服务模板是否添加");
            }

            return message;
        }

        return MapMessage.errorMessage().add("info", "serviceType is wrong : " + serviceType);
    }

    /**
     * {userName},{unFinishedDays},{finishedDaysCount},{unFinishedDaysCount}
     */
    private String replaceRemindJson(String json, long userId, long clazzId) {
        if (StringUtils.isBlank(json)) {
            return json;
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return json;
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || CollectionUtils.isEmpty(load.getCourses())) {
            return json;
        }
        Date startDate = DayRange.current().getStartDate();
        //在今天之前已经开课的单元
        List<String> unitList = load.getCourses().stream().filter(u -> u.getBeginDate().before(startDate)).map(ChipsEnglishProductTimetable.Course::getUnitId).collect(Collectors.toList());
        Map<String, String> unFinishedUnitMap = new HashMap<>();
        for (String unitId : unitList) {
            AIUserUnitResultHistory history = aiUserUnitResultHistoryDao.load(userId, unitId);
            if (history == null || !history.getFinished()) {
                unFinishedUnitMap.put(unitId, "");
            }
        }
        StringBuffer unFinishedBuffer = new StringBuffer();
        boolean flag = false;
        int finishedCount = 0;
        for (int i = 0; i < unitList.size(); i++) {
            if (unFinishedUnitMap.get(unitList.get(i)) != null) {
                if (flag) {
                    unFinishedBuffer.append(",");
                }
                unFinishedBuffer.append("Day" + (i + 1));
                flag = true;
            } else {
                finishedCount++;
            }
        }
        json = json.replace("{userName}", obtainUserName(userId, false));
        json = json.replace("{unFinishedDays}", unFinishedBuffer.toString());
        json = json.replace("{finishedDaysCount}", finishedCount + "");
        json = json.replace("{unFinishedDaysCount}", (unitList.size() - finishedCount) + "");
        return json;
    }

    /**
     * {userName},{unFinishedDays},{finishedDaysCount},{unFinishedDaysCount}
     */
    private String replaceRenewRemindJson(String json, long userId, long clazzId) {
        if (StringUtils.isBlank(json)) {
            return json;
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return json;
        }
        ChipsEnglishProductTimetable load = chipsEnglishProductTimetableDao.load(clazz.getProductId());
        if (load == null || CollectionUtils.isEmpty(load.getCourses())) {
            return json;
        }
        Date startDate = DayRange.current().getStartDate();
        //在今天之前已经开课的单元
        List<String> unitList = load.getCourses().stream().filter(u -> u.getBeginDate().before(startDate)).map(ChipsEnglishProductTimetable.Course::getUnitId).collect(Collectors.toList());
//        List<ChipsActiveServiceRecord> recordList = chipsActiveServiceRecordDao.loadByClassIdFilter(ChipsActiveServiceType.REMIND, clazzId, 0, null, null, userId, 1, 10000);
//        if (CollectionUtils.isEmpty(recordList)) {
//            return json;
//        }
//        Map<String, ChipsActiveServiceRecord> unFinishedUnitMap = recordList.stream().collect(Collectors.toMap(ChipsActiveServiceRecord::getUnitId, Function.identity()));
        Map<String, String> unFinishedUnitMap = new HashMap<>();
        for (String unitId : unitList) {
            AIUserUnitResultHistory history = aiUserUnitResultHistoryDao.load(userId, unitId);
            if (history == null || !history.getFinished()) {
                unFinishedUnitMap.put(unitId, "");
            }
        }
        int finishedCount = 0;
        for (int i = 0; i < unitList.size(); i++) {
            if (unFinishedUnitMap.get(unitList.get(i)) == null) {
                finishedCount++;
            }
        }
        json = json.replace("{userName}", obtainUserName(userId, false));
        json = json.replace("{finishedDaysCount}", finishedCount + "");
        return json;
    }

    @Override
    public MapMessage loadActiveServicePreviewTemplate(long userId, String qid, String bookId) {
        MapMessage message = MapMessage.successMessage();
        AiChipsEnglishTeacher teacher = chipsEnglishClazzService.loadTeacherByUserIdAndBookId(userId, bookId);
        if (teacher == null) {//随便选一个
            teacher = chipsEnglishClazzService.loadAllChipsEnglishTeacher().stream().findFirst().orElse(null);
        }
        if (teacher != null) {
            message.put("teacherName", teacher.getName());
            message.put("headPortrait", teacher.getHeadPortrait());
        }
        ActiveServiceUserTemplate userTemplate = activeServiceUserTemplateDao.load(userId + "-" + qid);
        if (userTemplate == null) {
            userTemplate = new ActiveServiceUserTemplate();
            userTemplate.setLearnSummary("");
            userTemplate.setGrammarList(Collections.emptyList());
            userTemplate.setPronList(Collections.emptyList());
            userTemplate.setKnowledgeList(Collections.emptyList());
        }
        message.add("userTemplate", userTemplate);
        message.add("summary", userTemplate.getLearnSummary());
        message.add("gramList", userTemplate.getGrammarList());
        message.add("pronList", userTemplate.getPronList());
        message.add("knowledgeList", userTemplate.getKnowledgeList());
        message.add("pageTitle", "");
        return message;
    }

    @Override
    public MapMessage deleteChipsActiveServiceRecord(long userId, long clazzId) {
        chipsActiveServiceRecordDao.disabled(userId, clazzId);
        return MapMessage.successMessage();
    }

    /**
     * @param userIdCol
     * @param productId 为空时 用户不是测试退费用户
     * @return
     */
    @Override
    public Map<Long, Boolean> isTestOrRefundUser(Collection<Long> userIdCol, String productId) {
        if (CollectionUtils.isEmpty(userIdCol)) {
            return Collections.emptyMap();
        }
        ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName(superKey);
        List<Long> superList = new ArrayList<>();
        if (config != null) {
            superList = Arrays.stream(config.getValue().split(",")).map(SafeConverter::toLong).collect(Collectors.toList());
        }
        Map<Long, Boolean> map = new HashMap<>();
        List<Long> finalSuperList = superList;
        userIdCol.forEach(u -> {
            map.put(u, finalSuperList.contains(u));
//            if (RuntimeMode.lt(Mode.STAGING)) {
//                map.put(u, false);
//                return;
//            }
//            if (StringUtils.isBlank(productId)) {
//                map.put(u, false);
//                return;
//            }
//            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), u);
//            UserOrder userOrder = userOrders.stream().filter(o -> o.getProductId().equals(productId)).findFirst().orElse(null);
//            if (userOrder == null) {
//                return;
//            }
//            PaymentStatus paymentStatus = userOrder.getPaymentStatus();
//            if (paymentStatus.equals(PaymentStatus.Refund)) {
//                map.put(u, true);
//                return;
//            }
//            List<UserOrderPaymentHistory> paymentHistoryList = userOrderLoaderClient.loadUserOrderPaymentHistoryList(u);
//            BigDecimal payAmount = paymentHistoryList.stream().filter(h -> h.getOrderId().equals(userOrder.getId())).map(UserOrderPaymentHistory::getPayAmount).findFirst().orElse(BigDecimal.ZERO);
//            if (payAmount.compareTo(BigDecimal.ONE) < 0) {
//                map.put(u, true);
//                return;
//            }
//            map.put(u, false);
        });
        return map;
    }

    @Override
    public Map<Long, Boolean> isRegisterWxUser(Collection<Long> userIdCol) {
        if (CollectionUtils.isEmpty(userIdCol)) {
            return Collections.emptyMap();
        }
        Map<Long, ChipsEnglishUserExtSplit> loadMap = chipsEnglishUserExtSplitDao.loads(userIdCol);
        Map<Long, Boolean> map = new HashMap<>();
        userIdCol.forEach(u -> {
            map.put(u, Optional.ofNullable(loadMap.get(u)).map(ChipsEnglishUserExtSplit::getWeAdd).orElse(Boolean.FALSE));
        });
        return map;
    }

    @Override
    public Map<Long, Integer> isActiveServiced(Collection<Long> userIdCol, Long clazzId) {
        if (CollectionUtils.isEmpty(userIdCol)) {
            return Collections.emptyMap();
        }
        List<ChipsActiveServiceRecord> recordList = chipsActiveServiceRecordDao.loadByClassId(ChipsActiveServiceType.SERVICE, clazzId);
        Map<Long, Integer> countMap = new HashMap<>();
        recordList.forEach(r -> {
            if (r.getServiced()) {
                Integer count = countMap.get(r.getUserId());
                if (count == null) {
                    count = 0;
                }
                countMap.put(r.getUserId(), count + 1);
            }
        });
        Map<Long, Integer> resultMap = new HashMap<>();
        userIdCol.forEach(u -> {
            Integer count = countMap.get(u);
            if (count == null) {
                resultMap.put(u, 0);
            } else {
                resultMap.put(u, count);
            }

        });
        return resultMap;
    }

    /**
     * 插入前一天的崔课提醒数据， unitId 不能是当日的
     *
     * @param clazzId
     * @param userIdList
     * @param unitId
     * @return
     */
    public MapMessage modifyRemindData(Long clazzId, List<Long> userIdList, String unitId) {
        if (CollectionUtils.isEmpty(userIdList)) {
            userIdList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId).stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return MapMessage.errorMessage().add("info", "no User");
        }
        List<String> delList = delNotWx(userIdList, unitId, clazzId);
        MapMessage message = MapMessage.successMessage();
        message.add("delList", delList);
        List<String> insertList = handleRemindData(userIdList, unitId, clazzId);
        message.add("insertList", insertList);
        return message;
    }

    public List<String> handleRemindData(List<Long> userIdList, String unitId, Long clazzId) {
        Map<Long, AIUserUnitResultHistory> unitResultHistoryMap = Optional.ofNullable(aiUserUnitResultHistoryDao.loadByUnitId(unitId))
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.stream()
                        .filter(r -> r.getFinished() != null && r.getFinished())
                        .collect(Collectors.toMap(result -> result.getUserId(), result -> result, (key1, key2) -> key1)))
                .orElse(new HashMap<>());
        if (MapUtils.isEmpty(unitResultHistoryMap)) {
            return Collections.emptyList();
        }
        Map<Long, Boolean> registerWxUser = isRegisterWxUser(userIdList);

        String productId = Optional.ofNullable(chipsEnglishClassPersistence.load(clazzId)).map(ChipsEnglishClass::getProductId).orElse(null);
        Map<Long, Boolean> testOrRefundUser = isTestOrRefundUser(userIdList, productId);
        List<String> insertList = new ArrayList<>();
        userIdList.forEach(ref -> {
            Long userId = ref;
            if (!registerWxUser.get(userId) || testOrRefundUser.get(userId)) {
                return;
            }
            AIUserUnitResultHistory aiUserUnitResultHistory = unitResultHistoryMap.get(userId);
            if (aiUserUnitResultHistory == null) { // 说明用户已经完成此单元，不用生成记录
                ChipsActiveServiceRecord record = ChipsActiveServiceRecord.valueOf(ChipsActiveServiceType.REMIND, clazzId, userId, unitId);
                String id = ChipsActiveServiceRecord.genId(ChipsActiveServiceType.REMIND, clazzId, userId, unitId);
                ChipsActiveServiceRecord load = chipsActiveServiceRecordDao.load(id);
                if (load == null) {
                    chipsActiveServiceRecordDao.insert(record);
                    insertList.add(ChipsActiveServiceType.REMIND + ":" + userId);
                } else {//处理不该写入的数据
                    if (record.getServiced()) {
                        chipsActiveServiceRecordDao.remove(id);
                    }
                }
            }
        });
        return insertList;
    }

    /**
     * 修复主动服务数据，unitId 是当日开课的
     *
     * @param clazzId
     * @param userIdList
     * @param unitId
     * @return
     */
    @Override
    public MapMessage handleActiveServiceData(Long clazzId, List<Long> userIdList, String unitId) {
        if (CollectionUtils.isEmpty(userIdList)) {
            userIdList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId).stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return MapMessage.errorMessage().add("info", "no User");
        }
        List<String> delList = delNotWx(userIdList, unitId, clazzId);
        MapMessage message = MapMessage.successMessage();
        message.add("delList", delList);
        List<String> insertList = insert(userIdList, unitId, clazzId);
        message.add("insertList", insertList);
        return message;
    }

    private List<String> insert(List<Long> userIdList, String unitId, Long clazzId) {
        Map<Long, AIUserUnitResultHistory> unitResultHistoryMap = Optional.ofNullable(aiUserUnitResultHistoryDao.loadByUnitId(unitId))
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.stream()
                        .filter(r -> r.getFinished() != null && r.getFinished())
                        .collect(Collectors.toMap(result -> result.getUserId(), result -> result, (key1, key2) -> key1)))
                .orElse(new HashMap<>());
        if (MapUtils.isEmpty(unitResultHistoryMap)) {
            return Collections.emptyList();
        }
        Map<Long, Boolean> registerWxUser = isRegisterWxUser(userIdList);

        String productId = Optional.ofNullable(chipsEnglishClassPersistence.load(clazzId)).map(ChipsEnglishClass::getProductId).orElse(null);
        Map<Long, Boolean> testOrRefundUser = isTestOrRefundUser(userIdList, productId);
        List<String> insertList = new ArrayList<>();
        userIdList.forEach(ref -> {
            Long userId = ref;
            if (!registerWxUser.get(userId) || testOrRefundUser.get(userId)) {
                return;
            }
            AIUserUnitResultHistory aiUserUnitResultHistory = unitResultHistoryMap.get(userId);
            if (aiUserUnitResultHistory != null) { // 说明用户已经完成此单元，不用生成记录
                ChipsActiveServiceRecord record = ChipsActiveServiceRecord.valueOf(ChipsActiveServiceType.SERVICE, clazzId, userId, unitId, ChipsActiveServiceRecord.RemarkStatus.Zero);
                String id = ChipsActiveServiceRecord.genId(ChipsActiveServiceType.SERVICE, clazzId, userId, unitId);
                ChipsActiveServiceRecord load = chipsActiveServiceRecordDao.load(id);
                if (load == null) {
                    chipsActiveServiceRecordDao.insert(record);
                    insertList.add(ChipsActiveServiceType.SERVICE + ":" + userId);
                }
                chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.REMIND, clazzId, userId, unitId);
                insertList.add(ChipsActiveServiceType.REMIND + ":" + userId);
            }

        });
        return insertList;
    }

    private List<String> delNotWx(List<Long> userIdList, String unitId, Long clazzId) {
        List<String> delList = new ArrayList<>();
        Map<Long, Boolean> registerWxUser = isRegisterWxUser(userIdList);
        userIdList.forEach(u -> {
            if (registerWxUser.get(u)) {
                return;
            }
//            serviceType-classId-userId-unitId
            String id = ChipsActiveServiceRecord.genId(ChipsActiveServiceType.SERVICE, clazzId, u, unitId);
            String id2 = ChipsActiveServiceRecord.genId(ChipsActiveServiceType.REMIND, clazzId, u, unitId);
            ChipsActiveServiceRecord load = chipsActiveServiceRecordDao.load(id);
            if (load != null) {
                chipsActiveServiceRecordDao.remove(id);
                delList.add(ChipsActiveServiceType.SERVICE + ":" + u);
            }
            ChipsActiveServiceRecord load2 = chipsActiveServiceRecordDao.load(id2);
            if (load2 != null) {
                chipsActiveServiceRecordDao.remove(id2);
                delList.add(ChipsActiveServiceType.REMIND + ":" + u);
            }
        });
        return delList;
    }

    /**
     * 通过unit找到qid， 查询vox_active_service_user_template 有记录代表服务过
     *
     * @param clazzId
     * @param userIdList
     * @param unitId
     * @return
     */
    @Override
    public MapMessage handleActiveServiceStatus(Long clazzId, List<Long> userIdList, String bookId, String unitId) {
        List<String> qidList = getQidList(bookId, unitId);
        if (CollectionUtils.isEmpty(userIdList)) {
            userIdList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId).stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return MapMessage.errorMessage().add("info", "no User");
        }
        Map<Long, Boolean> registerWxUser = isRegisterWxUser(userIdList);
        List<Long> updateList = new ArrayList<>();
        userIdList.forEach(u -> {
            if (!registerWxUser.get(u)) {
                return;
            }
            List<String> templateIdList = qidList.stream().map(qid -> u + "-" + qid).collect(Collectors.toList());
            Map<String, ActiveServiceUserTemplate> loads = activeServiceUserTemplateDao.loads(templateIdList);
            if (MapUtils.isEmpty(loads)) {
                return;
            }
            chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.SERVICE, clazzId, u, unitId);
            updateList.add(u);
        });
        return MapMessage.successMessage().add("updateList", updateList);
    }

    @Nullable
    private List<String> getQidList(String bookId, String unitId) {
        List<StoneData> lessonList = chipsEnglishContentLoaderImpl.loadLessonByUnitId(bookId, unitId);
        if (CollectionUtils.isEmpty(lessonList)) {
            return Collections.emptyList();
        }
        List<StoneLessonData> stoneLessonDataList = lessonList.stream().map(StoneLessonData::newInstance).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stoneLessonDataList)) {
            Collections.emptyList();
        }
        stoneLessonDataList.stream().map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getContent_ids);
        List<String> qidList = new ArrayList<>();
        stoneLessonDataList.forEach(l -> {
            if (l.getJsonData() == null || CollectionUtils.isEmpty(l.getJsonData().getContent_ids())) {
                return;
            }
            l.getJsonData().getContent_ids().forEach(qid -> {
                qidList.add(qid);
            });
        });
        return qidList;
    }

    @Override
    @Deprecated
    public long obtainActiveServiceRemained(ChipsActiveServiceType serviceType, Long classId) {
        if (serviceType == ChipsActiveServiceType.SERVICE || serviceType == ChipsActiveServiceType.REMIND) {
            List<ChipsActiveServiceRecord> records = chipsActiveServiceRecordDao.loadByClassId(serviceType, classId);
            if (CollectionUtils.isEmpty(records)) {
                return 0;
            }
            return records.stream().filter(r -> !r.getServiced()).count();
        }
        if (serviceType == ChipsActiveServiceType.BINDING) {
            List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(classId);
            userIdList = filterTestFefundNotAddWx(userIdList);
            Map<Long, Boolean> map = registeredInWeChatSubscription(userIdList);
            return map.values().stream().filter(f -> !f).count();
        }
        if (serviceType == ChipsActiveServiceType.USEINSTRUCTION) {
            List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(classId);
            userIdList = filterTestFefundNotAddWx(userIdList);
            Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
            return userExtSplitMap.values().stream().filter(s -> s.getServiceScore() == null || s.getServiceScore() == 0).count();
        }
        if (serviceType == ChipsActiveServiceType.RENEWREMIND) {
            return renewUser(classId).size();
        }
        return 0;
    }

    @Override
    public Map<String, Integer> obtainAllActiveServiceRemained(Long clazzId) {
        if (clazzId == 0L) {
            return Collections.emptyMap();
        }
        List<Long> userIdList = chipsEnglishClazzService.selectAllUserByClazzId(clazzId);
        // 过滤测试账号
        userIdList = filterTestFefundNotAddWx(userIdList);
        List<ChipsActiveServiceRecord> records = chipsActiveServiceRecordDao.loadByClazzId(clazzId);
        Map<String, List<ChipsActiveServiceRecord>> recordsMap = records.stream()
                .collect(Collectors.groupingBy(ChipsActiveServiceRecord::getServiceType, Collectors.toList()));

        int serviceC = 0;
        if (CollectionUtils.isNotEmpty(recordsMap.get(ChipsActiveServiceType.SERVICE.name()))) {
            serviceC = (int) recordsMap.get(ChipsActiveServiceType.SERVICE.name()).stream().filter(r -> !r.getServiced()).count();
        }
        int remindC = 0;
        if (CollectionUtils.isNotEmpty(recordsMap.get(ChipsActiveServiceType.REMIND.name()))) {
            remindC = (int) recordsMap.get(ChipsActiveServiceType.REMIND.name()).stream().filter(r -> !r.getServiced()).count();
        }
        Map<Long, Boolean> map = registeredInWeChatSubscription(userIdList);
        int bindC = (int) map.values().stream().filter(f -> !f).count();

        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishClazzService.loadChipsEnglishUserExtSplitByUserIds(userIdList);
        int instructionC = (int) userExtSplitMap.values().stream().filter(s -> s.getServiceScore() == null || s.getServiceScore() == 0).count();

        int renewC = renewUser(clazzId).size();

        Map<String, Integer> result = new HashMap<>();
        result.put(ChipsActiveServiceType.SERVICE.name(), serviceC);
        result.put(ChipsActiveServiceType.REMIND.name(), remindC);
        result.put(ChipsActiveServiceType.BINDING.name(), bindC);
        result.put(ChipsActiveServiceType.USEINSTRUCTION.name(), instructionC);
        result.put(ChipsActiveServiceType.RENEWREMIND.name(), renewC);
        return result;
    }

    private AIUserQuestionResultCollection loadAIUserQuestionResultCollection(String aid) {
        if (StringUtils.isBlank(aid)) {
            return null;
        }
        return aiUserQuestionResultCollectionDao.load(aid);
    }

    public Set<String> filterKeywordByAudioToWords(Set<String> keySet, Set<String> audioToWords) {
        return keySet.stream().filter(e -> !audioToWords.contains(e)).collect(Collectors.toSet());
    }

    private String audioToSentence(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String api = RuntimeMode.lt(Mode.STAGING) ? API_TEST : API_PRODUCTION;
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(url).execute();
        byte[] bytes = execute.getOriginalResponse();
        Map<String, String> hs = new HashMap<>();
        hs.put("appkey", APP_KEY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("voice", bytes, ContentType.MULTIPART_FORM_DATA, "voice");
        builder.addTextBody("codec", "mp3");
        AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance()
                .post(api).headers(hs).entity(builder.build())
                .execute();
        String result = resp.getResponseString();
        if (StringUtils.isBlank(result)) {
            return null;
        }
        Map<String, Object> map = JsonUtils.fromJson(resp.getResponseString());
        Object rec_text = map.get("rec_text");
        if (rec_text == null) {
            return null;
        }
        return rec_text.toString().trim();
    }

    /**
     * 句子转成单词，单词toLowerCase
     *
     * @param sentence
     * @return
     */
    private Set<String> sentenceToWords(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        String[] split = sentence.split(" ");
        for (String str : split) {
            if (StringUtils.isBlank(str)) {
                continue;
            }
            set.add(str.trim().toLowerCase());
        }
        return set;
    }

    @Override
    public MapMessage loadOtherServiceRenewUserData(long userId, long clazzId) {
        ChipsOtherServiceUserTemplate userTemplate = chipsOtherServiceUserTemplateDao.load(ChipsActiveServiceType.RENEWREMIND.name() + "-" + userId);
        if (userTemplate != null && StringUtils.isNotBlank(userTemplate.getTemplateId())) {
            MapMessage message = MapMessage.successMessage();
            ChipsOtherServiceTemplate template = chipsOtherServiceTemplateDao.load(userTemplate.getTemplateId());
            if (userTemplate.getTemplateId().contains("first")) {
                String bookId = getBookId(chipsEnglishClassPersistence.load(clazzId));
                String json = replaceRenewRemindJson(template.getJson(), userId, clazzId);
                RenewFirstPojo pojo = JsonUtils.fromJson(json, RenewFirstPojo.class);
                List<RenewFirstPojo.WeekPoint> weekPointList = handleRenewFirstPojoWeekPoint(pojo, userId, clazzId, bookId);
                weekPointList.stream().sorted(Comparator.comparing(RenewFirstPojo.WeekPoint::getWeekPointLevel));
                if (CollectionUtils.isNotEmpty(weekPointList)) {
                    pojo.setWeekPointList(Collections.singletonList(weekPointList.get(0)));
                } else {
                    pojo.setWeekPointList(Collections.emptyList());
                }
                message.add("pojo", pojo);
                message.add("bookId", bookId);
                List<Map<String, String>> userScoreList = userScoreList(clazzId, userId);
                message.add("userScoreList", userScoreList);
            } else {
                String json = replaceRenewRemindJson(template.getJson(), userId, clazzId);
                RenewFollowUpPojo pojo = JsonUtils.fromJson(json, RenewFollowUpPojo.class);
                message.add("pojo", pojo);
            }
            message.add("commonTemplateId", userTemplate.getTemplateId());
            message.add("userName", obtainUserName(userId, false));
            return message;
        }
        return MapMessage.errorMessage().add("info", "user template not exist : " + userId);
    }

    private  List<RenewFirstPojo.WeekPoint> handleRenewFirstPojoWeekPoint(RenewFirstPojo pojo, Long userId, Long clazzId,String bookId) {
        if (pojo == null || CollectionUtils.isEmpty(pojo.getWeekPointList())) {
            return Collections.emptyList();
        }
        List<RenewFirstPojo.WeekPoint> defaultList = pojo.getWeekPointList().stream().filter(e -> e.getWeekPointName().equals(UGCWeekPointsEnum.CS.name())).collect(Collectors.toList());
        Set<String> ugcWPSet = weekPointFromUgc(userId);
        List<RenewFirstPojo.WeekPoint> weekPointList = pojo.getWeekPointList();
        List<RenewFirstPojo.WeekPoint> ugcFilterList = weekPointList.stream().filter(e -> ugcWPSet.contains(e.getWeekPointDesc())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ugcFilterList)) {
           return ugcFilterList;
        }
        if (StringUtils.isBlank(bookId)) {
            return defaultList;
        }
        Set<String> wpSet = weekPointFromAIUserBookResult(userId, bookId);
        List<RenewFirstPojo.WeekPoint> wpList = weekPointList.stream().filter(e -> wpSet.contains(e.getWeekPointName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(wpList)) {
            return defaultList;
        }
        return wpList;
    }


    private  List<RenewV1Pojo.WeekPoint> handleRenewV1tPojoWeekPoint(RenewV1Pojo pojo, Long userId,String bookId) {
        if (pojo == null || CollectionUtils.isEmpty(pojo.getWeekPointList())) {
            return Collections.emptyList();
        }
        List<RenewV1Pojo.WeekPoint> defaultList = pojo.getWeekPointList().stream().filter(e -> e.getWeekPointName().equals(UGCWeekPointsEnum.CS.name())).collect(Collectors.toList());
        Set<String> ugcWPSet = weekPointFromUgc(userId);
        List<RenewV1Pojo.WeekPoint> weekPointList = pojo.getWeekPointList();
//        return weekPointList;
        List<RenewV1Pojo.WeekPoint> ugcFilterList = weekPointList.stream().filter(e -> ugcWPSet.contains(e.getWeekPointDesc())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ugcFilterList)) {
            return ugcFilterList;
        }
        if (StringUtils.isBlank(bookId)) {
            return defaultList;
        }
        Set<String> wpSet = weekPointFromAIUserBookResult(userId, bookId);
        List<RenewV1Pojo.WeekPoint> wpList = weekPointList.stream().filter(e -> wpSet.contains(e.getWeekPointName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(wpList)) {
            return defaultList;
        }
        return wpList;
    }


    /**
     * 调查问卷中填写的薄弱点
     * @param userId
     * @return
     */
    private Set<String> weekPointFromUgc(Long userId) {
        return Optional.ofNullable(chipsEnglishUserExtSplitDao.load(userId)).filter(e -> StringUtils.isNotBlank(e.getExpect())).map(ChipsEnglishUserExtSplit::getExpect)
                .map(e -> Arrays.stream(e.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    /**
     * 从定级报告中获取薄弱点
     * @param userId
     * @param bookId
     * @return  CS,G,L,P 的子集
     */
    private Set<String> weekPointFromAIUserBookResult(Long userId, String bookId) {
        return Optional.ofNullable(aiUserBookResultDao.load(AIUserBookResult.generateId(userId, bookId)))
                .map(AIUserBookResult::getDiaglogue).map(e -> e.stream().map(w -> w.name()).collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public MapMessage loadGradeReport(Long userId, String bookId) {
        List<UGCWeekPointsEnum> wpList = handleWeekPoint(userId, bookId);
        wpList.stream().sorted(Comparator.comparing(UGCWeekPointsEnum::getLevel));
        String wpText;
        if (CollectionUtils.isNotEmpty(wpList)) {
            wpText = wpList.get(0).getDesc();
        }  else {
            wpText = "";
        }
        String level = Optional.ofNullable(aiUserBookResultDao.load(AIUserBookResult.generateId(userId, bookId)))
                .map(AIUserBookResult::getLevel).map(AIUserBookResult.Level::getDescription).orElse("");

        MapMessage message = MapMessage.successMessage();
        message.add("sharePrimeTitle", obtainUserName(userId, false) + "同学的定级报告");
        message.add("shareSubTitle", "学习薄弱环节出现在" + wpText + "方面，建议学习" + level + "的课程");
        return message;
    }

    private  List<UGCWeekPointsEnum> handleWeekPoint(Long userId,String bookId) {
        List<UGCWeekPointsEnum> defaultList = Collections.singletonList(UGCWeekPointsEnum.CS);
        Set<String> ugcWPSet = weekPointFromUgc(userId);
        List<UGCWeekPointsEnum> ugcFilterList = Arrays.stream(UGCWeekPointsEnum.values()).filter(e -> ugcWPSet.contains(e.getDesc())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ugcFilterList)) {
            return ugcFilterList;
        }
        if (StringUtils.isBlank(bookId)) {
            return defaultList;
        }
        Set<String> wpSet = weekPointFromAIUserBookResult(userId, bookId);
        List<UGCWeekPointsEnum> wpList = Arrays.stream(UGCWeekPointsEnum.values()).filter(e -> wpSet.contains(e.getDesc())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(wpList)) {
            return defaultList;
        }
        return wpList;
    }

    @Override
    public List<ChipsActiveServiceRecord> loadChipsActiveServiceRecord(ChipsActiveServiceType serviceType, Date beginDate) {
        return chipsActiveServiceRecordDao.loadByServiceTypeDate(serviceType, beginDate);
    }

}
