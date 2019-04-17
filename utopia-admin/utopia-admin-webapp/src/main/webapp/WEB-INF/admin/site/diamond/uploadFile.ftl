<script type="text/javascript"
        src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.2.5.js"></script>
<script type="text/javascript"
        src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/handlers.js"></script>
<script type="text/javascript"
        src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.queue.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fileuploader/SimpleAjaxUploader.min.js"></script>

<div id="uploadphotoBox" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" onclick="location.reload();" data-dismiss="modal" aria-hidden="true">×
        </button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div id="content" class="startUploadBox">
            <form>
                <div>
                    <button id="uploadBtn" class="btn btn-large btn-primary">选择图片</button>
                </div>
            </form>
            <div id="divFileProgressContainer"></div>
            <div id="thumbnails"></div>
        </div>

        <div class="endUploadBox" style="display: none;">
            <p class="b_right">
                <span class="text-success">图片上传成功</span>
            </p>
        </div>
    </div>
</div>


<script type="text/javascript">
    var inputNode;
    var nameNode;

    var uploader = new ss.SimpleUpload({
        button: uploadBtn,
        url: '/site/diamond/uploadImage.vpage',
        name: 'file',
        multipart: true,
        hoverClass: 'hover',
        focusClass: 'focus',
        responseType: 'json',
        allowedExtensions: ['jpg', 'jpeg', 'png', 'gif'],
        onSubmit: function() {

        },
        onComplete: function( filename, response ) {
            if (response.success) {
                alert(inputNode);
                $(inputNode).val(response.filePath);
                $(nameNode).val(response.originalFileName);
                hideModel();
            } else {
                alert(response.info);
                hideModel();
            }
        },
        onError: function() {
        }
    });


    function initParamData(inputNodeParam,inputNameNode) {
        $(".startUploadBox").show();
        $(".endUploadBox").hide();
        inputNode = inputNodeParam;
        nameNode = inputNameNode;
    }
    function hideModel() {
        $('#uploadphotoBox').modal('hide');
    }
</script>