<script id="T:TERM_BASIC_APP" type="text/html" title="必学必会-单元练习">
    <!--ko if:!$root.ctLoading() && $root.contentList().length > 0-->
    <!--ko foreach:{data : $root.contentList(),as:'unit'}-->
    <div class="e-lessonsBox">
        <div class="finR-title" data-bind="text:unit.unitName()">&nbsp;</div>
        <!--ko foreach:{data:unit.unitContent(),as:'content'}-->
        <div class="e-lessonsList">
            <div class="el-title" data-bind="text:content.lessonName">&nbsp;</div>
            <div class="el-name" data-bind="text:$root.covertSentences(content.sentences())">&nbsp;</div>
            <div class="el-list">
                <ul data-bind="foreach:{data:content.categories(),as:'category'}">
                    <li data-bind="css:{'active' : category.checked}">
                        <div class="lessons-text previewText" data-bind="singleAppHover:true,click:$root.categoryPreview.bind($data,content.lessonId,$root)">
                            <i class="e-icons">
                                <img data-bind="attr:{src:$root.getCategoryIconUrl(category.categoryIcon())}"/></i>
                            <span class="text" data-bind="text:category.categoryName">&nbsp;</span>
                            <div class="preview lessons-mask">预览</div><!--鼠标滑过遮罩层show-->
                        </div>
                        <div class="lessons-btn operateBtn" data-bind="singleAppHover:category.checked">
                            <div data-bind="click:$root.addOrRemoveCategory.bind($data,$parents,$root,true),visible:!(category.checked && category.checked())"><i class="h-set-icon h-set-icon-add"></i><p>选入</p></div>
                            <div data-bind="click:$root.addOrRemoveCategory.bind($data,$parents,$root,false),visible:category.checked && category.checked()"><p>移除</p></div>
                        </div>
                        <div class="w-bean-location" data-bind="visible:category.teacherAssignTimes && category.teacherAssignTimes() > 0"><i class="w-icon w-icon-34"></i></div><!--已布置icon-->
                    </li>
                </ul>
            </div>
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
    <!--/ko-->

    <!--ko if:$root.contentList().length == 0 && !$root.ctLoading()-->
    <div class="h-set-homework current">
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner" style="padding: 15px 10px; text-align: center;">
                    <p>暂无数据，请选择其他内容</p>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <div style="height: 200px; background-color: white; width: 98%;" data-bind="visible:$root.ctLoading()">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>

</script>