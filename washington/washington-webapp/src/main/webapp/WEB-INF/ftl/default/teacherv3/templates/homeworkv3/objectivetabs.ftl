<script id="t:教学子目标形式" type="text/html">
    <!--ko if:currentTabs().length > 0-->
    <div class="w-base-switch w-base-two-switch h-switch">
        <ul class="Teachertitle">
            <!--ko foreach:currentTabs-->
            <li data-bind="click:$root.tabClick.bind($data,$root),css:{'active' : $data.type() == $root.focusTabType()}">
                <a href="javascript:void(0);">
                    <span class="h-arrow"></span>
                    <i class="tab-icon" data-bind="style:{backgroundImage : $data.icon()}"></i>
                    <p data-bind="attr:{title:name},text:name"></p>
                </a>
            </li>
            <!--/ko-->
        </ul>
        <!--ko if:tabs().length > $root.displayCount-->
        <div class="h-arrow h-arrow-L" data-bind="click:arrowClick.bind($data,'arrowLeft')"><i class="h-arrow-icon" data-bind="css:{'h-arrow-iconLhover' : leftEnabled()}"></i></div>
        <div class="h-arrow h-arrow-R" data-bind="click:arrowClick.bind($data,'arrowRight')"><i class="h-arrow-icon h-arrow-iconR" data-bind="css:{'h-arrow-iconRhover' : rightEnabled()}"></i></div>
        <!--/ko-->
    </div>
    <!--/ko-->
    <!--ko if:tabs().length == 0-->
    <div class="tabs-empty">当前课时暂无练习内容，请切换其他课时查看</div>
    <!--/ko-->
</script>