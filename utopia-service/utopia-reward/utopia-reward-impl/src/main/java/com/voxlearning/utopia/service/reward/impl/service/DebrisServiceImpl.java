package com.voxlearning.utopia.service.reward.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.reward.api.DebrisService;
import com.voxlearning.utopia.service.reward.entity.Debris;
import com.voxlearning.utopia.service.reward.entity.DebrisHistory;
import com.voxlearning.utopia.service.reward.entity.DebrisType;
import com.voxlearning.utopia.service.reward.impl.dao.DebrisDao;
import com.voxlearning.utopia.service.reward.impl.dao.DebrisHistoryDao;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ExposeService(interfaceClass = DebrisService.class)
public class DebrisServiceImpl extends SpringContainerSupport implements DebrisService {

    private static final Logger log = LoggerFactory.getLogger(DebrisServiceImpl.class);

    @Inject
    private DebrisDao debrisDao;
    @Inject
    private DebrisHistoryDao debrisHistoryDao;
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public List<DebrisHistory> loadDebrisHistoryByUserId(Long userId) {
        return debrisHistoryDao.loadByUserId(userId);
    }

    @Override
    public Debris loadDebrisByUserId(Long userId) {
        Debris debris = debrisDao.loadByUserId(userId);
        if (debris == null) {
            debris = new Debris();
            debris.setUserId(userId);
            debris.setTotalDebris(0L);
            debris.setUsableDebris(0L);
        }
        return debris;
    }

    @Override
    public MapMessage changeDebris(User user, DebrisHistory debrisHistory) {
        if (debrisHistory == null) {
            return MapMessage.errorMessage("历史为空");
        }
        Long userId = debrisHistory.getUserId();
        if (userId == null) {
            return MapMessage.errorMessage("用户ID为空");
        }
        Student student = studentLoaderClient.loadStudent(userId);
        if (student == null) {
            return MapMessage.errorMessage("未查找到该学生");
        }
        DebrisType debrisType = DebrisType.of(debrisHistory.getDebrisType());
        if (debrisType == DebrisType.UNKNOWN) {
            return MapMessage.errorMessage("未知的碎片类型");
        }
        if (debrisHistory.getDebris() == null) {
            debrisHistory.setDebris(0L);
        }
        debrisHistory.setComment(StringUtils.defaultString(debrisHistory.getComment()));

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("DebrisServiceImpl:changeDebris")
                    .keys(user.getId())
                    .callback(() -> executeChangeDebris(userId, debrisHistory))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("有正在处理的请求, 请重试...");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage changeDebris(Long userId, Integer debrisType, Long debris, String comment) {
        DebrisHistory debrisHistory = new DebrisHistory(userId, debrisType, debris);
        debrisHistory.setComment(comment);
        User user = new User();
        user.setId(userId);
        return changeDebris(user, debrisHistory);
    }

    private MapMessage executeChangeDebris(Long userId, DebrisHistory debrisHistory) {
        log.debug("changeDebris : userId             = {}", userId);
        log.debug("changeDebris : debris             = {}", debrisHistory.getDebris());
        log.debug("changeDebris : comment            = {}", debrisHistory.getComment());
        log.debug("changeDebris : debrisType         = {}", debrisHistory.getDebrisType());
        log.debug("changeDebris : addDebrisUserId    = {}", debrisHistory.getAddDebrisUserId());

        try {
            Debris debris = debrisDao.loadByUserId(userId);
            if (debris == null) {
                debris = new Debris();
                debris.setUserId(userId);
                debris.setTotalDebris(0L);
                debris.setUsableDebris(0L);
            }

            if (debris.getUsableDebris() + debrisHistory.getDebris() < 0) {
                return MapMessage.successMessage("碎片金额不足");
            }

            Long totalDebrisBefore = debris.getTotalDebris();
            Long usableDebrisBefore = debris.getUsableDebris();

            debrisHistory.setTotalDebrisBefore(totalDebrisBefore);
            debrisHistory.setUsableDebrisBefore(usableDebrisBefore);

            // 增加的情况
            if (debrisHistory.getDebris() > 0) {
                debris.setTotalDebris(totalDebrisBefore + debrisHistory.getDebris());   // 累加总额
                debrisHistory.setTotalDebrisAfter(totalDebrisBefore + debrisHistory.getDebris()); // 保存总额
            }
            debris.setUsableDebris(debris.getUsableDebris().intValue() + debrisHistory.getDebris());  // 累加可用额
            debrisHistory.setUsableDebrisAfter(debris.getUsableDebris().intValue() + debrisHistory.getDebris()); // 保存可用额

            debrisHistory.setTotalDebrisAfter(debris.getTotalDebris());
            debrisHistory.setUsableDebrisAfter(debris.getUsableDebris());

            debrisHistoryDao.upsert(debrisHistory);
            debrisDao.upsert(debris);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }
}
