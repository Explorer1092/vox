package com.voxlearning.utopia.mizar.entity;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用于多级联动的数据结构
 * Created by Yuechen.Wang on 2016/11/29.
 */
@Getter
@Setter
public class MizarQueryContext implements Serializable {

    public static final Pageable DEFAULT_PAGE = new PageRequest(0, 10);
    private static final long serialVersionUID = -6872151396208958951L;

    private Pageable pageable;               // 分页

    public MizarQueryContext() {
        new MizarQueryContext(null);
    }

    MizarQueryContext(Pageable pageable) {
        if (pageable == null) {
            pageable = DEFAULT_PAGE;
        }
        this.pageable = pageable;
    }

}
