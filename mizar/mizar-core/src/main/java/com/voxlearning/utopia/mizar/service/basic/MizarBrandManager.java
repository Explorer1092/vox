package com.voxlearning.utopia.mizar.service.basic;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.mizar.audit.MizarAuditEntityFactory;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.entity.BrandQueryContext;
import com.voxlearning.utopia.mizar.entity.MizarQueryContext;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 品牌管理相关Service.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
@Named
public class MizarBrandManager extends AbstractMizarService implements MizarManager<MizarBrand> {

    // 用于比较两个实体的字段
    private static final List<String> COMPARE_FIELDS = Arrays.asList(
            "brandName"              // 品牌名称
            , "brandLogo"            // 品牌LOGO
            , "introduction"         // 品牌介绍
            , "brandPhoto"           // 中心图片
            , "establishment"        // 创立时间
            , "shopScale"            // 品牌规模
            , "certificationPhotos"  // 获奖证书 图片
            , "certificationName"    // 获奖证书 描述
            , "points"               // 品牌特点
            , "showList"             // 是否显示在品牌管列表
            , "orderIndex"           // 品牌管列表中的排序值
    );


    public Page<MizarBrand> page(MizarAuthUser user, MizarQueryContext context) {
        if (user == null || context == null) {
            return emptyPage();
        }
        BrandQueryContext queryContext = (BrandQueryContext) context;
        String brandName = queryContext.getBrandName();

        if (user.isOperator()) {
            return mizarLoaderClient.loadBrandByPage(context.getPageable(), brandName);
        } else {
            // 其他角色只能看见自己名下机构的品牌
            List<String> shopIds = user.getShopList();
            Set<String> brandId = mizarLoaderClient.loadShopByIds(shopIds).values()
                    .stream()
                    .filter(shop -> StringUtils.isNotBlank(shop.getBrandId()))
                    .map(MizarShop::getBrandId)
                    .collect(Collectors.toSet());
            List<MizarBrand> brandList = mizarLoaderClient.loadBrandByIds(brandId).values()
                    .stream()
                    .filter(b -> StringUtils.isBlank(brandName) || b.getBrandName().contains(brandName))
                    .collect(Collectors.toList());
            return PageableUtils.listToPage(brandList, context.getPageable());
        }
    }

    /**
     * 新增品牌操作
     * 1. 不生成品牌实体
     * 2. 生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage create(MizarBrand brand, MizarAuthUser user) {
        // 检查参数以及权限
        MapMessage checkMsg = beforeCreate(brand, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        // 默认认为调用这个方法的都是新增品牌
        brand.setId(null);
        // 校验品牌的各项内容
        MapMessage validMsg = validate(brand);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        // 可以发起变更申请了
        MizarEntityChangeRecord record = MizarAuditEntityFactory.newBrandInstance(user, brand, brand.getBrandName());
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(record);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
        // 发送消息
        return MapMessage.successMessage();
    }

    /**
     * 修改品牌操作
     * 1. 不直接应用变更
     * 2. 如果存在一条相同记录的变更，则提示不能发起；否则生成一条申请记录
     * 3. 发送通知审核消息
     */
    public MapMessage modify(MizarBrand brand, MizarAuthUser user) {
        // 检查参数以及权限
        MapMessage checkMsg = beforeModify(brand, user);
        if (!checkMsg.isSuccess()) {
            return checkMsg;
        }
        String brandId = brand.getId();
        // 校验品牌的各项内容
        MapMessage validMsg = validate(brand);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        MizarBrand oldBrand = mizarLoaderClient.loadBrandById(brandId);
        if (oldBrand == null) {
            return MapMessage.errorMessage("无效的品牌ID：" + brandId);
        }
        // 查看字段是否有变更, 没有变更的话直接返回 success
        boolean changed = BeanUtils.getInstance().beanEquals(oldBrand, brand, COMPARE_FIELDS);
        if (changed) {
            return MapMessage.successMessage();
        }
        // 先判断一下是否有针对这个记录的更新
        List<MizarEntityChangeRecord> records = mizarChangeRecordLoaderClient.loadByTargetAndType(brandId, MizarEntityType.BRAND.getCode());
        if (CollectionUtils.isNotEmpty(records) && records.stream().anyMatch(record -> MizarAuditStatus.PENDING.name().equals(record.getAuditStatus()))) {
            return MapMessage.errorMessage("已有一条变更记录等待审核，暂时不能提交新的变更!");
        }
        MizarBrand diff = BeanUtils.getInstance().beanDiff(oldBrand, brand);
        // 可以发起变更申请了
        MizarEntityChangeRecord newRecord = MizarAuditEntityFactory.newBrandInstance(user, diff, brand.getBrandName());
        newRecord.setTargetId(brandId);
        MapMessage rcdMsg = mizarChangeRecordServiceClient.saveChangeRecord(newRecord);
        if (!rcdMsg.isSuccess()) {
            return rcdMsg;
        }
        // 发送消息

        return MapMessage.successMessage();
    }

    public MapMessage validate(MizarBrand brand) {
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
        if (StringUtils.isBlank(brand.getBrandName())) {
            validInfo.append("【品牌名称】不能为空!").append("<br />");
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

}
