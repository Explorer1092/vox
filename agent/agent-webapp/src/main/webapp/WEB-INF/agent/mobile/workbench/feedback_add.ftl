<#import "../layout.ftl" as layout>
<@layout.page group="workbench" title="意见反馈">

<div   class="ui-grid-a">
    <div id="visit-nav" data-role="navbar">
        <ul>
            <li><a href="feedback_list.vpage" data-ajax="false" >意见记录查询</a></li>
            <li><a href="#" data-ajax="false" class="ui-btn-active">新增意见反馈</a></li>
        </ul>
    </div>
</div>
<div id="search-form">
    <form id="feedback_add"  data-ajax="false">
        <fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">
            <#if agentFeedbackFirstType??>
                <select id="firstType" name="firstType">
                    <option value="-1">全部</option>
                    <#list agentFeedbackFirstType?keys as key>
                        <option value="${key}">${agentFeedbackFirstType[key]!''}</option>
                    </#list>
                </select>
                <select id="secondType" name="secondType"  >
                    <option value="-1">全部</option>
                </select>
            </#if>
        </fieldset>
        <div class="ui-field-contain">
            <label for="title">问题描述：</label>
            <textarea  name="content" id="content" cols="40" rows="5" ></textarea>
        </div>
        <div class="ui-field-contain">
            <label for="title">问题影响：</label>
            <textarea  name="comment" id="comment" cols="40" rows="4" placeholder="填写清楚有利于帮助我们判断反馈的重要程度"></textarea>
        </div>
    </form>
    <button id="save" class="ui-btn-inline">保存</button>
    <button id="cancel" class="ui-btn-inline">取消</button>
</div>
<script>
    var agentFeedbackSecondType;
    $(function(){
        agentFeedbackSecondType = ${agentFeedbackSecondType!};
        $("#firstType").on("change",function(){
            var first = $(this).find("option:selected").val();
            $("#secondType").empty();
            $("#secondType").append("<option value='-1'>全部</option>");
            for(var index in agentFeedbackSecondType){
                if(agentFeedbackSecondType[index].first == first){
                    $("#secondType").append("<option value='" +agentFeedbackSecondType[index].id + "'>" + agentFeedbackSecondType[index].desc + "</option>");
                }
            }
            $("#secondType").selectmenu("refresh",true);
        });
        $("#save").on("click",function(){
            if(validateFeedback()){
                $.ajax({
                    type:"post",
                    url:"feedback_save.vpage",
                    data:$("#feedback_add").serialize(),
                    success:function(data){
                        if(data.success){
                            alert("保存成功");
                            window.location.href="feedback_list.vpage";
                        }
                    }
                });
            }
        });
        $("#cancel").on("click",function(){
            window.location.href="feedback_list.vpage";
        });
    });
    function  validateFeedback(){
        var firstType = $("#firstType").find("option:selected").val();
        var secondType = $("#secondType").find("option:selected").val();
        var content =$("#content").val();
        if(firstType == -1 || firstType == undefined){
            alert("请选择问题分类");
            return false;
        }
        if(secondType == -1 || secondType == undefined){
            alert("请选择问题分类");
            return false;
        }
        if(content == "" ||  content == undefined){
            alert("请输入问题描述");
            return false;
        }
        return true;
    }
</script>
</@layout.page>