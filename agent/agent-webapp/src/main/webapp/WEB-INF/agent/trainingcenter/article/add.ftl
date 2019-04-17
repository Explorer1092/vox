<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='新建文章添加/编辑' page_num=17>
<style>
    .modal {background-color: inherit;!important;}

    .modal.fade.in {top: 30%}

    .device {
        background-image: url("/public/img/device-sprite.png");
        background-position: 0 0;
        background-repeat: no-repeat;
        background-size: 300% auto;
        display: block;
        font-family: "Helvetica Neue", sans-serif;
        height: 813px;
        position: relative;
        transition: background-image 0.1s linear 0s;
        width: 395px;
    }

    .device .device-content {
        background: #fff none repeat scroll 0 0;
        font-size: 0.85rem;
        height: 569px;
        left: 37px;
        line-height: 1.05rem;
        overflow: hidden;
        position: absolute;
        top: 117px;
        width: 295px;
        overflow-y: scroll;padding: 0 .75rem;
    }
    .device .device-content embed{
        width: 100%;
    }
</style>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>新建文章添加/编辑</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">标题</label>
                    <div class="controls">
                        <input name="title" id="task_title" class="js-postData input-xlarge focused js-needed" maxlength="30" type="text" data-einfo="请填写标题">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">栏目</label>
                    <div class="controls columnList">

                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">封面</label>
                    <div class="controls" id="sourceFileWrap">
                        <input id="sourceFile" name="sourceExcelFile" type="file" />
                    </div>
                </div>
                <div class="control-group noedit-item">
                    <label class="control-label">发布对象</label>
                    <div class="controls">
                        <input type="checkbox"  name ="roleId" class="selectAll"> 全部
                        <#if allRoleTypeList?? && allRoleTypeList?size gt 0>
                            <#list allRoleTypeList as list>
                            <input type="checkbox"  name ="roleType" value="${list.id!0}"> ${list.roleName}
                            </#list>
                        </#if>
                    </div>
                </div>
                <div class="control-group noedit-item">
                    <label class="control-label">发布部门</label>
                    <div class="controls">
                        <div id="useUpdateDep_con_dialog" class="span4"></div>
                    </div>
                </div>
                <div class="control-group noedit-item">
                    <label class="control-label">是否跳至APP内打开</label>
                    <div class="controls">
                        <label class="control-label" style="text-align: left;">
                            <input type="radio" name="openInAPP" value="true" checked/>是
                        </label>
                        <label class="control-label" style="text-align: left;">
                            <input type="radio" name="openInAPP" value="false" />否
                        </label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">正文</label>
                    <div class="controls">
                        <script id="content_area"  type="text/plain"></script>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn btn-primary cancleBtn">取消</button>
                    <button type="button" class="btn btn-primary previewBtn">手机预览</button>
                    <button type="button" class="btn btn-primary saveBtn" data-url="save">保存</button>
                    <button type="button" class="btn btn-primary saveBtn" data-url="back">保存并关闭</button>
                </div>
        </div>
    </div>
</div>
<!-- 模态框（Modal） -->
<div id="previewModal" class="modal hide fade" tabindex="-1" style="width: 430px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body" style="max-height: 900px; width: 400px;" id="previewBox">
                <div class="device" style="" id="layoutInDevice">
                    <div class="device-content">
                        <div id="window">

                        </div>
                    </div>
                </div>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<#--添加栏目模板-->
<script id="columnAdd" type="text/html">
    <span>
        一级
        <select name="oneLevelColumnId" id="oneLevelColumnId" class="js-postData js-needed oneLevelColumnId" data-einfo="请选择一级栏目">
            <option value="">请选择</option>
            <%for(var i = 0; i< res.length; i++){%>
            <%var data = res[i].first%>
            <option value="<%=data.id%>"><%=data.name%></option>
            <%}%>
        </select>
    </span>
    <span>
        二级
        <select name="twoLevelColumnId" id="twoLevelColumnId" class="js-postData js-needed twoLevelColumnId" data-einfo="请选择二级栏目">
            <option value="">请选择</option>
        </select>
    </span>

</script>
<script id="columnListAdd" type="text/html">
    <option value="">请选择</option>
    <%for(var i = 0; i< res.length; i++){%>
    <%var data = res[i]%>
    <option value="<%=data.id%>"><%=data.name%></option>
    <%}%>
</script>

<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js?v=20180706"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js?v=20180706"></script>
<script type="text/javascript">
    $(function () {
        var ue = UE.getEditor('content_area', {
            serverUrl: "/workspace/appupdate/ueditorcontroller.vpage",
            zIndex: 999,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload','insertvideo', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', '|', 'searchreplace'
            ]]
        });


        //获取栏目
        var column = [];
        $.get('/trainingcenter/column/findLinkageColumnList.vpage',function (res) {
            if(res.success){
                column = res.data;
                $('.columnList').html(template('columnAdd',{res:column || ''}));
            }else{
                layer.alert('获取栏目失败');
            }
        });

        $(document).on('change','.oneLevelColumnId',function () {
            var _this = $(this);
            var list = [];
            column.forEach(function (item) {
                if(item.first.id == _this.val()){
                    list = item.second;
                }
            });
            _this.parent().next().find('.twoLevelColumnId').html(template('columnListAdd',{res:list || ''}));
        });


        var imageFile = undefined;
        //选择封面
        $("#sourceFile").on('change',function (e) {
            imageFile = e.target.files;
            var sourceFile = $("#sourceFile").val();
            if (blankString(sourceFile)) {
                layer.alert("请选择封面！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt !== "jpg" && fileExt !== "jpeg" && fileExt !== "png") {
                $("#sourceFile").val('');
                $('.filename').text('No file selected');
                layer.alert("请上传正确格式(jpg、jpeg、png)的图片！");
                return false;
            }

            var reader = new FileReader();
            reader.onload = function(e) {
                var data = e.target.result;
                var image = new Image();
                var width = 0;
                var height = 0;
                image.onload=function(){
                    width = image.width;
                    height = image.height;
                    if(width!=200||height!=150){
                        layer.alert("请上传200*150的图片！");
                        return false;
                    }else{
                        var img = '<img id="uploadImage" src="' + e.target.result + '" class="upload_image" style="width: 200px;height:150px;display: block; "/>';
                        $('#sourceFileWrap img').remove();
                        $('#sourceFileWrap').append(img);
                    }
                };
                image.src= data;

            };
            reader.readAsDataURL(e.target.files[0]);
        });

        //全选发布对象
        $('.selectAll').on('click',function () {
           var $this = $(this);
           if($this.prop('checked')){
               $('input[name="roleType"]').parent('span').addClass('checked');
               $('input[name="roleType"]').attr('checked',true);
           }else {
               $('input[name="roleType"]').parent('span').removeClass('checked');
               $('input[name="roleType"]').attr('checked',false);
           }
        });

        $(document).on('click','input[name="roleType"]',function () {
            var $this = $(this);
            var bol = true;
            if($this.prop('checked') == false){
                $('.selectAll').parent('span').removeClass('checked');
                $('.selectAll').attr('checked',false);
                return;
            }
            $('input[name="roleType"]').toArray().forEach(function (item) {
                if($(item).prop('checked') == false){
                    bol = false
                }
            });
            if(bol){
                $('.selectAll').parent('span').addClass('checked');
                $('.selectAll').attr('checked',true);
            }
        });


        var postData = {};
        postData.id = '';
        function checkData() {
            var flag = true;
            $.each($(".js-postData"),function(i,item){
                if($(item).hasClass('js-needed')){
                    if(!($(item).val())){
                        layer.alert($(item).data("einfo"));
                        layer.close(_loading);//关闭loading
                        flag = false;
                        return false;
                    }else{
                        postData[item.name] = $(item).val();
                    }
                }
            });
            if($('#sourceFile').val() == ''){
                layer.alert('请选择封面');
                flag = false;
                layer.close(_loading);//关闭loading
                return false;
            }
            return flag;
        }

        // 上传操作
        var saveBtn = $('.saveBtn');//上传按钮
        var _loading;
        saveBtn.on('click',function () {
            var $this = $(this);
            _loading = layer.load(1, {
                shade: [0.5,'#fff'] //0.5透明度的白色背景
            });
            if(checkData()){
                //获取发布对象IDS
                var roleTypes = [];
                $('input[name="roleType"]:checked').each(function () {
                    var roleType = $(this).val();
                    roleTypes.push(roleType);
                });
                if(roleTypes.length === 0){
                    layer.alert('请选择发布对象');
                    layer.close(_loading);
                    return false;
                }else{
                    postData.roleIds = roleTypes.toString();
                }

                //获取发布部门IDS
                var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
                var area_code = [];
                if($("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes()){
                    var node = $("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes();
                    for(var i = 0; i< node.length; i++){
                        area_code.push(node[i].key);
                    }
                }
                if(area_code.length === 0){
                    layer.alert('请选择发布部门');
                    layer.close(_loading);
                    return false;
                }else{
                    postData.groupIds = area_code.toString();
                }

                //获取正文
                if(ue.getContent().trim().length === 0){
                    layer.alert('请填写正文');
                    layer.close(_loading);
                    return false;
                }else{
                    postData.content = ue.getContent().trim().replace(/[\r\n]/g, "");
                }

                //获取是否跳至APP打开
                postData.openInAPP = $('input[name="openInAPP"]:checked').val();

                //上传封面
                if($('.upload_image').attr('src').indexOf('https://')>-1){//没有修改封面图片无需再次上传图片
                    save(postData,$this);
                }else{
                    var postImage = new FormData();
                    postImage.append(1,imageFile[0]);
                    $.ajax({
                        url: '/file/multiple_file_upload.vpage',
                        type: "POST",
                        data: postImage,
                        processData: false,  // 告诉jQuery不要去处理发送的数据
                        contentType : false,
                        success: function (res) {
                            if(res.success){
                                postData.coverImgUrl = res.imageUrlList[0];
                                $('.upload_image').attr('src',res.imageUrlList[0]);
                                //上传封面成功 开始上传数据
                                save(postData,$this);
                            }
                        }
                    });
                }
            }
        });

        function save(postData,$this) {
            $.post('save_article.vpage',postData,function (res) {
                layer.close(_loading);//关闭loading
                var string = postData.id ? '编辑成功' : '添加成功';
                if(res.success){
                    if($($this).data('url') == 'save'){
                        layer.alert(string);
                        postData.id = res.id;
                        $('.noedit-item').hide();
                    }else{
                        window.history.back();
                    }
                }else{
                    if(res.info){
                        layer.alert(res.info);
                    }
                    if(res.errorList){
                        layer.alert(res.errorList.toString());
                    }
                }
            });
        }

        // 取消操作
        var cancleBtn = $('.cancleBtn');//上传按钮
        cancleBtn.on('click',function () {
            window.history.back();
        });

        //预览
        $(document).on("click", ".previewBtn", function () {
            $("#window").html(ue.getContent().replace(/<img[^>]*>/gi,function(match){
                var match = match.replace(/(height)=[\'"]+[0-9]+[\'"]+/gi, '');
                return match;
            }));
            $("#previewModal").modal("show");
        });

        var groupIds = "";
        $("#useUpdateDep_con_dialog").fancytree({
            source: {
                url: "get_all_department_tree.vpage?selectedGroupIds="+groupIds,
                cache:true
            },
            checkbox: true,
            autoCollapse:true,
            selectMode: 3
        });
    });
</script>
</@layout_default.page>