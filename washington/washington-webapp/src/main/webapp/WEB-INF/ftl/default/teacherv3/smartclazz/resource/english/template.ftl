<script id="t:课本" type="text/html">
    <%if(bookList != null && bookList.length > 0){%>
        <%for(var i = 0; i < bookList.length; i++){%>
            <li><a href="javascript:void(0);" data-book_id="<%=bookList[i].bookId%>"><%=bookList[i].bookName%></a></li>
        <%}%>
    <%}%>
</script>
<script id="t:单元" type="text/html">
    <%if(unitList != null && unitList.length > 0){%>
        <%for(var i = 0; i < unitList.length; i++){%>
            <li><a href="javascript:void(0);" data-unit_id="<%=unitList[i].unitId%>"><%=unitList[i].unitName%></a></li>
        <%}%>
    <%}%>
</script>
<#--基础练习-->
<script id="t:基础练习" type="text/html">
    <div class="w-base">
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3>基础练习</h3>
        </div>
        <div class="w-base-container">
            <div class="e-lessonsBox">
                <%for(var i=0;i < content.length;i++){ %>
                <div class="e-lessonsList">
                    <div class="el-title"><%=content[i].lessonName%></div>
                    <div class="el-name"><%=content[i].sentences.join("/")%></div>
                    <div class="el-list">
                        <ul>
                            <%for(var j = 0;j < content[i].categories.length;j++){%>
                            <li categoryId="<%=content[i].categories[j].categoryId%>" lessonId="<%=content[i].lessonId%>">
                                <div class="J_basicMask lessons-text">
                                    <div class="lessons-mask">预览</div>
                                    <i class="e-icons"><img src="<@app.link href='public/skin/teacherv3/images/homework/english-icon/'/>e-icons-<%=content[i].categories[j].categoryIcon%>.png"></i>
                                    <span class="text"><%=content[i].categories[j].categoryName%></span>
                                </div>
                                <%if(content[i].categories[j].teacherAssignTimes > 0){%>
                                <div class="w-bean-location"><i class="w-icon w-icon-34" style="right:0px;margin:0"></i></div>
                                <%}%>
                            </li>
                            <%}%>
                        </ul>
                    </div>
                </div>
                <%}%>
            </div>
        </div>
    </div>
</script>

<#--绘本筛选-->
<script id="t:绘本筛选" type="text/html">
    <div class="e-title"><span>全部绘本</span>新增优质系列绘本<i class="new-icon">New</i></div>
    <div class="h-tab-box">
        <div class="t-homework-form t-tab-box" style="overflow: visible;">
            <dl class="search-box"><dt>搜索：</dt><dd><input id="filter-readingName" class="filter_search" filterType="searchBtn" placeholder="请输入绘本英文名搜索" type="text"><span class="filter_searchBtn s-btn">搜索</span></dd></dl>
            <dl class="J_filter-clazzLevels">
                <dt>年级：</dt>
                <dd>
                    <div class="t-homeworkClass-list">
                        <div class="pull-down" style="width: auto;">
                            <p class="selAll label-check label-check-current"><span class="label">不限</span></p>
                            <%for(var i = 0;i < data.clazzLevels.length;i++){%>
                            <p class="filter-item" filterType="clazzLevels" filterId="<%=data.clazzLevels[i].clazzLevel%>"><span class="w-checkbox"></span><span class="w-icon-md"><%=data.clazzLevels[i].name%></span></p>
                            <%}%>
                        </div>
                    </div>
                </dd>
            </dl>
            <dl class="J_filter-topics theme-box"><!--showAll显示所有-->
                <dt>主题：</dt>
                <dd>
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p class="selAll label-check label-check-current"><span class="label">不限</span></p>
                            <%for(var i = 0;i < data.topics.length;i++){%>
                            <div class="side"><p class="filter-item" filterType="topics" filterId="<%=data.topics[i].topicId%>"><span class="w-checkbox"></span><span class="w-icon-md"><%=data.topics[i].topicName%></span></p></div>
                            <%}%>
                        </div>
                    </div>
                    <%if(data.topics && data.topics.length > 8){%>
                    <a href="javascript:void(0)" filterType="topics" class="J_showAllFilter arrow-show-icon"></a>
                    <%}%>
                </dd>
            </dl>
            <dl class="J_filter-series theme-box">
                <dt>系列：<span class="book-new"></span></dt>
                <dd>
                    <div class="t-homeworkClass-list">
                        <div class="pull-down">
                            <p class="selAll label-check label-check-current"><span class="label">不限</span></p>
                            <%for(var i = 0;i < data.series.length;i++){%>
                            <div class="side"><p class="filter-item" filterType="series" filterId="<%=data.series[i].seriesId%>"><span class="w-checkbox"></span><span class="w-icon-md"><%=data.series[i].seriesName%></span></p></div>
                            <%}%>
                        </div>
                    </div>
                    <%if(data.series && data.series.length > 8){%>
                    <a href="javascript:void(0)" filterType="series" class="J_showAllFilter arrow-show-icon"></a>
                    <%}%>
                </dd>
            </dl>
        </div>
    </div>
</script>
<#--绘本阅读-->
<script id="t:绘本阅读" type="text/html">
    <div class="e-pictureBox">
        <ul class="clearfix">
            <%for(var i = 0;i < data.length;i++){%>
            <li class="e-pictureList examTopicBox" pictureBookId="<%=data[i].pictureBookId%>">
                <div id="keywords_<%=data[i].pictureBookId%>" style="display: none;"><%=data[i].keywords.join("|")%></div>
                <%if(data[i].teacherAssignTimes > 0){%>
                <p class="state"></p>
                <%}%>
                <div class="title"><a href="javascript:void(0)"><%=data[i].pictureBookName%></a></div>
                <div class="lPic">
                    <a href="javascript:void(0)">
                        <%if(data[i].pictureBookThumbImgUrl){%>
                        <img src="<%=data[i].pictureBookThumbImgUrl%>">
                        <%}else{%>
                        <img src="<@app.link href='public/skin/teacherv3/images/homework/envelope-tea.png'/>">
                        <%}%>
                    </a>
                </div>
                <div class="rInfo">
                    <p class="text"><%=data[i].pictureBookSeries%></p>
                    <p class="text"><%=data[i].pictureBookClazzLevels.join(",")%></p>
                    <p class="text"><%=data[i].pictureBookTopics.join(",")%></p>
                    <%if(data[i].hasOral){%>
                    <p class="text"><span class="label">跟读</span></p>
                    <%}%>
                </div>
            </li>
            <%}%>
        </ul>
    </div>
    <%if(pageCount > 0){%>
    <%include('t:template模板分页')%>
    <%}%>
</script>

<#--阅读预览模板-->
<script id="t:预览" type="text/html">
    <div id="showViewContent">
        <div id="install_flash_player_box" style="margin:20px; display: none;">
            <div id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                <a href="<@app.client_setup_url />" target="_blank">您的系统组件需要升级。请点这里<span style="color:red;">下载</span>并<span style="color:red;">运行</span> “一起作业安装程序”。</a>
            </div>
        </div>
    </div>
</script>

<script id="t:听力资源" type="text/html">
    <div id="ttspage"  class="s-table s-table-line-b">
        <table>
            <thead>
            <tr>
                <th style="width: 50%;">资源标题</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <%
            if(paperPage != null && paperPage.totalPages > 0){
                for(var i = 0; i < paperPage.content.length; i++){
                var listenPaper = paperPage.content[i];
            %>
            <tr>
                <td><%=listenPaper.title%></td>
                <td>
                    <a class="ttsPreview" href="/tts_view.vpage?id=<%=listenPaper.id%>" target="_blank">预览</a>
                </td>
            </tr>
            <%}}else{%>
            <tr><td colspan="2">暂无相关内容</td></tr>
            <%}%>
            </tbody>
        </table>
        <%if(paperPage != null && paperPage.totalPages > 0){%>
        <%include('t:template模板分页')%>
        <%}%>
    </div>
</script>

<script id="t:template模板分页" type="text/html">
    <div style="float: right; padding: 0px 0px 10px;" class="system_message_page_list message_page_list">
        <%
            var currentIndex = pageNum;  //当前页对应的脚标
            var len = pageCount; //总页数
        %>
        <a class="v-page-btn <%if(currentIndex == 0){%> disable <%}else{%> enable <%}%>" href="javascript:void(0);" v="prev" data-index="<%if(currentIndex == 0){%>0<%}else{%><%=(currentIndex - 1)%><%}%>"><span>上一页</span></a>
        <%
        if(len - currentIndex > 5){
            for(var z = currentIndex,end = (z + 3); z < end; z++){
        %>
        <a class="v-page-btn <%if(z == currentIndex){%>this<%}%>" data-index="<%=z%>" href="javascript:void(0);"><span><%=(z + 1)%></span></a>
        <%
            }
        %>
        <span class="points"> ... </span>
        <a class="v-page-btn" href="javascript:void(0);" data-index="<%=(len-1)%>"><span><%=len%></span></a>
        <%
        }else{
            var k;
            if(len > 5){
                k = len - 5;
            }else{
                k = 0;
            }
            for(; k < len; k++){
        %>
        <a class="v-page-btn <%if(k == currentIndex){%> this <%}%>" href="javascript:void(0);" data-index="<%=k%>"><span><%=(k + 1)%></span></a>
        <%}}%>
        <a class="v-page-btn <%if(currentIndex == (len - 1)){%> disable <%}else{%> enable <%}%> " data-index="<%if(currentIndex == (len - 1)){%><%=currentIndex%><%}else{%><%=(currentIndex + 1)%><%}%>" href="javascript:void(0);" v="next"><span>下一页</span></a>
        <input style="width:50px;height: 20px;" id="inputPageNo" type="text" value=""><a id="goBtn20141120112130" style="width: 20px; margin-left: 8px;" class="w-btn w-btn-mini goBtn"><span>GO</span></a>
    </div>
</script>


<script id="t:加载中" type="text/html">
    <div style="height: 200px;"><img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" /></div>
</script>