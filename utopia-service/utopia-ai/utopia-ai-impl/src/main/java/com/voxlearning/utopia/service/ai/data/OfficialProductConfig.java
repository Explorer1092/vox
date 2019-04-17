package com.voxlearning.utopia.service.ai.data;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OfficialProductConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String productId;
    private List<String> books;
    private String productName;
    private Integer courses;
    private Integer grade;
}
