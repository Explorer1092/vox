package com.voxlearning.utopia.service.reward.api.mapper.newversion;

import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.CVTobyDressArgsEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class CVTobyDressArgsMapper implements Serializable {
    private CVTobyDressArgsEntity image;
    private CVTobyDressArgsEntity countenance;
    private CVTobyDressArgsEntity props;
}
