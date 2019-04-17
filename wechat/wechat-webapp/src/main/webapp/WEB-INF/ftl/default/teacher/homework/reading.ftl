<div id="hkReadingContent" data-bind="visible : $data.selectedHomeworkType() == 'READING',if: $data.selectedHomeworkType() == 'READING' ">
    <!--ko if: $root.readingWeeklyDetail().length > 0-->
    <div class="pictureBook-list" id="readingWeeklyDetailBox">
        <div class="pb-title">
            <span>本周推荐</span>
            <span class="describe" data-bind="text: $root.readingRecommendResult"></span>
        </div>
        <ul class="pb-box clearfix">
            <!-- ko foreach : {data : $root.readingWeeklyDetail(), as : '_reading'} -->
            <li data-bind="css: {'active': _reading.checked()}">
                <div class="pic" data-bind="click: $root.readingViewBtn.bind($data,'weekly')">
                    <img data-bind="attr: {'src' : _reading.pictureBookThumbImgUrl}"  src=""/>
                    <div class="title"><span data-bind="text: _reading.pictureBookName"></span></div>
                    <div class="label">
                        <span data-bind="visible: _reading.teacherAssignTimes() > 0">已出过</span>
                        <span data-bind="if:_reading.hasOral(),visible:_reading.hasOral()">跟读</span>
                    </div>
                </div>
                <div class="intro">
                    <div class="pb-text">
                        <p class="text" data-bind="text: _reading.pictureBookSeries">--</p>
                        <p class="text">
                            <!-- ko foreach : {data : _reading.pictureBookClazzLevels, as : '_level'} -->
                            <span data-bind="text: _level"></span>
                            <!--/ko-->
                        </p>
                        <p class="text" data-bind="text: _reading.pictureBookTopics().join(',')"></p>
                    </div>
                    <!--ko ifnot: _reading.checked()-->
                    <a href="javascript:void(0)" data-bind="click: $root.addReading.bind($data,'weekly')" class="pb-btn"><i>+</i><span>选入</span></a>
                    <!--/ko-->

                    <!--ko if: _reading.checked()-->
                    <a href="javascript:void(0)" data-bind="click: $root.removeReading.bind($data,'weekly')" class="pb-btn remove"><i>-</i><span>移除</span></a>
                    <!--/ko-->

                </div>
            </li>
            <!--/ko-->
        </ul>
    </div>
    <!--/ko-->


    <!--ko if: $root.readingSynchronousDetail().length > 0-->
    <div class="pictureBook-list" id="readingSynchronousDetailBox">
        <div class="pb-title">
            <span>课堂拓展</span>
            <span class="describe">与本单元话题匹配的绘本</span>
        </div>
        <ul class="pb-box clearfix">
            <!-- ko foreach : {data : $root.readingSynchronousDetail(), as : '_reading'} -->
            <li data-bind="css: {'active': _reading.checked()}">
                <div class="pic" data-bind="click: $root.readingViewBtn.bind($data,'synchronous')">
                    <img data-bind="attr: {'src' : _reading.pictureBookThumbImgUrl}"  src=""/>
                    <div class="title"><span data-bind="text: _reading.pictureBookName"></span></div>
                    <div class="label">
                        <span data-bind="visible: _reading.teacherAssignTimes() > 0">已出过</span>
                        <span data-bind="if:_reading.hasOral(),visible:_reading.hasOral()">跟读</span>
                    </div>
                </div>
                <div class="intro">
                    <div class="pb-text">
                        <p class="text" data-bind="text: _reading.pictureBookSeries">--</p>
                        <p class="text">
                            <!-- ko foreach : {data : _reading.pictureBookClazzLevels, as : '_level'} -->
                            <span data-bind="text: _level"></span>
                            <!--/ko-->
                        </p>
                        <p class="text" data-bind="text: _reading.pictureBookTopics().join(',')"></p>
                    </div>
                    <!--ko ifnot: _reading.checked()-->
                        <a href="javascript:void(0)" data-bind="click: $root.addReading.bind($data,'synchronous')" class="pb-btn"><i>+</i><span>选入</span></a>
                    <!--/ko-->

                    <!--ko if: _reading.checked()-->
                        <a href="javascript:void(0)" data-bind="click: $root.removeReading.bind($data,'synchronous')" class="pb-btn remove"><i>-</i><span>移除</span></a>
                    <!--/ko-->

                </div>
            </li>
            <!--/ko-->
        </ul>
    </div>
    <!--/ko-->
    <!--ko if: $root.readingAllDetail().length > 0 || true-->
    <div class="pictureBook-list" id="readingAll_box">
        <div class="empty-hbFixed" data-bind="visible: $root.readingSearchLock()"></div>

        <div class="_pft" data-bind="css:{'pb-fixTop': $root.readingSearchLock()}"><!--吸顶容器，需要置顶的时候添加类pb-fixTop-->
            <div class="pb-mask" style="display: none;" data-bind="visible: $root.readingSearchLock(),click: $root.readingCloseAllSearchBox"></div><!--下拉tab菜单时将其显示-->

            <div class="pb-topBox" id="topBox">
                <div class="pb-title">
                    <span>全部绘本</span>
                    <span class="describe">新增优质系列绘本</span>
                </div>
                <div class="pb-search">
                    <input type="text" placeholder="请输入绘本英文名搜索" data-bind="value: $root.readingSearchName,click: $root.readingSearchMenuShow" class="ipt">
                    <a href="javascript:void(0)" data-bind="click: $root.readingSearchBtn" class="link">搜索</a>
                </div>
                <div class="pb-searchTab clearfix">
                    <div class="item" data-bind="click: $root.readingSearchByClazzClick, css:{'active': $root.readingSearchByClazzBox}"><span class="label">年级</span></div>
                    <div class="item" data-bind="click: $root.readingSearchByTopicClick, css:{'active': $root.readingSearchByTopicBox}"><span class="label">主题</span></div>
                    <div class="item" data-bind="click: $root.readingSearchBySeriesClick, css:{'active': $root.readingSearchBySeriesBox}"><span class="label">系列</span><span class="book-new"></span></div>
                    <div class="pb-searchItem" data-bind="visible: $root.readingSearchByClazzBox" style="display: none;">
                        <ul class="clearfix">
                            <!-- ko foreach : {data : $root.readingSearchByClazzDetail, as : '_clazz'} -->
                            <li data-bind="text:_clazz.name, css:{'active': _clazz.checked}, click: $root.readingSearchSelectClick.bind($data,'clazz')">--</li>
                            <!--/ko-->
                        </ul>
                    </div>
                    <div class="pb-searchItem" data-bind="visible: $root.readingSearchByTopicBox" style="display: none;">
                        <ul class="clearfix">
                            <!-- ko foreach : {data : $root.readingSearchByTopicDetail, as : '_topic'} -->
                            <li data-bind="text:_topic.topicName, css:{'active': _topic.checked}, click: $root.readingSearchSelectClick.bind($data,'topic')">--</li>
                            <!--/ko-->
                        </ul>
                    </div>
                    <div class="pb-searchItem" data-bind="visible: $root.readingSearchBySeriesBox" style="display: none;">
                        <ul class="clearfix">
                            <!-- ko foreach : {data : $root.readingSearchBySeriesDetail, as : '_series'} -->
                            <li data-bind="text:_series.seriesName, css:{'active': _series.checked} , click: $root.readingSearchSelectClick.bind($data,'series')">--</li>
                            <!--/ko-->
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <ul class="pb-box clearfix" id="readingUlBox">
            <!-- ko foreach : {data : $root.readingAllDetail(), as : '_reading'} -->
            <li data-bind="css: {'active': _reading.checked()}">
                <div class="pic" data-bind="click: $root.readingViewBtn.bind($data,'all')">
                    <img data-bind="img: { src: _reading.pictureBookThumbImgUrl, fallback: '/public/images/teacher/homework/math/envelope-tea.png' }"  src=""/>
                    <div class="title"><span data-bind="text: _reading.pictureBookName"></span></div>
                    <div class="label">
                        <span data-bind="visible: _reading.teacherAssignTimes() > 0">已出过</span>
                        <span data-bind="if: _reading.hasOral(),visible:_reading.hasOral()">跟读</span>
                    </div>
                </div>
                <div class="intro">
                    <div class="pb-text">
                        <p class="text" data-bind="text: _reading.pictureBookSeries">--</p>
                        <p class="text">
                            <!-- ko foreach : {data : _reading.pictureBookClazzLevels, as : '_level'} -->
                            <span data-bind="text: _level"></span>
                            <!--/ko-->
                        </p>
                        <p class="text" data-bind="text: _reading.pictureBookTopics().join(',')"></p>
                    </div>
                    <!--ko ifnot: _reading.checked()-->
                    <a href="javascript:void(0)" data-bind="click: $root.addReading.bind($data,'all')" class="pb-btn"><i>+</i><span>选入</span></a>
                    <!--/ko-->

                    <!--ko if: _reading.checked()-->
                    <a href="javascript:void(0)" data-bind="click: $root.removeReading.bind($data,'all')" class="pb-btn remove"><i>-</i><span>移除</span></a>
                    <!--/ko-->

                </div>
            </li>
            <!--/ko-->
            <!--ko if: $root.readingAllDetail().length == 0-->
                <div id="reading_list_null_info_box" class="intro" style="text-align: center; padding: 3rem 0; display: none;">对不起，还没有满足条件的绘本</div>
            <!--/ko-->
        </ul>
    </div>
    <!--/ko-->
</div>