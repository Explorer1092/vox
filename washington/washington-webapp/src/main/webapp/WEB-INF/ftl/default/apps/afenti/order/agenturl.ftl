<#import "module.ftl" as com>
<@com.page step=1 title="请别人帮忙付款 - 一起作业" stepOnOff = "Confirmed">
<@sugar.capsule js=["ZeroClipboard"] />
<#switch productImg>
    <#case "AfentiExam">
        <#assign productNamePage="阿分题">
        <#macro productIntroduction>阿分题 是一款英语应试提分类的寓教于乐产品，它可以让 ${studentName!''} 的英语成绩快速提升，可以帮TA开通吗？${payingAgentUrl!''}</#macro>
        <#break />
    <#case "AfentiBasic">
        <#assign productNamePage="冒险岛">
        <#macro productIntroduction>冒险岛 是一款紧随课本的英语单词趣味练习产品，它可以让 ${studentName!''} 的英语成绩快速提升，可以帮TA开通吗？${payingAgentUrl!''}</#macro>
        <#break />
    <#case "AfentiTalent">
        <#assign productNamePage="单词达人">
        <#macro productIntroduction>单词达人 是一款包含了小学必会1200个英语单词的趣味学习产品，它可以让 ${studentName!''} 的英语成绩快速提升，可以帮TA开通吗？${payingAgentUrl!''}</#macro>
        <#break />
    <#default>
        <#macro productIntroduction>---------</#macro>
</#switch>
<div class="main">
    <!--step2-->
    <div class="payMainBox">
        <div class="curaddress">请别人帮忙付款</div>
        <div class="tabbox">
            <div class="tabLevel productView">
                <!--product-->
                <!--pay_link_way-->
                <div class="pay_link_way">
                    <ul>
                        <li>
                            <dl>
                                <dt>
                                    <span class="icons icons_1"></span>
                                </dt>
                                <dd>
                                    <h4>1.复制链接</h4>
                                    <p>复制付款的链接</p>
                                </dd>
                            </dl>
                        </li>
                        <li class="pay_link_way_top">
                            <span class="icons icons_2"></span>
                        </li>
                        <li>
                            <dl>
                                <dt>
                                    <span class="icons icons_3"></span>
                                </dt>
                                <dd>
                                    <h4>2.QQ发送</h4>
                                    <p>通过QQ发送给亲朋好友</p>
                                </dd>
                            </dl>
                        </li>
                        <li class="pay_link_way_top">
                            <span class="icons icons_2"></span>
                        </li>
                        <li>
                            <dl>
                                <dt>
                                    <span class="iblock ireceiving"></span>
                                </dt>
                                <dd>
                                    <h4>3.代付成功</h4>
                                    <p>对方成功为您付款</p>
                                </dd>
                            </dl>
                        </li>
                    </ul>
                </div>
                <!--//-->
            </div>
            <div class="clear"></div>
        </div>

        <div class="tabbox">
            <div class="tabLevel productView">
            <!--product-->
                <!--pay_link_detail-->
                <div class="pay_link_detail">
                    <dl>
                        <dt>
                            <span class="icons icons_4"></span>
                        </dt>
                        <dd>
                            <h3 class="text_black">复制链接发送给好友或长辈，请TA为你付款</h3>
                            <p class="title">
                                <textarea id="copy_info_url" readonly="readonly"><@productIntroduction/></textarea></p>
                            <p class="get_link">
                                <span id="clip_container"><a href="javascript:void(0);" class="getOrange gPaygetGreen buyNowSubmit" id="clip_button"><strong>复制链接</strong></a></span>
                                <span class='copyInfo clrgray' style="display: none;">复制成功！你可以在QQ上使用Ctrl+V粘贴，发送给亲朋好友</span>
                            </p>
                        </dd>
                    </dl>
                </div>
            <!--//-->
            </div>
            <div class="clear"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"), "copyInfo");

        var count = 0;
        setInterval(function(){
            if($(".copyInfo").attr("is-show") == 1 && count == 0){
                $17.tongji("找人代付-点击复制链接成功-${productNamePage!''}");
                count++;
            }
        }, 500);

        $17.traceLog({
            module: 'agentPay',
            op: 'load' ,
            s0: '${(productImg)!''}'
        });
    });
</script>
</@com.page>
