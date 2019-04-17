package com.voxlearning.utopia.mizar.entity.bookStore;


import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class XueMizarBookStoreOrderInfoBean implements Serializable {

    private Date payDatetime;
    private String orderId;
    private String studentName;
    private String tradeId;
}
