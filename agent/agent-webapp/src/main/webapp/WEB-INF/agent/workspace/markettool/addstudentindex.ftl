<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='批量导入快乐学学生账号' page_num=9>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 批量导入快乐学学生账号</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#include '../../widget_alert_messages.ftl'/>
            <form method="post" action="/workspace/markettool/addstudentconfirm.vpage" enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择EXCEL数据文件，格式如下图所示。<br/>
                        注意事项:<br/>
                        &nbsp;&nbsp;1. 需要保留第一行表头<br/>
                        &nbsp;&nbsp;2. 所上传的学校学要为字典表内学校，学校ID和学校名称必须和系统中的学校信息一致。<br/>
                        &nbsp;&nbsp;3. 老师已经注册，且为快乐学老师，老师ID和老师姓名需与系统中得老师信息保持一致。<br/>
                        &nbsp;&nbsp;4. 老师和学校的关联关系需要确保正确<br/>
                        &nbsp;&nbsp;5. 每次只能上传一所学校，且同一学校学生学号不能重复<br/>
                        &nbsp;&nbsp;6. 班内无该姓名学生时，会为其新注册账号；有该学生时，则会更新其学号<br/>
                        &nbsp;&nbsp;7. 学号后N位在学校内不重复的情况下将作为学生阅卷机填涂号，如遇重复将随机生成N位数字（N位学校当前填涂号位数，一般是5位）<br/>
                        <img src="${requestContext.webAppContextPath}/public/img/addmorestudents.png"/>
                    </label>
                    <br>
                    <div class="controls">
                        <input type="file" name="sourceExcelFile">
                    </div>
                </div>

                <ul class="inline">
                    <li>
                        <input class="btn" type="submit" value="上传" />
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