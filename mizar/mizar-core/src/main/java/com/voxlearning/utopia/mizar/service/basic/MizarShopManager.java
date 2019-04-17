package com.voxlearning.utopia.mizar.service.basic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.mizar.audit.MizarAuditEntityFactory;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.entity.MizarQueryContext;
import com.voxlearning.utopia.mizar.entity.ShopQueryContext;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarShopStatusType;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 品牌管理相关Service.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
@Named
public class MizarShopManager extends AbstractMizarService implements MizarManager<MizarShop> {

    // 用于比较两个实体的字段
    private static final List<String> COMPARE_FIELDS = Arrays.asList(
            "fullName"           // 机构全称
            , "shortName"        // 机构简称
            , "introduction"     // 机构介绍
            , "shopType"         // 机构类型
            , "regionCode"       // 所属地区
            , "tradeArea"        // 所属商圈
            , "address"          // 详细地址
            , "longitude"        // GPS经度
            , "latitude"         // GPS纬度
            , "contactPhone"     // 联系电话
            , "photo"            // 机构图片
            , "vip"              // 是否付费商家
            , "firstCategory"    // 一级分类
            , "secondCategory"   // 二级分类
            , "brandId"          // 所属品牌ID
            , "welcomeGift"      // 到店礼
            , "matchGrade"       // 适配年级,多个以逗号分隔
            , "cooperator"       // 是否合作机构
            , "cooperationLevel" // 合作等级分数
            , "adjustScore"      // 人工调整分数
            , "type"             // 是否线上机构
            , "faculty"          // 师资力量
    );

    public List<Integer> getCreateAuthRoles() {
        return Arrays.asList(
                MizarUserRoleType.Operator.getId(),
                MizarUserRoleType.BusinessDevelopment.getId()
        );
    }

    public List<Integer> getModifyAuthRoles() {
        return Arrays.asList(
                MizarUserRoleType.Operator.getId(),
                MizarUserRoleType.BusinessDevelopment.getId()
        );
    }

    public Page<MizarShop> page(MizarAuthUser user, MizarQueryContext context) {
        if (user == null || context == null) {
            return emptyPage();
        }
        ShopQueryContext queryContext = (ShopQueryContext) context;
        String shopName = queryContext.getShopName();
        Boolean cooperator = queryContext.getCooperator();
        Boolean vip = queryContext.getVip();

        if (user.isOperator()) {
            if (StringUtils.isNoneBlank(shopName) || cooperator != null || vip != null) {
                return mizarLoaderClient.loadAllShopByPage(context.getPageable(), shopName, vip, cooperator);
            }
        } else {
            List<String> shopIds = user.getShopList();
            Map<String, MizarShop> userShops = mizarLoaderClient.loadShopByIds(shopIds);
            List<MizarShop> shopList = userShops.values().stream()
                    .filter(p -> StringUtils.isBlank(shopName) || p.getFullName().contains(shopName))
                    .filter(p -> p.matchCooperator(cooperator))
                    .filter(p -> p.matchVip(vip))
                    .collect(Collectors.toList());
            return PageableUtils.listToPage(shopList, context.getPageable());
        }
        return emptyPage();
    }

    /**
     * 新增机构操作
     * 1. 生成机构实体
     * 2. 生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage create(MizarShop shop, MizarAuthUser user) {
        // 检查参数以及权限
        MapMessage checkMsg = beforeCreate(shop, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        // 默认认为调用这个方法的都是新增
        shop.setId(null);
        // 校验机构的各项内容
        MapMessage validMsg = validate(shop);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 新建的时候先生成实体
        MapMessage savMsg = mizarServiceClient.saveMizarShop(shop);
        if (savMsg.isSuccess()) {
            String shopId = SafeConverter.toString(savMsg.get("sid"));
            shop.setId(shopId);
            // BD新增机构之后，把这个机构给自己
            if (user.isBD()) {
                mizarUserServiceClient.saveUserShops(user.getUserId(), Collections.singletonList(shopId));
            }
        }
        // 可以发起变更申请了
        MizarEntityChangeRecord record = MizarAuditEntityFactory.newShopInstance(user, shop, shop.getFullName());
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(record);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
//        // 准备发送消息
//        Map<String, String> params = new HashMap<>();
//        params.put("user", SafeConverter.toString(user.getRealName(), "--"));
//        params.put("time", DateUtils.nowToString());
//        params.put("name", SafeConverter.toString(shop.getFullName(), "--"));
//        params.put("role", MizarUserRoleType.of(user.getRoleList().get(0)).getRoleName());
//        params.put("id", SafeConverter.toString(rcdMsg.get("rid")));
//        MizarNotify notify = MizarNotifyPostOffice.writeNotify(MizarNotifyTemplate.SHOP_CREATE, params);
//
//        // 暂时先向所有的运营人员发送消息
//        List<String> operateDept = mizarUserLoaderClient.loadAllDepartments()
//                .stream()
//                .filter(d -> MizarUserRoleType.Operator.getId().equals(d.getRole()))
//                .map(MizarDepartment::getId).collect(Collectors.toList());
//
//        List<String> operators = mizarUserLoaderClient.loadDepartmentUsers(operateDept).values()
//                .stream()
//                .flatMap(List::stream)
//                .map(MizarUser::getId)
//                .collect(Collectors.toList());
//
//        // FIXME 如果发送消息有异常，暂时忽略
//        mizarNotifyServiceClient.sendNotify(notify, operators);
        return MapMessage.successMessage();
    }

    /**
     * 修改品牌操作
     * 1. 不直接应用变更
     * 2. 如果存在一条相同记录的变更，则提示不能发起；否则生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage modify(MizarShop shop, MizarAuthUser user) {
        // 检查参数以及权限
        MapMessage checkMsg = beforeModify(shop, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        String shopId = shop.getId();
        // 校验机构的各项内容
        MapMessage validMsg = validate(shop);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        MizarShop oldShop = mizarLoaderClient.loadShopById(shopId);
        if (oldShop == null) {
            return MapMessage.errorMessage("无效的机构ID：" + shopId);
        }
        if (oldShop.getShopStatus() != null && MizarShopStatusType.ONLINE.name().equals(oldShop.getShopStatus())) {
            return MapMessage.errorMessage("该机构已经上线，不允许编辑");
        }
        // 查看字段是否有变更, 没有变更的话直接返回 success
        boolean changed = BeanUtils.getInstance().beanEquals(oldShop, shop, COMPARE_FIELDS);
        if (changed) {
            return MapMessage.successMessage();
        }
        // 先判断一下是否有针对这个记录的更新
        List<MizarEntityChangeRecord> records = mizarChangeRecordLoaderClient.loadByTargetAndType(shopId, MizarEntityType.SHOP.getCode());
        if (CollectionUtils.isNotEmpty(records) && records.stream().anyMatch(record -> MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()))) {
            return MapMessage.errorMessage("已有一条变更记录等待审核，暂时不能提交新的变更!");
        }
        MizarShop diff = BeanUtils.getInstance().beanDiff(oldShop, shop);
        // 可以发起变更申请了
        MizarEntityChangeRecord newRecord = MizarAuditEntityFactory.newShopInstance(user, diff, shop.getFullName());
        newRecord.setTargetId(shopId);
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(newRecord);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
//        // 准备发送消息
//        Map<String, String> params = new HashMap<>();
//        params.put("user", SafeConverter.toString(user.getRealName(), "--"));
//        params.put("time", DateUtils.nowToString());
//        params.put("name", SafeConverter.toString(shop.getFullName(), "--"));
//        params.put("role", MizarUserRoleType.of(user.getRoleList().get(0)).getRoleName());
//        params.put("id", SafeConverter.toString(rcdMsg.get("rid")));
//        MizarNotify notify = MizarNotifyPostOffice.writeNotify(MizarNotifyTemplate.SHOP_MODIFY, params);
//        // 暂时先向所有的运营人员发送消息
//        List<String> operateDept = mizarUserLoaderClient.loadAllDepartments()
//                .stream()
//                .filter(d -> MizarUserRoleType.Operator.getId().equals(d.getRole()))
//                .map(MizarDepartment::getId).collect(Collectors.toList());
//
//        List<String> operators = mizarUserLoaderClient.loadDepartmentUsers(operateDept).values()
//                .stream()
//                .flatMap(List::stream)
//                .map(MizarUser::getId)
//                .collect(Collectors.toList());
//        // FIXME 如果发送消息有异常，暂时忽略
//        mizarNotifyServiceClient.sendNotify(notify, operators);
        return MapMessage.successMessage();
    }

//    public MapMessage validate(MizarShop shop) {
//        // 暂无特别要求的输入项
//        StringBuilder validInfo = new StringBuilder();
//        if (StringUtils.isBlank(validInfo.toString())) {
//            return MapMessage.successMessage();
//        }
//        return MapMessage.errorMessage(validInfo.toString());
//    }

}
