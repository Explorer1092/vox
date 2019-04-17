/**
 * @author:
 * @description: "一起测校长报告"
 * @createdDate: 2018/9/4
 * @lastModifyDate: 2018/9/4
 */

// YQ: 通用方法(public/script/YQ.js), impromptu: 通用弹窗（http://trentrichardson.com/Impromptu/）, voxLogs: 打点
define(["jquery", "vue", "YQ", "echarts", "echarts-adminteacher", "impromptu", "voxLogs"],function($, Vue, YQ, echarts) {
    // 注：该项目无编译机制，不要使用es6语法

    var databaseLogs = "tianshu_logs"; // 打点的表
    var EChartsTheme = "walden"; // echart配色主题，假设配色文件为echarts-adminteacher

    // 打点方法如下：
    YQ.voxLogs({
        database: databaseLogs, // 表名
        module: '', // module
        op: '', // op
        s0: '', // s0, 有则带上
        s1: '' // s1, 有则带上
    });

    /*
     * 简单弹窗提示
     */
    var alertTip = function (content, title, callback) {
        var title = title || '系统提示';

        $.prompt(content, {
            title: title,
            buttons: {'确定': true},
            focus : 0,
            position: {width: 500},
            submit : function(e, v){
                if(v){
                    e.preventDefault();
                    if (callback) {
                        callback();
                    } else {
                        $.prompt.close();
                    }
                }
            }
        });
    };

    new Vue({
        el: '#rstaffPage',
        data: {
            a: '变量1',
            b: '变量2'
        },
        mounted: function () {
            alertTip('我是简单提示的弹窗，如接口报错');
        },
        computed: {

        },
        methods: {
            methodA: function () {

            },
            methodB: function () {

            }
        }
    });
});