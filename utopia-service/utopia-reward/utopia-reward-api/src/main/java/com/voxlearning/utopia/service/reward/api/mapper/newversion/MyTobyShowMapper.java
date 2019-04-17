package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.TobyEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class MyTobyShowMapper implements Serializable {
    private Boolean isChangeAvatar;
    private List<String> expiryDressNameList;
    private Long integralNum;
    private Long fragmentNum;
    private TobyEntity toby;
}
