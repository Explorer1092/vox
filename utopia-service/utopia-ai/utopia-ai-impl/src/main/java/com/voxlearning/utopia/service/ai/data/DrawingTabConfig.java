package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class DrawingTabConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String labelCode;
    private String labelName;
    private List<String> books;
}
