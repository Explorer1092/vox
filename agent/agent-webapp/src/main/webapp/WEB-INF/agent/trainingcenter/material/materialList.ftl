<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='素材管理' page_num=17>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>素材管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a data-type="add" class="btn btn-success addBtn" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    上传视频
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting">标题</th>
                        <th class="sorting">视频</th>
                        <th class="sorting">地址</th>
                        <th class="sorting">创建时间</th>
                        <th class="sorting">大小</th>
                        <th class="sorting">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="editDepInfo_dialog" class="modal fade hide" style="width:700px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">上传视频</h4>
            </div>
            <input id="videoUrl" type="hidden" value="">
            <div class="modal-body" style="height:400px;width:600px">
                <div id="upload_video" class="control-group businessType form-horizontal">
                    <label class="control-label">选择视频</label>
                    <div id="container" class="controls">
                        <div id="ossfile">你的浏览器不支持flash,Silverlight或者HTML5！</div>
                        <a id="selectfiles" href="javascript:void(0);" class='btn'>选择文件</a>
                        <a id="postfiles" href="javascript:void(0);" class='btn'>开始上传</a>
                    </div>
                    <span id="console"></span>
                </div>
                <div id="editInfoDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script id="editInfoDialogTemp" type="text/x-handlebars-template">
    <input class="column_id" type="hidden" value="{{_id}}">
    <div class="control-group businessType">
        <label class="control-label">标题</label>
        <div class="controls" style="height:18px;line-height:18px">
            <input type="text"  name ="businessType" value="{{title}}" id="name" maxlength="10">
        </div>
    </div>
    <div class="control-group businessType">
        <label class="control-label">缩略图截取时间</label>
        <div class="controls" style="height:18px;line-height:18px">
            <input type="number"  name ="businessType" value="{{videoTime}}" id="videoTime" maxlength="2" placeholder="秒">
        </div>
    </div>
    <div class="control-group businessType">
        <label class="control-label">简介</label>
        <div class="controls" style="height:18px;line-height:18px">
            <textarea class="introduction" name="info" id="" cols="30" rows="5" maxlength="200">{{introduction}}</textarea>
        </div>
    </div>
</script>
<!-- 视频上传  -->
<script src="${requestContext.webAppContextPath}/public/js/sso-upload/plupload.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/sso-upload/upload.js"></script>
<script type="text/javascript">
    var _17zyHost = '',
    video_status = true,
    videoSnapshotHost = "";
    $(function(){
        var data = {};
//        oss_uploader();
//        ?x-oss-process=video/snapshot,t_10000,m_fast
        var renderDepartment = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };
        $(document).on('click','.close',function () {
            $('.delete_video').click();
        })
        var add_column = function (_id,title,videoTime,introduction) {
            Handlebars.registerHelper("compare",function (v1,v2,options) {
                if(v1 === v2){
                    return options.fn(this);
                }else{
                    return options.inverse(this);
                }
            });
            renderDepartment("#editInfoDialogTemp",{
                        _id:_id,
                        title:title,
                        introduction:introduction,
                        videoTime:videoTime
            },"#editInfoDialog");
            if(_id){
                $('#upload_video').hide();
            }else{
                $('#upload_video').show();
            }
            $("#editDepInfo_dialog").modal('show');
        };
        var get_list = function () {
            $.get("findMaterialList.vpage",function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.data.length;i++){
                        var item = res.data[i];
//                        res.data[i].createTime =
                        var operator = '<span class="btn btn-primary down_load" data-id="'+item.id+'">下载</span><span class="edit_column btn btn-primary" data-id="' + item.id + '" data-title=" '+ item.title +'" data-introduction=" '+ item.introduction +'" data-videoTime = "'+item.videoTime+'">编辑</span>'
                                +'<span class="deleteManage btn btn-primary" data-name="'+item.title+'" data-id="'+ item.id+'">删除</span>';
                        var video = "<img src= '"+item.url +"?x-oss-process=video/snapshot,t_" + (item.videoTime || 0) * 1000 + ",f_jpg,w_130,h_80,m_fast'>"
                        var arr = [item.title,video , item.url,item.createTime, Math.floor(item.fileSize*10/1024/1024)/10 + 'M', operator];
                        dataTableList.push(arr);
                    }
                    var reloadDataTable = function () {
                        var table = $('#datatable').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据

                    };
                    setTimeout(reloadDataTable(),0);
                }else{
                    layer.alert(res.info)
                }
            });
        };
        $(document).on('change','.edit_level',function () {
            if($(this).val() == 1){
                $('.parent_level').hide('');
            }else{
                $('.parent_level').show('');
            }
        });
        $(document).on('click','.down_load',function () {
            window.location.href='downloadVideo.vpage?id='+$(this).data('id');
        });
        $(document).on('click','.delete_video',function () {
            $('#selectfiles').show();
            var toremove = '';
            var id = $(this).attr("data-id");
            for (var i in uploader.files) {
                if (uploader.files[i].id === id) {
                    toremove = i;
                }
            }
            uploader.files.splice(toremove, 1);
            $('#ossfile').html('');
        });
        $(".column_level").on("change",function(){
            var _level = [],
                    level='',
                    column_level = $(".column_level");
            for (var i = 0;i< column_level.length;i++){
                _level.push(column_level.eq(i).val());
            }
            level = _level.toString();
            get_list(level);
        });

        //跳转dialog
        $(document).on("click",'.addBtn',function () {
            data.url = {};
            $('#selectfiles').show();
            $('#console').html('');
            $('#ossfile').html('');
            add_column();
        });
        $(document).on("click",'#editDepSubmitBtn',function () {
            data.title = $('#name').val();
            if(data.title == ''){
                alert('标题必填');
                return false;
            }
            data.id = $('.column_id').val();
            if(!data.id){
                if(video_status){
                    data.fileSize = uploader.files[0].size;
                    data.url = 'https://' + _17zyHost + uploader.settings.multipart_params.key;
                    data.picUrl = 'https://' + videoSnapshotHost + uploader.settings.multipart_params.key;
                }
            }
            data.videoTime = $('#videoTime').val();
            data.introduction = $('.introduction').val();
            if(data.introduction == ''){
                alert('简介必填');
                return false;
            }
            $.post('saveMaterialData.vpage',data,function (res) {
                if(res.success){
                    alert('保存成功');
                    $("#editDepInfo_dialog").modal('hide');
                    window.location.reload();
                }else{
                    alert(res.info)
                }
            })
        });
        $(document).on("click",'.edit_column',function () {
            data.url = {};
            var _id = $(this).data('id');
            var title = $(this).data('title');
            var videoTime = $(this).data('videotime');
            var introduction = $(this).data('introduction');
            add_column(_id,title,videoTime,introduction);
        });
        //跳转详情页
        $(document).on('click','.contract_detail',function () {
            window.location.href = 'detail.vpage?id='+$(this).data('id');
        });
        $(document).on("click",'.deleteManage',function () {
            var _id = $(this).data("id");

            layer.confirm("是否确认删除"+$(this).data("name") +'栏目？', {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.post('deleteMaterial.vpage',{id:_id},function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        window.location.reload();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });
        $("#queryOralBtn").trigger("click");
        $("#queryArticleBtn").trigger("click");
        get_list('1,2')
    });
</script>
</@layout_default.page>
