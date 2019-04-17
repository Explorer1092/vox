<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="访问受限资源组管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<input type="hidden" id="resgId" name="resgId" value="${resg.id!}"/>
<div id="main_container" class="span9">
    <legend>
        <input id="btn_add_resg_content" type="button" class="btn" value="添加资源内容" />
    </legend>

    <ul class="inline">
        <table class="table table-bordered" >
            <tr>
                <th>ID</th>
                <th>中文组名</th>
                <th>英文组名</th>
                <th>资源内容</th>
                <th>操作</th>
            </tr>
            <tbody id="tbody">
                <#if resg.resgContentList ?? >
                    <#list resg.resgContentList as resgContentItem >
                    <tr>
                        <td>${resgContentItem.id!}</td>
                        <td>${resg.cname!}</td>
                        <td>${resg.ename!}</td>
                        <td>${resgContentItem.resName!}</td>
                        <td>
                            <a href="#" id="edit_resg_content_${resgContentItem.id!}">编辑</a>&nbsp;&nbsp;
                            <a href="#" id="del_resg_content_${resgContentItem.id}">删除</a>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </ul>
</div>
<input type="hidden" id="modalResgContentId" name="modalResgContentId" value="0"/>
<div id="modal_edit_resg_content_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title">编辑受限访问资源内容</h4>
            </div>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <form class="form-horizontal">
                    <div class="control-group">
                        <label class="col-sm-2 control-label">资源内容</label>
                        <div class="controls">
                            <input type="text" id="modalResName" name="modalResName" class="form-control input-xlarge"/>
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
        $('#btn_add_resg_content').on('click',function(){
            $('#modalResgContentId').val('0');
            $('#modalResName').val('');
            $('#modal_edit_resg_content_dialog').modal('show');
        });

        $("a[id^='edit_resg_content_']").on('click',function(){
            var resgContentId = $(this).attr("id").substring("edit_resg_content_".length);
            editResgContent(resgContentId);
        });

        $("a[id^='del_resg_content_']").on('click',function(){
            var resgContentId = $(this).attr("id").substring("del_resg_content_".length);
            delResgContent(resgContentId);
        });

        $('#btn_modal_submit').on('click',function(){
            saveResgContent();
        });
    });

    function delResgContent(resgContentId){
        if (!confirm("确实要删除该受限资源内容吗？")) {
            return false;
        }

        $.post('delresgcontent.vpage',{
            resgContentId:resgContentId
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                window.location.reload();
            }
        });
    }

    function editResgContent(resgContentId){
        $.post('getresgcontent.vpage',{
            resgContentId:resgContentId
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                $('#modalResgContentId').val(data.resgContent.id);
                $('#modalResName').val(data.resgContent.resName);
                $('#modal_edit_resg_content_dialog').modal('show');
            }
        });
    }

    function saveResgContent(){
        var resgId =  $('#resgId').val();
        var resgContentId = $('#modalResgContentId').val();
        var resName = $('#modalResName').val().trim();

        if (resName == '') {
            alert("请输入资源内容!");
            return false;
        }

        $.post('saveresgcontent.vpage',{
            resgId:resgId,
            resgContentId:resgContentId,
            resName:resName
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                $('#modal_edit_resg_content_dialog').modal('hide');
                window.location.reload();
            }
        });
    }

</script>

</@layout_default.page>