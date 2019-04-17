/* global define : true, PM : true, location : true, $:true */
/**
 *  @date 2015/9/16
 *  @auto liluwei
 *  @description 该模块主要负责最新作业页面的逻辑
 */

define(['ajax', 'getFromRecordByJq', 'fullTemplate', 'versionCompare', 'shortHref', 'tab', 'jqPopup'], function( ioPromise, getFromRecordByJq, fullTemplate, versionCompare, createShortHref){

    'use strict';

    var win = window,
        doc = document;

	var doTrack = PM.doTrack || $.noop;

    $(function(){

        // 获取奖励
        $.iosOnClick(
            '.doRewardbystarrank',
            function(){
                var $self = $(this),
                    data = $self.data();

                ioPromise(
                    '/parentMobile/reward/rewardbystarrank.vpage',
                    {
                        sid : data.sid,
                        month : +data.starrank_month
                    },
                    'POST',
                    {
                        seq : true
                    }
                )
                .done(function(res){
                     if(!res.success){
                         $.alert('获取奖励失败  ' + res.info);
                         return ;
                     }

                     $self.removeClass('doRewardbystarrank').text('已领取奖励').addClass("btn-s-disable");
                });
            }
        );

        // 获取当月 当学期积分
        (function(){
            if($(".parentMobileHomeworkReport").length === 0){
                return ;
            }
            var starUrl = '/parentMobile/reward/laststarrewardinfo.vpage';

            var templateArr = [

                // 当月积分
                [starUrl, { sid : PM.sid, currentMonth : 0 }, 'doRankMonth', 'getRank'],

                // 当学期积分
                [starUrl, { sid : PM.sid, currentMonth : 1 }, 'doRankTerm', 'getRank']

            ];

            fullTemplate( templateArr );
        })();

        // 家长任务相关
        (function(){
            // 创建任务
            $.iosOnClick(
                '.doMissionBox .doSetIntegralMission',
                function(){
                    var $self = $(this),
                        integral_num = $self.data('integral_num'),
                        toggleIntegralShow = function(fixIntegral, wish, wishType){
                            $('.integralShow')
                                .find('.fixIntegral')[fixIntegral]()
                                .end()
                                .find('input[name="wish"]')[wish]();

                            $('input[name="wishType"]').val(wishType);
                        };

                    integral_num ?
                        toggleIntegralShow("show", "hide", "INTEGRAL") :
                        toggleIntegralShow("hide", "show", "CUSTOMIZE");

                    $self.closest('.doMissionBox').hide();

                    $(".doStepV2").show();

                }
            )
            .iosOnClick(
                '.showClazzListByEdu .doSetMission',
                function(){
                    var $self = $(this),
                         parent = $self.closest('.showClazzListByEdu');

                    parent.find('.doSetMission').removeClass('active');
                    $self.addClass('active');

                    $('input[name="totalCount"]').val(
                        $self.parent().data('num')
                    );

                }
            )
            .iosOnClick(
                '#do_set_customize_mission',
                function() {
                    var $self = $(this),
                         ajaxData =  getFromRecordByJq(
                             $self.closest('.doStepV2')
                         );

                    ioPromise(
                        "/parentMobile/parentreward/setmission.vpage",
                        ajaxData,
                        "POST"
                    )
                    .done(function(res){
                        if(!res.success){
                            $.alert("创建错误, " + res.info);
                            return ;
                        }

                        !PM.doExternal( "goHome", "0" );
                        location.href="/parentMobile/parentreward/getmissions.vpage?sid=" + ajaxData.sid + "&cp=1&app_version="+((PM.client_params&&PM.client_params.app_version)||PM.app_version||'');

                    });

                }
            );

            // 更新任务进度
            var doTrackForUpdateProgressFail = function(trackType){
              doTrack("parent", trackType + "_fail");
            };

            $.iosOnClick(
                ".doUpdateProgress",
                function(){
                    var data = $(this).data(),
                        baseUrl = "/parentMobile/parentreward/",
                        url = +data.completed === 1 ?
                            baseUrl + "updatecomplete":
                            baseUrl + "updateprogress";

                    var trackType = data.track_error;

                    ioPromise(
                        url + ".vpage",
                        {
                            missionId : data.missionid
                        },
                        "POST"
                    )
                    .done(function(res){
                        if(!res.success){
                            doTrackForUpdateProgressFail(trackType);
                            $.alert('修改进度失败');
                            return ;
                        }

                        location.reload(true);
                    })
                    .fail(function(){
                         doTrackForUpdateProgressFail(trackType);
                    });
                }
            );

            var acceptedTypes = [
                    'image/png',
                    'image/jpeg',
                    'image/gif'
                ],
                uploadInfo = {
                    uploading : false
                },
                uploadFile = $("#uploadImage").get(0);

            //TODO 上传图片 目前只支持 每次只能上传一张 上传成功后 就刷新本界面
            $.iosOnClick(
                '.doUploadPic',
                function(){
                    if(uploadInfo.uploading){
                        return $.alert("请等待上一个上传图片完成");
                    }

                    var $selfData = $(this).data();

                    uploadInfo.missionId = $selfData.missionid;

                    var mockClickEvent = document.createEvent("MouseEvent");
                    mockClickEvent.initEvent("click", false, false);
                    uploadFile.dispatchEvent(mockClickEvent);
                }
            );

            $(doc)
                .on(
                'change',
                '#uploadImage',
                function(){

                    var loading = $.loading("正在上传图片");

                    var dfd = $.Deferred(),
                        resetUploadStatus = function(){
                            uploadInfo.uploading = false;
                        };

                    dfd
                    .always(resetUploadStatus)
                    .fail(function(error){
                        doTrack("parent", "photo_fail");
                        loading.destory();
                        $.alert('上传错误: ' + error);
                    });

                    var image = this.files[0];

                    if(acceptedTypes.indexOf(image.type) === -1){
                        return dfd.reject( '只能上传图片' );
                    }

                    if(image.size>2097152){
                        return dfd.reject( '图片太大了 伙计' );
                    }

                    uploadInfo.uploading = true;

                    var reader =  new win.FileReader();

                    reader.onload = function (event) {
                        uploadInfo.filedata = event.target.result.replace(/^.*base64,/, '');

                        var formData = new win.FormData();

                       $.each(uploadInfo, function(key, value){
                           formData.append(key, value);
                       });

                        ioPromise(
                            "/parentMobile/parentreward/uploadpicture.vpage",
                            formData,
                            "POST",
                            {
                                processData: false,
                                contentType: false
                            }
                        )
                        .done(function(res){

                            if(!res.success){
                                return dfd.reject(res.info);
                            }

                            location.reload(true);
                        })
                        .always(resetUploadStatus)
                        .fail(dfd.reject);
                    };

                    reader.onerror = dfd.reject;

                    reader.readAsDataURL(image);

                }
            );

        })();

        // 反馈
        (function(){

            $.iosOnClick(
                '.doFeedTypes .doFeedType',
                function(){
                    var $active = $(this).closest('.doFeedTypes').find('.doFeedType').removeClass('active').end().end().addClass("active");
                }
            )
            .iosOnClick(
                '.doSendFeedback',
                function(){

                    var feedbackText = $.trim(
                            $('textarea[name="q"]').val()
                        ),
                        type = $.trim($(".doFeedType.active").text());

                    if(!feedbackText){
                        $.alert("必须填写反馈信息");
                        return ;
                    }

                    if(feedbackText.length > 250){
                        $.alert("反馈信息不能超过250个字");
                        return ;
                    }

                    ioPromise(
                        '/parentMobile/parent/submitquestion.vpage',
                        {
                            q : feedbackText,
                            type : type
                        },
                        'POST'
                    )
                    .done(function(res){
                        if(!res.success ){
                            $.alert("反馈失败 请重新获取");
                            return ;
                        }

                        $('body').html( $(".doFeedbackDone").show() );

                    });
                }
            );

        })();

        //创建订单(课外乐园)
        (function(){

            var doLevelBoxSelector = "doLevelBox",
                $doLevelBox = $("." + doLevelBoxSelector),
                fullLevelBox = $.noop,
                doLevelBoxIsExist = $doLevelBox.length === 1,
                doStem101LevelProduct = $(".doStem101LevelProduct"),
                doStem101LevelProductMap = {};


            $(doStem101LevelProduct).each(function(i, t){
                var type = $(t).data("type"),
                    id = $(t).val();

                !doStem101LevelProductMap[type] && (doStem101LevelProductMap[type] = []);
                doStem101LevelProductMap[type].push(id);
            });


            $(".do_special_shop").each(function(index){
                $(this).parent().remove();
            });

            if(doLevelBoxIsExist){
                var $doLevelBoxTemp = $("#" + doLevelBoxSelector + "Temp");
                fullLevelBox = function(type){
                    $doLevelBox.html(
                        fullTemplate.template(
                            $doLevelBoxTemp.html()
                        )(
                            {
                                "productIds":doStem101LevelProductMap[type]
                            }
                        )
                    );

                    //某一阶段下全部已购买处理
                    setTimeout(function(){
                        if($(".doLevel").length===0){
                            $(".doCreateOrderPrice").text("已全部购买");
                            $(".doCreateOrder").hide();
                        }else{
                            $(".doCreateOrder").show();
                            $doLevelBox.find('.doLevel:first').addClass('active').click();
                        }
                    }, 200);
                };

                $.iosOnClick(
                    ".doLevel",
                    function(){
                        var $self = $(this);

                        if($self.hasClass("doBuyed")){
                            return false;
                        }

                        $('.doCreateOrderForm [name="productId"]').val(
                            $self.attr("data-productId")
                        );

                    }
                );
            }

            $
            .iosOnClick(
                ".doCreateOrder",
                function(){
                    var $self = $(this),
                        ajaxData =  getFromRecordByJq(
                            $self.closest('.doCreateOrderForm')
                        );

                    if(doLevelBoxIsExist && $(".doLevel.active").length === 0){
                        $.alert("您已购买该产品，请选购其他产品");
                        return ;
                    }

                    if($self.hasClass('dis')){
                        return ;
                    }

                    doTrack("m_DOZ8e8Px","o_rHqn2uVm",document.title+"_"+$self.text());
					var do_pay = function(){
						//pay confirm from wechat
						if(window.isFromWeChat()){
							ajaxData["refer"] = "200002";
						}else{
                            if(getQuery('refer') != null && getQuery("refer") != ''){
                                ajaxData["refer"] = getQuery('refer');
                            }else if(getQuery('orderReferer') == "mistakennotebook"){
						        //错题本
                                ajaxData["refer"] = "240004";
                            }else{
                                //获取订单来源
                                ajaxData["refer"] = "200001";

                                {//获取订单来源
                                    try{
                                        var order_refer = window['external']['localStorageGet'](
                                            JSON.stringify({
                                                category : "orderPay",
                                                key : 'referer'
                                            })
                                        )

                                        JSON.parse(order_refer) && (ajaxData["refer"] = JSON.parse(order_refer).value);
                                    }catch(e){
                                        console.warn(e);
                                    }
                                }

                            }
						}

                        $self.addClass('dis');
						ioPromise(
							'/parentMobile/order/createorder.vpage',
							ajaxData,
							'POST'
						)
						.done(function(res){
                            $self.removeClass('dis');

                            if(res.isOverTime){
                                $.alert("您已开通该产品，请直接进入学习~");
                                return ;
                            }
							if(!res.success){
								$.alert("购买失败");
								return ;
							}

                            //苹果支付测试支持
                            if($self.data("is_use_applepay")){
                                PM.doExternal("payOrder", JSON.stringify({
                                    orderId : res.orderId,
                                    orderType : 'order',
                                    payType : 4,
                                    data:JSON.stringify({
                                        product_id : ajaxData['productId']
                                    })
                                }));

                                return;
                            }


							if(versionCompare((PM.app_version || '0') , "1.5.5")>-1){
								PM.doExternal("openSecondWebview",JSON.stringify({
									url:"/view/mobile/parent/pay/order_detail?oid=" +  res.orderId+"&otype=order"
								}));
							}else{
								location.href = "/parentMobile/order/loadorder.vpage?oid=" +  res.orderId;
							}
						});

						//沃克打点
						if(getQuery('orderReferer') == "walkerTrialReport"){
							if($('a.doCreateOrder').length != 0){
							doTrack("walkerReportPay","walkerPayBtnClick");
							}
						}
					};

					var do_pay_pre = function(){
                        if($self.data('only_pc')){
                            $.alert('学生只能在电脑（不含平板电脑）使用本产品', {
                                on_ok : function(){
                                    do_pay();
                                    return true;
                                }
                            });
                        }else{
                            do_pay();
                        }
					};

					var dataset_tip = $self.data('tip');

					if(dataset_tip){
						$.alert(dataset_tip, {
							on_ok : function(){
								do_pay_pre();
								return true;
							}
						});
					}else{
						do_pay_pre();
					}

                }
            )
            .iosOnClick(".doToggleActive", function(){
                var $self = $(this);

                if($self.hasClass("doBuyed")){
                    return false;
                }
                $self.closest(".doToggleActives").find(".active").removeClass("active")
                    .end()
                    .find($self)
                    .addClass("active");

            })
            .iosOnClick(
                ".doCreateOrderPeriod",
                function(){
                    var $self = $(this);

                    var price = $self.data("price");
                    $(".doCreateOrderPrice").text(price);

                    var origin_price=$self.data("origin-price");
                    $(".doCreateOrderOriginPrie").text(
                        "￥"+origin_price
                    )[((origin_price&&(+price!=+origin_price))?"show":"hide")]();

                    $('.doCreateOrderForm [name="productId"]').val(
                        $self.data("product_id")
                    );

                    var tip = $self.data('tip');

                    tip && $(".do_tip").text(tip);
                    fullLevelBox($self.find(".doStem101LevelProduct").data("type"));

                }
            ).iosOnClick(
                ".JS-gotoGame",
                function() {
                    var $self = $(this);
                    var $dataValue = $self.attr("data-value");
                    var $gameItem = [];

                    if($dataValue){
                        $gameItem = $self.attr("data-value").split(',');
                    }

                    var launchUrl = $gameItem[1];

                    if(win.productType === 'PicListenBook'){
                        PM.doExternal("innerJump", JSON.stringify({
                            name:'point_read',
                            params:JSON.stringify({
                                type: 'specific_book',
                                book_id:((win.location.search||'').match(new RegExp("book_id=([^&]*)(&|$)", "i"))||[])[1]||'',
                                sdk: $self.attr("data-sdk"),
                                sdk_book_id: $self.attr("data-sdk_book_id"),
                                purchase_url : location.href

                            })
                        }));
                    }else if(win.productType === 'FeeCourse'){
                        PM.doExternal("openSecondWebview", JSON.stringify({
                            shareType    : 'NO_SHARE_VIEW',
                            shareContent : '',
                            shareUrl     : '',
                            type         : '',
                            url          : launchUrl
                        }));
                    }else{
                        var refer = getQuery("refer");
                        if(refer == null || refer == '') refer = "200001";
                        launchUrl += "&refer=" + refer;
                        PM.doExternal("openFairylandPage", JSON.stringify({
                            name: "fairyland_app:" + ($gameItem[0] || "link"),
                            url: hasUrlHttp(launchUrl),
                            useNewCore: $gameItem[2] || "system",
                            orientation: $gameItem[3] || "sensor",
                            initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                        }));
                    }
                    doTrack("m_DOZ8e8Px","o_7BKv3gLE",document.title+"_"+$self.text());
                }
            );


            // TODO 这真的是暂时的方案. 以后会在每个产品的表中配置相关字段
            $(".doCreateOrderPeriod").eq(window.shopDefaultPeriodIndex).click();

            //是否带http
            function hasUrlHttp(url){
                if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
                    return url;
                }

                return location.protocol + '//' + location.host + url;
            }




            var shopDetailPageDom = $("#do-shopDetailPage");
            if(shopDetailPageDom.length>0){
                //url里没有sid时添加切换孩子功能
                var url_search = win.location.search,
                    hasChildChange = url_search.indexOf('hasChildChange=true')>-1,
                    getAppVersion = function(){
                        var app_version = "";

                        try{
                            app_version = JSON.parse(win.external.getInitParams()).native_version;
                        }catch(e){
                            app_version = '';
                        }

                        return app_version;
                    },
                    reload_jump = function(sid){
                        win.location.search=[
                            url_search.match(/sid=\d+/)?url_search.replace(/sid=\d+/,'sid='+sid):url_search+'&sid='+sid,
                            hasChildChange?'':'&hasChildChange=true',
                            url_search.match(/app_version=/)?'':'&app_version='+getAppVersion()
                        ].join('');
                        return;
                    };


                if(!+(PM.sid||'0')|| hasChildChange){
                    $.get('/parentMobile/ucenter/shoppinginfo/availablestudentlist.vpage?productType='+win.productType, function(data){
                        if(data.success&&(data.students||[]).length>0){
                            if(!+(PM.sid||'0')){
                                reload_jump(data.students[0].id);
                            }

                            var is_has_active = function(item, i){
                                    if(PM.sid){
                                        if(item.id===PM.sid) return 'active';
                                    }else{
                                        if(i===0) return 'active';
                                    }
                                },
                                tpl = data.students.map(function(item, i){
                                return [
                                    '<div class="pur-image do_change_child '+is_has_active(item, i)+'" data-sid="'+item.id+'">',
                                        '<div class="img"><img src="'+item.img+'"></div>',
                                        '<div class="name">'+item.name+'</div>',
                                    '</div>'
                                ].join('');
                            }).join('');
                            $("#child_list_box").html(tpl);
                        }
                    });

                    $.iosOnClick(".do_change_child",function(){
                        var $self = $(this),
                            sid = $self.data("sid");


                        if($self.hasClass("active")){
                            return false;
                        }


                        reload_jump(sid)
                    });
                }

                //所有产品已购买处理
                if($(".doCreateOrderPeriod").length===0){
                    $(".grayLevel").removeClass("active");
                    $(".doCreateOrderPrice").text("已全部购买");
                    $(".doCreateOrder").remove();
                }

                //非学生端，家长端打开remove ".JS-gotoGame" btn
                var ua = navigator.userAgent.toLowerCase();
                if(ua.search('17parent') === -1 && ua.search('17student') === -1){
                    $(".JS-gotoGame").remove();
                }


                //打点
                win.productType && doTrack("m_DOZ8e8Px", "o_ePuVcz7q", document.title+'@'+win.productType+'@'+((win.location.search||'').match(new RegExp("book_id=([^&]*)(&|$)", "i"))||[])[1]||'', {track_s_separator:'@'});
            }
        })();

        (function(){
            //TODO 这只是一个暂时的临时的分页方法
            var $parentMobileOrderList = $(".parentMobileOrderList");

            if( $parentMobileOrderList.length === 0 ){
               return ;
            }

            $.iosOnClick(
                ".doTabAjaxPage a",
                function(event){
                    event.preventDefault();

                    $(this).closest(".doTabBlock").find(".doTab.active").data("tab_ajax_url", this.href).click();
                }
            );

        })();


        // 创建贡献学豆订单
        $(".doBean").data(
            "tab_change_fn",
            function(){
                $('.doPrice').html('<em>￥</em>'+$(this).data("price"));
            }
        );

        $.iosOnClick(
            ".doCreateGiveBeanOrder",
            function(){
                var sid = PM.sid,
                    tid = +$(".doSubject.active").data("tid"),
                    $beanDom =  $(".doBean.active"),
                    price = $beanDom.data("price"),
                    priceTag = $beanDom.data("price_tag"),
                    isActivity = $(this).data("is_activity");

                if(!tid){
                    return $.alert("获取老师编号失败");
                }

                ioPromise(
                    "/parentMobile/order/createintegralorder.vpage",
                    {
                        sid : sid,
                        tid : tid,
                        price : priceTag,
                        isActivity : isActivity
                    },
                    "POST"
                )
                .done(function(createIntegralOrderCbData){
                    if(!createIntegralOrderCbData.success){
                        return $.alert('生成贡献学豆订单失败: ' + createIntegralOrderCbData.info);
                    }

                    //新老板本兼容
                    if(versionCompare(((PM.client_params&&PM.client_params.app_version) || PM.app_version||'') , "1.5.5")>-1){
                        PM.doExternal("openSecondWebview",JSON.stringify({
                            url:"/view/mobile/parent/pay/order_detail?oid=" + createIntegralOrderCbData.orderId+"&otype=integral"
                        }));
                    }else{
                        $(".main").replaceWith(
                            fullTemplate.template(
                                "payOrderTemp",
                                {
                                    orderId : createIntegralOrderCbData.orderId,
                                    beanCount : $beanDom.data("bean_count"),
                                    price : price
                                }
                            )
                        );
                    }
                });
            }
        );

        // 点击支付
        $.iosOnClick(".doPay", function(){

            var $self = $(this),
                selfData = $self.data(),
                oid = "" + selfData.order_id,
                orderProductType = selfData.order_type;
            if(versionCompare((PM.app_version || '0') , "1.5.5")>-1){
                PM.doExternal("openSecondWebview",JSON.stringify({
                    url:"/view/mobile/parent/pay/order_detail?oid=" + oid+"&otype="+orderProductType
                }));
            }else{
                //pay action from wechat
                if(window.isFromWeChat()){
                    var url = window.location.protocol + '//' + wechatUrlHeader + '/parent/wxpay/pay-'+orderProductType+'.vpage?oid='+oid;
                    location.href = url;
                }else{
                    return PM.doExternal("payOrder", oid, orderProductType);
                }
            }
        });

        // 点击分享
        var isNotSupportJsonData = versionCompare((PM.app_version || '0') , "1.3.5") !== 1;

        $.iosOnClick(".doShare", function(){
            if(!PM.isWebview){
                return ;
            }

            var $self = $(this),
                $selfData = $self.data(),
                url = location.href + "&isShare=1";

            createShortHref(url)
            .done(function(shortUrl){
                $self.data("url", shortUrl);

                var shareData =
                    isNotSupportJsonData ?
                        shortUrl
                        :
                        JSON.stringify(
                            $.extend(
                                {
                                    type : "SHARE",
                                    content : "",
                                    url : ""
                                },
                                $selfData  // 必须包含 url type content
                            )
                        );

                PM.doExternal("alertDialog", shareData );
            })
            .fail(function(errorMsg){
                $.alert(errorMsg);
            });

        });

        // 星星绑定
        (function(){
            var starUrl = '/parentMobile/home/getIntegralChip.vpage',
                targetDomId = "doIntegralInfo",
                $targetDom = $("#" + targetDomId);

            if($targetDom.length === 0){
                return ;
            }

           var doFullPage = function(){
                var pageIndex = $targetDom.data("current_page"),
                    templateArr = [
                        [starUrl, { sid : PM.sid, pageIndex : pageIndex }, targetDomId, 'doIntegralTemp']
                    ];

                fullTemplate( templateArr );
               $("html").scrollTop(0);
            };

            doFullPage();

            $.iosOnClick(
                ".doPage",
                function(){
                    $targetDom.data("current_page", $(this).data('page'));
                    doFullPage();
                }
            );

        })();

        var getQuery = function(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };

        //沃克报告打点
        if(getQuery('orderReferer') == "walkerTrialReport"){
            doTrack("walkerReportPay","getPayIndex");
        }

        //孩子个人信息页事件处理
        (function(){
            var operate="",
                clickDOM=null;

            win.vox =win.vox || {};
            win.vox.task || (win.vox.task = {});
            var task = win.vox.task;

            $.iosOnClick(".J-doClick", function(){
              /* if(!PM.isWebview){
                    return ;
                }*/
                var $self = $(this);
                clickDOM=$self;
                operate=$self.data("operate");
                switch (operate){
                    case "switch-school":{
                        PM.doExternal("selectSchool");
                        break;
                    }
                    case "switch-grade":{
                        PM.doExternal("selectDataWidget", JSON.stringify({
                            "list":[
                                {"name":"学龄前","value":0},
                                {"name":"1年级","value":1},
                                {"name":"2年级","value":2},
                                {"name":"3年级","value":3},
                                {"name":"4年级","value":4},
                                {"name":"5年级","value":5},
                                {"name":"6年级","value":6},
                                {"name":"初中","value":7}
                            ],
                            "selectIndex":1
                        }));
                        break;
                    }
                    //选择性别
                    case "switch-gender":{
                        PM.doExternal("selectDataWidget",JSON.stringify({
                            "list":[{"name":"男","value":"M"},{"name":"女","value":"F"}],
                            "selectIndex":0
                        }));
                        break;
                    }
                    //选择年龄
                    case "switch-age":{
                        var max_timestamp = (new Date()).setFullYear(new Date().getFullYear()-0),
                            min_timestamp = (new Date()).setFullYear(new Date().getFullYear()-15);

                        PM.doExternal("showTimerPicker",JSON.stringify({
                            timestamp : max_timestamp,
                            max_timestamp: max_timestamp+"",
                            min_timestamp: min_timestamp+""
                        }));
                        break;
                    }
                    //加入班级
                    case "join-clazz":
                    {
                        PM.doExternal("commonJs", JSON.stringify({
                            "type": "OPEN_STUDENT_APP_MAIN"
                        }));
                        break;
                    }
                    //如何获取老师老师号码弹窗
                    case "get-help-show":{
                        $("#J-get-help-box").stop().show(0,function(){
                           $("#J-get-help-box-inner").stop().slideDown(100);
                        });
                        break;
                    }
                    //如何获取老师老师号码弹窗
                    case "get-help-close":{
                        $("#J-get-help-box-inner").stop().slideUp(100,function(){
                            $("#J-get-help-box").stop().hide();
                        });
                        break;
                    }
                }
            });

            var updateData=function(data,cal){
                var url="",
                    params={"sid":PM.sid};
                switch(operate){
                    case "switch-school":{
                        url="/parentMobile/home/update_channel_c_student_school.vpage";
                        params.school_id=data.schoolId,
                        params.school_name= $.trim(data.schoolName),
                        params.region_code=data.regionCode;
                        break;
                    }
                    case "switch-grade":{
                        url="/parentMobile/home/update_channel_c_student_clazz_level.vpage";
                        params.clazz_level=data;
                        break;
                    }
                    case "switch-gender":{
                        url="/parentMobile/home/update_student_gender.vpage";
                        params.gender=data;
                        break;
                    }
                    case "switch-age":{
                        url="/parentMobile/home/update_student_birthday.vpage";
                        params.birthday=data;
                        break;
                    }

                }


                if(url){
                    $.post(url, params, function(res){
                        if(res.success){
                            $.tip("保存成功!");
                            cal();
                        }else{
                            $.alert(res.info);
                        }
                    });
                }
            };
            //选择回调
            task.selectDataWidgeCallback=function(data){
                try{
                    data=JSON.parse(data);
                    switch(operate){
                        //选择性别
                        case "switch-gender":{
                            if(clickDOM&&clickDOM[0]){
                                if(data.name){
                                    updateData(data.value,function(){
                                        clickDOM.find("span").text(data.name);
                                    });
                                }
                            }
                            break;
                        }
                        //选择年级
                        case "switch-grade":{
                            if(clickDOM&&clickDOM[0]){
                                if(data.name){
                                    updateData(data.value,function(){
                                        clickDOM.find("span").text(data.name);
                                    });
                                }
                            }
                            break;
                        }

                    }
                }catch(e){
                    alert("error!");
                }
            }
            //选择学校回调
            task.selectSchoolCallback=function(data){
                try{
                    data=JSON.parse(data);
                    if(clickDOM&&clickDOM[0]){
                        var schoolName= $.trim(data.schoolName);
                        if(schoolName){
                            updateData(data,function(){
                                clickDOM.find("span").text(schoolName);
                            });
                        }
                    }
                }catch(e){
                    alert("error");
                }
            };

            //选择时间回调
            task.datetimeCallback=function(data){
                //日期格式话
                Date.prototype.Format = function (fmt) {
                    var o = {
                        "M+": this.getMonth() + 1,
                        "d+": this.getDate(),
                        "h+": this.getHours(),
                        "m+": this.getMinutes(),
                        "s+": this.getSeconds(),
                        "q+": Math.floor((this.getMonth() + 3) / 3),
                        "S": this.getMilliseconds()
                    };

                    if (/(y+)/.test(fmt)){
                        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
                    }
                    for (var k in o){
                        if (new RegExp("(" + k + ")").test(fmt)){
                            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1)
                                    ?
                                    (o[k])
                                    :
                                    (("00" + o[k]).substr(("" + o[k]).length))
                            );
                        }
                    }
                    return fmt;
                };


                function GetAgeByBrithday(birthday){
                    var age=-1,
                        today=new Date(),
                        todayYear=today.getFullYear(),
                        todayMonth=today.getMonth(),
                        todayDay=today.getDate(),
                        birthdayYear=birthday.getFullYear(),
                        birthdayMonth=birthday.getMonth(),
                        birthdayDay=birthday.getDate();
                    if(todayMonth*1<birthdayMonth*1){
                        age = (todayYear*1-birthdayYear*1)-1;
                    }else {
                        if(todayMonth*1===birthdayMonth*1){
                            if(todayDay*1<birthdayDay*1){
                                age = (todayYear*1-birthdayYear*1)-1;
                            }else{
                                age = (todayYear*1-birthdayYear*1);
                            }
                        }else{
                            age = (todayYear * 1 - birthdayYear * 1);
                        }
                    }
                    return age*1;
                }


                try{
                    var birthday_time=+data,
                        format_brithday=new Date(birthday_time).Format("yyyy-MM-dd"),
                        format_age=GetAgeByBrithday(new Date(birthday_time));


                    if(clickDOM&&clickDOM[0]){
                        updateData(format_brithday,function(){
                            clickDOM.find("span").text(format_age+"岁");
                        });
                    }
                }catch(e){
                    alert("error");
                }
            };
        })();

        //我的订单列表
        (function(){
            if($(".do_orderlist_page").length>0){
                var result = {},
                    do_inner_jump = function(obj){
                        PM.doExternal('innerJump', JSON.stringify(obj));
                    };

                    try{
                        result = JSON.parse(window.external.getAppIsLogin());
                    }catch(e){

                    }

                if (result.type === "type_no_login") {
                    do_inner_jump({
                        "name": "go_login",
                        "func_model": "grow",
                        "func_model_detail_type": "child_addchild",
                        "loginSource":4
                    });
                } else if (result.type === "type_login_and_no_child") {
                    do_inner_jump({
                        "name": "add_child",
                        "func_model": "grow",
                        "func_model_detail_type": "child_addchild",
                        "loginSource":2
                    });
                } else {
                }
            }
        })();
    });
});
