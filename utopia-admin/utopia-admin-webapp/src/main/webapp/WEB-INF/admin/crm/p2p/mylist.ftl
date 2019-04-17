<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="vitalityLogList" type="java.util.List<com.voxlearning.utopia.admin.data.VitalityMapper>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div class="tabbable" style="margin-bottom: 18px;">
        <ul id="activebox" class="nav nav-tabs">
            <li class="active"><a href="#tab1" data-name="LEVEL_1" data-ref="#teacher_level1" data-toggle="tab">7天内检查过</a></li>
            <li class=""><a href="#tab2" data-name="LEVEL_2" data-ref="#teacher_level2" data-toggle="tab">7-15天内检查过</a></li>
            <li class=""><a href="#tab3" data-name="LEVEL_3" data-ref="#teacher_level3" data-toggle="tab">15-30天内检查过</a></li>
            <li class=""><a href="#tab4" data-name="LEVEL_4" data-ref="#teacher_level4" data-toggle="tab">30天内未检查过</a></li>
            <li class=""><a href="#tab5" data-name="Ambassador" data-ref="#ambassador" data-toggle="tab">校园大使</a></li>
        </ul>
        <div style="padding-bottom: 9px; border-bottom: 1px solid #ddd;" class="tab-content">
            <div class="tab-pane active" id="tab1">
                <fieldset>
                    <legend>7天内检查过作业的老师<span></span>
                    </legend>
                    <ul class="pagination">
                    </ul>
                </fieldset>
                <table class="table table-hover table-striped table-bordered" id="teacher_level1">
                    <tr class="teacher_level_table" >
                        <th>教师姓名</th>
                        <th>手机</th>
                        <th>最近一次检查作业时间</th>
                        <th>30天内布置作业次数</th>
                        <th>备注</th>
                        <th>操作</th>
                    </tr>
                </table>

            </div>

            <div class="tab-pane" id="tab2">
                <fieldset>
                    <legend>7-15天内检查过作业的老师<span></span>
                    </legend>
                    <ul class="pagination">
                    </ul>
                </fieldset>
                <table class="table table-hover table-striped table-bordered" id="teacher_level2">
                    <tr class="teacher_level_table" >
                        <th>教师姓名</th>
                        <th>手机</th>
                        <th>最近一次检查作业时间</th>
                        <th>30天内布置作业次数</th>
                        <th>备注</th>
                        <th>操作</th>
                    </tr>
                </table>
            </div>

            <div class="tab-pane" id="tab3">
                <fieldset>
                    <legend>15-30天内检查过作业的老师<span></span>
                    </legend>
                    <ul class="pagination">
                    </ul>
                </fieldset>
                <table class="table table-hover table-striped table-bordered" id="teacher_level3">
                    <tr class="teacher_level_table" >
                        <th>教师姓名</th>
                        <th>手机</th>
                        <th>最近一次检查作业时间</th>
                        <th>30天内布置作业次数</th>
                        <th>备注</th>
                        <th>操作</th>
                    </tr>
                </table>
            </div>

            <div class="tab-pane" id="tab4">
                <fieldset>
                    <legend>30天内未检查过作业的老师<span></span>
                    </legend>
                    <ul class="pagination">
                    </ul>
                </fieldset>
                <table class="table table-hover table-striped table-bordered" id="teacher_level4">
                    <tr class="teacher_level_table" >
                        <th>教师姓名</th>
                        <th>手机</th>
                        <th>最近一次检查作业时间</th>
                        <th>30天内布置作业次数</th>
                        <th>备注</th>
                        <th>操作</th>
                    </tr>
                </table>
            </div>

            <div class="tab-pane" id="tab5">
                <fieldset>
                    <legend>校园大使<span></span>
                    </legend>
                    <ul class="pagination">
                    </ul>
                </fieldset>
                <table class="table table-hover table-striped table-bordered" id="ambassador">
                    <tr class="ambassador_table" >
                        <th>教师姓名</th>
                        <th>手机</th>
                        <th>最近一次检查作业时间</th>
                        <th>30天内布置作业次数</th>
                        <th>备注</th>
                        <th>操作</th>
                    </tr>
                </table>
            </div>
        </div>

    </div>

    <div id="note_mod_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改备注（最多输入200字）</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dd ><textarea style="height:200px;width:300px;"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary saveNote">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="send_sms_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>发短信</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>手机号</dt>
                        <dd><input type="text" id="mobile" readonly /></dd>
                    </li>
                    <li>
                        <dt>内容</dt>
                        <dd><textarea id="sms_content_box" maxlength="70" rows="6"></textarea></dd>
                        <p id="word_limit_box" style="float: right">还可以输入70字。</p>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary SmsSend">发 送</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<style type="text/css">
    .pagination > li {
        display: inline;
    }
    .pagination {
        border-radius: 4px;
        display: inline-block;
        float: right;
        margin: 0 0 10px;
        padding-left: 0;
    }
    .pagination > li > a, .pagination > li > span {
        background-color: #fff;
        border: 1px solid #ddd;
        color: #428bca;
        float: left;
        line-height: 1.42857;
        margin-left: -1px;
        padding: 6px 12px;
        position: relative;
        text-decoration: none;
    }
    .pagination > .disabled > span, .pagination > .disabled > span:hover, .pagination > .disabled > span:focus, .pagination > .disabled > a, .pagination > .disabled > a:hover, .pagination > .disabled > a:focus {
        background-color: #fff;
        border-color: #ddd;
        color: #777;
        cursor: not-allowed;
    }
    .pagination > .active > a, .pagination > .active > span, .pagination > .active > a:hover, .pagination > .active > span:hover, .pagination > .active > a:focus, .pagination > .active > span:focus {
        background-color: #428bca;
        border-color: #428bca;
        color: #fff;
        cursor: default;
        z-index: 2;
    }
</style>
<script>
    $(function(){
        var loadRecord = function(level,pageNo,targetTable,callback){
            var queryUrl = "load.vpage";
            $.post(queryUrl, {
                        activeLevel:level,
                        pageNo:pageNo
                    }, function (data) {
                        if(data.success){
                            $(targetTable).parent().find("span:first").text("("+data.total+"人)")
                            $(targetTable).find(".trecord").remove()
                            for(var i = 0 ; i < data.pageContent.length;i++){
                                var record = data.pageContent[i];
                                var tr = '<tr class="trecord">' +
                                        '<td><a href="/crm/teacher/teacherhomepage.vpage?teacherId='+record.teacherId+'" target="_blank">'+record.teacherName+'</a>('+record.teacherId+')</td>' +
                                        '<td>'+record.teacherCell+'</td>' +
                                        '<td>'+record.latestHomeworkTimeString+'</td>' +
                                        '<td>'+record.homeworkTime30d+'</td>' +
                                        '<td>'+record.note+'</td>' +
                                        '<td><button class="btn btn-primary modNote" type="button" teacherName="'+record.teacherName+'" teacherId="'+record.teacherId+'">修改备注</button>' +
                                        '   <button class="btn btn-primary sendSms" type="button" mobile="'+record.teacherCell+'">发短信</button>' +
                                        '</td>' +
                                        '</tr>'
                                $(targetTable).append(tr)
                            }
                            $(targetTable).parent().find(".pagination").html("")
                            var pageSize = 20
                            var totalPage = parseInt(data.total/pageSize) + (data.total%pageSize > 0 ? 1 : 0)
                            if(totalPage > 1){
                                $(targetTable).parent().find(".pagination").show()
                                for(var i = 0 ; i < totalPage ; i++){
                                    var li=''
                                    if(i == pageNo){
                                        li= '<li class="pgbutton active"><a>'+(i + 1)+'</a></li>'
                                    }else{
                                        li= '<li class="pgbutton"><a class="gotoPage" pageNo="'+i+'" level="'+level+'" href="javascript:void(0)">'+(i + 1)+'</a></li>'
                                    }
                                    $(targetTable).parent().find(".pagination").append(li)

                                }
                                var disableClass = pageNo == 0 ? 'class="disabled"' : ''
                                var gotoPageClass = pageNo == 0 ? '' : 'class="gotoPage" pageNo="'+(parseInt(pageNo) - 1)+'" level="'+level+'" href="javascript:void(0)"'
                                $(targetTable).parent().find(".pagination").find(".pgbutton:first").before('<li '+disableClass+'><a '+gotoPageClass+'>«上一页</a></li>')

                                disableClass = totalPage - pageNo == 1 ? 'class="disabled"' : ''
                                gotoPageClass = totalPage - pageNo == 1 ? '' : 'class="gotoPage" pageNo="'+(parseInt(pageNo) + 1)+'" level="'+level+'" href="javascript:void(0)"'
                                $(targetTable).parent().find(".pagination").find(".pgbutton:last").after('<li '+disableClass+'><a '+gotoPageClass+'>下一页»</a></li>')
                            }else{
                                $(targetTable).parent().find(".pagination").hide()
                            }
                        }else{
                            alert("加载失败")
                            return
                        }
                        if(null != callback){
                            callback();
                        }
                        return;
                    }
                    ,'json');
        }

        loadRecord("LEVEL_1",0,$("#teacher_level1"),function(){
        });

        $("#activebox").on("click", "li", function(){
            var $this = $(this);
            var $a = $this.find("a");
            var name = $a.attr("data-name");
            var dataTag = $a.attr("data-ref");
            loadRecord(name,0,$(dataTag),function(){});
        });

        $(document).on("click",".gotoPage",function(){
            var level = $(this).attr("level")
            var pageNo = $(this).attr("pageNo")
            loadRecord(level,pageNo,$(this).parents("div:first").find("table:first"),null)
        });

        $(document).on("click",".modNote",function(){
            $("#note_mod_dialog").modal('show');
            $("#note_mod_dialog").find("h3").text("修改"+$(this).attr("teacherName")+"的备注（最多输入200字）")
            $("#note_mod_dialog").find("textarea").val($(this).parent("td").prev().text())
            $(document).off("click",".saveNote");
            $(".saveNote").attr("teacherId",$(this).attr("teacherId"))
            var targetTd = $(this).parent("td").prev()
            $(document).on("click",".saveNote",function(){
                var queryUrl = "modNote.vpage";
                $.post(queryUrl, {
                            teacherId:$(this).attr("teacherId"),
                            note:$("#note_mod_dialog").find("textarea").val()
                        }, function (data) {
                            if(data.success){
                                $(targetTd).text($("#note_mod_dialog").find("textarea").val())

                            }else{
                                alert('操作失败')
                            }
                            $("#note_mod_dialog").modal('hide');
                        }
                        ,'json');
            });
        });

        $(document).on("click",".sendSms",function(){
            $("#send_sms_dialog").modal('show');
            $(document).off("click",".SmsSend");
            $("#mobile").val($(this).attr("mobile"));
            var content = $("#sms_content_box").val();
            if (content.length > 70) {
                alert("你输入的字数超出了70字，请重新填写。");
                return false;
            }
            $(document).on("click",".SmsSend",function(){
                var queryUrl = "sendsms.vpage";
                $.post(queryUrl, {
                            mobile: $("#mobile").val(),
                            content:content
                        }, function (data) {
                            if(data.success){
                                alert('发送成功')
                            }else{
                                alert(data.info)
                            }
                            $("#send_sms_dialog").modal('hide');
                        }
                        ,'json');
            });
        });

        $("#sms_content_box").on("keyup", function () {
            $("#word_limit_box").html(wordLengthLimit($(this).val().length,70));
        });

    });
    function wordLengthLimit(wordLen, defaultLen){
        var i = "<span>还可以输入" + (defaultLen - wordLen ) + "个字</span>";
        var s = "<span style='color: #ff1100'>已超出" + -(defaultLen - wordLen ) + "个字</span>";
        var t = defaultLen - wordLen < 0 ? s : i;
        return t;
    }
</script>
</@layout_default.page>