package com.voxlearning.utopia.admin.productpromotion.controller.dto.input;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-30 11:01
 **/
@Data
@Builder
@Accessors(chain = true)
public class ProductPromotionExportSmsParams {
    private String beginDay;
    private String endDay;
}
