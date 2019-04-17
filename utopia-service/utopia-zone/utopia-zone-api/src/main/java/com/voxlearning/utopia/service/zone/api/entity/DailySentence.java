package com.voxlearning.utopia.service.zone.api.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/10/23 1455
 * @Version1.0
 **/
@Getter
@Setter
@NoArgsConstructor
public class DailySentence implements Serializable {
  private Long id;

  private Integer day;

  private String text;

  private String pic;

  private Boolean finished =false;

}
