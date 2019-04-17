<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="教师查询">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack"><a href="javascript:window.history.back()">&lt;&nbsp;返回</a></div>
            <div class="headerText">
                <#if type == 1>注册学生
                <#elseif type == 2>认证学生
                <#elseif type == 3>本月高质量学生
                <#elseif type == 4>双科认证学生
                </#if>
            </div>
        </div>
    </div>
</div>
<#if list??>
    <#list list as item>
        <div class="mobileCRM-V2-noteBar">${item.key}</div>
        <div class="mobileCRM-V2-box mobileCRM-V2-info">
            <ul class="mobileCRM-V2-list">
                <#if item.value??>
                    <#list item.value as valueItem>
                        <li>
                            <a href="/mobile/school/statistics_group_list.vpage?schoolId=${schoolId!}&clazzId=${valueItem.clazzId!}&type=${type!}" class="link link-ico">
                                <div class="side-fl ">${valueItem.name!}</div>
                                <#if type == 1>
                                    <div class="side-fr side-width side-orange">${valueItem.totalCount!0}</div>
                                <#elseif type == 2>
                                    <div class="side-fr side-width side-orange" style="width: 8%;">${(valueItem.totalCount!0) - (valueItem.authedCount!0)}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width" style="width: 8%;">${valueItem.authedCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">认证</div>
                                <#elseif type == 3>
                                    <div class="side-fr side-width side-orange" style="width: 8%;">${(valueItem.authedCount!0) - (valueItem.hcaActiveCount!0)}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width" style="width: 8%;">${valueItem.hcaActiveCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">本月</div>
                                <#elseif type == 4>
                                    <div class="side-fr side-width side-orange" style="width: 8%;">${valueItem.noDoubleSubject!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width" style="width: 8%;">${valueItem.doubleSubjectCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">双科</div>
                                </#if>
                            </a>
                        </li>
                    </#list>
                </#if>
            </ul>
        </div>
    </#list>
</#if>
</@layout.page>