<#import "../../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_resource" submenu="">
    <@sugar.capsule js=["jquery.flashswf","smartclazz"] />

<div class="s-contain-box">
    <div class="sc-box s-magB-10">
        <ul class="sc-list">
            <li>
                <span class="tip">来源:</span>
                <p class="sl-menu" data-tag="17resource"><a href="javascript:void (0)" style="display: none;">一起作业资源</a></p>
                <p class="sl-menu current" data-tag="selfresouce"><a href="javascript:void (0)">我的资源</a></p>
            </li>
            <li>
                <span class="tip">资源类型:</span>
                <p class="resourceType sl-menu" data-tag-ref="17resource" data-value="basicPractices" style="display: none;"><a href="javascript:void(0);">基础练习</a></p>
                <p class="resourceType sl-menu" data-tag-ref="17resource" data-value="readingPractices" style="display: none;"><a href="javascript:void(0);">绘本阅读</a></p>
                <p class="resourceType sl-menu current" data-tag-ref="selfresouce" data-value="ttsPractices"><a href="javascript:void(0);">听力</a></p>
            </li>
            <li id="J_bookInfo">
                <span class="tip">教材版本:</span>
                <div id="bookSelect" class="w-select">
                    <div class="current"><span class="content" data-value="">课本名称</span><span class="w-icon w-icon-arrow"></span></div>
                    <ul id="bookList"></ul>
                </div>
                <div id="unitSelect" class="w-select" style="display: none;"><div class="current"><span class="content" data-value="">单元名称</span><span class="w-icon w-icon-arrow"></span></div>
                    <ul id="unitList"></ul>
                </div>
            </li>
        </ul>
    </div>
    <div id="basicPra" class="s-tab-box">
        <div class="inner" id="contextList"></div>
    </div>
    <div id="readingPra" class="e-pictureBook">
        <div id="readingFilter"></div>
        <div id="readingList" style="height:330px;"></div>
    </div>
</div>
<#include 'template.ftl'/>
<div id="previewDiv"></div>
</@temp.pagecontent>