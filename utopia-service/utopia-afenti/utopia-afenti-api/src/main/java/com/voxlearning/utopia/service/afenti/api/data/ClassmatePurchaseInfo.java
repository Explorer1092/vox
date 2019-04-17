package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author peng.zhang.a
 * @since 16-11-30
 */

@Getter
@Setter
@UtopiaCacheRevision("20161205")
public class ClassmatePurchaseInfo implements Serializable {
    private static final long serialVersionUID = -6318590753915049812L;

    private Long userId;
    private String realName;
    private PurchaseType purchaseType;
    private String imgUrl;
    private Date createDate;
}
