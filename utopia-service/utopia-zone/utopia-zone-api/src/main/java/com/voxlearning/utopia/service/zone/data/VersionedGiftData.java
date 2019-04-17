package com.voxlearning.utopia.service.zone.data;

import com.voxlearning.utopia.service.zone.api.entity.Gift;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class VersionedGiftData implements Serializable {
    private static final long serialVersionUID = 7465756200451757900L;

    private long version;
    private List<Gift> giftList;
}
