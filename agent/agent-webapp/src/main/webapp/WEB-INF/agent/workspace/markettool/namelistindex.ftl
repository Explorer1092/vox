<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='下载老师班级学生花名册' page_num=9>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 下载老师班级学生花名册</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#include '../../widget_alert_messages.ftl'/>
            <form method="post" action="/workspace/markettool/namelistdownload.vpage">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">输入要下载的老师ID或绑定的手机号码，以逗号分割，例如10001,10002,一次5个老师以内</label>
                    <div class="controls">
                        <textarea id="teacherIds" name="teacherIds" cols="45" rows="10" value="" placeholder="10001，10002"></textarea>
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