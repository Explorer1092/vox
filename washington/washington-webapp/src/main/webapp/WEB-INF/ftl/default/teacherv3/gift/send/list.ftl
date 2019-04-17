<#if giftSendOutPage?? && giftSendOutPage.content?has_content>
    <#list giftSendOutPage.content as g>
        <li>
            <p>
                <i class="gift_icon" style="background-image: url(<@app.link href="public/skin/common/images/gift/${g.giftImgUrl!''}"/>); width: 110px; height: 110px;"></i>
            </p>
            <strong class="t_1">赠送给：${g.receiverName!''}</strong>
            <strong class="t_1">${g.date!''}</strong>
        </li>
    </#list>
    <div class="message_page_list" ></div>

    <script type="text/javascript">
        $(function(){
            $("#gift_num_box").html("你一共送出${giftSendOutPage.getTotalElements()}份礼物");
            $(".message_page_list").page({
                total           : ${(giftSendOutPage.getTotalPages())!'0'},
                current         : ${(giftSendOutPage.getNumber()+1)!''},
                jumpCallBack    : function(index){
                    $("#send_gifts_list_box").load("/teacher/gift/send/list.vpage?currentPage="+(index-1));
                }
            });
        });
    </script>
<#else>
    <li style="float:none; width: auto;">
        <div class="w-noData-box">
            您还没有送出任何礼物哦！ 赶紧给您的同学送礼物吧！
        </div>
    </li>
</#if>