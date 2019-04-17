package com.voxlearning.utopia.service.zone.api.entity;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author yulong.ma
 * @Date 2018/11/10 1132
 * @Version1.0
 **/
@Getter
@Setter
@NoArgsConstructor
public class DifficultyQuestion {

  private Integer difficult;

  private List<String> questionIds;


}
