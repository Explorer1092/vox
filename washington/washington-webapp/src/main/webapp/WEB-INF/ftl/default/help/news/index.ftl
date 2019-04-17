<#import "../serviceV2Module.ftl" as layout/>
<@layout.page
title="一起作业新闻_一起作业大事记_一起作业"
keywords="一起作业新闻,一起作业大事记,一起作业网,新闻中心"
description="一起作业新闻汇聚了一起作业新闻报道、一起作业成长大事记、一起作业专题报道，让您更加了解一起作业。"
currentMenu="新闻中心"
pageJs=['jquery', 'init']
pageJsFile={'init': 'public/script/project/help'}
>
<style>
    .JS-newIndex-banner{ position: relative;}
    .JS-newIndex-banner .flex-control-nav{ bottom: 65px;}
</style>
<div class="news-section"  style="background: none;">
    <div class="w-wrapper">
        <div class="w-title">
            <div class="name">新闻</div>
            <div class="english">NEWS</div>
        </div>
        <div class="clearfix">
            <div class="news-pic">
                <div class="JS-newIndex-banner" style="width: 400px;">
                    <ul class="slides">
                        <li>
                            <a href="newscontent_77.vpage" target="_blank">
                                <img src="<@app.link href="public/skin/default/images/serviceV2/jobs/news/news-wuzhen-2017.jpg"/>" style="width: 100%; height: 260px;"/>
                                <span class="text" style="margin-top: 15px; display: inline-block;">再登乌镇世界互联网大会，刘畅谈网络文化共建共享</span>
                                <div class="time">2017-12-04</div>
                            </a>
                        </li>
                        <li>
                            <a href="newscontent_68.vpage" target="_blank">
                                <img src="<@app.link href="public/skin/default/images/serviceV2/jobs/news/futures-img.jpg?v=1.0.0"/>" style="width: 100%; height: 260px;"/>
                                <span class="text" style="margin-top: 15px; display: inline-block;">一起作业少年科学团对话全球顶级科学家</span>
                                <div class="time">2017-10-29</div>
                            </a>
                        </li>
                        <li>
                            <a href="newscontent_45.vpage" target="_blank">
                                <img src="<@app.link href="public/skin/default/images/serviceV2/jobs/news/new-170420.jpg?v=1.0.0"/>" style="width: 100%; height: 260px;"/>
                                <span class="text" style="margin-top: 15px; display: inline-block;">南开中学与一起作业联合开展十三五重点课题研究</span>
                                <div class="time">2017-04-20</div>
                            </a>
                        </li>
                        <li>
                            <a href="newscontent_40.vpage" target="_blank">
                                <img src="<@app.link href="public/skin/default/images/serviceV2/jobs/news/news-170204.jpg"/>" style="width: 100%; height: 260px;"/>
                                <span class="text" style="margin-top: 15px; display: inline-block;">央视走进一起作业，聚焦智能教育的中国探索</span>
                                <div class="time">2017-02-04</div>
                            </a>
                        </li>
                        <li>
                            <a href="newscontent_38.vpage" target="_blank">
                                <img src="<@app.link href="public/skin/default/images/serviceV2/jobs/news/news-170115.jpg"/>" style="width: 100%; height: 260px;"/>
                                <span class="text" style="margin-top: 15px; display: inline-block;">一起作业董事长王强捐赠未来科学大奖</span>
                                <div class="time">2017-01-15</div>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="news-list" id="newsContent">
                <div class="lists"></div>
                <div class="page">
                    <div class="news-nav"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="news_ul_temp">
    <div class="lists JS-lists">
        <% for(var i=0;i<pageNo;i++) {%>
            <ul class="news0<%=(i+1)%>"  style="display: <% if(i==0){%> block<% } else {%> none <% }%>;">
                <% for(var j=0;j < innerNo;j++){%>
                <#--如果循环出来的数据大于数据总量-->
                    <% if((j + innerNo*i) >= maxNo){%> <% } else {%>
                        <li>
                            <span class="time JS-time"> <%= data[j + innerNo*i].time %></span>
                            <a href="<%= data[j + innerNo*i].url %>" target="_blank"><%= data[j + innerNo*i].title %></a>
                        </li>
                    <% }%>
                <% }%>
            </ul>
        <% } %>
    </div>
    <div class="page">
        <div class="news-nav JS-news-nav">
            <% for(var i=1;i<=pageNo;i++) {%>
                <span class="navList JS-navList <% if(i==1){%>active <% }%>"><%=i%></span>
            <% } %>
        </div>
    </div>
</script>

<div class="news-section">
    <div class="w-wrapper">
        <div class="w-title">
            <div class="name">大事记</div>
            <div class="english">MEMORABILIA</div>
        </div>
        <div class="news-event">
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2017</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">砥砺奋进</div>
                    <div class="text">注册用户突破5000万，其中学生用户突破3000万；成为《经济学人》中国智能教育唯一代表案例；作为在线教育代表企业，连续获《人民日报》肯定；再登乌镇世界互联网大会</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2016</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">百尺竿头</div>
                    <div class="text">注册学生用户突破2000万；启用全新品牌标识；完成K12领域全学科、全学段布局；作为全球教育行业的唯一代表，登上乌镇世界互联网大会演讲台</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2015</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">纵情驰骋</div>
                    <div class="text">学生移动端正式上线；完成D轮1亿美元融资，成为中国K12在线教育领域单笔数额最大的一次融资；参加全国第七次少代会，登上《新闻联播》</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2014</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">锐意进取</div>
                    <div class="text">注册学生用户突破1000万；教师、家长微信端上线；作为中国在线教育的唯一代表，登上哈佛大学讲堂；完成C轮2000万美元融资</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2013</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">破壁前行</div>
                    <div class="text">学生累计使用突破1亿次；单日学生作业人数突破10万；小学数学产品上线；完成B轮1000万美元融资；作为中国在线教育代表，被BBC报道</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2012</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">上下求索</div>
                    <div class="text">注册学生用户突破100万；完成A轮融资500万美元；成为国家级“十二五”课题《新课标形势下小学英语网络作业形式探究》研究平台</div>
                </div>
            </div>
            <div class="box clearfix">
                <div class="time">
                    <div class="year">2011</div>
                </div>
                <div style="float:right;width:855px;">
                    <div class="head">从零到一</div>
                    <div class="text">一起作业正式上线，推出小学英语在线作业产品，成为中国第一个K12在线作业平台；第一个学生用户来自北京市中关村第二小学</div>
                </div>
            </div>
        </div>
    </div>
</div>

</@layout.page>
