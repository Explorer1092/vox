<#import "../layout.ftl" as homework>
<@homework.page title="学习报告" pageJs="reportIndex">
    <@sugar.capsule css=['jbox'] />
    <@sugar.capsule css=['report'] />
    <div class="study-report">
        <#include "../userpopup.ftl">
        <div class="title_box">
            <ul id="ul_subject">

                <li class="clear"></li>
            </ul>
        </div>
        <div class="clear"></div>
        <#--<div class="list list-1">
            <dl>
                <dt><img src="/public/images/parent/report/top-tab01.png"></dt>
                <dd>
                    <p class="title">单元报告</p>
                </dd>
            </dl>
        </div>
        <div class="list" style="border-bottom: 1px solid #d3d8df;">
            <dl>
                <dt><img src="/public/images/parent/report/top-tab03.png"></dt>
                <dd>
                    <p class="title title-1">错题本</p>
                    <p class="prompt">您有最新报告未查看</p>
                </dd>
            </dl>
        </div>-->
        <div class="list" style="border-bottom: 1px solid #d3d8df;">
            <a href="/parent/homework/common.vpage?page=errorlist">
                <dl>
                    <dt><img src="/public/images/parent/report/top-tab03.png"></dt>
                    <dd>
                        <p class="title title-1">做错考点</p>
                    <#--<p class="prompt">您有最新报告未查看</p>-->
                    </dd>
                </dl>
            </a>
        </div>
        <div class="list" style="border-bottom: 1px solid #d3d8df;">
            <a href="/parent/homework/common.vpage?page=weeklyreport">
                <dl>
                    <dt><img src="/public/images/parent/report/top-tab02.png"></dt>
                    <dd>
                        <p class="title title-1">每周报告</p>
                    <#--<p class="prompt">您有最新报告未查看</p>-->
                    </dd>
                </dl>
            </a>
        </div>
    </div>
    <div style=" height: 125px;"></div>
    <#include "../menu.ftl">
</@homework.page>