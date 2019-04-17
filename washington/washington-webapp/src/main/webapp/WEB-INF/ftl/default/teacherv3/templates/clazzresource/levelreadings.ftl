<script id="t:LEVEL_READINGS" type="text/html">
    <div class="e-pictureBook" data-bind="if:$root.packageList && $root.packageList().length > 1,visible:$root.packageList && $root.packageList().length > 1">
        <div class="h-topicPackage" style="margin: 0 0 -10px -18px;">
            <div class="topicBox" style="padding: 0 18px;">
                <ul data-bind="foreach:{data:$root.packageList,as:'pk'}">
                    <li class="active" data-bind="css:{'active':$root.focusIndex() == $index()},click:$root.packageClick.bind($data,$index(),$root)">
                        <p data-bind="text:pk.name">&nbsp;</p>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <!--ko template:{name:$root.displayMode}--><!--/ko-->
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
                </div>
            </div>
            <div class="e-pictureTips" style="display: none;"></div>
            <div class="e-pictureBox">
                <ul class="clearfix" data-bind="if:allReading.readingList().length > 0,visble:allReading.readingList().length > 0">
                    <!--ko foreach:{data:allReading.readingList,as:'reading'}-->
                    <li class="e-pictureList-2 examTopicBox" style="height: 257px;">
                        <p class="state" style="display: none;" data-bind="visible:reading.showAssigned"></p>
                        <i class="new-icon-2" style="display: none;" data-bind="visible:!reading.showAssigned && reading.isNew">NEW</i>
                        <div class="picbox">
                            <div class="pic-box" data-bind="click:$root.allReading.previewVideo.bind($data,$root.allReading)">
                                <img style="height:163px;" data-bind="attr:{src:reading.pictureBookThumbImgUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                <a class="play-btn" href="javascript:void(0)"><span class="" style="margin:67px auto 0;"></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:reading.pictureBookName">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookClazzLevelName">&nbsp;</p>
                            <p class="text" data-bind="text:reading.pictureBookSeries">&nbsp;</p>
                        </div>
                        <div class="h-btnGroup">
                            <a href="javascript:void(0)" class="btn" data-bind="if:!$root.allReading.previewLinkFlag(),visible:!$root.allReading.previewLinkFlag(),click:$root.allReading.readingView.bind($data,$root.allReading)">查看绘本</a>
                            <a target="_blank" class="btn" data-bind="if:$root.allReading.previewLinkFlag(),visible:$root.allReading.previewLinkFlag(),attr:{href:$root.fetchPreviewUrl(reading.pictureBookId)}">查看绘本</a>
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

<script type="text/html" id="t:UNKNOWN_READINGS">
    <div class="emptyBox"><i class="empty-icon"></i><p>对不起，还没有满足条件的绘本</p></div>
</script>