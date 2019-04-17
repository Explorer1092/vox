/**
 * @author yunzhao.li
 * @description "寒假压岁钱活动"
 * @createDate 2017/01/16
 */
define(['jquery', 'weui', 'voxLogs'], function($){
    //工具方法
    function getQuery(key) {
        var reg = new RegExp("(^|&)" + key + "=([^&]*)(&|$)");
        var res = window.location.search.substr(1).match(reg);
        return res != null ? decodeURIComponent(res[2]) : null;
    }
    function getExternal() {
        var _WIN = window;
        if (_WIN['yqexternal']) {
            return _WIN.yqexternal;
        } else if (_WIN['external']) {
            return _WIN.external;
        } else {
            return _WIN.external = function () {
            };
        }
    }
    function getAppVersion() {
        var native_version = "2.5.0";
        if (getExternal()["getInitParams"]) {
            var $params = getExternal().getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                native_version = $params.native_version;
            }
        }
        return native_version;
    }
    function openApp(_this) {
        //fix 兼容ios和Android 实现pageQueueNew和openFairylandPage的差异。#38664
        var en = getQuery('from') === 'fairyland' ? 'pageQueueNew' : 'openFairylandPage';
        if (getExternal()[en]) {
            setTimeout(function () {
                getExternal()[en](JSON.stringify({
                    url: window.location.origin + _this.launchUrl,
                    name: "fairyland_app:" + (_this.appKey || "link"),
                    useNewCore: _this.browser || "system",
                    orientation: _this.orientation || "sensor",
                    initParams: JSON.stringify({hwPrimaryVersion: _this.hwPrimaryVersion || "V2_4_0"}),
                    page_viewable: true
                }));
            }, 200);
        } else {
            YQ.voxLogs({
                module: 'fairyland_app',
                op: 'error',
                s0: 'wonderland-activity-lottery-null'
            });
        }
    }
    //简单处理下，防止页面跳转时统计数据丢失
    function redirectTo(url){
        setTimeout(function(){
            location.href = url;
        },200);
    }

    //网络延迟较大跳转提示：跳转中...
    $(document).on('click', '.anchor', function(){
        var $this = $(this);
        //标签data-initial-text属性用于存放原始文案，data-redirecting-tip属性用于指定跳转提示
        $this.attr('data-initial-text', $this.html()).html($this.attr('data-redirecting-tip') || '跳转中···');
    });

    //打点用
    var _logModule      = 'm_5OHs5iMq';

    //活动首页
    if(window.indexVM){
        //打点：压岁钱页面被加载
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_yWMvBDPI"
        });
        (function(){
            var indexVM = window.indexVM;

            var data = {
                activityId: getQuery('activityId') || 'WLA_20170001',
                version: getAppVersion()
            };
            function initData(){
                $.get('/wonderland/activity/fetchactivitydata.vpage', data, function(res){
                    if(res.success){
                        indexVM.dataReady=true;       //数据加载完成
                        $.extend(indexVM, res);
                    }else{
                        $.alert(res.info);
                    }
                }).fail(function(){
                    $.alert('网络连接错误');
                });
            }
            initData();

            //点击领取压岁钱
            $(document).on('click', '.receive-credit', function(){
                //打点：压岁钱页面领取按钮被点击
                YQ.voxLogs({
                    module  : _logModule,
                    op      : "o_FKUtr9kj"
                });

                if(indexVM.locked){     //如果领取按钮被锁住
                    return;
                }
                indexVM.locked = true;
                $.get('/wonderland/activity/ysq.vpage', data, function(res){
                    indexVM.locked = false;             //解锁
                    if(res.success){
                        indexVM.popup = true;
                        indexVM.amount = res.amount;
                        initData();
                    }else{
                        $.alert(res.info || 'Error！');
                    }
                }).fail(function(){
                    indexVM.locked = false;             //解锁
                    $.alert('网络连接错误');
                });
            });
        })();
    }

    //抽奖页面
    if(window.lotteryVM){
        //打点：抽奖页面被加载
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_nhPAjT9C"
        });

        (function () {
            var lotteryVM = window.lotteryVM;
            var data = {
                activityId: getQuery('activityId') || 'WLA_20160001',
                version: getAppVersion()
            };

            lotteryVM.initData = function(){
                $.get('/wonderland/activity/fetchactivitydata.vpage', data, function(res){
                    if(res.success){
                        lotteryVM.dataReady=true;       //数据加载完成，合并更新数据
                        $.extend(lotteryVM, res);
                    }else{
                        $.alert(res.info);
                    }
                }).fail(function(){
                    $.alert('网络连接错误');
                });
            };
            lotteryVM.initData();//初始化数据

            $('.draw-lottery').on('click', function(){
                //打点：抽奖页面开始抽奖按钮被点击
                YQ.voxLogs({
                    module  : _logModule,
                    op      : "o_Cs8Sv5rL"
                });

                if(!lotteryVM.dataReady || !lotteryVM.freeChance || lotteryVM.drawing){              //如果数据未加载好或没有抽奖次数或正在抽奖中
                    return;
                }
                lotteryVM.drawing = true;           //锁住抽奖按钮
                $.get('/wonderland/activity/drawlottery.vpage', data, function(res){
                    if(res.success){
                        lotteryVM.product = res;
                        lotteryVM.n = $('#'+res.award.id).index();
                        lotteryVM.freeChance = 0;
                    }else{
                        lotteryVM.stopRoll();
                        $.alert(res.info);                                  //弹窗错误提示
                    }

                    //打点：抽奖结果被加载
                    YQ.voxLogs({
                        module  : _logModule,
                        op      : "o_vR11vMAf",
                        s0      : res.award.id,
                        s1      : res.award.name
                    });
                }).fail(function(){
                    lotteryVM.stopRoll();
                    $.alert('网络连接错误');
                });
            });

            lotteryVM.openApp = openApp;
            $(document).on('click', '.used-btn', function(){
                $.alert('已经使用过了哦');
            });
        })();
    }

    //打点：压岁钱页面兑换预览按钮被点击
    $(document).on('click', '.log-rule', function(){
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_0iPmq3VI"
        });

        redirectTo('/studentMobile/activity/luckymoney/rule.vpage');
    });
    //打点：压岁钱页面去抽奖按钮被点击
    $(document).on('click', '.log-draw', function(){
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_v7cOx0mm"
        });
    });
    //打点：压岁钱页面获取更多学分按钮被点击
    $(document).on('click', '.log-buy', function(){
        YQ.voxLogs({
            module  : _logModule,
            op      : "o_FKPLiDVB"
        });

        redirectTo('/studentMobile/activity/ysq/buy.vpage?userType=student');
    });
});
