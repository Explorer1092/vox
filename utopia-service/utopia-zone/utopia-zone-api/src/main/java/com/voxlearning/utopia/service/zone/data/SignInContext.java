package com.voxlearning.utopia.service.zone.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class SignInContext implements Serializable {
    private static final long serialVersionUID = -6068908213240727008L;

    private Long studentId;
    private Long clazzId;
    private Map<String, Object> extensions;
}
