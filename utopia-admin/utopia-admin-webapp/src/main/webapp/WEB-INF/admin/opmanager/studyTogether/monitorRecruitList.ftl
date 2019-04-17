<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    p {
        margin: 0 0 10px;
        font-size: 10px;
        line-height: 0.1px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>班长招募课程控制</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
    <#--<input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>-->
    <#--<span style="white-space: nowrap;">-->
    <#--课程ID：<select id="selectLessonId" name="selectLessonId">-->
    <#--<#if lessonIds?? && lessonIds?size gt 0>-->
    <#--<#list lessonIds as lessonId>-->
    <#--<option value="${lessonId}"-->
    <#--<#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>-->
    <#--</#list>-->
    <#--<#else>-->
    <#--<option value="">暂无数据</option>-->
    <#--</#if>-->
    <#--</select>-->
    <#--</span>-->
    <#--<span style="white-space: nowrap;">-->
    <#--家长Id：<input type="text" id="searchParentId" name="searchParentId" value="${parentId!''}"/>-->
    <#--</span>-->
    <#--<span style="white-space: nowrap;">-->
    <#--学习群号：<input type="text" id="wechatGroupName" name="wechatGroupName" value="${wechatGroupName!''}"/>-->
    <#--</span>-->
    <#--<span style="white-space: nowrap;">-->
    <#--微信号：<input type="text" id="userWechatName" name="userWechatName" value="${userWechatName!''}"/>-->
    <#--</span>-->
    <#--<span style="white-space: nowrap;">-->
    <#--状态：<select id="selectStatus" name="selectStatus">-->
    <#--<option value="1">待审核</option>-->
    <#--<option value="2">审核未通过</option>-->
    <#--<option value="3">审核通过</option>-->
    <#--<option value="4">离职</option>-->
    <#--<option value="5">休整</option>-->
    <#--</select>-->
    <#--</span>-->
    </form>
<#--<button class="btn btn-primary" type="button" id="searchBtn">查询</button>-->

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课程ID</th>
                        <th>课程名称</th>
                        <th>期数</th>
                    <#--<th>班长人数</th>-->
                    <#--<th>辅导员人数</th>-->
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  monitor>
                            <tr>
                                <td>${monitor.lessonId!''}</td>
                                <td>${monitor.title!''}</td>
                                <td>${monitor.phase!''}</td>
                            <#--<td>${monitor.wechatGroupName!''}</td>-->
                            <#--<td>${monitor.createDate!''}</td>-->
                                <td>
                                    <#if !monitor.status>
                                        <a class="btn btn-success" name="start"
                                           data-lesson_id="${monitor.lessonId!''}" disabled="disabled"
                                           data-no_click="true">开始
                                        </a>
                                        <a class="btn btn-danger" name="stop"
                                           data-lesson_id="${monitor.lessonId!''}" data-no_click="false">结束
                                        </a>
                                    <#else>
                                        <a class="btn btn-success" name="start"
                                           data-lesson_id="${monitor.lessonId!''}" data-no_click="false">开始
                                        </a>
                                    <a class="btn btn-danger" name="stop"
                                       data-lesson_id="${monitor.lessonId!''}" disabled="disabled" data-no_click="true">结束
                                    </a>
                                    </#if>

                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>

                <ul class="message_page_list">
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {

    <#--$(".message_page_list").page({-->
    <#--total: ${totalPage!},-->
    <#--current: ${currentPage!},-->
    <#--autoBackToTop: false,-->
    <#--maxNumber: 20,-->
    <#--jumpCallBack: function (index) {-->
    <#--$("#pageNum").val(index);-->
    <#--$("#op-query").submit();-->
    <#--}-->
    <#--});-->

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        $("a[name='start']").on('click', function () {
            var noClick = $(this).data("no_click");
            if (noClick) {
                return;
            }
            var lessonId = $(this).data("lesson_id");
            var status = false;
            $.ajax({
                url: 'save_monitor_recruit_status.vpage',
                type: 'POST',
                data: {lessonId: lessonId, status: status},
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                        console.log("data error");
                    }
                }
            });
        });
        $("a[name='stop']").on('click', function () {
            var noClick = $(this).data("no_click");
            if (noClick) {
                return;
            }
            var lessonId = $(this).data("lesson_id");
            var status = true;
            $.ajax({
                url: 'save_monitor_recruit_status.vpage',
                type: 'POST',
                data: {lessonId: lessonId, status: status},
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                        console.log("data error");
                    }
                }
            });
        });
    });

</script>
</@layout_default.page>