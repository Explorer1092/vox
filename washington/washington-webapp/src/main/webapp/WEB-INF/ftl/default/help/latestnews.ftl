<#import "serviceV2Module.ftl" as layout/>
<@layout.page
title="一起教育科技公司简介_一起教育科技"
currentMenu='最新动态'
keywords="一起作业企业介绍,一起作业,一起作业网站,一起作业公司简介,一起作业网是干什么的"
description="一起作业隶属（上海合煦信息科技有限公司）于2011年10月正式上线，致力于为全中国的老师、学生和家长提供基于互联网的在线作业和能力提升平台，目前涵盖小学英语和数学两大学科，注册用户超过1400万，是中国最大的中小学在线学习平台。"
pageJs=['newwebsite']
pageJsFile={'newwebsite': 'public/script/project/newwebsite'}
>

<div class="news-wrap minWidth" >

    <div class="section1 md-banner">
        <a href="http://paper.people.com.cn/rmrb/html/2018-09/26/nw.D110000renmrb_20180926_2-13.htm" target="_blank"><img src="<@app.link href='public/skin/default/v5/images/bg-08.jpg'/>" alt=""></a>
    </div>
    <div class="section2" id="newsModal">
        <div class="news-main">
            <ul class="main-box JS-mainbox">
                <!-- ko foreach: newsDataList -->
                <li>
                    <a data-bind="attr: {href: link}" target="_blank" style="display: block;overflow: hidden">
                        <div class="pic-box"><img data-bind="attr: { src: $root.imgCdnHeader() + imgUrl }" /></div>
                        <div class="news-txt" >
                            <h3 data-bind="text: title"></h3>
                            <p data-bind="text: content"></p>
                        </div>
                    </a>
                </li>
                <!-- /ko -->
            </ul>

            <!-- 分页器 -->
            <div class="pagger" id="pagger"></div>
        </div>
    </div>

</div>
<script>
    var imgCdnHeader = "<@app.link href='/'/>" + 'public/skin/default/v5/images/';
</script>
</@layout.page>