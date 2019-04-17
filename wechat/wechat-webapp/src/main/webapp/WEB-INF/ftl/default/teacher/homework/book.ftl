<div class="mhw-slideBox" data-bind="visible: $data.changeEvent() == 'book'" style="display: none;">
    <div class="mask"></div>
    <div class="innerBox">
        <div class="hd">更改教材<span class="close" data-bind="click: $data.closeChangeEventBox">×</span>
        </div>
        <div class="mn">
            <div class="infoClass">
                <div class="left">
                    <ul>

                        <!-- ko foreach: {data : $data.changeBookClazzList(), as : '_bc'} -->
                        <li data-bind="text: _bc.name(),css: {'active' : _bc.level() == $root.changeBookSelectedClazzLevel()},click: $root.changeBookClazzClick.bind($data,_bc.level())"></li>
                        <!--/ko-->
                    </ul>
                </div>
                <div class="right margin-l">
                    <div class="tabWrap">
                        <ul class="tab">
                            <li data-bind="css: {'active' : $data.changeBookSelectedClazzTerm() == 1},click: $data.changeBookSelectedClazzTermClick.bind($data,1)">上册</li>
                            <li data-bind="css: {'active' : $data.changeBookSelectedClazzTerm() == 2},click: $data.changeBookSelectedClazzTermClick.bind($data,2)">下册</li>
                        </ul>
                    </div>
                    <ul class="material">
                        <!-- ko foreach: {data : $data.booksDetail(), as : '_book'} -->
                            <li class="txt-overflow" data-bind="css: {'active' : _book.checked()}, click : $root.changeBookSelectBook">
                                <i class="w-icon w-icon-radio"></i>
                                <span data-bind="text: _book.name()"></span>
                            </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>
        <div class="btns pad-30">

            <a data-bind="visible: !$root.showChangeBookSubmit()" style="display: none;" href="javascript:void(0)" class="w-btn disabled">确认</a>
            <a data-bind="click: $data.changeBookSubmit,visible: $root.showChangeBookSubmit()" href="javascript:void(0)" class="w-btn">确认</a>
        </div>
    </div>
</div>