<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='批量创建老师学生账号' page_num=9>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 批量创建老师学生账号</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#include '../../widget_alert_messages.ftl'/>
            <form method="post" action="/workspace/markettool/bulkaccountconfirm.vpage" enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择EXCEL数据文件，格式如下图所示。<br/>
                        注意事项:<br/>
                        &nbsp;&nbsp;1. EXCEL文件需要为97－2003版本格式(后缀名为.xls)<br/>
                        &nbsp;&nbsp;2. 需要保留第一行表头<br/>
                        &nbsp;&nbsp;3. 学校ID和学校名必须和系统中的学校信息一致，如果学校未创建，找客服帮忙创建学校。<br/>
                        &nbsp;&nbsp;4. 有年级班级姓名的将自动创建学生账号，如果没有将只生成老师账号。<br/>
                        &nbsp;&nbsp;5. 班级内学生姓名不能重复，如果确实有重名，请使用张三男，张三女，张三甲，张三乙这样的规则规避。<br/>
                        <img src="${requestContext.webAppContextPath}/public/img/bulkaccount1.png"/><br/>
                        <img src="${requestContext.webAppContextPath}/public/img/bulkaccount2.png"/>
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