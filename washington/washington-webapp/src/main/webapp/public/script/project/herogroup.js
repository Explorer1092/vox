/**
 * Created by free on 2016/12/20.
 */
define(['jquery', 'knockout', 'weui', 'voxLogs'], function($, ko){
    function groupModal(){
        var $this = this;
        $this.templateContent = ko.observable('loading');
        $this.database = ko.observable({});
        $this.getTemplateData = function(){
            $.get("/usermobile/xbt/yesterday/members.vpage?sid="+getQueryString("sid"),function(res){
                if(res.success){
                    $this.database(res);
                    $this.templateContent('hero_group_list');

                    var titleStr = "小组";
                    if(res.owner_info && res.owner_info.owner_name){
                        titleStr = res.date_str ? res.date_str+res.owner_info.owner_name+"小组" : res.owner_info.owner_name+"小组" ;
                    }

                    if (window['external'] && window.external['updateTitle']) {
                        window.external["updateTitle"](titleStr,"","");
                    }else{
                        document.title = titleStr;
                    }
                }else{
                    var errorInfo = res.info ? res.info : '请求出错';
                    $.alert(errorInfo);
                }
            });
        };

        $this.pointRead = function(){
            var point_read_url = "/view/mobile/parent/learnTools_guide";
            openLink(point_read_url);
        };

        $this.heroActivity = function(){
            var hero_act_url = "/usermobile/xbt/index.vpage";
            openLink(hero_act_url);
        };

        $this.getTemplateData();
    }

    var getQueryString = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    };

    var openLink = function (url) {
        if (window.external && window.external['openSecondWebview']) {
            window.external['openSecondWebview'](JSON.stringify(
                {
                    "url": url
                }
            ));
        }else{
            location.href = url;
        }
    };

    ko.applyBindings(new groupModal());

    /*--↓ 打点用 ↓--*/
    var _logModule  = 'm_ZxkOZD01',
        _dataType   = /(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent) ? 'app_17Parent_ios' : 'app_17parent_android',
        _sid        = getQueryString("sid");
    /*--↑ 打点用 ↑--*/

    /*--↓ 打点 ↓--*/
    //打点：点读机小组_英雄团成员列表页_被加载
    YQ.voxLogs({
        dataType : _dataType,
        database : 'normal',
        module   : _logModule,
        op       : "o_tPIB017f",
        s0       : _sid
    });
    //打点：点读机小组_底部tab_被点击
    $(document).on('click', '.log_tab', function(){
        console.log(1);
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_W0W0i1dt",
            s0       : $(this).html()
        });
    });
    /*--↑ 打点 ↑--*/
});