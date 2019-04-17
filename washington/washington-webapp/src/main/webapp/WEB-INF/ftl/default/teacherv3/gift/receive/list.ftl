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

                <a data-gift_history_id="${g.giftHistoryId!''}" data-receiver_id="${g.senderId!''}" data-lastes_reply="${(g.latestReply)!''}" class="gift_reply_but w-blue" href="javascript:void (0);">
                    <#if (g.latestReply)?has_content>已回复<#else>回复</#if>
                </a>
                <a data-delete_id="${g.giftHistoryId!''}" title="删除" href="javascript:void(0);" class="message_del_sender w-blue">
                    删除
                </a>
            </strong>
        </li>
    </#list>
    <div class="message_page_list" ></div>

    <script type="text/javascript">
        //礼物分页加载
        function reloadList(index){
            $("#receive_gifts_list_box").load("/teacher/gift/receive/list.vpage?currentPage="+(index));
        }

        $(function(){
            var pageIndex = ${(giftReceivedPage.getNumber())!''};

            $(".message_page_list").page({
                total           : ${(giftReceivedPage.getTotalPages())!'0'},
                current         : ${(giftReceivedPage.getNumber()+1)!''},
                jumpCallBack    : function(index){
                    reloadList(index-1);
                }
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

            //回复
            $(".gift_reply_but").on('click', function(){
                var $this = $(this);
                var replyContent = $this.data('lastes_reply');
                if(replyContent.length > 0){
                    replyContent = "已回复：" + replyContent;
                }
                var states = {
                    state: {
                        title: "答谢回复",
                        html: "<div style='margin: 10px 0;'>"+replyContent+"</div><textarea id='replyContent' name='content' placeholder='填写你对TA想说的赠言（1～50字哦）' maxlength='50' class='int_vox' style='height: 80px; width:456px;'></textarea><div id='word_limit_box' class='text_small' style='float:right;'>还可以输入50个字</div>",
                        buttons : {"发送" : true},
                        submit: function (e, v, m, f) {
                            if (v) {
                                if (f['content'].length > 50) {
                                    $.prompt.goToState('textToLong');
                                    return false;
                                }
                                if(f['content'].length == 0){
                                    $.prompt.goToState('textNull');
                                    return false;
                                }
                                $.post("/teacher/gift/sendreply.vpage", {
                                    receiverId : $this.data('receiver_id'),
                                    giftHistoryId : $this.data('gift_history_id'),
                                    message : f['content']
                                }, function (data) {
                                    if (data.success) {
                                        //更新
                                        $this.data('lastes_reply',f['content']);
                                        $this.text('已回复');
                                        $17.tongji("老师-成功回复赠言");

                                        $17.alert("回复成功");
                                    } else {
                                        $17.alert(data.info);
                                    }
                                });
                            }
                        }
                    },
                    textToLong: {
                        title: "答谢回复",
                        html: "答复内容太长，请限制在50字内。",
                        buttons: {"知道了": true},
                        submit: function (e) {
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    },
                    textNull: {
                        title: "答谢回复",
                        html: "答复内容不能为空。",
                        buttons: {"知道了": true},
                        submit: function (e) {
                            e.preventDefault();
                            $.prompt.goToState('state');
                        }
                    }
                };
                $.prompt(states,{
                    loaded : function(){
                        $("#replyContent").focus();
                    }
                });

                //还可以输入N个字
                $("#replyContent").on("keyup", function () {
                    $("#word_limit_box").html($17.wordLengthLimit($(this).val().length,50));
                });
            });

            //删除
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
                                $.post("/teacher/gift/deletegift.vpage?giftHistoryId=" + deleteId, function(data){
                                    if(data.success){
                                        giftLength--;
                                        if(giftLength == 0){
                                            reloadList((pageIndex - 1) <= 0 ? 0 : pageIndex - 1);
                                        }else{
                                            reloadList(pageIndex);
                                        }
                                        $17.tongji('老师-成功删除礼物');
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
        });
    </script>
<#else>
    <li style="float:none; width: auto;">
        <div class="w-noData-box">
            您还没有收到任何礼物哦！
        </div>
    </li>
</#if>