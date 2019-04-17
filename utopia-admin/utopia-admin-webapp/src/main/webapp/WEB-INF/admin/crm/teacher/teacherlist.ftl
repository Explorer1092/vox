<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<#import "teacherquery.ftl" as teacherQuery>
<#import "teachermobilepass.ftl" as teacherMobilePass>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="老师查询" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div class="span9">
    <@headsearch.headSearch/>
    <@teacherQuery.queryConditons/>
    <@teacherMobilePass.queryConditons/>
    <#if teacherList?has_content>
        <div class="table_soll">
            <table id="teachers" class="table table-hover table-striped table-bordered">
                <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>手机</th>
                    <#--<th>邮箱</th>-->
                    <th>学科</th>
                    <th>学校</th>
                    <th>省市区</th>
                    <th>园丁豆</th>
                    <th>是否认证</th>
                    <th>申请状态更新时间</th>
                </tr>

                <#list teacherList as teacher>
                    <tr>
                        <td>${teacher.teacherId!""}</td>
                        <td><a href="teacherhomepage.vpage?teacherId=${teacher.teacherId!""}" target="_blank">${teacher.teacherName!''}</a></td>
                        <td><#if teacher.teacherId??><button type="button" id="query_user_phone_${teacher.teacherId!''}" class="btn btn-info">查 看</button></#if></td>
                        <#--<td><#if teacher.teacherId??><button type="button" id="query_user_email_${teacher.teacherId!''}" class="btn btn-info">查 看</button></#if></td>-->
                        <td>${(teacher.subjectName)!''}</td>
                        <td><a href="../school/schoolhomepage.vpage?schoolId=${teacher.schoolId!''}" target="_blank">${teacher.schoolName!''}</a>（${teacher.schoolId!''}）</td>
                        <td>${teacher.regionName!''}（${teacher.regionCode!''}）</td>
                        <td><a href="../integral/integraldetail.vpage?userId=${teacher.teacherId!""}">${teacher.integral!''}</a></td>
                        <td>${teacher.verifiedState!''}</td>
                        <td>${teacher.applyDate!}</td>
                    </tr>
                </#list>
            </table>
        </div>
    </#if>
</div>
</@layout_default.page>