<#import "../layout.ftl" as homework>
<@homework.page title="学习报告" pageJs="menu">
    <@sugar.capsule css=['report'] />
    <@sugar.capsule css=['jbox'] />
    <div class="study-report">

        <div class="title_box">
            <ul id="ul_subject">

                <li class="clear"></li>
            </ul>
        </div>
        <div class="clear"></div>
        <div class="list" style="border-bottom: 1px solid #d3d8df;">
            <a href="/parent/homework/report/index.vpage">
                <dl>
                    <dt><img src="/public/images/parent/report/top-tab01.png"></dt>
                    <dd>
                        <p class="title title-1">单元报告</p>
                    </dd>
                </dl>
            </a>
        </div>
        <div class="list" style="border-bottom: 1px solid #d3d8df;">
            <a href="/parent/homework/common.vpage?page=wrongquestionlist">
                <dl>
                    <dt><img src="/public/images/parent/report/top-tab03.png"></dt>
                    <dd>
                        <p class="title title-1">错题本</p>
                        <#if wronglist_report>
                            <p class="prompt">您有最新报告未查看</p>
                        </#if>
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
                        <#if weekly_report>
                            <p class="prompt">您有最新报告未查看</p>
                        </#if>
                    </dd>
                </dl>
            </a>
        </div>
    </div>

    <#include "../menu.ftl">
</@homework.page>