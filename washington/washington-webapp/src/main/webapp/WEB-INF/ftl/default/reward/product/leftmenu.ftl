
    <div class="t-colum-list list-1">
        <#--<h2>奖品分类</h2>-->
        <ul style="min-height: 293px;" id="oneLevelTagsBox">
            <#--<li data-one_id="0"><a href="javascript:void(0);" class="all_sort"><i class="J_sprites"></i><span>全部分类</span></a></li>-->
            <#--<#if categories?has_content>-->
                <#--<#list categories as categories>-->
                    <#--<li data-one_id="${categories.id!''}"><a href="javascript:void(0);">${categories.name!''}</a></li>-->
                    <#--&lt;#&ndash;<li data-one_id="${categories.id!''}"><a href="javascript:void(0);">${categories.categoryName!''}</a></li>&ndash;&gt;-->
                <#--</#list>-->
            <#--</#if>-->
        </ul>
        <#-- 下线抽大奖入口 -->
        <#--<#if userType == 'STUDENT'>-->
            <#--<div class="luck-draw-join active"><a href="/campaign/studentlottery.vpage">幸运抽大奖</a></div>-->
        <#--</#if>-->
    </div>
    <script type="text/javascript">
        $(function(){
            $.ajax({
                type:'GET',
                url:'/reward/product/new/category.vpage',
                data:{},
                success:function (res) {
                    if(res){
                        var $vm=this
                        var categories=res.categoryList;
                        if(categories!=undefined){
                            var html=""
                            for(var i=0;i<categories.length;i++){
                                html+="<li class='titleLable'><a class='all-sort'><i></i><span></span> </a></li>"
                            }
                            $('#oneLevelTagsBox').append(html);
                            var liArr=$('.all-sort').length;
                            for(var j=0;j<liArr;j++){
                               $('#oneLevelTagsBox').find('span').eq(j).html(categories[j].name);
                               $('#oneLevelTagsBox').find('li').eq(j).attr('data-one_id',categories[j].id);
                               $('#oneLevelTagsBox').find('li').eq(j).attr('data-type',categories[j].type);
                            };
                            $('#oneLevelTagsBox').find('li').eq(0).addClass('active');
                            var roleTypes;
                            <#if userType == 'STUDENT'>
                                roleTypes = "web_student_logs";
                            <#elseif userType == 'TEACHER'>
                                roleTypes = "web_teacher_logs";
                            </#if>
                            var detail = new $17.Model({
                                oneLevelTagsBox : $("#oneLevelTagsBox li"),
                                smallLevelTagsBox : $("#smallLevelTagsBox li"),
                                twoLevelTagsBox : $("#twoLevelTagsBox a"),
                                threeLevelTagsBox : $("#threeLevelTagsBox li.sequence"),
                                canExchangeFlagBut : $("#canExchangeFlagBut .ck"),//兑换
                                reload_ftl_but : $("#reload_ftl_but"),
                            });
                            var filterFlag = {
                                canExchangeFlag : false,
                                teacherLevelFlag : false,
                                nextLevelFlag : false,
                                ambassadorLevelFlag : false,
                                showAffordable:false
                            };
                            var upDownFlag = "down";
                            $(".cla-small-label").hide();
                            detail.extend({
                                tagsAddClass : function($this,className, $type){
                                    if($type && $type == "leftMenu"){
                                        $this.parents("#oneLevelTagsBox").find("li").removeClass(className);
                                        $this.addClass(className);
                                    }else{
                                        if($this.hasClass(className)){
                                            if(upDownFlag == "down"){
                                                upDownFlag = "up";
                                                $this.find(".w-arrow").addClass("w-arrow-orange-up").removeClass("w-arrow-orange-down");
                                            }else{
                                                upDownFlag = "down";
                                                $this.find(".w-arrow").addClass("w-arrow-orange-down").removeClass("w-arrow-orange-up");
                                            }
                                        }else{
                                            if($this.hasClass("js-desc")){
                                                upDownFlag = "up";
                                                $this.find(".w-arrow").addClass("w-arrow-orange-up").removeClass("w-arrow-orange-down");
                                            }else{
                                                upDownFlag = "down";
                                                $this.find(".w-arrow").addClass("w-arrow-orange-down").removeClass("w-arrow-orange-up");
                                            }
                                        }
                                        $this.addClass(className).siblings().removeClass(className);
                                    }
                                },
                                getProductDetail : function(pageNum){
                                    var $this = $(this);
                                    var categoryId= $("#oneLevelTagsBox .active").data('one_id');
                                    var categoryType = $("#oneLevelTagsBox .active").data('type');
                                    var data = {
                                        categoryId :categoryId,
                                        categoryType:categoryType,
                                        oneLevelFilterId:$('#smallLevelTagsBox li.active').data('small_id'),
                                        pageNum : pageNum,
                                        pageSize : 16,
                                        orderBy : $("#threeLevelTagsBox .active").data('three_id'),
                                        upDown : upDownFlag,
                                        showAffordable:filterFlag.canExchangeFlag,
                                    };
                                        $.ajax({
                                            url:'/reward/product/new/productList.vpage',
                                            type:'GET',
                                            data:data,
                                            success:function(res){
                                                if( res.publicGoodTip && res.publicGoodTip!==''){
                                                    $("#warmPromtText p").html('温馨提示：'+res.publicGoodTip+'。');
                                                } else {
                                                    $("#warmPromtText p").html('');
                                                }
                                                if(res.filter==undefined||res.filter.length==0||categoryId==0){
                                                    $(".cla-small-label").hide()
                                                }else if(res.filter.length){
                                                    $(".cla-small-label").show();
                                                    var stagsList=res.filter;

                                                    var html="";
                                                    for(var i=0;i<stagsList.length;i++){
                                                        html+="<li class='mun'></li>"
                                                    }
                                                    if($('#smallLevelTagsBox li').length!=stagsList.length){
                                                    $('#smallLevelTagsBox').append(html);
                                                    for(var j=0;j<stagsList.length;j++){
                                                        $('#smallLevelTagsBox').find('li').eq(j).html(stagsList[j].name);
                                                        $('#smallLevelTagsBox').find('li').eq(j).attr('data-small_id',stagsList[j].id);
                                                    }
                                                        $('#smallLevelTagsBox').find('li').eq(0).addClass('active')

                                                    }
                                                }
                                                var box = $("#product_detail_list_box");
                                                var pageBox = $(".message_page_list");
                                                box.html('<div style="padding: 50px 0; text-align: center; font-size: 14px;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                                                box.html(template("t:商品详情", {
                                                    detailData : res.rows,
                                                    tagType : '${tagType!''}'
                                                }));

                                                //分页
                                                pageBox.page({
                                                    total           : res.totalPage,
                                                    current         : res.pageNum+1,
                                                    autoBackToTop   : false,
                                                    jumpCallBack    : function(index){
                                                        detail.getProductDetail(index-1)
                                                    }
                                                }).show()},
                                        });
                                    },
                                init : function(){
                                    var $this = this;
                                    $this.getProductDetail();
                                    /*一级标签 （分类）*/
                                    $this.oneLevelTagsBox.on('click', function(){
                                        var $that = $(this);
                                        if($that.hasClass("active")){
                                            return false;
                                        }
                                        $this.tagsAddClass($that,'active', "leftMenu");
                                        $('#smallLevelTagsBox li').remove()
                                        $this.getProductDetail();
                                        YQ.voxLogs({ database:roleTypes,module : "m_2ekTvaNe", op : "o_NVtpQ4J8", s0: $that.attr('data-one_id'), s1: "${(currentUser.userType)!0}"});
                                    });
                                    /*二级标签 （专享）*/
                                    $this.twoLevelTagsBox.on('click', function(){
                                        var $that = $(this);
                                        $this.tagsAddClass($that,'active');
                                        $this.getProductDetail();
                                    });

                                    /*三级标签 （排序）*/
                                    $this.threeLevelTagsBox.on('click', function(){
                                        var $that = $(this);
                                        if($that.attr("data-three_id") == "" && $that.hasClass('active')||$that.hasClass('dis')){
                                            return false;
                                        }
                                        $this.tagsAddClass($that,'active');
                                        $this.getProductDetail();
                                        YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "o_Nk7Wuc18", s0: $that.attr("data-three_id"), s1: "${(currentUser.userType)!0}"});
                                    });

                                    //我能兑换的奖品
                                    $this.canExchangeFlagBut.on('click', function(){
                                            var $that = $(this);
                                            var $thatDataType = $that.attr("data-type");
                                            $that.toggleClass('active');
                                            if($thatDataType == "canExchangeFlag"){
                                                if($that.hasClass('active')){
                                                    filterFlag.canExchangeFlag = true;
                                                    // if($("#smallLevelTagsBox li.active").html()=='公益'){
                                                    //     $("#warmPromtText").show()
                                                    // }else{
                                                    //     $("#warmPromtText").hide()
                                                    // }
                                                }else{
                                                    filterFlag.canExchangeFlag = false;
                                                }
                                            }
                                            if($thatDataType == "teacherLevelFlag"){
                                                if($that.hasClass('active')){
                                                    filterFlag.teacherLevelFlag = true;
                                                }else{
                                                    filterFlag.teacherLevelFlag = false;
                                                }
                                            }
                                            if($thatDataType == "ambassadorLevelFlag"){
                                                if($that.hasClass('active')){
                                                    filterFlag.ambassadorLevelFlag = true;
                                                }else{
                                                    filterFlag.ambassadorLevelFlag = false;
                                                }
                                            }
                                            if($thatDataType == "nextLevelFlag"){
                                                if($that.hasClass('active')){
                                                    filterFlag.nextLevelFlag = true;
                                                }else{
                                                    filterFlag.nextLevelFlag = false;
                                                }
                                            }
                                            $this.getProductDetail();
                                            YQ.voxLogs({ database:roleTypes,module : "m_2ekTvaNe", op : "o_Io7ZkVWt", s1: "${(currentUser.userType)!0}"});
                                    });

                                    //重新加载页面
                                    $this.reload_ftl_but.live('click', function(){
                                        detail.getProductDetail();
                                    });
                                    /*二级标签 （分类集合）*/
                                    $('#smallLevelTagsBox').on('click','li', function(){
                                        var $that = $(this);
                                        if($that.attr("data-small_id") == "" && $that.hasClass('active')){
                                            return false;
                                        }
                                        // $('#smallLevelTagsBox').attr('data-active_small_id', $that.data('small_id')); // 通过父级记录激活的二级标签
                                        $this.tagsAddClass($that,'active');
                                        $this.getProductDetail();
                                        YQ.voxLogs({database:roleTypes, module : "m_2ekTvaNe", op : "o_Nk7Wuc18", s0: $that.attr("data-small_id"), s1: "${(currentUser.userType)!0}"});
                                    });
                                }
                            }).init();
                        }
                    }
                 }
                })
        });
    </script>


