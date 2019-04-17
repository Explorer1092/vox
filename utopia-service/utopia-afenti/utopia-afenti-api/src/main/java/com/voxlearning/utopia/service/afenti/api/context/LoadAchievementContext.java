package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author peng.zhang.a
 * @since 16-8-5
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@UtopiaCacheRevision("20181106")
public class LoadAchievementContext extends AbstractAfentiContext<LoadAchievementContext> {

    private static final long serialVersionUID = 7341171406006744865L;
    // in
    @NonNull private StudentDetail studentDetail;
    @NonNull private Subject subject;

    // middle
    private Map<Long, User> classmateMap;
    private String orderProductServiceType;
    private Set<Long> paidOrderClassmates;
    private Map<Long, Map<String, Object>> usingUserMap; // 正在使用中用户
    private Map<Long, Map<String, Object>> expiredUserMap; // 已经过期的用户
    private Map<Long, Map<String, Object>> notPurchaseMap; // 未购买用户

    // out
    private Map<String, Object> result = new HashMap<>();
}