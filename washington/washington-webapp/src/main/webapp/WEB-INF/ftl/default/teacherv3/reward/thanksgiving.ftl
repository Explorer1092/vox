<#import "../../layout/project.module.ftl" as temp />
<@temp.page title="团购" header="show">
    <@app.css href="public/skin/project/thanksgiving/sendgold/sendgold.css" />
<div class="main">
    <#assign isSchoolAmbassador = (currentTeacherDetail.schoolAmbassador)!false />
    <#if isSchoolAmbassador>
        <div class="head-02">
            <div class="inner"></div>
        </div>
        <div class="content-infomercial-box">
            <div class="inner">
                <dl>
                    <dd>
                        <p>
                            现金红包就是通过红包的形式把现金发放到您的微信账号上，可以提现到银行卡，或者使用微信支付直接买东西，钱是您的，想怎么花，就怎么花，任性买买买！
                        </p>
                        <p>
                            此次现金大放送活动为校园大使专享，现金红包通过校园大使微信公众号发放，请老师们通过扫描页面右侧的二维码关注，以免影响您获得现金红包。未关注的老师不能获得现金红包。（此次活动的最终解释权归一起作业网所有）
                        </p>
                    </dd>
                    <dt>
                        <img src="/public/skin/project/thanksgiving/sendgold/schoolAmbassador_qrc.jpg" alt=""/>
                    </dt>
                </dl>
            </div>
        </div>
    <#else>
        <div class="head">
            <div class="inner"></div>
        </div>
    </#if>

    <div class="content-sendGold-box">
        <div class="contain-box">
            <div class="inner">
                <div class="tip">
                    <p class="right">共获得
                        <#if isSchoolAmbassador>
                            <strong>${((total)!0)}</strong> 元
                        <#else>
                            <strong>${((total)!0)*10}</strong> 园丁豆
                        </#if>
                    </p>
                    已完成 <strong>${total!'---'}</strong>人 / 待完成 <strong>${waitCount!}</strong>人
                </div>
                <#--//start-->
                <#if data?? && data?size gt 0>
                    <#list data?keys as dt>
                        <div class="slide-tip-box">
                            <h3 class="red-tip PNG_24">${dt?string}</h3>
                            <#if data[dt]?size gt 0>
                                <div class="list">
                                    <#list (data[dt]) as cl>
                                        <dl <#if cl.activeFlag!false>class="active"</#if>>
                                            <dt>
                                                <i>
                                                    <img src="<@app.avatar href='${cl.imgUrl}'/>" width="72" height="72"/>
                                                </i>
                                                <span class="finish"></span>
                                            </dt>
                                            <dd>
                                                <p>
                                                ${cl.studentName}<br>
                                                ${cl.studentId}
                                                </p>
                                            </dd>
                                        </dl>
                                    </#list>
                                    <div class="clear"></div>
                                </div>
                            <#else>
                                <div style="text-align: center; padding: 50px;">暂无数据</div>
                            </#if>
                        </div>
                    </#list>
                <#else>
                    <div class="studentList">
                        <div style="text-align: center; padding: 50px;">暂无数据</div>
                        <div class="clearNull"></div>
                    </div>
                </#if>
                <#--end//-->
            </div>
        </div>
        <div class="send-tree-bg">
            <div class="inner"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#btnGetPopup").hover(function(){
            $(this).find(".thanksgiving").show();
        }, function(){
            $(this).find(".thanksgiving").hide();
        });
    });
</script>
</@temp.page>