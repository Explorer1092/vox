<script id="t:LEVEL_READINGS" type="text/html">
    <div class="e-pictureBook" data-bind="if:$root.includeAllModule && $root.includeAllModule(),visible:$root.includeAllModule && $root.includeAllModule()">
        <div class="h-topicPackage" style="margin: 0 0 -10px -18px;">
            <div class="topicBox" style="padding: 0 18px;">
                <ul data-bind="foreach:{data:$root.packageList,as:'pk'}">
                    <li class="active" data-bind="css:{'active':$root.focusIndex() == $index()},click:$root.packageClick.bind($data,$index(),$root)">
                        <p data-bind="text:pk.name">&nbsp;</p>
                    </li>
                </ul>
            </div>
        </div>
        <div class="h-tab-box">
            <div class="t-homework-form t-tab-box" style="overflow: visible;">
                <dl class="search-box">
                    <dt>搜索：</dt>
                    <dd>
                        <input type="text" placeholder="" data-bind="textInput:$root.searchWord">
                        <span class="s-btn" data-bind="click:$root.searchClick">搜索</span>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
    <!--ko template:{name:$root.displayMode}--><!--/ko-->
</script>

<script type="text/html" id="t:PK_RECOMMEND">
    <div style="padding-bottom: 20px;">
    <!--ko if:readingRecommend.recTypeList().length > 0-->
    <!--ko foreach:{data:$root.readingRecommend.recTypeList,as:'recType'}-->
    <div class="e-pictureBook diffNew">
        <div class="e-title"><span data-bind="text:recType.moduleName">&nbsp;</span><!--ko text:recType.description--><!--/ko--></div>
        <!--ko if:recType.pictureBookList.length > 3-->
        <div class="arrow arrow-l" style="display: none;" data-bind="visible:recType.beginPos() > 0,click:$root.readingRecommend.arrowLeftClick.bind($data,$root.readingRecommend,$element)"><i class="icon"></i></div> <!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
        <div class="arrow arrow-r" style="display: block;" data-bind="visible:(recType.beginPos() + recType.moveCount) < recType.totalCount,click:$root.readingRecommend.arrowRightClick.bind($data,$root.readingRecommend,$element)"><i class="icon"></i></div>
        <!--/ko-->
        <div class="e-pictureBox" style="position:relative; height: 148px; overflow: hidden">
            <ul class="clearfix" data-bind="style:{position:'absolute',transition:'left 2s',left: (0 - recType.beginPos() * 250) + 'px',width:(recType.pictureBookList.length * 255 + 'px')}"><!--width为li的个数*250px-->
                <!--ko foreach:{data:recType.pictureBookList,as:'pb'}-->
                <li class="e-pictureList diffNew examTopicBox">
                    <p class="state" style="display: none;" data-bind="visible:pb.showAssigned"></p>
                    <i class="new-icon-2" style="display: none;" data-bind="visible:!pb.showAssigned && pb.isNew">NEW</i>
                    <div class="lPic" data-bind="click:$root.readingRecommend.readingView.bind($data,$root.readingRecommend,recType.module)">
                        <a href="javascript:void(0)">
                            <!--ko if:pb.pictureBookThumbImgUrl-->
                            <img data-bind="attr:{src:pb.pictureBookThumbImgUrl}">
                            <!--/ko-->
                            <!--ko ifnot:pb.pictureBookThumbImgUrl-->
                            <img src="<@app.link href='public/skin/teacherv3/images/homework/envelope-tea.png'/>">
                            <!--/ko-->
                        </a>
                    </div>
                    <div class="rInfo">
                        <div class="title"><a href="javascript:void(0)" data-bind="text:pb.pictureBookName">&nbsp;</a></div>
                        <!--ko if:$root.subject == 'CHINESE'-->
                        <p class="text" data-bind="text:pb.pictureBookTopics.join('、') + '|' + pb.pictureBookSeries">&nbsp;</p>
                        <p class="text" style="white-space:normal;display: -webkit-box;-webkit-box-orient: vertical;-webkit-line-clamp:2;" data-bind="text:pb.pictureBookSummary">&nbsp;</p>
                        <!--/ko-->
                        <!--ko if:$root.subject != 'CHINESE'-->
                        <p class="text" data-bind="text:pb.pictureBookClazzLevelName">&nbsp;</p>
                        <p class="text" data-bind="text:pb.pictureBookTopics.join('、')">&nbsp;</p>
                        <p class="text" data-bind="text:pb.pictureBookSeries">&nbsp;</p>
                        <!--/ko-->

                    </div>
                    <div class="h-btnGroup">
                        <!--ko if:$root.readingRecommend.pictureBookIds.indexOf(pb.pictureBookId) != -1-->
                        <a href="javascript:void(0)" class="btn cancel" data-bind="click:$root.readingRecommend.addOrCancel.bind($data,$root.readingRecommend,$element,recType.module)">移除</a>
                        <!--/ko-->
                        <!--ko ifnot:$root.readingRecommend.pictureBookIds.indexOf(pb.pictureBookId) != -1-->
                        <a href="javascript:void(0)" class="btn" data-bind="click:$root.readingRecommend.addOrCancel.bind($data,$root.readingRecommend,$element,recType.module)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                        <!--/ko-->
                    </div>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->
    <!--ko if:readingRecommend.recTypeList().length == 0-->
    <div class="e-pictureBox">
        <div class="emptyBox">
            <i class="empty-icon"></i><p>暂无布置的绘本</p>
        </div>
    </div>
    <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:PK_ALL">
    <div class="h-baseTab">
        <div class="e-pictureBook">
            <div class="h-tab-box">
                <div class="t-homework-form t-tab-box" style="overflow: visible;">
                    <dl class="J_filter-clazzLevels theme-box" data-bind="if:allReading.clazzLevelList().length > 0,visible:allReading.clazzLevelList().length > 0">
                        <dt data-bind="text:$root.subject == 'CHINESE' ? '等级：':'年级：'">&nbsp;</dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down" style="width: auto;">
                                    <p class="selAll label-check" data-bind="css:{'label-check-current':$root.allReading.levelSelectAll},click:$root.allReading.unlimitClick.bind($root.allReading,'clazzLevels')"><span class="label">不限</span></p>
                                    <div class="side">
                                    <!--ko foreach:{data: allReading.clazzLevelList,as:'clazzLevel'}-->
                                        <p class="filter-item" data-bind="css:{'w-radio-current':$root.allReading.levelIds.indexOf(clazzLevel.levelId) != -1},click:$root.allReading.addOrCancelLevel.bind($data,$root.allReading)">
                                            <span class="w-radio"></span><span class="w-icon-md" data-bind="text:clazzLevel.levelName">&nbsp;</span>
                                        </p>
                                    <!--/ko-->
                                    </div>
                                </div>
                            </div>
                            <!--ko if:allReading.clazzLevelList().length > 5-->
                            <a href="javascript:void(0)" filterType="clazzLevels" class="arrow-show-icon" data-bind="click:allReading.showAll.bind(allReading,$element)"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                    <dl class="J_filter-topics theme-box" data-bind="if:allReading.topicList().length > 0,visible:allReading.topicList().length > 0"><!--showAll显示所有-->
                        <dt>主题：</dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="selAll label-check" data-bind="css:{'label-check-current':$root.allReading.topicSelectAll},click:$root.allReading.unlimitClick.bind($root.allReading,'topics')"><span class="label">不限</span></p>
                                    <div class="side">
                                    <!--ko foreach:{data:allReading.topicList,as:'topic'}-->
                                        <p class="filter-item"  data-bind="css:{'w-checkbox-current':$root.allReading.topicIds.indexOf(topic.topicId) != -1},click:$root.allReading.addOrCancelTopic.bind($data,$root.allReading)">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="text:topic.topicName">&nbsp;</span>
                                        </p>
                                    <!--/ko-->
                                    </div>
                                </div>
                            </div>
                            <!--ko if:allReading.topicList().length > 5-->
                            <a href="javascript:void(0)" filterType="topics" class="arrow-show-icon" data-bind="click:allReading.showAll.bind(allReading,$element)"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                    <dl class="J_filter-series theme-box" data-bind="if:allReading.seriesList().length > 0,visible:allReading.seriesList().length > 0">
                        <dt>系列：<span class="book-new" style="display:none;"></span></dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="selAll label-check" data-bind="css:{'label-check-current':$root.allReading.seriesSelectAll},click:$root.allReading.unlimitClick.bind($root.allReading,'series')"><span class="label">不限</span></p>
                                    <div class="side">
                                    <!--ko foreach:{data:allReading.seriesList,as:'series'}-->
                                        <p class="filter-item" data-bind="css:{'w-checkbox-current':$root.allReading.seriesIds.indexOf(series.seriesId) != -1},click:$root.allReading.addOrCancelSeries.bind($data,$root.allReading)">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="text:series.seriesName">&nbsp;</span>
                                        </p>
                                    <!--/ko-->
                                    </div>
                                </div>
                            </div>
                            <!--ko if:allReading.seriesList().length > 5-->
                            <a href="javascript:void(0)" filterType="series" class="arrow-show-icon" data-bind="click:allReading.showAll.bind(allReading,$element)"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                </div>
            </div>
            <div class="e-pictureTips" style="display: none;"></div>
            <div class="e-pictureBox">
                <ul class="clearfix" data-bind="if:allReading.readingList().length > 0,visble:allReading.readingList().length > 0">
                    <!--ko foreach:{data:allReading.readingList,as:'reading'}-->
                    <li class="e-pictureList diffNew examTopicBox">
                        <p class="state" style="display: none;" data-bind="visible:reading.showAssigned"></p>
                        <i class="new-icon-2" style="display: none;" data-bind="visible:!reading.showAssigned && reading.isNew">NEW</i>
                        <div class="lPic" data-bind="click:$root.allReading.readingView.bind($data,$root.allReading)">
                            <a href="javascript:void(0)">
                                <!--ko if:reading.pictureBookThumbImgUrl-->
                                <img data-bind="attr:{src:reading.pictureBookThumbImgUrl}">
                                <!--/ko-->
                                <!--ko ifnot:reading.pictureBookThumbImgUrl-->
                                <img src="<@app.link href='public/skin/teacherv3/images/homework/envelope-tea.png'/>">
                                <!--/ko-->
                            </a>
                        </div>
                        <div class="rInfo">
                            <div class="title"><a href="javascript:void(0)" data-bind="text:reading.pictureBookName">&nbsp;</a></div>
                            <!--ko if:$root.subject == 'CHINESE'-->
                            <p class="text" data-bind="text:reading.pictureBookTopics.join('、') + '|' + reading.pictureBookSeries">&nbsp;</p>
                            <p class="text" style="white-space:normal;display: -webkit-box;-webkit-box-orient: vertical;-webkit-line-clamp:2;" data-bind="text:reading.pictureBookSummary">&nbsp;</p>
                            <!--/ko-->
                            <!--ko if:$root.subject != 'CHINESE'-->
                            <p class="text" data-bind="text:reading.pictureBookClazzLevelName">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookTopics.join('、')">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookSeries">&nbsp;</p>
                            <!--/ko-->
                        </div>
                        <div class="h-btnGroup">
                            <!--ko if:$root.allReading.pictureBookIds.indexOf(reading.pictureBookId) != -1-->
                            <a href="javascript:void(0)" class="btn cancel" data-bind="click:$root.allReading.addOrCancel.bind($data,$root.allReading,$element)">移除</a>
                            <!--/ko-->
                            <!--ko ifnot:$root.allReading.pictureBookIds.indexOf(reading.pictureBookId) != -1-->
                            <a href="javascript:void(0)" class="btn" data-bind="click:$root.allReading.addOrCancel.bind($data,$root.allReading,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                            <!--/ko-->
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
                <!--ko if:allReading.readingList().length == 0-->
                <div class="emptyBox">
                    <i class="empty-icon"></i><p>对不起，还没有满足条件的绘本</p>
                </div>
                <!--/ko-->
            </div>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:allReading.pagination,if:allReading.readingList().length > 0}"></div>
    </div>
</script>

<script type="text/html" id="t:PK_ASSIGNED_HISTORY">
    <div class="h-baseTab">
        <div class="e-pictureBook">
            <div class="e-pictureTips" style="display: none;"></div>
            <div class="e-pictureBox">
                <ul class="clearfix" data-bind="if:assignedHistory.historyList().length > 0,visible:assignedHistory.historyList().length > 0">
                    <!--ko foreach:{data:assignedHistory.historyList,as:'reading'}-->
                    <li class="e-pictureList diffNew examTopicBox">
                        <p class="state" style="display: none;" data-bind="visible:reading.showAssigned"></p>
                        <i class="new-icon-2" style="display: none;" data-bind="visible:!reading.showAssigned && reading.isNew">NEW</i>
                        <div class="lPic" data-bind="click:$root.assignedHistory.readingView.bind($data,$root.assignedHistory)">
                            <a href="javascript:void(0)">
                                <!--ko if:reading.pictureBookThumbImgUrl-->
                                <img data-bind="attr:{src:reading.pictureBookThumbImgUrl}">
                                <!--/ko-->
                                <!--ko ifnot:reading.pictureBookThumbImgUrl-->
                                <img src="<@app.link href='public/skin/teacherv3/images/homework/envelope-tea.png'/>">
                                <!--/ko-->
                            </a>
                        </div>
                        <div class="rInfo">
                            <div class="title"><a href="javascript:void(0)" data-bind="text:reading.pictureBookName">&nbsp;</a></div>
                            <!--ko if:$root.subject == 'CHINESE'-->
                            <p class="text" data-bind="text:reading.pictureBookTopics.join('、') + '|' + reading.pictureBookSeries">&nbsp;</p>
                            <p class="text" style="white-space:normal;display: -webkit-box;-webkit-box-orient: vertical;-webkit-line-clamp:2;" data-bind="text:reading.pictureBookSummary">&nbsp;</p>
                            <!--/ko-->
                            <!--ko if:$root.subject != 'CHINESE'-->
                            <p class="text" data-bind="text:reading.pictureBookClazzLevelName">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookTopics.join('、')">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookSeries">&nbsp;</p>
                            <!--/ko-->
                        </div>
                        <div class="h-btnGroup">
                            <!--ko if:$root.assignedHistory.pictureBookIds.indexOf(reading.pictureBookId) != -1-->
                            <a href="javascript:void(0)" class="btn cancel" data-bind="click:$root.assignedHistory.addOrCancel.bind($data,$root.assignedHistory,$element)">移除</a>
                            <!--/ko-->
                            <!--ko ifnot:$root.assignedHistory.pictureBookIds.indexOf(reading.pictureBookId) != -1-->
                            <a href="javascript:void(0)" class="btn" data-bind="click:$root.assignedHistory.addOrCancel.bind($data,$root.assignedHistory,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                            <!--/ko-->
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
                <!--ko if:assignedHistory.historyList().length == 0-->
                <div class="emptyBox"><i class="empty-icon"></i><p>对不起，还没有满足条件的绘本</p></div>
                <!--/ko-->
            </div>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:assignedHistory.pagination,if:assignedHistory.historyList().length > 0}"></div>
    </div>
</script>

<script type="text/html" id="t:UNKNOWN_READINGS">
    <div class="emptyBox"><i class="empty-icon"></i><p>对不起，还没有满足条件的绘本</p></div>
</script>

<script type="text/html" id="t:SELECT_PRACTICES_POPUP">
    <div class="popup-textbox">
        <p class="p-text">必选练习<i class="tag" data-bind="visible:$root.questionPractices().length>1">（练习可多选）</i></p>
        <div class="p-radio" data-bind="foreach:{data:$root.questionPractices,as:'practice'}">
            <!-- selected 选中 -->
            <span class="sel" data-bind="css:{'selected':$root.practiceIds.indexOf(practice.type) != -1},click:$root.typeClick.bind($data,$root)">
                <i class="i-radio"></i><!--ko text:practice.typeName--><!--/ko-->
            </span>
        </div>
        <!--ko if:$root.expandPractices && $root.expandPractices().length > 0-->
        <p class="p-text">拓展练习 <i class="tag"></i></p>
        <div class="p-radio" data-bind="foreach:{data:$root.expandPractices,as:'practice'}">
            <span class="sel-2" data-bind="css:{'selected':$root.practiceIds.indexOf(practice.type) != -1},click:$root.typeClick.bind($data,$root)">
                <i class="i-radio"></i><!--ko text:practice.typeName--><!--/ko--><i class="tips" data-bind="text:practice.description ? '(' + practice.description + ')' : ''"></i>
            </span>
        </div>
        <!--/ko-->
    </div>
</script>