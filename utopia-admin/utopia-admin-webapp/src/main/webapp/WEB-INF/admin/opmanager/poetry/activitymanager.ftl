<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style type="text/css">
    [v-cloak] { display: none }
</style>
<div class="span9">
    <fieldset>
        <legend>活动配置</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${page!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                ID：<input type="text" id="activityId" name="activityId" style="width: 100px;" value="${activityId!''}"/>
            </span>
            <span style="white-space: nowrap;">
                活动名称：<input type="text" id="activityName" name="activityName" value="${activityName!''}"/>
            </span>
            <span style="white-space: nowrap;">
                活动年级：<input type="text" id="activityLevel" name="activityLevel" value="${activityLevel!''}"/>
            </span>
            <span style="white-space: nowrap;">
                活动状态：<select id="activityStatus" name="activityStatus" style="width: 150px;">
                                <option value="" <#if activityStatus?? && activityStatus == ''> selected="selected"</#if>>请选择</option>
                                <option value="true" <#if activityStatus?? && activityStatus == 'true'> selected="selected"</#if>>已下线</option>
                                <option value="false" <#if activityStatus?? && activityStatus == 'false'> selected="selected"</#if>>已上线</option>
                           </select>
            </span>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
        </div>
    </form>
    <a class="btn btn-primary" target="_blank" href="/opmanager/poetry/activity_create_or_view.vpage?edit=1">新建活动</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="width: 150px;">活动地区</th>
                        <th style="width: 100px;">活动ID</th>
                        <th>活动名称</th>
                        <th>开始时间</th>
                        <th>结束时间</th>
                        <th>关卡数</th>
                        <th>活动年级</th>
                        <th>状态</th>
                        <th style="width: 68px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if activityList?? && activityList?size gt 0>
                            <#list activityList as activityObj>
                            <tr>
                                <td>${activityObj.regions!''}</td>
                                <td>${activityObj.activityId!''}</td>
                                <td>${activityObj.name!''}</td>
                                <td>${activityObj.startDate!''}</td>
                                <td>${activityObj.endDate!''}</td>
                                <td>${activityObj.missionSize!0}</td>
                                <td><#list activityObj.classLevel as item>${item}&nbsp;|&nbsp;</#list></td>
                                <td class="status"><#if activityObj.status?has_content && activityObj.status>已下线<#else>已上线</#if></td>
                                <td>
                                    <a class="btn btn-primary" style="margin-bottom: 5px;" target="_blank" href="/opmanager/poetry/activity_create_or_view.vpage?activityId=${activityObj.activityId!''}">详情</a>
                                    <a class="btn btn-success" style="margin-bottom: 5px;" target="_blank" href="/opmanager/poetry/activity_create_or_view.vpage?edit=1&activityId=${activityObj.activityId!''}">修改</a>
                                    <a class="btn btn-warning changeActivityStatus" style="margin-bottom: 5px;" data-id="${activityObj.activityId!''}" data-value="false" href="javascript;void(0);">上线</a>
                                    <a class="btn btn-warning changeActivityStatus" style="margin-bottom: 5px;" data-id="${activityObj.activityId!''}" data-value="true" href="javascript;void(0);">下线</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="9" style="text-align: center">暂无数据</td>
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
        $(".message_page_list").page({
            total: ${totalPage!1},
            current: ${page!1},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });


        $(".changeActivityStatus").on("click",function(){
            var $this = $(this);
            var activityId = $this.attr("data-id");
            var activityStatus = $this.attr("data-value");
             $.post("/opmanager/poetry/update_ancient_poetry_activity_status.vpage",{
                 activity_id: activityId,
                 status : activityStatus
             }).done(function(res){
                 if(res.success){
                     var statusText = activityStatus === "false" ? "已上线" : "已下线";
                     $this.parent().siblings(".status").text(statusText);
                     alert("更新成功");
                 }else{
                     alert("更新失败，请刷新重试");
                 }
             }).fail(function(e){
                 alert("网络错误，请刷新重试");
             });
             return false;
        });
    });
</script>
</@layout_default.page>