<#import "../../researchstaffv3.ftl" as com>
<@com.page menuIndex=20 menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">试卷及报告</li>
</ul>
<div class="r-titleResearch-box">
    <p>
    ${termText}${currentUser.formatManagedRegionStr()}小学英语教研试卷及数据报告
    </p>
    <div style="text-align: right">
        <a class="btn_vox btn_vox_small" href="/rstaff/testpaper/paperreport/list.vpage" id="btn-back">
            返回试卷列表
        </a>
    </div>
</div>
<div class="r-mapResearch-box">
    <div class="r-table">
        <table>
            <tbody>
            <tr>
                <td style="width: 89px;">试卷名称</td>
                <td id="paperName" style="text-align: left;">${paperName!}</td>
                <td style="width: 160px;">
                    <input type="hidden" name="paperId" id="paperId" value="${paperId!}">
                    <input type="button" id="deletePaper" value="删除">
                    <input type="button" id="send_message_to_teacher_box" value="通知老师">
                </td>
            </tr>
            <tr>
                <td>出卷人姓名：</td>
                <td colspan="2" style="text-align: left;">
                    <#if currentUser.subject == "ENGLISH">
                        <select id="author" name="author">
                            <option value="${currentUser.formatManagedRegionStr()}教研员" <#if author == "${currentUser.formatManagedRegionStr()}教研员">selected="selected"</#if>>${currentUser.formatManagedRegionStr()}教研员</option>
                            <option value="${currentUser.formatManagedRegionStr()}课题组" <#if author == "${currentUser.formatManagedRegionStr()}课题组">selected="selected"</#if>>${currentUser.formatManagedRegionStr()}课题组</option>
                            <option value="${currentUser.formatManagedRegionStr()}外语学会" <#if author == "${currentUser.formatManagedRegionStr()}外语学会">selected="selected"</#if>>${currentUser.formatManagedRegionStr()}外语学会</option>
                            <option value="一起作业课题组" <#if author == "一起作业课题组">selected="selected"</#if>>一起作业课题组</option>
                        </select>
                    <#elseif currentUser.subject == "MATH">
                        <select id="author" name="author">
                            <option value="${currentUser.formatManagedRegionStr()}数学教研组" <#if author == "${currentUser.formatManagedRegionStr()}数学教研组">selected="selected"</#if>>${currentUser.formatManagedRegionStr()}数学教研组</option>
                            <option value="一起作业数学课题组" <#if author == "一起作业数学课题组">selected="selected"</#if>>一起作业数学课题组</option>
                        </select>
                    </#if>
                </td>
            </tr>
            <tr>
                <td>试卷状态:</td>
                <td colspan="2" style="text-align: left;">
                    <select id="isOpen" name="isOpen">
                        <option value="true" <#if isOpen?? && isOpen>selected="selected"</#if>>已开放</option>
                        <option value="false" <#if !(isOpen?? && isOpen)>selected="selected" </#if>>未开放</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td></td>
                <td colspan="2" style="text-align: left;">
                    <a class="btn_vox btn_vox_primary " id="savePaper">保存</a>
                    <a class="btn_vox" id="cancelPaper" >取消</a>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<script id="t:消息模板" type="text/html">
    <div style="padding: 20px; line-height: 22px;">
        <textarea id="content_box" class="int_vox"  style="width: 310px; height: 140px; font: 14px/22px arial;" rows="5"><%=content%></textarea>
        <p class="text_gray_6 spacing_vox_top">
            以上文字可修改，通知将以系统消息形式发送给辖区老师。
        </p>
    </div>
</script>
<script type="text/javascript">
    function redirectPaperList(){
        setTimeout(function(){
            window.location.href = "/rstaff/testpaper/paperreport/list.vpage";
        },100);
    }

    $(function(){
        $("#savePaper").on("click", function () {
            var $this = $(this);
            if($this.isFreezing()){
                return false;
            }
            $this.freezing();
            $.post("/rstaff/exampaper/savepaper.vpage",{
                paperId : $.trim($("#paperId").val()),
                author  : $.trim($("#author").val()),
                isOpen  : $.trim($("#isOpen").val())
            },function(data){
                $this.thaw();
                if(data.success){
                    $17.alert("保存成功",function(){
                        redirectPaperList();
                    });
                }else{
                    $17.alert("保存失败");
                }
            },'json');
        });

        $("#cancelPaper").on("click",function(){
            redirectPaperList();
        });

        $("#deletePaper").on("click", function () {
            var $this = $(this);
            if($this.isFreezing()){
                return false;
            }
            var data = {
                paperId : $.trim($("#paperId").val())
            };
            $this.freezing();
            $.prompt("<p>试卷删除后将不能找回，已开放的试卷在开放区域老师的“教研员试卷”列表中也会消失<p> 您确定要删除吗？",{
                title  : "提示",
                focus: 1,
                buttons:{"取消":false,"确定":true},
                submit : function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        jQuery.ajax({
                            type: "post",
                            url: "/rstaff/exampaper/updatepaperenable.vpage",
                            data: data,
                            success: function(data){
                                $this.thaw();
                                if(data.success){
                                    $17.alert("删除成功",function(){
                                        redirectPaperList();
                                    });
                                }
                            },
                            error: function(){
                                $this.thaw();
                            },
                            dataType: "json"
                        });
                        $.prompt.close();
                        return false;
                    }else{
                        $this.thaw();
                        $.prompt.close();
                        return false;
                    }
                },
                close : function(e,v,m,f){
                    $this.thaw();
                }
            });
        });


        // 通知全区/全市
        $("#send_message_to_teacher_box").on("click", function(){
            var $this = $(this);
            // 获取paper单元格元素
            var paperName = $("#paperName").text();
            var contentText = "老师您好！我创建了" + paperName + "试卷，您可根据教学进度为使用此教材的班级布置此试卷，以便及时有效的掌握学生学习情况。";

            var userContent = "教研员 ${(currentUser.profile.realname)!}";

            var states = {
                sendMessage : {
                    html     : template("t:消息模板",{content : contentText + userContent}),
                    title    : "通知老师使用试卷",
                    position : {width:370},
                    focus    : 1,
                    buttons  : { "取消": false, "发送": true },
                    submit   : function(e,v,m,f){
                        e.preventDefault();
                        if(v){
                            e.preventDefault();
                            var content =  $("#content_box").val();
                            if($17.isBlank(content)){
                                $.prompt.goToState('contentEmptyTip');
                                return false;
                            }
                            if(content.length > 200){
                                $.prompt.goToState('contentLengthTip');
                                return false;
                            }
                            if($this.isFreezing()){
                                return false;
                            }
                            $this.freezing();
                            $.post("/rstaff/exampaper/sendmessagetoteachers.vpage",{
                                paperId : $.trim($("#paperId").val()),
                                content : content
                            },function(data){
                                $this.thaw();
                            });
                            $.prompt.close();
                            return false;
                        }else{
                            $.prompt.close();
                        }
                    }
                },
                contentEmptyTip  : {
                    html : "请填写要发送的系统消息",
                    title : "提示",
                    focus    : 1,
                    buttons  : {"确定" : true},
                    submit   : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState('sendMessage');
                    }
                },
                contentLengthTip : {
                    html : "消息内容不能大于200",
                    title : "提示",
                    focus    : 1,
                    buttons  : {"确定" : true},
                    submit   : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState('sendMessage');
                    }
                },
                successMessage : {
                    html  : "通知发送成功！",
                    title : "系统提示",
                    buttons : {"确定": true},
                    submit : function(e,v,m,f){
                        $.prompt.close();
                    }
                },
                errorMessage : {
                    html  : "通知未发送成功！",
                    title : "系统提示",
                    buttons : {"确定": true},
                    submit : function(e,v,m,f){
                        $.prompt.close();
                    }
                }
            };

            $.prompt(states);
        });

    });
</script>
</@com.page>