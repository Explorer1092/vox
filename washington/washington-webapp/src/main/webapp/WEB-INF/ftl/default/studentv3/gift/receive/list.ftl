<#if giftReceivedPage?? && giftReceivedPage.content?has_content>
    <#list giftReceivedPage.content as g>
        <li>
            <p>
                <i class="gift_icon" style="background-image: url(<@app.link href="public/skin/common/images/gift/${g.giftImgUrl!''}"/>); width: 110px; height: 110px;"></i>
            </p>
            <strong class="t_1">${g.senderName!''} 赠送</strong>
            <strong class="t_1">${g.date!''}</strong>
            <strong class="t_2">
                <#if g.postscript?has_content>
                    <a data-postscript="${g.postscript!''}" href="javascript:void(0);" class="w-blue postscript_box">查看赠言</a>
                <#else>
                    <a href="javascript:void(0);" style="cursor: default;" class="w-gray">暂无赠言</a>
                </#if>
                <a data-delete_id="${g.giftHistoryId!''}" title="删除" href="javascript:void(0);" class="message_del_sender w-blue">
                    删除
                </a>
            </strong>
            <#if (g.isThanks)!false>
                <strong><a href="javascript:void(0);" style="display: inline-block;background-color: #cfcfcf;border: 1px solid #cfcfcf;color:#fff;width:57px;padding:5px 0;font-size:14px;margin:7px 0; cursor: default;">已答谢</a></strong>
            <#else>
                <strong><a href="/student/gift/index.vpage?id=${g.senderId!''}&historyId=${(g.giftHistoryId)!}" style="display: inline-block;background-color: #f5c98e;border: 1px solid #e2b375;color:#fff;width:57px;padding:5px 0;font-size:14px;margin:7px 0;">答谢</a></strong>
            </#if>
        </li>
    </#list>
    <div class="message_page_list" ></div>

    <script type="text/javascript">
        $(function(){
            var pageIndex = ${(giftReceivedPage.getNumber())!''};

            //礼物分页加载
            function reloadList(index){
                $("#receive_gifts_list_box").load("/student/gift/receive/list.vpage?currentPage="+(index));
            }

            $("#gift_num_box").html("你一共收到${giftReceivedPage.getTotalElements()}份礼物");

            $(".message_page_list").page({
                total           : ${(giftReceivedPage.getTotalPages())!'0'},
                current         : ${(giftReceivedPage.getNumber()+1)!''},
                autoBackToTop   : false,
                jumpCallBack    : function(index){
                    reloadList(index-1);
                }
            });

            //删除礼物
            $(".message_del_sender").on('click', function(){
                var $this       = $(this);
                var deleteId    = $this.data("delete_id");
                var giftLength  = $("#receive_gifts_list_box").find("li").length;
                var sendMessage = {
                    state : {
                        title   : "系统提示",
                        html    : "确定删除该礼物吗？",
                        buttons : {"取消": false, "确定" : true},
                        focus   : 1,
                        position : {width : '400'},
                        submit  : function(e,v){
                            if(v){
                                $.post("/student/gift/deletegift.vpage?giftHistoryId=" + deleteId, function(data){
                                    if(data.success){
                                        giftLength--;
                                        if(giftLength == 0){
                                            reloadList((pageIndex - 1) <= 0 ? 0 : pageIndex - 1);
                                        }else{
                                            reloadList(pageIndex);
                                        }
                                        return false;
                                    }else{
                                        $17.alert("礼物删除失败！请稍后再删除。");
                                    }
                                    $.prompt.close();
                                });
                            }
                        }
                    }
                };
                $.prompt(sendMessage);
            });

            //学生查看赠言
            $("[data-postscript]").hover(function(){
                var _this = $(this);
                var offset = _this.offset();
                $("#msg").html(_this.attr("data-postscript"));
                $("#tipMsg").css({ top: offset.top + 30, left: offset.left - 20}).show();
            }, function(){
                $("#msg").html("");
                $("#tipMsg").hide();
            });
        });
    </script>
<#else>
    <li class="text_big">
        <div class="no-gift-info" style="padding: 0 100px;">
            <i class="no-gift-icon" style="float:left;background-image: url(<@app.link href="public/skin/common/images/gift/no-gift-icon-1.png"/>); width: 99px; height: 104px;display: inline-block;"></i>
            <p style="color:#7aacd0;padding: 35px 0 25px 145px;font-size: 18px;line-height: 30px;">啊噢，近30天还没有收到任何礼物噢～<br/>主动送礼物才会收到更多的回赠！</p>
            <div class="btn" style="text-align: center;">
                <a href="../index.vpage" class="send-gift-btn" style="color:#fff;font-size:14px;text-align:center;border:1px solid #db9640;border-radius:3px;display: inline-block;width:130px;padding:10px 0;background-color: #fdb962;"><i class="send-gift-icon" style="background-image: url(<@app.link href="public/skin/common/images/gift/send-gift-btn-icon.png"/>);display: inline-block;width:16px;height:16px;margin:0 10px 0 0;vertical-align: middle;"></i>去送礼物</a>
            </div>
        </div>
    </li>
    <script type="text/javascript">
        $(function(){
            $("#gift_num_box").html("你一共收到0份礼物");
        });
    </script>
</#if>