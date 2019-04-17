<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='在线学习'
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/skin"]}
bodyClass='bg-f4'
>
<#include "../function.ftl"/>
<div class="pub-header" style="background: -webkit-linear-gradient(left,#24d08c,#53cdbd) #3bcea4; padding-top: .6rem; color: #fff;">
    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/mycourse.vpage')}" class="pub-right" style="color: #fff;">我的课程</a>
    在线学习
</div>
<div class="independentTab-box">
    <div class="ipt-header">
        <div>
            <div class="lc-banner" id="headerBannerCrm" style="position: relative;">
                <ul class="slides" data-bind="foreach: headerBanner()">
                    <li>
                        <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink($root.goLink() + '?aid=' + id)}" data-linktype="headerBanner">
                            <!-- ko if : img -->
                            <img data-bind="attr:{src : $root.imgDoMain() + 'gridfs/' + img}"/>
                            <!-- /ko -->
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <#if nearClassFlag!false>
        <div class="ipt-head">
            <ul>
                <li>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/list.vpage?firstCategory=少儿教育', '少儿教育', 'tag')}">
                        <div class="image"></div>
                        <div class="info">少儿教育</div>
                    </a>
                </li>
                <li>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/list.vpage?firstCategory=少儿外语', '少儿外语', 'tag')}">
                        <div class="image"></div>
                        <div class="info">少儿外语</div>
                    </a>
                </li>
                <li>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/list.vpage?firstCategory=兴趣才艺', '兴趣才艺', 'tag')}">
                        <div class="image"></div>
                        <div class="info">兴趣才艺</div>
                    </a>
                </li>
                <li>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/list.vpage?firstCategory=游学玩乐', '游学玩乐', 'tag')}">
                        <div class="image"></div>
                        <div class="info">游学玩乐</div>
                    </a>
                </li>
            </ul>
        </div>
        </#if>
    </div>
    <div class="ipt-main">
        <#if (courseMap['GOOD_COURSE'])?has_content>
        <div class="ipt-title">
            <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/goodcourse.vpage', '好课试听')}" class="ipt-titleBar">查看全部</a>
            好课试听
        </div>
        <div class="ipt-banner" id="GoodCourseBox">

            <ul class="ipt-bannerImg slides">
                <#list courseMap['GOOD_COURSE'] as item>
                <li>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('${(item.redirectUrl)!}', '好课试听')}">
                        <#if (item.background)?has_content>
                            <img src="${(item.background)!}">
                            <em class="ipt-mask"></em>
                        </#if>
                        <#if (item.tags)?has_content>
                            <#list item.tags as tag>
                                <div class="ipt-tag" style="overflow: hidden;">
                                    <span class="tagLabel">${(tag)!}</span>
                                </div>
                            </#list>
                        </#if>
                        <div class="ipt-sideTitle">${(item.title)!'--'}</div>
                        <div class="ipt-sidebarTitle">
                            <span class="info">${(item.subTitle)!'--'}</span>

                        </div>
                    </a>
                </li>
                </#list>
            </ul>
        </div>
        </#if>
    </div>
    <div class="ipt-column">
        <div class="ipt-title">
            每日一课
        </div>
        <div class="ipt-content">
            <#if (courseMap['DAY_COURSE'])?has_content>
                <#list courseMap['DAY_COURSE'] as item>
                    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('${(item.redirectUrl)!}', '每日一课')}">
                        <div class="image"><img src="${(item.background)!}"></div>
                        <div class="topic">${(item.title)!}</div>
                        <div class="cont-side" style="margin-top: 0.5rem;">
                            <div class="info">
                                <#if (item.tags)?has_content>
                                    <#list item.tags as tag>
                                        ${tag}
                                    </#list>
                                </#if>
                            </div>
                            <#if (item.readCount gt 0)!false>
                                <div class="name">
                                    ${(item.readCount)!}阅读
                                </div>
                            </#if>
                        </div>
                    </a>
                </#list>
            <#else>
                <@getContentNull/>
            </#if>
            <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/daycourse.vpage', '每日一课')}" class="zx-tip more" style="padding-bottom: 0; margin: 0 0 0.5rem 0;">
                <span class="txtGreen">查看更多每日一课内容</span>
            </a>
        </div>
    </div>

    <div class="ipt-container" style="margin: 0;">
        <div class="ipt-title">
            精品视频课程
        </div>
        <#if (courseMap['VIDEO_COURSE'])?has_content>
        <ul class="ipt-list">
            <#list courseMap['VIDEO_COURSE'] as item>
            <li>
                <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('${(item.redirectUrl)!}', '精品视频课程')}">
                    <div class="cont-image">
                        <img src="${(item.background)!}">
                        <div class="cont-tag">
                            <#if (item.tags?size gt 0)!false>
                                <#list item.tags as tag>
                                    <span>${(tag)!}</span>
                                </#list>
                            </#if>
                        </div>
                    </div>
                    <div class="cont-head">${(item.title)!'---'}</div>
                    <div class="cont-side">
                        <#if (item.readCount gt 0)!false>
                            <div class="info">${(item.readCount)!0} 人观看</div>
                        </#if>
                        <div class="name">${(item.keynoteSpeaker)!}主讲</div>
                    </div>
                </a>
            </li>
            </#list>
        </ul>
        <#else>
            <@getContentNull/>
        </#if>
        <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/videocourse.vpage', '精品视频课程')}" class="zx-tip more" style="padding-bottom: 0; margin: -0.5rem 1rem 0.5rem 1rem; width: auto">
            <span class="txtGreen">查看更多精品视频课内容</span>
        </a>
        <div style="height: 1.5rem;"></div>

    <#--<div class="ipt-slide">
        <a href="javascript:void(0);">
            上次看到“XXXX课程名称XXXX课程名称”的12分20
            秒，点击继续观看
        </a>
    </div>-->
    </div>
</div>

<#macro getContentNull info="还没有数据！">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">${info!}</div>
</#macro>
<script type="text/javascript">
    var initMode = "GoClassMode";
</script>
</@layout.page>