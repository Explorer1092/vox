<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="正门照片" pageJs="schoolPhoto" footerIndex=4 navBar="hidden">
<@sugar.capsule css=["skin"]/>
<#assign shortIconTail = "?x-oss-process=image/resize,w_300,h_375/auto-orient,1">
<style>
    body{background:#f0eff5}
</style>
<a style="display:none;" href="javascript:void(0);" class="headerBtn js-handOutBtn">提交</a>
<div class="side-fr photoShow">
    <div id="photoShow"
         style="display:<#if (photoUrl!'')=''>none<#else>block</#if>;position: absolute;top: 50%;left: 50%;transform: translate(-100%,-50%);-ms-transform:translate(-100%,-50%);-moz-transform:translate(-100%,-50%);-webkit-transform:translate(-100%,-50%); -o-transform:translate(-100%,-50%);">
        <img src="${photoUrl!''}<#if photoUrl?? && photoUrl?contains("oss-image")!false>${shortIconTail!""}</#if>"
             style="vertical-align: middle;width: 200%;">
    </div>
</div>

<input type="hidden" id="photoUrl" name="photoUrl" value="${photoUrl!''}">
<div id="js-needGpsImage" class="gateImage <#if (photoUrl!'')=''>noImage<#else></#if>"
     style="position: absolute;top: 50%;left: 50%;transform: translate(-50%,-50%);-ms-transform:translate(-50%,-50%);-moz-transform:translate(-50%,-50%);-webkit-transform:translate(-50%,-50%); -o-transform:translate(-50%,-50%);">
</div>
<input type="hidden" id="returnUrl" name="returnUrl" value="${returnUrl!''}">
<script src="https://cdn-cnc.17zuoye.cn/public/script/voxLogs.js?v=2016-06-02"></script>
<script>
    var returnUrl = "${returnUrl!''}",
        referrerName;
    if(returnUrl == "addnewschoolpage.vpage"){
        referrerName = "添加学校" ;
    }else if (returnUrl == "schoolappraisal.vpage"){
        referrerName = "学校鉴定" ;
    }else{
        referrerName = "编辑学校信息" ;
    }
    $(function () {
        <#if errorMessage?? && errorMessage!="">
            alert("${errorMessage!''}");
        </#if>
    });
    var backUrl,backInfo,backData;
    var doubleHandFlag = true;
    var schoolId = ${schoolId!0};
    $(".js-handOutBtn").click(function () {
        if(backUrl && backInfo){
            if(doubleHandFlag){
                doubleHandFlag = false;
                $.post("save_school_info_photo.vpage",{url:backUrl,info:backInfo,schoolId:schoolId,type:"${type!""}"},function(result){
                    doubleHandFlag = true;
                    if(result.success){
                        opName = "o_nar5wlse" ;
                        moduleName = "m_o9GdyDrY" ;
                        try{
                            setLogs(moduleName,opName,referrerName,imgData);
                        }catch (e){
                            alert(e)
                        }
                        alert("保存成功");
                        disMissViewCallBack();
                    } else {
                        alert(result.info);
                        clearImg();
                    }
                });
            }else{
                alert("请选择照片提交");
            }
        }else{
            alert("请修改照片后提交");
        }
    });

    function clearImg(){
        $('#photoUrl').val('');
        $("#photoShow img").attr('src','');
        $("#photoShow").attr('style','display:none');
        $("#js-needGpsImage").attr('class','gateImage noImage');
    }
    $(document).ready(function () {
        $("#photoShow").click(function () {
            $("#js-needGpsImage").click();
        });
    });
    $(document).on("click","#js-needGpsImage",function(){
        var res =  {
            NeedAlbum:true,
            NeedCamera:true
        };
        do_external('getImageWithGPS',JSON.stringify(res));
    });

    var vox = vox || {};
    vox.task = vox.task || {};

    //图片完成回调
    var imgData;
        var opName,moduleName;

    vox.task.setImageWithGPS = function(res){
        imgData = JSON.parse(res);
        backData = imgData;
        backUrl = imgData.url;
        backInfo = JSON.stringify(imgData.info);
        opName = "o_RKsdLz2n";
        moduleName = "m_o9GdyDrY";
        schoolId = ${schoolId!0};
        try{
        setLogs(moduleName,opName,referrerName,imgData);
        }catch (e){
            alert(e)
        }
        /*前端展示*/
        $("#photoUrl").val("");
        $("#js-needGpsImage").removeClass("noImage");
        setTimeout(function(){
            $("#photoShow").show();
            $("#photoShow").empty().append('<img style="vertical-align: middle;width: 200%;" src="' + imgData.url + '">');
        },150);
    };
    function setLogs(module,op,referrerName,data){
        try{
            do_external('getLocation');
        vox.task.setLocation = function (res) {
            var resJson = JSON.parse(res);
            var x = resJson.location.longitude;
            var y = resJson.location.latitude;
                YQ.voxLogs({
                    database : "marketing", //不设置database  , 默认库web_student_logs
                    module : module, //打点流程模块名
                    op : op ,//打点事件名
                    userId:${requestContext.getCurrentUser().getUserId()!0},
                    s0 : ${schoolId!0},
                    s1 : referrerName,
                    s2 : x + "," + y,
                    s3 : data.source,
                    s4 : data.info.DateTime + ","+ data.info.Model +","+data.info.Longitude + ","+data.info.Latitude
                });

        };}catch (e){
            alert(e)
        }

    }

</script>
</@layout.page>