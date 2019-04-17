<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div class="span9">
    <fieldset>
        <legend>班长、辅导员历史班级管理记录</legend>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课程名称</th>
                        <th>期数</th>
                        <th>年级</th>
                        <th>课程开始时间</th>
                        <th>课程结束时间</th>
                        <th>管理的微信群</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if returnLessonList?? && returnLessonList?size gt 0>
                            <#list returnLessonList as  lesson>
                            <tr>
                                <td>${lesson.title!''}</td>
                                <td>${lesson.phase!''}</td>
                                <td>${lesson.getSuitableGradeText()!''}</td>
                                <td>${lesson.openDate!''}</td>
                                <td>${lesson.closeDate!''}</td>
                                <td id="${lesson.lessonId!''}"> <#if groupListMap["${lesson.lessonId!''}"]?? && groupListMap["${lesson.lessonId!''}"]?size gt 0>
                                <#list groupListMap["${lesson.lessonId!''}"] as  group>
                                    <p id="${group.id!''}" class="kol_p"> ${group.wechatGroupName!''}</p>
                                </#list>
                                <#else>
                                </#if></td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {
    });

</script>
</@layout_default.page>