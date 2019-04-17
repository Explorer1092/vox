<script id="T:BASIC_WORD" type="text/html">
    <div class="switch-inner">
        <!--ko if: !!$root.groupStatus() -->
        <div class="clazz-info">已布置班级不会重复布置：<span data-bind="text:groupStatus"></span></div>
        <!--/ko-->
        <!--ko if: $root.homeworkContent().length > 0-->
        <div class="switch-box">
            <p class="txt">选择温习内容</p>
            <div class="switch-list-box">
                <!--ko foreach:$root.homeworkContent()-->
                <div class="item" data-bind="css:{active:$data.checked()},click:$root.selContent">
                    <p class="sub-txt" data-bind="text:$data.contentTypeName"></p>
                    <p data-bind="text:$data.contentTypeDescription"></p>
                    <a class="i-checkbox" href="javascript:void(0);"></a>
                </div>
                <!--/ko-->
            </div>
        </div>
        <!--/ko-->
        <!--ko if: $root.homeworkDays().length > 0-->
        <div class="switch-box">
            <p class="txt">选择温习计划</p>
            <div class="switch-list-box-2">
                <!--ko foreach:$root.homeworkDays()-->
                <div class="item" data-bind="css:{active:$data.checked()},click:$root.selDays.bind($data,$root)">
                    <p class="sub-txt" data-bind="text:$data.day() + '天复习'"></p>
                    <p data-bind="text:'每天' + $data.minutes() + '分钟'"></p>
                    <a class="i-checkbox" href="javascript:void(0);"></a>
                </div>
                <!--/ko-->
            </div>
        </div>
        <!--/ko-->
        <div class="arrang-btn" data-bind="click:$root.assignBasicWord">一键布置</div>
    </div>
</script>