<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="访问受限资源组管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <legend>
        <input id="btn_add_resg" type="button" class="btn" value="添加资源组" />
    </legend>

    <form action="resgindex.vpage" method="post" id="resg_list_form">
        <ul class="inline">
            <table class="table table-bordered" >
                <tr>
                    <th>ID</th>
                    <th>中文组名</th>
                    <th>英文组名</th>
                    <th>组说明</th>
                    <th>内容列表</th>
                    <th>操作</th>
                </tr>
                <tbody id="tbody">
                    <#if resgList ?? >
                        <#list resgList as resgItem >
                        <tr>
                            <td>${resgItem.id!}</td>
                            <td>${resgItem.cname!}</td>
                            <td>${resgItem.ename!}</td>
                            <td>${resgItem.description!}</td>
                            <td>
                                <#if resgItem.resgContentList ?? >
                                    <#list resgItem.resgContentList as resgContentItem >
                                        ${resgContentItem.resName!}<br/>
                                    </#list>
                                </#if>
                            </td>
                            <td>
                                <a href="#" id="edit_resg_${resgItem.id!}">编辑</a>&nbsp;&nbsp;
                                <a href="#" id="del_resg_${resgItem.id}">删除</a>&nbsp;&nbsp;
                                <a href="editresgcontent.vpage?resgId=${resgItem.id}" id="eidt_resg_content${resgItem.id}">编辑内容</a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </ul>
    </form>
</div>
<input type="hidden" id="modalResgId" name="modalResgId" value="0"/>
<div id="modal_edit_resg_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">编辑受限访问资源</h4>
            </div>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <form class="form-horizontal">
                    <div class="control-group">
                        <label class="col-sm-2 control-label">组名(中文)</label>
                        <div class="controls">
                            <input type="text" id="modalResgCname" name="modalResgCname" class="form-control input-xlarge"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">组名(英文)</label>
                        <div class="controls">
                            <input type="text" id="modalResgEname" name="modalResgEname" class="form-control input-xlarge" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">说明</label>
                        <div class="controls">
                            <textarea id="modalResgDesc"  name="modalResgDesc" class="form-control input-xlarge" rows="3"></textarea>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="btn_modal_submit" type="button" class="btn btn-primary">保存</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<script type="text/javascript">

    $(function(){
        $('#btn_add_resg').on('click',function(){
            $('#modalResgId').val('0');
            $('#modalResgCname').val('');
            $('#modalResgEname').val('');
            $('#modalResgDesc').val('');
            $('#modal_edit_resg_dialog').modal('show');
        });

        $("a[id^='edit_resg_']").on('click',function(){
            var resgId = $(this).attr("id").substring("edit_resg_".length);
            editResg(resgId);
        });

        $("a[id^='del_resg_']").on('click',function(){
            var resgId = $(this).attr("id").substring("del_resg_".length);
            delResg(resgId);
        });

        $('#btn_modal_submit').on('click',function(){
            saveResg();
        });
    });

    function delResg(resgId){
        if (!confirm("确实要删除该受限资源组吗？")) {
            return false;
        }

        $.post('delresg.vpage',{
            resgId:resgId
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                window.location.reload();
            }
        });
    }

    function editResg(resgId){
        $.post('getresg.vpage',{
            resgId:resgId
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                $('#modalResgId').val(data.resg.id);
                $('#modalResgCname').val(data.resg.cname);
                $('#modalResgEname').val(data.resg.ename);
                $('#modalResgDesc').val(data.resg.description);
                $('#modal_edit_resg_dialog').modal('show');
            }
        });
    }

    function saveResg(){
        var resgId =  $('#modalResgId').val();
        var cname =  $('#modalResgCname').val().trim();
        var ename =  $('#modalResgEname').val();
        var desc =  $('#modalResgDesc').val();

        if (cname == '') {
            alert("请输入中文组名!");
            return false;
        }

        $.post('saveresg.vpage',{
            resgId:resgId,
            cname:cname,
            ename:ename,
            desc:desc
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                $('#modal_edit_resg_dialog').modal('hide');
                window.location.reload();
            }
        });
    }

</script>

</@layout_default.page>