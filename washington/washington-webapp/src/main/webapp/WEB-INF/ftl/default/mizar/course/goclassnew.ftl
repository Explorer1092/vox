<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='发现'
pageJs=["init"]
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/skin"]}
bodyClass='bg-f4'
>
<#include "../function.ftl"/>
<style>
    #ParentalActivityBox .flex-viewport{ height: 2.2rem !important;}
</style>
<div class="pub-header" style="background: -webkit-linear-gradient(left,#24d08c,#53cdbd) #3bcea4; padding-top: .6rem; color: #fff;">
    <a href="javascript:;" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/mycourse.vpage')}" class="pub-right" style="color: #fff;">我的课程</a>
    发现
</div>
<div class="independentTab-box">
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
    <div id="ActivityBannerBox"></div>
    <#if (courseMap['PARENTAL_ACTIVITY']?size gt 0)!false>
    <div class="travelBox" style="overflow: hidden; cursor: pointer" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/parentalcourse.vpage', '亲子游')}">
        <div class="travel-left">
            <span class="name">亲子游</span>
        </div>

        <div class="travel-right" id="ParentalActivityBox" style="overflow: hidden; height: 2.2rem;">
            <ul class="slides">
            <#list courseMap['PARENTAL_ACTIVITY'] as item>
                <#if (item.title)?has_content>
                <li style="height: 1.1rem;">
                    <div class="text">
                        <span class="type" style="overflow: hidden; white-space: nowrap;">
                        <#if (item.tags)?has_content>
                            <#list item.tags as tag>
                                <#if tag_index == 0 >${(tag)!'----'}</#if>
                            </#list>
                        </#if>
                        </span>
                        <span class="describe" style="overflow: hidden; white-space: nowrap;">${(item.description)!'-----'}</span>
                    </div>
                </li>
                </#if>
            </#list>
            </ul>
        </div>
    </div>
    </#if>
    <div class="ipt-container">
        <div class="ipt-title">
            好课试听
        </div>
        <#if (courseMap['GOOD_COURSE'])?has_content>
        <ul class="ipt-lessonList">
            <#list courseMap['GOOD_COURSE'] as item>
            <li>
                <a href="javascript:void(0);" data-bind="click: function(data, event){$root.GotoLink('${(item.redirectUrl)!}', '好课试听')}">
                    <div class="lesson-image">
                        <img src="${pressImageAutoW(item.background!'', 200)}" style="height: 4rem;"/>
                        <#if (item.tags)?has_content>
                            <#list item.tags as tag>
                                <#if tag_index == 0 && tag?has_content>
                                    <div class="state yellow">${(tag)!}</div>
                                </#if>
                            </#list>
                        </#if>
                        <div class="describe" style="background:none; background-image: -webkit-linear-gradient(top, rgba(0,0,0,0) 0%, rgba(0,0,0,0.7) 100%); ">
                            <#if (item.keynoteSpeaker)?has_content>
                            <span class="name">${(item.keynoteSpeaker)!}</span>
                            </#if>
                            <span class="num">${(item.readCount)!0}</span>
                        </div>
                    </div>
                    <div class="lesson-head" style="line-height: 128%">${((item.title)?string)!'--'}</div>
                </a>
            </li>
            </#list>
        </ul>
        <#else>
            <@getContentNull/>
        </#if>
        <div style="margin: -0.75rem 1rem 0 1rem"><a href="javascript:;" class="zx-tip more" data-bind="click: function(data, event){$root.GotoLink('/mizar/course/goodcourse.vpage?type=new', '好课试听')}" ><span class="txtGreen">查看更多好课试听内容</span></a></div>
    </div>
</div>

<#macro getContentNull info="还没有数据！">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">${info!}</div>
</#macro>
<script type="text/javascript">
    var initMode = "GoClassNew";
</script>

<script type="text/html" id="T:ActivityBannerBox">
    <%var items = result.data%>
    <div class="ahv-banner">
        <%for(var i = 0; i < items.length; i++){%>
        <%if(items.length%2 == 1 && i == 0){%>
        <!--大图-->
        <div class="ahB">
            <a href="javascript:;" data-link="<%=result.goLink%>?aid=<%=items[i].id%>" class="JS-clickGotoLink">
                <img src="<%=result.imgDoMain%>gridfs/<%=(items[i].gif ? items[i].gif : items[i].img)%>" width="100%" style="display: block;"/>
            </a>
        </div>
        <%}else{%>
        <!--小图-->
        <div class="ahB ahB-s">
            <a href="javascript:;" data-link="<%=result.goLink%>?aid=<%=items[i].id%>" class="JS-clickGotoLink">
                <img src="<%=result.imgDoMain%>gridfs/<%=items[i].img%>" width="100%" style="display: block;"/>
            </a>
        </div>
        <%}%>
        <%}%>
    </div>
</script>
</@layout.page>