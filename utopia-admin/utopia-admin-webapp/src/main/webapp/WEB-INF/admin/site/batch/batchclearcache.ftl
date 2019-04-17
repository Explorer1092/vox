<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
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
    <@h.head/>
    <fieldset>
        <legend>批量清除缓存</legend>
        <#--<form id="form" method="post" action="/site/cache/clearcache.vpage">-->
            <ul class="inline">
                <li>
                    缓存key：<textarea id="keyList" cols="45" rows="20" placeholder="换行符分隔，一行一条" style="width: 800px"></textarea>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <input id="clear_cache_submit" class="btn" type="button" onclick="post();" value="提交" />
                </li>
            </ul>
        <#--</form>-->
    </fieldset>
</div>

<script>
    $('#clear_cache_submit').on('click', function () {

        var keyList = $('#keyList').val();

        $.post('/site/cache/clearcache.vpage', {
            keyList: keyList
        }, function (data) {
            if (!data.success) {
                var failedList = data.failedList;
                if (failedList.length != 0) {
                    alert(data.info + "\n" + failedList);
                }
            } else {
                alert("删除成功");
                window.location.reload();
            }
        });
    });
</script>
</@layout_default.page>