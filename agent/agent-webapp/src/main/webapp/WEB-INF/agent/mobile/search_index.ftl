<#import "./layout_new.ftl" as layout>
<@layout.page group="搜索">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerText">搜索</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-searchTop mobileCRM-V2-mt">
    <a href="/mobile/school/school_list.vpage"><span class="school">搜学校</span></a>
    <a href="/mobile/teacher/v2/teacher_list.vpage"><span class="teacher">查老师</span></a>
</div>

<#if schoolLevel?? && schoolLevel == 2>
<ul class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="/mobile/invite/invite_list.vpage" class="link link-ico">
            <div class="side-fl side-orange">老师邀请</div>
        </a>
    </li>
</ul>
</#if>

<ul class="mobileCRM-V2-list mobileCRM-V2-mt">
    <li>
        <a href="/mobile/school/region_school.vpage" class="link link-ico">
            <div class="side-fl">按区域查看学校</div>
        </a>
    </li>
</ul>
</@layout.page>
