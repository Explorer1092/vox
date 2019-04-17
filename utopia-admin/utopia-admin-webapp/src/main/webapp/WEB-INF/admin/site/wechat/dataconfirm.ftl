<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <fieldset>
        <legend><span style="color: red">请认真确认数据，确认无误请点击确认按钮进行红包发送<span></legend>
    </fieldset>
    <div>
        <button id="confirmBut" type="button" class="btn btn-primary">确认发送</button>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>提交成功：</td>
                <td><#if datas??>${datas?size}</#if>条</td>
                <td>总金额：</td>
                <td>${totalAmount!0}元</td>
                <td>提交失败：</td>
                <td><#if wrongList??>${wrongList?size}</#if>条</td>
            </tr>
        </table>
        <label>成功记录：</label>
        <table class="table table-bordered">
            <tr>
                <td>用户ID</td>
                <td>金额（分）</td>
                <td>活动类型</td>
            </tr>
            <#if datas??>
                <#list datas as r>
                    <tr>
                        <td>${r.userId!''}</td>
                        <td>${r.amount!''}</td>
                        <td>${r.redPackType!''}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <label>失败记录：</label>
        <table class="table table-bordered">
            <#if wrongList??>
                <#list wrongList as l>
                    <tr>
                        <td>${l}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("#confirmBut").on("click", function () {
            var jsonData = ${dataJson!''};
            if (confirm("确定为这些用户发送红包吗？")) {
                appPostJson("send.vpage", {recharges: jsonData}, function (data) {
                    if (data.success) {
                        alert(data.info + ",失败" + data.errorCount + "个");
                        location.href = "sendredpack.vpage";
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    });

    function appPostJson(url, data, callback, error, dataType) {
        dataType = dataType || "json";
        return $.ajax({
            type: 'post',
            url: url,
            data: JSON.stringify(data),
            success: callback,
            error: error,
            dataType: dataType,
            contentType: 'application/json;charset=UTF-8'
        });
    }
</script>
</@layout_default.page>
