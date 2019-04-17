package com.voxlearning.utopia.service.zone.api.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/11/26 1619
 * @Version1.0
 **/
@Getter
@Setter
public class SignRecord implements Serializable {
  private Integer signType;

  private Boolean finished;

}
