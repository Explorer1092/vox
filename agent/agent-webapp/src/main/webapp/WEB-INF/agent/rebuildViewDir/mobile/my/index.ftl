<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="我的" pageJs="" footerIndex=4 navBar="show">
<@sugar.capsule css=['team']/>
<#assign width = 292,height = 292>
<#include "../../../ossImg.ftl">
<style>
    body {background-color: #f9f9fa;}
</style>
<div class="crmList-box">
    <div class="c-head fixed-head">
        <span>我的</span>
    </div>
    <div class="info">
        <div class="circle js-uploadBtn">
            <#if avatar??>
                <img src="${avatar}${shortIconTail!}"/>
            </#if>
        </div>
        <div class="info-bg my"></div>
        <div class="inline-list data-list my c-flex c-flex-2">
            <div>
                ${name!''}
            </div>
            <div>
                ${userName!''}
            </div>
        </div>
    </div>

    <div class="myTitle">
        <a onclick="openSecond('/view/mobile/crm/my/personal_info.vpage?userId=${userId!0}')" class="item" style=""> <i class="my-icon researcher"></i>个人信息</a>
        <a onclick="openSecond('/view/mobile/crm/visit/visit_detail.vpage')" class="item" style=""> <i class="my-icon researcher"></i>拜访记录</a>
        <#if !requestContext.getCurrentUser().isProductOperator()>
            <#--仅专员、市经理有权限-->
            <#if requestContext.getCurrentUser().isBusinessDeveloper() || requestContext.getCurrentUser().isCityManager()>
                <a onclick="openSecond('/view/mobile/crm/researcher/researcher_list.vpage')" class="item" style=""> <i class="my-icon researcher"></i>教研员资源</a>
                <@apptag.pageElement elementCode="ed0b5148c4794330">
                    <a onclick="openSecond('/view/mobile/my/school.vpage')" class="item"><i class="my-icon apply"></i>我创建的学校</a>
                </@apptag.pageElement>

            <#else>
                <a onclick="openSecond('/mobile/my/school.vpage')" class="item"><i class="my-icon apply"></i>我创建的学校</a>
                <a onclick="openSecond('/mobile/feedback/view/index.vpage?userId=${userId!0}')" class="item"><i class="my-icon apply"></i>产品反馈</a>
                <a onclick="openSecond('/view/mobile/crm/researcher/researcher_list.vpage')" class="item" style=""> <i class="my-icon researcher"></i>教研员资源</a>
            </#if>


        <a onclick="openSecond('/view/mobile/crm/my/matter_contact.vpage')" class="item gap"><i class="my-icon matter"></i>总部对接人</a>
        <a onclick="openSecond('/mobile/my/data_packet.vpage')" class="item"><i class="my-icon package"></i>资料包</a>
            <a  href="javascript:void(0);" class="item" id="checkNet"><i class="my-icon check"></i>网络检测</a>
        </#if>
        <a href="javascript:;" onclick="openSecond('/mobile/my/setting.vpage')" class="item gap" style="margin-top:0.75rem;"><i class="my-icon set"></i>设置</a>
        <a href="javascript:;" onclick="openSecond('/view/mobile/crm/study/training_list.vpage')" class="item gap" style="margin-top:0.75rem;"><i class="my-icon set"></i>学习</a>

    </div>
</div>
<div id="loadingDiv" style="display: none;" class="center">
    <img style='display:block;padding:2rem 0;margin:0 auto;width:6rem;' src='/public/rebuildRes/image/mobile/res/loading.gif' />
</div>
<script src="https://qiyukf.com/script/f10a2349a4bead156114e00f9084177c.js" charset="utf-8"></script>
<script>
    var AT = new agentTool();
    AT.cleanAllCookie();
    var vox = vox || {};
    vox.task = vox.task || {};
    //拍照回调
    vox.task.setImageToHtml = function(result){
        var resJson = JSON.parse(result);
        if(!resJson.errorCode){
            if(resJson.fileUrl){
                var url = resJson.fileUrl;
                $.post("upload_avatar.vpage",{
                    avatar:url
                },function(res){
                    if(res.success){
                        AT.alert("上传成功");
                        location.reload();
                    }else{
                        AT.alert(res.info);
                    }
                });
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
    };

    $(document).on("ready",function(){
        $(document).ready(function () {
            //隐藏顶部title
            try{
                var setTopBar = {
                    show:false
                };
                setTopBarFn(setTopBar);
            }catch(e){

            }
        });
        $(document).on("click","#callCS",function(){
            var service_info = "${requestContext.getCurrentUser().getGroupName()!''}"+"|"+"${requestContext.getCurrentUser().getRealName()!''}"+"|"+"${requestContext.getCurrentUser().getUserPhone()!''}";
            var userId = "${requestContext.getCurrentUser().getUserPhone()!''}";
            var data = {
                dest_id:9109,
                questionType:"TIANJI_ALL",
                userId:userId,
                <#--userName:${requestContext.getCurrentUser().getRealName()!''},-->
                service_info:service_info
        };
            do_external('jumpToCustomService',JSON.stringify(data));
        });

        $(document).on("click",".js-uploadBtn",function(){
            var carData =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(carData));

        });
        $(document).on("click",'#checkNet',function(){
            var service_info = "${requestContext.getCurrentUser().getGroupName()!''}"+"|"+"${requestContext.getCurrentUser().getRealName()!''}"+"|"+"${requestContext.getCurrentUser().getUserPhone()!''}";
            var userId = "${requestContext.getCurrentUser().getUserPhone()!''}";
            var netData = {
                dest_id:9109,
                questionType:"TIANJI_ALL",
                userId:userId,
            <#--userName:${requestContext.getCurrentUser().getRealName()!''},-->
                service_info:service_info
            };
            do_external('checkNetworkWithUploadData',JSON.stringify(netData));
        });
    });

    function turnPage(obj, title){
        var href = obj.href;
        var data = {
            url:href,
            title:title,
            back_close:true
        };
        do_external('openSecondWebview',JSON.stringify(data));
        return false;
    }

</script>
</@layout.page>
