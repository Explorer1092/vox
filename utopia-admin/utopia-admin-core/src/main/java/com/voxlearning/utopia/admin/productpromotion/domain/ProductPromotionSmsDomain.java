package com.voxlearning.utopia.admin.productpromotion.domain;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageResult;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.input.ProductPromotionExportSmsParams;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionCreateSmsDto;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionSmsListDto;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 14:46
 **/
public interface ProductPromotionSmsDomain {
    /**
     * 新建短信
     * @param sms
     */
    void create(ProductPromotionSms sms);

    /**
     * 发送短信
     * @param sms
     * @return
     */
    void upsert(ProductPromotionSms sms);

    /**
     * 获取详情
     * @param id
     * @return
     */
    ProductPromotionSms detail(String id);

    /**
     * 分页查询
     *
     * @param pageInfo 分页参数
     * @return 一页数据
     */
    List<ProductPromotionSms> query(String operationUserName, PageInfo pageInfo);

    /**
     * 根据发布时间查找
     * @param beginTime
     * @param endTime
     * @return
     */
    List<ProductPromotionSms> queryBySendTime(Date beginTime, Date endTime);

    /**
     * 查询总数量
     *
     * @return 总记录数
     */
    Integer count();

    /**
     * 查询总数量
     *
     * @return 总记录数
     */
    Integer count(String operationUserName);
}
