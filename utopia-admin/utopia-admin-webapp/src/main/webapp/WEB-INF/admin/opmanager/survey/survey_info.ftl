<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=9>
<div id="main_container" class="span9">
    <legend>问卷信息</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="downloadactivityinfo.vpage?" >
                    问卷活动ID：<input name="activityId" id="activityId" type="text" value="${activityId!}"/>
                    <a id="activityExport"  role="button" class="btn btn-success">导出问卷活动详情</a>
                </form>
            </div>
        </div>
    </div>

    <script  type="text/javascript">
        $(function(){
            $("#activityExport").on("click",function(){
                if($("#activityId").val()==""){
                    alert("请输入问卷活动ID");
                }else{
                    $("#frm").submit();
                }
            })
        })
    </script>
</@layout_default.page>