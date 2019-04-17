<#-- @ftlvariable name="description" type="java.lang.String" -->
<#-- @ftlvariable name="productTag" type="java.lang.String" -->
<#-- @ftlvariable name="availableProductTypes" type="java.util.List<java.lang.String>" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">

    <ul class="breadcrumb">
        <li><span>产品类型管理</span><span class="divider">|</span></li>
        <li><a href="regionproducthomepage.vpage">增加产品区域</a><span class="divider">|</span></li>
        <li><a href="regionproductlist.vpage">查询产品区域</a><span class="divider">|</span></li>
    </ul>

    <fieldset><legend>产品类型管理</legend></fieldset>
    <form action="?" method="post">
        <ul class="inline">
            <li>
                <label>产品类型：<input name="productTag" value="${productTag!}"/></label>
            </li>
            <li>
                <label>产品描述：<textarea name="description">${description!}</textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button type="submit">提交</button>
            </li>
        </ul>
    </form>

    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th></th>
            <th>产品类型</th>
            <th>操作</th>
        </tr>
        <#if availableProductTypes?has_content>
            <#list availableProductTypes as productType>
                <tr>
                    <td>${productType_index + 1}</td>
                    <td>${productType!}</td>
                    <td><a id="delete_product_type_${productType_index}" data-product_tag="${productType!}" href="javascript:void(0)">删除</a></td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function() {
        $('a[id^="delete_product_type_"]').click(function() {
            var $this = $(this);
            if (!window.confirm("是否确定删除?")) {
                return;
            }
            $.post('deleteregionproducttag.vpage', {productTag : $this.data('product_tag')}, function(data) {
                alert(data.info);
                if (data.success) {
                    $this.closest('tr').remove();
                }
            });
        });
    });
</script>
</@layout_default.page>