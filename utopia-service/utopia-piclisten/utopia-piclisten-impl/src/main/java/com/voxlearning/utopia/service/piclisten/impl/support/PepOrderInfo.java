package com.voxlearning.utopia.service.piclisten.impl.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin
 * @since 7/13/17.
 */
@Getter
@Setter
public class PepOrderInfo implements Serializable {
    private static final long serialVersionUID = -8533795479917148840L;

    private String platform_key;
    private String sign;
    private String user_id;
    private List<Info> order_info;

    private Boolean is_piclisten;

    //以下是人教打包订单同步需要的参数
    private Long pay_time;
    private Integer real_price;
    private String pay_tradeno;

    @Getter
    @Setter
    public class Info {
        private String book_id;
        private Long pay_time;
        private Integer real_price;
        private String pay_tradeno;
    }

    @Getter
    @Setter
    public class ChangeBookInfo extends Info {
        private String new_book_id;
    }
}
