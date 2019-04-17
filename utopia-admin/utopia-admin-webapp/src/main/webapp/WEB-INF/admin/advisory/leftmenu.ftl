<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<div class="span2">
    <div class="well sidebar-nav" style="background-color: #fff;">
        <li data-toggle="collapse" data-target="#article" class="nav-header">文章</li>
        <div id="article">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/advisory/wechatcrawleradmin.vpage">文章源抓取配置</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/viewrawarticles.vpage">文章筛选</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/viewarticles.vpage">文章管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/jxtnewslist.vpage">文章发布</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/commentpick.vpage">评论筛选</a></li>
            </ul>
        </div>

        <li data-toggle="collapse" data-target="#push" class="nav-header">精选推送与专题</li>
        <div id="push">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/advisory/pushmanage.vpage">推送精选管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/subjectmanage.vpage">专题管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/jxtnewssubjectlist.vpage">专题发布</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/albumManage.vpage">专辑管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/mizar/news/checkNewsList.vpage">外部文章审核</a></li>
            </ul>
        </div>

        <li data-toggle="collapse" data-target="#settings" class="nav-header">设置</li>
        <div id="settings">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/advisory/channelsmgr.vpage">频道管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/view_news_source.vpage">文章源设置</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/view_tag_list_keywords.vpage">文章类型设置</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/viewtagtree.vpage">标签树管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/wechatmassfilters.vpage">抓取-引导词及图片</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/wechatmassbadwords.vpage">抓取-敏感词</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/crawlerVideoView.vpage">抓取-音频</a></li>
            </ul>
        </div>

        <li data-toggle="collapse" data-target="#statistics" class="nav-header">统计</li>
        <div id="statistics">
            <ul class="nav nav-list">
                <li><a href="${requestContext.webAppContextPath}/advisory/tagNewsCount.vpage">标签下文章的数量</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/jxtnewscountlist.vpage">文章统计</a></li>
            </ul>
        </div>
    </div>
</div>
