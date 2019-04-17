<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="活动通知" page_num=24>
<style>
    .normal-pre {
        padding: 0;
        background: transparent;
        border: none;
        border-radius: 0;
        margin: 0;
    }
</style>
<span class="span9" style="font-size: 14px">
    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
    </#if>

    <form action="#" method="post">
        <div>
            <label class="control-label" for="content">活动通知内容:</label>
        </div>
        <div>
            <textarea name="content" id="content" placeholder="请输入活动通知内容" style="width: 80%"></textarea>
        </div>
        <input type="button" id="sendMailBtn" value="追加" class="btn btn-primary"/>
    </form>

    <div class="table_soll">
        <div>
            <fieldset>
                <legend>已发送活动通知<span style="font-size: small;color: red">（注：如果下表中没有刚刚发送的数据不要着急重发，稍后再查询试试。）</span></legend>
            </fieldset>
        </div>
        <table class="table table-bordered table-condensed table-striped">
            <thead>
                <tr>
                    <th>发送时间</th>
                    <th>内容</th>
                </tr>
            </thead>
            <#if mailList?has_content>
            <tbody>
                <#list mailList as mail>
                <tr>
                    <td>${mail.et?number_to_datetime}</td>
                    <td><pre class="normal-pre">${mail.mailDes}</pre></td>
                </tr>
                </#list>
            </tbody>
            </#if>
        </table>
    </div>

</span>

<script>
    $(function () {
        $('#sendMailBtn').click(function () {
            const content = $.trim($('#content').val());
            if (content === '') {
                alert('请输入活动通知内容。');
                return false;
            }
            $.post('/equator/mailservice/notice.vpage', {
                'content': content
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        })
    })
</script>
</@layout_default.page>