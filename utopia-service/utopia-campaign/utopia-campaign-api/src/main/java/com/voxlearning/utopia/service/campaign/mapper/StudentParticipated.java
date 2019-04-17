package com.voxlearning.utopia.service.campaign.mapper;

import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentParticipated implements java.io.Serializable {

    private static final long serialVersionUID = 6623128390441995489L;

    public StudentParticipated(Boolean allow, String info) {
        this.allow = allow;
        this.info = info;
    }

    public StudentParticipated(Boolean allow) {
        this.allow = allow;
    }

    private Boolean allow;   // 是否允许进入
    private String info;     // 拒绝进入的原因

    public Boolean isDeny() {
        return SafeConverter.toBoolean(!allow, true);
    }
}
