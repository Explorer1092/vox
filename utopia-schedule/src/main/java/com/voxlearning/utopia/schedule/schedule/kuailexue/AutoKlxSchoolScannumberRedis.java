/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.schedule.kuailexue;

import com.lambdaworks.redis.api.sync.RedisSetCommands;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.cache.redis.client.IRedisClient;
import com.voxlearning.alps.cache.redis.client.IRedisConnection;
import com.voxlearning.alps.cache.redis.client.RedisClientBuilder;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import com.voxlearning.utopia.service.user.api.entities.UserSchoolRef;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedSchoolServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/12/27
 */
@Named
@ScheduledJobDefinition(
        jobName = "每月第10天快乐学学校阅卷机号提前存到redis中",
        jobDescription = "快乐学阅卷机号提前存到redis中,每月第10天3点",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 10 * ?",
        ENABLED = false
)
public class AutoKlxSchoolScannumberRedis extends ScheduledJobWithJournalSupport {
    private static final Logger logger = LoggerFactory.getLogger(AutoKlxSchoolScannumberRedis.class);

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Deprecated
    @Inject
    private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;

    private UtopiaSql utopiaSql;
    private IRedisClient redisClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        RedisClientBuilder clientBuilder = RedisClientBuilder.Companion.getInstance();
        redisClient = clientBuilder.getRedisClient("user-klx");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
//        dealRealStudent();
//        dealVirtualStudent();
        dealKlxStudent();
    }

    private void dealKlxStudent() {
        //获取虚拟用户的schoolId scanNumber
        int pageIndex = 0;
        int pageSize = 50000;
        while (true) {
            List<Map<String, Object>> groupKlxStudents = utopiaSql.withSql("SELECT CLAZZ_GROUP_ID,STUDENT_ID FROM VOX_GROUP_KLX_STUDENT_REF WHERE DISABLED=FALSE limit ?,?")
                    .useParamsArgs(pageIndex * pageSize, pageSize)
                    .queryAll();
            pageIndex++;
            if (CollectionUtils.isEmpty(groupKlxStudents)) {
                break;
            }
            //处理业务
            logger.info("AutoKlxSchoolScannumberRedis dealKlxStudent: pageIndex {} ,size is {}", pageIndex, groupKlxStudents.size());
            putKlxStudentScanNumber2Redis(groupKlxStudents);
        }
    }

    private void dealRealStudent() {
        //获取真实学生的schoolId scanNumber
        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo")
                .getDatabase("vox-user");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("vox_student_ext_attribute");
        Bson bson = Filters.exists("scanNumber");

        int pageIndex = 0;
        int pageSize = 50000;
        while (true) {
            FindIterable<Document> documents = mongoCollection.find(bson).skip(pageIndex * pageSize).limit(pageSize);
            Map<Long, String> realStudentScanNumberMap = new HashMap<>();
            for (Document document : documents) {
                Long studentId = SafeConverter.toLong(document.get("_id"));
                String scanNumber = SafeConverter.toString(document.get("scanNumber"));
                realStudentScanNumberMap.put(studentId, scanNumber);
            }
            pageIndex++;
            if (MapUtils.isEmpty(realStudentScanNumberMap)) {
                break;
            }

            logger.info("AutoKlxSchoolScannumberRedis dealRealStudent: pageIndex {} ,size is {}", pageIndex, realStudentScanNumberMap.keySet().size());
            //处理业务方法使用多线程处理 数据量最大是pageSize=50000条 每个线程最多处理blockSize=8000条
            putRealStudentScanNumber2Redis(realStudentScanNumberMap);
        }
    }

    private void dealVirtualStudent() {
        //获取虚拟用户的schoolId scanNumber
        int pageIndex = 0;
        int pageSize = 50000;
        while (true) {
            List<Map<String, Object>> groupVritualStudentIdList = utopiaSql.withSql("SELECT CLAZZ_GROUP_ID,STUDENT_ID FROM VOX_GROUP_VIRTUAL_STUDENT_REF WHERE DISABLED=FALSE limit ?,?")
                    .useParamsArgs(pageIndex * pageSize, pageSize)
                    .queryAll();
            pageIndex++;
            if (CollectionUtils.isEmpty(groupVritualStudentIdList)) {
                break;
            }
            //处理业务
            logger.info("AutoKlxSchoolScannumberRedis dealVirtualStudent: pageIndex {} ,size is {}", pageIndex, groupVritualStudentIdList.size());
            putVirtualStudentScanNumber2Redis(groupVritualStudentIdList);
        }
    }

    private void putRealStudentScanNumber2Redis(Map<Long, String> realStudentScanNumberMap) {// realStudentScanNumberMap映射关系 studentId:ScanNumber
        if (MapUtils.isEmpty(realStudentScanNumberMap)) {
            return;
        }
        List<Long> realStudentIdList = realStudentScanNumberMap.keySet().stream().collect(Collectors.toList());

        int blockSize = 8000;
        int times = realStudentIdList.size() / blockSize;
        int leftNum = realStudentIdList.size() % blockSize;
        if (leftNum == 0) {
            times = times - 1;
        }

        final CountDownLatch latch = new CountDownLatch(times + 1);

        for (int timesIndex = 0; timesIndex <= times; timesIndex++) {
            int beginIndex = 0, endIndex = 0;
            if (timesIndex < times) {
                beginIndex = timesIndex * blockSize;
                endIndex = (timesIndex + 1) * blockSize;
            } else {
                beginIndex = times * blockSize;
                endIndex = realStudentIdList.size();
            }

            List<Long> tempRealStudentIdList = realStudentIdList.subList(beginIndex, endIndex);

            Runnable task = () -> {
                try {
                    logger.info("AutoKlxSchoolScannumberRedis dealRealStudent putRealStudentScanNumber2Redis thread: size is {}", tempRealStudentIdList.size());
                    asyncStudentServiceClient.getAsyncStudentService()
                            .loadStudentSchools(tempRealStudentIdList)
                            .getUninterruptibly()
                            .entrySet()
                            .forEach(longSchoolEntry -> {
                                Long studentId = longSchoolEntry.getKey();
                                Long schoolId = longSchoolEntry.getValue().getId();
                                String scanNumber = realStudentScanNumberMap.get(studentId);
                                if (StringUtils.isNotBlank(scanNumber)) {
                                    IRedisConnection connection = redisClient.getSharedConnection();
                                    RedisSetCommands<String, Object> commands = connection.RedisSetCommands();
                                    commands.sadd("klx_scanNumbers_" + schoolId, scanNumber);
                                    deprecatedSchoolServiceClient.getRemoteReference().addTempSchoolScanNumber(schoolId, scanNumber);
                                }
                            });
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            };
            AlpsThreadPool.getInstance().submit(task);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void putVirtualStudentScanNumber2Redis(List<Map<String, Object>> groupVritualStudentIdList) {
        if (CollectionUtils.isEmpty(groupVritualStudentIdList)) {
            return;
        }
        int blockSize = 8000;
        int times = groupVritualStudentIdList.size() / blockSize;
        int leftNum = groupVritualStudentIdList.size() % blockSize;
        if (leftNum == 0) {
            times = times - 1;
        }

        final CountDownLatch latch = new CountDownLatch(times + 1);

        for (int timesIndex = 0; timesIndex <= times; timesIndex++) {
            int beginIndex = 0, endIndex = 0;
            if (timesIndex < times) {
                beginIndex = timesIndex * blockSize;
                endIndex = (timesIndex + 1) * blockSize;
            } else {
                beginIndex = times * blockSize;
                endIndex = groupVritualStudentIdList.size();
            }

            List<Map<String, Object>> tempGroupVritualStudentIdList = groupVritualStudentIdList.subList(beginIndex, endIndex);
            Runnable task = () -> {
                try {
                    logger.info("AutoKlxSchoolScannumberRedis dealVirtualStudent putVirtualStudentScanNumber2Redis thread: size is {}", tempGroupVritualStudentIdList.size());
                    handleVirtualStudents(tempGroupVritualStudentIdList);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            };

            AlpsThreadPool.getInstance().submit(task);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleVirtualStudents(List<Map<String, Object>> groupVritualStudentIdList) {
        if (CollectionUtils.isEmpty(groupVritualStudentIdList)) {
            return;
        }
        Set<Long> groupIds = new HashSet<>();
        Set<String> studentUserNameSet = new HashSet<>();
        Map<String, Long> userNameGroupIdMap = new LinkedHashMap<>();
        groupVritualStudentIdList.forEach(stringObjectMap -> {
            Long groupId = (Long) stringObjectMap.get("CLAZZ_GROUP_ID");
            String studentUserName = (String) stringObjectMap.get("STUDENT_ID");
            groupIds.add(groupId);
            studentUserNameSet.add(studentUserName);
            if (StringUtils.isNotBlank(studentUserName)) {
                userNameGroupIdMap.put(studentUserName, groupId);
            }
        });

        Map<String, String> userNameScanNumberMap = newKuailexueLoaderClient.loadKlxStudentsByIds(studentUserNameSet).values().stream()
                .filter(studentVirtualUserInfo -> StringUtils.isNotBlank(studentVirtualUserInfo.getScanNumber()))
                .collect(Collectors.toMap(KlxStudent::getId, KlxStudent::getScanNumber));

        Map<Long, Long> groupIdTeacherIdMap = new LinkedHashMap<>();
        raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .groupByGroupIds(groupIds)
                .values()
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> {
                    List<GroupTeacherTuple> list = e.stream()
                            .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                            .collect(Collectors.toList());
                    return list.iterator().next();
                })
                .forEach(e -> groupIdTeacherIdMap.put(e.getGroupId(), e.getTeacherId()));

        Map<Long, Long> teacherIdSchoolIdMap = AlpsFutureBuilder.<Long, List<UserSchoolRef>>newBuilder()
                .ids(groupIdTeacherIdMap.values())
                .generator(id -> schoolLoaderClient.getSchoolLoader().findUserSchoolRefsByUserId(id))
                .buildMap()
                .regularize()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.toMap(UserSchoolRef::getUserId, UserSchoolRef::getSchoolId));

        userNameGroupIdMap.entrySet().forEach(userNameGroupId -> {
            String userName = userNameGroupId.getKey();
            String scanNumber = userNameScanNumberMap.get(userName);
            if (StringUtils.isNotBlank(scanNumber)) {
                Long groupId = userNameGroupId.getValue();
                Long teacherId = groupIdTeacherIdMap.get(groupId);
                if (teacherId != null) {
                    Long schoolId = teacherIdSchoolIdMap.get(teacherId);
                    if (schoolId != null) {
                        IRedisConnection connection = redisClient.getSharedConnection();
                        RedisSetCommands<String, Object> commands = connection.RedisSetCommands();
                        commands.sadd("klx_scanNumbers_" + schoolId, scanNumber);
                        deprecatedSchoolServiceClient.getRemoteReference().addTempSchoolScanNumber(schoolId, scanNumber);
                    }
                }
            }
        });

    }

    private void putKlxStudentScanNumber2Redis(List<Map<String, Object>> groupKlxStudentIdList) {
        if (CollectionUtils.isEmpty(groupKlxStudentIdList)) {
            return;
        }
        int blockSize = 8000;
        int times = groupKlxStudentIdList.size() / blockSize;
        int leftNum = groupKlxStudentIdList.size() % blockSize;
        if (leftNum == 0) {
            times = times - 1;
        }

        final CountDownLatch latch = new CountDownLatch(times + 1);

        for (int timesIndex = 0; timesIndex <= times; timesIndex++) {
            int beginIndex = 0, endIndex = 0;
            if (timesIndex < times) {
                beginIndex = timesIndex * blockSize;
                endIndex = (timesIndex + 1) * blockSize;
            } else {
                beginIndex = times * blockSize;
                endIndex = groupKlxStudentIdList.size();
            }

            List<Map<String, Object>> tempGroupKlxStudentIdList = groupKlxStudentIdList.subList(beginIndex, endIndex);
            Runnable task = () -> {
                try {
                    logger.info("AutoKlxSchoolScannumberRedis dealKlxStudent putKlxStudentScanNumber2Redis thread: size is {}", tempGroupKlxStudentIdList.size());
                    handleKlxStudents(tempGroupKlxStudentIdList);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            };

            AlpsThreadPool.getInstance().submit(task);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleKlxStudents(List<Map<String, Object>> groupKlxStudentIdList) {
        if (CollectionUtils.isEmpty(groupKlxStudentIdList)) {
            return;
        }
        Set<Long> groupIds = new HashSet<>();
        Set<String> klxStudentIdSet = new HashSet<>();
        Map<String, Long> klxStudentGroupIdMap = new LinkedHashMap<>();
        groupKlxStudentIdList.forEach(info -> {
            Long groupId = (Long) info.get("CLAZZ_GROUP_ID");
            String klxStudentId = (String) info.get("STUDENT_ID");
            groupIds.add(groupId);
            klxStudentIdSet.add(klxStudentId);
            if (StringUtils.isNotBlank(klxStudentId)) {
                klxStudentGroupIdMap.put(klxStudentId, groupId);
            }
        });

        Map<String, String> klxStudentScanNumberMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIdSet).values().stream()
                .filter(klxStudent -> StringUtils.isNotBlank(klxStudent.getScanNumber()))
                .collect(Collectors.toMap(KlxStudent::getId, KlxStudent::getScanNumber));

        Map<Long, Long> groupIdTeacherIdMap = new LinkedHashMap<>();
        raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .groupByGroupIds(groupIds)
                .values()
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> {
                    List<GroupTeacherTuple> list = e.stream()
                            .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                            .collect(Collectors.toList());
                    return list.iterator().next();
                })
                .forEach(e -> groupIdTeacherIdMap.put(e.getGroupId(), e.getTeacherId()));

        Map<Long, Long> teacherIdSchoolIdMap = AlpsFutureBuilder.<Long, List<UserSchoolRef>>newBuilder()
                .ids(groupIdTeacherIdMap.values())
                .generator(id -> schoolLoaderClient.getSchoolLoader().findUserSchoolRefsByUserId(id))
                .buildMap()
                .regularize()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.toMap(UserSchoolRef::getUserId, UserSchoolRef::getSchoolId));

        klxStudentGroupIdMap.forEach((klxStudentId, groupId) -> {
            String scanNumber = klxStudentScanNumberMap.get(klxStudentId);
            if (StringUtils.isNotBlank(scanNumber)) {
                Long teacherId = groupIdTeacherIdMap.get(groupId);
                if (teacherId != null) {
                    Long schoolId = teacherIdSchoolIdMap.get(teacherId);
                    if (schoolId != null) {
                        IRedisConnection connection = redisClient.getSharedConnection();
                        RedisSetCommands<String, Object> commands = connection.RedisSetCommands();
                        commands.sadd("klx_scanNumbers_" + schoolId, scanNumber);
                        deprecatedSchoolServiceClient.getRemoteReference().addTempSchoolScanNumber(schoolId, scanNumber);
                    }
                }
            }
        });
    }

}
