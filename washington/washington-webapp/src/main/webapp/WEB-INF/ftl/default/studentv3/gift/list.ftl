<#if gifts?? && gifts?has_content>
    <#list gifts.content as g>
        <li>
            <p class="send-gift-correct-side" style="cursor: pointer;" data-gift_id="${g.id!''}" data-gift_url="${g.imgUrl!''}" data-gift_name="${g.name!}" data-silver="${g.silver!'0'}">
                <span class="send-gift-correct" data-gift_id="${g.id!''}" data-gift_url="${g.imgUrl!''}" data-gift_name="${g.name!}" data-silver="${g.silver!'0'}"></span>
                <i class="gift_icon" style="background-image: url(<@app.link href="public/skin/common/images/gift/${g.imgUrl!''}"/>); width: 110px; height: 110px;"></i>
            </p>
            <strong class="t_1">${g.name!}</strong>
            <strong class="t_2"><#if g.silver?int == 0><#if .now gt '2015-03-03 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss')>做作业，送免费礼物<#else>免费礼物</#if><#else>${g.silver!''}学豆</#if></strong>
        </li>
    </#list>
    <div class="message_page_list" id="giftPageList"><#--分页列表--></div>

    <script type="text/javascript">
        $(function(){
            var giftType;
            var category = "${category!''}";
            switch (category){
                case "FESTIVAL" :
                    giftType =  "FESTIVAL";
                    break;
                case "BLESSING" :
                    giftType =  "BLESSING";
                    break;
                case "BIRTHDAY" :
                    giftType =  "BIRTHDAY";
                    break;
            }

            $("#giftPageList").page({
                total           : ${(gifts.getTotalPages())!'0'},
                current         : ${(gifts.getNumber()+1)!''},
                autoBackToTop   : false,
                jumpCallBack    : function(index){
                    $("#gift_list_box").load("/student/gift/list.vpage?category="+giftType+"&currentPage="+(index-1));
                }
            });

            $(document).ready(function(){
                $("#gift_list_box li p span").removeClass("send-gift-correct");
                $("#gift_list_box li p").removeClass("send-gift-correct-side");
            });

            //选择花束。
            $("#gift_list_box li p").click(function(){
                var $this = $(this);
                var span = $this.children("span");
                span.toggleClass("send-gift-correct");
                $(this).toggleClass("send-gift-correct-side");
                $this.closest("li").siblings().find("p span").removeClass("send-gift-correct");
                $this.closest("li").siblings().find("p").removeClass("send-gift-correct-side");

                //动画效果
                var gift_list_box = $("#gift_list_box");
                var top = gift_list_box.offset().top ;
                var left = gift_list_box.offset().left ;
                var gift = $(this).find("span.send-gift-correct");
                var _l = ($.browser.version == "6.0") ? (gift.offset().left - $this.width()) : (gift.offset().left - 348);
                $("#effect_box").remove();
                var effectBox = '<div id="effect_box" style="border: 1px solid #222222;position: absolute;"><img style="width: 50px;height: 50px;" src="<@app.link href='/'/>/public/skin/common/images/gift/'+gift.data("gift_url")+'"/></div>';
                $('#name_show_box').append(effectBox);
                $("#effect_box").css({top : (gift.offset().top - $(this).height() - 100), left : _l ,width:10, height:10}).show().animate({top:0, left:left, width:50, height:50, opacity:0.2},700,function(){
                    $(this).css({"border-width":0, opacity:1}).find("img").show();
                    $("#effect_box").delay("300").fadeOut();
                });
            });
        });
    </script>
</#if>