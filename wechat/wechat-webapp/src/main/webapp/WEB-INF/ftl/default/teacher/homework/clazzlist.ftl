<div class="mhw-slideBox" data-bind="visible: $data.changeEvent() == 'clazz'" style="display: none;">
    <div class="mask"></div>
    <div data-bind='template: { name: "clazzTemplate" }'> </div>
    <script type="text/html" id="clazzTemplate">
        <div class="innerBox">
            <div class="hd">选择班级<span class="close" data-bind="click: $data.closeChangeEventBox">×</span>
            </div>
            <div class="mn">
                <div class="infoClass">
                    <div class="left">
                        <ul>
                            <!--ko foreach: {data : $data.levelDetail(), as : '_level'} -->
                            <!--ko if:_level.length > 0-->
                            <li data-bind="text: $index()+1 +'年级', click:$root.levelClick.bind($data,$index() + 1), css:{'active' : $root.selectedLevel() == ($index() + 1)}"></li>
                            <!-- /ko -->
                            <!-- /ko -->
                        </ul>
                    </div>
                    <div class="right" style="margin-left: 9rem;">
                        <ul class="classInfo">
                            <!-- ko foreach: {data : $root.selectedClazz(), as : '_clazz'} -->
                            <li data-bind="text: _clazz.clazzName,click : $root.clazzClick.bind($data,$index()), css:{'active': _clazz.checked()}"></li>
                            <!-- /ko -->
                        </ul>
                        <!--ko if:$root.selectedClazz().length > 1-->
                        <a href="javascript:void(0)" class="cancel" data-bind="click:$root.chooseOrCancelAll">
                            <!--ko if: $root.isAllChecked-->
                            <span>取消全部</span>
                            <!--/ko-->
                            <!--ko ifnot: $root.isAllChecked-->
                            <span>全部</span>
                            <!--/ko-->
                        </a>
                        <!--/ko-->
                    </div>
                </div>
            </div>
            <div class="btns pad-30">
                <a data-bind="visible: $data.getCheckedClazzIds().length == 0" href="javascript:void(0)" class="w-btn disabled">确认</a>
                <a data-bind="click: $data.clazzSubmitBtn,visible: $data.getCheckedClazzIds().length > 0" href="javascript:void(0)" class="w-btn">确认</a>
            </div>
        </div>
    </script>
</div>