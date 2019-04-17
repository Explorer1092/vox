<#import './layout.ftl' as layout>

<#include "testPay.ftl">

<@layout.page className='HomeworkReportGiveBean' title="贡献学豆" pageJs="second" extraJs=extraJs![] specialCss="skin2" specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>贡献学豆</title>
'>
    <#escape x as x?html>
        <#assign isUseNewTitle = true><#--使用新的UI2 title-->
    <script><#--改版的样式，不适用adapt-->
    window.notUseAdapt=true;
    </script>
        <#assign topType = "topTitle">
        <#assign topTitle = "贡献学豆">
        <#include "./top.ftl" >

        <#assign MathTid = 0  EnglishTid = 0  ChineseTid = 0>
        <#list (teachers![]) as teacher>
            <#switch teacher.subject>
                <#case "MATH">
                    <#assign MathTid = teacher.teacherId>
                    <#break >
                <#case "ENGLISH">
                    <#assign EnglishTid = teacher.teacherId>
                    <#break >
                <#case "CHINESE">
                    <#assign ChineseTid = teacher.teacherId>
                    <#break >
                <#default>
                    <#break>
            </#switch>
        </#list>

        <#if  (MathTid +  EnglishTid + ChineseTid) ==0>
        <div class="parentApp-emptyProm parentApp-emptyProm-1">
            <div class="promTxt">没有可赠送学豆的老师</div>
        </div>
        <#else>
            <#assign subjects = [
                {
                    "name" : "ENGLISH",
                    "display" : "英语",
                    "tid" : EnglishTid
                },
                {
                    "name" : "MATH",
                    "display" : "数学",
                    "tid" : MathTid
                },
                {
                    "name" : "CHINESE",
                    "display" : "语文",
                    "tid" : ChineseTid
                }
            ]>
            <div class="purchase-box">
                <div class="pur-list">
                    <div class="pur-title">贡献班级学豆后，仅用于老师奖励本班学生</div>
                    <dl class="pur-content">
                        <dt>学科</dt>
                        <dd>
                            <div class="pur-cycle clearfix doTabBlock">
                                <#list subjects as subject>
                                    <#if subject.tid != 0>
                                        <a href="javascript:;" class="box doSubject doTab <#if subject_index == 0>active</#if>" data-tid="${subject.tid}" data-tab_local="nullTab"><div>${subject.display}</div></a>
                                    </#if>
                                </#list>
                            </div>
                        </dd>
                    </dl>
                    <dl class="pur-content">
                        <dt>学豆数</dt>
                        <dd>
                            <div class="pur-cycle clearfix doTabBlock">
                                <#assign beanInfos = [
                                    {
                                        "tag" : "1",
                                        "display" : "20",
                                        "price"  : 1
                                    },
                                    {
                                        "tag" : "2",
                                        "display" : "50",
                                        "price" : 2
                                    },
                                    {
                                        "tag" : "5",
                                        "display" : "120",
                                        "price" : 5
                                    },
                                    {
                                        "tag" : "10",
                                        "display" : "260",
                                        "price" : 10
                                    }
                                ]>
                                <#list beanInfos as beanInfo>
                                    <a href="javascript:;" class="box doBean doTab <#if beanInfo_index == 0>active</#if>" data-price="${beanInfo.price}" data-price_tag="${beanInfo.tag}" data-bean_count="${beanInfo.display}" data-tab_local="nullTab"><div>${beanInfo.display}</div></a>
                                </#list>
                            </div>
                        </dd>
                    </dl>
                </div>
                <div class="pur-prom gray">购买学豆等同于您已确认<a href="/parentMobile/ucenter/shopagreement.vpage" >《一起作业网声明》</a>，自愿送班级学豆，用于学习</div>
                <div class="purfooter">
                    <div class="inner clearfix">
                        <a href="javascript:;" data-is_activity="${(isActivity!false)?string}" class="btn red doCreateGiveBeanOrder">立即购买</a>
                        <div class="money">
                            <span>金额：</span>
                            <span class="red doPrice"><em>￥</em>${beanInfos[0].price}</span>
                        </div>
                    </div>
                </div>
            </div>
            <script type="text/html" id="payOrderTemp">
                <div class="parentApp-orderDetail">
                    <div class="orderBox">
                        <ul class="orderMain" style="background-color: #FFF;">
                            <li>订单编号：<span><%= orderId %></span></li>
                            <li>学豆：<em><%= beanCount %></em>个</li>
                            <li>价格：<em><%= price %></em>元</li>
                        </ul>
                    </div>
                </div>
                <div class="w-footer">
                    <a href="javascript:;" data-order_id="<%= orderId %>" data-order_price="<%= price %>" data-order_type = "integral" class="${doPayClassName!""} btn"><span style="color: #F9F9F9;">立即支付</span></a>
                </div>
            </script>
        </#if>
    </#escape>
</@layout.page>
