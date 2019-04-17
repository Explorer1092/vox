<#import "../../layout_default.ftl" as layout_default>
<#import "faultorderquery.ftl" as faultorderquery>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="用户跟踪查询" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div id="main_container" class="span9">
    <@headsearch.headSearch/>
    <@faultorderquery.queryPage/>
    <#if faultOrderList?has_content>
        <div class="table_soll">
            <table class="table table-striped table-bordered">
                <tr>
                    <td>创建时间</td>
                    <td>创建人</td>
                    <td>创建备注</td>
                    <td>用户角色</td>
                    <td>用户姓名（ID）</td>
                    <td>当前状态</td>
                    <td>故障类型</td>
                    <td>关闭时间</td>
                    <td>操作人</td>
                    <td>关闭备注</td>
                    <td>操作</td>
                </tr>
                <#list faultOrderList as faultOrder>
                    <tr>
                        <td>${faultOrder.createTime!''}</td>
                        <td>${faultOrder.creator!''}</td>
                        <td>${faultOrder.createInfo!''}</td>
                        <td>${faultOrder.userType!''}</td>
                        <td>${faultOrder.userName!''}
                            <#if faultOrder.userType == 'PARENT'>
                                （<a href="/crm/parent/parenthomepage.vpage?parentId=${faultOrder.userId!''}">${faultOrder.userId!''}</a>）
                            </#if>
                            <#if faultOrder.userType == 'TEACHER'>
                                （<a href="/crm/teacher/teacherhomepage.vpage?teacherId=${faultOrder.userId!''}">${faultOrder.userId!''}</a>）
                            </#if>
                            <#if faultOrder.userType == 'STUDENT'>
                                （<a href="/crm/student/studenthomepage.vpage?studentId=${faultOrder.userId!''}">${faultOrder.userId!''}</a>）
                            </#if>
                        </td>
                        <td><#if faultOrder.status == 0>追踪中<#else>关闭</#if></td>
                        <td>
                            <#if faultOrder.faultType == 1> 用户登录（PC）</#if>
                            <#if faultOrder.faultType == 2>用户登录（APP）</#if>
                            <#if faultOrder.faultType == 3>绑定手机</#if>
                            <#if faultOrder.faultType == 4>提交作业</#if>
                            <#if faultOrder.faultType == 5>作业录音</#if>
                        </td>
                        <td>${faultOrder.closeTime!''}</td>
                        <td>${faultOrder.closer!''}</td>
                        <td>${faultOrder.closeInfo!''}</td>
                        <td><#if faultOrder.status == 0><a onclick=closeOrder("${faultOrder.id!''}")>关闭</a></#if></td>
                    </tr>
                </#list>
            </table>

            <ul class="pager">
                <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                <#if conditionMap.page?number gt 1>
                    <li><a href="#" onclick="pagePost(${conditionMap.page?number-1})" title="Pre">&lt;</a></li>
                <#else>
                    <li class="disabled"><a href="#">&lt;</a></li>
                </#if>
                <li class="disabled"><a>第 ${conditionMap.page!} 页</a></li>
                <li class="disabled"><a>共 <#if totalPage?number==0>1<#else>${totalPage!}</#if> 页</a></li>
                <#if totalPage?number gt conditionMap.page?number>
                    <li><a href="#" onclick="pagePost(${conditionMap.page?number+1})" title="Next">&gt;</a></li>
                <#else>
                    <li class="disabled"><a href="#">&gt;</a></li>
                </#if>
            </ul>
        </div>
    </#if>
</div>

<div id="close_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>关闭追踪项</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>关闭备注</dt>
                    <input type="hidden" id="current_log_id" />
                    <dd><textarea id="closeInfo" cols="35" rows="5"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="dialog_close_order" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script>
    function closeOrder(id){
        $("#closeInfo").val('');
        $("#close_dialog").modal("show");
        $("#current_log_id").val(id);

    }

    $(function(){

        $("#dialog_close_order").on("click", function () {
            var faultTypes = [];
            $("input[type=checkbox][name=faultType]:checked").each(function (index, domEle) {
                var ckBox = $(domEle);
                faultTypes.push(ckBox.val());
            });

            var queryUrl = "/crm/faultOrder/closeRecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    id:$("#current_log_id").val(),
                    closeInfo:$("#closeInfo").val()
                },
                success: function (data) {
                    if (data.success) {
                        $("#current_log_id").val('');
                        $("#closeInfo").val('');
                        $("#close_dialog").modal("hide");
                        pagePost(1);
                    } else {
                        alert("添加跟踪记录失败。");
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>