package com.voxlearning.utopia.mizar.service.basic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.mizar.audit.MizarAuditEntityFactory;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.entity.GoodsQueryContext;
import com.voxlearning.utopia.mizar.entity.MizarQueryContext;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 品牌管理相关Service.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
@Named
public class MizarGoodsManager extends AbstractMizarService implements MizarManager<MizarShopGoods> {

    // 用于比较两个实体的字段
    private static final List<String> COMPARE_FIELDS = Arrays.asList(
            "shopId"           // 机构ID
            , "goodsName"      // 课程名称
            , "title"          // 课程标题
            , "desc"           // 课程简介
            , "goodsHours"     // 课时
            , "duration"       // 时长
            , "goodsTime"      // 上课时间
            , "target"         // 年龄段
            , "category"       // 课程分类
            , "audition"       // 试听
            , "price"          // 课程现价
            , "bannerPhoto"    // banner图片
            , "tags"           // 课程标签
            , "detail"         // 课程详情图片
            , "appointGift"    // 预约礼
            , "welcomeGift"    // 到店礼
            , "redirectUrl"    // 跳转链接
            , "recommended"    // 是否推荐到首页
            , "topImage"       // 顶部图
            , "smsMessage"      // 发送短信
            , "totalLimit"      // 课程总量
            , "dayLimit"        // 天限量
            , "sellCount"       // 总已售
            , "daySellCount"    // 天已售
            , "productId"       // 一起学商品id
            , "requireAddress"  // 是否需要收货地址 2 不需要 1需要
            , "buttonColor"     // 按钮颜色
            , "buttonText"      // 按钮文案
            , "buttonTextColor" // 按钮颜色
            , "successText"     // 报名成功文案
            , "offlineText"     // 下线文案
            , "dealSuccess"     // 成功交易
            , "inputBGColor"    // 输入框区域颜色
            , "clazzLevel"      // 年级
            , "requireSchool"   // 学校
            , "schoolAreas"   // 校区
            , "requireStudentName"   // 是否需要学生姓名
            , "requireRegion"   // 是否需要地区
    );


    public Page<MizarShopGoods> page(MizarAuthUser user, MizarQueryContext context) {
        if (user == null || context == null) {
            return emptyPage();
        }
        GoodsQueryContext queryContext = (GoodsQueryContext) context;
        String token = queryContext.getToken();
        String shopToken = queryContext.getShopToken();

        Map<String, MizarShop> shopMap = new HashMap<>();
        List<MizarShopGoods> goodsList = new LinkedList<>();
        if (user.isShopOwner() || user.isBD()) {
            // 机构业主 和 BD 只能看到自己管辖的机构
            shopMap = mizarLoaderClient.loadShopByIds(user.getShopList());
            // 过滤机构
            List<MizarShop> filterShop = shopMap.values().stream()
                    .filter(p -> StringUtils.isBlank(shopToken) || shopToken.equals(p.getId()) || p.getFullName().contains(shopToken))
                    .collect(Collectors.toList());

            shopMap = filterShop.stream().collect(Collectors.toMap(MizarShop::getId, Function.identity()));

            // 课程信息
            goodsList = mizarLoaderClient.loadShopGoodsByShop(shopMap.keySet()).values()
                    .stream()
                    .flatMap(List::stream)
                    .filter(p -> StringUtils.isBlank(token) || token.equals(p.getId()) || p.getGoodsName().contains(token))
                    .collect(Collectors.toList());
        } else {
            // 优先按照课程信息查询
            if (StringUtils.isNotBlank(token)) {
                if (ObjectId.isValid(token)) {
                    MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(token);
                    if (goods != null) goodsList.add(goods);
                } else {
                    goodsList = mizarLoaderClient.loadShopGoodsByPage(context.getPageable(), token).getContent();
                }
                List<String> shopIds = goodsList.stream().map(MizarShopGoods::getShopId).collect(Collectors.toList());
                shopMap = mizarLoaderClient.loadShopByIds(shopIds);
                // 过滤掉机构信息
                if (StringUtils.isNotBlank(shopToken)) {
                    if (ObjectId.isValid(shopToken)) {
                        shopMap.remove(shopToken);
                    } else {
                        shopMap = shopMap.values().stream()
                                .filter(s -> StringUtils.contains(s.getFullName(), shopToken))
                                .collect(Collectors.toMap(MizarShop::getId, Function.identity()));
                    }
                }
            } else if (StringUtils.isNotBlank(shopToken)) {
                if (ObjectId.isValid(shopToken)) {
                    MizarShop shop = mizarLoaderClient.loadShopById(shopToken);
                    if (shop != null) shopMap.put(shop.getId(), shop);
                } else {
                    shopMap = mizarLoaderClient.loadShopByPage(context.getPageable(), shopToken).getContent()
                            .stream()
                            .collect(Collectors.toMap(MizarShop::getId, Function.identity(), (s1, s2) -> {
                                logger.warn("Duplicate Shop Id found: {}", s1.getId());
                                return s1.getUpdateAt().after(s2.getUpdateAt()) ? s1 : s2;
                            }));
                }
                goodsList = mizarLoaderClient.loadShopGoodsByShop(shopMap.keySet()).values()
                        .stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }
        }
        String status = queryContext.getStatus();
        // 过滤状态
        if (CollectionUtils.isNotEmpty(goodsList) && StringUtils.isNotBlank(status)) {
            MizarGoodsStatus goodsStatus = MizarGoodsStatus.parse(status);
            if (goodsStatus != null)
                goodsList = goodsList.stream().filter(goods -> goods.getStatus() != null && goodsStatus == goods.getStatus()).collect(Collectors.toList());
        }

        // 在课程页面过滤掉亲子活动
        goodsList = goodsList.stream()
                .filter(p -> !p.isFamilyActivity() && !p.isUSTalkActivity())
                .collect(Collectors.toList());
        queryContext.setShopMap(shopMap);
        return PageableUtils.listToPage(goodsList, context.getPageable());
    }

    public List<Integer> getCreateAuthRoles() {
        return Arrays.asList(
                MizarUserRoleType.Operator.getId(),
                MizarUserRoleType.BusinessDevelopment.getId(),
                MizarUserRoleType.ShopOwner.getId()
        );
    }

    public List<Integer> getModifyAuthRoles() {
        return Arrays.asList(
                MizarUserRoleType.Operator.getId(),
                MizarUserRoleType.BusinessDevelopment.getId(),
                MizarUserRoleType.ShopOwner.getId()
        );
    }

    /**
     * 新增课程操作
     * 1. 会先生成一个课程实体，状态待审核
     * 2. 生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage create(MizarShopGoods goods, MizarAuthUser user) {
        // 检查参数以及权限
        MapMessage checkMsg = beforeCreate(goods, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        // 默认认为调用这个方法的都是新增课程，状态待审核
        goods.setId(null);
        goods.setStatus(MizarGoodsStatus.PENDING);
        // 校验课程的各项内容
        MapMessage validMsg = validate(goods);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 新建的时候先生成实体
        MapMessage savMsg = mizarServiceClient.saveMizarShopGoods(goods);
        if (savMsg.isSuccess()) {
            String goodsId = SafeConverter.toString(savMsg.get("gid"));
            goods.setId(goodsId);
        }
        // 可以发起变更申请了
        MizarEntityChangeRecord record = MizarAuditEntityFactory.newGoodsInstance(user, goods, goods.getGoodsName());
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(record);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
        // 发送消息
        return MapMessage.successMessage();
    }

    /**
     * 编辑课程操作
     * 1. 不直接应用变更
     * 2. 如果存在一条相同记录的变更，则提示不能发起；否则生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage modify(MizarShopGoods goods, MizarAuthUser user) {
        // 检查参数以及权限
            MapMessage checkMsg = beforeModify(goods, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        String goodsId = goods.getId();
        // 校验品牌的各项内容
        MapMessage validMsg = validate(goods);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        MizarShopGoods oldGoods = mizarLoaderClient.loadShopGoodsById(goodsId);
        if (oldGoods == null) {
            return MapMessage.errorMessage("无效的课程ID：" + goodsId);
        }
//        if (goods.getStatus() != null && MizarGoodsStatus.ONLINE == goods.getStatus()) {
//            return MapMessage.errorMessage("该课程已经上线，不允许编辑");
//        }
        // 查看字段是否有变更, 没有变更的话直接返回 success
        boolean changed = BeanUtils.getInstance().beanEquals(oldGoods, goods, COMPARE_FIELDS);
        if (changed) {
            return MapMessage.successMessage();
        }
        // 先判断一下是否有针对这个记录的更新
        List<MizarEntityChangeRecord> records = mizarChangeRecordLoaderClient.loadByTargetAndType(goodsId, MizarEntityType.GOODS.getCode());
        if (records != null && records.stream().anyMatch(record -> MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()))) {
            return MapMessage.errorMessage("已有一条变更记录等待审核，暂时不能提交新的变更!");
        }
        MizarShopGoods diff = BeanUtils.getInstance().beanDiff(oldGoods, goods);
        diff.setStatus(oldGoods.getStatus());
        // 可以发起新的变更申请了
        MizarEntityChangeRecord newRecord = MizarAuditEntityFactory.newGoodsInstance(user, diff, goods.getGoodsName());
        newRecord.setTargetId(goodsId);
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(newRecord);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
        return MapMessage.successMessage();
    }

    public MapMessage validate(MizarShopGoods goods) {
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
        if (StringUtils.isBlank(goods.getGoodsName())) {
            validInfo.append("【课程名称】不能为空!").append("<br />");
        }
        if (StringUtils.isBlank(goods.getTitle())) {
            validInfo.append("【课程标题】不能为空!").append("<br />");
        }
        if (StringUtils.isBlank(goods.getDesc())) {
            validInfo.append("【课程简介】不能为空!").append("<br />");
        }
        if (CollectionUtils.isNotEmpty(goods.getDetail()) && goods.getDetail().size() > 10) {
            validInfo.append("【课程图片】不能超过10张!").append("<br />");
        }
        if (CollectionUtils.isNotEmpty(goods.getBannerPhoto()) && goods.getBannerPhoto().size() > 10) {
            validInfo.append("【课程头图】不能超过10张!").append("<br />");
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

}
