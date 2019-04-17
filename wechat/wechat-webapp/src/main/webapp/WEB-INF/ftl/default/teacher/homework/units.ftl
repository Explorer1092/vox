<div class="mhw-slideBox" data-bind="visible: $data.changeEvent() == 'unit'" style="display: none;">
    <div class="mask"></div>
    <div class="innerBox">
        <div class="hd">选择单元<span class="close" data-bind="click: $data.closeChangeEventBox">×</span></div>
        <!--ko ifnot: $root.hasUnitModules-->
        <div class="mn">
            <div class="infoClass">
                <div class="right">
                    <ul class="material">
                        <!--ko foreach : {data: $data.unitsDetail(),as : '_units'} -->
                        <li class="txt-overflow" data-bind="css : {'active' : _units.isDefault}, click : $root.unitClick">
                            <i class="w-icon w-icon-radio"></i>
                            <span data-bind="text: _units.name || '--'"></span>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>
        <!--/ko-->

        <!--ko if: $root.hasUnitModules-->
        <div class="topicList mhw-slideOverflow">
            <ul>
                <!--ko foreach : {data: $data.unitsDetail(),as : '_modules'} -->
                <li>
                    <p class="name"><span class="text" data-bind="text: _modules.moduleName"></span></p>

                    <ul class="unitList">
                        <!-- ko foreach : {data : _modules.units, as : '_mu'} -->
                        <li class="txt-overflow" data-bind="css : {'active' : _mu.isDefault}, click : $root.unitClick">
                            <i class="w-icon w-icon-radio"></i>
                            <span data-bind="text: _mu.name || '--'"></span>
                        </li>
                        <!--/ko-->
                    </ul>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <!--/ko-->
        <div class="btns pad-30" data-bind="visible: $data.getSelectedUnitId != 0">
            <a data-bind="click: $data.unitSubmitBtn" href="javascript:void(0)" class="w-btn">确认</a>
        </div>
    </div>
</div>
