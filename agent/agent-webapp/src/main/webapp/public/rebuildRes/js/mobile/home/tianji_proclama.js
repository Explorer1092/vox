/**
 * 天玑榜
 * */
define(['dispatchEvent'], function (dispatchEvent) {
    $(document).ready(function(){
        var AT = new agentTool();
        //获取当前大区数据
        var getLocalDate = function(type){
            $.get("current_region_ranking_list.vpage?type="+type,function(res){
                if(res.success){
                    renderInfo(res);
                }else{
                    AT.alert(res.info);
                }
            });
        };

        var renderInfo =function(res){
            renderTemplate("tableTemp",{"rankingDataList":res.rankingDataList},"#rankingTable");

            if(res.myRankingData){
                var rankingData = res.myRankingData;
                rankingData["myData"] = true;
                renderTemplate("localContentTemp",rankingData,"#localContent");
            }else{
                renderTemplate("localContentTemp",{myDate:false},"#localContent");
            }

            if($(".js-tab>span.active").length != 0){
                if($(".js-tab>span.active").attr("data-type") == 1){
                    if(showFlag){
                        $(".js-onlyLocal").hide();
                    }
                }else{
                    $(".js-onlyLocal").show();
                }
            }

            if(res.first){
                $($("#topThree").find("span.rl-name")[1]).html(res.first.userName);
                if(res.second){
                    $($("#topThree").find("span.rl-name")[0]).html(res.second.userName);
                }else{
                    $($("#topThree").find("span.rl-name")[0]).html("");
                }
                if(res.third){
                    $($("#topThree").find("span.rl-name")[2]).html(res.third.userName);
                }else{
                    $($("#topThree").find("span.rl-name")[2]).html("");
                }
            }else{
                $("#topThree").find("span.rl-name").html("");
            }
        };

        var getRankList = function(type,order){
            var url = "user_ranking_list.vpage?type="+type;
            if(order){
                url = "user_ranking_list.vpage?type="+type+"&order="+order;
            }
            //-1 倒序
            $.post(url,function(res){
                if(res.success){
                    renderInfo(res);
                }else{
                    AT.alert(res.info);
                }
            })
        };

        var defaultType = 3;
        if( $(".js-tab").length != 0){
            defaultType = $(".js-tab>span.active").data("type");
        }
        getRankList(defaultType);

        //注册事件
        var eventOption = {
            base:[
                {
                    selector:".js-onlyLocal",
                    eventType:"click",
                    callBack:function(){
                        var type = 3;
                        if($(".js-tab>span.active").length != 0){
                            type = $(".js-tab>span.active").attr("data-type");
                        }

                        if($("#localContent").hasClass("all")){
                            $("#localContent").removeClass("all");
                            getRankList(type);
                        }else{
                            $("#localContent").addClass("all");
                            getLocalDate(type);
                        }
                    }
                },
                {
                    selector:".js-tab>span",
                    eventType:"click",
                    callBack:function(){
                        $(this).addClass("active").siblings("span").removeClass("active");
                        var type = $(this).data("type");
                        if(type == "3"){
                            $("#schoolSearch").val("");
                            $("#searchItem").show();

                        }else{
                            $("#searchItem").hide();
                        }

                        getRankList(type);
                    }
                },
                {
                    selector:".js-search",
                    eventType:"click",
                    callBack:function(){
                        var schoolInput = $("#schoolSearch").val();
                        if(schoolInput != ""){
                            $.get("search_ranking_list.vpage?type=3&name="+schoolInput,function(res){
                                if(res.success){
                                    renderTemplate("tableTemp",{"rankingDataList":res.rankingDataList},"#rankingTable");
                                }else{
                                    AT.alert(res.info);
                                }
                            });
                        }else{
                            AT.alert("请输入姓名");
                        }
                    }
                },
                {
                    selector:".js-reOrderAgent",
                    eventType:"click",
                    callBack:function(){
                        var type = $(".js-tab>span.active").data("type");
                        var order = $("#rankingTable").attr("order");
                        if(order == -1){
                            $("#rankingTable").attr("order","1");
                        }else{
                            $("#rankingTable").attr("order","-1");
                        }
                        getRankList(type,order);
                    }
                }
            ]
        };
        new dispatchEvent(eventOption);

    });
});