package com.voxlearning.utopia.service.reward.impl.loader;

import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.PublicGoodLoader;
import com.voxlearning.utopia.service.reward.api.enums.LikeSourceEnum;
import com.voxlearning.utopia.service.reward.api.mapper.CacheCollectMapper;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.LikeEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.TeacherJoinStatusMapper;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.*;
import com.voxlearning.utopia.service.reward.impl.internal.InternalPGRankService;
import com.voxlearning.utopia.service.reward.impl.service.PublicGoodServiceImpl;
import com.voxlearning.utopia.service.reward.mapper.PGParentChildRef;
import com.voxlearning.utopia.service.reward.mapper.PGRankEntry;
import com.voxlearning.utopia.service.reward.mapper.SameClazzMapper;
import com.voxlearning.utopia.service.reward.util.CacheKeyUtils;
import com.voxlearning.utopia.service.reward.util.TeacherNameWrapper;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.AsyncUserService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupClazzMapper;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSystemClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named
@ExposeServices({
        @ExposeService(interfaceClass = PublicGoodLoader.class, version = @ServiceVersion(version = "20180612")),
        @ExposeService(interfaceClass = PublicGoodLoader.class, version = @ServiceVersion(version = "20180717"))
})
@Slf4j
public class PublicGoodLoaderImpl implements PublicGoodLoader, InitializingBean {

    @Inject private PublicGoodStyleDao styleDao;
    @Inject private PublicGoodCollectDao collectDao;
    @Inject private PublicGoodElementTypeDao elementTypeDao;
    @Inject private PublicGoodFeedDao feedDao;
    @Inject private PublicGoodRewardDao rewardDao;
    @Inject private PublicGoodUserActivityDao activityDao;

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject private InternalPGRankService rankService;
    @Inject private SchoolLoaderClient schoolLoader;
    @Inject private PublicGoodServiceImpl pgService;

    private IRedisCommands redisCommands;

    @ImportService(interfaceClass = AsyncUserService.class)
    private AsyncUserService asyncUserService;

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        redisCommands = builder.getRedisCommands("user-easemob");
    }

    @Override
    public PublicGoodStyle loadStyleById(Long id) {
        return loadStyleAll()
                .stream()
                .filter(s -> Objects.equals(s.getId(), id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<PublicGoodStyle> loadStyleByModel(String model) {
        return styleDao.loadByModel(model);
    }

    @Override
    public List<PublicGoodStyle> loadStyleAll() {
        return styleDao.loadAll();
    }

    @Override
    public List<PublicGoodCollect> loadCollectByUserId(Long userId) {
        return collectDao.loadByUserId(userId);
    }

    public Map<Long, List<PublicGoodCollect>> loadCollectByUserId(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<PublicGoodCollect>> result = new HashMap<>();
        for (Long userId : userIds) {
            List<PublicGoodCollect> publicGoodCollects = loadCollectByUserId(userId);
            result.put(userId, publicGoodCollects);
        }
        return result;
    }


    @Override
    public List<PublicGoodElementType> loadAllElementTypes() {
        return elementTypeDao.loadAll();
    }

    @Override
    public List<PublicGoodElementType> loadElementTypeByStyleId(Long styleId){
        return  loadAllElementTypes()
                .stream()
                .filter(et -> Objects.equals(et.getStyleId(),styleId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicGoodFeed> loadFeedByUserId(Long activityId, Long userId, Boolean isRead) {
        List<PublicGoodFeed> feedList = feedDao.loadByUserId(userId).stream().sorted(new Comparator<PublicGoodFeed>() {
            @Override
            public int compare(PublicGoodFeed o1, PublicGoodFeed o2) {
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            }
        }).filter(i -> Objects.equals(i.getActivityId(), activityId)).collect(Collectors.toList());
        if (isRead) {
            redisCommands.sync().getRedisStringCommands().setbit(CacheKeyUtils.genFeedKey(activityId), userId, 0);
        }
        return feedList;
    }

    @Override
    public Boolean getFeedStatus(Long activityId, Long userId) {
        Long getbit = redisCommands.sync().getRedisStringCommands().getbit(CacheKeyUtils.genFeedKey(activityId), userId);
        return Objects.equals(getbit, 1L);
    }

    @Override
    public List<PublicGoodUserActivity> loadUserActivityByUserId(Long userId) {
        return activityDao.loadByUserId(userId);
    }

    @Override
    public MapMessage loadClazzCollect(Long activityId, Long userId) {
        User user = userLoaderClient.loadUser(userId);
        List<SameClazzMapper.ClazzMapper> clazzMapperList = new ArrayList<>();

        SameClazzMapper sameClazzMapper = new SameClazzMapper();
        sameClazzMapper.setUserId(userId);
        if (user.fetchUserType() == UserType.TEACHER) {
            sameClazzMapper.setType("TEACHER");

            // 获取包班制子账号班级
            List<Long> allTeacherId = teacherLoaderClient.loadSubTeacherIds(user.getId());
            allTeacherId.add(user.getId());

            List<GroupClazzMapper> groupClazzMapperList = new ArrayList<>();
            for (Long teacherId : allTeacherId) {
                groupClazzMapperList.addAll(teacherSystemClazzServiceClient.loadTeacherAllGroupsData(teacherId)
                        .stream()
                        .filter(i -> Objects.equals(ClazzType.PUBLIC.name(), i.getClazzType()))
                        .collect(Collectors.toList()));
            }

            // 一个老师如果担任同一班级的多个学科,会出现重复班级,这里去一下重
            Map<Long, String> clazzMap = new LinkedHashMap<>();
            for (GroupClazzMapper groupClazzMapper : groupClazzMapperList) {
                clazzMap.put(groupClazzMapper.getClazzId(), groupClazzMapper.getClazzName());
            }
            for (Map.Entry<Long, String> itemEntry : clazzMap.entrySet()) {
                SameClazzMapper.ClazzMapper clazzMapper = new SameClazzMapper.ClazzMapper();
                clazzMapper.setClazzName(itemEntry.getValue());

                Long clazzId = itemEntry.getKey();

                List<CacheCollectMapper> publicGoodCollects = getPublicGoodCollects(user, activityId, clazzId).stream().filter(i -> Objects.equals(i.getType(), 1)).collect(Collectors.toList());
                clazzMapper.setCollects(publicGoodCollects);
                clazzMapperList.add(clazzMapper);
            }
        } else if (user.fetchUserType() == UserType.STUDENT) {
            sameClazzMapper.setType("STUDENT");
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());

            SameClazzMapper.ClazzMapper clazzMapper = new SameClazzMapper.ClazzMapper();
            clazzMapper.setClazzName(studentDetail.getClazz().formalizeClazzName());

            List<CacheCollectMapper> publicGoodCollects = getPublicGoodCollects(user, activityId, studentDetail.getClazz().getId());
            clazzMapper.setCollects(publicGoodCollects);
            clazzMapperList.add(clazzMapper);
        }
        return MapMessage.successMessage().add("data", clazzMapperList).add("type", user.isTeacher() ? 0 : 1);
    }

    @Override
    public MapMessage loadSchoolRank(Long userId, Long activityId) {
        List<Long> clazzIds = new ArrayList<>();
        Long schoolId;

        User user = userLoaderClient.loadUser(userId);
        School school = asyncUserService.loadUserSchool(user).getUninterruptibly();
        if(school == null)
            return MapMessage.errorMessage("学校不存在!");
        else
            schoolId = school.getId();

        if(user.isStudent()){
            clazzIds.add(studentLoaderClient.loadStudentDetail(userId).getClazzId());
        }else if(user.isTeacher()){
            clazzIds.addAll(teacherLoaderClient.loadTeacherClazzIds(userId));
        }

        if(CollectionUtils.isEmpty(clazzIds))
            return MapMessage.errorMessage("班级不存在!");

        MapMessage resultMsg = MapMessage.successMessage();
        // 学校排行榜生成Key
        String schoolRankKey = CacheKeyGenerator.generateCacheKey(
                "PublicGoodRank:school",
                new String[]{"activityId","schoolId"},
                new Object[]{activityId,schoolId});

        Map<Long,PGRankEntry> rankMap = RewardCache.getPersistent().load(schoolRankKey);
        if(!MapUtils.isEmpty(rankMap)){
            List<PGRankEntry> rankList = rankMap.values()
                    .stream()
                    .sorted((r1, r2) -> Long.compare(r2.getMoney(), r1.getMoney()))
                    .collect(Collectors.toList());

            // 补充上rank字段
            for(long i = 1;i <= rankList.size();i++){
                rankList.get((int)(i - 1)).setRank(i);
            }

            List<PGRankEntry> myEntries = new ArrayList<>();
            clazzIds.forEach(cId -> {
                PGRankEntry mock = new PGRankEntry();
                mock.setClazzId(cId);
                mock.setSchoolId(schoolId);

                int existIndex = rankList.indexOf(mock);
                if(existIndex >= 0){
                    myEntries.add(rankList.get(existIndex));
                }
            });

            // 按照捐赠学豆从高到低，如果并列再按名次从前到后
            Comparator<PGRankEntry> cpr  = (e1,e2) -> Long.compare(e2.getMoney(),e1.getMoney());
            myEntries.sort(cpr.thenComparing(Comparator.comparingLong(PGRankEntry::getRank)));

            resultMsg.add("myEntries", myEntries);
            resultMsg.add("rank",rankList);
        }

        return resultMsg;
    }

    @Override
    public MapMessage loadNationRank(Long userId, Long activityId) {
        String nationRankKey = CacheKeyGenerator.generateCacheKey(
                "PublicGoodRank:nation",
                new String[]{"activityId"},
                new Object[]{activityId});

        MapMessage resultMsg = MapMessage.successMessage();
        Map<Long,PGRankEntry> rankMap = RewardCache.getPersistent().load(nationRankKey);

        // 优先比较捐的学豆，如果相等则比较教室完成数量
        Comparator<PGRankEntry> baseCpr = Comparator.comparingLong(PGRankEntry::getMoney).reversed();
        Comparator<PGRankEntry> finishCpr = Comparator.comparingLong(PGRankEntry::getFinishNum).reversed();

        List<PGRankEntry> rankList = Optional.ofNullable(rankMap)
                .orElse(Collections.emptyMap())
                .values()
                .stream()
                .sorted(baseCpr.thenComparing(finishCpr))
                .collect(Collectors.toList());

        // 补充上rank字段
        for (long i = 1; i <= rankList.size(); i++) {
            rankList.get((int) (i - 1)).setRank(i);
        }

        Long schoolId;
        User user = userLoaderClient.loadUser(userId);
        School school = asyncUserService.loadUserSchool(user).getUninterruptibly();
        if (school == null)
            return MapMessage.errorMessage("学校不存在!");
        else
            schoolId = school.getId();

        List<PGRankEntry> myEntries = new ArrayList<>();
        PGRankEntry mock = new PGRankEntry();
        mock.setSchoolId(schoolId);

        int existIndex = rankList.indexOf(mock);
        if (existIndex >= 0) {
            myEntries.add(rankList.get(existIndex));
        } else {
            // 如果不在百名之内，获取详细信息
            rankService.fetchRankEntry(activityId, schoolId, (rank, money, finishNum) -> {
                mock.setName(school.getShortName());
                mock.setRank(rank);
                mock.setMoney(money);
                mock.setFinishNum(finishNum);
            });

            myEntries.add(mock);
        }

        resultMsg.add("myEntries", myEntries);
        resultMsg.add("rank", rankList);

        return resultMsg;
    }

    @Override
    public List<LikeEntriesMapper> getLikeByCollectId(Long activityId, String collectId) {
        Long userId = Long.valueOf(collectId.split("-")[0]);
        List<PublicGoodFeed> likes = loadFeedByUserId(activityId, userId, false)
                .stream()
                .filter(u -> u.getSourceEnum() == LikeSourceEnum.A17 && u.getType() == PublicGoodFeed.Type.LIKE)
                .limit(10)
                .collect(Collectors.toList());

        List<Long> likeUserIds = likes.stream().map(PublicGoodFeed::getOpId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(likeUserIds);

        List<LikeEntriesMapper> likeEntries = new ArrayList<>();
        for (PublicGoodFeed like : likes) {
            User itemUser = userMap.get(like.getOpId());
            String realname = itemUser.getProfile().getRealname();

            LikeEntriesMapper itemLike = new LikeEntriesMapper();
            if (itemUser.isTeacher()) {
                itemLike.setName(TeacherNameWrapper.respectfulName(realname));
            } else {
                itemLike.setName(realname);
            }
            itemLike.setAvatarImg(itemUser.fetchImageUrl());
            likeEntries.add(itemLike);
        }
        return likeEntries;
    }

    @Override
    public CommentEntriesMapper getCommentByCollectId(Long activityId, String collectId) {
        Long userId = Long.valueOf(collectId.split("-")[0]);

        User user = userLoaderClient.loadUser(userId);
        if (user.isTeacher()) {
            return null;
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Long clazzId = studentDetail.getClazzId();
        if (clazzId == null) {
            return null;
        }

        List<CacheCollectMapper> cacheCollectMappers = getCollectMapperFromCache(activityId, clazzId).stream().filter(i -> Objects.equals(i.getType(), 0)).collect(Collectors.toList());
        List<Long> collectUserId = cacheCollectMappers.stream().map(CacheCollectMapper::getUserId).collect(Collectors.toList());
        Map<Long, User> collectUserMap = userLoaderClient.loadUsers(collectUserId);

        List<CommentEntriesMapper.Collect> collectList = new ArrayList<>();
        for (CacheCollectMapper cacheCollectMapper : cacheCollectMappers) {
            User tempUser = collectUserMap.get(cacheCollectMapper.getUserId());

            CommentEntriesMapper.Collect collect = new CommentEntriesMapper.Collect();
            collect.setTeacherName(TeacherNameWrapper.respectfulName(tempUser.getProfile().getRealname()));
            collect.setAvatarImg(tempUser.fetchImageUrl());

            if (collectList.indexOf(collect) < 0) {
                collectList.add(collect);
            }
        }

        List<PublicGoodFeed> comments = loadFeedByUserId(activityId, userId, false)
                .stream()
                .filter(u -> u.getType() == PublicGoodFeed.Type.COMMENTS)
                .limit(10)
                .collect(Collectors.toList());

        List<Long> commonTeacherIds = comments.stream().map(PublicGoodFeed::getOpId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(commonTeacherIds);

        List<CommentEntriesMapper.Comment> commentList = new ArrayList<>();
        for (PublicGoodFeed comment : comments) {
            CommentEntriesMapper.Comment commentItem = new CommentEntriesMapper.Comment();

            User itemUser = userMap.get(comment.getOpId());
            commentItem.setWord(TeacherNameWrapper.respectfulName(itemUser.getProfile().getRealname()) + "：" + comment.getComments());
            commentItem.setAvatarImg(itemUser.fetchImageUrl());
            commentList.add(commentItem);
        }

        CommentEntriesMapper mapper = new CommentEntriesMapper();
        mapper.setCollectList(collectList);
        mapper.setCommentList(commentList);
        return mapper;
    }

    public List<TeacherJoinStatusMapper> getTeacherJoinStatus(Long activityId, Long userId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null || studentDetail.getClazzId() == null) {
            return Collections.emptyList();
        }
        Long clazzId = studentDetail.getClazzId();

        List<CacheCollectMapper> collectMapperFromCache = getCollectMapperFromCache(activityId, clazzId)
                .stream()
                .filter(i -> Objects.equals(i.getType(), 0))
                .collect(Collectors.toList());

        List<Long> userIds = collectMapperFromCache.stream().map(CacheCollectMapper::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userIds);

        Map<Long, Long> collect = collectMapperFromCache.stream()
                .collect(Collectors.groupingBy(CacheCollectMapper::getUserId, Collectors.summingLong(value -> {
                    if (value.getMoney() == null) {
                        return 0;
                    }
                    return value.getMoney();
                })));

        List<TeacherJoinStatusMapper> result = new ArrayList<>();
        collect.forEach((LongId, money) -> {
            User user = userMap.get(LongId);
            TeacherJoinStatusMapper item = new TeacherJoinStatusMapper();
            item.setId(user.getId());
            item.setAvatarImg(user.fetchImageUrl());
            item.setName(TeacherNameWrapper.respectfulName(user.fetchRealname()));
            item.setMoney(money);
            result.add(item);
        });
        return result;
    }

    @Override
    public Set<Long> loadLikedCollect(Long activityId, Long userId) {
        PublicGoodUserActivity userActivity = activityDao.loadByUserId(userId)
                .stream()
                .filter(i -> Objects.equals(activityId, i.getActivityId()))
                .findFirst()
                .orElse(null);
        if (userActivity == null || userActivity.getLikedUser() == null) {
            return Collections.emptySet();
        }
        return userActivity.getLikedUser();
    }

    @Override
    public List<PublicGoodReward> loadRewardByModel(String model) {
        return rewardDao.loadByModel(model);
    }

    @Override
    public PGParentChildRef loadParentChildRef(Long parentId) {
        if(parentId == null || parentId == 0L)
            return null;

        String key = CacheKeyGenerator.generateCacheKey(
                "PulbicGoodParentChildRef",
                new String[]{"parentId"},
                new Object[]{parentId});

        return RewardCache.getPersistent().load(key);
    }

    @Override
    public MapMessage loadRankForBackDoor(Long activityId,Long schoolId) {
        if(activityId == null || activityId == 0)
            return MapMessage.errorMessage();

        String cacheKey = "PublicGoodRank:nation:activityId:" + activityId;
        RedisSortedSetCommands<String,Object> ssCommands = redisCommands.sync().getRedisSortedSetCommands();

        if(schoolId != null && schoolId != 0){
            long rank = ssCommands.zrevrank(cacheKey,schoolId);
            return MapMessage.successMessage().add("rank",rank);
        }else{
            return pgService.persistRank();
        }
    }

    private List<CacheCollectMapper> getPublicGoodCollects(User user,Long activityId, Long clazzId) {
        // 点赞过的数据
        Set<Long> likedCollect = loadLikedCollect(activityId, user.getId());

        List<CacheCollectMapper> cacheCollectMapperList = getCollectMapperFromCache(activityId, clazzId);
        Collection<CacheCollectMapper> result = cacheCollectMapperList.stream().collect(Collectors.toMap(CacheCollectMapper::getUserId, Function.identity(), (oldMapper, newMapper) -> {
            if (oldMapper.getUpdateTime() == null || newMapper.getUpdateTime() == null) {
                return oldMapper;
            }
            int i = oldMapper.getUpdateTime().compareTo(newMapper.getUpdateTime());
            if (i > 0) {
                return oldMapper;
            } else {
                return newMapper;
            }
        })).values();

        Set<Long> userId = result.stream().map(CacheCollectMapper::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(userId);

        List<CacheCollectMapper> allCollects = result
                .stream()
                .peek(i -> {
                    if (i.getMoney() == null) {
                        i.setMoney(0L);
                    }
                })
                .sorted((o1, o2) -> {
                    int i = o1.getType().compareTo(o2.getType());
                    if (i == 0) {
                        return o2.getMoney().compareTo(o1.getMoney());
                    }
                    return i;
                })
                .peek(i -> {
                    User tempUser = userMap.get(i.getUserId());
                    i.setLiked(likedCollect.contains(i.getUserId()));
                    i.setImgUrl(tempUser.fetchImageUrl());
                    if (Objects.equals(i.getType(), 0)) {
                        i.setMoney(null); // 不显示老师捐赠的金豆
                        i.setUserName(TeacherNameWrapper.respectfulName(tempUser.getProfile().getRealname())); // 老师显示尊称
                    } else {
                        i.setUserName(tempUser.getProfile().getRealname());
                    }
                }).collect(Collectors.toList());

        // 移除自己的教室
        allCollects.removeIf(next -> Objects.equals(next.getUserId(), user.getId()));
        return allCollects;
    }

    private List<CacheCollectMapper> getCollectMapperFromCache(Long activityId, Long clazzId) {
        Map<String, Object> hgetall = redisCommands.sync().getRedisHashCommands().hgetall(CacheKeyUtils.genClassCollectKey(activityId, SafeConverter.toLong(clazzId)));

        // 缓存中的 collectId
        List<CacheCollectMapper> redisCache = Optional.ofNullable(hgetall)
                .orElse(Collections.emptyMap())
                .values()
                .stream()
                .map(i -> (CacheCollectMapper) i)
                .collect(Collectors.toList());

        // 如果 redis 中没有尝试从 db 中恢复
        if (redisCache == null || redisCache.isEmpty()) {
            return restoreCollectMapperCache(activityId, clazzId);
        }
        return redisCache;
    }

    private List<CacheCollectMapper> restoreCollectMapperCache(Long activityId, Long clazzId) {
        String cacheKey = activityId + ":" + clazzId;

        List<CacheCollectMapper> cacheResult = RewardCache.getRewardCache()
                .<String, List<CacheCollectMapper>>createCacheValueLoader()
                .keyGenerator(s -> "reward:publicgood:" + s)
                .keys(Collections.singletonList(cacheKey))
                .loads()
                .externalLoader(collection -> {
                    Iterator<String> iterator = collection.iterator();
                    if (iterator.hasNext()) {
                        String next = iterator.next();
                        String[] split = next.split(":");
                        Long activityId1 = Long.parseLong(split[0]);
                        Long clazzId1 = Long.parseLong(split[1]);
                        List<CacheCollectMapper> cacheCollectMappers = getCacheCollectFromDB(activityId1, clazzId1);

                        Map<String, List<CacheCollectMapper>> map = new HashMap<>();
                        map.put(next, cacheCollectMappers);
                        return map;
                    }
                    return null;
                })
                .loadsMissed()
                .expiration(1)
                .write()
                .getResult()
                .get(cacheKey);

        for (CacheCollectMapper cacheCollectMapper : cacheResult) {
            redisCommands.sync().getRedisHashCommands().hset(CacheKeyUtils.genClassCollectKey(activityId, clazzId), cacheCollectMapper.getCollectId(), cacheCollectMapper);
        }

        return cacheResult;
    }

    private List<CacheCollectMapper> getCacheCollectFromDB(Long activityId, Long clazzId) {
        List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(clazzId);
        Set<Long> teacherIds = clazzTeachers.stream().map(i -> i.getTeacher().getId()).collect(toSet());

        List<Long> clazzStudents = studentLoaderClient.loadClazzStudentIds(Collections.singleton(clazzId)).get(clazzId);
        Set<Long> studentIds = new HashSet<>(clazzStudents);

        Set<Long> allUserId = new HashSet<>();
        allUserId.addAll(teacherIds);
        allUserId.addAll(studentIds);
        if (allUserId.size() > 200) {
            log.warn("public good sameclazz collect size :{}", allUserId.size());
        }

        // 查所有用户的捐赠总额
        Map<Long, PublicGoodUserActivity> userActivityMap = new HashMap<>();
        for (Long userId : allUserId) {
            PublicGoodUserActivity publicGoodUserActivity = activityDao.loadByUserId(userId)
                    .stream()
                    .filter(i -> Objects.equals(activityId, i.getActivityId()))
                    .findFirst().orElse(null);
            if (publicGoodUserActivity != null) {
                userActivityMap.putIfAbsent(userId, publicGoodUserActivity);
            }
        }

        // 如果没有捐赠金额说明压根没有创建教室, 下一步无需再查
        allUserId = userActivityMap.keySet();

        // 查所有用户的活动实体(教室)
        List<PublicGoodCollect> userLastCollects = new ArrayList<>();
        for (Long userId : allUserId) {
            List<PublicGoodCollect> publicGoodCollects = collectDao.loadByUserId(userId);
            PublicGoodCollect userLastCollect = publicGoodCollects.stream()
                    .filter(i -> Objects.equals(activityId, i.getActivityId()))
                    .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                    .findFirst().orElse(null);
            if (userLastCollect != null) {
                userLastCollects.add(userLastCollect);
            }
        }

        List<CacheCollectMapper> result = userLastCollects.stream().map(item -> {
            Long userId = item.getUserId();
            PublicGoodUserActivity userActivity = userActivityMap.get(userId);
            boolean isTeacher = teacherIds.contains(userId);

            CacheCollectMapper cacheCollectMapper = new CacheCollectMapper();
            cacheCollectMapper.setUserId(userId);
            cacheCollectMapper.setCollectId(item.getId());
            cacheCollectMapper.setStyleId(item.getStyleId());
            cacheCollectMapper.setActivityId(activityId);
            cacheCollectMapper.setMoney(SafeConverter.toLong(userActivity.getMoneyNum()));
            cacheCollectMapper.setEnableCode(item.getEnabledCode());
            cacheCollectMapper.setType(isTeacher ? 0 : 1);
            cacheCollectMapper.setDone(item.isFinished());
            return cacheCollectMapper;
        }).collect(toList());

        return result;
    }

    public static void main(String[] args){
        JexlEngine engine = new JexlEngine();
        JexlContext context = new MapContext();

        List<Map<String,Object>> groupHws = new ArrayList<>();
        groupHws.add(MapUtils.m("groupId",111,"checkNum",3,"finishNum",new int[]{1,2,3}));

        String expression = "for(ghw : groupHws){ for(fNum : ghw.finishNum) {result = false;}}";
        context.set("groupHws",groupHws);
        context.set("result",false);

        engine.createExpression(expression).evaluate(context);
        System.out.println(context.get("result"));
    }
}
