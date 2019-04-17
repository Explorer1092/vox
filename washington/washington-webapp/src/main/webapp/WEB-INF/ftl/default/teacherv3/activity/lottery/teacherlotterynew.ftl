<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="17奖学金活动"
pageJs=["init"]
pageJsFile={"init" : "public/script/teacherv3/activity/teacherlottery"}
pageCssFile={"init" : ["public/skin/project/teachernewlottery/css/newskin"]}
>
<#include "../../../layout/project.header.ftl">
<style>
    .m-footer,.m-footer .m-inner{
        background-color: #fad5d9;
        border-top:0;
    }
    .m-footer .copyright {
        color: #677384;
    }
    .m-footer .m-foot-link a{
        color: #b49d9f;
    }
    .m-footer .link {
        display: none !important;
    }
</style>
<!--弹窗-->
<div class="keyRecords-pop bg-1" style="display: none;" id="recordDialog">
    <div class="inner">
        <a href="javascript:void(0);" class="close js-closeDialog"></a>
        <div class="keyRecords-main">
            <div class="column js-recordContent"></div>
            <div class="content">
                <p class="arrow">温馨提示：</p>
                <p class="info">钥匙数和抽奖次数刷新可能有一段时间的延迟,可以刷新页面或过段时间再来查看，谢谢您的耐心等待!</p>
            </div>
        </div>
    </div>
</div>
<script id="recordDialogTemp" type="text/html">
    <%if(data.keyRecord.length > 0) {%>
    <ul>
        <%for(var i=0;i<data['keyRecord'].length;i++){%>
        <li>
            <%= data['keyRecord'][i].date %>,通过<%= data['keyRecord'][i].scholarKeyType %>获得<span><%= data['keyRecord'][i].keyNum %>把钥匙</span>
        </li>
        <% } %>
    </ul>
    <% }else{%>
    <div style="text-align: center;">暂无记录</div>
    <% } %>
</script>
<!--首页弹窗-->
<div class="keyRecords-pop bg-2" style="display: none;">
    <div class="inner">
        <a href="javascript:void(0);" class="close"></a>
    </div>
</div>
<!--弹窗1-->
<div class="keyRecords-pop bg-3" style="display: none;" id="authDialog">
    <div class="inner">
        <a href="javascript:void(0);" class="close"></a>
        <div class="keyRecords-main">
            <p class="item">恭喜您！在本活动期间完成认证，</p>
            <p class="item">获得<span class="pink">3把钥匙</span></p>
        </div>
    </div>
</div>
<!--弹窗2-->
<div class="keyRecords-pop bg-4" style="display: none;" id="lotteryErrorAlert">
    <div class="inner">
        <a href="javascript:void(0);" class="close"></a>
        <div class="keyRecords-main">
            <p class="item js-errorInfo"></p>
            <div class="btn">
                <a href="javascript:void(0);" class="pink_btn js-closeErrorAlert">确定</a>
            </div>
        </div>
    </div>
</div>
<div class="keyRecords-pop bg-4" style="display: none;" id="lotterySuccessAlert">
    <div class="inner">
        <a href="javascript:void(0);" class="close"></a>
        <div class="keyRecords-main">
            <p class="item">恭喜您！获得<span class="pink js-awardName"></span></p>
            <p class="sub">获得实物奖品的老师，我们的工作人员会在一周内，通过短信的方式联系您，安排发奖事宜。</p>
            <div class="btn">
                <a href="javascript:void(0);" class="pink_btn js-closeSuccessAlert">确定</a>
            </div>
        </div>
    </div>
</div>
<div class="scholarship-box">
    <div class="inner">
        <div class="scholarship-header">活动时间：4月13日-5月31日</div>
        <div class="scholarship-main">
            <div class="ssn-column">
                <div class="sLeft">
                    <p>剩余抽奖次数：<span class="js-freeChanceNo">0</span><span>次</span></p>
                    <p><i></i>我的钥匙：<span class="js-myKeyNo">0</span><span>把</span></p>
                    <a href="javascript:void(0);" class="js-showRecord">查看记录</a>
                </div>
                <div class="sRight">
                    <div class="prize-pool">
                        <div class="pool pool-1"></div>
                        <div class="pool pool-2"></div>
                        <div class="pool pool-3"></div>
                    </div>
                    <ul class="js-lotteryPool">
                        <li data-index="1">
                            <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/key01.png'/>">
                            <div class="tag tagGray">
                                <span class="tag50"></span>
                            </div>
                        </li>
                        <li data-index="2">
                            <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/key02.png'/>">
                            <div class="tag tagGray">
                                <span class="tag25"></span>
                            </div>
                        </li>
                        <li data-index="3">
                            <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/key03.png'/>">
                            <div class="tag tagGray">
                                <span class="tag10"></span>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="ssn-content">
                <div class="cLeft">
                    <div class="top"></div>
                    <div class="mid">
                        <h1>获奖动态</h1>
                        <ul class="list-1 js-bigPrizeList"></ul>
                        <ul class="list-2 js-resultList"></ul>
                    </div>
                    <div class="dot"></div>
                </div>
                <!--转盘-->
                <div class="side-lottery">
                    <div class="btn">
                        <a href="javascript:void(0);" class="turntable-btn js-submitLottery gray">抽奖次数：<span class="js-freeChanceNo">0</span>次</a>
                    </div>
                    <!--抽奖-->
                    <div class="container" id="lotteryItems"></div>
                </div>
            </div>
        </div>
        <div>
            <h2 class="ssn-side-title"></h2>
            <div class="ssn-side" style="text-align:center;">
                <p>17奖学金第2期，奖品升级，精彩继续！</p>
                <p>本次奖学金的评选，完全基于<b>日常作业</b>的【<b>知识点达标情况</b>】。我们相信，高质量完成每一次日常作业，真正地夯实基础，才能不断追求卓越</p>
                <p>热情提示：在新上线的【<b>学情评估</b>】中可以实时查看【<b>知识点达标情况</b>】</p>
                <h5 style="margin-top: 10px;">
                    规则说明:
                </h5>
                <p><span>1</span>用钥匙打开奖池，积攒钥匙，即可打开高级奖池</p>
                <p><span>2</span>【学情评估】里的单元达标率越高，钥匙数量越多</p>
                <p><span>3</span>仅认证老师可以参加活动，活动期间完成认证的老师，有惊喜礼包</p>
            </div>
        </div>
        <div class="scholarship-rule">
            <h2></h2>
            <div class="rule-box">
                <div class="ssn-side">
                    <p><span>1</span><i>【学情评估】中，每个班级的每个【知识点达标率】凡达到10%，老师即可获得1把钥匙。(如右图图所示，左侧班级获得4把钥匙，右侧获得7把钥匙，老师共获得11把钥匙）</i></p>
                    <p><span>2</span><i>4月10日至5月31日期间，老师布置作业涉及的所有班级、所有教材、所有单元的达标情况，都将被计入本次活动</i></p>
                    <p><span>3</span><i>获得实物奖品的老师，我们的工作人员将在一周内联系您，安排发奖事宜</i></p>
                    <p><span>4</span><i>所有奖品与Apple Inc.无关，活动最终解释权归一起作业所有</i></p>
                </div>
                <div class="ssn-picture">
                    <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/image01.png'/>">
                    <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/image02.png'/>">
                </div>
            </div>
        </div>

    </div>
</div>
<script id="bigPrizeTemp" type="text/html">
    <%if(list.length > 0){%>
        <%for(var i=0;i<list.length;i++){ %>
            <li>
                <div class="name"><%= list[i].userName%></div>
                <div class="city"><%= list[i].schoolName%></div>
                <div class="prize"><%= list[i].awardName%></div>
            </li>
        <%}%>
    <%}else{%>
        <li>
            <div>暂无记录</div>
        </li>
    <%}%>
</script>
<script id="lotteryItemTemp" type="text/html">
<%if(type == 1){%>
    <div class="icon icon-00" data-type="1">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize01.png'/>">
    </div>
    <div class="icon icon-01" data-type="2">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize02.png'/>">
    </div>
    <div class="icon icon-02" data-type="3">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize03.png'/>">
    </div>
    <div class="icon icon-03" data-type="7">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize09-v.png'/>">
    </div>
    <div class="icon icon-04" data-type="6">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize07-v.png'/>">
    </div>
    <div class="icon icon-05" data-type="8">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize12-v.png'/>">
    </div>
    <div class="icon icon-06" data-type="5">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize04.png'/>">
    </div>
    <div class="icon icon-07" data-type="4">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize05.png'/>">
    </div>
<% } else if(type == 2) {%>
    <div class="icon icon-00" data-type="4">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize08-v.png'/>">
    </div>
    <div class="icon icon-01" data-type="6">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize10-v.png'/>">
    </div>
    <div class="icon icon-02" data-type="1">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize03.png'/>">
    </div>
    <div class="icon icon-03" data-type="5">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize09-v.png'/>">
    </div>
    <div class="icon icon-04" data-type="7">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize12-v.png'/>">
    </div>
    <div class="icon icon-05" data-type="8">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize00.png'/>">
    </div>
    <div class="icon icon-06" data-type="3">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize04.png'/>">
    </div>
    <div class="icon icon-07" data-type="2">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize06.png'/>">
    </div>
<% } else if(type == 3 || type == 0) {%>
    <div class="icon icon-00" data-type="1">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize04.png'/>">
    </div>
    <div class="icon icon-01" data-type="3">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize10-v.png'/>">
    </div>
    <div class="icon icon-02" data-type="7">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize12-v.png'/>">
    </div>
    <div class="icon icon-03" data-type="2">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize09-v.png'/>">
    </div>
    <div class="icon icon-04" data-type="5">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize11-v1.png'/>">
    </div>
    <div class="icon icon-05" data-type="8">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize00.png'/>">
    </div>
    <div class="icon icon-06" data-type="6">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize12-v.png'/>">
    </div>
    <div class="icon icon-07" data-type="4">
        <img src="<@app.link href='public/skin/project/teachernewlottery/images/scholarship/prize10-v.png'/>">
    </div>
<% } %>
</script>
<#include "../../../layout/project.footer.ftl">
</@layout.page>