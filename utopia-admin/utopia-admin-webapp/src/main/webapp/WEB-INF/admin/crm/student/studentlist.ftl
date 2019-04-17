<#-- @ftlvariable name="parentInfo.isKeyParent" type="boolean" -->
<#-- @ftlvariable name="parentInfo.isAuthenticated" type="boolean" -->
<#-- @ftlvariable name="parentInfo.mobile" type="String" -->
<#-- @ftlvariable name="parentInfo.realName" type="String" -->
<#-- @ftlvariable name="parentInfo.id" type="Long" -->
<#-- @ftlvariable name="student.parentsInfo" type="java.util.List" -->
<#-- @ftlvariable name="student.teacherMobile" type="String" -->
<#-- @ftlvariable name="student.teacherName" type="String" -->
<#-- @ftlvariable name="student.teacherId" type="Long" -->
<#-- @ftlvariable name="student.className" type="String" -->
<#-- @ftlvariable name="student.classLevel" type="Integer" -->
<#-- @ftlvariable name="student.classId" type="Long" -->
<#-- @ftlvariable name="student.schoolName" type="String" -->
<#-- @ftlvariable name="student.studentName" type="String" -->
<#-- @ftlvariable name="student.schoolId" type="Long" -->
<#-- @ftlvariable name="student.studentId" type="Long" -->
<#-- @ftlvariable name="studentList" type="java.util.List" -->
<#import "../../layout_default.ftl" as layout_default>
<#import "studentquery.ftl" as studentQuery>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="学生查询" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div id="main_container" class="span9">
    <@headsearch.headSearch/>
    <@studentQuery.queryPage/>
    <#-- 学生精确查询 / 学生模糊查询模块 查询到的学生数据模块 -->
    <#if studentList?has_content>
    <div class="table_soll">
        <table class="table table-striped table-bordered" id="students">
            <tr>
                <th>I D</th>
                <th>姓 名</th>
                <th>学 校</th>
                <th>年级班级</th>
                <th>班级创建者</th>
                <th>班级创建者手机</th>
                <th>家长信息</th>
            </tr>

            <#list studentList as student>
                <tr>
                    <td>${student.studentId!""}</td>
                    <td><a href="studenthomepage.vpage?studentId=${student.studentId!""}" target="_blank"> ${student.studentName!''}</a></td>
                    <td><a href="../school/schoolhomepage.vpage?schoolId=${student.schoolId!}" target="_blank">${student.schoolName!''}</a>(${student.schoolId!})</td>
                    <td>
                        <#if student.classId??>
                            <a href="../clazz/groupinfo.vpage?studentId=${student.studentId!}" target="_blank">${student.classLevel!''}年级 ${student.className!''}</a>(${student.classId!})</td>
                        <#else>
                            ${student.classLevel!''}年级 ${student.className!''}
                        </#if>
                    <td>
                        <#if student.teacherId??>
                            <a href="../teachernew/teacherdetail.vpage?teacherId=${student.teacherId}" target="_blank">${student.teacherName!''}</a>(${student.teacherId!""}) </td>
                        <#else>
                            ${student.teacherName!''}
                        </#if>
                    <td><#if student.teacherId??><button id="query_user_phone_${student.teacherId!''}" class="btn btn-info">查 看</button></#if></td>
                    <td style="padding: 0;">
                        <table class="table table-striped table-hover" style="margin: 0;">
                            <#list student.parentsInfo as parentInfo>
                            <tr>
                                <td>
                                    <#if (parentInfo.id)??>
                                        <a href="../parent/parenthomepage.vpage?parentId=${parentInfo.id}">${(parentInfo.realName?html)!}</a>(${parentInfo.id})
                                    <#else>
                                        ${(parentInfo.realName?html)!}()
                                    </#if>
                                </td>
                                <td>${parentInfo.isAuthenticated?string('Bind', 'NoBind')}</td>
                                <td>${parentInfo.isKeyParent?string('Keyparent', 'NoKeyParent')}</td>
                                <td style="margin: 0 0 0 0;"><#if (parentInfo.id)??>tel:<button type="button" id="query_user_phone_${parentInfo.id!''}" class="btn btn-info">查 看</button></#if></td>
                            </tr>
                            </#list>
                        </table>
                    </td>
                </tr>
            </#list>
        </table>
    </div>
    </#if>

    <#-- klxId查询模块 查询到的学生数据模块（数据来自studentquery.ftl中接口） -->
    <div class="table_soll" id="klxStuInfoTable" style="display: none;">
        <h4>快乐学学生：</h4>
        <table class="table table-striped table-bordered">
            <tr>
                <th>I D</th>
                <th>姓 名</th>
                <th>学 校</th>
                <th>年级班级</th>
                <th>班级创建者</th>
                <th>班级创建者手机</th>
            </tr>
            <tr>
                <td>
                    <span>{{klxStuInfo.studentId}}</span>
                </td>
                <td>
                    <#--17ID存在则姓名可外链-->
                    <span v-if="klxStuInfo.studentId"><a :href="'studenthomepage.vpage?studentId=' + klxStuInfo.studentId" target="_blank">{{klxStuInfo.studentName}}</a></span>
                    <#--17ID不存在只展示姓名-->
                    <span v-else>{{klxStuInfo.studentName}}</span>
                </td>
                <td>
                    <span v-if="klxStuInfo.schoolId"><a :href="'../school/schoolhomepage.vpage?schoolId=' + klxStuInfo.schoolId" target="_blank">{{klxStuInfo.schoolName}}</a>({{klxStuInfo.schoolId}})</span>
                </td>
                <td>
                    <span v-if="klxStuInfo.classId"><a :href="'../clazz/groupinfo.vpage?clazzId=' + klxStuInfo.classId" target="_blank">{{klxStuInfo.classLevel + '年级 ' + klxStuInfo.className}}</a>({{klxStuInfo.classId}})</span>
                </td>
                <td>
                    <span v-if="klxStuInfo.teacherId"><a :href="'../teachernew/teacherdetail.vpage?teacherId=' + klxStuInfo.teacherId" target="_blank">{{klxStuInfo.teacherName}}</a>({{klxStuInfo.teacherId}})</span>
                </td>
                <td>
                    <span v-if="klxStuInfo.teacherId"><button :id="'query_user_phone_' + klxStuInfo.teacherId" class="btn btn-info">查看</button></span>
                </td>
            </tr>
        </table>
    </div>
</div>
</@layout_default.page>