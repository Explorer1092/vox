/**
 * 添加陪访
 * */
define(["dispatchEvent"], function (dispatchEvent) {
    $(document).ready(function(){
        reloadCallBack();
        //注册事件
        var eventOption = {
            base:[
                {
                    selector:"div.js-visTarget>div",
                    eventType:"click",
                    callBack:function(){
                        $(this).addClass("the").siblings("div").removeClass("the");
                    }
                },
                {
                    selector:".js-submitVisBtn",
                    eventType:"click",
                    callBack:function(){
                        var targetNode = $(".js-visTarget").find(".the");
                        if(targetNode.length == 0){
                            AT.alert("请选择陪访目的");
                            return false;
                        }
                        if($(".preparationScore .active").length == 0 || $(".productProficiencyScore .active").length == 0 || $(".resultMeetExpectedResultScore .active").length == 0){
                            AT.alert("请填写基础评价");
                            return false;
                        }
                        var visitAdvice = $("#visitAdvice").val();
                        $('.js-success').removeClass('js-submitVisBtn');
                        if(visitAdvice != ""){
                            var target = $(".js-visTarget").find(".the").data("type");
                            visitData.schoolRecordId = schoolRecordId;
                            visitData.workTitle = target;
                            visitData.partnerSuggest = visitAdvice;
                            //进校充分程度
                            visitData.preparationScore = $(".preparationScore .active").length;
                            //产品熟练度评分
                            visitData.productProficiencyScore = $(".productProficiencyScore .active").length;
                                //结果符合预期度评分
                            visitData.resultMeetExpectedResultScore = $(".resultMeetExpectedResultScore .active").length;
                            $.post("saveAccompanyVisitRecord.vpage",visitData,function(res){
                                if(res.success){
                                    AT.alert("添加陪访成功");
                                    setTimeout("location.href = '/view/mobile/crm/visit/visit_detail.vpage'",2000);
                                }else{
                                    AT.alert(res.info);
                                    $('.js-success').addClass('js-submitVisBtn');
                                }
                            });

                        }else{
                            $('.js-success').addClass('js-submitVisBtn');
                            AT.alert('请填写陪访建议');
                        }
                    }
                }
            ]
        };

        new dispatchEvent(eventOption);
        var setTopBar = {
            show:true,
            rightTextColor:"ff7d5a",
            rightText:"提交",
            needCallBack:true
        };
        var topBarCallBack = function () {
            $(".js-submitVisBtn").click();
        };
        setTopBarFn(setTopBar,topBarCallBack);

    });
});