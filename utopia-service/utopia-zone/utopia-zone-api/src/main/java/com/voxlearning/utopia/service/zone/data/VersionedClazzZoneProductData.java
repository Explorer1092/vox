package com.voxlearning.utopia.service.zone.data;

import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class VersionedClazzZoneProductData implements Serializable {
    private static final long serialVersionUID = 5311804803171574065L;

    private long version;
    private List<ClazzZoneProduct> clazzZoneProductList;
}
