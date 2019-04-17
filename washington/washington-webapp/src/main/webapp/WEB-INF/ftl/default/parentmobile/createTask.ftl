<#import './layout.ftl' as layout>

    <@layout.page title="创建目标" className='CreateTask' pageJs="second" >

    <#escape x as x?html>
        <#assign topType = "topTitle">
        <#assign topTitle = "创建目标">
        <#include "./top.ftl" >

        <#assign isFirstStep = (step!"v1") == "v1">

        <#if isFirstStep >
            <#assign freeGold = 10>
            <div class="pr-box doMissionBox" >
                <ul class="pr-list">
                    <#if isNotArranged!false>
                        <li class="clearfix">
                            <div class="pic pic-gold"><span>+${freeGold}</span></div>
                            <div class="txt">
                                <div>${freeGold}学豆奖励</div>
                                <div>每月免费赞助家长${freeGold}学豆</div>
                            </div>
                            <div class="box"><a href="javascript:;" class="green set-mission doSetIntegralMission" data-integral_num="${freeGold}">添加</a></div>
                        </li>
                    </#if>
                    <li class="clearfix set-mission" data-key="auto">
                        <div class="pic pic-add"></div>
                        <div class="txt">
                            <div>自定义奖励</div>
                            <div></div>
                        </div>
                        <div class="box"><a href="javascript:;" class="doSetIntegralMission green">添加</a></div>
                    </li>
                </ul>
            </div>
        </#if>

        <#include "./createTaskStep2.ftl">

    </#escape>
</@layout.page>
