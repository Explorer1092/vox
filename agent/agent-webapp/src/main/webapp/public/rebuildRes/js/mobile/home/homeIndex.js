/**
 * 首页
 * */

    $(document).ready(function () {
        var AT = new agentTool();
        AT.cleanAllCookie();

        //渲染页面
        var renderTemp = function (tempSelector, data, container) {
            var source = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };
        if(has_elements_roles){
            $.get("performanceProgress.vpage", function (res) {
                $('.apply_box1').html(template("cardItemTemp",{res:res}));
                $(".performanceData").show();
            });
        }
        var regionMsgHref = "";
        if(show_region){
            $.get("get_region_message.vpage", function (res) {
                if (res.success) {
                    if (res.regionMessage) {
                        $("#regionMsgDiv").html(res.regionMessage.message);
                        regionMsgHref =  "/mobile/performance/regionmsg.vpage?groupId=" + res.regionMessage.groupId;
                    }
                } else {
                    AT.alert(res.info);
                }
            });
        }

        $(document).on('click', '.js-item', function () {
            var _this = $(this),
                id = _this.data("id"),
                idType = _this.data("idtype"),
                level = _this.data("level"),
                mode = _this.data("mode"),
                href = "/view/mobile/crm/reportDisplay/report.vpage?id=" + id + "&idType=" + idType + "&schoolLevel="+level +"&mode="+mode+"&isCountryManager="+isCountryManager;
            openSecond(href);
        });
        $(document).on('click', '.js-regionMsg', function () {
            if(regionMsgHref != ""){
                openSecond(regionMsgHref);
            }
        });

    });
    Handlebars.registerHelper({
        'computeAdd': function() {
            var big = 0;
            try{
                var len = arguments.length - 1;
                for(var i = 0; i < len; i++){
                    if(arguments[i]){
                        big = eval(big +"+"+ arguments[i]);
                    }
                }
            }catch(e){
                throw new Error('Handlerbars Helper "computeAdd" can not deal with wrong expression:'+arguments);
            }
            return big;
        },
        'multiplication': function() {
            var big = 0;
            try{
                var len = arguments.length - 1;
                for(var i = 0; i < len; i++){
                    if(arguments[i]){
                        big = Math.floor(eval(100 +"*"+ arguments[i]));
                    }
                }
            }catch(e){
                throw new Error('Handlerbars Helper "computeAdd" can not deal with wrong expression:'+arguments);
            }
            return big;
        }
    });

