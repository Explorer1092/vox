<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">

    <@h.head/>

    <fieldset>
        <legend>批量创建订单</legend>
        <div class="form-horizontal">
            <div class="control-group">
                <label class="col-sm-2 control-label">选择导入产品</label>
                <div class="controls">
                    <select id="productKey" name="productKey" style="margin-bottom:0">
                        <#if availableProducts??>
                            <#list availableProducts as product>
                                <option value="${product.productKey?default('')}"
                                        data-appKey="${product.productServiceType?default('')}">${product.productName}</option>
                            </#list>
                        </#if>
                    </select>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">导入用户列表(每行一个,每次不超过3000个):</label>
                <div class="controls">
                    <textarea id="content" name="content" cols="45" rows="30" placeholder="每行一个"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">备注描述</label>
                <div class="controls">
                    <input id="externalTrade" name="externalTrade"
                           placeholder="长度不超过16个字"/>
                </div>
            </div>
        </div>
        <ul class="inline">
            <li>
                <input class="btn" id="sub_btn" type="button" value="提交"/>
            </li>
        </ul>
        <br/>
        <ul id="preview_fails" style="color: red;"></ul>
    </fieldset>
</div>

<script type="text/javascript">
    $(function () {
        $("#sub_btn").on("click", function () {
            if (!confirm("确定给当前所有用户创建(" + $("#productKey").find("option:checked").text() + ")订单吗？")) {
                return;
            }
            $("#sub_btn").attr("disabled", true);
            $("#sub_btn").val("插入中,请稍等...");
            var data = {
                content: $("#content").val(),
                productKey: $("#productKey").val(),
                appKey: $("#productKey").find("option:checked").attr("data-appKey"),
                externalTrade: $("#externalTrade").val()
            };
            $.post('/crm/batch/order/create.vpage', data, function (res) {
                if (res.success) {
                    alert("创建成功！" + res.message);
                    window.location.reload();
                } else {
                    alert("创建失败！" + res.info);
                    $("#sub_btn").attr("disabled", false);
                    $("#sub_btn").val("提交");
                }
            });
        });
    });
</script>
</@layout_default.page>