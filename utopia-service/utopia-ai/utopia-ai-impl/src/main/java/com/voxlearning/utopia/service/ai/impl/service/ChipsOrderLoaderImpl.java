package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsOrderLoader;
import com.voxlearning.utopia.service.ai.data.ChipsOrderExtBO;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsGroupShoppingPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOrderExtDao;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsOrderLoader.class)
public class ChipsOrderLoaderImpl implements ChipsOrderLoader {

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    private static String USER_WECHAT_DEFAUAL_NAME = "薯条学员";

    private static String USER_WECHAT_DEFAUAL_ROLE_IMAGE = "http://cdn.17zuoye.com/fs-resource/5c6a53f0b43327e1ce9af229.png";

    @Override
    public ChipsOrderExtBO loadOrderExtInfo(String orderId) {
        ChipsUserOrderExt chipsUserOrderExt = chipsUserOrderExtDao.load(orderId);
        if (chipsUserOrderExt != null) {
            ChipsOrderExtBO chipsOrderExtBO = new ChipsOrderExtBO();
            chipsOrderExtBO.setOrderId(orderId);
            chipsOrderExtBO.setUserId(chipsUserOrderExt.getUserId());
            chipsOrderExtBO.setGroupCode(StringUtils.isNotBlank(chipsUserOrderExt.getNewGroupCode()) ? chipsUserOrderExt.getNewGroupCode() : chipsUserOrderExt.getGroupShoppingCode());
            if (StringUtils.isNotBlank(chipsUserOrderExt.getNewGroupCode())) {
                chipsOrderExtBO.setRepeatSponsor(true);
            } else if (chipsUserOrderExt.getSponsor() != null) {
                chipsOrderExtBO.setRepeatSponsor(false);
            }
            chipsOrderExtBO.setCreateDate(chipsUserOrderExt.getCreateDate());
            ChipsGroupShopping chipsGroupShopping = chipsGroupShoppingPersistence.loadByCode(chipsOrderExtBO.getGroupCode());
            chipsOrderExtBO.setGroupSuccess(chipsGroupShopping != null && chipsGroupShopping.getNumber() != null && chipsGroupShopping.getNumber() > 1);
            return chipsOrderExtBO;
        }
        return null;
    }

    @Override
    public MapMessage loadGroupShoppingList() {
        long now = System.currentTimeMillis();
        List<ChipsGroupShopping> groupList = chipsGroupShoppingPersistence.loadInGroupingRecord().stream()
                .filter(e -> (e.getCreateTime().getTime() + 24 * 60 * 60 * 1000L) > now)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupList)) {
            return MapMessage.successMessage().set("groupList", Collections.emptyList());
        }
        groupList = groupList.subList(0, Math.min(20, groupList.size()));

        Map<Long, ParentExtAttribute> userExtMap = parentLoaderClient.loadParentExtAttributes(groupList.stream().map(ChipsGroupShopping::getSponsor).collect(Collectors.toSet()));

        List<Map<String, Object>> resList = new ArrayList<>();

        groupList.forEach(e -> {
            ParentExtAttribute userExt = userExtMap.get(e.getSponsor());
            String userName = Optional.ofNullable(userExt).filter(ext -> StringUtils.isNotBlank(ext.getWechatNick())).map(ParentExtAttribute::getWechatNick).orElse(USER_WECHAT_DEFAUAL_NAME);
            String image = Optional.ofNullable(userExt).filter(ext -> StringUtils.isNotBlank(ext.getWechatImage())).map(ParentExtAttribute::getWechatImage).orElse(USER_WECHAT_DEFAUAL_ROLE_IMAGE);
            Map<String, Object> map = new HashMap<>();
            map.put("userName", userName);
            map.put("code", e.getCode());
            map.put("image", image);
            map.put("surplusTime", (e.getCreateTime().getTime() + 24 * 60 * 60 * 1000L - now) / 1000);
            resList.add(map);
        });

        return MapMessage.successMessage().set("groupList", resList);
    }

    @Override
    public MapMessage loadGroupSponsorInfo(String groupCode) {
        if (StringUtils.isBlank(groupCode)) {
            return MapMessage.errorMessage("参数为空");
        }
        ChipsGroupShopping chipsGroupShopping = chipsGroupShoppingPersistence.loadByCode(groupCode);
        if (chipsGroupShopping == null) {
            return MapMessage.errorMessage("拼团码不存在");
        }
        Long surTime = Optional.ofNullable(chipsGroupShopping).map(ChipsGroupShopping::getCreateTime)
                .map(e -> e.getTime() + 24 * 60 * 60 * 1000L - System.currentTimeMillis())
                .filter(e -> e > 0L)
                .map(e -> e / 1000L)
                .orElse(0L);
        ParentExtAttribute userExt = Optional.ofNullable(chipsGroupShopping)
                .map(ChipsGroupShopping::getSponsor)
                .map(parentLoaderClient::loadParentExtAttribute)
                .orElse(null);
        String userName = Optional.ofNullable(userExt)
                .filter(e -> StringUtils.isNotBlank(e.getWechatNick()))
                .map(ParentExtAttribute::getWechatNick)
                .orElse(USER_WECHAT_DEFAUAL_NAME);
        String image = Optional.ofNullable(userExt).filter(ext -> StringUtils.isNotBlank(ext.getWechatImage()))
                .map(ParentExtAttribute::getWechatImage).orElse(USER_WECHAT_DEFAUAL_ROLE_IMAGE);

        return MapMessage.successMessage().set("groupSuccess", chipsGroupShopping.getNumber() != null && chipsGroupShopping.getNumber() > 1).set("surplusTime", surTime).set("userName", userName).set("image", image);
    }

    @Override
    public List<ChipsGroupShopping> loadGroupShoppingListForCrm() {
        return chipsGroupShoppingPersistence.loadInGroupingRecord();
    }

}
