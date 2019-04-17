package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.entity.AwardContext;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.campaign.api.CampaignLoader;
import com.voxlearning.utopia.service.campaign.api.constant.AwardType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignLotteryHelper;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Xiaochao.Wei
 * @since 2018/1/29
 */
@Named
public class StudentAppLotteryWrapper extends AbstractCampaignWrapper {

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;
    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject private RewardServiceClient rewardServiceClient;
    @Inject private RewardLoaderClient rewardLoaderClient;
    @Inject private CampaignLoader campaignLoader;

    private final static String chanceKey = "STUDENT_APP_LOTTERY_FREE_CHANCE";
    private final static String bigLotteryWeekCountKey = "STUDENT_APP_BIG_LOTTERY_WEEK_COUNT";
    private static int REDUCE_INTEGER = 5;//扣除的学豆

    @Override
    public MapMessage doLottery(CampaignType campaignType, User user, LotteryClientType clientType) {
        Map<String, Object> result = new HashMap<>();

        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("对不起，您不能参与该活动");
        }

        try {
            int freeChance = getLotteryFreeChance(user.getId());
            if (freeChance <= 0) {
                return MapMessage.errorMessage("今天抽奖次数已用完，明天再来试试吧");
            }

            //扣除学豆
            UserIntegral integral = studentLoaderClient.loadStudentDetail(user.getId()).getUserIntegral();
            int reduceIntegral = freeChance > 1 ? REDUCE_INTEGER : REDUCE_INTEGER * 2;
            if (integral == null || integral.getUsable() < reduceIntegral) {
                return MapMessage.errorMessage("学豆不足，不能参与抽奖哦");
            }

            IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.PRIMARY_STUDENT_APP_LOTTERY, -reduceIntegral);
            integralHistory.setComment("学生APP抽奖消耗");
            MapMessage msg = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
            if (!msg.isSuccess()) {
                return MapMessage.errorMessage("扣减学豆失败");
            }

            int campaignId = campaignType.getId();
            CampaignLottery lotteryResult;
            // 未认证的话，暂时先给倒数第二个奖品
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail.isPrimaryStudent() && !userAuthQueryServiceClient.isAuthedStudent(user.getId(), SchoolLevel.JUNIOR)) {
                // 未认证的话，从虚拟里面的奖品抽
                lotteryResult = drawVirtual(campaignId);
            } else {
                //抽奖
                lotteryResult = drawLottery(campaignType.getId(), campaignLottery -> Optional.ofNullable(JsonUtils.fromJsonToList(campaignLottery.getAwardContent(), Map.class))
                        .orElse(Collections.emptyList())
                        .stream()
                        .anyMatch(c -> Objects.equals(c.get("disable"), 0)));
                if (lotteryResult == null) {
                    IntegralHistory integralHistory1 = new IntegralHistory(user.getId(), IntegralType.PRIMARY_STUDENT_APP_LOTTERY, reduceIntegral);
                    integralHistory1.setComment("抽奖失败返还");
                    userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory1);
                    return MapMessage.errorMessage("抽奖失败,请稍候再试");
                }
                // 如果中了超级大奖
                else if (CampaignLotteryHelper.isSuperBig(lotteryResult)) {
                    String response = campaignCacheSystem.CBS.persistence.load(bigLotteryWeekCountKey);
                    if (StringUtils.isNotBlank(response)) {
                        // 本周已经抽出kindle,其余中kindle的将学豆
                        // sendBeans(result, user.getId(), MapUtils.m("num", 10,"awardId", 7));
                        // hadSend = true;

                        // 讲奖项修改为两学豆那个
                        // 又改为抽虚拟
                        lotteryResult = drawVirtual(campaignId);
                    } else {
                        campaignCacheSystem.CBS.persistence.incr(bigLotteryWeekCountKey, 1, 1, DateUtils.getCurrentToWeekEndSecond());
                    }
                }
            }
            String awardContent = lotteryResult.getAwardContent();
            if (StringUtils.isNotEmpty(awardContent)) {
                List<AwardContext> awardContexts = JSON.parseArray(awardContent, AwardContext.class);
                AwardContext awardContext = awardContexts.get(0);
                AwardContext.Type type = awardContext.getType();
                if (type == null) {
                    type = AwardContext.Type.THANKS;
                }
                result.put("type", type);
            }

//            Map<String, Object> jsonObject = JsonUtils.fromJson(lotteryResult.getAwardContent());
            List<Map> jsonArray = JsonUtils.fromJsonToList(lotteryResult.getAwardContent(), Map.class);
            for (Map<String, Object> jsonObject : jsonArray) {
                //抽中实物向订单推数据
                if (AwardType.INTEGRAL.name().equals(jsonObject.get("type"))) {//中学豆
                    sendBeans(result, user.getId(), jsonObject);
                } else if (AwardType.HEAD_WEAR.name().equals(jsonObject.get("type"))) {
                    sendHeadWear(user.getId());
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", lotteryResult.getAwardName());
                    result.put("isVirtual", true);
                } else if (AwardType.RC_PRODUCT.name().equals(jsonObject.get("type"))) {
                    //调用订单接口发送实物奖励
                    rewardServiceClient.createRewardOrderFree(user.getId(), ConversionUtils.toLong(jsonObject.get("awardId")), ConversionUtils.toInt(jsonObject.get("num")));
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", lotteryResult.getAwardName());
                    result.put("isVirtual", false);
                } else {
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", lotteryResult.getAwardName());
                }
            }

            if (CampaignLotteryHelper.isBig(lotteryResult)) {
                addRecentCampaignLotteryResultBig(user, lotteryResult, campaignType.getId(), DateUtils.getCurrentToWeekEndSecond());
                // 记录大奖记录
                CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                bigHistory.setUserId(user.getId());
                bigHistory.setAwardId(lotteryResult.getAwardId());
                bigHistory.setCampaignId(campaignType.getId());
                campaignService.$insertCampaignLotteryBigHistory(bigHistory);
            }

            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(lotteryResult.getAwardId());
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);

            //抽奖机会减1
            String key = CacheKeyGenerator.generateCacheKey(chanceKey, null, new Object[]{user.getId()});
            campaignCacheSystem.CBS.persistence.decr(key, 1, 0, DateUtils.getCurrentToDayEndSecond());

            return MapMessage.successMessage().add("result", result);
        } catch (Exception e) {
            logger.error("STUDENT APP LOTTERY ERROR:", e);
            return MapMessage.errorMessage("抽奖失败");
        }

    }

    /**
     * 从虚拟奖品的池子中，随机抽个出来
     *
     * @param campaignId
     * @return
     */
    private CampaignLottery drawVirtual(int campaignId) {
        CampaignLottery lotteryResult = drawLottery(campaignId, cl -> {
            return Optional.ofNullable(JsonUtils.fromJsonToList(cl.getAwardContent(), Map.class))
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(c -> (!Objects.equals(c.get("type"), "RC_PRODUCT"))
                            && Objects.equals(c.get("disable"), 0));
        });

        // 兼容下找不到数据的情况
        if (lotteryResult == null) {
            logError("奖品中心虚拟奖品抽奖异常，未抽中奖品!");
            lotteryResult = pickWorthlessOption(campaignId);
        }

        return lotteryResult;
    }

    private CampaignLottery pickWorthlessOption(Integer campaignId) {
        return campaignLoader.findCampaignLottery(campaignId, 6);
    }

    private void sendBeans(Map<String, Object> result,
                           Long userId,
                           Map<String, Object> awardMap) {
        if (MapUtils.isEmpty(awardMap)) {
            logError("学豆奖励配置丢失!");
            return;
        }

        Integer awardId = MapUtils.getInteger(awardMap, "awardId");
        if (awardId == null) {
            logError("学豆奖励awardId配置丢失!");
            return;
        }

        Integer num = MapUtils.getInteger(awardMap, "num");
        if (num == null || num <= 0) {
            logError("学豆奖励数量配置错误!awardId:{}", awardId);
            return;
        }

        IntegralHistory integralHistory = new IntegralHistory(userId, IntegralType.PRIMARY_STUDENT_APP_LOTTERY, num);
        integralHistory.setComment("学生APP抽奖奖励");

        MapMessage resultMsg = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
        if (!resultMsg.isSuccess()) {
            logError("增加学豆失败!detail:" + resultMsg.getInfo());
            return;
        }

        result.put("awardId", awardId);
        result.put("awardName", num + "学豆");
        result.put("isVirtual", true);
    }

    private void logError(String msg, Object... args) {
        logger.error("奖品中心抽奖:" + msg, args);
    }

    private void sendHeadWear(Long userId) {
        // 获得所有在架上的头饰商品，随机抽中一个
        List<RewardProduct> headWearProducts = rewardLoaderClient.loadRewardPrivilegeProduct()
                .stream()
                .filter(p -> p.getOnlined())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(headWearProducts))
            return;

        Integer randomResult = RandomUtils.nextInt(headWearProducts.size());
        RewardProduct headWearProduct = headWearProducts.get(randomResult);
        if (headWearProduct == null)
            return;

        Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(headWearProduct.getRelateVirtualItemId());

        privilegeServiceClient.getPrivilegeService().postponedPrivilege(userId, privilege.getCode(), 15);
    }

    @Override
    public Long addLotteryChance(Long userId, int delta) {
        return null;
    }

    @Override
    public int getLotteryFreeChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(chanceKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject == null) {
            return 0;
        }
        String response = StringUtils.trim(cacheObject.getValue());
        if (StringUtils.isNotBlank(response)) {
            return NumberUtils.toInt(response);
        }
        //初始的抽奖次数
        int initialCount = 4;
        Boolean ret = campaignCacheSystem.CBS.persistence.add(key, DateUtils.getCurrentToDayEndSecond(), Integer.toString(initialCount));
        if (Boolean.TRUE.equals(ret)) {
            return initialCount;
        } else {
            logger.warn("Add '{}' failed, maybe someone has already add this value", key);
            return NumberUtils.toInt(StringUtils.trim(campaignCacheSystem.CBS.persistence.load(key)));
        }
    }

    @Override
    public MapMessage validateCampaignLottery(CampaignLottery campaignLottery) {
        try {
            Validate.notEmpty(campaignLottery.getAwardLevelName(), "奖项不能为空");
            Validate.notEmpty(campaignLottery.getAwardName(), "奖品不能为空");
            Validate.notNull(campaignLottery.getAwardRate(), "中奖率不能为空");

            String awardContent = campaignLottery.getAwardContent();
            Validate.notEmpty(awardContent, "奖品内容不能为空");

            // 数组有点多余, 历史遗留问题, 继续兼容吧
            try {
                JSON.parseArray(awardContent);
            } catch (Exception e) {
                return MapMessage.errorMessage("奖品内容不是 json 数组");
            }

            JSONArray jsonArray = JSONArray.parseArray(awardContent);
            Validate.isTrue(jsonArray.size() == 1, "数组中必须且只能有一个对象");
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            Integer awardId = jsonObject.getInteger("awardId");
            Validate.notNull(awardId, "奖品内容必须有 awardId");

            Integer num = jsonObject.getInteger("num");
            Validate.notNull(num, "奖品内容必须有 num");

            String type = jsonObject.getString("type");
            Validate.notNull(type, "奖品内容必须有 type");

            Integer disable = jsonObject.getInteger("disable");
            Validate.notNull(disable, "奖品内容必须有 disable");

            try {
                jsonObject.getBoolean("big");
            } catch (Exception e) {
                throw new IllegalArgumentException("big 必须是 true 或者 false");
            }
            Boolean superBigFlag = false;
            try {
                Boolean superBig = jsonObject.getBoolean("superBig");
                if (superBig != null) {
                    superBigFlag = superBig;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("superBig 必须是 true 或者 false");
            }

            /*String describe = jsonObject.getString("describe");
            Validate.notEmpty(describe, "奖品内容必须有 describe");*/

            String img = jsonObject.getString("img");
            Validate.notEmpty(img, "奖品内容必须有 img");

            //如果是新增并且新增的是超级大奖, 判断是否已存在超级大奖
            if (superBigFlag) {
                List<CampaignLottery> campaignLotteries = campaignLoader.findCampaignLotteries(campaignLottery.getCampaignId());
                long count = campaignLotteries.stream().filter(new SuperBigFilter(campaignLottery.getId())).count();
                if (count > 0) {
                    throw new IllegalArgumentException("已存在超级大奖");
                }
            }

            if (campaignLottery.getId() != null) {
                CampaignLottery oldLottery = campaignLoader.findCampaignLotterie(campaignLottery.getId());
                JSONObject oldObj = JSONObject.parseArray(oldLottery.getAwardContent()).getJSONObject(0);
                JSONObject newObj = JSONObject.parseArray(campaignLottery.getAwardContent()).getJSONObject(0);
                if (!Objects.equals(oldObj.getLong("awardId"), newObj.getLong("awardId"))) {
                    return MapMessage.errorMessage("保存失败, 不能修改奖品内容 json 中的 awardId");
                }
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            return MapMessage.errorMessage("校验发生未知错误");
        }

        return MapMessage.successMessage();
    }

    static class SuperBigFilter implements Predicate<CampaignLottery> {
        private Long campaignLotteryId;

        SuperBigFilter(Long id) {
            campaignLotteryId = id;
        }

        @Override
        public boolean test(CampaignLottery item) {
            try {
                if (item.getId().equals(campaignLotteryId)) {
                    return false;
                }
                String content = item.getAwardContent();
                if (StringUtils.isEmpty(content)) {
                    return false;
                }
                JSONArray array = JSON.parseArray(content);
                if (array.size() == 1) {
                    JSONObject object = array.getJSONObject(0);
                    Boolean itemSuperBig = object.getBoolean("superBig");
                    Integer itemDisable = object.getInteger("disable");
                    if (Objects.equals(0, itemDisable) && itemSuperBig != null && itemSuperBig) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    }
}
