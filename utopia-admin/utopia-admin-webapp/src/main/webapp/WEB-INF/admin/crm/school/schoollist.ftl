<#import "../../layout_default.ftl" as layout_default/>
<#import "schoolquery.ftl" as schoolQuery/>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="学校查询" page_num=3>
<div id="main_container" class="span10">
    <@headsearch.headSearch/>
    <@schoolQuery.queryPage/>
    <br/>
    <div>
        <#if schoolSnapShotList?has_content>
            <table id="schools" class="table table-hover table-striped table-bordered">
                <tr>
                    <th>学校ID</th>
                    <th>学校名称</th>
                    <th>学校简称</th>
                    <th>所在地区</th>
                    <th>地区编号</th>
                    <th>鉴定状态</th>
                    <th>学校级别</th>
                    <th>vip等级</th>
                    <th>学校类型</th>
                    <th>创建时间</th>
                </tr>

                <#setting datetime_format="yyyy-MM-dd HH:mm"/>
                <#list schoolSnapShotList as schoolSnapShot>
                    <tr>
                        <td>${schoolSnapShot.schoolId!''}</td>
                        <td><a href="schoolhomepage.vpage?schoolId=${schoolSnapShot.schoolId!''}" target="_blank">${schoolSnapShot.schoolName!''}</a></td>
                        <td><a href="schoolhomepage.vpage?schoolId=${schoolSnapShot.schoolId!''}" target="_blank">${schoolSnapShot.shortName!''}</a></td>
                        <td>${schoolSnapShot.regionName!''}</td>
                        <td>${schoolSnapShot.regionCode!''}</td>
                        <td>${schoolSnapShot.authenticationState!''}</td>
                        <td>${schoolSnapShot.schoolLevel!''}</td>
                        <td>${schoolSnapShot.vipLevel!''}</td>
                        <td>${schoolSnapShot.schoolType!''}</td>
                        <td>${schoolSnapShot.createTime!''}</td>
                    </tr>
                </#list>
            </table>
        </#if>
    </div>
</div>
</@layout_default.page>