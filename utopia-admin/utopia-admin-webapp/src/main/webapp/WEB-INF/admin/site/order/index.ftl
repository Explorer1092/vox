<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
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

    <fieldset>
        <legend>通过用户ID或者手机号码批量查询用户购买增值产品详情</legend>
        <form method="post" action="/site/order/downloadorderdetail.vpage" id="downloadOrderForm">
        <#--<form method="post" action="/site/order/index.vpage">-->
            <ul class="inline">
                <li>
                    <label>输入：账号/手机号码 (空格或tab) 咨询日期（精确到秒：2018-10-08 14:21:20）<textarea name="keyList" style="width:300px;" cols="45" rows="10" placeholder="请在这里输入用户ID，一行一条"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="button" value="导出" id="export" />
                <#--<input id="order_output" class="btn" type="button"  value="导出" />-->
                </li>
            </ul>
        </form>
    </fieldset>
</div>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
<script>
    $(function () {
        $('#export').on('click', function () {
            // 先check数据，在form表单下载
            $.ajax({
                url: '/site/order/ordervalitioncheck.vpage',
                type: 'POST',
                data: {
                    keyList: $("textarea[name='keyList']").val()
                },
                success: function (res) {
                    if (res.success) {
                        $('#downloadOrderForm').submit();
                    } else {
                        alert(res.info);
                    }
                }
            })
        });
    });
</script>
</@layout_default.page>