<#import "module.ftl" as com>
<@com.page step=1 title="请确认代付信息 - 一起作业" paymentType="agent" stepOnOff = "${orderStatus}">
<#switch productImg>
    <#case "AfentiExam">
        <#assign productNamePage = "阿分题"/>
        <#macro productIntroduction>
            <p> 阿分题 是一款英语应试提分类的寓教于乐产品。</p>
            <p>可以有效地提升孩子的英语成绩以及孩子英语听，说，读，写的综合能力。</p>
            <p>同时让孩子在一个开心快乐的过程中得到成长。</p>
            <p>爱TA就让TA在快乐中学习成长，帮TA开通 阿分题。</p>
        </#macro>
        <#break />
    <#case "AfentiBasic">
        <#assign productNamePage = "冒险岛"/>
        <#macro productIntroduction>
            <p>冒险岛 是一款紧随课本的英语单词趣味练习产品。</p>
            <p>可以有效地提升孩子的英语成绩以及孩子英语听，说，读，写的综合能力。</p>
            <p>同时让孩子在一个开心快乐的过程中得到成长。</p>
            <p>爱TA就让TA在快乐中学习成长，帮TA开通 冒险岛。</p>
        </#macro>
        <#break />
    <#case "AfentiTalent">
        <#assign productNamePage = "单词达人"/>
        <#macro productIntroduction>
            <p>单词达人 是一款包含了小学必会1200个英语单词的趣味学习产品。</p>
            <p>可以有效地提升孩子的英语成绩以及孩子英语听，说，读，写的综合能力。</p>
            <p>同时让孩子在一个开心快乐的过程中得到成长。</p>
            <p>爱TA就让TA在快乐中学习成长，帮TA开通 单词达人</p>
        </#macro>
        <#break />
    <#default>
        <#assign productNamePage = ""/>
        <#macro productIntroduction>错误页面，如有问题请联系客服。</#macro>
</#switch>
<div class="main">
    <!--step2-->
    <div class="payMainBox">
        <div class="curaddress">请确认代付信息</div>
        <div class="tabbox">
            <div class="tabLevel productView">
                <!--product-->
                <#if paidTalent?? && paidTalent?has_content>
                    <#if orderStatus == "Confirmed">
                        <div class="tabLevel">
                            <div class="successBox">
                                <s class="iblock ireceiving"></s>
                                <div class="content" style="margin-top: -45px;">
                                    <p class="txt">${productNamePage!''} 订单已支付成功</p>
                                </div>
                            </div>
                        </div>
                    <#else>
                        <div class="tabLevel">
                            <div class="successBox loseBox">
                                <s class="iblock iexclamation"></s>
                                <div class="content" style="margin-top: -45px;">
                                    <p class="txt">${productNamePage!''} 代付订单已失效，已经被抢先付款啦！</p>
                                </div>
                            </div>
                        </div>
                    </#if>
                <#else>
                <#-- New, Canceled, Confirmed,-->
                    <#if orderStatus == "Confirmed">
                        <div class="tabLevel">
                            <div class="successBox">
                                <s class="iblock ireceiving"></s>
                                <div class="content">
                                    <p class="txt">${productNamePage} ${productDate!'--'} 天订单已支付成功</p>
                                </div>
                            </div>
                        </div>
                    <#else>
                        <!--pay_link_info-->
                        <div class="pay_link_info">
                            <dl>
                                <dt>
                                <p class="img">
                                    <img src="<@app.avatar href='${studentavatar}'/>" width="68" height="68">
                                </p>
                                </dt>
                                <dd class="pay_link_info_box">
                                    <h2>${studentName!'xxx'}<strong>（一起作业号：${studentId!'***'}）</strong>发起了1笔代付，请您帮忙付款 <span class="clrorange">${productPrice!'--'}</span> 元，开通${productName!'---'} ${productDate!'--'} 天的使用权。</h2>
                                    <div class="down">
                                        <dl>
                                            <dt>
                                            <p class="img">
                                                <img src="<@app.link href="public/skin/project/afenti/images/${productImg!'0'}.jpg" />" width="128" height="108">
                                            </p>
                                            </dt>
                                            <dd>
                                                <@productIntroduction/>
                                            </dd>
                                        </dl>
                                        <div class="arrow"></div>
                                    </div>
                                </dd>
                                <dd>
                                    <form action="?" method="post" id="frmPayment">
                                        <input type="hidden" name="orderId" value="${orderId!}">
                                        <#--<p class="font-info">为了更好的为您服务，麻烦留下您的手机号<input type="text" name="mobile" class="int_vox"><span style="color: #CCCCCC;">(非必填)</span></p>-->
                                        <input type="hidden" name="mobile" value="${mobile!}">
                                        <input type="hidden" name="userName" value="${studentName!}">
                                    </form>
                                </dd>
                                <dd style="text-align: center; margin-top: 30px;"><a id="confirmationPayment" class="getOrange" href="javascript:void(0);">确认付款</a></dd>
                            </dl>
                        </div>
                        <script type="text/javascript">
                            $(function(){
                                $("#confirmationPayment").on("click", function(){
                                    $("#frmPayment").submit();
                                    $17.tongji("找人代付-点击确认付款按钮-${productNamePage!''}");
                                    return false;
                                });

                                $17.tongji("找人代付-打开代付链接-${productNamePage!''}");

                                $17.traceLog({
                                    module: 'agentPayDetail',
                                    op: 'load' ,
                                    s0: '${(productName)!''}',
                                    s1: '${(orderStatus)!''}'
                                });
                            });
                        </script>
                    </#if>
                </#if>
                <!--//-->
            </div>
            <div class="clear"></div>
        </div>
    </div>
</div>
</@com.page>
