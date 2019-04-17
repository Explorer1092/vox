package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author malong
 * @since 2017/3/22
 */
@NoArgsConstructor
@Getter
@Setter
public class ParentSignConfig implements Serializable {
    private static final long serialVersionUID = -2379675024920336004L;

    private String icon;    //icon
    private String url;     //跳转的H5页面链接
    private String mainGray;    //灰度main
    private String subGray;     //灰度sub
}
