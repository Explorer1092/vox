package com.voxlearning.washington.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author malong
 * @since 2018/10/09
 */
@Data
@NoArgsConstructor
public class WebViewConfig {
    private String source;
    private String type;
    @JsonProperty("not_hard_phone")
    private String notHardPhone;
}
