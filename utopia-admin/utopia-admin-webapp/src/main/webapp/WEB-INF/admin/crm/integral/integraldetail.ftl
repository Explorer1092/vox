<#import "../../layout_default.ftl" as layout_default>
<#if userType == 1>
    <#assign type = "园丁豆" />
<#else>
    <#assign type = "学豆"/>
</#if>
<@layout_default.page page_title="用户${type}详情" page_num=3>
<style>
    .win{color:dodgerblue;}
    .lose{color:red;}
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset class="inline">
            <legend>用户<a href="../user/userhomepage.vpage?userId=${userId!}">${userName!}</a>(${userId})${type}详情 </legend>
        </fieldset>
        <br/>

        时间区间：
        <input id="startDate" type="text" class="input input-small" style="margin-bottom: 0;" value="${startDate!}" placeholder="2016-12-01" maxlength="10">
        至 <input id="endDate" type="text" class="input input-small" style="margin-bottom: 0;" value="${endDate!}" placeholder="2016-12-25" maxlength="10">

        <button class="btn btn-success" onclick="queryHistory()" style="margin-right: 10px;"> 查 询 </button>
        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
            <button class="btn btn-primary" onclick="addIntegral()"> 添 加 </button>
        </#if>
        <#if integralHistoryList?has_content>
            <select id="payType" style="float:right;margin-bottom: 0;margin-right: 10px; width: 150px;" onchange="sumUpHistories()">
                <option value=0>全部记录</option>
                <option value=1>获得记录</option>
                <option value=-1>支出记录</option>
            </select>
        </#if>
        <table id="students" class="table table-hover table-striped table-bordered" style="margin-top: 20px;">
            <thead>
                <tr id="integral_history_title">
                    <th> 创建时间</th>
                    <th> 积分类型</th>
                    <th> ${type}</th>
                    <th> 备 注</th>
                    <th> 关联用户</th>
                    <th> 操作人ID</th>
                </tr>
            </thead>
            <tbody id="integral_history_content">
            <#if integralHistoryList?has_content>
                <#list integralHistoryList as integralHistory>
                    <tr id="integral_history_${integralHistory.id!""}">
                        <td>${integralHistory.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                        <td>${integralHistory.integralType!""}</td>
                        <td class="integral_amount<#if (integralHistory.integral) lt 0> lose<#elseif (integralHistory.integral) gt 0> win</#if>">
                            ${integralHistory.integral!0}
                        </td>
                        <td>${integralHistory.comment!""}</td>
                        <td>
                            <#if integralHistory.relationUserId?? && integralHistory.relationUserId != 0 >
                                <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                <a href="../user/userhomepage.vpage?userId=${integralHistory.relationUserId!""}">${integralHistory.relationUserId!""}</a>
                                <#else>
                                ${integralHistory.relationUserId!""}
                                </#if>
                            <#else>
                                0
                            </#if>
                        </td>
                        <td>${integralHistory.addIntegralUserId!""}</td>
                    </tr>
                </#list>
                <tr>
                    <th colspan="2">累计${type}总数</th>
                    <th colspan="4" id="integral_sumUp" class="<#if (sumUp) lt 0>lose<#elseif (sumUp) gt 0>win</#if>">${sumUp!0}</th>
                </tr>
            </#if>
            <#if error??>
                <tr>
                    <td colspan="6" style="text-align: center;">${error!}</td>
                </tr>
            </#if>
            </tbody>
        </table>
    </div>
    <div id="integral_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户积分修改</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户 ID</dt>
                        <dd>${userId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd><#if userType == 1>老师<#else>学生</#if>操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>类型</dt>
                        <dd>
                            <select class="multiple" name="integralType" id="integralType">
                                <#list integralTypeList as integralType>
                                    <option value='${integralType.type!''}'>${integralType.description!''}</option>
                                </#list>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>${type}</dt>
                        <dd><input type="text" name="integral" id="integral" placeholder="只能是数字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备注</dt>
                        <dd><textarea id="comment" name="comment" cols="35" rows="3" value=""></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>增加用户${type}</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary"
                    onclick="$('#integral_dialog').modal('hide');$('#dialog-confirm').modal('show')">确 定
            </button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="dialog-confirm" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>提 示</h3>
        </div>
        <div class="modal-body">
            <span>是否继续修改？</span>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" onclick="change()">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<script type="text/javascript">

    $('#startDate').datepicker({
        maxDate: 0,
        dateFormat: "yy-mm-dd",
        onSelect: function(dateText){
            $('#endDate').datepicker("option","minDate",dateText);
        }
    });
    $('#endDate').datepicker({
        maxDate: 0,
        dateFormat: 'yy-mm-dd',
        onSelect: function(dateText){
            $('#startDate').datepicker("option","maxDate",dateText);
        }
    });

    function addIntegral() {
        $("#integral").val('');
        $("#comment").val('');
        $("#integral_dialog").modal("show");
    }

    function queryHistory() {
        var queryStr = '?userId=${userId!}';
        var $start = $('#startDate').val();
        var $end = $('#endDate').val();
        if (($start != '' && $end == '') || ($start == '' && $end != '')) {
            alert("请正确填写日期区间");
            return false;
        }
        if ($start != '' && $end != '') {
            queryStr += '&start=' + $start;
            queryStr += '&end=' + $end;
        }
        location.href = queryStr;
    }

    function sumUpHistories() {
        var payType = parseInt($('#payType').find('option:selected').val());
        var sumUp = 0;
        $('.integral_amount').each(function () {
            var amount = parseInt($(this).html());
            if (payType * amount < 0) {
                $(this).parent().hide();
            } else {
                $(this).parent().show();
                sumUp += amount;
            }
        });
        var $sp = $('#integral_sumUp');
        $sp.html(sumUp);
        $sp.removeClass("win");
        $sp.removeClass("lose");
        if (sumUp > 0) {
            $sp.addClass("win");
        } else if (sumUp < 0) {
            $sp.addClass("lose");
        }
    }

    function change() {
        var queryUrl = "addintegralhistory.vpage";
        $.ajax({
            type: "post",
            url: queryUrl,
            data: {
                userId: ${userId!''},
                integral: $("#integral").val(),
                integralType: $("#integralType").val(),
                comment: $("#comment").val()
            },
            success: function (data) {
                if (data.success) {
                    location.href = '?userId=${userId}';
                } else {
                    alert("增加积分失败，请检查是否正确填写数量和备注。");
                }
                $("#dialog-confirm").modal('hide');
            }
        });
    }
</script>
</@layout_default.page>