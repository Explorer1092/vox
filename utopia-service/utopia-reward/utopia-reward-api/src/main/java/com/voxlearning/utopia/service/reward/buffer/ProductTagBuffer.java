package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-11-29 20:27
 **/
public class ProductTagBuffer extends NearBuffer<List<ProductTag>> {

    private List<ProductTag> list = new ArrayList<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ProductTag> productTags) {
        list = productTags;
    }

    public List<ProductTag> loadProductTagByParent(Long parentId, Integer parentType) {
        return supplyWithReadLock(() -> {
            return list.stream().filter(tag -> Objects.equals(parentId, tag.getParentId()) && Objects.equals(parentType, tag.getParentType())).collect(Collectors.toList());
        });
    }
}
