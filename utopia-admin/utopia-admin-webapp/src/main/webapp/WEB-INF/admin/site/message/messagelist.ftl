<#-- @ftlvariable name="adminId" type="java.lang.String" -->
<#-- @ftlvariable name="totalPageNum" type="java.lang.Integer" -->
<#-- @ftlvariable name="pageNum" type="java.lang.Integer" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <fieldset>
        <legend>系统消息查询</legend>
    </fieldset>
    <form action="?" method="post">
        <ul class="inline">
            <li>
                <label>管理员ID:<input type="text" name="adminId" value="${adminId!}"/></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>全部<input type="checkbox" name="allAdminUsers"
                                <#if allAdminUsers??>checked="checked"</#if>/></label>
            </li>
            <li>
                <button id="submit" type="submit" class="btn btn-primary">查询</button>
            </li>
            <li>
                <a class="btn" href="messagehomepage.vpage">发送</a>
            </li>
            <li>
                <a class="btn" href="batchmessagehomepage.vpage">批量发送</a>
            </li>
            <input type="hidden" id="pageNum" name="pageNum" value="${pageNum!'0'}"/>
        </ul>
    </form>
</div>
<div class="span9">
    <fieldset>
        <legend>查询结果</legend>
    </fieldset>
    <a id="pre_page" href="javascript:void(0)">上一页</a>
    第${(pageNum + 1)!'0'}页/共${totalPageNum!'0'}页
    <a id="next_page" href="javascript:void(0)">下一页</a>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width: 195px;">发送时间</th>
            <th style="width: 95px;">接收人ID</th>
            <th style="width: 175px;">消息类型</th>
            <th style="width: 125px;">发送人ID</th>
            <th>消息内容</th>
            <th style="width: 50px;">操作</th>
        </tr>
        <#if messageJournalList?has_content>
            <#list messageJournalList as messageJournal>
                <tr>
                    <td>${messageJournal.createTime?number_to_datetime}</td>
                    <td>
                        <a href="${requestContext.getWebAppContextPath()}/crm/user/userhomepage.vpage?userId=${messageJournal.receiverId!}">${messageJournal.receiverId!}</a>
                    </td>
                    <td>${messageJournal.type}</td>
                    <td>${messageJournal.senderId!}</td>
                    <td>${(messageJournal.payload?html)!}</td>
                    <td><a href="javascript:void(0)" class="deleteMessage"
                           data-send_log_id="${messageJournal.id!}">删除</a></td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function () {

        $('#pre_page').on('click', function () {
            var $pageNum = $('#pageNum');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum - 1);
            $('#submit').trigger('click');
        });

        $('#next_page').on('click', function () {
            var $pageNum = $('#pageNum');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum + 1);
            $('#submit').trigger('click');
        });

        $('.deleteMessage').click(function () {
            var $this = $(this);
            $.post('deletemessage.vpage', {sendLogId: $this.data('send_log_id')}, function (data) {
                alert(data.info);
                if (data.success) {
                    $this.closest('tr').remove();
                }
            });
        });

    });
</script>
</@layout_default.page>