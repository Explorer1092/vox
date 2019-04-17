/**
 * Created by yunzhao.li on 2017/1/3.
 */
define(['jquery', 'knockout', 'weui', 'voxLogs'], function($, ko){
    /*--↓ 打点用 ↓--*/
    var _logModule  = 'm_ZxkOZD01',
        _dataType   = /(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent) ? 'app_17Parent_ios' : 'app_17parent_android',
        _sid        = getQueryString("sid");
    /*--↑ 打点用 ↑--*/

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    function openLink(url) {
        if (window.external && window.external['openSecondWebview']) {
            window.external['openSecondWebview'](JSON.stringify(
                {
                    "url": url
                }
            ));
        }else{
            location.href = url;
        }
    }

    function getPicBookUrl(pictureBookId) {
        return location.origin + '/resources/apps/hwh5/homework/V2_5_0/drawBook/index.html?__p__=' + encodeURIComponent(JSON.stringify({
                domain: location.origin,
                env: env || 'test',
                pictureBookIds: pictureBookId,
                subject: 'ENGLISH',
                isMobileReview: true
            }));
    }
    if(location.href.indexOf('/member')!==-1){          //成员列表页
        //打点：绘本小组_成员列表页_被加载
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_LAcvr8av",
            s0       : _sid,
            s1       : getQueryString('rel') || '没有rel参数'
        });

        (function(){
            function MemberModel(){
                var $this = this;
                $this.templateContent = ko.observable('loading');
                $this.database = ko.observable({});
                $this.getTemplateData = function(){
                    $.get("/usermobile/xbt/yesterday/members.vpage?sid="+getQueryString("sid")+'&activity='+getQueryString("activity"),function(res){
                        if(res.success){

                            //mock data 可删除
                            // var res = {
                            //     owner_info : {
                            //         owner_id : 1,
                            //         avatar_url : 'http://cdn-portrait.test.17zuoye.net/gridfs/avatar-30005-52ac7b7ae4b07f63bcbca266.jpg',
                            //         owner_name : 'foo',
                            //         is_used : true,
                            //         picbook_id : 2,
                            //         picbook_name : '17zuoye'
                            //     },
                            //     member_list : [
                            //         {
                            //             member_id : 3,
                            //             avatar_url : 'http://cdn-portrait.test.17zuoye.net/gridfs/avatar-30005-52ac7b7ae4b07f63bcbca266.jpg',
                            //             member_name : 'haha',
                            //             is_used : true,
                            //             picbook_id : 2,
                            //             picbook_name : '17zuoye'
                            //         },
                            //         {
                            //             member_id : 3,
                            //             avatar_url : 'http://cdn-portrait.test.17zuoye.net/gridfs/avatar-30005-52ac7b7ae4b07f63bcbca266.jpg',
                            //             member_name : 'haha',
                            //             is_used : true,
                            //             picbook_id : 2,
                            //             picbook_name : '17zuoye'
                            //         },
                            //         {
                            //             member_id : 3,
                            //             avatar_url : 'http://cdn-portrait.test.17zuoye.net/gridfs/avatar-30005-52ac7b7ae4b07f63bcbca266.jpg',
                            //             member_name : 'haha',
                            //             is_used : true,
                            //             picbook_id : 2,
                            //             picbook_name : '17zuoye'
                            //         }
                            //     ],
                            //     date_str : '2016-01-03'
                            // };
                            res.getUrl = getPicBookUrl;
                            res.sid = _sid;
                            $this.database(res);
                            $this.templateContent('member_list');

                            var titleStr = "小组";
                            res.owner_info && res.owner_info.owner_name && (titleStr = res.date_str ? res.date_str+res.owner_info.owner_name+"小组" : res.owner_info.owner_name+"小组");
                            (window['external'] && window.external['updateTitle']) ? window.external["updateTitle"](titleStr,"","") : document.title = titleStr;
                        }else{
                            var errorInfo = res.info ? res.info : '请求出错';
                            $.alert(errorInfo);
                        }
                    });
                };

                $this.pointRead = function(){
                    var point_read_url = "/view/mobile/parent/learning_tool/huiben/list?app_version=1.6";
                    openLink(point_read_url);
                };

                $this.heroActivity = function(){
                    var hero_act_url = "/usermobile/xbt/index.vpage?activity=readingZdy";
                    openLink(hero_act_url);
                };

                $this.getTemplateData();
            }
            ko.applyBindings(new MemberModel());
        })();
    }else if (location.href.indexOf('/detail')!==-1){   //成员详情页
        //打点：绘本小组_成员详情页_被加载
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_SKV0IYiY"
        });
        (function(){
            function DetailModel(){
                var $this = this;
                $this.templateContent = ko.observable('loading');
                $this.database = ko.observable({});

                $.get("/usermobile/xbt/zdy/member/detail.vpage?member_id="+getQueryString("member_id") + '&sid=' + _sid, function(res) {
                    if (res.success) {
                        //mock data 可删除
                        // var res = {
                        //     "success":true,
                        //     "name":"李淳风",
                        //     "day_count":19,
                        //     "title":"最近阅读/推荐阅读",
                        //     "avatar_url": "http://placeimg.com/640/480/any",
                        //     "pic_book_list":[    // 教材列表
                        //         {
                        //             "picture_book_id":"Bu_id_ll02342131231",
                        //             "picture_book_img":"http://placeimg.com/640/480/any",
                        //             "picture_book_name":"unti 1 aldclclddold"
                        //         },
                        //         {
                        //             "picture_book_id":"Bu_id_ll02342131231",
                        //             "picture_book_img":"http://placeimg.com/640/480/any",
                        //             "picture_book_name":"unti 1 aldclclddold"
                        //         },
                        //         {
                        //             "picture_book_id":"Bu_id_ll02342131231",
                        //             "picture_book_img":"http://placeimg.com/640/480/any",
                        //             "picture_book_name":"unti 1 aldclclddold"
                        //         },
                        //         {
                        //             "picture_book_id":"Bu_id_ll02342131231",
                        //             "picture_book_img":"http://placeimg.com/640/480/any",
                        //             "picture_book_name":"unti 1 aldclclddold"
                        //         },
                        //         {
                        //             "picture_book_id":"Bu_id_ll02342131231",
                        //             "picture_book_img":"http://placeimg.com/640/480/any",
                        //             "picture_book_name":"unti 1 aldclclddold"
                        //         }
                        //     ]
                        // };

                        //获取绘本地址
                        res.getUrl = getPicBookUrl;
                        res.day_count = res.day_count || 0;
                        $this.database(res);
                        $this.templateContent('member-detail');

                        (window['external'] && window.external['updateTitle']) ? window.external["updateTitle"](res.name,"","") : document.title = res.name;

                    }else{
                        var errorInfo = res.info ? res.info : '请求出错';
                        $.alert(errorInfo);
                    }
                });
            }
            ko.applyBindings(new DetailModel());
        })();
    }

    /*--↓ 打点 ↓--*/
    //绘本小组_成员列表页_用户头像_被点击
    $(document).on('click', '.log_avatar', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_zhusekjg",
            s0       : _sid
        });
    });
    //绘本小组_成员列表页_某本绘本名称_被点击
    $(document).on('click', '.log_picbook', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_ViV0EhiB",
            s0       : _sid,
            s1       : $(this).attr('picbook_id')
        });
    });
    //打点：绘本小组_底部tab_被点击
    $(document).on('click', '.log_tab', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_N0xuDI8a",
            s0       : $(this).html()
        });
    });

    //打点：绘本小组_成员详情页_某本绘本_被点击
    $(document).on('click', '.log_book', function(){
        YQ.voxLogs({
            dataType : _dataType,
            database : 'normal',
            module   : _logModule,
            op       : "o_7NNa5OBl",
            s0       : $(this).attr('picbook_id')
        });
    });
    /*--↑ 打点 ↑--*/
});