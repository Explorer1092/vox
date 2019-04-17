package com.voxlearning.utopia.admin.productpromotion.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageResult;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.input.ProductPromotionExportSmsParams;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionCreateSmsDto;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.Result;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionSmsListDto;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.Date;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 11:05
 **/
public interface ProductPromotionSmsService {

    /**
     * 新建短信
     * @param dto
     */
    MapMessage create(ProductPromotionCreateSmsDto dto, AuthCurrentAdminUser user);

    /**
     * 发送短信
     * @param id
     * @return
     */
    Boolean send(String id, AuthCurrentAdminUser user);

    /**
     *分页获取列表
     * @param pageInfo
     * @return
     */
    PageResult<ProductPromotionSmsListDto> queryPage(String operationUserName, PageInfo pageInfo);

    HSSFWorkbook export(ProductPromotionExportSmsParams params);
}
