package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.campaign.api.OpenSchoolTestService;
import com.voxlearning.utopia.service.campaign.api.mapper.OpenSchoolTest;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.mapper.IntegralInfo;
import com.voxlearning.utopia.service.integral.client.IntegralServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
@ExposeService(interfaceClass = OpenSchoolTestService.class)
public class OpenSchoolTestServiceImpl implements OpenSchoolTestService {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private IntegralServiceClient integralServiceClient;
    @Inject
    private CampaignCacheSystem campaignCacheSystem;

    private Set<Integer> clazzLevelSet = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));    // 只关注1~6年级
    private static final long END_DATE = 1552579200000L;  // 2019-03-07 测试结束,缓存保留到 3月15日0点
    public static final long SEND_REWARD_LIMIT = RuntimeMode.isProduction() ? 20L : 3L;     // 达成奖励人数
    private static Set<Integer> cityCode = new HashSet<>();
    public static Set<String> newExamId = new HashSet<>();

    static {
        cityCode.add(500100);
        cityCode.add(420100);
        cityCode.add(410100);
        cityCode.add(371300);
        cityCode.add(430100);
        cityCode.add(350200);
        cityCode.add(130200);
        cityCode.add(130600);

        if (RuntimeMode.isUsingTestData() || RuntimeMode.isStaging()) {
            cityCode.add(110100);
        }

        if (RuntimeMode.ge(Mode.STAGING)) {
            newExamId.add("E_10200310980913");
            newExamId.add("E_10200310981850");
            newExamId.add("E_10200310982543");
            newExamId.add("E_10200310983691");
            newExamId.add("E_10200310984012");
            newExamId.add("E_10200310985995");
        } else {
            newExamId.add("E_10200299987462");
            newExamId.add("E_10200299988019");
        }
        if (RuntimeMode.isStaging()) {
            newExamId.add("E_10200310996005");
        }
    }


    @Override
    public MapMessage index(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(getMainTeacher(teacherId));
        Region region = raikouSystem.loadRegion(teacherDetail.getCityCode());

        if (!teacherDetail.getSubjects().contains(Subject.MATH)) {
            MapMessage errMsg = MapMessage.errorMessage("暂不支持的学科").add("code", "201");
            if (region != null) {
                errMsg.put("city_code", region.getCode());
                errMsg.put("city_name", region.getName());
            }
            return errMsg;
        }
        if (!cityCode.contains(teacherDetail.getCityCode())) {
            MapMessage errMsg = MapMessage.errorMessage("暂不支持该地区").add("code", "202");
            if (region != null) {
                errMsg.put("city_code", region.getCode());
                errMsg.put("city_name", region.getName());
            }
            return errMsg;
        }

        MapMessage mapMessage = MapMessage.successMessage();

        if (region != null) {
            mapMessage.put("city_code", region.getCode());
            mapMessage.put("city_name", region.getName());
        }

        Long studentSubmit = getStudentSubmit(teacherId);
        OpenSchoolTest openSchoolTest = loadByTeacher(teacherId);
        Set<Integer> clazzLevel = getTeacherClazzLevel(teacherId);

        mapMessage.put("clazz_level", clazzLevel);
        mapMessage.put("assign_size", Math.min(studentSubmit, SEND_REWARD_LIMIT));
        mapMessage.put("assign_ed", openSchoolTest != null);                 // 是否已参加活动
        mapMessage.put("assign_reward", studentSubmit >= SEND_REWARD_LIMIT); // 是否已发布置奖励
        mapMessage.put("share_reward", teacherShareSuccess(teacherId));      // 是否已发分享奖励
        if (openSchoolTest != null) {
            mapMessage.put("group", openSchoolTest.getGroupList());
        }
        return mapMessage;
    }

    // 注意更新的原子性 start
    @Override
    public OpenSchoolTest loadByTeacher(Long teacherId) {
        String cacheKey = genTeacherAssignCacheKey(teacherId);
        CacheObject<Object> cacheObject = campaignCacheSystem.CBS.storage.get(cacheKey);
        if (cacheObject.containsValue()) {
            return (OpenSchoolTest) cacheObject.getValue();
        }
        return null;
    }

    @Override
    public void save(OpenSchoolTest openSchoolTest) {
        String cacheKey = genTeacherAssignCacheKey(openSchoolTest.getTeacherId());
        campaignCacheSystem.CBS.storage.set(cacheKey, getExpirationInSeconds(), openSchoolTest);
    }

    @Override
    public void addGroupId(Long teacherId, Long groupId) {
        OpenSchoolTest openSchoolTest = loadByTeacher(teacherId);
        if (openSchoolTest == null) {
            openSchoolTest = new OpenSchoolTest();
            openSchoolTest.setTeacherId(teacherId);
            openSchoolTest.setShareReward(false);
            openSchoolTest.setGroupList(new HashSet<>());
        }
        openSchoolTest.getGroupList().add(groupId);
        save(openSchoolTest);
    }

    @Override
    public Long incrStudentSubmit(Long teacherId) {
        String cacheKey = genStudentSubmitCacheKey(teacherId);
        Long incr = campaignCacheSystem.CBS.storage.incr(cacheKey, 1, 1, getExpirationInSeconds());
        return incr;
    }

    @Override
    public Long getStudentSubmit(Long teacherId) {
        String cacheKey = genStudentSubmitCacheKey(teacherId);
        CacheObject<Object> cacheObject = campaignCacheSystem.CBS.storage.get(cacheKey);
        if (cacheObject.containsValue()) {
            return SafeConverter.toLong(cacheObject.getValue());
        }
        return 0L;
    }

    @Override
    public Long setStudentSubmit(Long teacherId, Long count) {
        String cacheKey = genStudentSubmitCacheKey(teacherId);
        return campaignCacheSystem.CBS.storage.incr(cacheKey, 0, count, getExpirationInSeconds());
    }
    // 注意原子性 end

    public void sendTeacherReward(Long teacherId, Integer num, String comment, String uniqueKeyPrefix) {
        int integral = num * 10;
        IntegralInfo integralInfo = new IntegralInfo();
        integralInfo.setUserId(teacherId);
        integralInfo.setIntegralType(IntegralType.TEACHER_ACTIVITY_OPEN_SCHOOL.getType()); // TODO: 学豆类型需要修改
        integralInfo.setUniqueKey(uniqueKeyPrefix + teacherId);
        integralInfo.setIntegral(integral);
        integralInfo.setComment(comment);
        boolean duplicate = integralServiceClient.checkDuplicate(integralInfo);
        if (!duplicate) {
            MapMessage reward = integralServiceClient.reward(integralInfo);
            if (!reward.isSuccess()) {
                log.warn("开学第一课智能测试发放奖励时异常, userId:{},info:{}", teacherId, reward.getInfo());
            }
        }
    }

    @Override
    public Long getMainTeacher(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null) {
            mainTeacherId = teacherId;
        }
        return mainTeacherId;
    }

    @Override
    public void teacherShare(Long teacherId) {
        String cacheKey = genTeacherShareCacheKey(teacherId);
        Long incr = campaignCacheSystem.CBS.storage.incr(cacheKey, 1, 1, getExpirationInSeconds());
        // 只第一次分享发奖励
        if (Objects.equals(incr, 1L)) {
            sendTeacherReward(teacherId, 50, "新学期开学第一课分享奖励", "OPEN_SCHOOL_SHARE_");
        }
    }

    private boolean teacherShareSuccess(Long teacherId) {
        String cacheKey = genTeacherShareCacheKey(teacherId);
        CacheObject<Object> cacheObject = campaignCacheSystem.CBS.storage.get(cacheKey);
        return cacheObject.containsValue();
    }

    private int getExpirationInSeconds() {
        long diff = (END_DATE - System.currentTimeMillis()) / 1000;
        return SafeConverter.toInt(diff);
    }

    private String genTeacherAssignCacheKey(Long teacherId) {
        return "CAMPAIGN:OPEN_SCHOOL_ASSIGN:" + teacherId;
    }

    private String genTeacherShareCacheKey(Long teacherId) {
        return "CAMPAIGN:OPEN_SCHOOL_SHARE:" + teacherId;
    }

    private String genStudentSubmitCacheKey(Long teacherId) {
        return "CAMPAIGN:OPEN_SCHOOL_SUBMIT:" + teacherId;
    }

    private Set<Integer> getTeacherClazzLevel(Long mainTeacherId) {
        List<Long> teacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        HashSet<Long> teacherIdSet = new HashSet<>(teacherIds);
        teacherIdSet.add(mainTeacherId);

        return teacherLoaderClient.loadTeachersClazzIds(teacherIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(clazzId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isTerminalClazz())
                .map(clazz -> clazz.getClazzLevel().getLevel())
                .filter(i -> clazzLevelSet.contains(i))
                .sorted(Integer::compare)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
