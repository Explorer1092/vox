<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend>家长报名查询</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <div>
            <span style="white-space: nowrap;">
                家长手机号：<input type="text" id="parentMobile" name="parentMobile" value="${parentMobile!''}"/>
            </span>
            <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
                <option value="all">全部</option>
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}"
                                <#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
            </select>
            </span>
            <span style="white-space: nowrap;">
                <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
            </span>
        </div>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长ID</th>
                        <th>课程ID</th>
                        <th>课程名称</th>
                        <th>报名时分配的个人微信号</th>
                        <th>报名时间</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if joinRecord?? && joinRecord?size gt 0>
                            <#list joinRecord as bean>
                                 <tr>
                                     <td>${bean.parentId!''}</td>
                                     <td>${bean.studyLessonId!''}</td>
                                     <td>${bean.lessonName!''}</td>
                                     <td>${bean.wechatNumber!''}</td>
                                     <td>${bean.createDate!''}</td>
                                 </tr>
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="4" style="text-align: center">${info!''}</td>
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
        $("#searchBtn").on('click', function () {
            $("#op-query").submit();
        });
    });
</script>
</@layout_default.page>