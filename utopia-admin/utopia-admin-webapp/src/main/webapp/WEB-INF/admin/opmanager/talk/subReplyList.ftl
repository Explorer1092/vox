<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        观点列表
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" method="get" action="?">
                <ul class="inline">
                    <li>
                        <label>话题状态</label>
                        <select name="auditType" id="auditType">
                            <option value="0">全部</option>
                            <option value="1" <#if auditType?exists && auditType == 1>selected</#if>>未审</option>
                            <option value="2" <#if auditType?exists && auditType == 2>selected</#if>>通过</option>
                            <option value="3" <#if auditType?exists && auditType == 3>selected</#if>>拒绝</option>
                            <option value="4" <#if auditType?exists && auditType == 4>selected</#if>>隐藏</option>
                        </select>
                    </li>
                    <li>
                        <label>发布时间&nbsp;
                            <input id="startTime" name="startTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="起始" value="${startTime!''}">
                            至
                            <input id="endTime" name="endTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="结束" value="${endTime!''}">
                        </label>
                    </li>
                    <li>
                        <button type="submit" id="filter" class="btn btn-primary">查 询</button>
                    </li>
                </ul>
                <ul class="inline" style="height: 20px;">
                    <button type="button" class="btn btn-primary" style="float: right; margin-right: 10px;" id="btn_clear">隐藏本页未处理的话题</button>
                </ul>
            </form>

        </div>

        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>评论id</th>
                <th style="width:400px;">评论内容</th>
                <th>评论时间</th>
                <th>评论用户id</th>
                <th>用户立场</th>
                <th>审核状态</th>
                <th>审核时间</th>
                <th>审核人</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <#if replies?exists>
                <#list replies as reply>
                <tr>
                    <td>${reply.replyId!''}</td>
                    <td style="word-break:normal;word-wrap:break-word;">${reply.concept!''}</td>
                    <td>${reply.createTime!''}</td>
                    <td>${reply.userType}-${reply.userId!''}</td>
                    <td>${reply.optionTitle!''}</td>
                    <td>${reply.auditStatus!''}</td>
                    <td>${reply.auditTime!''}</td>
                    <td>${reply.auditName!''}</td>
                    <td data-status="${reply.auditStatus}" data-id="${reply.replyId}">
                        <button data-audit="pass" data-id="${reply.replyId}">通过</button><button data-audit="reject" data-id="${reply.replyId}">拒绝</button>
                        </button>
                    </td>
                </tr>
                </#list>
            </#if>
            </tbody>
        </table>
        <ul class="pager" data-index="${pageIndex}">
            <#if pageCount?exists && pageCount gt 0>
                <#if pageIndex?exists && pageIndex gt 10>
                    <li data-index="1"><a href="${query}1">首页</a></li>
                    <li data-index="${start - 1}"><a href="${query}${start - 1}">前十页</a></li>
                </#if>
                <#list start .. end as page>
                    <li data-index="${page}"><a href="${query}${page}">${page}</a></li>
                </#list>
                <#if pageIndex?exists && pageCount gt 10 && pageIndex lt (pageCount / 10) * 10>
                    <li data-index="${end + 1}"><a href="${query}${end + 1}">后十页</a></li>
                    <li data-index="${pageCount}"><a href="${query}${pageCount}">尾页</a></li>
                </#if>
            </#if>

        </ul>
    </div>
</div>
<div id="reject" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">审核不通过的原因</h4>
            </div>
            <div class="modal-body">
                <p>
                    <div class="form-inline">
                        <div class="radio">
                            <label><input type="radio" class="checkbox" name="reason" value="色情"/>色情</label>
                        </div>
                        <div class="radio">
                            <label><input type="radio" class="checkbox" name="reason" value="政治"/>政治</label>
                        </div>
                        <div class="radio">
                            <label><input type="radio" class="checkbox" name="reason" value="广告"/>广告</label>
                        </div>
                        <div class="radio">
                            <label><input type="radio" class="checkbox" name="reason" value="其他"/>其他</label>
                        </div>
                    </div>
                    <input type="hidden" id="currentReplyId" value=""/>
                    <div class="form-group" style="display: block;">
                        <textarea name="extend" style="width: 90%;" class="form-control" rows="3"></textarea>
                    </div>
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" id="audit-close" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="btn_reject_ok">确定</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<script lang="javascript">
    $(document).ready(function () {

        $("td[data-status='通过'] button[data-audit='pass']").attr("disabled", "disabled");
        $("td[data-status='拒绝'] button[data-audit='reject']").attr("disabled", "disabled");

        $("td[data-status='拒绝'] button[data-action='choice']").attr("disabled", "disabled");
        $("td[data-status='未审核'] button[data-action='choice']").attr("disabled", "disabled");

        var index = $(".pager").attr("data-index");
        $(".pager li").each(function () {
            if($(this).attr("data-index") == index){
                $(this).addClass("disabled");
            }
        });

        $(".form_datetime").datetimepicker({
            autoclose: true,
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii:ss',
        });
        //审核通过
        $("[data-audit='pass']").click(function () {
            var id = $(this).attr("data-id");
            $.ajax({
                url: "auditReply.vpage",
                type: "post",
                dataType: "json",
                data: {"replyId": id, "pass": true},
                success: function (data) {
                    $("button[data-id='"+id+"']").parent().attr("data-status", "通过");
                    $("button[data-id='" + id + "'][data-audit='pass']").attr("disabled", "disabled");
                    $("button[data-id='" + id + "'][data-audit='reject']").removeAttr("disabled");
                    $("button[data-id='"+id+"'][data-action='choice']").removeAttr("disabled");
                }
            });
        });
        $("button[data-audit='reject']").click(function () {
            $('#reject').modal('show');
            $('#currentReplyId').val($(this).attr("data-id"));
        });
        $("#audit-close").click(function () {
            $('#reject').modal('hide');
        });
        //拒绝
        $("#btn_reject_ok").click(function () {
            var reason = $("input[name='reason']:checked").val();
            if(reason == '其他'){
                reason = $("[name='extend']").val();
            }
            var replyId = $("#currentReplyId").val();

            $.ajax({
                url: "auditReply.vpage",
                type: "post",
                dataType: "json",
                data: {"replyId": replyId, "reason": reason, "pass": false},
                success: function (data) {
                    $('#reject').modal('hide');
                    $("button[data-id='"+replyId+"']").parent().attr("data-status", "拒绝");
                    $("button[data-id='" + replyId + "'][data-audit='reject']").attr("disabled", "disabled");
                    $("button[data-id='" + replyId + "'][data-audit='pass']").removeAttr("disabled");
                    $("button[data-id='"+replyId+"'][data-action='choice']").attr("disabled", "disabled");
                }
            });
        });

        $("#btn_clear").click(function () {
            var list = $("td[data-status='未审核']");
            var length = list.length;
            if(length == 0){
                return;
            }
            var ids = "";
            for(var i = 0; i < length; i++){
                ids += ",";
                ids += list.eq(i).attr("data-id");
            }

            ids = ids.substring(1, ids.length);

            $.ajax({
                url: "hidereply.vpage",
                type: "post",
                dataType: "json",
                data: {"replyIds": ids},
                success: function (data) {
                    document.location.reload();
                }
            });

        });
    });
</script>

</@layout_default.page>