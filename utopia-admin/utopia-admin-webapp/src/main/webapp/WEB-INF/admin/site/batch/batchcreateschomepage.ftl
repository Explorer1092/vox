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
        <legend>批量注册系统自建班级</legend>

        <form method="post" action="/site/teacher/batchcreatesc.vpage">
            <ul class="inline">
                <li>
                    <label>
                        <p>输入生成自建班级内容(学校ID 年级 班级名称)</p>

                        <textarea id="content" name="content" cols="45" rows="10" placeholder="95077	3 四班"></textarea>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input id="preview_btn" class="btn" type="button" value="预览" />
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <input class="btn" type="submit" value="提交" />
                </li>
            </ul>
        </form>
        <div>
            <label>统计：</label>
            <table class="table table-bordered">
                <tr>
                    <td>成功：</td><td><#if successlist??>${successlist?size}</#if>件</td>
                    <td>失败：</td><td><#if failedlist??>${failedlist?size}</#if>件</td>
                </tr>
            </table>
            <label>失败记录：</label>
            <table class="table table-bordered">
                <#if failedlist??>
                    <#list failedlist as l>
                        <tr>
                            <td>${l}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
        <br />
        <ul id="preview_fails" style="color: red;"></ul>
    </fieldset>
</div>

<script type="text/javascript">
    $(function(){
        $("#preview_btn").bind('click',function(){
            var content = $.trim($("#content").val());
            if(content == null || content == '') {
                alert("提交内容为空！");
                return;
            }
            var data = {
                content : content
            };
            $.post('/site/teacher/batchcreatetcspreview.vpage', data, function(resp) {
                if(!resp) {
                    alert("系统异常！");
                } else {
                    var msg = resp.errorMessage;
                    $("#preview_msg").text(msg);
                    var fail = resp.failList;
                    $("#preview_fails").empty();
                    if(fail != null && fail.length>0){
                        $.each(fail, function() {
                            $("#preview_fails").append("<li><span>"+ this.item+"</span>&nbsp;&nbsp;&nbsp;&nbsp;<span>"+ this.message+"</span></li>");
                        });
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>