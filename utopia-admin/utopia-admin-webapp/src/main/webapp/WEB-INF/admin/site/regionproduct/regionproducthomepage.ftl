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

    <ul class="breadcrumb">
        <li><a href="createregionproducttag.vpage">产品类型管理</a><span class="divider">|</span></li>
        <li><span>增加产品区域</span><span class="divider">|</span></li>
        <li><a href="regionproductlist.vpage">查询产品区域</a><span class="divider">|</span></li>
    </ul>

    <fieldset>
        <legend>增加产品区域</legend>
        <ul class="inline">
            <li>
                <label>输入区域编码：<textarea name="productRegionCode" cols="35" rows="3"
                                        placeholder="请以','或空白符隔开"></textarea></label>
            </li>
            <li>
                <label>选择产品种类：<select name="productType">
                    <#if productTypeList?has_content>
                        <#list productTypeList as productType>
                            <option value="${productType!}">${productType!}</option>
                        </#list>
                    </#if>
                </select></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" id="submit_button">提交</button>
            </li>
        </ul>
    </fieldset>
    <br/>
    <fieldset>
        <legend>用户ID列表</legend>
        <div class="clear"></div>
        <div id="error_tip"></div>
        <div class="clear"></div>
        <div id="product_region_code_list"></div>
    </fieldset>
</div>
<script>

    $(function () {
        $('[name="productRegionCode"]').on('keyup', function () {

            var content = $(this).val();
            var productRegionCodeList = content.split(/[,，\s]+/);

            var $productRegionCodeList = $('#product_region_code_list');
            $productRegionCodeList.empty();
            $productRegionCodeList.append('<br/><ul class="inline"></ul>');

            var $errorTip = $('#error_tip');
            $errorTip.text('');

            var $productRegionCodeListULNode = $productRegionCodeList.find('ul');
            var wrongIds = '';

            for (var i = 0, length = productRegionCodeList.length; i < length; i++) {

                if (productRegionCodeList[i] == '') {
                    continue;
                }

                if (!productRegionCodeList[i].match(/^\d+$/)) {
                    if (wrongIds != '') {
                        wrongIds += ','
                    } else {
                        wrongIds += '<span class="warn">提示：</span>';
                    }

                    wrongIds += '<span class="warn">[' + i + ']</span><span>' + productRegionCodeList[i] + '</span>';
                    $productRegionCodeListULNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + productRegionCodeList[i] + '</span></li><br/>');
                } else {
                    $productRegionCodeListULNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + productRegionCodeList[i] + '</span></li><br/>');
                }

            }

            if (wrongIds != '') {
                $errorTip.append(wrongIds + '<span class="warn"> 不是规范的区域编码</span>');
            }

        });

        $('#submit_button').on('click', function () {
            var postData = {
                productRegionCode: $('[name="productRegionCode"]').val(),
                productType: $('[name="productType"]').val()
            };
            //noinspection JSUnresolvedFunction
            $.post('?', postData, function (data) {
                alert(data.info);
                if (data.success) {
                    location.href = "?";
                }
            });
        });
    });
</script>
</@layout_default.page>