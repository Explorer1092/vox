<#import '../layout.ftl' as layout>
<@layout.page className='walkerreport' title="家长APP-沃克学习报告" pageJs="walkerreport" globalJs = []>
<#assign topType = "topTitle">
<#assign topTitle = "自学报告">
<#include "../top.ftl" >
<#include "../constants.ftl">
${buildLoadStaticFileTag("walkerreport", "css")}
    <div class="selfStudyReport-box">
        <div class="top">
            <dl>
                <dt><img src="<@app.link href='public/skin/parentMobile/images/walker_head.png'/>"></dt>
                <dd>
                    <div class="info">家长你好，您的孩子<span>${stuName!"--"}</span>今天在趣味学习《沃克单词冒险》中自学掌握了<span class="num">${wordNum!"0"}</span>个单词</div>
                </dd>
            </dl>
        </div>
        <div class="sr-title">掌握详情</div>
        <div class="main">
            <ul>
            <#if words?exists>
                <#list words?keys as key>
                    <li>
                        <span class="translate">${words[key]}</span><b>${key}</b>
                    </li>
                </#list>
            <#else>
                <li>没有单词</li>
            </#if>
            </ul>
            <div class="sr-title titleMar">家长建议</div>
            <div class="column">
                <p>如果您能够抽出时间询问和夸奖孩子今天的自学成果，可以大大提高孩子的自学积极性。</p>
            </div>
        </div>
        <div class="content">
            <p>《沃克单词冒险》提供<span class="sub">5</span>天免费试学，您的孩子还能继续自学<span class="sub">${surplusStageNum!"0"}</span>天。</p>
        </div>
        <div class="footer">
            <div class="footerEmpty"></div>
            <div class="btn">
                <a href="${buyUrl}" class="open_btn doTrack" data-track="walkerReport|openBtnClick">为孩子开通</a>
            </div>
            <p class="hidden doAutoTrack" data-track="walkerReport|pageLoad"></p>
        </div>
    </div>
</@layout.page>

