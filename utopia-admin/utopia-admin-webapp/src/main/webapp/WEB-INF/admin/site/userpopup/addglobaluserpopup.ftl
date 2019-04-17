<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">

    <legend>
        <a href="userpopuplist.vpage">弹窗广告</a> &nbsp;&nbsp;
        <a href="batchpopuphomepage.vpage">批量弹窗</a> &nbsp;&nbsp;
        全局弹窗
    </legend>

    <form method="post" action="saveglobalpopup.vpage">
        <ul class="inline">
            <li>
                <label>消息标题：<input name="title" type="text" <#if popupItem??> value="${popupItem.title}" <#else> placeholder="输入消息标题, 便于管理" </#if>> </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>消息内容：<textarea name="content" cols="60" rows="10"  placeholder="请在这里输入要发送的内容, HTML格式"><#if popupItem??>${popupItem.content!}</#if></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>发送对象：</label>
            </li>
            <#list allRuleMap as popupRule>
                <#assign ruleName = popupRule.name() />
                <#assign checked = false />
                <#if popupItem?? && popupItem.popupRules??>
                    <#assign checked = popupItem.popupRules?contains(ruleName) />
                </#if>
                <li>
                    <input type="checkbox" name="popupRule" value="${ruleName}" <#if checked>checked</#if>/> ${popupRule.showName!} &nbsp; &nbsp;
                </li>
            </#list>
        </ul>
        <ul class="inline">
            <li>
                <label>
                    开始时间：
                    <input name="startDatetime" type="text" placeholder="格式：2012-12-22 00:00" <#if popupItem?? && popupItem.startDatetime??>value="${popupItem.startDatetime?string("yyyy-MM-dd HH:mm")}"</#if>/>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>
                    结束时间：
                    <input name="endDatetime" type="text" placeholder="格式：2012-12-22 00:00" <#if popupItem?? && popupItem.endDatetime??>value="${popupItem.endDatetime?string("yyyy-MM-dd HH:mm")}" </#if> />
                </label>
            </li>
        </ul>
        <input type="hidden" name="popupId" value="<#if popupItem??>${popupItem.id!}</#if>"/>
        <ul class="inline">
            <li>
                <input class="btn" type="submit" value="保存" /> &nbsp;&nbsp;
                <#if popupItem?? && popupItem.id gt 0>
                    <a href="delglobaluserpopup.vpage?popupId=${popupItem.id}">删除</a> &nbsp;&nbsp;
                </#if>
                <a href="globaluserpopup.vpage">取消</a>
            </li>
        </ul>
    </form>
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
        $('[name="startDatetime"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
        $('[name="endDatetime"]').datetimepicker({
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
