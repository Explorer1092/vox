package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author malong
 * @since 2018/3/6
 */
@Getter
@Setter
public class ParentRewardMapper implements Serializable {
    private static final long serialVersionUID = -8802711121361735590L;
    private String id;
    private String key;
    private String realType;
    private String type;
    private Integer count;
    private String title;
    private String createDate;
    private String sendDate;
    private String receiveDate;
    private String sendUser;
    private int status;
}
