<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='老师申诉审核' page_num=3>
<div id="main_container" class="span9">
    <legend>老师申诉审核</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                    <input type="hidden" value="${appeal.id}" id="appealId" />
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">申诉类型</label>
                            <div class="controls">
                                ${appeal.type.description!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学校名称</label>
                            <div class="controls">
                                ${appeal.schoolName!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">所属区域</label>
                            <div class="controls">
                                ${appeal.pname!''}${appeal.cname!''}${appeal.aname!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">老师姓名</label>
                            <div class="controls">
                                ${appeal.userName!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">申诉原因</label>
                            <div class="controls">
                                ${appeal.reason!''}
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">上传资格证明</label>
                            <div class="controls">
                                <a href='${prePath!}${appeal.fileName!}' target='_blank'>
                                    <img id="imgSrc"
                                         <#if appeal.fileName?? && appeal.fileName?has_content>src="${prePath!}${appeal.fileName!}" </#if>
                                         style="height:150px;"/>
                                </a>
                            </div>
                        </div>
                        <#if appeal.type.name() == 'CHEATING'>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">查看作业</label>
                                <div class="controls">
                                    <a href="/crm/teachernew/teachernewhomeworkhistory.vpage?teacherId=${appeal.userId!}&homeworkDay=30">查看作业详情</a>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">审核意见：</label>
                            <div class="controls">
                                <input type="text" id="comment" />
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-footer">
            <button id="passBtn" class="btn btn-primary">审核通过</button>
            <button id="unPassBtn" class="btn btn-red">驳回</button>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#passBtn").on("click", function () {
            var recordMapper = {
                appealId: $("#appealId").val(),
                comment: $("#comment").val()
            };
            if (recordMapper.comment == undefined || recordMapper.comment.trim() == '') {
                alert("请输入审核意见");
                return false;
            }
            $.ajax({
                type: "post",
                url: "auditappealpass.vpage",
                data: recordMapper,
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'index.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#unPassBtn").on("click", function () {
            var recordMapper = {
                appealId: $("#appealId").val(),
                comment: $("#comment").val()
            };
            if (recordMapper.comment == undefined || recordMapper.comment.trim() == '') {
                alert("请输入审核意见");
                return false;
            }
            $.ajax({
                type: "post",
                url: "auditappealunpass.vpage",
                data: recordMapper,
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'index.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>