<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='批量注册老师和学生' page_num=9>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 批量注册老师和学生</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form method="post" action="/workspace/markettool/tmpbatchcreatetcs.vpage">
                <ul class="inline">
                    <li>
                        <label>
                            <p>输入生成老师内容(学校ID 学校名称 老师姓名 学科 老师手机 年级 班级名称 学生姓名)</p>
                            <textarea id="batch_content" name="batch_content" cols="45" rows="10" value="" placeholder="95077	沧州市新华区第九中学	赵洪广 ENGLISH 15133741803 3 四班 张华曦"></textarea>
                        </label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <input id="preview_btn" class="btn" type="button" value="预览纠错" />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <input class="btn" type="submit" value="提交" />
                    </li>
                </ul>
            </form>
            <br />
            <p>导入预览提示：<span id="preview_msg" style="color: red;"><#if error??>${error}<#else >无</#if></span></p>
            <p>导入失败记录列表</p>
            <ul id="preview_fails" style="color: red;"></ul>
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