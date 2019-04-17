<div class="mhw-slideBox" data-bind="visible: $data.changeEvent() == 'subject'" style="display: none;">
    <div class="mask"></div>
    <div class="innerBox">
        <div class="hd">选择学科<span class="close" data-bind="click: $data.closeChangeEventBox">×</span>
        </div>
        <div class="mn">
            <ul class="infoSubject">
                <!-- ko foreach : {data : $root.subjectList(), as : '_subject'} -->
                <li data-bind="text: _subject.value(), css: {'active' : _subject.checked()}, click: $root.selectSubjectBtn">--</li>
                <!--/ko-->
            </ul>
        </div>
        <div class="mhw-btns">
            <a href="javascript:void(0)" class="w-btn" data-bind="click: submitSubjectBtn">确认</a>
        </div>
    </div>
</div>