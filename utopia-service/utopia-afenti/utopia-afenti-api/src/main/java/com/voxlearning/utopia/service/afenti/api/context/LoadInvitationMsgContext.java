package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-7-19
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@UtopiaCacheRevision("20181106")
public class LoadInvitationMsgContext extends AbstractAfentiContext<LoadInvitationMsgContext> {
    private static final long serialVersionUID = 4491982882902815440L;

    // in
    @NonNull private User user;
    @NonNull private Subject subject;

    // middle
    private Map<Long, User> classmateMap = new HashMap<>();
    private String orderProductServiceType;
    private Map<Long, List<UserActivatedProduct>> userOrderMap = new HashMap<>();
    private Map<Long, Map<String, Object>> usingUserMap = new HashMap<>(); // 正在使用中用户
    private Map<Long, Map<String, Object>> expiredUserMap = new HashMap<>(); // 已经过期的用户
    private Map<Long, Map<String, Object>> notPurchaseMap = new HashMap<>(); // 未购买用户

    // out
    private Map<String, Object> result = new HashMap<>();
}