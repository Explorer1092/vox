<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=17>
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
        <li><span>增加Global配置</span><span class="divider">|</span></li>
        <li><a href="globaltaglist.vpage">查询Global配置</a><span class="divider">|</span></li>
    </ul>

    <fieldset>
        <legend>增加Global配置</legend>
        <ul class="inline">
            <li>
                <label>选择Tag名称：
                    <select name="tagName">
                        <#if tagNames?has_content>
                            <#list tagNames as tagName>
                                <option value="${tagName!}">${tagName!}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>输入tag值：<textarea name="configStr" cols="35" rows="3"
                                        placeholder="请以','或空白符隔开"></textarea></label>
            </li>
            <li>
                <label>输入备注：<textarea name="comment" cols="35" rows="3"
                                      placeholder="请输入备注原因"></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" id="submit_button">提交</button>
            </li>
        </ul>
    </fieldset>
    <br/>
</div>
<script>

    $(function () {
        $('#submit_button').on('click', function () {
            var postData = {
                tagValue: $('[name="configStr"]').val(),
                comment:$('[name="comment"]').val(),
                tagName: $('[name="tagName"]').val()
            };
            $.post("addglobaltag.vpage", postData, function (data) {
                alert(data.info);
                if (data.success) {
                    location.href = "addglobaltags.vpage";
                }
            });
        });
    });
</script>
</@layout_default.page>