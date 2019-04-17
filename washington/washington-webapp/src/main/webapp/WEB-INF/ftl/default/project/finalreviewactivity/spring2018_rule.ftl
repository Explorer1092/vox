<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
    title="一起复习，领双重奖学金"
    pageJs=["finalreview"]
    pageJsFile={"finalreview" : "public/script/project/finalreview_spring2018"}
    pageCssFile={"finalreview" : ["public/skin/project/finalreviewactivity/spring2018/css/skin"]}>

    <#include "../../layout/project.header.ftl">
    <div class="rule-wrap">
        <div class="ruleTitle">奖学金评奖细则</div>
        <!--前言-->
        <div class="rulePreface">
            <div class="ruleTag fLeft">前言：</div>
            <div class="txtPreface">为了帮助老师更好地进行期末复习，一起作业精心准备了包含基础、重点专项，试卷等模块，并设立期末复习奖学金，奖励认真复习的老师和学生。</div>
        </div>
        <!--奖学金设置-->
        <div class="awardSetting">
            <div class="ruleTag">奖学金设置：</div>
            <div class="settingTable">
                <table>
                    <thead>
                    <tr>
                        <td>分类</td>
                        <td>用户</td>
                        <td>名额</td>
                        <td>奖品</td>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>每日复习奖学金</td>
                        <td>老师</td>
                        <td>30</td>
                        <td>500教育基金</td>
                    </tr>
                    <tr>
                        <td>师生奖学金</td>
                        <td>老师</td>
                        <td>10</td>
                        <td>2999教育基金</td>
                    </tr>
                    <tr>
                        <td>师生奖学金</td>
                        <td>学生</td>
                        <td>60</td>
                        <td>米兔儿童电话手表</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!--选评规则-->
        <div class="ruleSection">
            <div class="ruleTag">选评规则：</div>
            <h2 class="ruleType-tit"><i class="icon02"></i>每日复习奖学金<i class="icon01"></i></h2>
            <div class="ruleState">每天布置指定类型的复习作业，参加每日评奖，<br>学生完成越多，积累的复习数据越丰富，得奖几率越大。</div>
            <div class="aside">
                <div class="ruleLabel">老师参评条件：</div>
                <p>当日布置过期末复习作业的认证老师</p>
                <p><span class="dotIcon"></span>当天布置了期末复习作业。</p>
                <p><span class="dotIcon"></span>曾布置期末复习基础必过，且未删除。</p>
            </div>
            <div class="aside">
                <div class="ruleLabel">评选规则:</div>
                <div class="numTxt"><span class="num">1.</span><p>从所有符合条件的老师中随机评选；</p></div>
                <div class="numTxt"><span class="num">2.</span><p>布置的班级越多，学生积累的复习数据越多，随机概率越大。</p></div>
                <div class="numTxt">全国参与老师数量巨大，为保证评奖公平性，评选参考复习数据同时会增加一定的随机性</div>
            </div>
            <div class="aside">
                <div class="ruleLabel">实物奖品：</div>
                <p>每天1个500元教育基金，共30个中奖机会。</p>
            </div>
            <div class="aside">
                <div class="ruleLabel">结果公布：</div>
                <div class="numTxt"><span class="num">1.</span><p>每日公布前一天获奖名单，获奖老师将收到获奖通知短信，老师将根据短信提示领取奖学金；</p></div>
                <div class="numTxt"><span class="num">2.</span><p>所有奖品将7月16日后统一发出。</p></div>
            </div>
            <h2 class="ruleType-tit"><i class="icon01 icon01-d"></i>师生奖学金<i class="icon02 icon02-d"></i></h2>
            <div class="aside">
                <div class="ruleLabel">老师参评条件：</div>
                <div class="numTxt"><span class="num">1.</span><p>布置过“基础必过”复习作业；</p></div>
                <div class="numTxt"><span class="num">2.</span><p>布置过5个期末复习作业（不含基础必过），一次可布置多个作业；</p></div>
                <div class="numTxt"><span class="num">3.</span><p>且为认证老师。</p></div>
            </div>
            <div class="aside">
                <div class="ruleLabel">评选规则:</div>
                <div class="numTxt"><span class="num">1.</span><p>老师奖学金10名，将从以下3类老师中随机挑选；</p></div>
                <p>老师需要满足以下至少1个条件：</p>
                <p>（全国参与老师数量巨大，为保证评奖公平性，评选参考复习数据同时会增加一定的随机性）</p>
                <div class="condition margin-btm">
                    <div class="row">
                        <span class="lType">成绩优异：</span>
                        <p class="rTxt">期末复习，学生平均分高于80。复习效果好，班级进步大。</p>
                    </div>
                    <div class="row">
                        <span class="lType">科学复习：</span>
                        <p class="rTxt">期末复习作业覆盖了所有的单元。布置最科学、覆盖知识全面。</p>
                    </div>
                    <div class="row">
                        <span class="lType">积极复习：</span>
                        <p class="rTxt">班级学生平均完成人数>30，完成率>80%。学生参与人数多，完成率高。</p>
                    </div>
                </div>
                <div class="numTxt"><span class="num">2.</span><p>学生奖学金60名。</p></div>
                <p>每个获奖老师的学生6名：</p>
                <div class="condition">
                    <div class="row">
                        <span class="lType">优异之星：</span>
                        <p class="rTxt">老师班级，复习作业和基础必过全部完成，且普通作业平均分最高的前3名。</p>
                    </div>
                    <div class="row">
                        <span class="lType">勤奋之星：</span>
                        <p class="rTxt">老师班级，复习作业和基础必过全部完成，且基础必过完成时间最早的前3名。</p>
                    </div>
                </div>
            </div>
            <div class="aside">
                <div class="ruleLabel">实物奖品：</div>
                <p>老师：2999元教育基金，共10名（将以京东E卡方式发放，供老师教育使用）。</p>
                <p>学生：米兔儿童电话手表。</p>
            </div>
            <div class="aside">
                <div class="ruleLabel">结果公布：</div>
                <div class="numTxt"><span class="num">1.</span><p>6月28日公布获奖名单，获奖老师将收到获奖通知短信，老师将根据短信提示领取奖学金；</p></div>
                <div class="numTxt"><span class="num">2.</span><p>所有奖品将7月16日后统一发出。</p></div>
            </div>
        </div>
    </div>
    <script>
        var initMode = 'ruleMode';
    </script>
    <#include "../../layout/project.footer.ftl">
</@layout.page>