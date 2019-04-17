package com.voxlearning.utopia.service.afenti.client;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.UserPicBookService;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookStat;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.PicBookContext;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.CollectionUtils.containsAny;
import static com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * 这个类实现的依赖非常非常有问题！先临时传入进来。不要依赖那些不属于你自己的client。。。。
 */
public class UserPicBookServiceClient implements InitializingBean {

    private static final int ENGLISH_READING = 1;
    private static final int ENGLISH_OFTEN_USED = 2;
    private static final int ENGLISH_ORAL = 3;
    private static final int ENGLISH_PRACTISE = 4;

    private static final int FEATURED_FREE_NUM = 2;
    private static final int FEATURED_CHARGE_NUM = 7;
    private static final int FEATURED_CHARGE_POOL_SIZE = 12;

    private static final int BUY_TIME_INTERVAL = 30;

    private final Map<String, String> CLAZZ_LEVEL_MAP = new LinkedHashMap<>();

    {
        CLAZZ_LEVEL_MAP.put(L0.name(), "预备级");
        CLAZZ_LEVEL_MAP.put(L1A.name(), "一级A");
        CLAZZ_LEVEL_MAP.put(L1B.name(), "一级B");
        CLAZZ_LEVEL_MAP.put(L2A.name(), "二级A");
        CLAZZ_LEVEL_MAP.put(L2B.name(), "二级B");
        CLAZZ_LEVEL_MAP.put(L3A.name(), "三级A");
        CLAZZ_LEVEL_MAP.put(L3B.name(), "三级B");
        CLAZZ_LEVEL_MAP.put(L4A.name(), "四级A");
        CLAZZ_LEVEL_MAP.put(L4B.name(), "四级B");
        CLAZZ_LEVEL_MAP.put(L5A.name(), "五级A");
        CLAZZ_LEVEL_MAP.put(L5B.name(), "五级B");
        CLAZZ_LEVEL_MAP.put(L6A.name(), "六级A");
        CLAZZ_LEVEL_MAP.put(L6B.name(), "六级B");
    }

    @Getter
    @ImportService(interfaceClass = UserPicBookService.class)
    UserPicBookService remoteReference;

    @Inject private PicBookContentLoaderManagement contentLoaderManagement;
    @Inject private NewContentLoaderClient contentLoaderClient;
    @Inject private UserOrderLoaderClient usrOrderLoaderCli;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public interface PicBookContentLoader {
        MapMessage loadContent(PictureBookPlus pictureBook, int module);
    }

    /**
     * 获取用户下面对应type类型的绘本记录。按照购买时间(备选阅读时间)来由近及远来排序
     *
     * @param userId 用户ID
     * @param type   对应OrderServiceProductType里面的值
     * @return 用户绘本实体列表
     */
    public List<UserPicBook> loadAllUserPicBooks(Long userId, String type) {
        return remoteReference.loadAllUserPicBooks(userId)
                .stream()
                .filter(pb -> Objects.equals(pb.getType(), type))
                .filter(pb -> !SafeConverter.toBoolean(pb.getDisabled()))
                .collect(Collectors.toList());
    }

    /**
     * 我的绘本中，需要过滤付费且未付款的绘本
     *
     * @return
     */
    private Predicate<PictureBookPlus> getMyBooksFilter(Long userId, String appKey) {
        // 获得用户购买历史信息
        AppPayMapper payMapper = usrOrderLoaderCli.getUserAppPaidStatus(appKey, userId, true);
        // 我的绘本只可能是免费或者已购买的
        return pb -> payMapper.containsAppItemId(pb.getId()) || isFreePicBook(pb);
    }

    /**
     * 获得用户拥有的绘本，并排好顺序(按照阅读时间和购买时间)。
     *
     * @param userId
     * @param appKey
     * @return
     */
    public List<PictureBookPlus> loadAllUserPicBooksOrdered(Long userId, String appKey, PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        Map<String, UserPicBookProgress> userPicBookProgressMap = loadAllUserPicBookProgress(userId);
        // 获得某一本书的排序时间，优先选阅读时间，如果没有则是购买时间
        Function<UserPicBook, Long> rtFunc = pb -> Optional.ofNullable(userPicBookProgressMap.get(pb.getBookId()))
                .map(UserPicBookProgress::getReadTime)
                .orElse(pb.getBuyTime());

        List<String> bookIds = loadAllUserPicBooks(userId, appKey)
                .stream()
                // 按照最近一次阅读时间排序
                .sorted((b1, b2) -> Long.compare(rtFunc.apply(b2), rtFunc.apply(b1)))
                .map(UserPicBook::getBookId)
                .collect(toList());

        return loadPicBooks(bookIds, pictureBookPlusServiceClient)
                .stream()
                .filter(getMyBooksFilter(userId, appKey))
                .collect(toList());
    }

    public Subject loadTypeSubject(OrderProductServiceType type) {
        return remoteReference.loadTypeSubject(type == null ? "" : type.name());
    }

    private String generateUserLevelKey(Long userId) {
        return CacheKeyGenerator.generateCacheKey(
                "UserPictureBook:userLevel",
                new String[]{"userId"},
                new Object[]{userId});
    }

    public List<PictureBookNewClazzLevel> loadCachedUserLevel(Long userId) {
        UtopiaCache cache = AfentiCache.getPersistent();
        CacheObject<String> result = cache.get(generateUserLevelKey(userId));
        if (result != null && result.containsValue()) {
            return Arrays.stream(result.getValue().split(","))
                    .map(PictureBookNewClazzLevel::safeValueOf)
                    .collect(Collectors.toList());
        }

        return null;
    }

    public boolean modifyUserLevelCache(Long userId, List<PictureBookNewClazzLevel> newLvls) {
        if (CollectionUtils.isEmpty(newLvls))
            return false;

        String newLvlCacheVal = newLvls.stream()
                .map(Enum::name)
                .reduce((acc, item) -> acc + "," + item)
                .orElse(null);

        UtopiaCache cache = AfentiCache.getPersistent();
        boolean result = cache.set(generateUserLevelKey(userId), 0, newLvlCacheVal);
        // 如果更改成功，让首页推荐的缓存失效，重新load
        if (result) {
            return AfentiCache.getAfentiCache().delete(featuredCacheKeyGenerator(userId));
        }

        return false;
    }

    private String featuredCacheKeyGenerator(Long userId) {
        return "UserPictureBook:featuredBookIds:" + Long.toString(userId);
    }

    public List<PictureBookPlus> loadSelfPicBooks(PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        Collection<PictureBookPlus> picBookList = loadPicBooksMap(pictureBookPlusServiceClient).values();

        return picBookList.stream()
                .filter(pb -> pb.pbApplyTo() != null)
                .filter(pb -> pb.pbApplyTo().contains(ApplyToType.SELF.name()))
                // 先过滤掉资源为空的那些记录
                .filter(pb -> StringUtils.isNotBlank(pb.getIosFileUrl()))
                .filter(pb -> "ONLINE".equals(pb.getStatus()))
                .collect(Collectors.toList());
    }

    private Map<String, PictureBookPlus> loadPicBooksMap(PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        return pictureBookPlusServiceClient.toMap();
    }

    public List<PictureBookPlus> loadFeaturedBooks(PicBookContext context, PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        Long userId = context.getUserId();
        // 获得用户推荐绘本列表ID的Function
        Function<Long, List<String>> pickFunc = uid -> pickFeaturedBooks(context, pictureBookPlusServiceClient)
                .stream()
                .filter(Objects::nonNull)
                .map(b -> b.getId())
                .collect(Collectors.toList());

        List<String> cachedBookIds = AfentiCache.getAfentiCache()
                .<Long, List<String>>createCacheValueLoader()
                .keyGenerator(this::featuredCacheKeyGenerator)
                .keys(Collections.singleton(userId))
                .loads()
                .externalLoader(userIds -> userIds.stream().collect(toMap(uid -> uid, pickFunc)))
                .loadsMissed()
                .expiration(DateUtils.getCurrentToDayEndSecond())
                .write()
                .getAndResortResult()
                .get(userId);

        return loadPicBooks(cachedBookIds, pictureBookPlusServiceClient);
    }

    /**
     * 从教材中获得对应的阅读等级
     *
     * @param bookId 教材ID
     * @return
     */
    public List<PictureBookNewClazzLevel> getReadLvlFromBookId(String bookId) {
        NewBookProfile bookProfile = contentLoaderClient.loadBook(bookId);
        if (bookProfile == null)
            return Collections.emptyList();

        int newClazzLevel = bookProfile.getClazzLevel();
        int termType = bookProfile.getTermType();

        // 获得和教材匹配的NewClazzLevel，后面进行过滤
        final List<PictureBookNewClazzLevel> matchingLevels;
        if (bookProfile.getStartClazzLevel() == 3) {
            matchingLevels = PictureBookNewClazzLevel.realLevelToNewClazzLevelStartFrom3
                    .getOrDefault(newClazzLevel, Collections.emptyMap())
                    .getOrDefault(termType, Collections.emptyList());
        } else {
            matchingLevels = PictureBookNewClazzLevel.realLevelToNewClazzLevelStartFrom1
                    .getOrDefault(newClazzLevel, Collections.emptyMap())
                    .getOrDefault(termType, Collections.emptyList());
        }

        return matchingLevels;
    }

    private List<PictureBookPlus> pickFeaturedBooks(PicBookContext context, PictureBookPlusServiceClient pictureBookPlusServiceClient) {

        // 过滤完，剩下学科与阅读等级相匹配的
        List<PictureBookPlus> picBooksStream = loadSelfPicBooks(pictureBookPlusServiceClient)
                .stream()
                .filter(pb -> Objects.equals(context.getSubjectId(), pb.getSubjectId()))
                .filter(pb -> containsAny(pb.getNewClazzLevels(), context.getClazzLevels()))
                .collect(Collectors.toList());

        // 过滤免费的绘本
        Predicate<PictureBookPlus> filterFreePd = pb -> {
            Integer freeFlag = pb.getFreeMap().getOrDefault(ApplyToType.SELF, 0);
            return freeFlag == 1;
        };

        List<PictureBookPlus> allFreePicBooks = picBooksStream.stream().filter(filterFreePd).collect(Collectors.toList());
        // 免费的随机挑两本
        PictureBookPlus[] tmpBookArrays = new PictureBookPlus[FEATURED_FREE_NUM];

        int pickNum = Math.min(allFreePicBooks.size(), FEATURED_FREE_NUM);
        RandomUtils.randomPickFew(allFreePicBooks, pickNum, tmpBookArrays);
        List<PictureBookPlus> freePicBooks = Arrays.asList(tmpBookArrays);

        // 付费的随机挑四本
        Predicate<PictureBookPlus> filterChargedPd = pb -> {
            Integer freeFlag = pb.getFreeMap().getOrDefault(ApplyToType.SELF, 0);
            return freeFlag == 2;
        };

        context.setBooks(picBooksStream);

        List<PictureBookPlus> chargeBooks = picBooksStream.stream().filter(filterChargedPd).collect(toList());
        List<String> chargeBookIds = chargeBooks.stream().map(pb -> pb.getId()).collect(toList());
        // 获得绘本的付费配置信息
        Map<String, List<OrderProduct>> productMap = usrOrderLoaderCli.loadOrderProductByAppItemIds(chargeBookIds);
        Function<String, Boolean> hadPayInfoFunc = bookId -> productMap.getOrDefault(bookId, emptyList()).size() > 0;

        // 按权重排完序后先放个池子里，再从池子中随机出来推荐的本数。
        // 在这里面就过滤掉没有付费配置信息的绘本，以防后面推荐的地方出现空位
        List<PictureBookPlus> chargePicBooks = chargeBooks.stream()
                .filter(b -> hadPayInfoFunc.apply(b.getId()))
                .sorted(getFeaturedComparator(context))
                .limit(FEATURED_CHARGE_POOL_SIZE)
                .collect(toList());

        tmpBookArrays = new PictureBookPlus[FEATURED_CHARGE_NUM];
        pickNum = Math.min(chargePicBooks.size(), FEATURED_CHARGE_NUM);
        RandomUtils.randomPickFew(chargePicBooks, pickNum, tmpBookArrays);
        chargePicBooks = Arrays.asList(tmpBookArrays);

        List<PictureBookPlus> featuredBooks = new ArrayList<>();
        // 免费在前，付费在后
        featuredBooks.addAll(freePicBooks);
        featuredBooks.addAll(chargePicBooks);

        return featuredBooks;
    }

    private Comparator<PictureBookPlus> getFeaturedComparator(PicBookContext context) {
        Date startBuyTime = DateUtils.addDays(new Date(), -BUY_TIME_INTERVAL);
        // 过滤类型和30天内，用户购买过的绘本记录
        Predicate<UserOrder> filter = uo -> uo.getCreateDatetime().after(startBuyTime)
                && Objects.equals(uo.getOrderProductServiceType(), context.getType())
                && uo.getPaymentStatus() == PaymentStatus.Paid;

        Set<String> boughtBookId = usrOrderLoaderCli.loadUserOrderRelateItems(context.getUserId(), filter)
                .stream()
                .map(OrderProductItem::getAppItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 买过的系列
        HashSet<String> boughtSeries = new HashSet<>();
        // 过滤完，剩下学科与阅读等级相匹配的
        context.getBooks().forEach(pb -> {
            if (boughtBookId.contains(pb.getId())) {
                boughtSeries.add(pb.getSeriesId());
            }
        });

        Map<String, PicBookStat> bookStatMap = loadPicBookStatMap();
        // 获得权重的Function
        Function<PictureBookPlus, Double> weightFunc = pb -> {
            double baseWeight = Optional.ofNullable(bookStatMap.get(pb.getId()))
                    .map(stat -> SafeConverter.toDouble(stat.getWeight()))
                    .orElse(0d);

            int seriesWeight = boughtSeries.contains(pb.getSeriesId()) ? 2 : 0;
            int boughtWeight = boughtBookId.contains(pb.getId()) ? -2 : 0;

            return baseWeight + seriesWeight + boughtWeight;
        };

        return (pb1, pb2) -> weightFunc.apply(pb2).compareTo(weightFunc.apply(pb1));
    }

    public PictureBookPlus loadPicBook(String bookId, PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        return pictureBookPlusServiceClient.loadById(bookId);
    }

    public List<PictureBookPlus> loadPicBooks(PicBookContext context,
                                              PageBlockContent levelConfig,
                                              PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        Long userId = context.getUserId();
        // 获得阅读等级
        List<PictureBookNewClazzLevel> readLvl = Optional.ofNullable(loadCachedUserLevel(userId)).orElse(singletonList(L1B));
        List<PictureBookPlus> allBooks = loadSelfPicBooks(pictureBookPlusServiceClient)
                .stream().filter(pb -> Objects.equals(context.getSubjectId(), pb.getSubjectId()))
                .collect(toList());

        List<String> jx = new ArrayList<>();
        List<String> yz = new ArrayList<>();
        List<String> tj = new ArrayList<>();
        if (levelConfig != null) {
            Map<String, Object> configMap = JsonStringDeserializer.getInstance()
                    .deserialize(levelConfig.getContent());
            if (configMap != null) {
                jx = (List<String>) configMap.get("jx");
                yz = (List<String>) configMap.get("yz");
                tj = (List<String>) configMap.get("tj");
            }
        }
        switch (context.getBookType()) {
            case "selection":
                List<String> finalJx = jx;
                allBooks = allBooks.stream()
                        .filter(pb -> finalJx.contains(pb.getSeriesId()))
                        .collect(Collectors.toList());
                break;
            case "highQuality":
                List<String> finalYz = yz;
                allBooks = allBooks.stream()
                        .filter(pb -> finalYz.contains(pb.getSeriesId()))
                        .collect(Collectors.toList());
                break;
            case "recommend":
                List<String> finalTj = tj;
                allBooks = allBooks.stream()
                        .filter(pb -> finalTj.contains(pb.getSeriesId()))
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }
        context.setBooks(allBooks);

        List<String> bookIds = allBooks.stream().map(pb -> pb.getId()).collect(Collectors.toList());
        Map<String, List<OrderProduct>> productMap = usrOrderLoaderCli.loadOrderProductByAppItemIds(bookIds);

        // 取绘本的价格Function
        Function<PictureBookPlus, Double> getPriceFunc = picBook -> {
            return Optional.ofNullable(productMap.get(picBook.getId()))
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(pb -> pb.getPrice().doubleValue())
                    .findFirst()
                    .orElse(0d);
        };

        // 延迟加载，不是每次都用的上
        LazyInitializationSupplier<Map<String, PicBookStat>> statMapSupplier = new LazyInitializationSupplier<>(this::loadPicBookStatMap);
        // 取绘本的销量Function
        Function<PictureBookPlus, Integer> getSaleFunc = picBook -> {
            return Optional.ofNullable(statMapSupplier.initializeIfNecessary().get(picBook.getId()))
                    .map(s -> SafeConverter.toInt(s.getSales()))
                    .orElse(0);
        };

        // 默认是按照创建时间由近及远
        Comparator<PictureBookPlus> comparator = getFeaturedComparator(context);
        switch (context.getOrderBy()) {
            case "priceDesc":
                comparator = (pb1, pb2) -> Double.compare(getPriceFunc.apply(pb2), getPriceFunc.apply(pb1));
                break;
            case "priceAsc":
                comparator = Comparator.comparingDouble(getPriceFunc::apply);
                break;
            case "salesVolDesc":
                comparator = (pb1, pb2) -> Integer.compare(getSaleFunc.apply(pb2), getSaleFunc.apply(pb1));
                break;
            default:
                break;
        }

        return allBooks.stream()
                .filter(pb -> containsAny(pb.pbNewClassLevels(), readLvl))
                // 过滤掉付费类型但是没有配置付费信息的绘本
                .filter(pb -> isFreePicBook(pb) || isNotEmpty(productMap.get(pb.getId())))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<PictureBookPlus> loadPicBooks(Collection<String> bookIds, PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        if (CollectionUtils.isEmpty(bookIds))
            return Collections.emptyList();

        List<PictureBookPlus> picBooks = new ArrayList<>();
        Map<String, PictureBookPlus> pictureBookMap = loadPicBooksMap(pictureBookPlusServiceClient);
        for (String bookId : bookIds) {
            PictureBookPlus pb = pictureBookMap.get(bookId);
            if (pb == null)
                continue;

            picBooks.add(pb);
        }

        return picBooks;
    }

    public MapMessage savePicBookStat(PicBookStat stat) {
        return remoteReference.savePicBookStat(stat);
    }

    /**
     * 获得绘本模块里面的内容
     */
    public MapMessage loadPicBookContentInModule(String bookId,
                                                 int module,
                                                 PictureBookPlusServiceClient pictureBookPlusServiceClient) {
        Map<String, PictureBookPlus> picBookMap = pictureBookPlusServiceClient.loadByIds(Collections.singletonList(bookId));
        PictureBookPlus picBook = picBookMap.get(bookId);

        if (picBook == null)
            return MapMessage.errorMessage("绘本不存在!");

        PicBookContentLoader contentLoader = contentLoaderManagement.getContentLoader(picBook.getSubjectId());
        if (contentLoader == null) {
            return MapMessage.errorMessage("未知的绘本类型!");
        }

        return contentLoader.loadContent(picBook, module);
    }

    public Map<String, String> getClazzLevelMap() {
        return this.CLAZZ_LEVEL_MAP;
    }

    public String loadClazzLevelName(String clName) {
        return this.CLAZZ_LEVEL_MAP.get(clName);
    }

    /**
     * 上报阅读绘本的进度
     *
     * @param record
     * @return
     */
    public MapMessage report(List<UserPicBookResult> record) {
        return remoteReference.report(record);
    }

    public List<String> loadShoppingCartBookIds(Long userId, OrderProductServiceType type) {
        return remoteReference.loadShopinCartBookIds(userId, type == null ? "" : type.name());
    }

    public MapMessage addShoppingCartItem(Long userId, OrderProductServiceType type, String bookId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("PicBookAddShoppingCart")
                    .keys(userId, type.name(), bookId)
                    .callback(() -> remoteReference.addShopinCartItem(userId, type == null ? "" : type.name(), bookId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请勿频繁操作~").setErrorCode("500");
        } catch (Throwable t) {
            return MapMessage.errorMessage();
        }
    }

    public List<String> removeShoppingCartItem(Long userId, OrderProductServiceType type, List<String> bookIds) {
        return remoteReference.removeShopingCartItem(userId, type == null ? "" : type.name(), bookIds);
    }

    public UserPicBookAchieve loadThisWeekAchieve(Long userId) {
        return remoteReference.getThisWeekAchieve(userId);
    }

    private List<PicBookStat> loadAllPicBookStat() {
        return remoteReference.loadAllPicBookStat();
    }

    public Map<String, PicBookStat> loadPicBookStatMap() {
        return loadAllPicBookStat()
                .stream()
                .collect(toMap(k -> k.getBookId(), v -> v));
    }

    public UserPicBookProgress loadUserPicBookProgress(Long userId, String bookId) {
        return remoteReference.loadAllUserPicBookProgress(userId).get(bookId);
    }

    public Map<String, UserPicBookProgress> loadAllUserPicBookProgress(Long userId) {
        return remoteReference.loadAllUserPicBookProgress(userId);
    }

    public Map<String, Boolean> loadReadStatus(Long userId, Collection<String> bookIds) {
        return remoteReference.loadReadStatus(userId, bookIds);
    }

    public boolean isFreePicBook(PictureBookPlus pb) {
        return Optional.ofNullable(pb.getFreeMap())
                .map(m -> m.getOrDefault(ApplyToType.SELF, 0) == 1)
                .orElse(false);
    }

    /**
     * 英语绘本内容读取
     */
    public static class EnglishPicBookContentLoader implements PicBookContentLoader, InitializingBean {

        @Inject private PicBookContentLoaderManagement contentLoaderManagement;
        @Inject private QuestionLoaderClient questionLoaderCli;

        @Override
        public MapMessage loadContent(PictureBookPlus picBook, int module) {
            if (picBook == null)
                return MapMessage.errorMessage("绘本不存在!");

            MapMessage resultMsg = MapMessage.successMessage();
            switch (module) {
                case ENGLISH_READING:
                    picBook.setIosFileUrl(picBook.getIosFileUrl());
                    picBook.setAndroidFileUrl(picBook.getAndroidFileUrl());
                    resultMsg.add("content", picBook);
                    break;
                case ENGLISH_OFTEN_USED:
                    resultMsg.add("content", picBook.allOftenUsedWords());
                    resultMsg.add("pronunciation", picBook.getPronunciation()); // 标注发音
                    resultMsg.add("screenMode", picBook.getScreenMode());// 标注横竖屏
                    break;
                case ENGLISH_ORAL:
                    resultMsg.add("content", questionLoaderCli.loadQuestions(picBook.getOralQuestions()).values());
                    resultMsg.add("maxRecordTime", 2);
                    break;
                case ENGLISH_PRACTISE:
                    resultMsg.add("content", questionLoaderCli.loadQuestions(picBook.getPracticeQuestions()).values());
                    break;
                default:
                    break;
            }

            return resultMsg;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            contentLoaderManagement.register(Subject.ENGLISH.getId(), this);
        }
    }

    public static class PicBookContentLoaderManagement {
        private Map<Integer, PicBookContentLoader> contentLoaderMap = new HashMap<>();

        public void register(int subjectId, PicBookContentLoader contentLoader) {
            contentLoaderMap.put(subjectId, contentLoader);
        }

        PicBookContentLoader getContentLoader(int module) {
            return contentLoaderMap.get(module);
        }
    }

}
