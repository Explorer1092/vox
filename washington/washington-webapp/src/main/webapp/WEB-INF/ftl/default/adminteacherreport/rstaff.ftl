<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-教研员测评报告"
pageJs=["rstaff", "jquery"]
pageJsFile={"rstaff": "public/script/adminteacherreport/rstaff"}
pageCssFile={"index": ["/public/skin/adminteacherreport/css/skin", "/public/skin/adminteacherreport/css/common"]}>
<div id="rstaffPage" class="report_wrap city" ref="roportTitle" v-cloak>
    <#--城市报告-->
    <!--头部 标题-->
    <div class="r_header">
        <div class="r_header_outer">
            <div class="r_header_inner">
                <a href="/" class="logo"></a>
                <div class="text01 title" >
                    <p class="title_city_name">{{reportTitle[0]}}</p>
                    <p class="title_grade_report">{{reportTitle[1]}}</p>
                </div>
                <div class="active_text01 reminder_btn" @click="clickReminder">建议与报错</div>
                <div class="active_text01 download" @click="downloadReport"> <i class="icon_download"></i>下载报告</div>
            </div>
        </div>
    </div>
        <#--主体内容-->
    <div class="r_main" >
        <!--左边栏 目录-->
        <div class="r_sidebar_a">
            <!--1.测评情况-->
            <div class="menu_box" v-for="(bTitle,bIndex) in catalog" :key="bIndex" ref="titleName">
                <div :class="['text03','title',{'active_menu':activeColor==bIndex}]"
                     @click="bigScroll(bIndex)">{{bTitle.title}}
                    <i :class="['arrow',{'down_arrow':ulActive===bIndex}]" v-if="catalog[bIndex].childTitle.length>0"></i>
                </div>
                <ul class="drop_menu text04" v-show="ulActive===bIndex">
                    <li v-for="(sTitle,index) in bTitle.childTitle"
                        @click="smallScroll(index,$event)"
                        :key="index"
                        :class="{'active_menu':sTitleActive==index && bIndex === ulActive}" >{{sTitle}}</li>
                </ul>
            </div>
        </div>
        <!--右边栏 报告内容-->
        <div class="right_box">
            <div class="content_box">
                <div class="box_hidden">

                    <#--2 测评工具   #ee784f-->
                     <#--3 区教研员   #3ab2b3-->
                        <#--1 市教研员 #377dc1-->
                        <#--4 省教研员  4f3892-->
                       <#--5 校长 393390-->
                    <#--封皮-->
                    <div class="download_box beginPageBox " :style="{background: bgColor}"  >
                        <img src="<@app.link href='public/resource/adminteacherreport/images/begin_page_bg.png'/>" alt="" class="coverBg" >
                        <div class="beginNum">{{ number }}</div>
                        <div class="beginName">
                            <p class="beginNameTitle" >{{ reportTitle[0]}}</p>
                            <div>
                                <p class="beginNameSmallTitle">{{ reportTitle[1]}}</p>
                                <p>{{ reportSubTitle[0]}}{{reportSubTitle[1]}}</p>
                            </div>

                        </div>
                        <div class="botContent">
                            <p class="date">{{ nowDate }}</p>
                            <p class="copyRight">Copyright©{{ nowYear }} 17Zuoye Corporation.All rights reversed.</p>
                        </div>
                    </div>
                    <#--目录-->
                    <div class="download_box catalog_wrap ">
                        <div class="catalog_title_box">
                            <div class="main_title city_name">{{reportTitle[0]}}</div>
                            <div class="main_title report_name">{{reportTitle[1]}}</div>
                            <div class="sub_title">{{reportDesc[0]}}</div>
                            <div class="sub_title">{{reportSubTitle[0]}}{{reportSubTitle[1]}}</div>
                        </div>
                        <div class="content">
                            <div class="text01">目录</div>
                            <div class="first_menu_item clearfix" v-for="(bTitle,bIndex) in catalog" :key="bIndex" ref="titleName">
                                <span class="menu_name fl">{{bTitle.title}}</span>
                                <span class="menu_page fr">{{ bIndex + 1}}</span>
                                <!--二级标题-->
                                <div class="second_menu_item fr" v-for="(sTitle,index) in bTitle.childTitle">
                                    <span class="menu_name fl">{{sTitle}}</span>
                                    <span class="menu_page fr">{{ bIndex + 1}}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <#--封底-->
                    <div class="download_box page_wrap" :style="{background: bgColor}">
                        <div class="page_title">版权声明</div>
                        <div class="copyright_text">
                            本报告版权属于一起教育科技，未得到一起教育科技同意，
                            不得使用或公开报告的任何内容
                        </div>
                        <div class="er_code">
                            <img src="<@app.link href='public/resource/adminteacherreport/images/qrCode.png'/>" alt="" class="coverBg" >
                        </div>
                        <div class="copyright_info">Copyright © {{ nowYear }} 17Zuoye Corporation. All rights reserved.</div>
                    </div>
                </div>
                <#--内容-->
                <div id="page_content" class="download_box" v-for="(mItem, mIndex) in main" :key="mIndex">
                    <#--页眉-->
                    <div class="page_header">
                        <span class="page_header_left">{{ mItem.title }}</span><span class="page_header_right">{{ mIndex + 1 }}</span>
                    </div>
                    <#--scroll 测试报告想,项目实施过程-->
                    <div class="text01 big_title">{{ mItem.title }}</div>
                    <#--<div class="text04 del_txt">{{ mItem.desc }}</div>-->
                    <div class="text04 del_txt">
                        <ul class="del_box">
                            <li class="text04 del_txt" v-for="(item, index) in mItem.desc" :key="index">
                                {{ item }}
                            </li>
                        </ul>
                    </div>
                    <div class="projectBox" v-if="mIndex === 1 && (regionLevel == 'school')">
                            <div class="projectList projectList01">
                                <div class="projectLabel">测评工具研发</div>
                                <div class="projectRight">此次测评使用的试卷均由有经验的教研员命制，在经过命题框架研发及后续试测、测评工具分析等过程验证后，信效度均好。</div>
                            </div>
                            <div class="projectList projectList02">
                                <div class="projectLabel">施测</div>
                                <div class="projectRight">
                                    <ul>
                                        <li>线上施测</li>
                                        <li>各年级参测人数见表1</li>
                                        <li>测评内容见表2</li>
                                    </ul>
                                </div>
                            </div>
                            <!-- diff 只有两行文字时上下padding为20 -->
                            <div class="projectList projectList03 diff">
                                <div class="projectLabel">数据分析</div>
                                <div class="projectRight">经典测量理论（CTT）与项目反映理论（IRT）结合，分析了学生能力及题目质量</div>
                            </div>
                            <div class="projectList projectList04 diff">
                                <div class="projectLabel">输出反馈报告</div>
                                <div class="projectRight">
                                    <ul>
                                        <li>呈现学生在学科技能，能力素养等方面的表现</li>
                                        <li>呈现题目难度，区分度等指标（参见《测评工具分析报告》）</li>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    <div class="download_middle" v-for="(dItem, dIndex) in mItem.contents" :key="dIndex">
                        <div class="text02 small_title">{{ dItem.title }}</div>
                        <ul class="del_box">
                            <li class="text04 del_txt" v-for="(tItem, tIndex) in dItem.text" :key="tIndex">
                                {{ tItem }}
                            </li>
                            <!--echarts-->
                            <li class="canvas3_01" v-for="(eItem, eIndex) in dItem.echart" :key="eIndex">
                                <template v-if="eItem.echartData instanceof Array">
                                    <div class="canvas_box"
                                         :class ="'echarts' + mIndex + dIndex + eIndex"
                                         v-for="(eeItem, eeIndex) in eItem.echartData" :key="eeIndex">
                                    </div>
                                </template>
                                <template v-else >
                                    <div class="canvas_box" :class ="'echarts' + mIndex + dIndex + eIndex">
                                    </div>
                                </template>
                                <div class="text04 canvas_name">{{ eItem.echartTitle }}</div>
                                <div class="text04 canvas_del">
                                    <ul class="del_box">
                                        <li class="text04 del_txt" v-for="(descItem, descIndex) in eItem.echartDesc" :key="descIndex">
                                            {{ descItem }}
                                        </li>
                                    </ul>
                                <#--{{ eItem.echartDesc }}-->
                                </div>
                            </li>
                            <#--table-->
                            <li class="table" v-for="(gItem, gIndex) in dItem.grid"  :key="gIndex">
                            <#--表一-->
                                <div v-if="dItem.grid[gIndex].gridType === '1'">
                                    <div class="text04 table_name" >{{gItem.gridTitle }}</div>
                                    <div class="table_box">
                                        <table :class="{table2_01:mIndex===1&&gItem.gridData[0]}">
                                            <thead>
                                            <tr>
                                                <th :class="{'nameWidth':tdItem.value==='得分率(%)'}" :style="{ 'background-color':tdItem.byColor,'color':tdItem.fontColor}" v-for="(tdItem,tdIndex) in gItem.gridData[0]">{{tdItem.value}}</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr  v-for="(trItem,trIndex) in gItem.gridData"  :key="trIndex">
                                                <td :class="{'blank':trIndex===0}"
                                                    :style="{'background-color':tdItem.byColor,'color':tdItem.fontColor}"
                                                    v-for="(tdItem,tdIndex) in trItem">
                                                    {{tdItem.value=='NA'?'':tdItem.value}}
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <div v-else-if="dItem.grid[gIndex].gridType === '2'">
                                    <div class="text04 table_name" >{{gItem.gridTitle }}</div>
                                    <div class="table_box">
                                        <table class="table4_03">
                                            <thead class="text05">
                                            <tr>
                                                <th :style="{ 'background-color':tdItem.byColor,'color':tdItem.fontColor}"
                                                    v-for="(tdItem,tdIndex) in gItem.gridData[0]"
                                                    :class="{'th4_31':tdIndex === 0,'th4_32':tdIndex>=1}">{{tdItem.value}}</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr v-for="(trItem,trIndex) in gItem.gridData"  :key="trIndex">
                                                <td :class="{'blank':trIndex===0,'td4_31':tdIndex===0,'td4_32':tdIndex>=1}"
                                                    :style="{'background-color':tdItem.byColor,'color':tdItem.fontColor}"
                                                    v-for="(tdItem,tdIndex) in trItem">{{tdItem.value=='NA'?'':tdItem.value}}</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <div v-else-if="dItem.grid[gIndex].gridType === '0'">
                                    <div class="text04 table_name" >{{gItem.gridTitle }}</div>
                                    <div class="table_box">
                                        <table class="table2_02">
                                            <thead>
                                            <tr>
                                                <th
                                                        :style="{ 'background-color':thItem.byColor,'color':thItem.fontColor}"
                                                        v-for="(thItem,thIndex) in table02.table02Th"
                                                        :class="{'th01':thIndex===0,'th02':thIndex===1,'th03':thIndex>=2}">{{thItem.value}}</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr v-for="(tdItem,tdIndex) in table02.table02Tr" :key="tdIndex" :class="{'meg_line':tdItem[0].rowSpan!=''}">
                                                <td :rowspan="ttdItem.rowSpan"
                                                    :class="{'td01':ttdItem.rowSpan!='',
                                                        'td02':ttdIndex===0&&ttdItem.rowSpan===''||ttdIndex===1&&tdItem[0].rowSpan!='',
                                                        'td03':ttdItem.rowSpan!=''&&ttdIndex!=1&&ttdIndex!=0||ttdIndex>=1&&ttdItem.rowSpan===''}"
                                                    :style="{'background-color':ttdItem.byColor,'color':ttdItem.fontColor}"
                                                    v-for="(ttdItem,ttdIndex) in tdItem">
                                                    {{ttdItem.value=='NA'?'':ttdItem.value}}
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <div class="text04 table_del">
                                    <ul class="del_box">
                                        <li class="text04 del_txt" v-for="(deItem, deIndex) in gItem.gridDesc" :key="deIndex">
                                            {{deItem}}
                                        </li>
                                    </ul>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#--建议报错弹窗-->
    <div class="pop_up_wrap" v-show="popUpBox.isOpen">
        <div class="main_box">
            <div class="tip_text">
                <p>感谢您阅读报告。</p>
                <p>请写下您的建议（如呈现更多的指标）或批评，我们会及时处理</p>
            </div>
            <div class="text_box">
                <textarea name="" id="" cols="30" rows="10" class="text_content"></textarea>
            </div>
            <div class="btns"><span class="left_btn" @click="submitBtn">提交</span><span class="right_btn" @click="hiddenBtn">暂时隐藏</span></div>
        </div>
    </div>
    <#--弹窗-点击下载报告后的-->
    <div class="pop_up_download" v-show="downloadIsShow">
        <div class="convert-report-box">
            <div class="header-title">系统提示</div>
            <div class="loading-icon"></div>
            <p>离线报告生成中（预计需要30~60s），请耐心等候</p>
        </div>
    </div>

 </div>
    <#include "./footer.ftl">
</div>
</@layout.page>