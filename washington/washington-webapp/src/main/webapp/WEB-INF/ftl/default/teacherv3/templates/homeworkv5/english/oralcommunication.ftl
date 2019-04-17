<script id="t:ORAL_COMMUNICATION" type="text/html">
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


<script type="text/html" id="t:RECOMMEND_PK">
    <div style="padding-bottom: 20px;">
        <!--ko if:recommendModuleList().length > 0-->
        <!--ko foreach:{data:recommendModuleList,as:'moduleObj'}-->
        <div class="e-pictureBook">
            <div class="e-title"><span data-bind="text:moduleObj.moduleName">&nbsp;</span><!--ko text:moduleObj.description--><!--/ko--></div>
            <div class="sliderHolder" name="J_recommend">
                <!--ko if:moduleObj.oralCommunicationList.length > 4-->
                <div id="left-recommend" class="arrow-2 arrow-l" style="display: none;" data-bind="visible:moduleObj.beginPos() > 0,click:$parent.arrowLeftClick.bind($data,$root,$element)"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于4时，arrow显示-->
                <div id="right-recommend" class="arrow-2 arrow-r" style="display: block;" data-bind="visible:(moduleObj.beginPos() + moduleObj.moveCount) < moduleObj.totalCount,click:$parent.arrowRightClick.bind($data,$root,$element)"><i class="icon"></i></div>
                <!--/ko-->
                <div id="container-recommend" class="e-pictureBox" style="position:relative; height: 190px; overflow: hidden">
                    <ul class="clearfix" data-bind="style:{left: (0 - moduleObj.beginPos() * 190) + 'px',width:(moduleObj.oralCommunicationList.length * 190 + 'px'),position:'absolute',transition: 'left 2s'}"><!--width为li的个数*192px-->
                        <!--ko foreach:{data : moduleObj.oralCommunicationList, as : 'item'}-->
                        <li class="e-pictureList-2 examTopicBox">
                            <!--ko if:item.showAssigned-->
                            <p class="state"></p>
                            <!--/ko-->
                            <!--ko if:(!item.showAssigned) && item.isNew-->
                            <i class="new-icon-2">NEW</i>
                            <!--/ko-->
                            <div class="picbox">
                                <div class="pic-box" data-bind="click:$parents[1].previewItem.bind($data,$parents[1],moduleObj.module)">
                                    <img style="width: 100%;" data-bind="attr:{src:item.thumbUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                    <a class="play-btn" href="javascript:void(0)" style="display: none;"><span class=""></span></a>
                                </div>
                            </div>
                            <div class="video-info">
                                <p class="title" data-bind="text:item.oralCommunicationName">&nbsp;</p>
                                <p class="text" data-bind="text:item.clazzLevel">&nbsp;</p>
                                <#--<p class="text" data-bind="text:item.sentences.join(' , ')">&nbsp;</p>-->
                            </div>
                            <div class="h-btnGroup">
                                <!--ko if:$parents[1].oralCommunicationIds.indexOf(item.oralCommunicationId) == -1-->
                                <a href="javascript:void(0);" class="btn" data-bind="click:$parents[1].addOrCancel.bind($data,$parents[1],$element,moduleObj.module)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                                <!--/ko-->
                                <!--ko ifnot:$parents[1].oralCommunicationIds.indexOf(item.oralCommunicationId) == -1-->
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
                <i class="empty-icon"></i><p>暂无推荐的口语内容</p>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:ALL_CONTENT">
    <div class="h-baseTab">
        <div class="e-pictureBook" data-title="全部内容">
            <div class="e-title"><span>全部内容</span>
                <!--ko text:description--><!--/ko-->
            </div>
            <div class="h-tab-box">
                <div class="t-homework-form t-tab-box" style="overflow: visible;">
                    <dl class="theme-box" data-bind="css:{'showAll':clazzLevelSize() == clazzLevelList.length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>年级：</dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check" data-bind="click:noFilter.bind($data,'LEVEL'),css:{'label-check-current': defaultClazzLevel() == 0}">
                                        <span class="label">不限</span>
                                    </p>
                                    <div class="side" data-bind="foreach:{data:clazzLevelList,as:'item'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'LEVEL'),css:{'w-radio-current':$parent.defaultClazzLevel && item.levelId == $parent.defaultClazzLevel()}">
                                            <span class="w-radio"></span>
                                            <span class="w-icon-md" data-bind="attr:{title : item.levelName},text:item.levelName">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="if:clazzLevelList.length > 5,visible:clazzLevelList.length > 5,click:showAll.bind($data,'LEVEL')"></a>
                        </dd>
                    </dl>
                    <dl class="theme-box" data-bind="css:{'showAll':oralTypeSize() == oralTypeList().length}"><!--默认隐藏，点击下拉切换按钮添加showAll显示所有-->
                        <dt>类型：<span class="book-new" style="display: none;"></span></dt>
                        <dd>
                            <div class="t-homeworkClass-list">
                                <div class="pull-down">
                                    <p class="label-check" data-bind="click:noFilter.bind($data,'ORAL_TYPE'),css:{'label-check-current': oralTypeIds().length == 0}">
                                        <span class="label">不限</span>
                                    </p>
                                    <div class="side" data-bind="foreach:{data:oralTypeList,as:'item'}">
                                        <p class="filter-item" data-bind="click:$parent.selFilter.bind($data,$parent,'ORAL_TYPE'),css:{'w-checkbox-current':$parent.oralTypeIds.indexOf(item.typeId) != -1}">
                                            <span class="w-checkbox"></span>
                                            <span class="w-icon-md" data-bind="attr:{title:item.typeName},text:item.typeName">&nbsp;</span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <!--ko if:oralTypeList().length > defaultOralTypeSize-->
                            <a href="javascript:void(0)" class="arrow-show-icon" data-bind="click:showAll.bind($data,'ORAL_TYPE')"></a>
                            <!--/ko-->
                        </dd>
                    </dl>
                </div>
            </div>
            <div class="e-pictureBox">
                <!--ko if:itemList().length > 0-->
                <ul class="clearfix" data-bind="foreach:{data:itemList(),as:'item'}">
                    <li class="e-pictureList-2 examTopicBox" style="width: 160px;">
                        <!--ko if:item.showAssigned-->
                        <p class="state"></p>
                        <!--/ko-->
                        <!--ko if:(!item.showAssigned) && item.isNew-->
                        <i class="new-icon-2">NEW</i>
                        <!--/ko-->
                        <div class="picbox">
                            <div class="pic-box" data-bind="click:$parent.previewItem.bind($data,$parent)">
                                <img style="width: 100%;" data-bind="attr:{src:item.thumbUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'"  >
                                <a class="play-btn" href="javascript:void(0)"  style="display: none;"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:item.oralCommunicationName">&nbsp;</p>
                            <p class="text" data-bind="text:item.clazzLevel">&nbsp;</p>
                            <#--<p class="text" data-bind="text:item.sentences.join(' , ')">&nbsp;</p>-->
                        </div>
                        <div class="h-btnGroup">
                            <!--ko if:$parent.itemIds.indexOf(item.oralCommunicationId) == -1-->
                            <a href="javascript:void(0)" class="btn" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                            <!--/ko-->
                            <!--ko ifnot:$parent.itemIds.indexOf(item.oralCommunicationId) == -1-->
                            <a href="javascript:void(0)" class="btn cancel" data-bind="click:$parent.addOrCancel.bind($data,$parent,$element)">移除</a>
                            <!--/ko-->
                        </div>
                    </li>
                </ul>
                <!--/ko-->
                <!--ko if:itemList().length==0-->
                <div class="emptyBox"><i class="empty-icon"></i><p>暂无适合该年级的口语内容</p></div>
                <!--/ko-->
            </div>
        </div>
        <div data-bind="template:{name:'T:PAGE_TEMPLATE',data:pagination,if:itemList().length > 0}"></div>
    </div>
</script>