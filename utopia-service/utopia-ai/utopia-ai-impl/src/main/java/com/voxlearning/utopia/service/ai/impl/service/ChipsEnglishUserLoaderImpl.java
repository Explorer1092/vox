package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.apache.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuan.zhu
 * @date 2018/8/23 20:16
 * 薯条英语用户 service 实现
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = ChipsEnglishUserLoader.class, version = @ServiceVersion(version = "20190222")),
        @ExposeService(interfaceClass = ChipsEnglishUserLoader.class, version = @ServiceVersion(version = "20190315"))
})
public class ChipsEnglishUserLoaderImpl extends AbstractAiSupport implements ChipsEnglishUserLoader {

    private static final int pageSize = 20;

    //至少包含一个英文字母的正则
    public static final String REGEX = ".*[a-zA-Z]+.*";

    @Inject
    private ChipsEnglishUserExtDao chipsEnglishUserExtDao;
    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;
    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;
    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;
    @Inject
    private ChipEnglishInvitationPersistence chipEnglishInvitationPersistence;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private ChipsEnglishUserUnitResultOperationLogDao chipsEnglishUserUnitResultOperationLogDao;
    @Inject
    private AIDialogueLessonConfigDao aiDialogueLessonConfigDao;
    @Inject
    private AIDialogueTaskConfigDao aiDialogueTaskConfigDao;
    @Inject
    private ActiveServiceUserQuestionTemplateDao activeServiceUserQuestionTemplateDao;
    @Inject
    private ChipsEnglishContentLoaderImpl chipsEnglishContentLoaderImpl;
    @Inject
    private ActiveServiceUserTemplateDao activeServiceUserTemplateDao;

    @Override
    public MapMessage loadSimpleUserByClassId(String productId, Long classId, String excludeUserId, int minCost, int maxCost, int pageNumber) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<AiUserInfoSimple> result = new ArrayList<>();
        mapMessage.put("userList", result);
        List<ChipsEnglishClass> chipsEnglishClassList = chipsEnglishClassPersistence.loadByProductId(productId);
        if (CollectionUtils.isNotEmpty(chipsEnglishClassList)) {
            chipsEnglishClassList
                    .stream()
                    .filter(c -> classId == 0L || Objects.equals(c.getId(), classId))
                    .forEach(c -> {
                        Long cId = c.getId();
                        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByClassId(cId);

                        if (CollectionUtils.isNotEmpty(userRefList)) {
                            // 用户 id 过滤
                            List<Long> userIds = userRefList
                                    .stream()
                                    .map(u -> u.getUserId())
                                    .filter(uId -> {
                                        if (excludeUserId != null && excludeUserId.trim().equals("*")) {
                                            return true;
                                        } else if ("".equals(excludeUserId)) {
                                            return true;
                                        } else {
                                            return excludeUserId.equals(uId.toString());
                                        }
                                    })
                                    .collect(Collectors.toList());

                            if (CollectionUtils.isEmpty(userIds)) {
                                return;
                            }

                            // 家长通消费过滤
                            Map<Long, ChipsEnglishUserExt> chipsEnglishUserExtMap = chipsEnglishUserExtDao.loads(userIds);
                            userIds = chipsEnglishUserExtMap.values()
                                    .stream()
                                    .filter(chipsEnglishUserExt -> {
                                        Double jztConsume = chipsEnglishUserExt.getJztConsume() == null ? 0.0D : chipsEnglishUserExt.getJztConsume().doubleValue();

                                        if (minCost == maxCost && minCost != 0) {
                                            return jztConsume == minCost;
                                        } else if (minCost < maxCost) {
                                            return jztConsume >= minCost && jztConsume <= maxCost;
                                        } else if (minCost > maxCost && maxCost == 0) {
                                            return jztConsume >= minCost;
                                        } else {
                                            return true;
                                        }
                                    })
                                    .map(ChipsEnglishUserExt::getId)
                                    .collect(Collectors.toList());

                            if (CollectionUtils.isEmpty(userIds)) {
                                return;
                            }

                            // 分页
                            Pageable pageable = new PageRequest(pageNumber - 1, pageSize);
                            Page<Long> pageData = PageableUtils.listToPage(userIds, pageable);

                            userIds = pageData.getContent();

                            if (CollectionUtils.isEmpty(userIds)) {
                                return;
                            }

                            mapMessage.put("totalPages", pageData.getTotalPages());
                            mapMessage.put("currentPage", pageNumber);

                            Map<Long, String> userNameMap = obtainUserNameBatch(userIds, false);

                            userIds.forEach(userId -> {
                                ChipsEnglishUserExt chipsEnglishUserExt = chipsEnglishUserExtMap.get(userId);
                                if (chipsEnglishUserExt != null) {
                                    AiUserInfoSimple aiUserInfoSimple = new AiUserInfoSimple();
                                    aiUserInfoSimple.setId(userId);
                                    aiUserInfoSimple.setName(userNameMap.get(userId));
                                    aiUserInfoSimple.setClassName(c.getName());
                                    aiUserInfoSimple.setBuyTimes(obtainBuyTimes(userId));
                                    aiUserInfoSimple.setJztConsume(chipsEnglishUserExt.getJztConsume());
                                    result.add(aiUserInfoSimple);
                                }
                            });
                        }
                    });
        }
        return mapMessage;
    }

    @Override
    public AIUserInfoDetail loadUserDetailByUserId(Long userId, String operator) {
        ChipsEnglishUserExt chipsEnglishUserExt = Optional.ofNullable(chipsEnglishUserExtDao.load(userId))
                .orElse(null);

        AIUserInfoDetail aiUserInfoDetail = new AIUserInfoDetail();
        aiUserInfoDetail.setId(userId);
        if (chipsEnglishUserExt != null) {
            aiUserInfoDetail.setName(obtainUserName(userId, false));
            aiUserInfoDetail.setPhone(obtainUserPhone(userId, operator));
            aiUserInfoDetail.setProvince(chipsEnglishUserExt.getProvince());
            aiUserInfoDetail.setChipsConsume(chipsEnglishUserExt.getChipsConsume() == null ? BigDecimal.valueOf(0.0D) : chipsEnglishUserExt.getChipsConsume());
            aiUserInfoDetail.setJztConsume(chipsEnglishUserExt.getJztConsume() == null ? BigDecimal.valueOf(0.0D) : chipsEnglishUserExt.getJztConsume());
            aiUserInfoDetail.setLastActive(chipsEnglishUserExt.getLastActive());
            aiUserInfoDetail.setBuyTimes(obtainBuyTimes(userId));
            aiUserInfoDetail.setSuccessfulRecommendTimes(obtainSuccessfulRecommendTimes(userId));
        }

        ChipsEnglishUserExtSplit chipsEnglishUserExtSplit = Optional.ofNullable(chipsEnglishUserExtSplitDao.load(userId))
                .orElse(null);
        if (chipsEnglishUserExtSplit != null) {
            aiUserInfoDetail.setWxCode(chipsEnglishUserExtSplit.getWxCode());
            aiUserInfoDetail.setStudyDuration(chipsEnglishUserExtSplit.getStudyDuration());
            aiUserInfoDetail.setBuyCompetitor(chipsEnglishUserExtSplit.getBuyCompetitor());
            aiUserInfoDetail.setShowPlay(chipsEnglishUserExtSplit.getShowPlay());
            aiUserInfoDetail.setLevel(chipsEnglishUserExtSplit.getLevel());
        }

        List<String> productIds = new ArrayList<>(chipsUserService.loadUserBoughtProduct(userId));

        List<OrderProduct> userBoughtProducts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productIds)) {
            Map<String, OrderProduct> productMap = userOrderLoaderClient.loadOrderProducts(productIds);
            productIds.forEach(productId -> {
                OrderProduct orderProduct = productMap.get(productId);
                if (orderProduct != null) {
                    userBoughtProducts.add(orderProduct);
                }
            });
        }

        aiUserInfoDetail.setUserBoughtProducts(userBoughtProducts);

        return aiUserInfoDetail;


    }

    @Override
    public Map<Long, List<AIUserInfoWithScore>> loadClassSingleUserInfoWithScore(Long classId, List<Long> userIds) {
        Map<Long, List<AIUserInfoWithScore>> result = new HashMap<>();

        ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(classId);
        String productId = chipsEnglishClass.getProductId();
        // 产品所有的 item
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);

        new HashSet<>(userIds).forEach(userId -> {
            List<AIUserInfoWithScore> scoreInfoList = new ArrayList<>();


            Map<OrderProductItem, List<AIUserUnitScore>> scoreListMap = loadUserScoreList(userId, orderProductItemList);

            ChipsEnglishUserExtSplit chipsEnglishUserExtSplit = chipsEnglishUserExtSplitDao.load(userId);
            ChipsEnglishLevel level = chipsEnglishUserExtSplit == null ? null : chipsEnglishUserExtSplit.getLevel();
            Boolean showPlay = chipsEnglishUserExtSplit == null ? Boolean.FALSE : chipsEnglishUserExtSplit.getShowPlay();


            scoreInfoList.addAll(convertToAIUserInfoWithScoreList(userId, scoreListMap,
                    level, showPlay, obtainUserName(userId, false), orderProduct));

            result.put(userId, scoreInfoList);
        });

        return result;
    }

    @Override
    public List<AIUserInfoWithScore> loadUserAllClassScore(Long userId, String productId) {
        List<AIUserInfoWithScore> result = new ArrayList<>();

        ChipsEnglishUserExtSplit chipsEnglishUserExtSplit = chipsEnglishUserExtSplitDao.load(userId);
        ChipsEnglishLevel level = chipsEnglishUserExtSplit == null ? null : chipsEnglishUserExtSplit.getLevel();
        Boolean showPlay = chipsEnglishUserExtSplit == null ? Boolean.FALSE : chipsEnglishUserExtSplit.getShowPlay();
        String userName = obtainUserName(userId, false);

        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
        if (orderProduct == null) {
            return result;
        }

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(productId);
        Map<OrderProductItem, List<AIUserUnitScore>> scoreListMap = loadUserScoreList(userId, orderProductItemList, timetable);
        result.addAll(convertToAIUserInfoWithScoreList(userId, scoreListMap, level, showPlay, userName, orderProduct));

        return result;
    }

    @Override
    public List<AiUserOperationInfo> loadUserOperationInfoList(Long userId) {
        List<AiUserOperationInfo> result = new ArrayList<>();
        List<ChipsEnglishClassUserRef> chipsEnglishClassUserRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(chipsEnglishClassUserRefList)) {
            return result;
        }

        ChipsEnglishUserExt chipsEnglishUserExt = Optional.ofNullable(chipsEnglishUserExtDao.load(userId))
                .orElse(null);
        if (chipsEnglishUserExt == null) {
            return result;
        }
        String userName = obtainUserName(userId, false);

        chipsEnglishClassUserRefList.forEach(e -> {
            AiUserOperationInfo aiUserOperationInfo = new AiUserOperationInfo();
            aiUserOperationInfo.setId(userId);
            aiUserOperationInfo.setName(userName);

            ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(e.getChipsClassId());
            aiUserOperationInfo.setClassName(chipsEnglishClass.getName());

            String productId = chipsEnglishClass.getProductId();
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            aiUserOperationInfo.setProductName(orderProduct.getName());

            List<ChipsUserCourse> courseList = chipsUserCoursePersistence.loadByUserId(userId);
            if (CollectionUtils.isEmpty(courseList)) {
                return;
            }
            ChipsUserCourse course = courseList.stream().filter(c -> c.getProductId().equals(productId)).findFirst().orElse(null);
            if (course == null) {
                return;
            }
            aiUserOperationInfo.setRegisterDate(course.getCreateTime() != null ? course.getCreateTime().getTime() : -1);

            aiUserOperationInfo.setInGroup(e.getInGroup());
            aiUserOperationInfo.setOrderRef(e.getOrderRef());
            aiUserOperationInfo.setQuestionnaires(e.getQuestionnaires());
            result.add(aiUserOperationInfo);
        });


        return result;
    }

    @Override
    public void transferUserExtToUserExtSplit() {
        List<ChipsEnglishUserExt> chipsEnglishUserExtList = chipsEnglishUserExtDao.query();
        chipsEnglishUserExtList.forEach(ext -> {
            ChipsEnglishUserExtSplit userExtSplit = new ChipsEnglishUserExtSplit();
            userExtSplit.setId(ext.getId());
            userExtSplit.setWxCode(ext.getWxCode() != null ? ext.getWxCode() : "");
            userExtSplit.setStudyDuration(ext.getStudyDuration() != null ? ext.getStudyDuration() : "");
            userExtSplit.setBuyCompetitor(ext.getBuyCompetitor() != null ? ext.getBuyCompetitor() : Boolean.FALSE);
            userExtSplit.setShowPlay(ext.getShowPlay() != null ? ext.getShowPlay() : Boolean.FALSE);
            userExtSplit.setLevel(ext.getLevel());
            userExtSplit.setCreateTime(new Date());
            userExtSplit.setUpdateTime(new Date());
            chipsEnglishUserExtSplitDao.upsert(userExtSplit);
        });
    }

    @Override
    public void editUnitResultOperationLog(Long userId, String unitId, String operationLog) {
        this.chipsEnglishUserUnitResultOperationLogDao.upsert(userId, unitId, operationLog);
    }

    @Override
    public Map<Long, List<ScoreSimpleInfo>> loadUserResultSimpleInfo(String productId, List<Long> userIdList) {
        Map<Long, List<ScoreSimpleInfo>> result = new HashMap<>();
        if (userIdList == null || userIdList.size() <= 0) {
            return result;
        }
        // 产品所有的 item
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);

        //计算完成度与最新一次的成绩
        if (orderProductItemList != null) {
            orderProductItemList.forEach(orderProductItem -> {
                String bookId = orderProductItem.getAppItemId();

                // 课表
                List<ChipsEnglishProductTimetable.Course> courses = new ArrayList<>();
                ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(productId);
                if (timetable != null) {
                    courses = timetable.getCourses();
                    if (CollectionUtils.isEmpty(courses)) {
                        courses = new ArrayList<>();
                    }
                }

                // 今日时间
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                today = calendar.getTime();

                // 获取上一个课程
                ChipsEnglishProductTimetable.Course course = null;
                for (int i = 0; i < courses.size(); i++) {
                    if (today.before(courses.get(i).getBeginDate())) {
                        break;
                    }
                    course = courses.get(i);
                }

                if (course == null && courses.size() > 0) {
                    course = courses.get(courses.size() - 1);
                }

                ChipsEnglishProductTimetable.Course finalCourse = course;
                userIdList.forEach(userId -> {
                    List<ScoreSimpleInfo> scoreSimpleInfos = result.computeIfAbsent(userId, id -> new ArrayList<>());
                    //成绩
                    Map<String, AIUserUnitResultHistory> aiUserUnitResultHistoryMap = new HashMap<>();
                    List<StoneUnitData> unitDataList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
                    aiUserUnitResultHistoryDao.loadByUserId(userId)
                            .stream()
                            .filter(r -> bookId.equals(r.getBookId())).collect(Collectors.toList())
                            .forEach(aiUserUnitResultHistory ->
                                    aiUserUnitResultHistoryMap.put(aiUserUnitResultHistory.getUnitId(), aiUserUnitResultHistory));

                    ScoreSimpleInfo scoreSimpleInfo = new ScoreSimpleInfo();
                    scoreSimpleInfo.setBookId(bookId);
                    scoreSimpleInfo.setTotalNum(unitDataList.size());
                    scoreSimpleInfo.setBookName(orderProductItem.getName());

                    long finishedNum = unitDataList.stream()
                            .map(unitData -> aiUserUnitResultHistoryMap.get(unitData.getId()))
                            .filter(history -> history != null)
                            .filter(history -> {
                                Boolean finished = history.getFinished();
                                return finished != null && finished && history.getScore() != null;
                            }).count();
                    scoreSimpleInfo.setFinishedNum(finishedNum);

                    // 上一次课程分数
                    int recentlyScore = -1;
                    String recentlyUnitId = null;
                    if (finalCourse != null) {
                        AIUserUnitResultHistory recentlyAiUserUnitResultHistory = aiUserUnitResultHistoryMap.get(finalCourse.getUnitId());
                        if (finalCourse != null && recentlyAiUserUnitResultHistory != null) {
                            Boolean finished = recentlyAiUserUnitResultHistory.getFinished();
                            if (finished != null && finished && recentlyAiUserUnitResultHistory.getScore() != null) {
                                recentlyScore = recentlyAiUserUnitResultHistory.getScore();
                                recentlyUnitId = recentlyAiUserUnitResultHistory.getUnitId();
                            }
                        }
                    }
                    scoreSimpleInfo.setRecentlyScore(recentlyScore);
                    if (recentlyUnitId != null) {
                        scoreSimpleInfo.setRecentlyUnitId(recentlyUnitId);
                    }

                    scoreSimpleInfos.add(scoreSimpleInfo);

                });

            });
        }
        return result;
    }


    /**
     * 获取用户购课次数
     */
    private int obtainBuyTimes(Long userId) {
        Set<String> productIds = chipsUserService.loadUserBoughtProduct(userId);
        return CollectionUtils.isEmpty(productIds) ? 0 : productIds.size();
    }

    /**
     * 获取成功推荐的次数
     */
    private int obtainSuccessfulRecommendTimes(Long userId) {
        List<ChipEnglishInvitation> chipEnglishInvitationList = chipEnglishInvitationPersistence.loadByInviterId(userId);
        return chipEnglishInvitationList != null ? chipEnglishInvitationList.size() : 0;
    }

    /**
     * 获取用户姓名
     *
     * @param isReal true: 获取真实姓名  false：获取nickName
     * @return userName
     */
    private String obtainUserName(Long userId, boolean isReal) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return "";
        }
        if (isReal) {
            return user.fetchRealname();
        } else {
            UserProfile userProfile = user.getProfile();
            return userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "";
        }
    }

    /**
     * 批量回去用户名称
     *
     * @return userId -> userName
     */
    private Map<Long, String> obtainUserNameBatch(List<Long> userIds, boolean isReal) {
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);
        Map<Long, String> result = new HashMap<>();
        if (userMap == null) {
            return result;
        }
        userMap.forEach((userId, user) -> {
            if (isReal) {
                result.put(userId, user.fetchRealname());
            } else {
                UserProfile userProfile = user.getProfile();
                result.put(userId, userProfile == null ? "" : userProfile.getNickName() != null ? userProfile.getNickName() : "");
            }
        });

        return result;
    }

    /**
     * 获取用户电话号码
     */
    private String obtainUserPhone(Long userId, String operator) {
        User user = userLoaderClient.loadUserIncludeDisabled(userId);
        String phone = "";
        if (user != null) {
            phone = sensitiveUserDataServiceClient.showUserMobile(userId, "CrmAccountCtl:getUserPhone", operator);
        }
        return phone;
    }

    /**
     * 加载用户某个产品下的全部课程分数信息（由于产品可以打包出售，故一个产品下有多个课程）
     */
    private Map<OrderProductItem, List<AIUserUnitScore>> loadUserScoreList(Long userId, List<OrderProductItem> orderProductItemList) {
        Map<OrderProductItem, List<AIUserUnitScore>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            return map;
        }

        Map<String, String> operationLogMap = Optional.ofNullable(chipsEnglishUserUnitResultOperationLogDao.loadByUserId(userId))
                .map(l -> l.stream().collect(Collectors.toMap(ChipsEnglishUserUnitResultOperationLog::getUnitId, ChipsEnglishUserUnitResultOperationLog::getOperationLog)))
                .orElse(new HashMap<>());

        orderProductItemList
                .forEach(orderProductItem -> {
                    List<AIUserUnitScore> aiUserScores = new ArrayList<>();

                    String bookId = orderProductItem.getAppItemId();

                    //成绩
                    Map<String, AIUserUnitResultHistory> aiUserUnitResultHistoryMap = new HashMap<>();
                    aiUserUnitResultHistoryDao.loadByUserId(userId)
                            .stream()
                            .filter(r -> bookId.equals(r.getBookId())).collect(Collectors.toList())
                            .forEach(aiUserUnitResultHistory ->
                                    aiUserUnitResultHistoryMap.put(aiUserUnitResultHistory.getUnitId(), aiUserUnitResultHistory));

                    List<StoneUnitData> unitDataList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
                    if (unitDataList != null && unitDataList.size() > 0) {
                        for (int i = 0; i < unitDataList.size(); i++) {
                            StoneUnitData unitData = unitDataList.get(i);
                            AIUserUnitScore aiUserUnitScore = new AIUserUnitScore();

                            AIUserUnitResultHistory aiUserUnitResultHistory =
                                    aiUserUnitResultHistoryDao == null ? null : aiUserUnitResultHistoryMap.get(unitData.getId());
                            if (aiUserUnitResultHistory == null) {
                                aiUserUnitScore.setScore(-1);
                                aiUserUnitScore.setFinished(Boolean.FALSE);
                            } else {
                                Boolean finished = aiUserUnitResultHistory.getFinished();
                                if (finished != null && finished) {
                                    aiUserUnitScore.setScore(aiUserUnitResultHistory.getScore());
                                } else {
                                    aiUserUnitScore.setScore(-1);
                                }
                                aiUserUnitScore.setFinished(finished);
                            }

                            aiUserUnitScore.setName(unitData.getJsonData().getName());
                            aiUserUnitScore.setRank(i);
                            aiUserUnitScore.setUnitId(unitData.getId());
                            aiUserUnitScore.setOperationLog(operationLogMap.getOrDefault(unitData.getId(), ""));
                            aiUserScores.add(aiUserUnitScore);
                        }
                        map.put(orderProductItem, aiUserScores);
                    } else {
                        // 如果是老课本
                        List<NewBookCatalog> newBookCatalogList = fetchUnitListExcludeTrial(bookId);
                        if (CollectionUtils.isEmpty(newBookCatalogList)) {
                            newBookCatalogList
                                    .stream()
                                    .sorted(Comparator.comparingInt(NewBookCatalog::getRank))
                                    .forEach(newBookCatalog -> {

                                        AIUserUnitScore aiUserUnitScore = new AIUserUnitScore();

                                        AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao == null ? null : aiUserUnitResultHistoryMap.get(newBookCatalog.unitId());
                                        if (aiUserUnitResultHistory == null) {
                                            aiUserUnitScore.setScore(-1);
                                            aiUserUnitScore.setFinished(Boolean.FALSE);
                                        } else {
                                            Integer score = aiUserUnitResultHistory.getFinished() ? aiUserUnitResultHistory.getScore() : -1;
                                            aiUserUnitScore.setScore(score);
                                            aiUserUnitScore.setFinished(aiUserUnitResultHistory.getFinished());
                                        }

                                        aiUserUnitScore.setName(newBookCatalog.getName());
                                        aiUserUnitScore.setRank(newBookCatalog.getRank());
                                        aiUserUnitScore.setUnitId(newBookCatalog.unitId());
                                        aiUserUnitScore.setOperationLog(operationLogMap.getOrDefault(newBookCatalog.unitId(), ""));
                                        aiUserScores.add(aiUserUnitScore);
                                    });

                            map.put(orderProductItem, aiUserScores);
                        }
                    }

                });

        return map;
    }


    /**
     * 加载用户某个产品下的全部课程分数信息（由于产品可以打包出售，故一个产品下有多个课程）
     */
    private Map<OrderProductItem, List<AIUserUnitScore>> loadUserScoreList(Long userId, List<OrderProductItem> orderProductItemList,
                                                                           ChipsEnglishProductTimetable timetable) {
        Map<OrderProductItem, List<AIUserUnitScore>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            return map;
        }

        List<ChipsEnglishProductTimetable.Course> courses = null;
        if (timetable != null) {
            courses = timetable.getCourses();
        }

        if (CollectionUtils.isEmpty(courses)) {
            courses = new ArrayList<>();
        }

        // 单元开课日期
        Map<String, Date> unitOpenDateMap = courses
                .stream()
                .collect(Collectors.toMap(e -> e.getBookId() + "-" + e.getUnitId(), e -> e.getBeginDate(), (k1, k2) -> k1));


        Map<String, String> operationLogMap = Optional.ofNullable(chipsEnglishUserUnitResultOperationLogDao.loadByUserId(userId))
                .map(l -> l.stream().collect(Collectors.toMap(ChipsEnglishUserUnitResultOperationLog::getUnitId, ChipsEnglishUserUnitResultOperationLog::getOperationLog)))
                .orElse(new HashMap<>());

        orderProductItemList
                .forEach((OrderProductItem orderProductItem) -> {
                    List<AIUserUnitScore> aiUserScores = new ArrayList<>();

                    String bookId = orderProductItem.getAppItemId();

                    //成绩
                    Map<String, AIUserUnitResultHistory> aiUserUnitResultHistoryMap = new HashMap<>();
                    aiUserUnitResultHistoryDao.loadByUserId(userId)
                            .stream()
                            .filter(r -> bookId.equals(r.getBookId())).collect(Collectors.toList())
                            .forEach(aiUserUnitResultHistory ->
                                    aiUserUnitResultHistoryMap.put(aiUserUnitResultHistory.getUnitId(), aiUserUnitResultHistory));

                    List<StoneUnitData> unitDataList = chipCourseSupport.fetchUnitListExcludeTrialV2(bookId);
                    if (unitDataList != null && unitDataList.size() > 0) {
                        for (int i = 0; i < unitDataList.size(); i++) {
                            StoneUnitData unitData = unitDataList.get(i);
                            AIUserUnitScore aiUserUnitScore = new AIUserUnitScore();

                            AIUserUnitResultHistory aiUserUnitResultHistory =
                                    aiUserUnitResultHistoryDao == null ? null : aiUserUnitResultHistoryMap.get(unitData.getId());
                            if (aiUserUnitResultHistory == null) {
                                aiUserUnitScore.setScore(-1);
                                aiUserUnitScore.setFinished(Boolean.FALSE);
                            } else {
                                Boolean finished = aiUserUnitResultHistory.getFinished();
                                if (finished != null && finished) {
                                    aiUserUnitScore.setScore(aiUserUnitResultHistory.getScore());
                                } else {
                                    aiUserUnitScore.setScore(-1);
                                }
                                aiUserUnitScore.setFinished(finished);
                            }

                            aiUserUnitScore.setName(unitData.getJsonData().getName());
                            aiUserUnitScore.setRank(i);
                            aiUserUnitScore.setUnitId(unitData.getId());
                            aiUserUnitScore.setOperationLog(operationLogMap.getOrDefault(unitData.getId(), ""));
                            aiUserUnitScore.setOpenDate(unitOpenDateMap.getOrDefault(bookId + "-" + unitData.getId(), new Date()));
                            aiUserScores.add(aiUserUnitScore);
                        }
                        map.put(orderProductItem, aiUserScores);
                    } else {
                        // 如果是老课本
                        List<NewBookCatalog> newBookCatalogList = fetchUnitListExcludeTrial(bookId);
                        if (CollectionUtils.isEmpty(newBookCatalogList)) {
                            newBookCatalogList
                                    .stream()
                                    .sorted(Comparator.comparingInt(NewBookCatalog::getRank))
                                    .forEach(newBookCatalog -> {

                                        AIUserUnitScore aiUserUnitScore = new AIUserUnitScore();

                                        AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao == null ? null : aiUserUnitResultHistoryMap.get(newBookCatalog.unitId());
                                        if (aiUserUnitResultHistory == null) {
                                            aiUserUnitScore.setScore(-1);
                                            aiUserUnitScore.setFinished(Boolean.FALSE);
                                        } else {
                                            Integer score = aiUserUnitResultHistory.getFinished() ? aiUserUnitResultHistory.getScore() : -1;
                                            aiUserUnitScore.setScore(score);
                                            aiUserUnitScore.setFinished(aiUserUnitResultHistory.getFinished());
                                        }

                                        aiUserUnitScore.setName(newBookCatalog.getName());
                                        aiUserUnitScore.setRank(newBookCatalog.getRank());
                                        aiUserUnitScore.setUnitId(newBookCatalog.unitId());
                                        aiUserUnitScore.setOperationLog(operationLogMap.getOrDefault(newBookCatalog.unitId(), ""));
                                        aiUserUnitScore.setOpenDate(unitOpenDateMap.getOrDefault(bookId + "-" + newBookCatalog.getId(), new Date()));
                                        aiUserScores.add(aiUserUnitScore);
                                    });

                            map.put(orderProductItem, aiUserScores);
                        }
                    }

                });

        return map;
    }

    /**
     * 转换为用户分数列表详细信息
     */
    private List<AIUserInfoWithScore> convertToAIUserInfoWithScoreList(Long userId, Map<OrderProductItem, List<AIUserUnitScore>> scoreListMap,
                                                                       ChipsEnglishLevel chipsEnglishLevel, Boolean showPlay,
                                                                       String userName, OrderProduct orderProduct) {
        return scoreListMap.entrySet().stream().map(es -> {
            AIUserInfoWithScore aiUserInfoWithScore = new AIUserInfoWithScore();
            aiUserInfoWithScore.setId(userId);
            aiUserInfoWithScore.setName(userName);
            aiUserInfoWithScore.setProductName(orderProduct == null ? "" : orderProduct.getName());
            aiUserInfoWithScore.setProductItemName(es.getKey().getName());
            aiUserInfoWithScore.setBookId(es.getKey().getAppItemId());
            aiUserInfoWithScore.setLevel(chipsEnglishLevel);
            List<AIUserUnitScore> scoreList = es.getValue();
            aiUserInfoWithScore.setScoreLis(scoreList);
            long totalNum = 0L;
            long finishedNum = 0L;
            if (scoreList.size() <= 0) {
                aiUserInfoWithScore.setFinishRate(0D);
            } else {
                totalNum = scoreList.size();
                finishedNum = scoreList.stream().filter(s -> Boolean.TRUE.equals(s.getFinished())).count();
                aiUserInfoWithScore.setFinishRate(finishedNum * 1.0 / scoreList.size());
            }
            aiUserInfoWithScore.setTotalNum(totalNum);
            aiUserInfoWithScore.setFinishedNum(finishedNum);
            aiUserInfoWithScore.setShowPlay(showPlay);
            return aiUserInfoWithScore;
        }).collect(Collectors.toList());

    }

    /**
     * 过滤掉 mock_choice 这个类型没有master字段的数据
     */
    private boolean filterCollection(AIUserQuestionResultCollection collection) {
        if (collection == null || collection.getQuestionType() == null) {
            return true;
        }
        switch (collection.getQuestionType()) {
            case mock_choice:
                if (collection.getMaster() == null) {
                    return false;
                }
            default:
                return true;
        }
    }

    /**
     * 是否满足主动服务2的过滤条件
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=44843543
     */
    private boolean filter(AIUserQuestionResultCollection collection, LessonType lessonType) {
        if (lessonType == null) {
            return false;
        }
        if (lessonType == LessonType.video_conversation) {
            return filterVideoConversation(collection);
        }
        if (lessonType == LessonType.warm_up) {
            return filterWarmUp(collection);
        }
        return false;
    }

    private boolean filterWarmUp(AIUserQuestionResultCollection collection) {
        return filterScore(collection);
    }

    private boolean filterVideoConversation(AIUserQuestionResultCollection collection) {
        String level = collection.getLevel().toUpperCase();
        if (level.equals("E") || level.contains("F")) {
            return false;
        }
        if (!level.equals("A+")) {
            return true;
        }
        return filterScore(collection);
    }

    private boolean filterScore(AIUserQuestionResultCollection collection) {
        BigDecimal seven = new BigDecimal(7);
        BigDecimal zero = new BigDecimal(0);
        List<AIQuestionAppraisionRequest.Line> lines = Optional.ofNullable(collection.getVoiceEngineJson()).map(json -> JsonUtils.fromJson(json, AIQuestionAppraisionRequest.class)).map(AIQuestionAppraisionRequest::getLines).orElse(Collections.emptyList());
        for (AIQuestionAppraisionRequest.Line line : lines) {
            List<AIQuestionAppraisionRequest.Word> words = line.getWords();
            if (CollectionUtils.isEmpty(words)) {
                continue;
            }
            for (AIQuestionAppraisionRequest.Word word : words) {
                if (word == null || StringUtils.isBlank(word.getText()) || word.getScore() == null || !word.getText().matches(REGEX)) {
                    continue;
                }
                int i = word.getScore().compareTo(seven);
                int j = word.getScore().compareTo(zero);
                if ((i == -1 || i == 0) && (j == 1 || j == 0)) {//小于等于7分 大于0
                    return true;
                }
            }
        }
        return false;
    }

    private List<StoneLessonData> sort(List<StoneLessonData> list) {
        Map<LessonType, List<StoneLessonData>> map = new HashMap<>();
        list.forEach(s -> {
            if (s.getJsonData() == null || s.getJsonData().getLesson_type() == null) {
                return;
            }
            LessonType lessonType = s.getJsonData().getLesson_type();
            List<StoneLessonData> temp = map.get(lessonType);
            if (temp == null) {
                temp = new ArrayList<>();
                map.put(lessonType, temp);
            }
            temp.add(s);
        });
        List<StoneLessonData> sorted = new ArrayList<>();
        List<StoneLessonData> l1 = map.get(LessonType.video_conversation);
        if (CollectionUtils.isNotEmpty(l1)) {
            sorted.addAll(l1);
        }
        List<StoneLessonData> l2 = map.get(LessonType.warm_up);
        if (CollectionUtils.isNotEmpty(l2)) {
            sorted.addAll(l2);
        }
        return sorted;
    }

    private BigDecimal scoreHandler(AIUserQuestionResultCollection userQuestionResultCollection, BigDecimal defaultScore) {
        if (userQuestionResultCollection == null || userQuestionResultCollection.getQuestionType() == null) {
            return defaultScore;
        }
        switch (userQuestionResultCollection.getQuestionType()) {
            case choice_lead_in:
            case choice_sentence2pic:
            case choice_word2pic:
            case choice_word2trans:
            case choice_sentence2audio:
            case choice_cultural:
            case mock_choice:
                return new BigDecimal(userQuestionResultCollection.getMaster() != null && userQuestionResultCollection.getMaster() ? 100 : 25);
            default:
                return defaultScore;
        }
    }


    @Override
    public MapMessage loadQuestionResult4Crm(Long userId, String lessonId) {

        if (userId == null || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("没有记录");
        }
        MapMessage mm = MapMessage.successMessage();

        List<AIUserQuestionResultCollection> list = aiUserQuestionResultCollectionDao.loadByUidAndLessonId4Crm(userId, lessonId);

        if (list.isEmpty()) {
            return mm.add("list", list);
        }
        list = list.stream().filter(c -> filterCollection(c)).collect(Collectors.toList());

        // 处理精度
        list.forEach(e -> {
//            e.setOriginScore(Optional.ofNullable(e.getOriginScore()).map(x -> x.setScale(2, BigDecimal.ROUND_HALF_UP)).orElse(BigDecimal.ZERO));
            e.setOriginScore(Optional.ofNullable(scoreHandler(e, e.getOriginScore())).map(x -> x.setScale(2, BigDecimal.ROUND_HALF_UP)).orElse(BigDecimal.ZERO));
            e.setScore(Optional.ofNullable(scoreHandler(e, e.getScore())).map(x -> x.setScale(2, BigDecimal.ROUND_HALF_UP)).orElse(BigDecimal.ZERO));
        });


        Map<String, List<AIUserQuestionResultCollection>> qrhMap = groupByQidAndSortByCreateDateDesc(list);

        Map<String, StoneData> questionData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(lessonId));

        List<Map<String, Object>> ret = new ArrayList<>();


        Map<String, StoneData> stoneDataMap = buildQuestionToStoneData(questionData);

        checkData(qrhMap, stoneDataMap);
        Map<String, ActiveServiceUserQuestionTemplate> templateMap = Optional.of(stoneDataMap).map(Map::values)
                .map(l -> l.stream().map(d -> userId + "-" + d.getId()).collect(Collectors.toList())).map(l -> activeServiceUserQuestionTemplateDao.loads(l))
                .orElse(Collections.emptyMap());

        stoneDataMap.forEach((k, v) -> {
            Map<String, Object> value = new HashMap<>();
            value.put("question", v);
            value.put("answer", Optional.ofNullable(qrhMap.get(k)).orElse(Collections.emptyList()));
            value.put("activeServiceFlag", templateMap.get(userId + "-" + v.getId()) == null);
            ret.add(value);
        });
        mm.add("userId", userId);
        return mm.add("list", ret);

    }

    @Override
    public MapMessage loadQuestionResultByUnit4Crm(Long userId, String bookId, String unitId) {
        List<StoneData> lessonList = chipsEnglishContentLoaderImpl.loadLessonByUnitId(bookId, unitId);
        MapMessage message = MapMessage.successMessage();
        message.add("userId", userId);
        if (CollectionUtils.isEmpty(lessonList)) {
            return message.add("list", Collections.emptyList());
        }
        List<StoneLessonData> stoneLessonDataList = lessonList.stream().map(StoneLessonData::newInstance).collect(Collectors.toList());
//        stoneLessonDataList = sort(stoneLessonDataList);
        List<AIUserQuestionResultCollection> allAnswerListForActiveService = new ArrayList<>();//过滤掉不满足主动服务的用户回答
        List<AIUserQuestionResultCollection> allAnswerList = new ArrayList<>();
        Map<String, StoneData> allLessonDataMap = new HashMap<>();
        StoneLessonData defaultLesson = null;
        for (StoneLessonData lesson : stoneLessonDataList) {
            if (lesson.getJsonData().getLesson_type() == LessonType.video_conversation) {
                defaultLesson = lesson;
            }
            Map<String, StoneData> lessonData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(lesson.getId()));
            if (MapUtils.isNotEmpty(lessonData)) {
                allLessonDataMap.putAll(lessonData);
            }
            LessonType lessonType = lesson.getJsonData().getLesson_type();
            if (lessonType == null || (lessonType != LessonType.video_conversation && lessonType != LessonType.warm_up)) {
                continue;
            }
            List<AIUserQuestionResultCollection> list = aiUserQuestionResultCollectionDao.loadByUidAndLessonId4Crm(userId, lesson.getId());
            List<AIUserQuestionResultCollection> answerList = loadAnswer(list, userId, lesson.getId(), lessonData, lessonType);
            List<AIUserQuestionResultCollection> activeServiceAnswerList = answerList.stream().filter(c -> filter(c, lessonType)).collect(Collectors.toList());//过滤满足主动服务的数据
            if (CollectionUtils.isNotEmpty(answerList)) {
                allAnswerList.addAll(answerList);
            }
            if (CollectionUtils.isNotEmpty(activeServiceAnswerList)) {
                allAnswerListForActiveService.addAll(activeServiceAnswerList);
            }
        }
        if (CollectionUtils.isEmpty(allAnswerListForActiveService)) {
            allAnswerListForActiveService = defaultAnswer(userId, defaultLesson);
        }
        List<Map<String, Object>> list = buildQuestionResult(userId, allAnswerListForActiveService, allLessonDataMap, allAnswerList);
        message.add("list", list);
        return message;
    }

    /**
     * 取视频对话中任意一个题目的回答结果
     *
     * @param userId
     * @param lessonData
     * @return
     */
    private List<AIUserQuestionResultCollection> defaultAnswer(Long userId, StoneLessonData lessonData) {
        if (lessonData == null) {
            return Collections.emptyList();
        }
        List<AIUserQuestionResultCollection> list = aiUserQuestionResultCollectionDao.loadByUidAndLessonId4Crm(userId, lessonData.getId());
        List<String> contentIds = lessonData.getJsonData().getContent_ids();
        if (CollectionUtils.isEmpty(contentIds)) {
            return Collections.emptyList();
        }
        Map<String, List<AIUserQuestionResultCollection>> groupMap = list.stream().filter(x -> x.getQid() != null).collect(Collectors.groupingBy(AIUserQuestionResultCollection::getQid));
        for (String qid : contentIds) {
            List<AIUserQuestionResultCollection> answerList = groupMap.get(qid);
            if (CollectionUtils.isNotEmpty(answerList)) {
                return answerList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 需要展示的用户回答结果
     *
     * @param userId
     * @param lessonId
     * @param lessonData
     * @return
     */
    public List<AIUserQuestionResultCollection> loadAnswer(List<AIUserQuestionResultCollection> answerList, Long userId, String lessonId, Map<String, StoneData> lessonData, LessonType lessonType) {
        if (userId == null || StringUtils.isBlank(lessonId) || CollectionUtils.isEmpty(answerList)) {
            return Collections.emptyList();
        }
//        LessonType lessonType = Optional.ofNullable(lessonData.get(lessonId)).map(StoneLessonData::newInstance).map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getLesson_type).orElse(null);
        if (lessonType == null || (lessonType != LessonType.video_conversation && lessonType != LessonType.warm_up)) {
            return Collections.emptyList();
        }
        return answerList.stream().filter(c -> filterCollection(c)).collect(Collectors.toList());//过滤一下异常数据
    }

    public List<AIUserQuestionResultCollection> loadAnswer(Long userId, String lessonId, Map<String, StoneData> lessonData) {
        if (userId == null || StringUtils.isBlank(lessonId)) {
            return Collections.emptyList();
        }
        LessonType lessonType = Optional.ofNullable(lessonData.get(lessonId)).map(StoneLessonData::newInstance).map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getLesson_type).orElse(null);
        if (lessonType == null || (lessonType != LessonType.video_conversation && lessonType != LessonType.warm_up)) {
            return Collections.emptyList();
        }
        List<AIUserQuestionResultCollection> list = aiUserQuestionResultCollectionDao.loadByUidAndLessonId4Crm(userId, lessonId);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        list = list.stream().filter(c -> filterCollection(c)).collect(Collectors.toList());//过滤一下异常数据
        return list.stream().filter(c -> filter(c, lessonType)).collect(Collectors.toList());//过滤满足主动服务的数据
    }

    /**
     * @param userId
     * @param list          主动服务需要展示的用户回答
     * @param lessonData
     * @param allAnswerList unit下所有的用户回答
     * @return
     */
    public List<Map<String, Object>> buildQuestionResult(Long userId, List<AIUserQuestionResultCollection> list, Map<String, StoneData> lessonData, List<AIUserQuestionResultCollection> allAnswerList) {
        // 处理精度
        list.forEach(e -> {
            e.setOriginScore(Optional.ofNullable(scoreHandler(e, e.getOriginScore())).map(x -> x.setScale(2, BigDecimal.ROUND_HALF_UP)).orElse(BigDecimal.ZERO));
            e.setScore(Optional.ofNullable(scoreHandler(e, e.getScore())).map(x -> x.setScale(2, BigDecimal.ROUND_HALF_UP)).orElse(BigDecimal.ZERO));
        });
        Map<String, List<AIUserQuestionResultCollection>> qrhMap = groupByQidAndSortByCreateDateDesc(list);
        Map<String, StoneData> stoneDataMap = buildQuestionToStoneData(lessonData);
        List<Map<String, Object>> ret = new ArrayList<>();
        checkData(qrhMap, stoneDataMap);//石头堆中没有查到的qid补充默认值
        Map<String, ActiveServiceUserTemplate> templateMap = Optional.of(stoneDataMap).map(Map::values)
                .map(l -> l.stream().map(d -> userId + "-" + d.getId()).collect(Collectors.toList())).map(l -> activeServiceUserTemplateDao.loads(l))
                .orElse(Collections.emptyMap());
//        qrhMap.forEach((k,v) -> {
//            Map<String, Object> value = new HashMap<>();
//            value.put("question", stoneDataMap.get(k));
//            List<AIUserQuestionResultCollection> answerList = Optional.ofNullable(v).orElse(Collections.emptyList());
//            value.put("aids", answerList.stream().map(AIUserQuestionResultCollection::getId).collect(Collectors.toList()));
//            value.put("answer", answerList);
//            value.put("lessonId", CollectionUtils.isEmpty(answerList) ? "" : answerList.get(0).getLessonId());
//            value.put("activeServiceFlag", templateMap.get(userId + "-" + k) == null);
//            ret.add(value);
//        });
        Map<String, Integer> sortMap = sortByAnswerCount(allAnswerList);
        sortMap.forEach((k, v) -> {
            List<AIUserQuestionResultCollection> temp = qrhMap.get(k);
            if (CollectionUtils.isEmpty(temp)) {
                return;
            }
            Map<String, Object> value = new HashMap<>();
            value.put("question", stoneDataMap.get(k));
            List<AIUserQuestionResultCollection> answerList = Optional.ofNullable(temp).orElse(Collections.emptyList());
            value.put("aids", answerList.stream().map(AIUserQuestionResultCollection::getId).collect(Collectors.toList()));
            value.put("answer", answerList);
            value.put("lessonId", CollectionUtils.isEmpty(answerList) ? "" : answerList.get(0).getLessonId());
            value.put("activeServiceFlag", templateMap.get(userId + "-" + k) == null);
            value.put("answerCount", v);
            ret.add(value);
        });
        return ret;
    }

    /**
     * 根据题目的回答次数进行排序
     *
     * @param allAnswerList
     * @return
     */
    private Map<String, Integer> sortByAnswerCount(List<AIUserQuestionResultCollection> allAnswerList) {
        Map<String, List<AIUserQuestionResultCollection>> allQidToAnswerMap = allAnswerList.stream().filter(x -> x.getQid() != null).collect(Collectors.groupingBy(AIUserQuestionResultCollection::getQid));
        HashMap<String, Integer> sortMap = allQidToAnswerMap.entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().size(), (x1, x2) -> x2, HashMap::new));
        return sortMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue(), (x1, x2) -> x2, LinkedHashMap::new));
    }

    /**
     * 石头堆中没有查到的qid 补充默认值
     */
    private void checkData(Map<String, List<AIUserQuestionResultCollection>> qrhMap, Map<String, StoneData> stoneDataMap) {
        qrhMap.keySet().forEach(x -> {
            if (stoneDataMap.get(x) != null) {
                return;
            }
            // Build data
            ChipsQuestionType type = Optional.ofNullable(qrhMap.get(x)).map(y -> y.get(0)).map(AIUserQuestionResultCollection::getQuestionType).orElse(ChipsQuestionType.unknown);
            StoneData data = new StoneData();
            data.setId(x);
            data.setSchemaName(type.name());
            data.setCustomName("");
            data.setJsonData("");
            stoneDataMap.put(x, data);
        });
    }

    /**
     * 根据qid分组并根据creeeateDate排序
     *
     * @param list
     * @return
     */
    @NotNull
    private Map<String, List<AIUserQuestionResultCollection>> groupByQidAndSortByCreateDateDesc(List<AIUserQuestionResultCollection> list) {
        Map<String, List<AIUserQuestionResultCollection>> groupMap = list.stream().filter(x -> x.getQid() != null).collect(Collectors.groupingBy(AIUserQuestionResultCollection::getQid));
        Map<String, List<AIUserQuestionResultCollection>> qrhMap = new HashMap<>();
        // sort qrhmap value
        groupMap.forEach((k, v) -> {
            List<AIUserQuestionResultCollection> result = v.stream().sorted(Comparator.comparing(AIUserQuestionResultCollection::getCreateDate).reversed()).collect(Collectors.toList());
            qrhMap.put(k, result);
        });
        return qrhMap;
    }

    /**
     * 针对task_npc 这种类型的question 获取下面的子question
     * 根据lessonoid从石头堆获取的content_ids 字段的值
     *
     * @return 每个questionId对应的石头堆数据，如果是task_npc 类型的key就是所有的子题id
     */
    @NotNull
    private Map<String, StoneData> buildQuestionToStoneData(Map<String, StoneData> lessonData) {
        List<String> questionIds = new ArrayList<>();
        lessonData.forEach((k, v) -> {
            questionIds.addAll(Optional.ofNullable(StoneLessonData.newInstance(v)).map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getContent_ids).orElse(Collections.emptyList()));
        });
        Map<String, StoneData> parentStoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(questionIds);
        Map<String, StoneData> stoneDataMap = new LinkedHashMap<>();
        // Load task_npc -> task_topic
        parentStoneDataMap.forEach((k, v) -> {
            if (ChipsQuestionType.task_npc.name().equals(v.getSchemaName())) {
                List<String> addQids = Optional.ofNullable(StoneTalkNpcQuestionData.newInstance(v)).map(StoneTalkNpcQuestionData::getJsonData).map(StoneTalkNpcQuestionData.Npc::getContent_ids).orElse(Collections.emptyList());
                stoneDataMap.putAll(stoneDataLoaderClient.loadStoneDataIncludeDisabled(addQids));
            } else {
                stoneDataMap.put(k, v);
            }
        });
        return stoneDataMap;
    }

    /**
     * 获取用户教材
     */
    @Override
    public MapMessage loadUserChipsBookIds(Long parentId) {
        if (parentId == null) {
            return MapMessage.errorMessage("没有记录");
        }
        Set<String> productItemIds = chipsUserCoursePersistence.loadByUserId(parentId).stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());
//        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
//        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.ChipsEnglish == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());
//        List<String> productItemIds = userActivatedProducts.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
//                .map(UserActivatedProduct::getProductItemId).collect(Collectors.toList());
        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(productItemIds);

        List<Map<String, String>> bookIds = new ArrayList<>();
        productItemIds.forEach(itemId -> {
            OrderProductItem orderProductItem = orderProductItemMap.get(itemId);

            if (orderProductItem != null && StringUtils.isNotBlank(orderProductItem.getAppItemId())) {
                Map<String, String> map = new HashMap<>();
                map.put("bookId", orderProductItem.getAppItemId());
                map.put("bookName", orderProductItem.getName());
                bookIds.add(map);
            }
        });
        MapMessage mm = MapMessage.successMessage();
        User user = userLoaderClient.loadUser(parentId);
        mm.add("username", Optional.ofNullable(user).map(User::fetchRealname).orElse("该用户不存在"));
        mm.add("list", bookIds);
        return mm;
    }

    /**
     * 获取unitId和lessonId
     */
    @Override
    public MapMessage loadUserChipsLessonIds(Long parentId, String bookId, ChipsUnitType unitType, String unitId) {
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("没有记录");
        }

        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(bookId));

        MapMessage mm = MapMessage.successMessage();
        Map<String, List<String>> dmap = new LinkedHashMap<>();
        stoneDataMap.forEach((k, v) -> {

            StoneBookData bookData = StoneBookData.newInstance(v);

            List<StoneBookData.Node> unitList = Optional.ofNullable(bookData).map(StoneBookData::getJsonData).map(StoneBookData.Book::getChildren).orElse(Collections.emptyList());

            unitList.forEach(x -> {
                List<String> lessonIds = new ArrayList<>();
                List<StoneBookData.Node> lessonList = x.getChildren();
                if (lessonList != null) {
                    lessonList.forEach(y -> {
                        if (StringUtils.isNotBlank(y.getStone_data_id())) {
                            lessonIds.add(y.getStone_data_id());
                        }
                    });

                }
                if (!lessonIds.isEmpty()) {
                    dmap.put(x.getStone_data_id(), lessonIds);
                }

            });


        });

        if (dmap.isEmpty()) {
            return MapMessage.errorMessage("查不到此记录");
        }

        // load cname for id
        Set<String> unitIds = dmap.keySet();
        Set<String> lessonIds = new HashSet<>();
        Collection<List<String>> lessonList = dmap.values();
        lessonList.forEach(lessonIds::addAll);

        Map<String, StoneData> unitData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(unitIds);
        Map<String, StoneData> lessonData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(lessonIds);

        Map<String, String> unitName = new HashMap<>();
        List<String> excludeByType = new ArrayList<>();
        Map<String, String> lessonName = new HashMap<>();
        unitData.forEach((k, v) -> {
            StoneUnitData data = StoneUnitData.newInstance(v);
            if (data != null) {
                ChipsUnitType type = Optional.ofNullable(data.getJsonData()).map(StoneUnitData.Unit::getUnit_type).orElse(ChipsUnitType.unknown);
                if (ChipsUnitType.unknown != unitType && unitType != type) {
                    excludeByType.add(k);
                    return;
                }
            }
            unitName.put(k, Optional.ofNullable(v.getCustomName()).orElse(k));
        });
        lessonData.forEach((k, v) -> lessonName.put(k, Optional.ofNullable(v.getCustomName()).orElse(k)));

        List<Map> list = new ArrayList<>();
        dmap.forEach((k, v) -> {
            // filtered
            if (excludeByType.contains(k)) {
                return;
            }

            Map<String, Object> m = new HashMap<>();
            m.put("unitId", k);
            m.put("unitName", unitName.get(k));

            List<Map<String, Object>> lessons = new ArrayList<>();
            v.forEach(x -> {
                Map<String, Object> lemap = new HashMap<>();
                lemap.put("lessonId", x);
                lemap.put("lessonName", lessonName.get(x));
                Integer score = 0;
                if (parentId != null) {
                    AIUserLessonResultHistory result = aiUserLessonResultHistoryDao.load(parentId, x);
                    score = Optional.ofNullable(result).map(AIUserLessonResultHistory::getScore).orElse(0);
                }
                lemap.put("lessonScore", score);
                lessons.add(lemap);
            });
            m.put("lessons", lessons);
            list.add(m);
        });
        int unitIndex = -1;
        if (StringUtils.isNotBlank(unitId)) {
            for (int i = 0; i < list.size(); i++) {
                Object tmp = list.get(i).get("unitId");
                if (tmp != null && tmp.toString().equals(unitId)) {
                    unitIndex = i;
                }
            }
        }

        mm.add("unitIndex", unitIndex);
        return mm.add("list", list);
    }

    @Override
    public List<ChipsUserCourseMapper> loadUserEffectiveCourse(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<ChipsUserCourse> list = chipsUserService.loadUserEffectiveCourse(userId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        //取产品
        Set<String> productIds = list.stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream().filter(e -> productIds.contains(e.getId())).collect(Collectors.toMap(OrderProduct::getId, e -> e));

        //取教材
        Set<String> productItems = list.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());
        Map<String, String> itemBookMap = new HashMap<>();
        userOrderLoaderClient.loadAllOrderProductItems().stream().filter(e -> productItems.contains(e.getId())).forEach(e -> itemBookMap.put(e.getId(), e.getAppItemId()));
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(itemBookMap.values());

        List<ChipsUserCourseMapper> res = new ArrayList<>();
        for (ChipsUserCourse chipsUserCourse : list) {
            StoneBookData stoneBookData = Optional.ofNullable(chipsUserCourse).map(e -> itemBookMap.get(e.getProductItemId()))
                    .map(e -> stoneDataMap.get(e))
                    .map(StoneBookData::newInstance)
                    .orElse(null);
            ChipsUserCourseMapper userCourseMapper = transferToMapper(orderProductMap.get(chipsUserCourse.getProductId()), chipsUserCourse, stoneBookData);
            if (userCourseMapper == null) {
                continue;
            }
            res.add(userCourseMapper);
        }

        return res;
    }

    private ChipsUserCourseMapper transferToMapper(OrderProduct orderProduct, ChipsUserCourse userCourse, StoneBookData stoneBookData) {
        if (orderProduct == null || userCourse == null || stoneBookData == null || !userCourse.getProductId().equals(orderProduct.getId())) {
            return null;
        }
        ChipsUserCourseMapper userCourseMapper = new ChipsUserCourseMapper();
        userCourseMapper.setBookId(stoneBookData.getId());
        userCourseMapper.setBookName(stoneBookData.getJsonData().getName());
        userCourseMapper.setId(userCourse.getId());
        userCourseMapper.setOperation(userCourse.getOperation().name());
        userCourseMapper.setProductId(orderProduct.getId());
        userCourseMapper.setProductName(orderProduct.getName());
        userCourseMapper.setServiceBeginDate(DateUtils.dateToString(userCourse.getServiceBeginDate()));
        userCourseMapper.setServiceEndDate(DateUtils.dateToString(userCourse.getServiceEndDate()));
        userCourseMapper.setStatus("");
        int rank = Optional.of(orderProduct)
                .map(OrderProduct::getAttributes)
                .map(JsonUtils::fromJson)
                .map(e -> SafeConverter.toInt(e.get("rank")))
                .orElse(0);
        userCourseMapper.setRank(rank);
        return userCourseMapper;
    }

    @Override
    public MapMessage loadLessonConfigExpend() {


        List<AIDialogueLessonConfig> dialogueList = aiDialogueLessonConfigDao.findAll();
        List<AIDialogueTaskConfig> taskList = aiDialogueTaskConfigDao.findAll();


        List<ExpendLessonConfigData> dialogueJsgf = new ArrayList<>();
        List<ExpendLessonConfigData> taskJsgf = new ArrayList<>();
        dialogueList.forEach(x -> {
            List<ExpendLessonConfigData.ExpendLessonConfigJsgf> jsgfList = new ArrayList<>();
            if (x.getTopic() != null) {
                x.getTopic().forEach(y -> {
                    if (y != null && y.getContents() != null) {
                        y.getContents().forEach(z -> {
                            if (z != null && null != z.getPattern() && z.getPattern().length() > 10) {

                                ExpendLessonConfigData.ExpendLessonConfigJsgf jsgf = new ExpendLessonConfigData.ExpendLessonConfigJsgf();
                                jsgf.setJsgf(z.getPattern());
                                jsgf.setLevel(Optional.ofNullable(z.getFeedback()).map(feed -> feed.get(0)).map(AIDialogueLesson::getLevel).orElse("NULL"));
                                jsgfList.add(jsgf);
                            }
                        });
                    }
                });
            }

            if (!jsgfList.isEmpty()) {
                ExpendLessonConfigData data = new ExpendLessonConfigData();
                data.setId(x.getId());
                data.setTitle(x.getTitle());
                data.setJsgfList(jsgfList);
                dialogueJsgf.add(data);
            }

        });

        taskList.forEach(task -> {
            List<ExpendLessonConfigData.ExpendLessonConfigJsgf> jsgfList = new ArrayList<>();
            if (task.getNpcs() != null) {
                task.getNpcs().forEach(x -> {
                    if (x.getTopic() != null) {
                        x.getTopic().forEach(y -> {
                            if (y != null && y.getContents() != null) {

                                y.getContents().forEach(z -> {
                                    if (z != null && null != z.getPattern() && z.getPattern().length() > 10) {

                                        ExpendLessonConfigData.ExpendLessonConfigJsgf jsgf = new ExpendLessonConfigData.ExpendLessonConfigJsgf();
                                        jsgf.setJsgf(z.getPattern());
                                        jsgf.setLevel(Optional.ofNullable(z.getFeedback()).map(feed -> feed.get(0)).map(AITaskLesson::getLevel).orElse("NULL"));
                                        jsgfList.add(jsgf);
                                    }
                                });
                            }
                        });
                    }
                });
            }

            if (!jsgfList.isEmpty()) {
                ExpendLessonConfigData data = new ExpendLessonConfigData();
                data.setId(task.getId());
                data.setTitle(task.getTitle());
                data.setJsgfList(jsgfList);
                taskJsgf.add(data);
            }

        });


        //  get expand data
        dialogueJsgf.parallelStream().forEach(x -> {
            x.getJsgfList().forEach(y -> {
                y.setData(getExpandData(y.getJsgf()));
            });
        });

        taskJsgf.parallelStream().forEach(x -> {
            x.getJsgfList().forEach(y -> {
                y.setData(getExpandData(y.getJsgf()));
            });
        });


        return MapMessage.successMessage().add("dialogue", dialogueJsgf).add("task", taskJsgf);
    }


    private List<String> getExpandData(String jsfg) {
        String response = HttpRequestExecutor.defaultInstance()
                .post("http://10.7.13.75:9090/jsgf/expand")
                .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
                .addParameter("jsgf", jsfg)
                .execute().getResponseString();
        Map<String, Object> map = JsonUtils.fromJson(response);
        if (MapUtils.isEmpty(map) || !"success".equals(map.get("result")) || map.get("data") == null) {
            return Collections.emptyList();
        }
        return (List<String>) map.get("data");
    }

    @Override
    public AIActiveServiceUserTemplateItem buildUserAnswer(String unitId, String lessonId, String qid, String aid, String name) {
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitId));
        ChipsUnitType chipsUnitType = Optional.ofNullable(stoneDataMap).map(e -> e.get(unitId)).map(StoneUnitData::newInstance).map(StoneUnitData::getJsonData).map(StoneUnitData.Unit::getUnit_type).orElse(ChipsUnitType.unknown);
        Map<String, StoneData> lessonStoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(lessonId));
        LessonType lessonType = Optional.ofNullable(lessonStoneDataMap).map(e -> e.get(lessonId)).map(StoneLessonData::newInstance).map(StoneLessonData::getJsonData).map(StoneLessonData.Lesson::getLesson_type).orElse(null);
        if (lessonType == null) {
            return null;
        }
        Map<String, StoneData> questionStoneDataMap = stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(qid));
        ChipsQuestionType chipsQuestionType = Optional.ofNullable(questionStoneDataMap).map(e -> e.get(qid)).map(StoneQuestionData::newInstance).map(StoneQuestionData::getSchemaName).orElse(ChipsQuestionType.unknown);
        switch (chipsUnitType) {
            case topic_learning: {
                if (lessonType.equals(LessonType.video_conversation)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    if (collection != null && StringUtils.isNotBlank(collection.getUserVideo())) {
                        return new AIActiveServiceUserTemplateItem(name, collection.getUserVideo(), "video", -1, false);
                    }
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                if (lessonType.equals(LessonType.warm_up) && (chipsQuestionType.equals(ChipsQuestionType.word_repeat) || chipsQuestionType.equals(ChipsQuestionType.sentence_repeat))) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                break;
            }
            case special_consolidation: {
                if (lessonType.equals(LessonType.pattern_reserve) && chipsQuestionType.equals(ChipsQuestionType.qa_sentence)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : StringUtils.isNotBlank(collection.getUserAnswer()) ? collection.getUserAudio() : collection.getUserAudio(), "audio", -1, false);
                }
                if (lessonType.equals(LessonType.pattern_reserve) && chipsQuestionType.equals(ChipsQuestionType.sentence_repeat)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                if (lessonType.equals(LessonType.vocab_charging) && chipsQuestionType.equals(ChipsQuestionType.word_repeat)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                break;
            }
            case mock_test: {
                if (chipsQuestionType.equals(ChipsQuestionType.mock_qa)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                break;
            }
            case dialogue_practice: {
                if (lessonType.equals(LessonType.task_conversation)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : StringUtils.isNotBlank(collection.getUserAnswer()) ? collection.getUserAudio() : collection.getUserAudio(), "audio", -1, false);
                }
                break;
            }
            case short_lesson: {
                if (lessonType.equals(LessonType.warm_up) && (chipsQuestionType.equals(ChipsQuestionType.word_repeat) || chipsQuestionType.equals(ChipsQuestionType.sentence_repeat))) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
                if (lessonType.equals(LessonType.video_conversation) || lessonType.equals(LessonType.task_conversation)) {
                    AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                    if (collection != null && StringUtils.isNotBlank(collection.getUserVideo())) {
                        return new AIActiveServiceUserTemplateItem(name, collection.getUserVideo(), "video", -1, false);
                    }
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
            }
            default:{
                AIUserQuestionResultCollection collection = aiUserQuestionResultCollectionDao.load(aid);
                if (collection != null && StringUtils.isNotBlank(collection.getUserVideo())) {
                    return new AIActiveServiceUserTemplateItem(name, collection.getUserVideo(), "video", -1, false);
                }
                if(collection != null && StringUtils.isNotBlank(collection.getUserAudio())){
                    return new AIActiveServiceUserTemplateItem(name, collection == null ? null : collection.getUserAudio(), "audio", -1, false);
                }
            }
        }
        return null;
    }

    @Override
    public ChipsUserMailInfo loadUserMailInfo(long userId) {
        ChipsEnglishUserExtSplit split = chipsEnglishUserExtSplitDao.load(userId);
        if (split != null) {
            return ChipsUserMailInfo.valueOf(split);
        }
        return null;
    }

    @Override
    public boolean updateUserMailInfo(long userId, String editType, String value) {
        ChipsEnglishUserExtSplit split = chipsEnglishUserExtSplitDao.load(userId);

        if (split == null) {
            return false;
        }

        if ("addr".equals(editType)) {
            split.setRecipientAddr(value);
        } else if ("name".equals(editType)) {
            split.setRecipientName(value);
        } else if ("tel".equals(editType)) {
            split.setRecipientTel(value);
        } else {
            return false;
        }

        chipsEnglishUserExtSplitDao.updateRecipient(userId, split.getRecipientName(), split.getRecipientTel(), split.getRecipientAddr());

        return true;
    }

    @Override
    public MapMessage updateMailAddrAndCourseLevel(long userId, String recipientName, String recipientTel, String recipientAddr, String courseLevel) {
        ChipsEnglishUserExtSplit split = chipsEnglishUserExtSplitDao.load(userId);

        if (split == null) {
            split = new ChipsEnglishUserExtSplit();
            split.setId(userId);
            split.setRecipientName(recipientName);
            split.setRecipientTel(recipientTel);
            split.setRecipientAddr(recipientAddr);
            split.setCourseLevel(courseLevel);
            chipsEnglishUserExtSplitDao.insert(split);
            return MapMessage.successMessage();
        }
        chipsEnglishUserExtSplitDao.updateMailAddrAndCourseLevel(userId, recipientName, recipientTel, recipientAddr, courseLevel);
        return MapMessage.successMessage();
    }

    @Override
    public boolean updateUserWxNum(long userId, String wxNum) {
        ChipsEnglishUserExtSplit split = chipsEnglishUserExtSplitDao.load(userId);

        if (split == null) {
            return false;
        }

        split.setWxCode(wxNum);

        chipsEnglishUserExtSplitDao.updateWx(userId, split.getWxCode(), split.getWxName());

        return true;
    }

    @Override
    public boolean updateUserWxName(long userId, String wxName) {
        ChipsEnglishUserExtSplit split = chipsEnglishUserExtSplitDao.load(userId);

        if (split == null) {
            return false;
        }

        split.setWxName(wxName);

        chipsEnglishUserExtSplitDao.updateWx(userId, split.getWxCode(), split.getWxName());
        return true;
    }

    @Override
    public Collection<ChipsEnglishClass> loadMyClazz(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<ChipsEnglishClassUserRef> refs = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(refs)) {
            return Collections.emptyList();
        }

        return chipsEnglishClassPersistence.loads(refs.stream().map(ChipsEnglishClassUserRef::getChipsClassId).collect(Collectors.toSet())).values();
    }

}

