<#import "../../layout/webview.layout.ftl" as layout>
<@layout.page title="教师节送祝福"
pageCssFile={"teaDay":["public/skin/project/teacherday/css/teaDay"]}
pageJsFile={
    "teaDay":"/public/script/project/teaDay",
    "jqcookie" : "/public/script/project/jquery.cookie"
}
pageJs=["jqcookie","teaDay","weui","voxLogs"]>
<div class="blessings-box">
    <div class="teaDay-header">
        <div class="td-avatar">
            <ul>
                <li id="js-teacher" class="active" data-tid="${teacher_id!}"><img src="${teacher_avatar!}"><p class="name">${teacher_name!}</p></li>
            </ul>
            <div class="icon-beans"><i class="beanIcon"></i></div>
        </div>
    </div>
    <div id="js-box">
        <div class="td-rules">
            <div class="title">拍一张你笑脸的照片，上传给老师吧！</div>
            <div class="content">
                <div class="info">图例：</div>
                <div class="image js-photo">
                    <img width="100%" src="/public/skin/project/teacherday/images/eg.jpg">
                </div>
                <div class="tip js-photo">点击添加祝福</div>
            </div>
        </div>
        <div class="teaDay-btn">
            <div class="innerBox fixFooter">
                <div class="btnBox">
                    <a href="javascript:void(0)" class="w-btn w-btnDefault">发送祝福</a>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="T:未拍照">
    <div class="td-rules">
        <div class="title">拍一张你笑脸的照片，上传给老师吧！</div>
        <div class="content">
            <div class="info">图例：</div>
            <div class="image js-photo">
                <img width="100%" src="/public/skin/project/teacherday/images/eg.jpg">
            </div>
            <div class="tip js-photo">点击添加祝福</div>
        </div>
    </div>
    <div class="teaDay-btn">
        <div class="innerBox fixFooter">
            <div class="btnBox">
                <a href="javascript:void(0)" class="w-btn w-btnDefault">发送祝福</a>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:已拍照">
    <div class="td-rules">
        <div class="title">点击“发送祝福”，把祝福发送到老师那里吧～</div>
        <div class="content" style="height:auto;">
            <div class="image-big">
                <img id="js-url" width="100%" src="<%=imgUrl%>">
            </div>
            <a href="javascript:void(0)" class="del-btn js-delete"></a>
        </div>
    </div>
    <div class="upload-tip">发送祝福后无法修改和删除照片</div>
    <div class="teaDay-btn">
        <div class="innerBox fixFooter">
            <div class="btnBox">
                <a href="javascript:void(0)" class="w-btn js-send-bless">发送祝福</a>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="T:发送祝福成功">
    <div class="td-rules">
        <div class="title">您的祝福已成功送出！</div>
        <div class="content active" style="height:auto;">
            <div class="image-big">
                <img width="100%" src="<%=imgUrl%>">
            </div>
        </div>
    </div>
</script>
</@layout.page>