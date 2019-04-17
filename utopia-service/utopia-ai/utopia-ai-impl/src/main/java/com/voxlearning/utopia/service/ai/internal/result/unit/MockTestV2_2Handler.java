package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;

import javax.inject.Named;

@Named
public class MockTestV2_2Handler extends MockV2UnitResultHandler {

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.mock_test_unit_2;
    }
    
}
