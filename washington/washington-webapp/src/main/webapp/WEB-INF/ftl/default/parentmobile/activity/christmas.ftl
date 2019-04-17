<#import '../layout.ftl' as layout>

<#assign extraRequireJs = [
    "public/script/parentMobile/activity/christmas"
]>

<@layout.page className='parentMobileThanksGivingDay' pageJs="activityThanksGivingDay_isExtraRequireJs" title="圣诞节" extraRequireJs = extraRequireJs>
    <#assign staticImgBaseUrl = "activity/thanksGivingDay/">
    <#include "../constants.ftl">
    ${buildLoadStaticFileTag("christmas", "css")}

    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "圣诞节">
        <#include "../top.ftl" >

        <div class="projectChristmas-bg-1">
            <div class="module-1">老师们变身圣诞老人，来奖励完成<br>作业／测验的孩子！</div>
            <div class="module-2">活动时间：12月21日－12月31日</div>
        </div>

        <div class="projectChristmas-bg-2">
            <div class="module-box">
            <div class="module-inner">
                <div class="module-head">活动具体步骤：</div>
                <div class="module-cont">
                    <ul class="list">
                        <li class="teacher">
                            <div class="hd">
                                <div class="portrait"></div>
                                <div class="box">老师需要做</div>
                            </div>
                            <div class="txt">
                                <div class="box">1.布置作业／测验</div>
                                <div class="box">2.将学豆装入圣诞袜</div>
                            </div>
                        </li>
                        <li class="student">
                            <div class="hd">
                                <div class="portrait"></div>
                                <div class="box">学生需要做</div>
                            </div>
                            <div class="txt">
                                <div class="box">3.完成作业／测验</div>
                                <div class="box">4.领取圣诞袜</div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="module-head" style="margin:10px 0 0;">给老师学生送礼物：</div>
                <div class="module-cont" id="sendFlower">
                    <ul class="gift">
                        <li>
                            <div class="box">
                                <div class="ico-flower"></div>
                                <div class="txt">已有<span>{{sfpc}}</span>个家长给老师送花。</div>
                                <a v-if="canSend === true" href="javascript:;" v-on:click="doSendFlower" class="btn">给老师送花</a>
                                <a v-else="canSend === false" href="javascript:;" class="btn">已送花</a>
                            </div>
                        </li>
                        <#--
                        <li>
                            <div class="box">
                                <div class="ico-beans"></div>
                                <div class="txt">已有<span>{{cipc}}</span>个家长给班级贡献<span>{{cic}}</span>个学豆。</div>
                               <a href="javascript:;" class="btn" v-on:click="doSendIntegral">给老师送学豆</a>
                            </div>
                        </li>
                        -->
                    </ul>
                </div>
            </div>
        </div>
        </div>

        <div class="projectChristmas-bg-3"  id="kids">
            <div class="table-head">
                <div class="box">
                    <span style="position: relative;">
                        <select v-on:change="kidChange">
                            <option  v-for="kid in kids" value="{{kid.id}}">{{kid.name}}</option>
                        </select>
                        <b style="font-size: 20px; position: absolute; right: 20px; top: 15px; color: #fff">▼</b>
                    </span>
                    <span class="num-beans">{{sic}}</span>
                    <span class="num-gift">{{ssc}}</span>
                </div>
            </div>
            <div class="table-main">
                <div class="inner">
                    <table>
                        <thead>
                        <tr>
                            <td>学生</td>
                            <td>获得圣诞袜</td>
                            <td>获得的学豆</td>
                        </tr>
                        </thead>
                        <tbody>
                            <tr v-for="student in students">
                                <td>{{student.studentName}}</td>
                                <td>{{student.sc}}</td>
                                <td>{{student.ic}}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </#escape>
    <script src="//cdn.bootcss.com/vue/1.0.10/vue.min.js"></script>

</@layout.page>
