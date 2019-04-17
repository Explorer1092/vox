<div style="height: 100%; overflow: hidden; overflow-y: scroll;-webkit-overflow-scrolling : touch;">
    <div class="mhw-emptyBox"></div>
    <div class="mhw-header mar-b14">
        <div class="header-inner">
            <div class="fl" data-bind="text: $root.getCategoryNameByValue('BASIC_APP')">--</div>
        </div>
    </div>
    <div class="details-box">
        <!-- ko foreach : {data : $data.currentReportDetail(), as : '_basic'} -->
        <div class="details-list">
            <div class="ex-header">
                <div class="ex-fLeft"><span data-bind="text: _basic.unitName">--</span></div>
            </div>
            <!-- ko foreach : {data : _basic.lessons, as : '_less'} -->
            <div class="d-container">
                <div class="d-title" data-bind="text: _less.lessonName">--</div>
                <ul class="ex-side">
                    <!-- ko foreach : {data : _less.categories, as : '_cate'} -->
                    <li>
                        <div class="ex-left">
                            <span class="ex-content content-1">
                                <img src="" data-bind="attr : {'src': '/public/images/teacher/homework/english/basicappicon/e-icons-'+_cate.practiceCategory()+'.png'}" alt="">
                            </span>
                            <span class="des" data-bind="text: _cate.categoryName()">--</span>
                        </div>
                        <div class="ex-right">
                            <span class="grade" data-bind="text: _cate.averageScore() +'分'">--</span>
                            <span class="det" data-bind="click: $root.basicAppDetailBtn.bind($data,_less.lessonId())">详情<span></span></span>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
    </div>
</div>

