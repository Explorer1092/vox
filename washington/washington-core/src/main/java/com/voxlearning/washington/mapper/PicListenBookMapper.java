package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin
 * @since 12/1/16.
 */
@Getter
@Setter
public class PicListenBookMapper implements Serializable {
    private static final long serialVersionUID = -2412566825813543316L;

    private String bookId;
    private String bookName;
    private String bookImgUrl;
}
