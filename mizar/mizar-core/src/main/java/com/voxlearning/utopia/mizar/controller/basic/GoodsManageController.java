package com.voxlearning.utopia.mizar.controller.basic;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarGoodsController;
import com.voxlearning.utopia.mizar.entity.GoodsQueryContext;
import com.voxlearning.utopia.mizar.service.basic.MizarGoodsManager;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.client.AsyncMizarCacheServiceClient;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 服务管理-课程管理 Controller
 * Created by yuechen.wang on 16/9/6.
 */
@Controller
@RequestMapping(value = "/basic/goods")
public class GoodsManageController extends AbstractMizarGoodsController {

    @Inject private AsyncMizarCacheServiceClient asyncMizarCacheServiceClient;

    @Inject private MizarGoodsManager mizarGoodsManager;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage")
    public String index(Model model) {
        String token = getRequestString("token");
        String status = getRequestString("status");
        String shopToken = getRequestString("shopToken");
        Integer pageNum = Integer.max(1, getRequestInt("page", 1)); // 当前页

        Pageable pageable = new PageRequest(pageNum - 1, 200);

        // 初始化参数
        GoodsQueryContext queryContext = new GoodsQueryContext(pageable);
        queryContext.setToken(token);
        queryContext.setStatus(status);
        queryContext.setShopToken(shopToken);

        Page<MizarShopGoods> goodsPage = mizarGoodsManager.page(getCurrentUser(), queryContext);

        model.addAttribute("goodsList", splitList(mapShopGoods(queryContext.getShopMap(), goodsPage.getContent()), 10));
        model.addAllAttributes(queryContext.toParamMap());
        model.addAttribute("page", pageNum);
        return "basic/goods/goodslist";
    }

    // 选择生效门店
    @RequestMapping(value = "chooseshop.vpage", method = RequestMethod.GET)
    public String chooseShop(Model model) {
        // 加载出之前缓存的门店信息
        String shopObject = SafeConverter.toString(asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                .MizarUserSessionManager_getUserSessionAttribute(currentUserId(), "SelectedShop")
                .getUninterruptibly(), "");
        Set<String> shopIds = Stream.of(shopObject.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(shopIds)) {
            List<Map<String, Object>> shopList = mizarLoaderClient.loadShopByIds(shopIds).values()
                    .stream()
                    .map(MizarShop::simpleInfo)
                    .collect(Collectors.toList());
            model.addAttribute("shopList", shopList);
        }
        return "basic/goods/chooseshop";
    }

    @RequestMapping(value = "searchshop.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchShop() {
        String token = getRequestString("shopToken");
        String selected = getRequestString("shopList");
        Set<String> selectedShop = Stream.of(selected.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        try {
            List<MizarShop> shopList = new ArrayList<>();
            MizarAuthUser curUser = getCurrentUser();
            if (curUser.isBD() || curUser.isShopOwner()) {
                Map<String, MizarShop> userShops = mizarLoaderClient.loadShopByIds(currentUserShop());
                shopList = userShops.values().stream()
                        .filter(p -> StringUtils.isBlank(token) || token.equals(p.getId()) || p.getFullName().contains(token))
                        .collect(Collectors.toList());

            } else {
                if (StringUtils.isBlank(token)) {
                    return MapMessage.errorMessage("查询条件不能为空");
                }
                if (ObjectId.isValid(token)) {
                    MizarShop shop = mizarLoaderClient.loadShopById(token);
                    if (shop != null) shopList.add(shop);
                } else {
                    Pageable page = new PageRequest(0, 40);
                    shopList = mizarLoaderClient.loadShopByPage(page, token).getContent();
                }
            }
            List<Map<String, Object>> result = new ArrayList<>();
            shopList.forEach(s -> {
                Map<String, Object> info = new HashMap<>();
                info.putAll(s.simpleInfo());
                info.put("selected", selectedShop.contains(s.getId()));
                result.add(info);
            });
            return MapMessage.successMessage().add("shopList", result);
        } catch (Exception ex) {
            logger.error("Search Shop Failed, token={}", token, ex);
            return MapMessage.errorMessage("查询失败：" + ex.getMessage());
        }
    }

    // 缓存选择门店
    @RequestMapping(value = "shopselected.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveShopSession() {
        String shopList = getRequestString("shopList");
        try {
            asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_addUserSessionAttribute(currentUserId(), "SelectedShop", shopList)
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    // 课程编辑详情页
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        String goodsId = getRequestString("gid");
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
        if (StringUtils.isNotBlank(goodsId) && goods == null) {
            return "redirect: /basic/goods/index.vpage";
        }
        // 校验GoodsId权限
        boolean isNew = goods == null;
        Set<String> shopIds;
        if (isNew) {
            String shopObject = SafeConverter.toString(asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_getUserSessionAttribute(currentUserId(), "SelectedShop")
                    .getUninterruptibly(), "");
            shopIds = Stream.of(shopObject.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            // 查看缓存中的课程信息
            Object goodsBuffer = asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_getUserSessionAttribute(currentUserId(), "GoodsBuffer")
                    .getUninterruptibly();
            if (goodsBuffer instanceof MizarShopGoods) {
                goods = (MizarShopGoods) goodsBuffer;
            }
        } else {
            shopIds = Collections.singleton(goods.getShopId());
            goods.setRequireAddress(goods.featureRequireAddress());
            goods.setDealSuccess(goods.featureDealSuccess());
            goods.setRequireSchool(goods.featureRequireSchool());
            goods.setRequireStudentName(goods.featureRequireStudentName());
            goods.setRequireRegion(goods.featureRequireRegion());
            // 编辑模式清空缓存
            asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_removeUserSession(currentUserId())
                    .awaitUninterruptibly();
        }
        List<Map<String, Object>> shopInfo = mizarLoaderClient.loadShopByIds(shopIds).values()
                .stream().map(MizarShop::simpleInfo).collect(Collectors.toList());

        model.addAttribute("isNew", isNew);
        model.addAttribute("goods", goods);
        model.addAttribute("shopInfo", shopInfo);
        if (isNew || getRequestBool("edit")) {
            return "basic/goods/goodsdetail";
        }
        return "basic/goods/goodsview";
    }

    // 缓存编辑信息
    @RequestMapping(value = "goodsbuffer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveGoodsSession() {
        try {
            MizarShopGoods goods = getRequestMizarGoods();
            asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_addUserSessionAttribute(currentUserId(), "GoodsBuffer", goods)
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    // 放弃编辑时清除缓存
    @RequestMapping(value = "abandonedit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage abandonEdit() {
        try {
            asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_removeUserSession(currentUserId())
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "addgoods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addGoods() {
        Set<String> shopSet = new HashSet<>(requestStringList("sid"));
        if (CollectionUtils.isEmpty(shopSet)) {
            return MapMessage.errorMessage("请先选择【生效门店】!");
        }
        try {
            // 获取实体
            MizarShopGoods goods = getRequestMizarGoods();
            goods.setRecommended(getRequestBool("recommended"));
            goods.setRedirectUrl(getRequestString("redirectUrl"));
            for (String sid : shopSet) {
                MizarShopGoods temp = goods.clone();
                temp.setShopId(sid);
                MapMessage mapMessage = mizarGoodsManager.create(temp, getCurrentUser());
                if (!mapMessage.isSuccess()) {
                    return MapMessage.errorMessage(mapMessage.getInfo());
                }
            }
            // 保存成功之后清除缓存
            asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .MizarUserSessionManager_removeUserSession(currentUserId())
                    .awaitUninterruptibly();
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("新增课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "editgoods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editGoods() {
        String goodsId = getRequestString("gid");
        try {
            MizarShopGoods goods = getRequestMizarGoods();
            goods.setRecommended(getRequestBool("recommended"));
            goods.setRedirectUrl(getRequestString("redirectUrl"));
            goods.setShopId(getRequestString("sid"));
            goods.setId(goodsId);
            MapMessage message = mizarGoodsManager.modify(goods, getCurrentUser());
            if (message.isSuccess()) {
                // 清除缓存
                asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                        .MizarUserSessionManager_removeUserSession(currentUserId())
                        .awaitUninterruptibly();
            }
            return message;
        } catch (Exception ex) {
            logger.error("Save Mizar goods failed.", ex);
            return MapMessage.errorMessage("编辑课程保存失败：" + ex.getMessage());
        }
    }

    // 变更上/下线状态
    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeGoodsStatus() {
        if (!getCurrentUser().isOperator()) {
            return MapMessage.errorMessage("您没有操作权限");
        }
        String goodsId = getRequestString("gid");
        String status = getRequestString("status");
        try {
            MizarGoodsStatus goodsStatus = MizarGoodsStatus.parse(status);
            MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
            if (goodsStatus == null || goods == null) {
                return MapMessage.errorMessage("无效的参数");
            }
            //  状态已经变更的话，直接返回
            if (goods.getStatus() != null && goodsStatus == goods.getStatus()) {
                return MapMessage.successMessage();
            }
            goods.setStatus(goodsStatus);
            return mizarServiceClient.saveMizarShopGoods(goods);
        } catch (Exception ex) {
            logger.error("Failed change status, gid={}, status={}", goodsId, status, ex);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    private List<Map<String, Object>> mapShopGoods(Map<String, MizarShop> shopMap,
                                                   List<MizarShopGoods> goodsList) {
        if (MapUtils.isEmpty(shopMap) || CollectionUtils.isEmpty(goodsList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        goodsList.forEach(record -> {
            MizarShop shop = shopMap.get(record.getShopId());
            Map<String, Object> info = new HashMap<>();
            info.put("shopName", shop == null ? "" : shop.getFullName());
            info.put("shopId", record.getShopId());
            info.put("goodsId", record.getId()); // 课程ID
            info.put("goodsName", record.getGoodsName()); // 课程名称
            info.put("price", record.getPrice()); // 课程价格
            info.put("status", record.getStatus() == null ? "离线" : record.getStatus().getDesc());// 课程状态
            result.add(info);
        });
        result.sort((r1, r2) -> {
            String name1 = SafeConverter.toString(r1.get("shopName"));
            String name2 = SafeConverter.toString(r2.get("shopName"));
            return name2.compareTo(name1);
        });
        return result;
    }

}
