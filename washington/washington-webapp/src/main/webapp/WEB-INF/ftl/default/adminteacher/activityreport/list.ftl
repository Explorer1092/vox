<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-趣味测试"
pageJs=["activityList", "jquery"]
pageJsFile={"activityList": "public/script/adminteacher/activityreport/list"}
pageCssFile={"index": ["/public/skin/adminteacher/css/list",  "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
<div class="outercontainer" id="activitylist" v-cloak>
    <div class="container">
        <!-- 主体 -->
        <div class="mainBox" >
            <div class="mainInner">
                <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                <div class="contentBox activityReport">

                    <#include "../nav.ftl">

                    <div class="contentMain">
                        <div class="section">
                            <div class="selectWrap selectWrap02">
                                <div class="selList" id="choiceList">
                                    <span class="selText">活动类型</span>
                                    <a class="selBox" @blur="closeList" href="javascript:void(0)" >
                                        <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp-->
                                        <div id="reportType" class="list"
                                             @click="arrowDirectionType"
                                            >
                                            <span v-text="allActivity"></span>
                                            <i :class="['arrow',{'arrowUp':arrowUp1}]"></i>
                                        </div>
                                        <ul class="hideBox" id="actList" v-show="arrowUp1 === true">
                                            <li :class="{'active':aind===aindex}"
                                                @click="choiceActive(alist,aindex)"
                                                v-for="(alist,aindex) in activityList"
                                                :key="aindex" v-text="alist.name"></li>
                                        </ul>
                                    </a>
                                </div>
                                <div class="selListRight" >
                                    <a class="selBox" @blur="closeTime" href="javascript:void(0)" >
                                        <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp-->
                                        <div class="list" id="reportDate" @click="arrowDirectionTime" >
                                            <span v-text="activityTime"></span>
                                            <i :class="['arrow',{'arrowUp':arrowUp2}]"></i>
                                        </div>
                                        <ul class="hideBox" id="activityTime" v-show="arrowUp2===true">
                                            <li :class="{'active':tind===tindex}"
                                                @click="choiceTime(tlist,tindex)"
                                                v-for="(tlist,tindex) in activityTimeList"
                                                :key="tindex" v-text="tlist.name"></li>
                                        </ul>
                                    </a>
                                </div>
                             </div>
                        </div>
                        <div class="section section02">
                            <div class="secRank">
                                <div class="rankTitle">
                                    <span class="num03">活动名称</span>
                                    <span class="num04">活动时间</span>
                                    <span class="num05">时间限制</span>
                                    <span class="num05">题量限制</span>
                                    <span class="num06">活动报告</span>
                                </div>
                                <ul class="rankList" v-show="activityReportList.length!='0'">
                                    <li class="rankBox" v-for="(rList,rIndex) in activityReportList" :key="rIndex">
                                        <div class="num03" v-text="rList.name" :title="rList.name"></div>
                                        <div class="num04" v-text="rList.activityDate"></div>
                                        <div class="num05" v-text="rList.limitTime"></div>
                                        <div class="num05" v-text="rList.limitAmount=undefined?无:rList.limitAmount"></div>
                                        <div class="num06"><a :href="'/schoolmaster/activityReport/report.vpage?regionLevel='+rList.regionLevel+'&regionCode='+rList.regionCode+'&id='+rList.id+'&reportType='+rList.name">查看</a></div>
                                    </li>
                                </ul>
                                <div class="empty-tip" v-if="activityReportList.length===0">暂无数据</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "../footer.ftl">
</@layout.page>