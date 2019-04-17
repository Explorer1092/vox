<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#--<#import "studentquery.ftl" as studentQuery>-->
<@layout_default.page page_title="${(parent.profile.realname)!}(${(parent.id)!})" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>

<div class="span9">
    <@headsearch.headSearch/>
    <legend>家长等级详情:${(parent.profile.realname)!}(${(parent.id)!})
    </legend>
    <table class="table table-bordered">
        <tr>
            <th>活跃值</th>
            <th>家长等级</th>
        </tr>
        <tbody id="user-records-list">
        <tr>
            <td>${(parentLevel.value)!'--'}</td>
            <td>${(parentLevel.level)!'--'}</td>
        </tr>
        </tbody>
    </table>

    <legend>孩子等级</legend>
    <table class="table table-bordered">
        <tr>
            <th>学生ID</th>
            <th>学生姓名</th>
            <th>关系</th>
            <th>学生活跃值</th>
            <th>学生等级</th>
            <th>学生家庭等级</th>
        </tr>
        <tbody>
        <#if studentInfos??>
            <#list studentInfos as info>
                <tr>
                    <td>${(info.child.id)!'--'}</td>
                    <td>${(info.child.profile.realname)!'--'}</td>
                    <td>${(info.callName)!'--'}</td>
                    <td>${(info.studentLevel.value)!'--'}</td>
                    <td>${(info.studentLevel.level)!'--'}</td>
                    <td>${(info.homeLevel.level)!'--'}</td>
                </tr>
            </#list>
        </#if>
        </tbody>
    </table>

    <legend>近7天活跃值记录</legend>
    <table class="table table-bordered">
        <tr>
            <th>日期</th>
            <th>行为</th>
            <th>活跃值</th>
        </tr>
        <tbody>
        <#if logs??>
            <#list logs as log>
                <tr>
                    <td>${(log.date)!'--'}</td>
                    <td>${(log.action)!'--'}</td>
                    <td>${(log.value)!'--'}</td>
                </tr>
            </#list>
        </#if>
        </tbody>
    </table>
</div>
</@layout_default.page>