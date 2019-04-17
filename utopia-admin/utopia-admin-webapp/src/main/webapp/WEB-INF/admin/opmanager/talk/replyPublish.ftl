<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>

<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong>17说</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="replyForm" class="well form-horizontal" method="post" action="/opmanager/talk/savereply.vpage">
                <fieldset>
                    <legend>发布观点</legend>
                    <div class="control-group">
                        <label class="control-label">话题名称：</label>
                        <div class="controls">
                            <select name="topicId" id="topicId">
                                <#if topics?exists>
                                    <#list topics as topic>
                                        <option value="${topic.topicId!}">${topic.title!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">话题选项：</label>
                        <div class="controls" data="options">
                            <#if options?exists>
                                <#list options as option>
                                    <label data="${option.topicId}">
                                        <input type="radio" data-title="${option.topicId}" class="form-radio-input" value="${option.optionId!}" name="optionId"/>${option.title!}
                                    </label>
                                </#list>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">观念：</label>
                        <div class="controls">
                            <textarea class="form-control span8"
                                      placeholder="输入观念"
                                      name="concept" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">马甲号：</label>
                        <div class="controls">
                            <select name="majia" id="majia">
                                <#assign keys = parents?keys>
                                <#list keys as key>
                                <option value="${parents["${key}"]}">${key}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
<script lang="javascript">
    var change = function () {
        var val = $("#topicId").val();
        $("div[data='options'] label").hide();
        $("label[data='"+val+"']").show();
        $(":radio[data-title!='"+val+"']").prop("checked", "false");
        $(":radio[data-title='"+val+"']").first().prop("checked", "checked");
    }
    $(document).ready(function () {
        change();
        $("#topicId").change(function () {
            change();
        });

        $("#saveBtn").click(function () {
            $("#replyForm").ajaxSubmit({
                url:"/opmanager/talk/savereply.vpage",
                type:"post",
                dataType:"json",
                beforeSubmit:function(){
                    if($("[name='concept']").val() == ''){
                        alert("请输入观念内容");
                        $("[name='concept']").focus();
                        return false;
                    }
                    return true;
                },
                success:function(data){
                    if(data.success){
                        alert("保存成功，可以继续添加");
                    }else {
                        alert("服务器异常");
                    }
                },
                clearForm:false,
                resetForm:false
            });
        });
    })
</script>
</@layout_default.page>