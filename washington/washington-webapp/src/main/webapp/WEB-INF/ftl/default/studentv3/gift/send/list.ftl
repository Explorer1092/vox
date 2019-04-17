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
                    $("#send_gifts_list_box").load("/student/gift/send/list.vpage?currentPage="+(index-1));
                }
            });
        });
    </script>
<#else>
    <li class="text_big">
        <div class="no-gift-info" style="padding: 0 100px;">
            <i class="no-gift-icon" style="float:left;background-image: url(<@app.link href="public/skin/common/images/gift/no-gift-icon-1.png"/>); width: 99px; height: 104px;display: inline-block;"></i>
            <p style="color:#7aacd0;padding: 35px 0 25px 145px;font-size: 18px;line-height: 30px;">你好懒，近30天还木有送出一个礼物～<br/>别让朋友等到花都谢了！</p>
            <div class="btn" style="text-align: center;">
                <a href="../index.vpage" class="send-gift-btn" style="color:#fff;font-size:14px;text-align:center;border:1px solid #db9640;border-radius:3px;display: inline-block;width:130px;padding:10px 0;background-color: #fdb962;">
                    <i class="send-gift-icon" style="background-image: url(<@app.link href="public/skin/common/images/gift/send-gift-btn-icon.png"/>);display: inline-block;width:16px;height:16px;margin:0 10px 0 0;vertical-align: middle;"></i>去送礼物</a>
            </div>
        </div>
    </li>
</#if>