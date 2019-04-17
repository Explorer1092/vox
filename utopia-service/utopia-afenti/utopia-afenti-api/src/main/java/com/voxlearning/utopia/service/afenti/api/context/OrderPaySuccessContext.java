package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import lombok.*;

/**
 * @author peng.zhang.a
 * @since 16-7-26
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@UtopiaCacheRevision("20181106")
public class OrderPaySuccessContext extends AbstractAfentiContext<OrderPaySuccessContext> {
    private static final long serialVersionUID = 5529656602660847449L;

    // in
    @NonNull private Long userId;
    @NonNull private String productServiceType;//OrderProductServiceType
    @NonNull private Integer period;
}