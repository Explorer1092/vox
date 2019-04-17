<#if pagination?? && pagination.getTotalElements() gt 0 >
    <style type="text/css">
        .colorBlack {font-weight: bold; cursor: pointer;}
    </style>
    <#list pagination.getContent() as message >
        <dl class="tm-box">
            <dt>
                <span class="w-icon w-icon-8"></span>
            </dt>
            <dd>
                <div class="con">
                    <div class="font <#if message.status == "UNREAD"> colorBlack </#if>" data-message_id="${message.id!}">${message.payload!}</div>
                    <p class="w-ag-right">${(message.createTime)?number_to_datetime}</p>
                </div>
                <div class="set-btn">
                    <a class="w-change-btn w-fl-right delete_content_but" data-message_id="${message.id!}" href="javascript:void (0)" title="删除">删除</a>
                </div>
            </dd>
        </dl>
    </#list>
    <script type="text/javascript">
        $(function () {
            $(".message_page_list").page({
                total: ${pagination.getTotalPages()!0},
                current: ${currentPage!},
                jumpCallBack: createPageList
            });

            /*删除系统消息*/
            $(".delete_content_but").on("click", function () {
                var $this = $(this);
                var deleteMessage = {
                    state: {
                        title: "系统提示",
                        html: "确定删除该条系统消息？",
                        buttons: {"取消": false, "确定": true},
                        position : {width : 400},
                        focus: 1,
                        submit: function (e, v) {
                            e.preventDefault();
                            if (v) {
                                $.post('/student/message/deleteSysMes.vpage',{messageId : $this.data('message_id')},function(data){
                                    if (data.success) {
                                        $17.tongji('消息中心-通知-删除');
                                        $.prompt.close();
                                        $this.closest('dl').remove();
                                    } else {
                                        $17.alert("参数错误,请刷新页面重试.");
                                    }
                                });
                            } else {
                                $.prompt.close();
                            }
                        }
                    }
                };
                $.prompt(deleteMessage);
            });

            //标记已读
            $(".colorBlack").on('click', function(){
                var $this = $(this);
                var megId = $this.data('message_id');
                if(!$this.hasClass('colorBlack')){return false;}
                $.get('/student/message/mark.vpage?messageId='+megId, function(data){
                    if(data.success){
                        $this.removeClass('colorBlack');
//                        $.get("/student/bubbles.vpage",function(data){
//                            if(data.unreadTotalCount == 0){
//                                $("#popinfo").hide();
//                                $(".unreadSystemMessageCount").hide();
//                            }else{
//                                $("#popinfo").show();
//                                $(".unreadSystemMessageCount").text(data.unreadTotalCount).show();
//                            }
//
//                        });
                    }else{
                        $17.alert('系统消息标记已读失败');
                    }

                });

            });
        });
    </script>
<#else>
    <div class="w-noData-box">
        没有系统消息！
    </div>
</#if>