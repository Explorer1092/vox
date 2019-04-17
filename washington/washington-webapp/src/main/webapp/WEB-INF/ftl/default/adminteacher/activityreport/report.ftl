<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-校长-测评"
pageJs=["activityReport", "jquery"]
pageJsFile={"activityReport": "public/script/adminteacher/activityreport/report"}
pageCssFile={"index": ["/public/skin/adminteacher/css/activityreport",  "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
    <div class="outercontainer" id="activityReport" v-cloak>
        <div class="container">
            <!-- 主体 -->
            <div class="mainBox">
                <div class="mainInner">
                    <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                    <div class="contentBox">
                        <!-- 操作按钮 -->
                        <div class="operateBox">
                            <div class="unloadBtn" @click="downloadReport" >下载报告</div>
                        </div>
                        <!-- 主要内容 -->
                        <div class="downloadBox">
                            <div class="downloadInner">
                                <!-- 首页 -->
                                <div class="loadSection l-section01">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon01.png'/>" alt="" class="l-section01-img1">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon02.png'/>" alt="" class="l-section01-img2">
                                    <div class="fileBox">
                                        <div class="fileName" v-text="reportName"></div>
                                        <div class="fileText">数据报告</div>
                                    </div>
                                    <div class="fileLogo"></div>
                                </div>
                                <!-- 目录 -->
                                <div class="loadSection l-section02">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon03.png'/>" alt="" class="l-section02-img1">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon04.png'/>" alt="" class="l-section02-img2">
                                    <div class="catalogTitle">
                                        <div class="chinese">目录</div>
                                        <div class="english">Catalogue</div>
                                    </div>
                                    <ul class="catalogList">
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line"></span>
                                            <span class="title">前言</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line02"></span>
                                            <span class="title">1.参与概况</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line03"></span>
                                            <span class="title">2.得分状况</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line04" style="border: 1px dashed #1b92ff;"></span>
                                            <span class="title">3.成绩分布</span>
                                        </li>
                                        <li v-show="noData4">
                                            <span class="dot"></span>
                                            <span class="line line05" ></span>
                                            <span class="title">4.答题速度</span>
                                        </li>
                                    </ul>
                                </div>
                                <!-- 前言 page2 -->
                                <div class="loadSection">
                                    <div class="prefaceTitle">前言</div>
                                    <div class="prefaceText">
                                        <p> 一起教育科技是全球领先的K12智能教育平台，旗下拥有一起作业、一起学和一起公益三大品牌。怀着“让学习成为美好体验”的使命，一起教育科技致力于用前沿的教育科技、优质的教育内容和持续的教育热情，为K12阶段的学校、家庭、社会教育场景，提供更为高效、美好的产品和体验。经过六年的沉淀与发展，如今在全国，已有31个省市、近12万所学校、超过6000万的用户乐于使用我们的平台，开启了智能教育的全新体验。面对未来，一起教育科技将努力让知识和能力一起， 构建学生核心素养；让乡镇和城市一起，共享优质教育资源；让科技和教育一起，实现学习美好体验。</p>
                                        <p>一起教育科技活动平台是在核心素养的时代背景下应运而生的线上活动工具，用以发起、承接小学阶段各省、市、区、校级别的数学、语文、英语趣味活动。平台紧密结合小学阶段知识体系，注重趣味性，旨在通过趣味活动提升学生的学习兴趣、培养思维灵活性、启发学生寻找规律培养探索精神。</p>
                                        <p>一起教育科技活动平台已累计承接了数百场趣味活动，累计服务学生数达百万量级，未来活动平台会致力于提升活动丰富度和趣味性，同时从知识和能力角度完善内容与功能，更好地服务广大小学老师和学生！</p>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 1.参与概况 page3 -->
                                <div class="loadSection l-section01">
                                    <div class="prefaceTitle">1.参与概况</div>
                                    <#--why-->
                                    <ul class="surveyBox" v-show="noData1">
                                        <li >
                                            <span class="s-title">活动类型：</span>
                                            <span class="blueTxt">{{main01.activityType}}</span>
                                        </li>
                                        <li >
                                            <span class="s-title">年级：</span>
                                            <span class="blueTxt" v-for="(cList,cIndex) in main01.clazz"> <i v-show="cIndex!=0">,</i> {{cList}}</span>
                                        </li>
                                        <li>
                                            <span class="s-title">活动时间：</span>
                                            <span class="blueTxt">{{main01.activityDate}}</span>
                                        </li>
                                        <li>
                                            <span class="s-title">时间限制：</span>
                                            <span class="blueTxt">{{main01.limitTime}}</span>
                                        </li>
                                        <li v-if="main01.regionLevel==='city'">
                                            <span class="s-title">实际参与区域数：</span>
                                            <span class="blueTxt">{{main01.regions}}</span>
                                        </li>
                                        <li v-if="main01.regionLevel!=='school'">
                                            <span class="s-title">实际参与学校数：</span>
                                            <span class="blueTxt">{{main01.schools}}</span>
                                        </li>
                                        <li>
                                            <span class="s-title">实际参与班级数：</span>
                                            <span class="blueTxt">{{main01.clazzs}}</span>
                                        </li>
                                        <li>
                                            <span class="s-title">实际参与学生数：</span>
                                            <span class="blueTxt">{{main01.students}}</span>
                                        </li>
                                        <li>
                                            <span class="s-title">人均参与次数：</span>
                                            <span class="blueTxt">{{main01.participationNums}} </span>
                                        </li>
                                    </ul>
                                    <div class="tipsBox" v-show="noData1">
                                        <div class="noteText margin-t">
                                            <p>注：</p>
                                            <p>每位学生在活动时间内，每日最多可参加一次活动。</p>
                                        </div>
                                    </div>
                                    <p class="noData" v-show="!noData1" >暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 1.参与概况--表格 page4 -->
                                <div class="loadSection l-section01" v-show="noData1">
                                    <div class="prefaceTable" v-if="grid.gridHead!='undefined'">
                                        <table>
                                            <thead >
                                                <tr>
                                                    <th  v-for="(ghList,ghIndex) in grid.gridHead" :key="ghIndex">{{ghList}}</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="(grList,grIndex) in grid.gridData" :key="grIndex">
                                                    <td v-for="(gdList,gdIndex) in grList" :key="gdIndex">{{gdList}}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <#--1.参与概况-柱状图-->
                                <div class="loadSection l-section01"   v-show="noData1" v-if="barMaps!=undefined && cList.regions>'1'" v-for="(cList,cIndex) in barMaps" :key="cIndex">
                                     <div class="canvasBox echarts0000" v-bind:class="'echarts0000' + cIndex">
                                    </div>
                                    <div :class="['tipsBox',{'echartssType': cList.xAxisData.length >'40'}]">
                                        <div :class="['tipsBox',{'echartssType': cList.xAxisData.length >'40'}]">
                                            <div class="noteText" v-if="cList.topThree!=''">
                                                <p v-show="cList.topThree!=undefined">1.<span class="redTxt">{{cList.clazzLevel}}</span>共<span class="redTxt">{{cList.regions}}个{{cList.viewRegion}}</span>参与，积极性最高的{{cList.viewRegion}}依次是<span class="redTxt" v-for="(btList,btIndex) in cList.topThree" :key="btIndex"> <i v-show="btIndex!==0">,</i> {{btList}}</span></p>
                                                <p v-show="cList.lastOne!=undefined && cList.lastOne.lastName!=undefined">2.积极性较低的<span class="redTxt">{{cList.viewRegion}}</span>是<span class="redTxt">{{cList.lastOne.lastName}}{{cList.lastOne.lastValue}}</span>，低于年级平均水平<span class="redTxt">{{cList.diff}}</span>次/人</p>
                                            </div>
                                        </div>
                                        <div class="noteText margin-t">
                                            <p>注：</p>
                                            <p>平均参与次数=参与次数/学生人数</p>
                                        </div>
                                    </div>
                                    <p v-if="barMaps==undefined && barMaps.length==undefined" class="noData scoreNodata" >暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>

                            <#--2.得分状况 -->
                                <div class="loadSection l-section02">
                                    <div class="prefaceTitle">2.得分状况</div>
                                    <!-- canvas 左边饼图 -->
                                    <div v-show="noData2" >
                                        <div class="canvasBox scoreDetails scorePieEchars" v-show="main02WholeScoreMap.fullMarks!='不限总分'"></div>
                                        <div :class="['tipsBox',  'rightTipsBox',{'scorePie':main02WholeScoreMap.fullMarks==='不限总分'}]">
                                            <div class="tipsText scrocText">总分：<span class="redTxt">{{main02WholeScoreMap.fullMarks}}</span></div>
                                            <div class="tipsText scrocText">最高分平均分：<span class="redTxt">{{main02WholeScoreMap.highAvgScore}}分</span></div>
                                        </div>
                                    </div>
                                    <div class="tipsBox" v-show="noData2">
                                        <div class="noteText">共<span class="redTxt">{{main02WholeScoreMap.grades}}</span>个年级，<span class="redTxt">{{main02WholeScoreMap.regions}}</span>个<span class="redTxt">{{main02WholeScoreMap.wholeViewRegion}}</span>参与本次测评，整体最高分平均分为<span class="redTxt">{{main02WholeScoreMap.highAvgScore}}分</span></div>
                                        <div class="noteText margin-t scoreNoteText">
                                            <p>注：</p>
                                            <p>1.某学生的最高得分=在活动期间，学生参与活动取得的最高得分。（学生每日最多可参加一次活动）</p>
                                            <p>2.最高分平均分=各学生的最高得分之和/实际参与学生数量</p>
                                        </div>
                                    </div>
                                    <p class="noData scoreNodata" v-show="!noData2" >暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 2.得分状况--表格 page6 -->
                                <div class="loadSection l-section02" v-show="noData2">
                                    <div class="prefaceTable" v-if="main02Grid.gridHead!='undefined'">
                                        <table>
                                            <thead >
                                            <tr>
                                                <th  v-for="(ghList,ghIndex) in main02Grid.gridHead" :key="ghIndex">{{ghList}}</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr v-for="(grList,grIndex) in main02Grid.gridData" :key="grIndex">
                                                <td v-for="(gdList,gdIndex) in grList" :key="gdIndex">{{gdList}}</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 2.得分状况--柱状图 page6 -->
                                <div class="loadSection l-section02" v-show="noData2" v-if="main02barMaps.length>0 && sList.regions>'1'" v-for="(sList,sIndex) in main02barMaps" :key="sIndex">
                                    <div  class="canvasBox" v-bind:class="'scoreEcharts' + sIndex">
                                    </div>
                                    <div class="['tipsBox', {'echartssType': cList.xAxisData.length >'40'}]" >
                                        <div class="tipsBox echartssType">
                                            <div class="noteText" >
                                                <p v-show="sList.topThree!=undefined&&sList.regions>=2">1.<span class="redTxt">{{sList.clazzLevel}}</span>共<span class="redTxt">{{sList.regions}}个</span>{{sList.viewRegion}}参与，表现较好的<span class="redTxt">{{sList.viewRegion}}</span>依次是<span class="redTxt" v-for="(btList,btIndex) in sList.topThree" :key="btIndex"><i v-show="btIndex!==0">,</i>{{btList}}</span></p>
                                                <p  v-show="sList.lastOne!=undefined && sList.lastOne.lastName!=undefined">2.表现一般的是<span class="redTxt">{{sList.lastOne.lastName}}{{sList.lastOne.lastValue}}</span>，比年级平均水平低<span class="redTxt">{{sList.diff}}分</span></p>
                                            </div>
                                        </div>
                                    </div>
                                    <p v-if="main02barMaps.length==0 && main02barMaps.length==undefined" class="noData scoreNodata" >暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 3.成绩分布 -->
                                <div class="loadSection l-section03">
                                    <div class="prefaceTitle">3.成绩分布</div>
                                    <#--3.成绩分布饼图-->
                                    <div class="tableBox clearfix" v-show="noData3">
                                        <!-- 左侧表格 -->
                                        <div class="leftTable" v-if="main03WholeGrid!=undefined">
                                            <table>
                                                <thead >
                                                    <tr>
                                                        <th v-if="main03WholeGrid.gridHead!=undefined"  v-for="(ghList,ghIndex) in main03WholeGrid.gridHead" :key="ghIndex">{{ghList}}</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr v-if="main03WholeGrid.gridData!=undefined" v-for="(grList,grIndex) in main03WholeGrid.gridData" :key="grIndex">
                                                        <td v-for="(gdList,gdIndex) in grList" :key="gdIndex">{{gdList}}</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- canvas图表 -->
                                        <div class="rightTable canvasBox studyLevelChart1 pieEcharts"></div>
                                        <div class="tipsBox studyTipbox" >
                                            <div class="tipsBox">
                                                <div class="noteText" v-if="main03WholeGrid.topOne!=undefined && main03WholeGrid.lastOne!=undefined">
                                                    <p><span class="redTxt">{{main03WholeGrid.topOne}}</span>的人数比例最高，<span class="redTxt">{{main03WholeGrid.lastOne}}</span>的人数比例最低</p>
                                                </div>
                                            </div>
                                            <div class="noteText margin-t ">
                                                <p>注：</p>
                                                <p>此处的成绩取每位学生活动期间的最好成绩</p>
                                            </div>
                                        </div>
                                    </div>
                                    <p class="noData" v-show="!noData3">暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <#--3.成绩分布 表格-->
                                <div class="loadSection l-section03" v-show="noData3" >
                                        <div class="prefaceTable" v-if="main03Grid!=undefined">
                                            <table>
                                                <thead >
                                                    <tr v-if="main03Grid.gridHead!=undefined">
                                                        <th  v-for="(ghList,ghIndex) in main03Grid.gridHead" :key="ghIndex">{{ghList}}</th>
                                                    </tr>
                                                </thead>
                                                <tbody v-if="main03Grid.gridData!=undefined">
                                                    <tr v-for="(grList,grIndex) in main03Grid.gridData" :key="grIndex">
                                                        <td v-for="(gdList,gdIndex) in grList" :key="gdIndex">{{gdList}}</td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                        <div class="pageNum">
                                            <i class="pageLogo"></i>
                                            <span class="num"></span>
                                        </div>
                                    </div>
                                <#--3.成绩分布 柱状图-->
                                <div class="loadSection l-section03 studyBarmap" v-show="noData3" v-if="main03barMaps!==undefined && fList.regions>'1'" v-for="(fList,fIndex) in main03barMaps" :key="fIndex">
                                    <div  class=""  v-if="fList!=undefined&&fList.isHaveData!=undefined">
                                        <div class="scorenoValue noData scoreNodata studyBarmapNodata">暂无数据</div>
                                        <div class="tipsBox margin-t studyBarmapNoData">
                                            <div class="noteText ">
                                                <p>{{fList.message}}</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div v-show="fList.isHaveData==undefined">
                                        <div class="canvasBox "   v-bind:class="'studybarEcharts' + fIndex">
                                        </div>
                                        <div class="tipsBox"  v-bind:class="{'echartssType': fList.xAxisData.length >'40'}" v-if="fList.xAxisData!=undefined && fList.xAxisData.length>0">
                                            <div class="tipsBox echartssType" v-if="fList!=undefined">
                                                <div class="noteText" >
                                                    <p v-if="fList.topThree!=''">1.<span class="redTxt">{{fList.clazzLevel}}</span>，高分人数比例较高的{{fList.viewRegion}}依次是<span class="redTxt" v-for="(btList,btIndex) in fList.topThree" :key="btIndex"><i v-show="btIndex!==0">,</i>{{btList}}</span></p>
                                                    <p v-if="fList.lastOne.lastName!=undefined">2.高分人数比例较低的{{fList.viewRegion}}是<span class="redTxt">{{fList.lastOne.lastName}}{{fList.lastOne.lastValue}}</span>，比年级平均水平低<span class="redTxt">{{fList.diff}}%</span></p>
                                                </div>
                                            </div>
                                            <div class="noteText margin-t">
                                                <p>注：</p>
                                                <p v-if="fList!=undefined">高分的定义：{{fList.topScoreDefineMsg}}</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                            <#--4.答题速度-->
                                <div class="loadSection l-section04" v-show="noData4">
                                    <div class="prefaceTitle">4.答题速度</div>
                                    <div class="tableBox clearfix" >
                                        <!-- 左侧表格 -->
                                        <div class="leftTable" v-if="main04WholeGrid.gridHead!=undefined" >
                                            <table>
                                                <thead>
                                                <tr>
                                                    <th v-for="(ahList,ahIndex) in main04WholeGrid.gridHead" :key="ahIndex">{{ahList}}</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr  v-for="(arList,arIndex) in main04WholeGrid.gridData" :key="arIndex">
                                                    <td v-for="(adList,adIndex) in arList" :key="adIndex">{{adList}}</td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- canvas图表 -->
                                        <div class="rightTable canvasBox answerBarMapEcharts"></div>
                                        <div class="tipsBox studyTipbox" >
                                            <div class="tipsBox">
                                                <div class="noteText">
                                                    <p v-if="main04topOneData.topOneName!=undefined">{{main04topOneData.topOneName}}答题速度较快{{main04topOneData.topOneVal}}，比整体水平高{{main04topOneData.topDiff}}</p>
                                                    <p v-if="main04lastOneData.lastOneName!=undefined">{{main04lastOneData.lastOneName}}答题速度一般{{main04lastOneData.lastOneVal}}，比整体水平少{{main04lastOneData.lastDiff}}</p>
                                                </div>
                                            </div>
                                            <div class="noteText margin-t ">
                                                <p>注：</p>
                                                <p>答题速度=答对题目数量/答题总时间</p>
                                            </div>
                                        </div>
                                    </div>
                                    <p class="noData " v-show="!noData4">暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <#--4.答题速度 表格-->
                                <div class="loadSection l-section04" v-show="noData4">
                                    <div class="prefaceTable">
                                        <table>
                                            <thead >
                                            <tr>
                                                <th v-for="(a1List,a1Index) in main04Grid.gridHead" :key="a1Index">{{a1List}}</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr v-for="(ar1List,ar1Index) in main04Grid.gridData" :key="ar1Index">
                                                <td v-for="(ad1List,ad1Index) in ar1List" :key="ad1Index">{{ad1List}}</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <#--4.答题速度 柱状图-->
                                <div class="loadSection l-section04" v-show="noData4" v-if="main04barMaps!=undefined&& gList.regions>'1'" v-for="(gList,gIndex) in main04barMaps" :key="gIndex">
                                    <div  class="canvasBox " v-bind:class="'answerSpeedEcharts' + gIndex" v-if="gList"></div>
                                    <div :class="['tipsBox', {'echartssType': gList.xAxisData.length >'40'}]" >
                                        <div :class="['tipsBox', {'echartssType': gList.xAxisData.length >'40'}]">
                                            <div class="noteText"  >
                                                <p v-if="gList.topThree!=''">1.<span class="redTxt">{{gList.clazzLevel}}</span>答题速度快的<span class="redTxt">{{gList.viewRegion}},</span>依次是<span class="redTxt" v-for="(btList,btIndex) in gList.topThree" :key="btIndex">{{btList}},</span></p>
                                                <p v-if="gList.lastOne!=undefined && gList.lastOne.lastName!=undefined">2.<span class="redTxt">{{gList.lastOne.lastName}}</span>的答题速度一般<span class="redTxt">{{gList.lastOne.lastValue}}</span>,比年级平均速度少<span class="redTxt">{{gList.diff}}(题/每分钟)</span></p>
                                            </div>
                                        </div>
                                    </div>
                                    <p v-if="main04barMaps==undefined && main04barMaps.length==undefined" class="noData scoreNodata" >暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 结束页 -->
                                <div class="loadSection">
                                    <div class="endLogo"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="totop-btn" id="gotoTop" @click="bindGlobalEvent" v-show="navi"></div>
        <!-- 报告生成中弹窗 -->
        <div class="loadPopUp" id="loadWindow" style="display: none">
            <div class="convert-report-box" >
                <div class="sysHint">温馨提示</div>
                <div class="loading-icon"></div>
                <p>离线报告生成中，大约需要60s~120s,请稍候</p>
            </div>
        </div>
        <!-- 浏览器不兼容提示弹窗 -->
        <div class="change-browser-box" style="display: none;">
            <p class="sorry-tip">抱歉，您的浏览器不支持次功能<br>可以通过安装一下浏览器来解决此问题</p>
            <div class="browser-box">
                <a href="#">
                    <div class="browser-icon liebao-icon"></div>
                    <p>猎豹浏览器</p>
                </a>
                <a href="#">
                    <div class="browser-icon chrome-icon"></div>
                    <p>谷歌浏览器</p>
                </a>
                <a href="#">
                    <div class="browser-icon firefox-icon"></div>
                    <p>火狐浏览器</p>
                </a>
                <a href="#">
                    <div class="browser-icon qihu-icon"></div>
                    <p>360浏览器</p>
                </a>
            </div>
            <p class="other-tip">温馨提示：<br>1.如果使用猎豹浏览器仍无法下载，请在页面点击鼠标右键，左键选择“切换到极速模式” <br>2.如果使用360浏览器仍无法下载，请左键点击浏览器地址栏右侧绿色e"<i></i>"标志，选择 “极速模式(推荐)”</p>
        </div>
    </div>
    <#include "../footer.ftl">
</@layout.page>