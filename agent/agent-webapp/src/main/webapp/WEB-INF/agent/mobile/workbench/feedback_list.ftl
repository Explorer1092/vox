<#import "../layout.ftl" as layout>
<@layout.page group="workbench" title="意见反馈">
<div  class="ui-grid-a">
    <div id="visit-nav" data-role="navbar">
        <ul>
            <li><a href="#" data-ajax="false" class="ui-btn-active">意见记录查询</a></li>
            <li><a href="feedback_add.vpage" data-ajax="false">新增意见反馈</a></li>
        </ul>
    </div>
</div>
<div id="search-form">
    <fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">
        <#if agentFeedbackFirstType??>
            <select id="firstType" name="firstType">
                <option value="-1">全部</option>
                <#list agentFeedbackFirstType?keys as key>
                    <option value="${key}" <#if key == firstType?string>selected='selected'</#if>>${agentFeedbackFirstType[key]!''}</option>
                </#list>
            </select>
            <select id="secondType" name="secondType">
                <option value="-1">全部</option>
            </select>
        </#if>
    </fieldset>

    <#if error??>
        <p>${error!''}</p>
    <#else >
        <#if userFeedbackList?? && userFeedbackList?has_content>
            <ul  data-role="listview" data-inset="true">
                <#list userFeedbackList as feedback>
                    <li>
                        <p>
                            问题描述：${feedback.content!''}
                            <br>
                            <br>
                            问题影响:${feedback.comment!''}
                        </p>
                    </li>
                </#list>
            </ul>
        </#if>
    </#if>
</div>
<script>
    var agentFeedbackSecondType;
    var firstType;
    var secondType;
    $(function(){
        agentFeedbackSecondType = ${agentFeedbackSecondType!};
        firstType = ${firstType!};
        secondType = ${secondType!};
        $("#secondType").empty();
        $("#secondType").append("<option value='-1'>全部</option>");
        for(var index in agentFeedbackSecondType){
            if(agentFeedbackSecondType[index].first == firstType){
                if(agentFeedbackSecondType[index].id ==secondType){
                    $("#secondType").append("<option value='" +agentFeedbackSecondType[index].id + "' selected='selected'>" + agentFeedbackSecondType[index].desc + "</option>");
                }else{
                    $("#secondType").append("<option value='" +agentFeedbackSecondType[index].id + "'>" + agentFeedbackSecondType[index].desc + "</option>");
                }
            }
        }
        $("#secondType").selectmenu("refresh",true);

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
        $("#secondType").on("change",function(e){
            if(e.hasOwnProperty('originalEvent')) {
                var secondType = $("#secondType").find("option:selected").val();
                var firstType = $("#firstType").find("option:selected").val();
                if (secondType != -1 && secondType != undefined && firstType != -1 && firstType != undefined) {
                    window.location.href = "feedback_list.vpage?secondType=" +secondType + "&firstType=" + firstType;
                }
            }
        });
    });
</script>
</@layout.page>