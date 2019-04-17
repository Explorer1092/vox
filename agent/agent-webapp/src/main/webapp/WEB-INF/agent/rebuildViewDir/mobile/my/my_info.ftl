<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="个人信息" pageJs="" footerIndex=4>
<@sugar.capsule css=['team']/>
<#assign width = 292,height = 292>
<#include "../../../ossImg.ftl">
<style>
    body {background-color: #fff;}
    .fr{float: right;}
    .crmList-box .info .circle{border: 0;position: relative;}
    .crmList-box .info .item{
        margin: 0 .8rem;
        padding: .5rem 0;
        font-size: .8rem;
        color: #444;
        height: 1.5rem;
        line-height: 1.5rem;
        border-bottom: 1px solid #ccc;
    }
    .crmList-box .info .item:nth-child(2){
        margin-top: 1.2rem;
        border-top: 1px solid #ccc;
    }
</style>
<div class="crmList-box">
    <div class="info">
        <div class="circle js-uploadBtn">
            <#if avatar??>
                <img src="${avatar}${shortIconTail!}"/>
            </#if>
        </div>
        <div class="item">
            <label>姓名</label><span class="fr">${name!''}</span>
        </div>
        <div class="item">
            <label>用户名</label><span class="fr">${userName!''}</span>
        </div>
        <div class="item">
            <label>在职天数</label><span class="fr">${workingDayNum!''}</span>
        </div>
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
                        do_external('updateUserInfo');//更新个人信息
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
        $(document).on("click",".js-uploadBtn",function(){
            var carData =  {
                uploadUrlPath:"mobile/file/upload.vpage",
                NeedAlbum:true,
                NeedCamera:true,
                uploadPara: {}
            };
            do_external('getImageByHtml',JSON.stringify(carData));
        });
    });
</script>
</@layout.page>
