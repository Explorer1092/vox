<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="参与组会" pageJs="joinMeeting" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['new_base','school']/>
<style>
    .top_head{padding:1rem;font-size:.65rem;text-align:center;background:#eaeaea;
        cursor: pointer;}
    .top_head span{padding:.5rem;border:.05rem solid #000}
    .bottom_head{padding:.5rem 1rem;font-size:.65rem;text-align:left;}
    .inner-right{float:right}
    .main_body{font-size:.7rem}
    .body_div_bottom{padding:.5rem 0}
    .body_div{padding:.5rem 1rem;
        cursor: pointer;}
</style>
<a href="javascript:void(0)" class="inner-right js-submitVisBtn js-success" style="display:none;">提交</a>
<div class="flow">
    <div class="item GPS">
        请打开GPS
        <#if signType?? && signType == 1>
            <div class="inner-right btn-stroke fix-padding disabled">
                签到成功
            </div>
        <#else>
            <div class="inner-right btn-stroke fix-padding js-signBtn">
                签到
            </div>
        </#if>
    </div>
    <div class="main_body">

    </div>
    <div class="item tip show">
        <div class="photo clearfix photo2">
            <div class="shot">
                现场照片
            </div>
            <div class="pick">
                <div class="file" id="getSchoolGate"><img src="" width="100%" height="100%" ></div>
                <input type="hidden" id="photoUrl" name="photoUrl" value="">
                <div class="text">
                    <p class="dt">点此添加照片</p>
                    <p class="dd">尽量包含横幅、电子屏、 一起作业讲师等内容</p>
                </div>
            </div>
        </div>
    </div>
    <div class="item tip show">
        会议内容及效果（选填）
        <textarea style="padding:0" rows="5" class="content" id="meetingNote" name="meetingContent" data-einfo="请填写会议内容及效果" placeholder="请点击填写..."></textarea>
    </div>
</div>
<script id="main_body" type="text/html">
    <%if(res){%>
        <div class="body_div" onclick="addMeeting(this)" data-id="<%=res.id%>">
            <div><%=res.workerName%>
                <span class="inner-right">
                        <%=res.workTime%>
                    </span>
            </div>
            <div class="body_div_bottom">
                <%=res.workTitle%>
            </div><div class="body_div_bottom">
                <span style="font-size: .6rem">签到位置: <%=res.address%></span>
            </div>
        </div>
    <%}%>
</script>
<script>
    var vox = vox || {};
    vox.task = vox.task || {};
    var postData = {
        meetingRecordId: getUrlParam('id'),
        signType:1
    };
    var meetingLongitude = '';
    var meetingLatitude = '';
    vox.task.setLocation = function(res){
        visitData = {};
        var resJson = JSON.parse(res);
        //签到回调
        if(!resJson.errorCode){
            meetingLongitude = resJson.location.longitude;
            meetingLatitude  = resJson.location.latitude;
            if(meetingLatitude != '' && meetingLongitude != ''){
                $(".js-signBtn").addClass("disabled").text("签到成功");
            }
        }else if(resJson.errorCode == 1){
            $(".js-signBtn").removeClass("disabled").text("签到");
            $(".js-signBtn").addClass("js-signBtn");
            setTimeout(function(){
                AT.alert("客户端无位置信息权限");
            },150);
        }else if(resJson.errorCode == 2){
            $(".js-signBtn").removeClass("disabled").text("签到");
            $(".js-signBtn").addClass("js-signBtn");
            setTimeout(function(){
                AT.alert("客户端获取地理位置失败");
            },150);
        }else if(resJson.errorCode == 3){
            $(".js-signBtn").removeClass("disabled").text("签到");
            $(".js-signBtn").addClass("js-signBtn");
            setTimeout(function(){
                AT.alert("客户端获取地理编码失败");
            },150);
        }
        layer.close(_loading);
    };
    var getSignLocation = function(){
        if(!$(".js-signBtn").hasClass("disabled")){
            _loading = layer.load(1, {
                shade: [0.1, '#fff'] //0.1透明度的白色背景
            });
            do_external('getLocation');
        }else{
            layer.close(_loading);
            return false ;
        }

    };
    $(".js-signBtn").on("click",function(){
        getSignLocation();
    });

    //图片完成回调
    vox.task.setImageToHtml = function(result){
        var resJson = JSON.parse(result);
        try{
            if(!resJson.errorCode){
                if(resJson.fileUrl){
                    var url = resJson.fileUrl;
                    setTimeout(function () {
                        $("#photoUrl").val(url);
                        $("#getSchoolGate").find("img").attr("src",url);
                    },150)
                }else{
                    setTimeout(function(){
                        AT.alert("客户端未获取到图片");
                    },150);
                }
            }else{
                setTimeout(function(){
                    AT.alert("客户端出错");
                },150);
            }
        }catch(e){alert(e)}
    };
    $("#getSchoolGate").on("click",function(){
        var carData =  {
            uploadUrlPath:"mobile/file/upload.vpage",
            NeedAlbum:true,
            NeedCamera:true,
            uploadPara: {}
        };
        do_external('getImageByHtml',JSON.stringify(carData));
    });
</script>
</@layout.page>
