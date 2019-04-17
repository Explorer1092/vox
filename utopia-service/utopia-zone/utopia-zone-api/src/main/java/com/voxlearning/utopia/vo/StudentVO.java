package com.voxlearning.utopia.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/10/30 1944
 * @Version1.0
 **/
@Getter
@Setter
public class StudentVO implements Serializable {

  private Long userId;

  private String userName;

  private String pic;
}
