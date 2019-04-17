/*
趣味数学
*/
define(["jquery","$17","jbox"], function ($,$17) {
    var selectedStudentId = $('#child_list_box').data('selected_student');
    var currentProduct,currentIds;

    //选择难度
    $(document).on('click', '#cycle_list_box li', function () {
        //清空上次的缓存
        $('#array-product').val("");
        $('.price_box').html("0元");

        var $this = $(this);
        $this.addClass('choose').siblings().removeClass('choose');
        var index = $(this).data("index");
        var levelArray = [];
        $.each(currentProduct,function(i,item){
            if(item.additionalProductName == index){
                levelArray.push(item);
            }
        });

        var levelList = '';
        for(var i=0;i<levelArray.length;i++){
            var name = levelArray[i].name.split(" ")[2];
            var price = levelArray[i].price;
            var productId = levelArray[i].productId;
            levelList += "<li class='sub' data-product_id='"+productId+"' data-price='"+price+"'>"+name+"</li>"
        }

        $("ul.js-levelList").html(levelList);

        if(currentIds){
            var currentIdsContent = currentIds.substring(1,currentIds.length-1);

            //置灰判定
            if(currentIdsContent.length != 0){
                var pids = currentIdsContent.split(",");
                for(var j=0;j<pids.length;j++){
                    $('li[data-product_id="'+pids[j]+'"]').addClass("gray");
                }
            }
        }

    });

    //段位选择
    $(document).on('click', '.js-levelList li', function () {
        var $this = $(this);
        if(!$this.hasClass("gray")){
            $this.addClass('con').siblings().removeClass('con');
            var pid = $(this).data("product_id");
            var price = $(this).data("price")+"元";
            $('#array-product').val(pid);
            $('.price_box').html(price);
        }else{
            return false;
        }
    });

    //选择孩子
    $(document).on('click', "#child_list_box li", function () {
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');

        //清空上次的数据
        $("#cycle_list_box").find("li.stemItem").removeClass("choose");
        $("ul.js-levelList").html("");
        $('#array-product').val("");
        $('.price_box').html("0元");

        //产品相关数据
        var product = $this.data('products');
        var buyIds = $this.data('buyids');
        currentProduct = product;
        currentIds = buyIds;
        $("#array-student").val($this.data("student_id"));  //选择的孩子ID
    });

    //默认选中学生
    if ($17.isBlank(selectedStudentId)) {
        $("#child_list_box").find("li:first").click();
    } else {
        $("#child_list_box").find("li[data-child_id=" + selectedStudentId + "]").trigger('click');
    }

    //立即购买
    $("#buy_but").on('click', function () {
        var childId = $("#child_list_box li.active").data("student_id"); //child_id
        if ($17.isBlank(childId)) {
            $17.jqmHintBox("选择孩子");
            return false;
        }else if($('.js-levelList').find('li.con').length == 0){
            $17.jqmHintBox("请选择难度段位");
            return false;
        } else {
            document.forms[0].submit();
        }
    });
});