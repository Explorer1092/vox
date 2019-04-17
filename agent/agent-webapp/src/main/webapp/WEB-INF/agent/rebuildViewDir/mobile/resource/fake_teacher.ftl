<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="老师详情" pageJs="common" footerIndex=2>
    <@sugar.capsule css=['res']/>
<div class="resources-box">
    <div class="res-content">
        <dl class="res-column">
            <dt><img width="100%" height="100%" src="/public/images/mobileCRM-V2-info-headPor.png" alt=""></dt>
            <dd>
                <span class="name">${teacher.realName!''}</span>
                <span class="icon-box">
                    <#if teacher.isEnglish!false><i class="icon-ying"></i></#if>
                    <#if teacher.isMath!false><i class="icon-shu"></i></#if>
                    <#if teacher.isChinese!false><i class="icon-yu"></i></#if>
                </span>
                <span>
                    <i class="icon-fake"></i>
                </span>
                <div class="res-info">
                    <span class="number" style="padding-right:.55rem;">ID：${teacher.teacherId!''}</span>
                    <span class="schoolName">
                        <#--<a href="/mobile/resource/school/card.vpage?schoolId=${teacher.schoolId}">${teacher.schoolShortName}</a>-->
                    </span>
                </div>
            </dd>
        </dl>
    </div>
    <div class="aut-title"><i class="aut-ico-1"></i>认证信息:</div>
    <div class="res-autInfor">
        <ul class="aut-list">
            <li><span class="aut-data">注册日期：${teacher.registerTimeStr!'--'}</span><span>手机号码：<a href="tel:${teacher.mobile!'--'}"> ${teacher.mobile!'--'}</a></span></li>
            <li>判假时间：<span class="aut-data">${teacher.fakeTime!''}</span></li>
            <li>判假人及原因：<span class="aut-data">${teacher.fakeCreatorName!''};${teacher.fakeDesc!''}</span></li>
        </ul>
    </div>
    <div class="js-cancleFake" style="width:100%;height:2rem;line-height: 2rem;text-align:center;background:#ff7d5a;color:#fff;margin-top:5rem">取消判假</div>
</div>
<script>
    var teacherId = ${teacher.teacherId!''}
    $(document).on('click','.js-cancleFake',function(){
        openSecond("/mobile/resource/teacher/relieve_faketeacher_view.vpage?teacherId=" + teacherId);
    });
    $(document).ready(function () {
        reloadCallBack();
        try{
            var setTopBar = {
                show:true,
                rightText:"",
                rightTextColor:"ff7d5a",
                needCallBack:true
            };
            setTopBarFn(setTopBar);
        }catch(e){
            var vox = window.vox = window.vox || {};
            vox.task = vox.task || {};
            vox.task.webViewDidFinishLoad = function(){
                var setTopBar = {
                    show:true,
                    rightText:"",
                    rightTextColor:"ff7d5a",
                    needCallBack:true
                };
                setTopBarFn(setTopBar);
            };
        }
    })
</script>
</@layout.page>