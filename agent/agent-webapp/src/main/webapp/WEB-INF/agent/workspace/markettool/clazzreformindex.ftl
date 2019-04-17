<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='班级重组' page_num=9>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 班级重组</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#include '../../widget_alert_messages.ftl'/>
            <form method="post" action="/workspace/markettool/clazzreformconfirm.vpage" enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择EXCEL数据文件，格式如下图所示。<br/>
                        注意事项:<br/>
                        &nbsp;&nbsp;1. 需要保留第一行表头<br/>
                        &nbsp;&nbsp;2. 名单中的老师ID和姓名必须和系统中老师信息一致，并且是对应班级的任课教师，如果不是，请先设置为任课教师<br/>
                        &nbsp;&nbsp;3. 名单中的学生的年级必须和系统中的年级一致，如果不一样，请先手工调整年级<br/>
                        &nbsp;&nbsp;4. 同年级重名学生不做任何处理，请手工调整<br/>
                        <img src="${requestContext.webAppContextPath}/public/img/clazzreform.png"/>
                    </label>
                    <br>
                    <div class="controls">
                        <input type="file" name="sourceExcelFile">
                    </div>
                </div>

                <ul class="inline">
                    <li>
                        <input class="btn" type="submit" value="提交" />
                    </li>
                </ul>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
            String.prototype.replaceAll = function(s1,s2) {
                return this.replace(new RegExp(s1,"gm"),s2);
            }
        $("#preview_btn").on('click',function(){
            var content = $.trim($("#batch_content").val());
            if(content == null || content == '') {
                alert("提交内容为空！");
                return;
            }
            $("#preview_msg").html("正在检查数据，是否符合规则");
            $("#preview_fails").html();
            var data = {
                content : content
            };
            $.post('/workspace/markettool/batchcreatetcspreview.vpage', data, function(resp) {
                if(!resp) {
                    alert("系统异常！");
                } else {
                    var msg = resp.errorMessage;
                    $("#preview_msg").text(msg);
                    var fail = resp.failList;
                    $("#preview_fails").empty();
                    if(fail != null && fail.length>0){
                        $.each(fail, function() {
                            var itemStr = this.item;
                            itemStr=itemStr.replace(new RegExp("{","gm")," ");
                            itemStr=itemStr.replace(new RegExp("}","gm")," ");
                            $("#preview_fails").append("<li><span>"+ itemStr+"</span>&nbsp;&nbsp;&nbsp;&nbsp;<span>"+ this.message+"</span></li>");
                        });
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>