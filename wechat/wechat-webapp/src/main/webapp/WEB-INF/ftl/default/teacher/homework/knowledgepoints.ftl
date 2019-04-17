<div data-bind="visible: $data.examKnowledgePointsListBox" style="display: none;">
    <div class="mhw-slideBox">
        <div class="mask"></div>
        <div class="innerBox">
            <div class="hd">选择知识点<span class="close" data-bind="click : function(){$root.examKnowledgePointsListBox(false)}">×</span></div>
            <div class="topicList mhw-slideOverflow">
                <ul>
                    <!--ko if: $root.examKnowledgePointsDetail().length > 0-->
                    <!-- ko foreach : {data : $root.examKnowledgePointsDetail(), as : '_kp'} -->
                    <li data-bind="css: {'show': _kp.isActive}" class="show">
                        <p class="name" data-bind="click: $root.examPkIsActiveClick">
                            <span class="text" data-bind="text: _kp.kpType">--</span>
                            <i class="arrow"></i>
                        </p>
                        <div class="labelBox">
                            <!-- ko foreach : {data : _kp.knowledgePoints, as : '_kpd'} -->
                            <span data-bind="text: _kpd.kpName, click: $root.examChangeKp, css:{'active': _kpd.checked}">--</span>
                            <!--/ko-->
                        </div>
                    </li>
                    <!--/ko-->
                    <!--/ko-->
                </ul>
            </div>
            <#--<div class="mhw-btns">
                <a href="javascript:void(0)" class="w-btn">确认</a>
            </div>-->
        </div>
    </div>
</div>