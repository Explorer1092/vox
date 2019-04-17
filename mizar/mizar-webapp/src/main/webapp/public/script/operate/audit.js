/*-----------课程管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform","template"],function($){

    /*查询*/
    $("#js-filter").on("click",function(){
        $("#filter-form").submit();
    });

    /*分页插件*/
    var paginator = $('#paginator');
    var pages = $(".one-page");
    var currentPage = 1;
    if(paginator.length>0){
        paginator.jqPaginator({
            totalPages: pages.length,
            visiblePages: 10,
            currentPage: 1,
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (num) {
                pages.eq(num-1).addClass("displayed").siblings().removeClass("displayed");
                currentPage = num;
            }
        });
    }

    /*-------审核相关--------*/
    $(".approve-btn").on("click",function(){
        var rid = $('#rid').val();
        $.prompt("<div style='text-align:center;'>确认通过该条申请？</div>", {
            title: "审核通过",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                   $.post('approve.vpage', {rid:rid},function (res) {
                      if(res.success) {
                          $.prompt("<div style='text-align:center;'>审核通过！</div>", {
                              title: "操作提示",
                              buttons: { "确定": true },
                              focus : 1,
                              submit: function(e,v) {
                                    if (v) {
                                        location.href='/operate/audit/index.vpage';
                                    }
                               },
                              useiframe:true
                          });
                      } else {
                          $.prompt("<div style='text-align:center;'>"+(res.info||"审核失败！")+"</div>", {
                              title: "错误提示",
                              buttons: { "确定": true },
                              focus : 1,
                              useiframe:true
                          });
                      }
                   });
                }
            },
            useiframe:true
        });
    });

    $(".reject-btn").on("click",function(){
        var rid = $('#rid').val();
        $.prompt("<div style='text-align:center;'>请填写驳回原因<br /><textarea class='audit-note' rows='4'></textarea></div>", {
            title: "审核通过",
            buttons: { "取消": false, "确定": true },
            submit: function( e,v ){
                if ( v ) {
                    var note = $('.audit-note').val();
                    $.post('reject.vpage', {rid:rid, note:note},function (res) {
                        if(res.success) {
                            $.prompt("<div style='text-align:center;'>驳回成功！</div>", {
                                title: "操作提示",
                                buttons: { "确定": true },
                                focus : 1,
                                submit: function(e,v) {
                                    if (v) {
                                        location.href='/operate/audit/index.vpage';
                                    }
                                },
                                useiframe:true
                            });
                        } else {
                            $.prompt("<div style='text-align:center;'>"+(res.info||"驳回失败！")+"</div>", {
                                title: "错误提示",
                                buttons: { "确定": true },
                                focus : 1,
                                useiframe:true
                            });
                        }
                    });
                }
            },
            useiframe:true
        });
    });

    //查看详情历史
    $('.detailHistoryBtn').on('click',function(){
        var detail = $(this).data('detail').split(',');
        var imgHtml = '';
        for(var i = 0; i < detail.length; i++){
            imgHtml+='<span style="padding: 0 10px;"><img width="170" height="127" src="'+detail[i]+'"></span>';
        }
        $.prompt(imgHtml, {
            title: "预览",
            buttons: { "确定": true },
            focus : 1,
            position: {width: 610},
            useiframe:true
        });
        console.info(detail);
    });

    //师资力量查看历史
    $('#facultyBoxBeforeBtn').on('click', function () {
        var imgHtml = $('#facultyBoxBefore').html();
        $.prompt(imgHtml, {
            title: "预览",
            buttons: {"确定": true},
            focus: 1,
            position: {width: 610},
            useiframe: true
        });
    });

    $(document).on('click', '.op-pending,.op-reject,.op-approve', function(){
        location.href=$(this).data().link;
    });

    // $(document).on('click', '.op-approve', function(){
    //     location.href='/operate/goods/detail.vpage?gid='+$(this).data().rid;
    // });

    //

    /*********地图相关*******/
    if (typeof AMap != "undefined") {
        //首先判断对象是否存在
        var longitude = $('#longitude').val();
        var latitude = $('#latitude').val();
        var map = new AMap.Map('innerMap', {
            resizeEnable:true,
            zoom: 12
        });

        var marker = '';
        if(longitude != '' && latitude != ''){
            map.setCenter([longitude,latitude]);
            marker = new AMap.Marker({map: map, position: [longitude,latitude], animation: 'AMAP_ANIMATION_DROP'});
        }

        map.on("click", function (e) {
            //预览页面禁止重新标记
            var disable = $('#innerMap').data('disable') || false;
            if (!disable) {
            } else {
                return false;
            }

            $('#longitude').val(e.lnglat.getLng());
            $('#latitude').val(e.lnglat.getLat());
            $('#baiduGps').attr("checked", true);

            if(marker != ''){
                marker.setMap(null);//清空已有的标记
            }
            marker = new AMap.Marker({map: map, position: e.lnglat, animation: 'AMAP_ANIMATION_DROP'});

            AMap.service('AMap.Geocoder',function(){//回调函数
                var geocoder = new AMap.Geocoder();
                geocoder.getAddress(e.lnglat, function (status, result) {
                    if (status === 'complete' && result.info === 'OK') {
                        $("#address").val(result.regeocode.formattedAddress);
                    }
                });
            })
        });
    }

});