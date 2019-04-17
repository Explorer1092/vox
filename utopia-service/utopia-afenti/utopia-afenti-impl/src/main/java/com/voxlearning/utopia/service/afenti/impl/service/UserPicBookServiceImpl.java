package com.voxlearning.utopia.service.afenti.impl.service;

import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.UserPicBookService;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookPurchaseGift;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankType;
import com.voxlearning.utopia.service.afenti.api.data.PicBookPurchaseProp;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankInfo;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankReward;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookStat;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.PicBookCacheSystem;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.StudentPicBookRankCacheManager;
import com.voxlearning.utopia.service.afenti.base.cache.managers.picbook.StudentPicBookSchoolRankCacheManager;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.afenti.cache.UserPicBookCache;
import com.voxlearning.utopia.service.afenti.impl.dao.PicBookStatDao;
import com.voxlearning.utopia.service.afenti.impl.dao.UserPicBookDao;
import com.voxlearning.utopia.service.afenti.impl.dao.UserPicBookResultDao;
import com.voxlearning.utopia.service.afenti.impl.service.internal.InternalPBReportService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ganhaitian on 2018/1/22.
 */
@Named
@ExposeService(interfaceClass = UserPicBookService.class)
public class UserPicBookServiceImpl extends SpringContainerSupport implements UserPicBookService {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private PictureBookLoaderClient pictureBookLoaderCli;
    @Inject private UserPicBookDao userPicBookDao;
    @Inject private UserPicBookResultDao userPicBookResultDao;
    @Inject private PicBookStatDao picBookStatDao;
    @Inject private InternalPBReportService internalPBReportSrv;
    @Inject private PicBookCacheSystem picBookCacheSystem;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private StudentPicBookRankCacheManager studentPicBookRankCacheManager;
    @Inject private StudentPicBookSchoolRankCacheManager studentPicBookSchoolRankCacheManager;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @AlpsPubsubPublisher(topic = "utopia.afenti.picbook.topic")
    private MessagePublisher messagePublisher;

    private IRedisCommands redisCommands;
    private UserPicBookCache cache;

    private static final String levelKey = "PicBookLevelConfig";
    private static final String levelKey2 = "level_list";

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
        cache = new UserPicBookCache(AfentiCache.getPersistent());
    }

    @Override
    public List<UserPicBook> loadAllUserPicBooks(Long userId) {
        if (userId == null || userId == 0L)
            return Collections.emptyList();

        return userPicBookDao.loadUserPicBookList(userId);
    }

    @Override
    public MapMessage report(List<UserPicBookResult> records) {
        try {
            Validate.noNullElements(records, "上报数据是空!");

            Map<String, Object> msgParams = new HashMap<>();
            msgParams.put("messageType", "reading");
            msgParams.put("records", records);

            // 处理上报数据
            MapMessage resultMsg = internalPBReportSrv.process(records);

            // 稍微耗点儿时间的，放消息里面后面慢慢弄
            Message message = Message.newMessage().writeObject(msgParams);
            messagePublisher.publish(message);

            return resultMsg;
        } catch (Exception e) {
            logger.error("Pic book report error!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage createUserPicBook(Long userId, String bookId, String orderProductServiceType) {
        try {
            UserPicBook existBook = loadUserPicBook(userId, bookId);
            Validate.isTrue(existBook == null, "已经存在!");

            AtomicCallback<MapMessage> callback = () -> {
                UserPicBook userPicBook = new UserPicBook();
                userPicBook.setUserId(userId);
                userPicBook.setBookId(bookId);
                userPicBook.setRead(false);
                userPicBook.setDisabled(false);
                userPicBook.setType(orderProductServiceType);
                userPicBook.setBuyTime(new Date().getTime());

                userPicBookDao.upsertUsrPicBook(userPicBook);
                // 增加销量
                picBookStatDao.incSales(bookId, 1);
                return MapMessage.successMessage();
            };

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("UserPicBookService:createUserPicBook")
                    .keys(userId, bookId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("操作太频繁!");
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        } finally {
            // 不过怎么样都移除下购物车
            removeShopingCartItem(userId, orderProductServiceType, Collections.singletonList(bookId));
        }
    }

    @Override
    public MapMessage updateUserPicBook(UserPicBook userPicBook) {
        try {
            Validate.notNull(userPicBook, "the pic book is null!");
            Validate.notNull(userPicBook.getUserId(), "the user id is null!");

            userPicBookDao.upsertUsrPicBook(userPicBook);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage saveUserPicBookHistory(UserPicBookResult history) {
        try {
            userPicBookResultDao.insert(history);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    private String buildKey(Object... keyParts) {
        return "UserPicBookShoppingCart1:" + StringUtils.join(keyParts, ":");
    }

    @Override
    public List<String> loadShopinCartBookIds(Long userId, String orderProductServiceType) {
        RedisSortedSetCommands<String, Object> redisSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        String cacheKey = buildKey("userId", userId, "type", orderProductServiceType);

        return redisSetCommands.zrange(cacheKey, 0, -1)
                .stream()
                .map(Objects::toString)
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage addShopinCartItem(Long userId, String orderProductServiceType, String bookId) {
        if (StringUtils.isBlank(bookId) || orderProductServiceType == null || userId == null)
            return MapMessage.errorMessage("非法的参数!");

        PictureBookPlus picBook = pictureBookPlusServiceClient.loadById(bookId);
        if (picBook == null) {
            return MapMessage.errorMessage("绘本不存在!");
        }

        // 值关联的score
        long now = new Date().getTime();

        RedisSortedSetCommands<String, Object> redisSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        String cacheKey = buildKey("userId", userId, "type", orderProductServiceType);

        redisSetCommands.zadd(cacheKey, now, bookId);

        Long numAfterAdd = redisSetCommands.zcard(cacheKey);
        return MapMessage.successMessage().add("itemNum", numAfterAdd);
    }

    @Override
    public List<String> removeShopingCartItem(Long userId, String orderProductServiceType, List<String> bookIds) {
        if (userId == null || userId == 0L || StringUtils.isEmpty(orderProductServiceType))
            return Collections.emptyList();

        RedisSortedSetCommands<String, Object> redisSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        String cacheKey = buildKey("userId", userId, "type", orderProductServiceType);

        List<String> orgItems = redisSetCommands.zrange(cacheKey, 0, -1)
                .stream()
                .map(SafeConverter::toString)
                .collect(Collectors.toList());

        // 原样返回
        if (CollectionUtils.isEmpty(bookIds))
            return orgItems;

        orgItems.removeAll(bookIds);

        Object[] bookIdValues = bookIds.toArray();
        redisSetCommands.zrem(cacheKey, bookIdValues);

        return orgItems;
    }

    @Override
    public UserPicBookAchieve getThisWeekAchieve(Long userId) {
        return cache.loadAchieve(userId);
    }

    @Override
    public List<PicBookStat> loadAllPicBookStat() {
        return picBookStatDao.loadAll();
    }

    @Override
    public MapMessage savePicBookStat(PicBookStat stat) {
        try {
            picBookStatDao.updatePicBookStat(stat);
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("Save pic book stat error!", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public Map<String, UserPicBookProgress> loadAllUserPicBookProgress(Long userId) {
        List<String> bookIds = loadAllUserPicBooks(userId)
                .stream()
                .map(pb -> pb.getBookId())
                .collect(Collectors.toList());
        return cache.loadProgress(userId, bookIds);
    }

    @Override
    public Map<String, Boolean> loadReadStatus(Long userId, Collection<String> bookIds) {
        Map<String, UserPicBookProgress> progressMap = loadAllUserPicBookProgress(userId);
        Function<String, Boolean> finishFunc = bookId -> {
            return Optional.ofNullable(progressMap.get(bookId))
                    .map(p -> p.isModuleFinished(1))
                    .orElse(false);
        };

        return bookIds.stream().collect(Collectors.toMap(booId -> booId, finishFunc));
    }

    @Override
    public MapMessage loadUserRanksInfo(Long userId, PicBookRankCategory rankCategory, PicBookRankType rankType, String cdnBaseUrl) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (student == null)
            return MapMessage.errorMessage("用户错误");
        MapMessage result = MapMessage.successMessage();

        if (rankType == PicBookRankType.SCHOOL) {
            if (rankCategory == PicBookRankCategory.READ) {
                result = loadSchoolReadRankResult(student, rankCategory, cdnBaseUrl);
            } else if (rankCategory == PicBookRankCategory.WORD) {
                result = loadSchoolWordRankResult(student, rankCategory, cdnBaseUrl);
            }
        } else if (rankType == PicBookRankType.ALL) {
            if (rankCategory == PicBookRankCategory.READ) {
                result = loadAllReadRankResult(student, rankCategory, cdnBaseUrl);
            } else if (rankCategory == PicBookRankCategory.WORD) {
                result = loadAllWordRankResult(student, rankCategory, cdnBaseUrl);
            }
        }
        return result;
    }

    private MapMessage loadAllWordRankResult(StudentDetail student, PicBookRankCategory rankCategory, String cdnBaseUrl) {
        Map<String, Object> myRank = new HashMap<>();
        myRank.put("img", getUserAvatarImgUrl(student.fetchImageUrl(), cdnBaseUrl));
        myRank.put("name", student.fetchRealname());
        // 去redis查询我的实际排名
        int week = WeekRange.current().getWeekOfYear();
        PicBookRankInfo rankInfo = studentPicBookRankCacheManager.getStudentRankByStudentIdAndRankType(student.getId(), rankCategory, week);
        if (rankInfo != null && rankInfo.getRank() != null) {
            myRank.put("num", rankInfo.getWordCount());
            myRank.put("rank", rankInfo.getRank());
        }
        // 查询全国榜
        List<Map<String, Object>> allRanksResult = new ArrayList<>();
        List<PicBookRankInfo> allRanks = picBookCacheSystem.getStudentPicBookTopRankCacheManager()
                .fetchStudentRankList(rankCategory, WeekRange.current().getWeekOfYear());
        if (CollectionUtils.isNotEmpty(allRanks)) {
            allRanks = allRanks.stream()
                    .filter(r -> r.getWordCount() != null && r.getWordCount() > 0)
                    .sorted((r1, r2) ->
                            (r2.getWordCount()).compareTo(r1.getWordCount()) == 0
                                    ? (r2.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    .compareTo(r1.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    : (r2.getWordCount()).compareTo(r1.getWordCount())
                    ).collect(Collectors.toList());

            // 将名次加入班级列表
            int rankIndex = 0;
            int rankCount = 0;
            long tempTotal = -1;
            for (PicBookRankInfo info : allRanks) {
                rankCount++;

                if (tempTotal != info.getWordCount()) {
                    // 总数不同时名次增加，反之名次并列
                    tempTotal = info.getWordCount();
                    rankIndex = rankCount;
                }

                // 显示到前100名
                if (rankIndex > 100) break;

                Map<String, Object> rankMap = new HashMap<>();
                rankMap.put("rank", rankIndex);
                rankMap.put("img", getUserAvatarImgUrl(info.getImg(), cdnBaseUrl));
                rankMap.put("name", info.getStudentName());
                rankMap.put("num", info.getWordCount());
                rankMap.put("clazzName", info.getClassName());
                rankMap.put("schoolName", info.getSchoolName());

                allRanksResult.add(rankMap);
            }
        }
        return MapMessage.successMessage().add("myRank", myRank).add("rankList", allRanksResult);
    }

    private MapMessage loadAllReadRankResult(StudentDetail student, PicBookRankCategory rankCategory, String cdnBaseUrl) {
        Map<String, Object> myRank = new HashMap<>();
        myRank.put("img", getUserAvatarImgUrl(student.fetchImageUrl(), cdnBaseUrl));
        myRank.put("name", student.fetchRealname());
        // 去redis查询我的实际排名
        int week = WeekRange.current().getWeekOfYear();
        PicBookRankInfo rankInfo = studentPicBookRankCacheManager.getStudentRankByStudentIdAndRankType(student.getId(), rankCategory, week);
        if (rankInfo != null && rankInfo.getRank() != null) {
            myRank.put("num", rankInfo.getReadCount());
            myRank.put("rank", rankInfo.getRank());
        }
        // 查询全国榜
        List<Map<String, Object>> allRanksResult = new ArrayList<>();
        List<PicBookRankInfo> allRanks = picBookCacheSystem.getStudentPicBookTopRankCacheManager()
                .fetchStudentRankList(rankCategory, WeekRange.current().getWeekOfYear());
        if (CollectionUtils.isNotEmpty(allRanks)) {
            allRanks = allRanks.stream()
                    .filter(r -> r.getReadCount() != null && r.getReadCount() > 0)
                    .sorted((r1, r2) ->
                            (r2.getReadCount()).compareTo(r1.getReadCount()) == 0
                                    ? (r2.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    .compareTo(r1.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    : (r2.getReadCount()).compareTo(r1.getReadCount())
                    ).collect(Collectors.toList());

            // 将名次加入班级列表
            int rankIndex = 0;
            int rankCount = 0;
            long tempTotal = -1;
            for (PicBookRankInfo info : allRanks) {
                rankCount++;

                if (tempTotal != info.getReadCount()) {
                    // 总数不同时名次增加，反之名次并列
                    tempTotal = info.getReadCount();
                    rankIndex = rankCount;
                }

                // 显示到前100名
                if (rankIndex > 100) break;

                Map<String, Object> rankMap = new HashMap<>();
                rankMap.put("rank", rankIndex);
                rankMap.put("img", getUserAvatarImgUrl(info.getImg(), cdnBaseUrl));
                rankMap.put("name", info.getStudentName());
                rankMap.put("num", info.getReadCount());
                rankMap.put("clazzName", info.getClassName());
                rankMap.put("schoolName", info.getSchoolName());

                allRanksResult.add(rankMap);
            }
        }
        return MapMessage.successMessage().add("myRank", myRank).add("rankList", allRanksResult);
    }

    private MapMessage loadSchoolWordRankResult(StudentDetail student, PicBookRankCategory rankCategory, String cdnBaseUrl) {
        Map<String, Object> myRank = new HashMap<>();
        myRank.put("img", getUserAvatarImgUrl(student.fetchImageUrl(), cdnBaseUrl));
        myRank.put("name", student.fetchRealname());
        // 去redis查询我的实际排名
        int week = WeekRange.current().getWeekOfYear();
        PicBookRankInfo rankInfo = studentPicBookSchoolRankCacheManager.getStudentRankByStudentIdAndRankType(student.getId(),
                rankCategory, student.getClazz().getSchoolId(), week);
        if (rankInfo != null && rankInfo.getRank() != null) {
            myRank.put("num", rankInfo.getWordCount());
            myRank.put("rank", rankInfo.getRank());
        }

        // 查询全校榜
        List<Map<String, Object>> schoolRanksResult = new ArrayList<>();
        List<PicBookRankInfo> schoolRanks = picBookCacheSystem.getStudentPicBookTopSchoolRankCacheManager()
                .fetchStudentRankList(student.getClazz().getSchoolId(), rankCategory, WeekRange.current().getWeekOfYear());
        if (CollectionUtils.isNotEmpty(schoolRanks)) {
            schoolRanks = schoolRanks.stream()
                    .filter(r -> r.getWordCount() != null && r.getWordCount() > 0)
                    .sorted((r1, r2) ->
                            (r2.getWordCount()).compareTo(r1.getWordCount()) == 0
                                    ? (r2.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    .compareTo(r1.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    : (r2.getWordCount()).compareTo(r1.getWordCount())
                    ).collect(Collectors.toList());

            // 将名次加入班级列表
            int rankIndex = 0;
            int rankCount = 0;
            long tempTotal = -1;
            for (PicBookRankInfo info : schoolRanks) {
                rankCount++;

                if (tempTotal != info.getWordCount()) {
                    // 总数不同时名次增加，反之名次并列
                    tempTotal = info.getWordCount();
                    rankIndex = rankCount;
                }

                // 显示到前10名
                if (rankIndex > 10) break;

                Map<String, Object> rankMap = new HashMap<>();
                rankMap.put("rank", rankIndex);
                rankMap.put("img", getUserAvatarImgUrl(info.getImg(), cdnBaseUrl));
                rankMap.put("name", info.getStudentName());
                rankMap.put("num", info.getWordCount());
                rankMap.put("clazzName", info.getClassName());
                rankMap.put("schoolName", info.getSchoolName());

                schoolRanksResult.add(rankMap);
            }
        }
        return MapMessage.successMessage().add("myRank", myRank).add("rankList", schoolRanksResult);
    }

    private MapMessage loadSchoolReadRankResult(StudentDetail student, PicBookRankCategory rankCategory, String cdnBaseUrl) {
        Map<String, Object> myRank = new HashMap<>();
        myRank.put("img", getUserAvatarImgUrl(student.fetchImageUrl(), cdnBaseUrl));
        myRank.put("name", student.fetchRealname());
        // 去redis查询我的实际排名
        int week = WeekRange.current().getWeekOfYear();
        PicBookRankInfo rankInfo = studentPicBookSchoolRankCacheManager.getStudentRankByStudentIdAndRankType(student.getId(),
                rankCategory, student.getClazz().getSchoolId(), week);
        if (rankInfo != null && rankInfo.getRank() != null) {
            myRank.put("num", rankInfo.getReadCount());
            myRank.put("rank", rankInfo.getRank());
        }

        // 查询全校榜
        List<Map<String, Object>> schoolRanksResult = new ArrayList<>();
        List<PicBookRankInfo> schoolRanks = picBookCacheSystem.getStudentPicBookTopSchoolRankCacheManager()
                .fetchStudentRankList(student.getClazz().getSchoolId(), rankCategory, WeekRange.current().getWeekOfYear());
        if (CollectionUtils.isNotEmpty(schoolRanks)) {
            schoolRanks = schoolRanks.stream()
                    .filter(r -> r.getReadCount() != null && r.getReadCount() > 0)
                    .sorted((r1, r2) ->
                            (r2.getReadCount()).compareTo(r1.getReadCount()) == 0
                                    ? (r2.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    .compareTo(r1.getStudentId().equals(student.getId()) ? new Integer(1) : new Integer(0))
                                    : (r2.getReadCount()).compareTo(r1.getReadCount())
                    ).collect(Collectors.toList());

            // 将名次加入列表
            int rankIndex = 0;
            int rankCount = 0;
            long tempTotal = -1;
            for (PicBookRankInfo info : schoolRanks) {
                rankCount++;

                if (tempTotal != info.getReadCount()) {
                    // 总数不同时名次增加，反之名次并列
                    tempTotal = info.getReadCount();
                    rankIndex = rankCount;
                }

                // 显示到前10名
                if (rankIndex > 10) break;

                Map<String, Object> rankMap = new HashMap<>();
                rankMap.put("rank", rankIndex);
                rankMap.put("img", getUserAvatarImgUrl(info.getImg(), cdnBaseUrl));
                rankMap.put("name", info.getStudentName());
                rankMap.put("num", info.getReadCount());
                rankMap.put("clazzName", info.getClassName());
                rankMap.put("schoolName", info.getSchoolName());

                schoolRanksResult.add(rankMap);
            }
        }
        return MapMessage.successMessage().add("myRank", myRank).add("rankList", schoolRanksResult);
    }


    @Override
    public Map<String, List<PicBookPurchaseProp>> getRewardListByBookIds(List<String> bookIds) {
        Map<String, List<PicBookPurchaseProp>> propMap = new HashMap<>();
        Map<String, PictureBookPlus> bookPlusMap = pictureBookPlusServiceClient.loadByIds(bookIds);
        if (MapUtils.isNotEmpty(bookPlusMap)) {

            PageBlockContent blockContent = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(levelKey)
                    .stream()
                    .filter(e -> e.getDisabled() == null || !e.getDisabled())
                    .filter(p -> Objects.equals(levelKey2, p.getBlockName()))
                    .findFirst().orElse(null);
            List<String> jx = new ArrayList<>();
            List<String> yz = new ArrayList<>();
            List<String> tj = new ArrayList<>();
            if (blockContent != null) {
                Map<String, Object> configMap = JsonUtils.convertJsonObjectToMap(blockContent.getContent());
                if (configMap != null) {
                    jx = (List<String>) configMap.get("jx");
                    yz = (List<String>) configMap.get("yz");
                    tj = (List<String>) configMap.get("tj");
                }
            }

            for (Map.Entry<String, PictureBookPlus> entry : bookPlusMap.entrySet()) {
                String seriesId = entry.getValue().getSeriesId();
                List<PicBookPurchaseProp> propList = new ArrayList<>();
                PicBookPurchaseProp prop;

                if (jx.contains(seriesId)) {// 剑桥彩虹 培生  Farfaria
                    // 汉堡*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.H_B.getId());
                    prop.setName(PicBookPurchaseGift.H_B.getCname());
                    prop.setImg(PicBookPurchaseGift.H_B.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.H_B.getDesc());
                    propList.add(prop);

                    // 竞技卡*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.J_J_K.getId());
                    prop.setName(PicBookPurchaseGift.J_J_K.getCname());
                    prop.setImg(PicBookPurchaseGift.J_J_K.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.J_J_K.getDesc());
                    propList.add(prop);

                    // 万能宝石*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.W_N_B_S.getId());
                    prop.setName(PicBookPurchaseGift.W_N_B_S.getCname());
                    prop.setImg(PicBookPurchaseGift.W_N_B_S.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.W_N_B_S.getDesc());
                    propList.add(prop);

                    // 自学积分*10
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.Z_X_J_F.getId());
                    prop.setName(PicBookPurchaseGift.Z_X_J_F.getCname());
                    prop.setImg(PicBookPurchaseGift.Z_X_J_F.getImg());
                    prop.setNum(10);
                    prop.setDesc(PicBookPurchaseGift.Z_X_J_F.getDesc());
                    propList.add(prop);
                } else if (yz.contains(seriesId)) {// e-future
                    // 汉堡*4
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.H_B.getId());
                    prop.setName(PicBookPurchaseGift.H_B.getCname());
                    prop.setImg(PicBookPurchaseGift.H_B.getImg());
                    prop.setNum(4);
                    prop.setDesc(PicBookPurchaseGift.H_B.getDesc());
                    propList.add(prop);

                    // 竞技卡*4
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.J_J_K.getId());
                    prop.setName(PicBookPurchaseGift.J_J_K.getCname());
                    prop.setImg(PicBookPurchaseGift.J_J_K.getImg());
                    prop.setNum(4);
                    prop.setDesc(PicBookPurchaseGift.J_J_K.getDesc());
                    propList.add(prop);

                    // 万能宝石*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.W_N_B_S.getId());
                    prop.setName(PicBookPurchaseGift.W_N_B_S.getCname());
                    prop.setImg(PicBookPurchaseGift.W_N_B_S.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.W_N_B_S.getDesc());
                    propList.add(prop);

                    // 自学积分*10
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.Z_X_J_F.getId());
                    prop.setName(PicBookPurchaseGift.Z_X_J_F.getCname());
                    prop.setImg(PicBookPurchaseGift.Z_X_J_F.getImg());
                    prop.setNum(10);
                    prop.setDesc(PicBookPurchaseGift.Z_X_J_F.getDesc());
                    propList.add(prop);
                } else if (tj.contains(seriesId)) {// 英语文化读本
                    // 冰激凌*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.B_J_L.getId());
                    prop.setName(PicBookPurchaseGift.B_J_L.getCname());
                    prop.setImg(PicBookPurchaseGift.B_J_L.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.B_J_L.getDesc());
                    propList.add(prop);

                    // 竞技卡*3
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.J_J_K.getId());
                    prop.setName(PicBookPurchaseGift.J_J_K.getCname());
                    prop.setImg(PicBookPurchaseGift.J_J_K.getImg());
                    prop.setNum(3);
                    prop.setDesc(PicBookPurchaseGift.J_J_K.getDesc());
                    propList.add(prop);

                    // 万能宝石*5
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.W_N_B_S.getId());
                    prop.setName(PicBookPurchaseGift.W_N_B_S.getCname());
                    prop.setImg(PicBookPurchaseGift.W_N_B_S.getImg());
                    prop.setNum(5);
                    prop.setDesc(PicBookPurchaseGift.W_N_B_S.getDesc());
                    propList.add(prop);

                    // 自学积分*10
                    prop = new PicBookPurchaseProp();
                    prop.setId(PicBookPurchaseGift.Z_X_J_F.getId());
                    prop.setName(PicBookPurchaseGift.Z_X_J_F.getCname());
                    prop.setImg(PicBookPurchaseGift.Z_X_J_F.getImg());
                    prop.setNum(10);
                    prop.setDesc(PicBookPurchaseGift.Z_X_J_F.getDesc());
                    propList.add(prop);
                }

                propMap.put(entry.getKey(), propList);
            }
        }
        return propMap;
    }

    @Override
    public PicBookRankReward loadUserWeekRankReward(Long stuId, int week) {
        if (stuId == null || week == 0) {
            return null;
        }
        PicBookRankReward rankReward = new PicBookRankReward();
        rankReward.setShow(false);
        return rankReward;

//        boolean showCard = picBookCacheSystem.getStudentPicBookWeekRankRewardCacheManager().showCard(stuId);
//        PicBookRankReward rankReward = new PicBookRankReward();
//        if (!showCard) {
//            rankReward.setShow(false);
//            return rankReward;
//        }
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(stuId);
//        if (studentDetail == null) {
//            return null;
//        }
//        rankReward.setShow(true);
//        // 没弹过，来看一下上周排名
//        // 全国
//        boolean inRank = false;
//        PicBookRankInfo readAllRank = studentPicBookRankCacheManager.getStudentRankByStudentIdAndRankType(stuId, PicBookRankCategory.READ, week);
//        int totalRewardScore = 0;
//        if (readAllRank == null) {
//            rankReward.setReadRank("-");
//        } else {
//            rankReward.setReadRank(readAllRank.getRank() >= 10000 ? "10000+" : readAllRank.getRank().toString());
//            if (readAllRank.getRank() <= 100) {
//                inRank = true;
//                // 计算发奖总数
//                totalRewardScore = totalRewardScore + getRewardByRank(readAllRank.getRank());
//            }
//        }
//        PicBookRankInfo wordAllRank = studentPicBookRankCacheManager.getStudentRankByStudentIdAndRankType(stuId, PicBookRankCategory.WORD, week);
//        if (wordAllRank == null) {
//            rankReward.setWordRank("-");
//        } else {
//            rankReward.setWordRank(wordAllRank.getRank() >= 10000 ? "10000+" : wordAllRank.getRank().toString());
//
//            if (wordAllRank.getRank() <= 100) {
//                inRank = true;
//                totalRewardScore = totalRewardScore + getRewardByRank(wordAllRank.getRank());
//            }
//        }
//
//        // 全校
//        if (studentDetail.getClazz() == null || studentDetail.getClazz().getSchoolId() == null) {
//            rankReward.setReadRankSchool("-");
//            rankReward.setWordRankSchool("-");
//        } else {
//            PicBookRankInfo readSchoolRank = studentPicBookSchoolRankCacheManager.getStudentRankByStudentIdAndRankType(stuId, PicBookRankCategory.READ,
//                    studentDetail.getClazz().getSchoolId(), week);
//            if (readSchoolRank != null) {
//                rankReward.setReadRankSchool(readSchoolRank.getRank().toString());
//                if (readSchoolRank.getRank() <= 10) {
//                    inRank = true;
//                    totalRewardScore = totalRewardScore + getRewardBySchoolRank(readSchoolRank.getRank());
//                }
//            } else {
//                rankReward.setReadRankSchool("-");
//            }
//
//            PicBookRankInfo wordSchoolRank = studentPicBookSchoolRankCacheManager.getStudentRankByStudentIdAndRankType(stuId, PicBookRankCategory.WORD,
//                    studentDetail.getClazz().getSchoolId(), week);
//            if (wordSchoolRank != null) {
//                rankReward.setWordRankSchool(wordSchoolRank.getRank().toString());
//                if (wordSchoolRank.getRank() <= 10) {
//                    inRank = true;
//                    totalRewardScore = totalRewardScore + getRewardBySchoolRank(wordSchoolRank.getRank());
//                }
//            } else {
//                rankReward.setWordRankSchool("-");
//            }
//        }
//        rankReward.setInRank(inRank);
//        if (inRank) {
//            // 发奖
//            CreditHistory hs = CreditHistoryBuilderFactory.newBuilder(stuId, CreditType.ELevelReading_incr)
//                    .withAmount(totalRewardScore)
//                    .withComment("小U绘本排行榜奖励自学积分")
//                    .build();
//            userIntegralServiceClient.getUserIntegralService().changeCredit(hs);
//            rankReward.setTotalRewardScore(totalRewardScore);
//        } else {
//            rankReward.setCallName(studentDetail.fetchRealname());
//            rankReward.setContent("很遗憾，在上周的阅读成就排行榜中你没有上榜，要继续努力哦~");
//        }
//
//        // 记录弹窗
//        picBookCacheSystem.getStudentPicBookWeekRankRewardCacheManager().pop(stuId);
//
//        return rankReward;
    }

    private String getUserAvatarImgUrl(String imgFile, String cdnBaseUrl) {
        String imgUrl;
        if (!StringUtils.isEmpty(imgFile)) {
            imgUrl = "gridfs/" + imgFile;
            return cdnBaseUrl + imgUrl;
        } else {
            return "";
        }
    }

    private int getRewardBySchoolRank(Integer rank) {
        if (rank == 1) {
            return 10;
        } else if (rank == 2) {
            return 8;
        } else if (rank == 3) {
            return 5;
        } else if (rank >= 4 && rank <= 10) {
            return 2;
        }
        return 0;
    }

    private int getRewardByRank(Integer rank) {
        if (rank == 1) {
            return 500;
        } else if (rank == 2) {
            return 300;
        } else if (rank == 3) {
            return 100;
        } else if (rank >= 4 && rank <= 20) {
            return 30;
        } else if (rank >= 21 && rank <= 100) {
            return 10;
        }
        return 0;
    }
}
