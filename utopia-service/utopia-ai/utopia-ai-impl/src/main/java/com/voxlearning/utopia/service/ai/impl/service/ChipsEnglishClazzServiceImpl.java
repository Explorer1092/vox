package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.data.ClazzComparePojo;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.exception.ProductNotExitException;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/8/24
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = ChipsEnglishClazzService.class, version = @ServiceVersion(version = "20190403")),
        @ExposeService(interfaceClass = ChipsEnglishClazzService.class, version = @ServiceVersion(version = "20190408"))
})
public class ChipsEnglishClazzServiceImpl implements ChipsEnglishClazzService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsEnglishUserExtDao chipsEnglishUserExtDao;
    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;

    @Inject
    private ChipsEnglishClassStatisticsDao chipsEnglishClassStatisticsDao;
    @Inject
    private ChipsClassStatisticsDao chipsClassStatisticsDao;

    @Inject
    private ChipsEnglishClassStatisticsLatestDao chipsEnglishClassStatisticsLatestDao;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private AiChipsEnglishTeacherDao chipsEnglishTeacherDao;

    @Inject
    private ChipsClazzCompareDao chipsClazzCompareDao;

    @Inject
    protected StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private AIUserBookResultDao aiUserBookResultDao;

    @Inject
    private ChipsOtherServiceUserTemplateDao chipsOtherServiceUserTemplateDao;

    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    @Inject
    private ChipsEnglishClassUpdateLogDao chipsEnglishClassUpdateLogDao;

    @Inject
    private ChipsUserCoursePersistence chipsUserCoursePersistence;

    private static final int PAGE_SIZE = 10;

    @Override
    public ChipsEnglishClass loadMyDefaultClass(Long userId) {
        AIUserLessonBookRef aiUserLessonBookRef;
        try {
            aiUserLessonBookRef = chipsUserService.fetchOrInitBookRef(userId);
        } catch (ProductNotExitException e) {
            return null;
        }

        Set<Long> userClazzSet = chipsEnglishClassUserRefPersistence.loadByUserId(userId).stream().map(ChipsEnglishClassUserRef::getChipsClassId).collect(Collectors.toSet());
        return selectChipsEnglishClassByProductId(aiUserLessonBookRef.getProductId())
                .stream()
                .filter(e -> userClazzSet.contains(e.getId()))
                .findFirst().orElse(null);
    }

    @Override
    public List<ChipsEnglishClass> selectAllChipsEnglishClass() {
        return chipsEnglishClassPersistence.loadAll();
    }

    @Override
    public ChipsEnglishClass selectChipsEnglishClassById(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return null;
        }
        return chipsEnglishClassPersistence.load(clazzId);
    }

    @Override
    public List<ChipsEnglishClass> selectChipsEnglishClassByProductId(String productId) {
        if (StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        return chipsEnglishClassPersistence.loadByProductId(productId);
    }

    @Override
    public List<ChipsEnglishClass> selectChipsEnglishClassByProductIdTeacherName(String productId, String teacherName) {
        if (StringUtils.isBlank(productId) && StringUtils.isBlank(teacherName)) {
            return null;
        }
        if (StringUtils.isBlank(productId)) {
            return chipsEnglishClassPersistence.loadByTeacherName(teacherName);
        }
        List<ChipsEnglishClass> clazzList = chipsEnglishClassPersistence.loadByProductId(productId);
        if (StringUtils.isBlank(teacherName)) {
            return clazzList;
        }
        return clazzList.stream().filter(c -> StringUtils.isNotBlank(c.getTeacher()) && c.getTeacher().equals(teacherName)).collect(Collectors.toList());
    }

    @Override
    public List<ChipsEnglishUserExt> selectChipsEnglishUserExtByUserIds(Collection<Long> userIdCollection) {
        if (CollectionUtils.isEmpty(userIdCollection)) {
            return null;
        }
        Map<Long, ChipsEnglishUserExt> userExtMap = chipsEnglishUserExtDao.loads(userIdCollection);
        if (MapUtils.isEmpty(userExtMap)) {
            return null;
        }
        return new ArrayList<>(userExtMap.values());
    }

    @Override
    public Map<Long, ChipsEnglishUserExtSplit> loadChipsEnglishUserExtSplitByUserIds(Collection<Long> userIdCollection) {
        if (CollectionUtils.isEmpty(userIdCollection)) {
            return Collections.emptyMap();
        }
        Map<Long, ChipsEnglishUserExtSplit> userExtSplitMap = chipsEnglishUserExtSplitDao.loads(userIdCollection);
        if (userExtSplitMap == null) {
            return new HashMap<>();
        }
        return userExtSplitMap;
    }

    @Override
    public ChipsEnglishUserExt selectChipsEnglishUserExtByUserId(Long userId) {
        if (userId == null || userId == 0L) {
            return null;
        }
        return chipsEnglishUserExtDao.load(userId);
    }

    @Override
    public ChipsEnglishUserExtSplit selectChipsEnglishUserExtSplitByUserId(Long userId) {
        if (userId == null || userId == 0L) {
            return null;
        }
        return chipsEnglishUserExtSplitDao.load(userId);
    }

    @Override
    public List<ChipsEnglishClassUserRef> selectChipsEnglishClassUserRefByClazzId(Long clazzId) {
        return chipsUserService.selectChipsEnglishClassUserRefByClazzId(clazzId);
    }

    @Override
    public ChipsEnglishClassUserRef selectChipsEnglishClassUserRefByUserId(Long userId, Long clazzId) {
        if (userId == null || userId == 0L || clazzId == null || clazzId == 0L) {
            return null;
        }
        List<ChipsEnglishClassUserRef> list = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().filter(e -> clazzId.equals(e.getChipsClassId())).findFirst().orElse(null);
    }

    @Override
    public MapMessage saveOrUpdateChipsEnglishClass(ChipsEnglishClass chipsEnglishClazz) {
        if (chipsEnglishClazz == null || chipsEnglishClazz.getId() == null) {
            return MapMessage.errorMessage().add("info", "班级为null");
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(chipsEnglishClazz.getId());
        if (clazz == null) {
            clazz = new ChipsEnglishClass();
            clazz.setCreateTime(new Date());
            clazz.setDisabled(false);
        }
        clazz.setUserLimit(chipsEnglishClazz.getUserLimit());
        clazz.setProductId(chipsEnglishClazz.getProductId());
        clazz.setTeacher(chipsEnglishClazz.getTeacher());
        clazz.setName(chipsEnglishClazz.getName());
        clazz.setType(chipsEnglishClazz.getType());
        clazz.setUpdateTime(new Date());
        chipsEnglishClassPersistence.insertOrUpdate(clazz);
        return MapMessage.successMessage();
    }

    /**
     * 加锁
     */
    @Override
    public MapMessage mergeChipsEnglishClass(Long clazzId, Long aimClazzId) {
        //1、查找老班级下的所有用户，3、更新user的班级3、disable 老班级
        if (clazzId == null || clazzId == 0L || aimClazzId == null || aimClazzId == 0L) {
            return MapMessage.errorMessage().add("info", "clazzId=" + clazzId + ";aimClazzId=" + aimClazzId);
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByClassId(clazzId);
        userRefList.forEach(ref -> {
            try {
                chipsEnglishClassUserRefPersistence.disabled(ref);
                ref.setChipsClassId(aimClazzId);
                ref.setId(null);
                chipsEnglishClassUserRefPersistence.insertOrUpdate(ref);
            } catch (Exception e) {
                logger.info("mergeChipsEnglishClass fail user: " + ref.getUserId() + "; clazzId: " + clazzId + "; aimClazzId: " + aimClazzId);
            }
        });
//        List<Long> userIdList = extractUserId(userRefList);
//        if (CollectionUtils.isNotEmpty(userIdList)) {
//            chipsEnglishClassUserRefPersistence.updateClazzId(userIdList, clazzId, aimClazzId);
//        }
        chipsEnglishClassPersistence.disableByClazzId(clazzId);
        return MapMessage.successMessage();
    }


    private List<Long> extractUserId(List<ChipsEnglishClassUserRef> userRefList) {
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        for (ChipsEnglishClassUserRef userRef : userRefList) {
            if (userRef == null || userRef.getUserId() == null) {
                continue;
            }
            list.add(userRef.getUserId());
        }
        return list;
    }

    @Override
    public List<ChipsEnglishClassStatistics> selectChipsEnglishClassStatisticsByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return Collections.emptyList();
        }
        return chipsEnglishClassStatisticsDao.loadByClassId(clazzId);
    }

    @Override
    public List<ChipsClassStatistics> selectChipsClassStatisticsByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return Collections.emptyList();
        }
        return chipsClassStatisticsDao.loadByClassId(clazzId);
    }


    @Override
    public List<ChipsEnglishClassStatisticsLatest> selectChipsEnglishClassStatisticsLatestByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return null;
        }
        return chipsEnglishClassStatisticsLatestDao.loadByClassId(clazzId);
    }

    @Override
    public List<Long> selectAllUserByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0L) {
            return Collections.emptyList();
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByClassId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return Collections.emptyList();
        }
        List<Long> list = new ArrayList<>();
        userRefList.forEach(e -> list.add(e.getUserId()));
        return list;
    }

    @Override
    public List<ChipsEnglishClassUserRef> selectAllChipsEnglishClassUserRefByUserId(Long userId) {
        return chipsEnglishClassUserRefPersistence.loadByUserId(userId);
    }

    @Override
    public MapMessage saveUserRefExt(Long clazzId, Long userId, String wechatNumber, Boolean joinedGroup, String duration) {
        if (clazzId == null || clazzId == 0L || userId == null || userId == 0L) {
            return MapMessage.errorMessage().add("info", "clazzId=" + clazzId + ";userId=" + userId);
        }
        ChipsEnglishClassUserRef userRef = new ChipsEnglishClassUserRef();
        userRef.setOrderRef("");
        userRef.setUpdateTime(new Date());
        userRef.setCreateTime(new Date());
        userRef.setChipsClassId(clazzId);
        userRef.setUserId(userId);
        userRef.setInGroup(joinedGroup);
        userRef.setDisabled(false);
        chipsEnglishClassUserRefPersistence.insertOrUpdate(userRef);
        chipsEnglishUserExtSplitDao.upsert(userId, wechatNumber, duration);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage insertOrUpdateUserExt(List<Long> userIdList, boolean showPlay) {
        chipsEnglishUserExtSplitDao.updateShowDisplay(userIdList, showPlay);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage insertOrUpdateUserExtWxAddStatus(Long userId, boolean wxAddStatus) {
        chipsEnglishUserExtSplitDao.updateWxAddStatus(userId, wxAddStatus);
        return MapMessage.successMessage();
    }
    @Override
    public MapMessage insertOrUpdateUserExtEpWxAddStatus(Long userId, boolean epWxAddStatus) {
        chipsEnglishUserExtSplitDao.updateEpWxAddStatus(userId, epWxAddStatus);
        return MapMessage.successMessage();
    }

    @Override
    public Long loadClazzIdByUserAndUnit(Long userId, String unitId) {
        return chipsUserService.loadClazzIdByUserAndUnit(userId, unitId);
    }

    @Override
    public ChipsEnglishClass loadClazzIdByUserAndProduct(Long userId, String productId) {
        return chipsUserService.loadClazzIdByUserAndProduct(userId, productId);
    }

    @Override
    public MapMessage upsertChipsEnglishUserExtSplit(ChipsEnglishUserExtSplit extSplit) {
        try {
            chipsEnglishUserExtSplitDao.upsert(extSplit);
        } catch (Exception ex) {
            logger.error("upsert chips user ext split error, uid {}", extSplit.getId(), ex);
        }
        return MapMessage.successMessage();
    }

    @Override
    public List<AiChipsEnglishTeacher> loadAllChipsEnglishTeacher() {
        List<AiChipsEnglishTeacher> list = chipsEnglishTeacherDao.loadAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public AiChipsEnglishTeacher loadChipsEnglishTeacherById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return chipsEnglishTeacherDao.load(id);
    }

    @Override
    public AiChipsEnglishTeacher upsertAiChipsEnglishTeacher(AiChipsEnglishTeacher teacher) {
        return chipsEnglishTeacherDao.upsert(teacher);
    }

    @Override
    public MapMessage removeAiChipsEnglishTeacher(String id) {
        boolean remove = chipsEnglishTeacherDao.remove(id);
        if (remove) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

    @Override
    public AiChipsEnglishTeacher loadTeacherByUserIdAndBookId(Long userId, String bookId) {
        if (userId == null || userId == 0L || StringUtils.isBlank(bookId)) {
            return null;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        List<String> productList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .map(OrderProduct::getId).collect(Collectors.toList());
        Map<String, List<OrderProductItem>> productToItemList = userOrderLoaderClient.loadProductItemsByProductIds(productList);
        Set<String> productIdSet = new HashSet<>();
        for (Map.Entry<String, List<OrderProductItem>> entry : productToItemList.entrySet()) {
            for (OrderProductItem item : entry.getValue()) {
                if (StringUtils.isNotBlank(item.getAppItemId()) && item.getAppItemId().equals(bookId)) {
                    productIdSet.add(entry.getKey());
                }
            }
        }
        String teacherName = userRefList.stream().map(r -> chipsEnglishClassPersistence.load(r.getChipsClassId())).filter(c -> productIdSet.contains(c.getProductId()))
                .map(ChipsEnglishClass::getTeacher).findFirst().orElse(null);
        if (teacherName == null) {
            return null;
        }
        List<AiChipsEnglishTeacher> teacherList = chipsEnglishTeacherDao.loadByName(teacherName);
        return teacherList.stream().findFirst().orElse(null);
    }

    public AiChipsEnglishTeacher loadTeacherByUserIdAndClazzId(Long userId, long clazzId) {
        if (userId == null || userId == 0L || clazzId == 0L) {
            return null;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return null;
        }

        String teacherName = userRefList.stream().map(r -> chipsEnglishClassPersistence.load(r.getChipsClassId())).filter(c -> clazz.getProductId().equals(c.getProductId()))
                .map(ChipsEnglishClass::getTeacher).findFirst().orElse(null);
        if (teacherName == null) {
            return null;
        }
        List<AiChipsEnglishTeacher> teacherList = chipsEnglishTeacherDao.loadByName(teacherName);
        return teacherList.stream().findFirst().orElse(null);
    }

    @Override
    public MapMessage loadAllChipsClazzCompare(int pageNum) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        List<ChipsClazzCompare> clazzCompareList = chipsClazzCompareDao.loadByPage(pageNum, PAGE_SIZE);
        MapMessage message = MapMessage.successMessage();
        message.add("clazzCompareList", clazzCompareList);
        long totalNum = chipsClazzCompareDao.count();
        long totalPage = totalNum / PAGE_SIZE + 1;
        message.add("totalPage", totalPage);
        return message;
    }

    @Override
    public MapMessage saveChipsClazzCompare(ChipsClazzCompare clazzCompare) {
        chipsClazzCompareDao.upsert(clazzCompare);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadChipsClazzCompareById(String id) {
        MapMessage message = MapMessage.successMessage();
        ChipsClazzCompare clazzCompare = chipsClazzCompareDao.load(id);
        if (clazzCompare != null) {
            Map<Long, ChipsEnglishClass> compareClazzArrMap = loadChipsEnglishClass(clazzCompare.getCompareClazzIds());
            message.add("clazzCompare", clazzCompare);
            message.add("compareClazzIdArr", compareClazzArrMap.values().stream().map(ChipsEnglishClass::getId).collect(Collectors.toList()));
            message.add("compareClazzArr", compareClazzArrMap.values());
            if (StringUtils.isNotBlank(clazzCompare.getBasicClazzId())) {
                message.add("basicClazz", compareClazzArrMap.get(Long.parseLong(clazzCompare.getBasicClazzId())));
            }
            return message;
        } else {
            return MapMessage.errorMessage().add("info", "no record: " + id);
        }
    }

    private Map<Long, ChipsEnglishClass> loadChipsEnglishClass(String compareClazzIds) {
        long t1 = System.currentTimeMillis();
        if (StringUtils.isBlank(compareClazzIds)) {
            return Collections.emptyMap();
        }
        String[] split = compareClazzIds.split(",");
        List<Long> idList = new ArrayList<>();
        for (String idStr : split) {
            if (StringUtils.isBlank(idStr)) {
                continue;
            }
            idList.add(Long.parseLong(idStr));
        }
        Map<Long, ChipsEnglishClass> result = chipsEnglishClassPersistence.loads(idList);
//        logger.info(split.length + " ;loadChipsEnglishClass cost: " + (System.currentTimeMillis() - t1));
        return result;
    }

    /**
     * @param id
     * @param type     "1":最新数据，"2":当日数据
     * @param dayIndex 课次
     * @return
     */
    @Override
    public MapMessage buildClazzCompareData(String id, String type, int dayIndex) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().add("info", "id is null : " + id);
        }
        ChipsClazzCompare clazzCompare = chipsClazzCompareDao.load(id);
        if (clazzCompare == null) {
            return MapMessage.errorMessage().add("info", "对比班级 ChipsClazzCompare is null : " + id);
        }
        MapMessage message = MapMessage.successMessage();
        if ("2".equals(type)) {
            ClazzComparePojo pojo = buildThatDayData(clazzCompare, dayIndex);
            message.add("clazzComparePojo", pojo);

        } else {
            ClazzComparePojo pojo = buildCurrentDayData(clazzCompare, dayIndex);
            message.add("clazzComparePojo", pojo);
        }
        return message;
    }

    private ClazzComparePojo buildThatDayData(ChipsClazzCompare clazzCompare, int dayIndex) {
        String compareClazzIds = clazzCompare.getCompareClazzIds();
        if (StringUtils.isBlank(compareClazzIds)) {
            return null;
        }
        Map<Long, ChipsEnglishClass> clazzMap = loadChipsEnglishClass(compareClazzIds);
        ClazzComparePojo pojo = new ClazzComparePojo();
        //每个班级
        Map<Long, String> clazzToUnitMap = clazzToUnitMap(clazzToBookMap(clazzMap), dayIndex);
        List<ChipsEnglishClassStatistics> statisticsList = chipsEnglishClassStatisticsDao.loadByClassIds(clazzToUnitMap.keySet());
        HashMap<String, ChipsEnglishClassStatistics> collect = statisticsList.stream().collect(HashMap::new, (m, v) ->
                m.put(v.getClassId() + "-" + v.getUnitId(), v), HashMap::putAll);
        Map<Long, ChipsEnglishClassStatistics> clazzToStatMap = new HashMap();
        for (Map.Entry<Long, String> entry : clazzToUnitMap.entrySet()) {
            Long clazzId = entry.getKey();
            String unitId = entry.getValue();
            if (StringUtils.isNotBlank(unitId)) {
                clazzToStatMap.put(clazzId, collect.get(clazzId + "-" + unitId));
            }
        }
        //学习人数
        int totalUserCount = 0;//学习人数
        int totalCompleteCount = 0;//完课人数
        List<ClazzComparePojo.FirstTable> firstTableList = new ArrayList<>();
        List<ClazzComparePojo.SecondTable> secondTableList = new ArrayList<>();
        List<ClazzComparePojo.ThridTable> thridTableList = new ArrayList<>();
        int totalRemarkCount = 0;//完课点评人数
        int totalPaidCount = 0;//续费人数
        for (Map.Entry<Long, ChipsEnglishClassStatistics> entry : clazzToStatMap.entrySet()) {
            ChipsEnglishClassStatistics stat = entry.getValue();
            int userCount = 0;
            int completeCount = 0;
            int remarkCount = 0;
            if (stat != null) {
                userCount = stat.getClassNum();
                completeCount = stat.getClassFinishNum();
                remarkCount = stat.getClassRemarkNum();
                totalUserCount += stat.getClassNum();
                totalCompleteCount += stat.getClassFinishNum();
                totalRemarkCount += stat.getClassRemarkNum();
            }
            String unitId = clazzToUnitMap.get(entry.getKey());
            Long remindCount = 0l;
            if (StringUtils.isNotBlank(unitId)) {
                List<ChipsActiveServiceRecord> recordList = chipsActiveServiceRecordDao.loadByClassId(ChipsActiveServiceType.REMIND, entry.getKey());
                remindCount = recordList.stream().filter(r -> r.getServiced() && unitId.equals(r.getUnitId())).count();
            }
            ClazzComparePojo.FirstTable firstTable = new ClazzComparePojo.FirstTable();
            firstTable.setClazzId(entry.getKey());
            firstTable.setProductId(clazzMap.get(entry.getKey()).getProductId());
            firstTable.setClazzName(clazzMap.get(entry.getKey()).getName());
            firstTable.setUserCount(userCount);
            firstTable.setCompleteCount(completeCount);
            firstTable.setCompleteRate(formatRateToDouble(completeCount, userCount));
            firstTable.setCompleteRateStr(formatRate(completeCount, userCount));
            firstTable.setRemindCount(remindCount.intValue());

            ClazzComparePojo.SecondTable secondTable = new ClazzComparePojo.SecondTable();
            secondTable.setClazzId(entry.getKey());
            secondTable.setProductId(clazzMap.get(entry.getKey()).getProductId());
            secondTable.setClazzName(clazzMap.get(entry.getKey()).getName());
            secondTable.setCompleteCount(completeCount);
            secondTable.setRemarkCount(remarkCount);
            secondTable.setRemarkRate(formatRateToDouble(remarkCount, completeCount));
            secondTable.setRemarkRateStr(formatRate(remarkCount, completeCount));

            ClazzComparePojo.ThridTable thridTable = new ClazzComparePojo.ThridTable();
            thridTable.setClazzId(entry.getKey());
            thridTable.setProductId(clazzMap.get(entry.getKey()).getProductId());
            thridTable.setClazzName(clazzMap.get(entry.getKey()).getName());
            List<ChipsClassStatistics> clazzStatList = chipsClassStatisticsDao.loadByClassId(entry.getKey());
            int gradeCount = 0;
//            int paidCount = 0;
            int paidCount = calRenewCount(clazzMap.get(entry.getKey()));
            totalPaidCount += paidCount;
            if (CollectionUtils.isNotEmpty(clazzStatList) && clazzStatList.get(0) != null) {
                ChipsClassStatistics clazzStat = clazzStatList.get(0);
//                paidCount = clazzStat.getClassPaidNum();
                gradeCount = clazzStat.getClassRankNum();
//                totalPaidCount += clazzStat.getClassPaidNum();
            }
            thridTable.setGradeCount(gradeCount);

            thridTable.setPaidCount(paidCount);
            thridTable.setPaidRate(formatRateToDouble(paidCount, userCount));
            thridTable.setPaidRateStr(formatRate(paidCount, userCount));
            firstTableList.add(firstTable);
            secondTableList.add(secondTable);
            thridTableList.add(thridTable);
        }
        pojo.setTotalUserCount(totalUserCount);
        pojo.setTotalCompleteCount(totalCompleteCount);
        pojo.setTotalCompleteRate(formatRate(totalCompleteCount, totalUserCount));
        firstTableList.sort(Comparator.comparing(ClazzComparePojo.FirstTable::getCompleteRate).reversed());
        pojo.setFirstList(firstTableList);
        pojo.setTotalRemarkCount(totalRemarkCount);
        pojo.setTotalRemarkRate(formatRate(totalRemarkCount, totalCompleteCount));
        secondTableList.sort(Comparator.comparing(ClazzComparePojo.SecondTable::getRemarkRate).reversed());
        pojo.setSecondList(secondTableList);
        pojo.setTotalPaidCount(totalPaidCount);
        pojo.setTotalPaidRate(formatRate(totalPaidCount, totalUserCount));
        thridTableList.sort(Comparator.comparing(ClazzComparePojo.ThridTable::getPaidRate).reversed());
        pojo.setThridList(thridTableList);
        return pojo;
    }

    private String formatRate(Integer numerator, Integer denominator) {
        if (numerator == null || numerator == 0 || denominator == null || denominator == 0) {
            return "0.00%";
        }
        double val = new BigDecimal(numerator * 100).divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return val + "%";
    }

    private double formatRateToDouble(Integer numerator, Integer denominator) {
        if (numerator == null || numerator == 0 || denominator == null || denominator == 0) {
            return 0.00;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private Map<Long, String> clazzToBookMap(Map<Long, ChipsEnglishClass> clazzMap) {
        if (MapUtils.isEmpty(clazzMap)) {
            return Collections.emptyMap();
        }
        long t1 = System.currentTimeMillis();
        Map<Long, String> clazzToProductMap = clazzMap.values().stream().collect(Collectors.toMap(ChipsEnglishClass::getId, ChipsEnglishClass::getProductId));
        Map<String, List<OrderProductItem>> productToItemMap = userOrderLoaderClient.loadProductItemsByProductIds(new HashSet<>(clazzToProductMap.values()));
        Map<Long, String> clazzToBookMap = new HashMap<>();//班级对应的教材
        for (Map.Entry<Long, String> entry : clazzToProductMap.entrySet()) {
            Long clazzId = entry.getKey();
            String productId = entry.getValue();
            List<OrderProductItem> itemList = productToItemMap.get(productId);
            if (CollectionUtils.isEmpty(itemList)) {
                clazzToBookMap.put(clazzId, null);
            } else {//多教材的只取第一本教材
                clazzToBookMap.put(clazzId, itemList.get(0).getAppItemId());
            }
        }
//        logger.info(clazzToProductMap.size() + " ; clazzToBookMap cost : " + (System.currentTimeMillis() - t1));
        return clazzToBookMap;
    }

    /**
     * @return 班级对应的dayIndex的unit, map对应的value可能为null
     */
    private Map<Long, String> clazzToUnitMap(Map<Long, String> clazzToBookMap, int dayIndex) {
        if (MapUtils.isEmpty(clazzToBookMap)) {
            return Collections.emptyMap();
        }
        long t1 = System.currentTimeMillis();
        Map<Long, String> clazzToUnitMap = new HashMap<>();//班级对应的dayIndex的unit
        //教材石头堆数据
        Map<String, StoneData> bookStoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(clazzToBookMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        for (Map.Entry<Long, String> entry : clazzToBookMap.entrySet()) {
            Long clazzId = entry.getKey();
            String bookId = entry.getValue();
            if (StringUtils.isNotBlank(bookId) && bookStoneDataMap.get(bookId) != null) {
                String unitId = getUnit(StoneBookData.newInstance(bookStoneDataMap.get(bookId)), dayIndex);
                clazzToUnitMap.put(clazzId, unitId);
            } else {
                clazzToUnitMap.put(clazzId, null);
            }
        }
//        logger.info(clazzToBookMap.size() + " ;clazzToUnitMap cost : " + (System.currentTimeMillis() - t1));
        return clazzToUnitMap;
    }

    private String getUnit(StoneBookData bookData, int dayIndex) {
        if (bookData == null || bookData.getJsonData() == null || CollectionUtils.isEmpty(bookData.getJsonData().getChildren())) {
            return null;
        }
        List<StoneBookData.Node> unitList = bookData.getJsonData().getChildren();
        if (dayIndex < 1 || dayIndex > unitList.size()) {
            return null;
        }
        return unitList.get(dayIndex - 1).getStone_data_id();
    }

    /**
     * 每个班级下未退费的用户
     *
     * @param clazzCol
     * @return
     */
    private Map<Long, Set<Long>> clazzUserExceptRefund(Collection<Long> clazzCol) {
        if (CollectionUtils.isEmpty(clazzCol)) {
            return Collections.emptyMap();
        }
        Map<Long, Set<Long>> clazzToUserMap = new HashMap<>();
        long t1 = System.currentTimeMillis();
        for (Long clazzId : clazzCol) {
            if (RuntimeMode.lt(Mode.STAGING)) {
                clazzToUserMap.put(clazzId, chipsUserService.selectChipsEnglishClassUserRefByClazzId(clazzId).stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toSet()));
            } else {
                Set<Long> userIdSet = chipsUserService.selectChipsEnglishClassUserRefByClazzId(clazzId).stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toSet());
                ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName(ChipsActiveServiceImpl.superKey);
                if (config != null) {
                    List<Long> superList = Arrays.stream(config.getValue().split(",")).map(SafeConverter::toLong).collect(Collectors.toList());
                    userIdSet = userIdSet.stream().filter(u -> !superList.contains(u)).collect(Collectors.toSet());
                }
                clazzToUserMap.put(clazzId, userIdSet);
            }
        }
//        logger.info(clazzCol.size() + " ;clazzUserExceptRefund cost: " + (System.currentTimeMillis() - t1));
        return clazzToUserMap;
    }

    /**
     * 查找该unit 完课的用户
     *
     * @param userList
     * @param unitId
     * @return
     */
    private Set<Long> queryUserUnitResultFilterFinish(Set<Long> userList, String unitId) {
        if (CollectionUtils.isEmpty(userList) || StringUtils.isBlank(unitId)) {
            return Collections.emptySet();
        }
        Set<Long> set = new HashSet<>();
        List<AIUserUnitResultHistory> historyList = aiUserUnitResultHistoryDao.load(userList, unitId);
        for (AIUserUnitResultHistory unitResult : historyList) {
            if (unitResult != null && unitResult.getFinished()) {
                set.add(unitResult.getUserId());
            }
        }
        return set;
    }

    private Set<Long> queryUserUnitResultFilterFinish1(Set<Long> userList, String unitId) {
        if (CollectionUtils.isEmpty(userList) || StringUtils.isBlank(unitId)) {
            return Collections.emptySet();
        }
        Set<Long> set = new HashSet<>();
        for (Long userId : userList) {
            AIUserUnitResultHistory unitResult = aiUserUnitResultHistoryDao.load(userId, unitId);
            if (unitResult != null && unitResult.getFinished()) {
                set.add(userId);
            }
        }
        return set;
    }

    /**
     * 每个班级对应的完课人数
     *
     * @return
     */
    private Map<Long, Set<Long>> clazzToCompleteUserMap(Map<Long, Set<Long>> clazzToUserMap, Map<Long, String> clazzToUnitMap) {
        long t1 = System.currentTimeMillis();
        Set<Long> clazzSet = clazzToUnitMap.keySet();
        Map<Long, Set<Long>> clazzToCompleteMap = new HashMap<>();
        for (Long clazzId : clazzSet) {
            Set<Long> userList = clazzToUserMap.get(clazzId);
            String unitId = clazzToUnitMap.get(clazzId);
            //该单元 userList 对应的单元完成结果
            Set<Long> finishUserList = queryUserUnitResultFilterFinish(userList, unitId);
            clazzToCompleteMap.put(clazzId, finishUserList);
        }
//        logger.info(clazzSet.size() + " ;clazzToCompleteUserMap cost : " + (System.currentTimeMillis() - t1));
        return clazzToCompleteMap;
    }

    /**
     * 每个班级定级人数
     *
     * @param clazzToUserMap
     * @param clazzToBookMap
     * @return
     */
    private Map<Long, Set<Long>> clazzToGradeUserMap(Map<Long, Set<Long>> clazzToUserMap, Map<Long, String> clazzToBookMap) {
        long t1 = System.currentTimeMillis();
        Set<Long> clazzSet = clazzToBookMap.keySet();
        Map<Long, Set<Long>> clazzToGradeUserMap = new HashMap<>();
        for (Long clazzId : clazzSet) {
            Set<Long> userList = clazzToUserMap.get(clazzId);
            String bookId = clazzToBookMap.get(clazzId);
            // userList 对应的该教材的定级结果
            List<String> bookResultIdList = userList.stream().map(u -> AIUserBookResult.generateId(u, bookId)).collect(Collectors.toList());
            Map<String, AIUserBookResult> bookResultMap = aiUserBookResultDao.loadByIds(bookResultIdList);
            Set<Long> userSet = bookResultMap.values().stream().filter(e -> e != null && e.getLevel() != null).map(AIUserBookResult::getUserId).collect(Collectors.toSet());
            clazzToGradeUserMap.put(clazzId, userSet);
        }
//        logger.info(clazzSet.size() + " ;clazzToGradeUserMap cost: " + (System.currentTimeMillis() - t1));
        return clazzToGradeUserMap;
    }

    private Map<Long, Set<Long>> clazzToPaidRemindUserMap(Map<Long, Set<Long>> clazzToUserMap) {
        long t1 = System.currentTimeMillis();
        Map<Long, Set<Long>> clazzToPaidRemindUserMap = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : clazzToUserMap.entrySet()) {
            Long clazzId = entry.getKey();
            Set<Long> userList = clazzToUserMap.get(clazzId);
            if (CollectionUtils.isEmpty(userList)) {
                clazzToPaidRemindUserMap.put(clazzId, Collections.emptySet());
            } else {
                Set<String> idList = userList.stream().map(e -> ChipsActiveServiceType.RENEWREMIND.name() + "-" + e).collect(Collectors.toSet());
                Map<String, ChipsOtherServiceUserTemplate> userTemplateMap = chipsOtherServiceUserTemplateDao.loads(idList);
                Set<Long> userSet = userTemplateMap.values().stream().map(ChipsOtherServiceUserTemplate::getUserId).collect(Collectors.toSet());
                clazzToPaidRemindUserMap.put(clazzId, userSet);
            }
        }
//        logger.info(clazzToUserMap.size() + " ;clazzToPaidRemindUserMap cost : " + (System.currentTimeMillis() - t1));
        return clazzToPaidRemindUserMap;
    }

    private ClazzComparePojo buildCurrentDayData(ChipsClazzCompare clazzCompare, int dayIndex) {
        long t1 = System.currentTimeMillis();
        String compareClazzIds = clazzCompare.getCompareClazzIds();
        if (StringUtils.isBlank(compareClazzIds)) {
            return null;
        }
        Map<Long, ChipsEnglishClass> clazzMap = loadChipsEnglishClass(compareClazzIds);
        ClazzComparePojo pojo = new ClazzComparePojo();
        //每个班级
        Map<Long, String> clazzToBookMap = clazzToBookMap(clazzMap);
        Map<Long, String> clazzToUnitMap = clazzToUnitMap(clazzToBookMap, dayIndex);
        //班级对应的未退费的用户 --> 学习人数
        Map<Long, Set<Long>> clazzToUserMap = clazzUserExceptRefund(clazzMap.keySet());
        //每个班级dayIndex对应的完课人数
        Map<Long, Set<Long>> clazzToCompleteUserMap = clazzToCompleteUserMap(clazzToUserMap, clazzToUnitMap);
        //改教材下的定级用户
        Map<Long, Set<Long>> clazzToGradeUserMap = clazzToGradeUserMap(clazzToUserMap, clazzToBookMap);
        Map<Long, Set<Long>> clazzToPaidRemindUserMap = clazzToPaidRemindUserMap(clazzToUserMap);
        //学习人数
        int totalUserCount = 0;//学习人数
        int totalCompleteCount = 0;//完课人数
        List<ClazzComparePojo.FirstTable> firstTableList = new ArrayList<>();
        List<ClazzComparePojo.SecondTable> secondTableList = new ArrayList<>();
        List<ClazzComparePojo.ThridTable> thridTableList = new ArrayList<>();
        int totalRemarkCount = 0;//完课点评人数
        int totalPaidCount = 0;//续费人数
        long t2 = System.currentTimeMillis();
        for (Long clazzId : clazzMap.keySet()) {
            int userCount = clazzToUserMap.get(clazzId) == null ? 0 : clazzToUserMap.get(clazzId).size();
            int completeCount = clazzToCompleteUserMap.get(clazzId) == null ? 0 : clazzToCompleteUserMap.get(clazzId).size();
            Long remindCount = 0l;
            Long remarkCount = 0l;
            String unitId = clazzToUnitMap.get(clazzId);
            if (StringUtils.isNotBlank(unitId)) {
                List<ChipsActiveServiceRecord> remindRecordList = chipsActiveServiceRecordDao.loadByClassId(ChipsActiveServiceType.REMIND, clazzId);
                remindCount = remindRecordList.stream().filter(r -> r.getServiced() && unitId.equals(r.getUnitId())).count();
                List<ChipsActiveServiceRecord> serviceRecordList = chipsActiveServiceRecordDao.loadByClassId(ChipsActiveServiceType.SERVICE, clazzId);
                remarkCount = serviceRecordList.stream().filter(r -> r.getServiced() && unitId.equals(r.getUnitId())).count();
            }
            totalUserCount += userCount;
            totalCompleteCount += completeCount;
            totalRemarkCount += remarkCount;
            ClazzComparePojo.FirstTable firstTable = new ClazzComparePojo.FirstTable();
            firstTable.setClazzId(clazzId);
            firstTable.setProductId(clazzMap.get(clazzId).getProductId());
            firstTable.setClazzName(clazzMap.get(clazzId).getName());
            firstTable.setUserCount(userCount);
            firstTable.setCompleteCount(completeCount);
            firstTable.setCompleteRate(formatRateToDouble(completeCount, userCount));
            firstTable.setCompleteRateStr(formatRate(completeCount, userCount));
            firstTable.setRemindCount(remindCount.intValue());

            ClazzComparePojo.SecondTable secondTable = new ClazzComparePojo.SecondTable();
            secondTable.setClazzId(clazzId);
            secondTable.setProductId(clazzMap.get(clazzId).getProductId());
            secondTable.setClazzName(clazzMap.get(clazzId).getName());
            secondTable.setCompleteCount(completeCount);
            secondTable.setRemarkCount(remarkCount.intValue());
            secondTable.setRemarkRate(formatRateToDouble(remarkCount.intValue(), completeCount));
            secondTable.setRemarkRateStr(formatRate(remarkCount.intValue(), completeCount));

            ClazzComparePojo.ThridTable thridTable = new ClazzComparePojo.ThridTable();
            thridTable.setClazzId(clazzId);
            thridTable.setProductId(clazzMap.get(clazzId).getProductId());
            thridTable.setClazzName(clazzMap.get(clazzId).getName());
            int gradeCount = clazzToGradeUserMap.get(clazzId) == null ? 0 : clazzToGradeUserMap.get(clazzId).size();

//            int paidCount = 0;
//            List<ChipsClassStatistics> clazzStatList = chipsClassStatisticsDao.loadByClassId(clazzId);
//            if (CollectionUtils.isNotEmpty(clazzStatList) && clazzStatList.get(0) != null) {
//                ChipsClassStatistics clazzStat = clazzStatList.get(0);
//                paidCount = clazzStat.getClassPaidNum();
//                totalPaidCount += clazzStat.getClassPaidNum();
//            }
            int paidCount = calRenewCount(clazzMap.get(clazzId));
            totalPaidCount += paidCount;
            thridTable.setGradeCount(gradeCount);
            thridTable.setPaidCount(paidCount);
            thridTable.setPaidRate(formatRateToDouble(paidCount, userCount));
            thridTable.setPaidRateStr(formatRate(paidCount, userCount));
            thridTable.setPaidRemindCount(clazzToPaidRemindUserMap.get(clazzId) == null ? 0 : clazzToPaidRemindUserMap.get(clazzId).size());//TODO
            firstTableList.add(firstTable);
            secondTableList.add(secondTable);
            thridTableList.add(thridTable);
        }
        pojo.setTotalUserCount(totalUserCount);
        pojo.setTotalCompleteCount(totalCompleteCount);
        pojo.setTotalCompleteRate(formatRate(totalCompleteCount, totalUserCount));
        firstTableList.sort(Comparator.comparing(ClazzComparePojo.FirstTable::getCompleteRate).reversed());
        pojo.setFirstList(firstTableList);
        pojo.setTotalRemarkCount(totalRemarkCount);
        pojo.setTotalRemarkRate(formatRate(totalRemarkCount, totalCompleteCount));
        secondTableList.sort(Comparator.comparing(ClazzComparePojo.SecondTable::getRemarkRate).reversed());
        pojo.setSecondList(secondTableList);
        pojo.setTotalPaidCount(totalPaidCount);
        pojo.setTotalPaidRate(formatRate(totalPaidCount, totalUserCount));
        thridTableList.sort(Comparator.comparing(ClazzComparePojo.ThridTable::getPaidRate).reversed());
        pojo.setThridList(thridTableList);
        long end = System.currentTimeMillis();
//        logger.info("buildCurrentDayData cost " + (end - t1) + "; end - t2 :" + (end - t2));
        return pojo;
    }

    public int dayIndexCount(Long clazzId) {
        if (clazzId == null || clazzId == 0l) {
            return 0;
        }
        ChipsEnglishClass chipsEnglishClass = chipsEnglishClassPersistence.load(clazzId);
        if (chipsEnglishClass == null || StringUtils.isBlank(chipsEnglishClass.getProductId())) {
            return 0;
        }
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(chipsEnglishClass.getProductId());
        if (CollectionUtils.isEmpty(itemList)) {
            return 0;
        }
        OrderProductItem item = itemList.get(0);
        if (item == null || StringUtils.isBlank(item.getAppItemId())) {
            return 0;
        }
        Map<String, StoneData> bookStoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(item.getAppItemId()));
        StoneData stoneData = bookStoneDataMap.get(item.getAppItemId());
        if (stoneData == null) {
            return 0;
        }
        StoneBookData stoneBookData = StoneBookData.newInstance(stoneData);
        if (stoneBookData == null || stoneBookData.getJsonData() == null || CollectionUtils.isEmpty(stoneBookData.getJsonData().getChildren())) {
            return 0;
        }
        return stoneBookData.getJsonData().getChildren().size();
    }

    @Override
    public MapMessage removeClazzCompareData(String id) {
        boolean remove = chipsClazzCompareDao.remove(id);
        if (remove) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage().add("info", "删除失败");
        }
    }

    @Override
    public List<ChipsEnglishClass> loadAllChipsEnglishClass() {
        return chipsEnglishClassPersistence.loadAll();
    }

    /**
     * 更改ChipsEnglishClass的productId 字段，并更改该班级下所有用户的ChipsUserCourse 信息
     *
     * @param clazzId
     * @param productId
     * @return
     */
    @Override
    public MapMessage updateChipsEnglishClassProduct(Long clazzId, String productId) {
        if (clazzId == null || clazzId == 0l || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("input param is wrong clazzId: " + clazzId + " ; productId : " + productId);
        }
        ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("no ChipsEnglishClass id: " + clazzId);
        }
        chipsEnglishClassPersistence.updateProductId(clazz, productId);
        //更新ChipsUserCourse
        updateProductAndProductItem(clazzId, clazz.getProductId(), productId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateUserClazzAndUserCourse(Long userId,Long originClazzId, Long clazzId,String originProductId, String productId) {
        if (clazzId == null || clazzId == 0l || StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("input param is wrong clazzId: " + clazzId + " ; productId : " + productId);
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        ChipsEnglishClassUserRef userRef = userRefList.stream().filter(e -> e.getChipsClassId().equals(originClazzId)).findFirst().orElse(null);
        if (userRef == null) {
            return MapMessage.errorMessage("can not find record from user_ref, userId: " + userId + "; clazzId: " + originClazzId);
        }
        //更新UserRef
        try {
            chipsEnglishClassUserRefPersistence.disabled(userRef);
            userRef.setChipsClassId(clazzId);
            userRef.setId(null);
            chipsEnglishClassUserRefPersistence.insertOrUpdate(userRef);
        } catch (Exception e) {
            logger.info("updateUserClazzAndUserCourse fail user: " + userRef.getUserId() + "; clazzId: " + userRef.getChipsClassId() + "; aimClazzId: " + clazzId);
        }
        //更新ChipsUserCourse
        String productItemId = productItem(productId);
        chipsUserCoursePersistence.updateProductAndProductItem(userRef.getUserId(), productId, productItemId, originProductId);
        return MapMessage.successMessage();
    }

    private String productItem(String productId) {
        return Optional.ofNullable(productId).map(e -> userOrderLoaderClient.loadProductItemsByProductId(e)).filter(l -> CollectionUtils.isNotEmpty(l)).map(l -> l.get(0)).map(e -> e.getId()).orElse(null);
    }

    /**
     * 更新该班级下所有用户的ChipsUserCourse
     *
     * @param clazzId
     * @param productId
     */
    private void updateProductAndProductItem(Long clazzId, String originProductId, String productId) {
        String productItemId = productItem(productId);
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByClassId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return;
        }
        for (ChipsEnglishClassUserRef userRef : userRefList) {
            chipsUserCoursePersistence.updateProductAndProductItem(userRef.getUserId(), productId, productItemId, originProductId);
        }
    }

    @Override
    public MapMessage insertChipsEnglishClassUpdateLog(ChipsEnglishClassUpdateLog log) {
        if (log == null) {
            return MapMessage.errorMessage("ChipsEnglishClassUpdateLog is null ");
        }
        chipsEnglishClassUpdateLogDao.insert(log);
        return MapMessage.successMessage();
    }

    @Override
    public List<ChipsUserCourse> loadChipsUserCourseByUserId(Long userId) {
        return chipsUserService.loadUserAllCourse(userId);
    }

    /**
     * 计算一个产品的续费人数
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=46011009
     * 续费人数＝用户加入该班级的日期后，又产生了“薯条英语”的订单（订单的“产品服务类型“为ChipsEnglish，“创建时间“需要晚于用户购买该班级所在产品的时间）
     * 续费率＝续费人数／该班级的学习人数
     * @param clazz
     * @return
     */
    public int calRenewCount(ChipsEnglishClass clazz) {
        if (clazz == null || clazz.getId() == null || StringUtils.isBlank(clazz.getProductId())) {
            return 0;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByClassId(clazz.getId());
        if (CollectionUtils.isEmpty(userRefList)) {
            return 0;
        }
        int count = 0;
        for (ChipsEnglishClassUserRef userRef : userRefList) {
            List<ChipsUserCourse> chipsUserCourses = chipsUserCoursePersistence.loadByUserId(userRef.getUserId());
            Date createTime = chipsUserCourses.stream().filter(e -> Boolean.TRUE.equals(e.getActive())).filter(e -> StringUtils.isNotBlank(e.getProductId()))
                    .filter(e -> e.getProductId().equals(clazz.getProductId())).map(e -> e.getCreateTime()).findFirst().orElse(null);
            if (createTime == null) {
                continue;
            }
            ChipsUserCourse ct = chipsUserCourses.stream() .filter(e -> Boolean.TRUE.equals(e.getActive()))
                    .filter(e -> StringUtils.isNotBlank(e.getProductId())).filter(e -> e.getCreateTime().after(createTime)).findFirst().orElse(null);
            if (ct == null) {
                continue;
            }
            count ++;
        }
        return count;
    }

}
