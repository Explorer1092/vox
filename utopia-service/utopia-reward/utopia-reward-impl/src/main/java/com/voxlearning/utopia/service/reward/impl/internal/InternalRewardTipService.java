package com.voxlearning.utopia.service.reward.impl.internal;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.clazz.api.ClazzLoader;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
public class InternalRewardTipService extends SpringContainerSupport {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private static final String KEY_REMARD_TIP = "REMARD_CENTRE_TIP";
    private static final String INTEGRAL_OFFSET_FREIGHT_TIP_TIMES = "INTEGRAL_OFFSET_FREIGHT_TIP";

    private static final String KEY_GRADUATE_STOP_CONVERSION_TIP = "KEY_GRADUATE_STOP_CONVERSION_TIP";
    private static final String GRADUATE_STOP_CONVERSION_TIP_TIMES = "GRADUATE_STOP_CONVERSION_TIP";
    private IRedisCommands redisCommands;

    @ImportService(interfaceClass = TeacherLoader.class)
    private TeacherLoader teacherLoader;
    @ImportService(interfaceClass = ClazzLoader.class)
    private ClazzLoader clazzLoader;

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    /**
     * 返回所有需要显示tip的与运算
     * @param user
     * @return
     */
    public int tryShowTip(User user) throws InterruptedException {
        long userId = user.getId();
        int result = 0;
        //仅仅老师有此提示
        if (UserType.TEACHER.equals(user.fetchUserType()) && this.isOpenIntegralOffsetFreightTip()) {
            String day = org.apache.http.client.utils.DateUtils.formatDate(new Date(), "yyyy-MM-dd");
            String key = genKey(KEY_REMARD_TIP, day);
            if (this.tryShowTip(userId, key)) {
                result += 1;
            }
        }

        if (isGraduateStopConvert(user)) {
            String day = org.apache.http.client.utils.DateUtils.formatDate(new Date(), "MM-dd");
            String key = genKey(KEY_GRADUATE_STOP_CONVERSION_TIP, day);
            if (this.tryShowTip(user.getId(), key)) {
                result += 2;
            }
        }
        return result;
    }

    public boolean isGraduateStopConvert(User user) throws InterruptedException {
        boolean isGraduateUser = false;
        if (this.isOpenGraduateStopConvertTip()) {
            if (user instanceof StudentDetail) {
                StudentDetail studentDetail = (StudentDetail) user;
                Clazz clazz = studentDetail.getClazz();
                if (clazz != null && clazz.getClazzLevel() != null) {
                    if (clazz.getClazzLevel().getLevel() == ClazzLevel.FIFTH_GRADE.getLevel() && EduSystemType.P5.equals(clazz.getEduSystem())
                            || clazz.getClazzLevel().getLevel() == ClazzLevel.SIXTH_GRADE.getLevel() && EduSystemType.P6.equals(clazz.getEduSystem())
                            || clazz.getClazzLevel().getLevel() == ClazzLevel.NINTH_GRADE.getLevel()
                            || clazz.isTerminalClazz()) {
                        isGraduateUser = true;
                    }
                }
            } else if (user instanceof TeacherDetail) {
                TeacherDetail teacherDetail = (TeacherDetail) user;
                List<Long> clazzIds = teacherLoader.loadTeacherClazzIds(teacherDetail.getId());
                if (clazzIds != null && !clazzIds.isEmpty()) {
                    AlpsFuture<Map<Long, Clazz>> future = clazzLoader.loadClazzs(clazzIds);
                    Map<Long, Clazz> clazzMap = future.get();
                    if (clazzMap != null && !clazzMap.isEmpty()) {
                        for (Clazz clazz : clazzMap.values()) {
                            if (clazz.getClazzLevel().getLevel() == ClazzLevel.FIFTH_GRADE.getLevel() && clazz.getEduSystem().equals(EduSystemType.P5)
                                    || clazz.getClazzLevel().getLevel() == ClazzLevel.SIXTH_GRADE.getLevel() && clazz.getEduSystem().equals(EduSystemType.P6)
                                    || clazz.getClazzLevel().getLevel() == ClazzLevel.NINTH_GRADE.getLevel()
                                    || clazz.isTerminalClazz()) {
                                isGraduateUser = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return isGraduateUser;
    }

    /**
     * 尝试获取，提示语显示标志，返回1显示，返回0不显示
     * @param userId
     * @param key
     * @return
     */
    private boolean tryShowTip(long userId,  String key) {
        boolean result = false;
        try {
            RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
            RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();

            if (stringCommands.getbit(key, userId) == 0) {
                result = true;
                stringCommands.setbit(key, userId, 1);

                // 设置过期时间
                long ttl = keyCommands.ttl(key);
                if(ttl == -1){
                    keyCommands.expireat(key, com.voxlearning.alps.calendar.DateUtils.getTodayEnd());
                }
            }
        } catch (Exception e) {
        }

        return result;
    }


    private boolean isOpenGraduateStopConvertTip() {
        String tadayStr = DateUtils.dateToString(new Date(), "MM-dd");
        String beginDayStr = "05-01";
        String endDayStr = "06-05";

        String openTimes = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(),
                GRADUATE_STOP_CONVERSION_TIP_TIMES);
        if (StringUtils.isNotBlank(openTimes)) {
            String [] strs = openTimes.split(",");
            if (strs.length == 2) {
                beginDayStr = strs[0];
                endDayStr = strs[1];
            }
        }

        if (tadayStr.compareTo(endDayStr) <= 0 && tadayStr.compareTo(beginDayStr) >= 0) {
            return true;
        }
        return false;
    }

    private boolean isOpenIntegralOffsetFreightTip() {
        String tadayStr = DateUtils.getTodaySqlDate();
        String beginDayStr = "2018-07-01";
        String endDayStr = "2018-09-05";

        String openTimes = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(),
                INTEGRAL_OFFSET_FREIGHT_TIP_TIMES);
        if (StringUtils.isNotBlank(openTimes)) {
            String [] strs = openTimes.split(",");
            if (strs.length == 2) {
                beginDayStr = strs[0];
                endDayStr = strs[1];
            }
        }

        if (tadayStr.compareTo(endDayStr) <= 0 && tadayStr.compareTo(beginDayStr) >= 0) {
            return true;
        }
        return false;
    }

    private String genKey(String perfix, String... keyParts){
        return perfix + ":" + StringUtils.join(keyParts,":");
    }
}
