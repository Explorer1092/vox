<script type="text/html" id="t:NATURAL_SPELLING">
    <!--ko if:$root.contentList().length > 1-->
    <div class="material-tab" data-bind="foreach:{data:contentList(),as:'tab'}">
        <div class="item" data-bind="click:$root.changeTab.bind($root,$index())">
            <i class="icon " data-bind="css:{'active':$root.currentTab()==$index()}"></i>
            <!--ko text:tab.type()=="nonUniversal" ? "教材同步版":"通用版"--><!--/ko-->
        </div>
    </div>
    <!--/ko-->


    <!--ko if:contentList().length > 0 && !ctLoading()-->
    <div  data-bind="foreach:{data:contentList(),as:'tab'}">
        <div style="display: none;" data-bind="visible:$root.currentTab()==$index()">
            <div class="levelSelect-section" data-bind="if:tab.type()=='universal'">
                <div class="levelName">Level选择</div>
                <div class="levelBox">
                    <!--ko foreach:{data:tab.levels,as:'item'}-->
                    <span class="" data-bind="text:item.levelName,css:{'active':item.level()==$root.currentLevel()},click:$root.changeLevel.bind($root,item.level())"></span>
                    <!--/ko-->
                </div>
            </div>
            <div data-bind="foreach:{data:tab.nonUniversalContents.slice((tab.currentPage()-1)*tab.pageSize(),tab.currentPage()*tab.pageSize()),as:'unit'}">
                <div class="e-lessonsTitle" data-bind="text:unit.unitName" ></div>
                <div class="e-lessonsBox">
                    <!--ko foreach:{data:unit.lessons,as:'content'}-->
                    <div class="e-lessonsList">
                        <div class="el-title" data-bind="text:content.lessonName"></div>
                        <!--ko foreach:{data:content.categoryGroups,as:'ctGroup'}-->
                        <div class="el-name">
                            <!--ko if:ctGroup.newLine && ctGroup.newLine()-->
                            <!--ko foreach:{data : ctGroup.sentences(),as:'st'}-->
                            <p data-bind="text:st">&nbsp;</p>
                            <!--/ko-->
                            <!--/ko-->
                            <!--ko ifnot:ctGroup.newLine && ctGroup.newLine()-->
                            <!--ko text:$root.covertSentences(ctGroup.sentences())--><!--/ko-->
                            <!--/ko-->
                        </div>
                        <div class="el-list">
                            <ul>
                                <!--ko foreach:{data:ctGroup.categories(),as:'category'}-->
                                <li data-bind="css:{'active' : category.checked}">
                                    <div class="lessons-text previewText" data-bind="singleAppHover:true,click:$root.categoryPreview.bind($data,content.lessonId(),$root)">
                                        <div class="preview lessons-mask">预览</div>
                                        <i class="e-icons">
                                            <img data-bind="attr:{src:$root.getCategroyIconUrl(category.categoryIcon())}">
                                        </i>
                                        <span class="text" data-bind="text:category.categoryName"></span>
                                    </div>
                                    <div class="lessons-btn operateBtn" data-bind="singleAppHover:category.checked">
                                        <div data-bind="click:$root.addCategory.bind($data,ctGroup,content,$root),visible:!(category.checked && category.checked())"><i class="h-set-icon h-set-icon-add"></i><p>选入</p></div>
                                        <div data-bind="click:$root.removeCategory.bind($data,content,$root),visible:category.checked && category.checked()"><p>移除</p></div>
                                    </div>
                                    <div class="w-bean-location" data-bind="visible:category.teacherAssignTimes && category.teacherAssignTimes() > 0"><i class="w-icon w-icon-34"></i></div>
                                </li>
                                <!--/ko-->
                            </ul>
                        </div>
                        <!--/ko-->
                    </div>
                    <!--/ko-->
                </div>
            </div>

            <!--ko if:tab.nonUniversalContents().length == 0 && !$root.ctLoading()-->
            <div class="h-set-homework current">
                <div class="seth-mn">
                    <div class="testPaper-info">
                        <div class="inner" style="padding: 15px 10px; text-align: center;">
                            <p>该部分的内容正在加紧制作，敬请期待。</p>
                        </div>
                    </div>
                </div>
            </div>
            <!--/ko-->
            <!--ko if:tab.nonUniversalContents().length > 0-->
            <div class="system_message_page_list homework_page_list" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">

                <a data-bind="css:{'disable' : tab.currentPage() <= 1,'enable' : tab.currentPage() > 1},click:$root.page_click.bind($root,tab.currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>

                <!--ko if:tab.totalPage() <= 7-->
                <!--ko foreach:ko.utils.range(1,tab.totalPage())-->
                <a data-bind="css:{'this':$data == tab.currentPage()},click:$root.page_click.bind($root,$data)" href="javascript:void(0);">
                    <span data-bind="text:$data"></span>
                </a>
                <!--/ko-->
                <!--/ko-->

                <!--ko if:tab.totalPage() > 7 && tab.currentPage() <= 4-->
                <!--ko foreach:ko.utils.range(1,6)-->
                <a data-bind="css:{'this':$data == tab.currentPage()},click:$root.page_click.bind($root,$data)">
                    <span data-bind="text:$data"></span>
                </a>
                <!--/ko-->
                <span class="points">...</span>
                <a data-bind="click:$root.page_click.bind($root,tab.totalPage())">
                    <span data-bind="text:tab.totalPage()"></span>
                </a>
                <!--/ko-->

                <!--ko if:tab.totalPage() > 7 && tab.currentPage() > 4-->
                <a data-bind="click:$root.page_click.bind($root,1)"><span>1</span></a>
                <span class="points">...</span>

                <!--ko if:(tab.totalPage() - tab.currentPage()) <= 3-->
                <!--ko foreach:ko.utils.range(tab.totalPage() - 5,tab.totalPage())-->
                <a data-bind="css:{'this':$data == tab.currentPage()},click:$root.page_click.bind($root,$data)"><span data-bind="text:$data"></span></a>
                <!--/ko-->
                <!--/ko-->

                <!--ko if:(tab.totalPage() - tab.currentPage()) > 3-->
                <!--ko foreach:ko.utils.range(tab.currentPage() - 2,tab.currentPage())-->
                <a data-bind="css:{'this':$data ==tab.currentPage()},click:$root.page_click.bind($root,$data)"><span data-bind="text:$data"></span></a>
                <!--/ko-->

                <!--ko foreach:ko.utils.range(tab.currentPage() + 1,tab.currentPage() + 2)-->
                <a data-bind="click:$root.page_click.bind($root,$data)"><span data-bind="text:$data"></span></a>
                <!--/ko-->
                <span class="points">...</span>
                <a data-bind="click:$root.page_click.bind($root,tab.totalPage())"><span data-bind="text:tab.totalPage()"></span></a>
                <!--/ko-->
                <!--/ko-->

                <a data-bind="css:{'disable' : tab.totalPage() <= 1 || tab.currentPage() >= tab.totalPage(), 'enable' : tab.totalPage() > 1 && tab.currentPage() < tab.totalPage()},click:$root.page_click.bind($root,tab.currentPage() + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
                <div class="pageGo">
                    <input value="" type="text" data-bind="textInput:tab.userInputPage" /><span class="goBtn" data-bind="click:$root.goSpecifiedPage">GO</span>
                </div>
            </div>
            <!--/ko-->
        </div>
    </div>
    <!--/ko-->
    <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:ctLoading,visible:ctLoading">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
    </div>

</script>

<script id="t:UFO_NATURAL_SPELLING" type="text/html">
    <span class="name"><%=tabTypeName%></span>
    <span class="count" data-count="<%=count%>">0</span>
    <span class="icon"><i class="J_delete h-set-icon-delete h-set-icon-deleteGrey"></i></span>
</script>