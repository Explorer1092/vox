package com.voxlearning.utopia.service.reward.impl.internal;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.constant.RewardConstants;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.newversion.PrizeClaw;
import com.voxlearning.utopia.service.reward.entity.newversion.PrizeClawWinningRecord;
import com.voxlearning.utopia.service.reward.impl.dao.PrizeClawDao;
import com.voxlearning.utopia.service.reward.impl.dao.PrizeClawWinningRecordDao;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.RewardServiceImpl;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardServiceImpl;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.zone.client.PersonalZoneServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Named
public class InternalGameService extends SpringContainerSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private PrizeClawDao prizeClawGameDao;

    @Inject
    private PrizeClawWinningRecordDao prizeClawWinningRecordDao;

    @Inject
    private UserIntegralServiceClient userIntegralServiceClient;
    @Inject
    private InternalTobyService internalTobyService;
    @Inject
    private RewardLoaderImpl rewardLoader;
    @Inject
    private RewardServiceImpl rewardServiceImpl;
    @Inject
    private PrivilegeServiceClient privilegeServiceClient;
    @Inject
    private PersonalZoneServiceClient personalZoneServiceClient;
    @Inject private InternalRewardOrderService internalCreateRewardOrder;
    @Inject private NewRewardServiceImpl newRewardService;

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;


    private IRedisCommands redisCommands;

    private static final String KEY_REWARD_CLAW_NEWCOMERS_TIP = "KEY_REWARD_CLAW_NEWCOMERS_TIP";

    private static final String KEY_REWARD_GAME_CLAW_ONEDAY_LIMIT = "KEY_REWARD_GAME_CLAW_ONEDAY_LIMIT";

    @Override
    public void afterPropertiesSet() {
        redisCommands = RedisCommandsBuilder.getInstance().getRedisCommands("user-easemob");
    }

    public List<PrizeClaw> loadGameBySite(int site) {
        return prizeClawGameDao.loadBySite(site);
    }

    private String genKey(String perfix, String... keyParts) {
        return perfix + ":" + StringUtils.join(keyParts, ":");
    }

    /**
     * 尝试获取，提示语显示标志
     *
     * @param userId
     * @return
     */
    public Boolean tryShowTip(Long userId) {
        Boolean result = false;
        String key = genKey(KEY_REWARD_CLAW_NEWCOMERS_TIP, String.valueOf(userId));
        try {
            RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
            Object obj = stringCommands.get(key);
            if (obj == null) {
                result = true;
                stringCommands.set(key, 1);
            } else if ((Integer) obj < 3) {
                result = true;
                stringCommands.set(key, (Integer) obj + 1);
            }
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 尝试获取，并加一
     *
     * @param userId
     * @return
     */
    public Boolean tryGameOneDayLimitAndAdd(Long userId) {
        Integer gametimes = 1;
        String key = genKey(KEY_REWARD_GAME_CLAW_ONEDAY_LIMIT, String.valueOf(userId));
        RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        Object obj = stringCommands.get(key);
        if (obj != null) {
            gametimes += (Integer) obj;
        }

        stringCommands.set(key, gametimes);
        // 设置过期时间
        Long ttl = keyCommands.ttl(key);
        if(ttl == -1){
            keyCommands.expireat(key, DateUtils.getTodayEnd());
        }
        return gametimes < 10;
    }

    /**
     * 尝试获取
     * @param userId
     * @return
     */
    public Boolean tryGameOneDayLimit(Long userId) {
        String key = genKey(KEY_REWARD_GAME_CLAW_ONEDAY_LIMIT, String.valueOf(userId));
        RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
        Object obj = stringCommands.get(key);

        return obj==null || (Integer)obj < 10;
    }


    public List<PrizeClawWinningRecord> loadPrizeClawWinningRecord(long userId) {
        return prizeClawWinningRecordDao.loadByUserId(userId);
    }

    public void addPrizeClawWinningRecord(PrizeClawWinningRecord record) {
        prizeClawWinningRecordDao.insert(record);
    }

    public PrizeClaw loadOne(long id) {
        return prizeClawGameDao.load(id);
    }

    public Boolean prizeClawJudge(User user, long id) {

        boolean result = true;
        PrizeClaw prizeClaw = prizeClawGameDao.load(id);
        if (prizeClaw == null) {
            logger.warn("prizeClaw is not found id:{}, useId:{}", id, user.getId());
            return false;
        }
        IntegralHistory comsumer = new IntegralHistory(user.getId(), IntegralType.REWARD_GAME_CLAW_PAY_INTEGRAL, -1 * prizeClaw.getConsumerNum());
        comsumer.setComment(RewardConstants.STUSENT_REWARD_NAME + "抓一抓小游戏支出");
        comsumer.setComment(newRewardService.fetchRewardIntegralComment(comsumer.toIntegralType()));

        MapMessage comsumerMsg = userIntegralServiceClient.getUserIntegralService().changeIntegral(comsumer);
        if (!comsumerMsg.isSuccess()) {
            logger.warn("changeIntegral error integralHistory:{}", comsumer.toString());
            return false;
        }

        if (isPrize(prizeClaw)) {
            if (prizeClaw.getPrizeType() == PrizeClaw.PrizeType.INTEGRAL.getType()) {
                IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.REWARD_GAME_CLAW_GOT_INTEGRAL, prizeClaw.getPrize().intValue());
                integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

                MapMessage msg = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
                if (!msg.isSuccess()) {
                    logger.warn("changeIntegral error integralHistory:{}", integralHistory.toString());
                    result = false;
                }
            } else if (prizeClaw.getPrizeType() == PrizeClaw.PrizeType.TOPKNOT.getType()) {

                MapMessage message = internalCreateRewardOrder.createRewardOrder(user, prizeClaw.getPrize(), false, RewardOrder.Source.claw);
                if (message.isSuccess()) {
                    dealAfterOrderCreated(user, prizeClaw.getPrize(), 1);
                } else {
                    logger.warn("topknot createOrder error userId:{}, prizeClaw:{}, msg:{}", user.getId(), prizeClaw.toString(), message.getInfo());
                    result = false;
                }

            } else if (prizeClaw.getPrizeType() == PrizeClaw.PrizeType.TOBY_IMAGE.getType()) {
                MapMessage message = internalCreateRewardOrder.createRewardOrder(user, prizeClaw.getPrize(), false, RewardOrder.Source.claw);
                if (message.isSuccess()) {
                    internalTobyService.ownTobyImageUnexpendIntegal(user.getId(), prizeClaw.getPrize());
                } else {
                    logger.warn("toby createOrder error userId:{}, prizeClaw:{}, msg:{}", user.getId(), prizeClaw.toString(), message.getInfo());
                    result = false;
                }

            } else {
                logger.warn("prize type is not allow default prizeClaw:{}", prizeClaw.toString());
            }
        } else {
            result = false;
        }

        if (result) {
            PrizeClawWinningRecord record = new PrizeClawWinningRecord();
            record.setConsumeNum(prizeClaw.getConsumerNum());
            record.setPrizeClawId(prizeClaw.getId());
            record.setSite(prizeClaw.getSite());
            record.setUserId(user.getId());
            record.setIsPrize(result);
            record.setPrize(prizeClaw.getPrize());
            record.setPrizeName(prizeClaw.getPrizeName());
            record.setPrizeType(prizeClaw.getPrizeType());
            addPrizeClawWinningRecord(record);
        }

        return result;
    }

    private MapMessage dealAfterOrderCreated(User user, Long productId, int quantity) {
        RewardProduct rewardProduct = rewardLoader.getRewardProductBuffer().loadRewardProductMap().get(productId);
        if (rewardProduct == null) {
            logger.error("Reward center prize claw config error, product {} is not found", productId);
            return MapMessage.errorMessage("未找到商品");
        }

        String headWearId = rewardProduct.getRelateVirtualItemId();
        if (StringUtils.isBlank(headWearId)) {
            return MapMessage.successMessage();
        }

        Privilege p = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(headWearId);
        if (p == null) {
            return MapMessage.errorMessage("头饰不存在!");
        }

        Date now = new Date();
        Date expiryDate;
        Integer expiryLength = rewardProduct.getExpiryDate();
        if (expiryLength == null || expiryLength == 0) {
            expiryDate = null;
        } else {
            expiryDate = new Date(DateUtils.roundDateToDay235959InMillis(now, expiryLength * quantity));
        }

        // 购买后，再装扮上
        privilegeServiceClient.getPrivilegeService().grantPrivilege(user.getId(), p, expiryDate);
        personalZoneServiceClient.getPersonalZoneService().changeHeadWear(user.getId(), headWearId);

        return MapMessage.successMessage();
    }

    /**
     * 得到一个100以内的随机正整数，和中奖概率对比，
     * 如果随机数大于概率数则不中奖，否则中奖
     *
     * @param prizeClaw
     * @return
     */
    private boolean isPrize(PrizeClaw prizeClaw) {
        Random random = new Random();
        int judgeNum = random.nextInt(1000000) + 1;
        if (prizeClaw.getOdds() > judgeNum) {
            return true;
        }
        return false;
    }

}
