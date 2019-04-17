<#--阅读绘本预览-->
<script type="text/html" id="readingViewTemplate">
    <div class="mhw-slideBox">
        <div class="mask" data-bind="click: $root.koTemplateClose"></div>
        <div class="innerBox">
            <div class="hd">预览<span class="close" data-bind="click: $root.koTemplateClose">×</span></div>
            <!-- ko foreach : {data : $root.readingViewDetail(), as : '_reading'} -->
            <div class="pictureBook-details">
                <div class="b-content">
                    <div class="b-list">
                        <div class="pic"><img data-bind="attr:{'src': _reading.pictureBookThumbImgUrl}" src="" alt=""></div>
                        <div class="info">
                            <div class="b-title" data-bind="text: _reading.pictureBookName">--</div>
                            <div class="b-side">
                                <p data-bind="text: _reading.pictureBookSeries">--</p>
                                <p data-bind="text: _reading.pictureBookClazzLevels().join('|')">--</p>
                                <p data-bind="text: _reading.pictureBookTopics().join('|')">--</p>
                            </div>
                            <!--ko ifnot: _reading.checked()-->
                            <a href="javascript:void(0)" data-bind="click: $root.addReading" class="choice-btn"><i>+</i><span>选入</span></a>
                            <!--/ko-->

                            <!--ko if: _reading.checked()-->
                            <a href="javascript:void(0)" data-bind="click: $root.removeReading" class="choice-btn remove"><i>-</i><span>移除</span></a>
                            <!--/ko-->

                        </div>
                    </div>
                </div>
                <!--ko if: _reading.keywords().length > 0-->
                <div class="b-column">
                    <div class="b-info">重点词汇</div>
                    <div class="d-main">
                        <!-- ko foreach : {data : _reading.keywords(), as : '_keyword'} -->
                        <span data-bind="text: _keyword">--</span>
                        <!--/ko-->
                    </div>
                </div>
                <!--/ko-->
            </div>
            <!--/ko-->
        </div>
    </div>
</script>

<script type="text/html" id="readingViewTemplate_Flash">
    <div class="mhw-slideBox">
        <div class="mask" data-bind="click: $root.koTemplateClose"></div>
        <div class="innerBox">
            <div class="hd">预览<span class="close" data-bind="click: $root.koTemplateClose">×</span></div>
            <div style="height: 700px;-webkit-overflow-scrolling: touch;overflow-y: scroll;"><iframe id="readingReview" style="display:block !important;" width="100%" height="700" data-bind="attr : {src : $root.readingViewFlashUrl}"></iframe></div>
        </div>
    </div>
</script>

<script type="text/html" id="quizPackageAllBox">
    <div class="mhw-slideBox">
        <div class="mask" data-bind="click: $root.koTemplateClose"></div>
        <div class="innerBox">
            <div class="hd">选择题包<span class="close" data-bind="click: $root.koTemplateClose">×</span></div>
            <div class="topicPackage-tab mhw-slideOverflow topicPackage-slide">
                <ul>
                    <!--ko foreach : {data : $data.quizPackageList(), as : "_package"}-->
                    <li data-bind="click: $root.quizPackageBoxSelected, css : {'active':_package.checked()}">
                        <p class="name" data-bind="text: _package.title()">--</p>
                        <!--ko if: _package.selCount() * 1 != 0 -->
                        <span class="state" data-bind="text: _package.selCount">--</span>
                        <!--/ko-->

                        <!--ko if: _package.packageUsed() && _package.selCount() == 0 -->
                        <span class="state state-grey">用过</span>
                        <!--/ko-->
                    </li>
                    <!--/ko-->

                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="examPackageAllBox">
    <div class="mhw-slideBox">
        <div class="mask" data-bind="click: $root.koTemplateClose"></div>
        <div class="innerBox">
            <div class="hd">选择题包<span class="close" data-bind="click: $root.koTemplateClose">×</span></div>
            <div class="topicPackage-tab mhw-slideOverflow topicPackage-slide">
                <ul>
                    <!--ko foreach : {data : $data.packageList(), as : "_package"}-->
                    <li data-bind="click: $root.packageBoxSelected, css : {'active':_package.checked()}">
                        <p class="name" data-bind="text: _package.name()">--</p>
                        <!--ko if: _package.selCount() * 1 != 0 -->
                        <span class="state" data-bind="text: _package.selCount">--</span>
                        <!--/ko-->

                        <!--ko if: _package.packageUsed() && _package.selCount() == 0 -->
                        <span class="state state-grey">用过</span>
                        <!--/ko-->
                    </li>
                    <!--/ko-->

                </ul>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="oralPracticePackageAllBox">
    <div class="mhw-slideBox">
        <div class="mask" data-bind="click: $root.koTemplateClose"></div>
        <div class="innerBox">
            <div class="hd">选择题包<span class="close" data-bind="click: $root.koTemplateClose">×</span></div>
            <div class="topicPackage-tab mhw-slideOverflow topicPackage-slide">
                <ul>
                    <!--ko foreach : {data : $data.oralPracticePackageList(), as : "_package"}-->
                    <li data-bind="click: $root.oralPracticePackageBoxSelected, css : {'active':_package.checked()}">
                        <p class="name" data-bind="text: _package.name()">--</p>
                        <!--ko if: _package.selCount() * 1 != 0 -->
                        <span class="state" data-bind="text: _package.selCount">--</span>
                        <!--/ko-->

                        <!--ko if: _package.packageUsed() && _package.selCount() == 0 -->
                        <span class="state state-grey">用过</span>
                        <!--/ko-->
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
    </div>
</script>

