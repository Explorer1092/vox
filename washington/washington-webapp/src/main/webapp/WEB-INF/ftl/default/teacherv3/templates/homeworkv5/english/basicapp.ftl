<script type="text/html" id="t:BASIC_APP">
    <div class="h-topicPackage" data-bind="if:!ctLoading() && packageList().length > 1,visible:!ctLoading() && packageList().length > 1">
        <div class="topicBox">
            <ul>
                <!--ko foreach:{data:packageList(),as:'package'}-->
                <li data-bind="css:{'active':$root.focusPackage() && package.groupName == $root.focusPackage().groupName},click:$root.viewPackage.bind($data,$root,$index())">
                    <p data-bind="attr:{title:package.groupName}"><!--ko text:package.groupName--><!--/ko--></p>
                </li>
                <!--/ko-->
            </ul>
        </div>
        <div class="line"></div>
    </div>
    <!--ko if:contentList().length > 0 && !ctLoading()-->
    <div class="e-lessonsBox">
        <!--ko foreach:{data:contentList,as:'content'}-->
        <div class="e-lessonsList">
            <div class="el-title" data-bind="text:content.lessonName"></div>
            <div class="el-name" data-bind="text:$root.covertSentences(content.sentences())"></div>
            <div class="el-list">
                <ul>
                    <!--ko foreach:{data:content.categories(),as:'category'}-->
                    <li data-bind="css:{'active' : category.checked}">
                        <div class="lessons-text previewText" data-bind="singleAppHover:true,click:$root.categoryPreview.bind($data,content.lessonId,$root)">
                            <div class="preview lessons-mask">预览</div>
                            <i class="e-icons">
                                <img data-bind="attr:{src:$root.getCategroyIconUrl(category.categoryIcon())}">
                            </i>
                            <span class="text" data-bind="text:category.categoryName"></span>
                        </div>
                        <div class="lessons-btn operateBtn" data-bind="singleAppHover:category.checked">
                            <div data-bind="click:$root.addCategory.bind($data,$parent,$root),visible:!(category.checked && category.checked())"><i class="h-set-icon h-set-icon-add"></i><p>选入</p></div>
                            <div data-bind="click:$root.removeCategory.bind($data,$parent,$root),visible:category.checked && category.checked()"><p>移除</p></div>
                        </div>
                        <div class="w-bean-location" data-bind="visible:category.teacherAssignTimes && category.teacherAssignTimes() > 0"><i class="w-icon w-icon-34"></i></div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
    <!--ko if:contentList().length == 0 && !ctLoading()-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>温馨提示：该单元暂无应用，请选择其他单元布置</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:ctLoading,visible:ctLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>

</script>

<script id="t:UFO_BASIC_APP" type="text/html">
    <span class="name"><%=tabTypeName%></span>
    <span class="count" data-count="<%=count%>">0</span>
    <span class="icon"><i class="J_delete h-set-icon-delete h-set-icon-deleteGrey"></i></span>
</script>