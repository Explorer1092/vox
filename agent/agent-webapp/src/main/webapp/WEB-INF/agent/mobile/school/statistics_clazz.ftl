<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="学生统计">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">学生统计</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-tab">
    <div <#if type == 1>class="active"</#if>>
        <a href="/mobile/school/statistics_clazz.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&type=1">使用</a>
    </div>
    <div <#if type == 2>class="active"</#if>>
        <a href="/mobile/school/statistics_clazz.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&type=2">认证</a>
    </div>
    <div <#if type == 3>class="active"</#if>>
        <a href="/mobile/school/statistics_clazz.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&type=3">本月高覆盖</a>
    </div>
    <div <#if type == 4>class="active"</#if>>
        <a href="/mobile/school/statistics_clazz.vpage?schoolId=${schoolId}&clazzLevel=${clazzLevel}&type=4">双科认证</a>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">年级</div>
            <div class="side-fr side-time side-width" style="width: 15%;">
                <#if type == 1 || type == 2>注册
                <#elseif type == 3 || type == 4>认证
                </#if>
            </div>
            <div class="side-fr side-time side-width" style="width: 5%;">/</div>
            <div class="side-fr side-time side-width" style="width: 30%;">
                <#if type == 1>使用
                <#elseif type == 2>认证
                <#elseif type == 3>本月高覆盖
                <#elseif type == 4>双科
                </#if>
            </div>
        </div>
    </li>
    <#if list??>
        <#list list as item>
            <li>
                <a href="/mobile/school/clazz_info.vpage?schoolId=${schoolId}&clazzLevel=${item.clazzLevel}&clazzId=${item.clazzId}&type=${type}" class="link link-ico">
                    <div class="side-fl" style="line-height: 1.5rem;"  >${item.name!''}</div>
                    <div class="side-fr side-width" style="width: 15%;">
                        <#if type == 1>${item.totalCount!0}
                        <#elseif type == 2>${item.totalCount!0}
                        <#elseif type == 3>${item.authedCount!0}
                        <#elseif type == 4>${item.authedCount!0}
                        </#if>
                    </div>
                    <div class="side-fr side-width" style="width: 5%;">/</div>
                    <div class="side-fr side-orange side-width" style="width: 30%;">
                        <#if type == 1>${item.usedCount!0}
                        <#elseif type == 2>${item.authedCount!0}
                        <#elseif type == 3>${item.hcaActiveCount!0}
                        <#elseif type == 4>${item.doubleSubjectCount!0}
                        </#if>
                    </div>
                </a>
            </li>
        </#list>
    </#if>
</ul>
</@layout.page>