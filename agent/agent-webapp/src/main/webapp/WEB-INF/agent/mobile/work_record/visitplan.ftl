<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/work_record/index.vpage" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">计划列表</div>
        </div>
    </div>
</div>

<div>
    <#if (msgList![])?size gt 0>
        <#list msgList as msg>
        <div class="js-planItem" style="margin: 20px 0 20px 0;line-height: 30px;">
            <div>
                <div>
                    ${msg.schoolName!""}
                </div>
                <div class="js-vtime" style="float: right;">
                    ${msg.visitTime?string("yyyy-MM-dd")!""}
                </div>
                <p>
                    计划内容：${msg.content!''}
                </p>
            </div>
            <#if .now lt (msg.visitTime!'')>
                <div style="cursor: pointer;">
                    <div class="js-updateTime" data-pid="${msg.id!""}">
                        修改时间
                    </div>
                    <div class="js-delItem" data-pid="${msg.id!""}">
                        删除
                    </div>
                </div>
            </#if>
        </div>
        </#list>
    <#else>
        <p style="text-align: center;margin-top: 50px;">
            暂无任何计划
        </p>
    </#if>
</div>

<#--修改时间-->
<div id="updateDateDialog" class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">修改时间</div>
                        <input type="date" id="upDate" class="textDate">
                    </div>
                </li>
            </ul>
            <div class="boxFoot" style="cursor: pointer;">
                <div class="side-fl" id="upDateCancel">取消</div>
                <div class="side-fr" id="upDateSure">确定</div>
            </div>
        </div>
    </div>
</div>

<script>
    $(function(){

        $(document).on("click",".js-updateTime",function(){
            var pid = $(this).data("pid");
            $("#updateDateDialog").show();
            $("#upDateSure").attr("data-reid",pid);
            $("#upDate").val($(this).parents(".js-planItem").find(".js-vtime").html().trim());

        });

        $(document).on("click",".js-delItem",function(){
            var pid = $(this).data("pid");

            if(confirm("确定要删除该计划?")){
                console.log(pid);
                $.post("removeProgram.vpage",{
                    recordId:pid
                },function(res){
                    if(res.success){
                        alert("删除成功");
                        location.reload();
                    }else{
                        alert(res.info);
                    }
                });

            }

        });

        $(document).on("click","#upDateSure",function(){
            var newDate = $("#upDate").val();
            var reId = $(this).data("reid");

            $.post("updatePlanTime.vpage",{
                recordId:reId,
                updateTime:newDate
            },function(res){
                if(res.success){
                    alert("修改时间成功");
                    location.reload();
                }else{
                    alert(res.info);
                }
            });

            console.log(newDate);
            $("#updateDateDialog").hide();
        });

        $(document).on("click","#upDateCancel",function(){
            $("#updateDateDialog").hide();

        });

    });
</script>

</@layout.page>
