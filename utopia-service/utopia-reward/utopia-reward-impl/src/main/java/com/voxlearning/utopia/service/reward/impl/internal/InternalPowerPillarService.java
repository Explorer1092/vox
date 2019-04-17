package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.PowerPizePoolMapper;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.TryPowerPizeMapper;
import com.voxlearning.utopia.service.reward.constant.RewardConstants;
import com.voxlearning.utopia.service.reward.entity.DebrisType;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPillar;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrize;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrizeRecord;
import com.voxlearning.utopia.service.reward.impl.dao.PowerPillarDao;
import com.voxlearning.utopia.service.reward.impl.dao.PowerPrizeDao;
import com.voxlearning.utopia.service.reward.impl.dao.PowerPrizeRecordDao;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.DebrisServiceImpl;
import com.voxlearning.utopia.service.reward.impl.service.RewardServiceImpl;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Named
public class InternalPowerPillarService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int FIRST_POWER_FULL_NUM = 600;
    private static final int SECOND_POWER_FULL_NUM = 1000;
    private static final int ORTHER_POWER_FULL_NUM = 2000;

    private static final long FIRST_POWER_PRIZE_NUM = 40;
    private static final long SECOND_POWER_PRIZE_NUM = 60;
    private static final long ORTHER_POWER_PRIZE_NUM = 100;

    @Inject
    private PowerPillarDao powerPillarDao;
    @Inject
    private PowerPrizeDao powerPrizeDao;
    @Inject
    private DebrisServiceImpl debrisService;
    @Inject
    private PowerPrizeRecordDao powerPrizeRecordDao;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private InternalRewardOrderService internalRewardOrderService;

    public PowerPillar getPowerPillar(long userId) {
        return powerPillarDao.loadByUserId(userId);
    }

    public boolean addPowerPillarNum(long userId, int num) {
        synchronized (powerPillarDao) {
            if (powerPillarDao.loadByUserId(userId) == null) {
                PowerPillar powerPillar = new PowerPillar();
                powerPillar.setUserId(userId);
                powerPillar.setPowerPillar(num);
                powerPillar.setResetNumber(0);
                powerPillarDao.insert(powerPillar);
            } else {
                powerPillarDao.addPowerPillarNum(userId, num);
            }
        }
        return true;
    }

    public void resetPowerPillar(long userId, int powerNum) {
        PowerPillar powerPillar = powerPillarDao.loadByUserId(userId);
        if (powerPillar == null) {
            powerPillar = new PowerPillar();
            powerPillar.setUserId(userId);
            powerPillar.setPowerPillar(0);
            powerPillar.setResetNumber(0);
        } else {
            int resetNmber = powerPillar.getResetNumber();
            powerPillar.setResetNumber(resetNmber + 1);
            int newPower = powerPillar.getPowerPillar() - powerNum;
            powerPillar.setPowerPillar(newPower>0 ? newPower:0);
        }
        powerPillarDao.upsert(powerPillar);
    }

    public PowerPizePoolMapper loadPowerPrizePool(User user) {
        PowerPizePoolMapper mapper = new PowerPizePoolMapper();
        PowerPillar powerPillar = powerPillarDao.loadByUserId(user.getId());
        int level = this.getPowerLevel(powerPillar);
        int fullPowerNumber = this.getFullPowerNumber(powerPillar);
        long fragmentNum = this.getFragmentPrizeNum(powerPillar);
        mapper.setFragmentNum(fragmentNum);
        mapper.setFullPowerNumber(fullPowerNumber);
        Integer powerPillarNum = (powerPillar==null||powerPillar.getPowerPillar()==null) ? 0:powerPillar.getPowerPillar();
        mapper.setPowerPillar(powerPillarNum);

        boolean isRealGoodsOfflineCity = isRealGoodsOfflineCity((StudentDetail) user);
        boolean isRealGoodsOfflineTerminal = isRealGoodsOfflineTerminal((StudentDetail)user);
        if (isRealGoodsOfflineCity || isRealGoodsOfflineTerminal) {
            return mapper;
        }
        List<PowerPrize> powerPrizeList = powerPrizeDao.loadByLevel(level);
        if (powerPrizeList == null || powerPrizeList.isEmpty()) {
            logger.warn(String.format("powerPrizeList is empty userId", user.getId()));
        } else {
            List<PowerPizePoolMapper.RealGoodsEntity> realGoodsList = powerPrizeList
                    .stream()
                    .map(t -> {
                        PowerPizePoolMapper.RealGoodsEntity entity = mapper.new RealGoodsEntity();
                        entity.setName(t.getName());
                        entity.setPictuerUrl(t.getPicterUrl());
                        return entity;
                    })
                    .collect(toList());
            mapper.setRealGoodsList(realGoodsList);
        }
        return mapper;
    }

    public List<PowerPrize> loadAllPowerPrize() {
        return powerPrizeDao.loadAll();
    }

    public PowerPrize loadPowerById(long id) {
        return powerPrizeDao.load(id);
    }

    public void updatePowerPrize(PowerPrize powerPrize) {
        powerPrizeDao.upsert(powerPrize);
    }

    public void deletePowerPrize(long id) {
        powerPrizeDao.remove(id);
    }

    /**
     * key：能量柱满奖励级别
     * value:奖励内容
     * @param user
     * @return
     */
    public void tryPowerPrize(User user, TryPowerPizeMapper mapper) {
        PowerPillar powerPillar = powerPillarDao.loadByUserId(user.getId());
        int level = getPowerLevel(powerPillar);

        if (!isPowerFull(powerPillar)) {
            mapper.setIsPize(false);
            mapper.setTip("你的能量柱能量未满哦！");
            return;
        }

        int fullPowerNum = getFullPowerNumber(powerPillar);
        long fragmentNum = this.getFragmentPrizeNum(powerPillar);
        List<TryPowerPizeMapper.PrizeEntity> prizeList = new ArrayList<>();
        mapper.setIsPize(true);
        String comment = "能量柱奖励";
        if (user.isStudent()) {
            comment = RewardConstants.STUSENT_REWARD_NAME + comment;
        } else {
            comment = RewardConstants.TEACHER_REWARD_NAME + comment;
        }
        debrisService.changeDebris(user.getId(), DebrisType.TOBY.getType(), fragmentNum, comment);

        TryPowerPizeMapper.PrizeEntity fragmentEntity = mapper.new PrizeEntity();
        fragmentEntity.setType(PowerPrize.PrizeType.FRAGMENT.intValue());
        fragmentEntity.setName("碎片");
        fragmentEntity.setPictuerUrl("");
        fragmentEntity.setFragmentNum(fragmentNum);
        prizeList.add(fragmentEntity);

        PowerPrize fragmentPrize = new PowerPrize();
        fragmentPrize.setPrize(fragmentNum * 1l);
        fragmentPrize.setType(PowerPrize.PrizeType.FRAGMENT.intValue());
        fragmentPrize.setName("碎片");
        //增加碎片
        addPowerPrizeRecord(user.getId(), fragmentPrize);
        //重置能量柱
        resetPowerPillar(user.getId(), fullPowerNum);

        boolean isRealGoodsOfflineCity = isRealGoodsOfflineCity((StudentDetail)user);
        boolean isRealGoodsOfflineTerminal = isRealGoodsOfflineTerminal((StudentDetail)user);
        if (!isRealGoodsOfflineCity && !isRealGoodsOfflineTerminal) {
            PowerPrize powerPrize = this.extractPrize(powerPrizeDao.loadByLevel(level));
            if (powerPrize != null) {
                MapMessage message = internalRewardOrderService.createRewardOrder(user, powerPrize.getPrize(), true, RewardOrder.Source.power_pillar);
                if (message.isSuccess()) {
                    addPowerPrizeRecord(user.getId(), powerPrize);
                    TryPowerPizeMapper.PrizeEntity realGoods = mapper.new PrizeEntity();
                    realGoods.setName(powerPrize.getName());
                    realGoods.setType(PowerPrize.PrizeType.REAL_GOODS.intValue());
                    realGoods.setPictuerUrl(powerPrize.getPicterUrl());
                    prizeList.add(realGoods);
                }
            }
        }
        mapper.setPrizeList(prizeList);
    }

    private long getFragmentPrizeNum(PowerPillar powerPillar) {
        if (powerPillar == null || powerPillar.getResetNumber()== null || powerPillar.getResetNumber() == 0) {
            return FIRST_POWER_PRIZE_NUM;
        } else if (powerPillar.getResetNumber() == 1) {
            return SECOND_POWER_PRIZE_NUM;
        } else {
            return ORTHER_POWER_PRIZE_NUM;
        }
    }

    // 下线城市的灰度地区
    public boolean isRealGoodsOfflineCity(StudentDetail student) {
        return grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(student, "Reward", "OfflineShiWu", true);
    }

    //五月份之后毕业班能量柱奖励不发实物
    private boolean isRealGoodsOfflineTerminal(StudentDetail student) {
        Clazz clazz = student.getClazz();
        // fixme 毕业班德能兑换实物的月份判断 InternalRewardTipService，在另外一个分支和这个分支同时进行，需要到上线后才能和过来
        String month = DateUtils.dateToString(new Date(), "MM-dd");
        if (month.compareTo("05-01") > 0 && month.compareTo("08-09") < 0 && clazz != null && clazz.getClazzLevel() != null) {
            if (clazz.getClazzLevel().getLevel() == ClazzLevel.FIFTH_GRADE.getLevel() && clazz.getEduSystem().equals(EduSystemType.P5)
                    || clazz.getClazzLevel().getLevel() == ClazzLevel.SIXTH_GRADE.getLevel() && clazz.getEduSystem().equals(EduSystemType.P6)
                    || clazz.getClazzLevel().getLevel() == ClazzLevel.NINTH_GRADE.getLevel()
                    || clazz.isTerminalClazz()) {
                return true;
            }
        }
        return false;
    }

    private synchronized PowerPrize extractPrize(List<PowerPrize> powerPrizeList) {
        if (CollectionUtils.isEmpty(powerPrizeList)) {
            return null;
        }
        PowerPrize powerPrize = null;
        Set<PowerPrize> powerPrizeSet = new HashSet<>(powerPrizeList);
        for (PowerPrize entity : powerPrizeSet) {
            if (entity.getStock() > 0) {
                powerPrize = entity;
                break;
            }
        }
        if (powerPrize != null) {
            powerPrizeDao.updateStock(powerPrize.getId());
        } else {//常规奖励池没有物品了，则在备用物品里得到奖励
            for (PowerPrize entity : powerPrizeSet) {
                if (entity.getIsReserve()) {
                    powerPrize = entity;
                    break;
                }
            }
        }
        return powerPrize;
    }


    private int getPowerLevel(PowerPillar powerPillar) {
        int level;
        if (powerPillar == null) {
            return PowerPrize.PowerLevel.ONE.intValue();
        }
        int resetNumber = (powerPillar==null||powerPillar.getResetNumber()==null) ? 0:powerPillar.getResetNumber();
        if (resetNumber == 1) {
            level = PowerPrize.PowerLevel.TWO.intValue();
        }
        else if (resetNumber >= 2) {
            level = PowerPrize.PowerLevel.THREE.intValue();
        } else {
            level = PowerPrize.PowerLevel.ONE.intValue();
        }
        return level;
    }


    public boolean isPowerFull(PowerPillar powerPillar) {
        if (powerPillar==null || powerPillar.getResetNumber()==null ) {
            return  false;
        }
        boolean result = false;
        if (powerPillar.getResetNumber()==0) {
            if (powerPillar.getPowerPillar()!=null && powerPillar.getPowerPillar()>=FIRST_POWER_FULL_NUM) {
                result = true;
            }
        } else if ( powerPillar.getResetNumber()==1) {
            if (powerPillar.getPowerPillar()!=null && powerPillar.getPowerPillar()>=SECOND_POWER_FULL_NUM) {
                result = true;
            }
        } else {
            if (powerPillar.getPowerPillar()!=null && powerPillar.getPowerPillar()>=ORTHER_POWER_FULL_NUM) {
                result = true;
            }
        }
        return result;
    }

    public int getFullPowerNumber(PowerPillar powerPillar) {
        int number = ORTHER_POWER_FULL_NUM;
        if (powerPillar == null || powerPillar.getResetNumber() == null || powerPillar.getResetNumber() == 0) {
            number = FIRST_POWER_FULL_NUM;
        } else if (powerPillar.getResetNumber() == 1) {
            number = SECOND_POWER_FULL_NUM;
        }
        return number;
    }

    public void addPowerPrizeRecord(long userId, PowerPrize powerPrize) {
        if (powerPrize == null) {
            return;
        }
        PowerPrizeRecord record = new PowerPrizeRecord();
        record.setPictuerUrl(powerPrize.getPicterUrl());
        record.setPrizeId(powerPrize.getId());
        record.setPrizeType(powerPrize.getType());
        if (record.getPrizeType() == PowerPrize.PrizeType.FRAGMENT.intValue()) {
            record.setFragmentNum(powerPrize.getPrize().intValue());
        }
        record.setPrizeName(powerPrize.getName());
        record.setUserId(userId);
        powerPrizeRecordDao.insert(record);
    }

}
