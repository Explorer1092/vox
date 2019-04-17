<#import "module.ftl" as temp>
<@temp.page title="资源" level="我的阅读">
<!-- 制作阅读文章 -->
<#if !dataList?has_content>
<div class="w-noData-box">您还没有上传过我的阅读，快去制作吧！</div>
<#else>
<div class="w-table w-table-border-bot" style="margin-top:0;">
<table>
    <tr>
        <th class="subject">题目</th>
        <th class="status">状态</th>
        <th class="handle">操作</th>
    </tr>
    <#list dataList as data>
        <tr>
            <td>${data.ename!}</td>
            <td>
                <#switch data.status?upper_case>
                    <#case "DRAFT">草稿<#break />
                    <#case "PUBLISH">未审核<#break />
                    <#case "VERIFYED">审核通过<#break />
                    <#case "VERIFYFAILURE">审核不通过<#break />
                </#switch>
            <td>
                <a target="_blank" href="/teacher/resource/reading/index.vpage?readingDraftId=${data.id!}">编辑</a>
                <#if data.status != "verifyed">
                    <a data-readingid="${data.id!}" class="delete_reading" href="javascript:void(0);">删除</a>
                </#if>
            </td>
        </tr>
    </#list>
</table>
</div>
</#if>
<div style="padding: 30px; text-align: center;">
    <p style="padding: 5px 0 30px 0;" class="text_center ugc_make_article">
        <a id="createReading" target="_blank" href="/teacher/resource/reading/index.vpage" class="w-btn"><strong>制作阅读文章</strong></a>
    </p>
    <p id="upload_copy_confirm_checkbox" class="color_gray step_1 text_center" style="padding-bottom: 50px;">
        <label for="sure"><input id="sure" name="sure" type="checkbox" checked="checked" value=""> 上传即表示您同意并遵循《<a href="/help/agreement.vpage" target="_blank" class="color_blue">一起作业用户协议</a>》</label>
    </p>
</div>

<script type="text/javascript">
    $(function(){
        <#-- ResourcePlatform 2015.11.03 Sir0xb -->
        <#if currentTeacherDetail.subject == "ENGLISH" && currentTeacherWebGrayFunction.isAvailable("ResourcePlatform", "Open")>
            LeftMenu.focus("mytts");
        </#if>

        $("a.delete_reading").on("click", function(){
            var $self = $(this);

            $.prompt("是否确认要删除？", {
                title   : "系统提示",
                focus   : 1,
                buttons : { "取消" : false, "确定" : true },
                submit  : function(e, v){
                    if(v){
                        $.post("/teacher/resource/reading/delete.vpage", {
                            readingDraftId : $self.attr("data-readingid")
                        }, function(data){
                            if(data.success){
                                setTimeout(function(){
                                    location.reload();
                                }, 500);
                            }
                        });

                    }
                }
            });

            return false;
        });

        $("#upload_copy_confirm_checkbox input").on("click", function(){
            $("#createReading").toggleClass("btn_disable");

            if(!$("#createReading").hasClass("btn_disable")){
                $("#createReading").attr("target", "_blank");
                $("#createReading").attr("href", "/teacher/resource/reading/index.vpage");
            }else{
                $("#createReading").attr("target", "_self");
                $("#createReading").attr("href", "javascript:void(0);");
            }
        });
    });
</script>
</@temp.page>
