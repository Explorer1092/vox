<script id="t:DUBBING" type="text/html">
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
    <!--ko template:{name:$root.displayMode,data:$root.displayViewModel()}--><!--/ko-->
</script>


<script type="text/html" id="t:DUBBING_RECOMMEND">
    <div style="padding-bottom: 20px;">
        <!--ko if:recommendModuleList().length > 0-->
        <!--ko foreach:{data:recommendModuleList,as:'moduleObj'}-->
        <div class="e-pictureBook">
            <div class="e-title"><span data-bind="text:moduleObj.moduleName">&nbsp;</span><!--ko text:moduleObj.description--><!--/ko--></div>
            <div class="sliderHolder" name="J_recommend">
                <!--ko if:moduleObj.dubbingList.length > 4-->
                <div id="left-recommend" class="arrow-2 arrow-l" style="display: none;" data-bind="visible:moduleObj.beginPos > 0,click:$root.readingRecommend.arrowLeftClick.bind($data,$root,$element)"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于4时，arrow显示-->
                <div id="right-recommend" class="arrow-2 arrow-r" style="display: block;" data-bind="visible:(moduleObj.beginPos + moduleObj.moveCount) < moduleObj.totalCount,click:$root.readingRecommend.arrowRightClick.bind($data,$root,$element)"><i class="icon"></i></div>
                <!--/ko-->
                <div id="container-recommend" class="e-pictureBox" style="position:relative; height: 190px; overflow: hidden">
                    <ul class="clearfix" data-bind="style:{left: (0 - moduleObj.beginPos * 250) + 'px',width:(moduleObj.dubbingList.length * 192 + 'px'),position:'absolute',transition: 'left 2s'}"><!--width为li的个数*192px-->
                        <!--ko foreach:{data : moduleObj.dubbingList,as : 'dubbingObj'}-->
                        <li class="e-pictureList-2 examTopicBox">
                            <!--ko if:dubbingObj.showAssigned-->
                            <p class="state"></p>
                            <!--/ko-->
                            <!--ko if:(!dubbingObj.showAssigned) && dubbingObj.isNew-->
                            <i class="new-icon-2">NEW</i>
                            <!--/ko-->
                            <div class="picbox">
                                <div class="pic-box" data-bind="click:$root.dubbingView.bind($data,$root,moduleObj.module)">
                                    <img style="width: 100%;" data-bind="attr:{src:dubbingObj.coverUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                    <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                                </div>
                            </div>
                            <div class="video-info">
                                <p class="title" data-bind="text:dubbingObj.name">&nbsp;</p>
                                <p class="text" data-bind="text:dubbingObj.albumName">&nbsp;</p>
                                <p class="text" data-bind="text:dubbingObj.clazzLevel + ' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                                <p class="text" data-bind="text:dubbingObj.topics.join('、')">&nbsp;</p>
                            </div>
                            <div class="h-btnGroup">
                                <!--ko if:$parents[1].dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                                <a href="javascript:void(0);" class="btn" data-bind="click:$parents[1].addOrCancel.bind($data,$parents[1],$element,moduleObj.module)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                                <!--/ko-->
                                <!--ko ifnot:$parents[1].dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                                <a href="javascript:void(0);" class="btn cancel" data-bind="click:$parents[1].addOrCancel.bind($data,$parents[1],$element,moduleObj.module)">移除</a>
                                <!--/ko-->
                            </div>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--ko if:recommendModuleList().length == 0-->
        <div class="e-pictureBox">
            <div class="emptyBox">
                <i class="empty-icon"></i><p>暂无推荐的配音</p>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:DUBBING_ALL">
    <div class="h-baseTab">
        <div class="e-pictureBook" data-title="全部配音">
            <div class="e-title"><span>全部配音</span>
                <!--ko text:description--><!--/ko-->
            </div>
            <div class="h-tab-box">
                <div class="t-homework-form t-tab-box" style="overflow: visible;">
                    <dl class="theme-box" data-bind="css:{'showAll':levelSize() == levelList.length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>年级：</dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check">
                                    </p>
                                    <div class="side" data-bind="foreach:{data:levelList,as:'item'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'LEVEL'),css:{'w-radio-current':$parent.defaultClazzLevel && item.key == $parent.defaultClazzLevel()}">
                                            <span class="w-radio"></span>
                                            <span class="w-icon-md" data-bind="attr:{title : item.name},text:item.name">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="click:showAll.bind($data,'LEVEL')"></a>
                        </dd>
                    </dl>
                    <dl class="theme-box" data-bind="css:{'showAll':channelSize() == channelList().length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>类型：<span class="book-new" style="display: none;"></span></dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check" data-bind="click:noFilter.bind($data,'CHANNEL'),css:{'label-check-current': channelIds().length == 0}">
                                        <span class="label">不限</span>
                                    </p>
                                    <div class="side" data-bind="foreach:{data:channelList,as:'channel'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'CHANNEL'),css:{'w-checkbox-current':$parent.channelIds.indexOf(channel.channelId) != -1}">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="attr:{title:channel.channelName},text:channel.channelName">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <!--ko if:channelList().length > defaultChannelSize-->
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="click:showAll.bind($data,'CHANNEL')"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                    <dl class="J_filter-albums theme-box" data-bind="css:{'showAll' : albumSize() == albumList().length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>专辑：<span class="book-new" style="display: none;"></span></dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check" data-bind="click:noFilter.bind($data,'ALBUM'),css:{'label-check-current':albumIds().length == 0}">
                                        <span class="label">不限</span>
                                    </p>
                                    <div class="side" data-bind="foreach:{data:albumList,as:'album'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'ALBUM'),css:{'w-checkbox-current':$parent.albumIds.indexOf(album.albumId) != -1}">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="attr:{title:album.albumName},text:album.albumName">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <!--ko if:albumList().length > defaultAlbumSize-->
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="click:showAll.bind($data,'ALBUM')"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                    <dl class="theme-box" data-bind="css:{'showAll':themeSize() == themeList().length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>主题：</dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check" data-bind="click:noFilter.bind($data,'THEME'),css:{'label-check-current':themeIds().length == 0}">
                                        <span class="label">不限</span>
                                    </p>
                                    <div class="side" data-bind="foreach:{data:themeList,as:'themeObj'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'THEME'),css:{'w-checkbox-current':$parent.themeIds.indexOf(themeObj.themeId) != -1}">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="attr:{title:themeObj.themeName},text:themeObj.themeName">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <!--ko if:themeList().length > defaultThemeSize-->
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="click:showAll.bind($data,'THEME')"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                </div>
            </div>
            <div class="e-pictureBox">
                <!--ko if:dubbingList().length > 0-->
                <ul class="clearfix" data-bind="foreach:{data:dubbingList(),as:'dubbingObj'}">
                    <li class="e-pictureList-2 examTopicBox">
                        <!--ko if:dubbingObj.showAssigned-->
                        <p class="state"></p>
                        <!--/ko-->
                        <!--ko if:(!dubbingObj.showAssigned) && dubbingObj.isNew-->
                        <i class="new-icon-2">NEW</i>
                        <!--/ko-->
                        <div class="picbox">
                            <div class="pic-box" data-bind="click:$parent.dubbingView.bind($data,$parent)">
                                <img style="width: 100%;" data-bind="attr:{src:dubbingObj.coverUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:dubbingObj.name">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.albumName">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.clazzLevel + ' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.topics.join('、')">&nbsp;</p>
                        </div>
                        <div class="h-btnGroup">
                            <!--ko if:$parent.dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                            <a href="javascript:void(0)" class="btn" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                            <!--/ko-->
                            <!--ko ifnot:$parent.dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                            <a href="javascript:void(0)" class="btn cancel" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)">移除</a>
                            <!--/ko-->
                        </div>
                    </li>
                </ul>
                <!--/ko-->
                <!--ko if:dubbingList().length==0-->
                <div class="emptyBox"><i class="empty-icon"></i><p>对不起，还没有满足条件的趣味配音</p></div>
                <!--/ko-->
            </div>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:pagination,if:dubbingList().length > 0}"></div>
    </div>
</script>


<script type="text/html" id="t:COLLECT_DUBBING">
    <div class="h-baseTab">
        <div class="e-pictureBook" data-title="收藏配音">
            <div class="e-pictureBox">
                <!--ko if:dubbingList().length > 0-->
                <ul class="clearfix" data-bind="foreach:{data:dubbingList(),as:'dubbingObj'}">
                    <li class="e-pictureList-2 examTopicBox">
                        <!--ko if:dubbingObj.showAssigned-->
                        <p class="state"></p>
                        <!--/ko-->
                        <!--ko if:(!dubbingObj.showAssigned) && dubbingObj.isNew-->
                        <i class="new-icon-2">NEW</i>
                        <!--/ko-->
                        <div class="picbox">
                            <div class="pic-box" data-bind="click:$parent.dubbingView.bind($data,$parent)">
                                <img style="width: 100%;" data-bind="attr:{src:dubbingObj.coverUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:dubbingObj.name">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.albumName">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.clazzLevel + ' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.topics.join('、')">&nbsp;</p>
                        </div>
                        <div class="h-btnGroup">
                            <!--ko if:$parent.dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                            <a href="javascript:void(0)" class="btn" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                            <!--/ko-->
                            <!--ko ifnot:$parent.dubbingIds.indexOf(dubbingObj.dubbingId) == -1-->
                            <a href="javascript:void(0)" class="btn cancel" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)">移除</a>
                            <!--/ko-->
                        </div>
                    </li>
                </ul>
                <!--/ko-->
                <!--ko if:dubbingList().length==0-->
                <div class="emptyBox"><i class="empty-icon"></i><p>暂无配音收藏</p></div>
                <!--/ko-->
            </div>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:pagination,if:dubbingList().length > 0}"></div>
    </div>
</script>