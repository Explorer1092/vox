<#import './layout.ftl' as layout>

<@layout.page className='HomeworkGetmission' title="成长心愿单" pageJs="second">
    <#escape x as x?html>
        <#assign topType = "topTitle">
        <#assign topTitle = "成长心愿单">
        <#include "./top.ftl" >
        <div class="pr-top">
            <div class="pr-line"><span class="add"></span></div>
            <div class="text">为孩子设置新目标及奖励</div>
            <a href="/parentMobile/parentreward/createTask.vpage?step=v1&sid=${sid}" class="btn-yellow doTrack" id="set_new_mission" data-track="parent|setgoal_click">创建目标</a>
            <a href="/parentMobile/parentreward/childWish.vpage?sid=${sid}&cp=1" class="btn-pink doTrack" data-track="parent|wish_click">孩子心愿</a>
        </div>

        <#if result.success>

            <#if (result.missions![])?size == 0>
                <div class="parentApp-emptyProm parentApp-emptyProm-5" style="margin-top: 100px; position: static;">
                    <#--<div class="promIco"></div>-->
                    <div class="promTxt">太棒了，已经没有未完成目标了。<br/>快来创建新目标吧！</div>
                </div>
            <#else>
                <div style='height: 0px;width: 0px; overflow:hidden;'><input id="uploadImage" accept="image/jpg" type="file" value="" /></div>
                <#list (result.missions![]) as mission>
                    <#assign missionId = mission.id!"">
                    <div class="pr-main">
                        <div>
                            <div class="clearfix">
                                <div class="pr-line"><span class="dot"></span></div>
                                <div class="pic v-clickPic doUploadPic"  data-missionid="${missionId}">
                                    <img src="${mission.img!""}" alt="奖励照片">
                                </div>
                                <div class="text">
                                    <div>目标：${mission.mission!""}</div>
                                    <div><span>${mission.missionDate!""}</span><span class="time">${mission.finishCount!0}/${mission.totalCount!0}</span></div>
                                    <div>奖励：${mission.rewards!0}</div>
                                </div>
                            </div>
                            <div class="clerafix">
                                <div>
                                    <a href="javascript:;" class="btn btn-blue doUploadPic doTrack" data-track="parent|photo_click" data-missionid="${missionId}" >更换照片</a>
                                </div>
                                <#if (mission.finishCount!0)  gte (mission.totalCount!0)>
                                    <a href="javascript:;" class="btn btn-blue doUpdateProgress doTrack" data-track="parent|complete_click" data-track_error="complete" data-completed = "1"  data-missionid="${missionId}">完成目标</a>
                                <#else>
                                    <a href="javascript:;" class="btn btn-blue doUpdateProgress doTrack" data-track="parent|step_click" data-track_error="step" data-completed = "0"  data-missionid="${missionId}">进度+1</a>
                                </#if>
                            </div>
                        </div>
                    </div>
                </#list>
            </#if>
        <#else>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
        <style>
            .parentApp-layerBox .layerInner{
                width: 76%;
                left: 47%;
                margin: 0 0 0 -35%;
                border-radius: 20px;
            }
            .header{
                padding: 50px 0 0;
            }
            .layerMain.content{
                padding: 10px 40px 0 40px;
            }
            .content>p{
                font-size: 23px;
            }
            .parentApp-layerBox .layerFoot{
                padding: 45px 0 60px;
            }
            .footer a{
                color: #fff;
                background: #41bb54;
                padding: 0.6rem 1.6rem;
                border-radius: 38px;
                font-size: 25px;
            }

        </style>
        <div id="popup_modal" class="popup_modal_block" style="z-index: 28;"></div>
        <div  class="parentApp-layerBox popup_block" tabindex="0" style="left: 0px; display: block; top: 0px; z-index: 30;">
            <div class="layerInner">
                <div class="layerHead header">
                    <p class="popup_title">公告</p>
                </div>
                <div class="layerMain content">
                    <p>亲爱的家长，由于一起作业平台整体产品线升级，"成长心愿单”已于2017年6月15日下线。</p>
                    <p>推荐您升级到最新版家长通，新版中将为您和孩子提供更多的学习奖励。</p>
                </div>
                <div class="layerFoot footer">
                    <a href="http://wx.17zuoye.com/download/17parentapp?cid=203005">立即升级</a>
                </div>
            </div>
        </div>
    </#escape>

</@layout.page>

