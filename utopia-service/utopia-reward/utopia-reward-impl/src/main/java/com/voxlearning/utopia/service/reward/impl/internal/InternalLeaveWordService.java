package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.reward.entity.DebrisType;
import com.voxlearning.utopia.service.reward.entity.newversion.LeaveWord;
import com.voxlearning.utopia.service.reward.entity.newversion.LeaveWordGoods;
import com.voxlearning.utopia.service.reward.impl.dao.LeaveWordDao;
import com.voxlearning.utopia.service.reward.impl.dao.LeaveWordGoodsDao;
import com.voxlearning.utopia.service.reward.impl.service.DebrisServiceImpl;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class InternalLeaveWordService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;
    @Inject
    private DebrisServiceImpl debrisService;
    @Inject
    private InternalRewardCenterService internalRewardCenterService;
    @Inject
    private LeaveWordDao leaveWordDao;

    @Inject
    private LeaveWordGoodsDao leaveWordGoodsDao;

    public List<LeaveWord> loadLeaveWord(Long userId) {
        return leaveWordDao.loadByUserId(userId);
    }

    public List<LeaveWord> loadUnreadLeaveWord(Long userId) {
        return leaveWordDao.loadByUserId(userId)
                .stream()
                .filter(i -> !i.getIsRead())
                .collect(Collectors.toList());
    }

    public boolean isHasUnreadLeaveWord(long userId) {
        List<LeaveWord> leaveWordList = loadUnreadLeaveWord(userId);
        return leaveWordList != null && !leaveWordList.isEmpty();
    }

    public void updateToReadAlready(long userId, Set<Long> idList) {
        leaveWordDao.updateToReadAlready(userId, idList);
    }

    public MapMessage doLeaveWord(User user, long businessUserId, long leaveWordId) {
        LeaveWordGoods leaveWordGoods = leaveWordGoodsDao.load(leaveWordId);
        if (leaveWordGoods == null) {
            return MapMessage.errorMessage("留言不存在！");
        }

        if (leaveWordGoods.getSpendType() == LeaveWordGoods.SpendType.FRAGMENT.intValue()) {
            long fragmentNum = debrisService.loadDebrisByUserId(user.getId()).getUsableDebris();
            if (fragmentNum < leaveWordGoods.getPrice()) {
                return MapMessage.errorMessage("碎片不足，留言失败！");
            }
            debrisService.changeDebris(user.getId(), DebrisType.TOBY.getType(), -leaveWordGoods.getPrice() * 1l, "给同学留言");
        } else if (leaveWordGoods.getSpendType() == LeaveWordGoods.SpendType.INTEGRAL.intValue()){
            long integralNum = internalRewardCenterService.getIntegral(user);
            if (integralNum < leaveWordGoods.getPrice()) {
                return MapMessage.errorMessage("学豆不足，留言失败！");
            }
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.REWARD_TOBY_INTEGRAL, -leaveWordGoods.getPrice());
            integralHistory.setComment("奖品中心留言支出");
            MapMessage mapMessage = userIntegralService.changeIntegral(integralHistory);
            if (!mapMessage.isSuccess()) {
                logger.warn("doLeaveWord changeIntegral error:{}", mapMessage.getInfo());
                return MapMessage.errorMessage("留言失败");
            }
        } else {
            logger.warn(String.format("leaveWordGoods spend type error bean:%s", leaveWordGoods.toString()));
            return MapMessage.errorMessage("留言失败！");
        }

        LeaveWord leaveWord = new LeaveWord();
        leaveWord.setVisitorUserId(user.getId());
        leaveWord.setUserId(businessUserId);
        leaveWord.setLeaveWordGoodsId(leaveWordId);
        leaveWord.setIsRead(false);
        leaveWordDao.upsert(leaveWord);

        return MapMessage.successMessage();
    }

    public List<LeaveWordGoods> loadAllLeaveWordGoods() {
        return leaveWordGoodsDao.loadAll();
    }

    public Map<Long, LeaveWordGoods> loadAllLeaveWordGoodsMap() {
        Map<Long, LeaveWordGoods> result = Collections.emptyMap();
        List<LeaveWordGoods> goodsList = leaveWordGoodsDao.loadAll();
        if (goodsList != null && !goodsList.isEmpty()) {
            result = goodsList
                    .stream()
                    .collect(Collectors.toMap(LeaveWordGoods::getId, Function.identity()));
        }
        return result;
    }
}
