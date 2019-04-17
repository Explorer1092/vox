package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramGroupService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import com.voxlearning.utopia.service.piclisten.api.entity.MiniProgramGroup;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.MiniProgramCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.dao.MiniProgramGroupDao;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RA
 */

@Named
@ExposeService(interfaceClass = MiniProgramGroupService.class)
@Slf4j
public class MiniProgramGroupServiceImpl implements MiniProgramGroupService {


    @Inject
    private MiniProgramGroupDao miniProgramGroupDao;

    @Inject
    private MiniProgramCheckService miniProgramCheckService;

    @Inject
    private MiniProgramReadService miniProgramReadService;

    @Inject
    private MiniProgramCacheManager miniProgramCacheManager;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;


    private static final String GROUP_LOCK_KEY = "MINI_PROGRAM_BIND_GROUP_LOCK:%d_%d_%s";

    private static final String GROUP_RANK_CACHE_PREFIX = "MINI_PROGRAM_GROUP_RANK_CACHE:%s_%d_%d_%s";


    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();


    public void save(MiniProgramGroup po) {
        if (po != null) {
            miniProgramGroupDao.insert(po);
        }

    }


    @Override
    public MapMessage loadTotalGroupRank(Long uid, Long pid, String gid) {
        MapMessage mm = new MapMessage();

        // Query cache
        IRedisCommands redisCommands = miniProgramCacheManager.getRedisCommands();
        String key = String.format(GROUP_RANK_CACHE_PREFIX, "total", uid, pid, gid);

        Long ttl = redisCommands.sync().getRedisKeyCommands().ttl(key);

        if (ttl > 0) {
            mm.put("rank", redisCommands.sync().getRedisHashCommands().hget(key, "rank"));
            mm.put("my_rank", redisCommands.sync().getRedisHashCommands().hget(key, "my_rank"));
            return mm;
        }

        // try bind if not exist.
        bind(uid, pid, gid);
        List<GroupUserInfo> guInfo = loadGroupUserInfoByGid(gid);


        Map<Long, Long> totalTimeMap = new HashMap<>();
        Map<Long, Integer> totalCheckMap = new HashMap<>();
        for (Long id : guInfo.stream().map(GroupUserInfo::getUid).collect(Collectors.toList())) {

            // Read time data
            totalTimeMap.put(id, miniProgramReadService.getTotalReadTimes(id));
            // Check data
            totalCheckMap.put(id, miniProgramCheckService.getTotalCheckCount(id));
        }


        List<GroupRank> rankList = groupRank(uid, pid, guInfo, totalTimeMap, totalCheckMap);

        mm.put("rank", rankList);

        GroupRank myRank = rankList.stream().filter(x -> x.current == 1).findFirst().orElse(new GroupRank());
        mm.put("my_rank", myRank);

        // write cache
        if (rankList.size() > 0) {
            redisCommands.sync().getRedisHashCommands().hset(key, "rank", rankList);
            redisCommands.sync().getRedisHashCommands().hset(key, "my_rank", myRank);
            redisCommands.sync().getRedisKeyCommands().expire(key, 30);
        }
        return mm;

    }


    @Override
    public MapMessage loadWeekGroupRank(Long uid, Long pid, String gid) {
        MapMessage mm = new MapMessage();

        // Query cache
        IRedisCommands redisCommands = miniProgramCacheManager.getRedisCommands();
        String key = String.format(GROUP_RANK_CACHE_PREFIX, "week", uid, pid, gid);

        Long ttl = redisCommands.sync().getRedisKeyCommands().ttl(key);

        if (ttl > 0) {
            mm.put("rank", redisCommands.sync().getRedisHashCommands().hget(key, "rank"));
            mm.put("my_rank", redisCommands.sync().getRedisHashCommands().hget(key, "my_rank"));
            return mm;
        }

        // try bind if not exist.
        bind(uid, pid, gid);
        List<GroupUserInfo> guInfo = loadGroupUserInfoByGid(gid);

        Map<Long, Long> weekTimeMap = new HashMap<>();
        Map<Long, Integer> weekCheckMap = new HashMap<>();

        for (Long id : guInfo.stream().map(GroupUserInfo::getUid).collect(Collectors.toList())) {

            // Read time data
            List<Long> list = miniProgramReadService.getWeekReadTimes(id);

            long times = 0;
            for (Long time : list) {
                times += time;
            }
            weekTimeMap.put(id, times);

            // Check data
            weekCheckMap.put(id, miniProgramCheckService.getWeekContinuousCheckCount(id));
        }


        List<GroupRank> rankList = groupRank(uid, pid, guInfo, weekTimeMap, weekCheckMap);

        mm.put("rank", rankList);

        GroupRank myRank = rankList.stream().filter(x -> x.current == 1).findFirst().orElse(new GroupRank());
        mm.put("my_rank", myRank);


        // write cache
        if (rankList.size() > 0) {
            redisCommands.sync().getRedisHashCommands().hset(key, "rank", rankList);
            redisCommands.sync().getRedisHashCommands().hset(key, "my_rank", myRank);
            redisCommands.sync().getRedisKeyCommands().expire(key, 30);
        }
        return mm;

    }


    private List<GroupRank> groupRank(Long uid, Long pid, List<GroupUserInfo> guInfoList, Map<Long, Long> timeMap, Map<Long, Integer> checkMap) {

        List<Long> uids = guInfoList.stream().map(GroupUserInfo::getUid).collect(Collectors.toList());
        List<Long> pids = guInfoList.stream().map(GroupUserInfo::getPid).collect(Collectors.toList());

        // Load all user data
        Map<Long, User> students = userLoaderClient.loadUsers(uids);
        Map<Long, ParentExtAttribute> parents = parentLoaderClient.loadParentExtAttributes(pids);

        List<GroupRank> list = new ArrayList<>();

        for (GroupUserInfo guInfo : guInfoList) {
            User student = students.get(guInfo.uid);
            if (student == null) {
                continue;
            }

            GroupRank gr = new GroupRank();
            gr.times = timeMap.get(guInfo.uid);
            gr.checked = checkMap.get(guInfo.uid);
            gr.current = 0;
            if (guInfo.uid.equals(uid)) {
                gr.current = 1;
            }

            ParentExtAttribute parent = parents.get(guInfo.pid);
            // load user info
            String name = student.fetchRealname();
            String avatar = "";
            if (parent != null) {
                if (StringUtils.isBlank(name)) {
                    // Get parent weixin name
                    name = parent.getWechatNick() + "的孩子";
                }
                avatar=parent.getWechatImage();
            }

            gr.name = name;
            gr.avatar = avatar;

            list.add(gr);
        }

        // Sort
        List<GroupRank> rankList = list.stream().sorted(Comparator.comparing(GroupRank::getTimes)
                .thenComparing(GroupRank::getChecked)
                .thenComparing(GroupRank::getName).reversed()).collect(Collectors.toList());

        return rankList;

    }


    public void bind(Long uid, Long pid, String gid) {
        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(pid, "pid must not be null!");
        Assertions.notEmpty(gid, "gid must not be null!");

        String lockKey = String.format(GROUP_LOCK_KEY, uid, pid, gid);
        try {
            lock.acquireLock(lockKey);

            // Check has bind
            if (hasBind(uid, pid, gid)) {
                return;
            }

            // Check data
            List<User> student = studentLoaderClient.loadParentStudents(pid);
            if (student.isEmpty()) {
                log.warn("Parent id: {} not exist,bind field.", pid);
                // parent not exist
                return;
            }
            boolean flag = false;
            for (User s : student) {
                if (s.getId().equals(uid)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                log.warn("Parent id: {} has no child id:{} ,bind field.", pid, uid);
                return;
            }


            MiniProgramGroup group = new MiniProgramGroup();
            group.setUid(uid);
            group.setGid(gid);
            group.setPid(pid);
            // bind
            save(group);
        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock on bindGroup method", lockKey);

        } finally {
            lock.releaseLock(lockKey);
        }
    }


    public boolean hasBind(Long uid, Long pid, String gid) {
        Assertions.notNull(uid, "uid must not be null!");
        Assertions.notNull(pid, "pid must not be null!");
        Assertions.notEmpty(gid, "gid must not be null!");
        return miniProgramGroupDao.hasBind(uid, pid, gid);

    }

    public List<GroupUserInfo> loadGroupUserInfoByGid(String gid) {
        Assertions.notEmpty(gid, "gid must not be null!");
        List<MiniProgramGroup> list = miniProgramGroupDao.loadByGid(gid);
        List<GroupUserInfo> infos = new ArrayList<>(list.size());
        if (list.size() > 0) {
            for (MiniProgramGroup group : list) {
                GroupUserInfo info = new GroupUserInfo();
                info.pid = group.getPid();
                info.uid = group.getUid();
                infos.add(info);
            }
        }
        return infos;

    }


    @Data
    private static class GroupUserInfo implements Serializable {
        private static final long serialVersionUID = -5730087352239075426L;
        private Long uid;
        private Long pid;
    }

    @Data
    private static class GroupRank implements Serializable {

        private static final long serialVersionUID = 8536247256694532521L;
        private String name;
        private String avatar;
        private int checked;
        private long times;
        private int current;
    }

}
