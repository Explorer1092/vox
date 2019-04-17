<#--DEMO：在学生端和家长端调起原生相机 -->
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="调起原生相机demo"
pageJs=['jquery']
>
<div id="camera" style="background:red; text-align: center;color:#fff;line-height:4rem;">调起原生相机</div>
<div style="text-align: center;margin-top:1rem;">
    <img id="preview" src="http://pic1.win4000.com/wallpaper/d/5860d8fb26969.jpg" style="max-width:100%;display:none;">
</div>
<script type="text/javascript">
    signRunScript = function () {
        function getExternal() {
            var _WIN = window;
            if (_WIN['yqexternal']) {
                return _WIN.yqexternal;
            } else if (_WIN['external']) {
                return _WIN.external;
            } else {
                return _WIN.external = function () {
                };
            }
        }

        $('#camera').on('click', function(){
            //在学生端和家长端调起原生相机的通用方法：showTakePhoto
            //wiki地址：http://wiki.17zuoye.net/pages/viewpage.action?pageId=22748459
            if (getExternal()['showTakePhoto']) {
                getExternal().showTakePhoto(JSON.stringify({
                    show: true,
                    photoId: new Date().getTime(),
                    photoNum: 1,
                    photoSize: 5 * 1024
                }));
            }else {
                alert("未实现showTakePhoto方法");
            }
        });

        //上传成功后回调
        window.vox = {
            task: {
                uploadPhotoCallback: function (res) {
                    res = JSON.parse(res);
                    $('#preview').attr('src', res.pictures[0].url).show();
                }
            }
        };
    }


</script>
</@layout.page>
