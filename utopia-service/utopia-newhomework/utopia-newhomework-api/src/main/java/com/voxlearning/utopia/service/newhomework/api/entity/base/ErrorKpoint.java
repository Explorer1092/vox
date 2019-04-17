package com.voxlearning.utopia.service.newhomework.api.entity.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorKpoint implements Serializable {
    private static final long serialVersionUID = 4981463185201408421L;

    private String errorKpId;        //错误知识ID
    private String errorCause;          //错因
}
