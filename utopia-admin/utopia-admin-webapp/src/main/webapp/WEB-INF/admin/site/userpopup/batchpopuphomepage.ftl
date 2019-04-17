<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <legend>
        <a href="userpopuplist.vpage">弹窗广告</a> &nbsp;&nbsp;
        批量弹窗&nbsp;&nbsp;
        <a href="globaluserpopup.vpage">全局弹窗</a> &nbsp;&nbsp;
    </legend>

    <form method="post" action="batchpopupsend.vpage">
        <ul class="inline">
            <li>
                <label>输入弹窗消息内容：<textarea name="content" cols="45" rows="10" placeholder="请在这里输入要发送的用户ID及内容"></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>
                    发送时间
                    <input name="nextDateTime" type="text" placeholder="格式：2012-12-22 00:00"/>
                </label>
            </li>
            <li>
                <input class="btn" type="submit" value="提交" />
            </li>
        </ul>
    </form>
    <div>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>发送成功：</td><td><#if successlist??>${successlist?size}</#if>件</td>
                <td>发送失败：</td><td><#if failedlist??>${failedlist?size}</#if>件</td>
            </tr>
        </table>
        <label>失败记录：</label>
        <table class="table table-bordered">
            <#if failedlist??>
                <#list failedlist as l>
                    <tr>
                        <td>${l}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
<script>

$(function(){
    $.fn.datetimepicker.dates['zh-CN'] = {
        days        : ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
        daysShort   : ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
        daysMin     : ["日", "一", "二", "三", "四", "五", "六", "日"],
        months      : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        monthsShort : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        today       : "今日",
        suffix      : [],
        meridiem    : []
    };
    $('[name="nextDateTime"]').datetimepicker({
        language:  'zh-CN',
        weekStart: 1,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        forceParse: 0,
        showMeridian: 1
    });
});
</script>
</@layout_default.page>
