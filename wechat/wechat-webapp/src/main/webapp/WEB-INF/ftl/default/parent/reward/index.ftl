<#import "../layout.ftl" as parentWard>
<@parentWard.page title='成长心愿单' pageJs="parentWard">
<@sugar.capsule css=['jbox'] />
<#include "../userpopup.ftl">
<div class="main" data-bind="visible: $root.isGraduate()" style="display: none; text-align: center;">
    <div>暂不支持小学毕业账号</div>
</div>

<style> .top-warn { position: relative; background-color:#ffffd9; font-size: 0.8rem; line-height: 1.25rem; padding: 0.5rem; } </style>
<div class="top-warn">
    <p>
       亲爱的家长，由于一起作业平台整体产品线升级，"微信端家长奖励"将于2017年8月15日下线。下线后您可以使用最新家长通App，App中为您和孩子提供了更多的学习奖励。
    </p>
</div>
<div data-bind="if: !$root.isGraduate()">
    <div class="pr-top" style="background: #fff; height: 70px; overflow: hidden;padding: 0.3rem 0.5rem;">
        <div class="pr-swtic clearfix" style="margin:0.1rem 0 0;">
            <div class="tab-mission" data-bind="click: showComp,css:{active: showTab() == 'complete'}">全部</div>
            <div class="tab-mission" data-bind="click: showGoing,css:{active: showTab() == 'ongoing'}">未完成</div>
        </div>
    </div>

    <div class="pr-top">
        <div class="pr-line"><span class="add"></span></div>
        <div class="text" >为孩子设置新目标及奖励</div>
        <a href="javascript:void(0);" class="btn-yellow" data-bind="click: setNewMission">创建目标</a>
        <a href="javascript:void(0);" class="btn-pink" data-bind="click: setChildNotices">孩子心愿</a>
    </div>

    <div id="mission-box" class="pr-main" data-bind="visible: missionsContent().length != 0,foreach: missionsContent">
        <div>
            <div class="pr-line">
                <span data-bind="css:{right: missionState!='ONGOING'}" class="dot"></span>
            </div>
            <div class="clearfix">
                <!-- ko if: img -->
                <div class="pic" data-bind="css:{'v-clickPic': img!== ''},visible: img!== '',attr: {'data-changeImageBox': id}">
                    <img data-bind="visible: img!== '',attr:{ src: '<@app.cdnImage/>'+img }" alt=""/>
                </div>
                <!-- /ko -->
                <div class="pic" data-bind="visible: img== '',attr: {'data-imageBox': id}">还没有照片</div>
                <div class="text">
                    <div data-bind="text: '目标：'+mission"></div>
                    <div><span data-bind="text: missionDate"></span><span class="time" data-bind="text: finishCount+'/'+totalCount"></span></div>
                    <div data-bind="text: '奖励：'+rewards"></div>
                </div>
            </div>
            <div class="clerafix">
                <div>
                    <a href="javascript:void(0);" class="btn btn-blue upload-pic" style="position: relative;" data-bind="visible: img !== '',attr: {'data-missionid': id,'type':'changeImgBtn'}">更换照片</a>
                    <a href="javascript:void(0);" class="btn btn-blue upload-pic" style="position: relative;" data-bind="visible: img == '',attr: {'data-missionid': id,'type':'upImgBtn'}">上传照片</a>
                </div>
                <a href="#" class="btn btn-blue update_progress" data-bind="visible: missionState == 'ONGOING' && op == 'PARENT_UPDATE_PROGRESS',attr: {'data-missionid': id},click: $parent.updateProgress">进度+1</a>
                <a href="#" class="btn btn-yellow doreward" data-bind="visible: missionState == 'ONGOING' && op == 'PARENT_REWARD',attr: {'data-missionid': id},click:$parent.doReward">发放奖励</a>
            </div>
        </div>
    </div>

    <div class="pr-main">
        <div data-bind="visible: missionsContent().length == 0">
            <div class="pr-line"><span class="dot"></span></div>
            <div class="clearfix" style="line-height: 8rem; text-align: center; color: #999;">
                没有未完成的任务！
            </div>
        </div>

        <a href="javascript:void(0);" class="btn btn-blue" data-bind="visible: !haveNext(),click: nextPage" style="margin-right: 21px;">下一页</a>
    </div>

    <div class="pr-layerbox" style="display:none;">
        <div class="pr-layerbg"></div>
        <div class="pr-layerpic">
            <span class="close"></span>
            <img src="" alt="">
        </div>
    </div>
</div>

<script>
    <#if ret?has_content>
        var wechatConfig = ${json_encode(ret)};
    </#if>
</script>
</@parentWard.page>
