<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#--<#import "studentquery.ftl" as studentQuery>-->
<@layout_default.page page_title="${(student.profile.realname)!}(${(student.id)!})" page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>

<div class="span9">
    <@headsearch.headSearch/>
    <legend>学生等级详情:${(student.profile.realname)!}(${(student.id)!})
    </legend>
    <table class="table table-bordered">
        <tr>
            <th>活跃值</th>
            <th>学生等级</th>
            <th>家庭等级</th>
        </tr>
        <tbody id="user-records-list">
        <tr>
            <td>${(studentLevel.value)!'--'}</td>
            <td>${(studentLevel.level)!'--'}</td>
            <td>${(homeLevel.level)!'--'}</td>
        </tr>
        </tbody>
    </table>

    <legend>家长等级</legend>
    <table class="table table-bordered">
        <tr>
            <th>家长ID</th>
            <th>身份</th>
            <th>活跃值</th>
            <th>家长等级</th>
        </tr>
        <tbody id="user-records-list">
            <#if parentLevels??>
                <#list parentLevels as parentLevel>
                    <tr>
                        <td>${(parentLevel.parentId)!'--'}</td>
                        <td>${(parentLevel.callName)!'--'}</td>
                        <td>${(parentLevel.level.value)!'--'}</td>
                        <td>${(parentLevel.level.level)!'--'}</td>
                    </tr>
                </#list>
            </#if>
        </tbody>
    </table>

    <legend>最近7天活跃值记录</legend>
    <table class="table table-bordered">
        <tr>
            <th>日期</th>
            <th>行为</th>
            <th>活跃值</th>
            <th>备注</th>
        </tr>
        <tbody>
        <#if logs??>
            <#list logs as log>
                <tr>
                    <td>${(log.date)!'--'}</td>
                    <td>${(log.action)!'--'}</td>
                    <td>${(log.value)!'--'}</td>
                    <td>${(log.ext)!'--'}</td>
                </tr>
            </#list>
        </#if>
        </tbody>
    </table>
</div>

</@layout_default.page>