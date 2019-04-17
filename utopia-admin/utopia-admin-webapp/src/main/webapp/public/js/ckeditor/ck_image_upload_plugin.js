
(function () {
	var input = document.createElement('input');
	input.id = 'ck_image_upload';
	input.type = 'file';
	input.style.display = "none";
	$(input).change(function () {
	    var file = $(this)[0].files[0];
	    var fileOriginName = file.name;
        var index = fileOriginName.lastIndexOf(".");
        var ext = fileOriginName.substring(index + 1, fileOriginName.length);

	    $.ajax({
            url: "/chips/ai/todaylesson/getSignature.vpage",
            data: {
                ext: ext
            },
            type:"get",
            async: false,
            success:function (data) {
                var signResult = data.data;
                var store  = new OSS({
                    accessKeyId: signResult.accessid,
                    accessKeySecret: signResult.accessKeySecret,
                    endpoint: signResult.endpoint,
                    bucket: signResult.bucket
                });

                var ossPath = signResult.dir + signResult.filename + "." + ext;
                store.multipartUpload(ossPath, file).then(function (result) {
                    console.log("https://" + signResult.videoHost + ossPath);
                    var url = "https://" + signResult.videoHost + ossPath;
                    CKEDITOR.instances.editor.insertHtml('asd<img width="100%" src="'+ url +'"/>')
                }).catch(function (err) {
                    alert('上传失败');
                });
            }
        });
	});
	document.body.append(input);

    var a = {
        exec: function (editor) {
        //调用jsp中的函数弹出上传框，
           $('#ck_image_upload').click()
        }
    },
    b = 'ck_image_upload';
    CKEDITOR.plugins.add(b, {
        init: function (editor) {
            editor.addCommand(b, a);
            editor.ui.addButton('ck_image_upload', {
                label: '添加图片',  //鼠标悬停在插件上时显示的名字
                icon: './image-icon.png',   //自定义图标的路径
                command: b
            });
        }
    });
})();