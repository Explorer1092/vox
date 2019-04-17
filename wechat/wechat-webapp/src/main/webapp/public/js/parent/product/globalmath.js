/*
* created by free at 2016/02/25
* */
define(["jquery","$17","logger","jbox"], function ($,$17,logger) {
    var selectedStudentId = $('#child_list_box').data('selected_student');
    var price,currentIds;

    //体验购买
    if(mathType == "trial"){
        $("#cycle_list_box").empty();
        var tabhtml = '<li data-pid="0" class="active"><table cellpadding="0" cellspacing="0"><tr><td class="wtxt">试用版报名</td></tr></table></li>'
        $("#cycle_list_box").html(tabhtml);
        $("#buy_but>a").text("确定");
    }

    //选择难度
    $(document).on('click', '#cycle_list_box li', function () {
        var $this = $(this);
        if(!$this.hasClass("disabled")){
            //清空上次的缓存
            $('#array-product').val("");
            $('.price_box').html("0元");
            $this.addClass('active').siblings().removeClass('active');
            var pid = $(this).data("pid");
            if(pid != 0){
                $('#array-product').val(pid);
                $('.price_box').html(price+"元");
            }
        }
    });

    //选择孩子
    $(document).on('click', "#child_list_box li", function () {
        var $this = $(this);
        $this.addClass('active').siblings().removeClass('active');

        //清空上次的数据
        if(mathType != "trial"){
            $("#cycle_list_box").find("li").removeClass("active").removeClass("disabled");
        }
        $('#array-product').val("");
        $('.price_box').html("0元");

        //产品相关数据
        var product = $this.data('products')[0];
        var buyIds = $this.data('buyids');
        price = product.price;
        currentIds = buyIds;
        $("#array-student").val($this.data("student_id"));  //选择的孩子ID

        if(mathType != "trial"){
            if(currentIds){
                $.each($("#cycle_list_box>li"),function(i,item){
                    $(item).removeClass("active").addClass("disabled");
                });
            }
        }
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
            $17.jqmHintBox("请选择孩子");
            return false;
        }
        else {
            if(mathType != "trial"){
                if($("#cycle_list_box").children('li.active').length == 0) {
                    if($($("#cycle_list_box").children('li')[0]).hasClass("disabled")){
                        $17.jqmHintBox("该学生已经开通，请登录PC参与比赛");
                    }else{
                        $17.jqmHintBox("请选择难度");
                    }
                    return false;
                }else{

					var form = document.forms[0];

					if(window.isFromParent){

						$.get(
							"/parent/ucenter/getBody.vpage",
							{
								method : "post",
								url : location.protocol + "//www." + location.host.replace(/www\.|wechat\./g, "") + '/parentMobile/order/createorder.vpage',
								data : JSON.stringify({
									sid:childId,
									productId : $(form).find('input[name="productId"]').val()
								})
							},
							function(result){

								$17.loadingEnd();

								var errorMsg = result.info || null;

								if(result.success){

									var body = result.body;

									if(body.success){
										window.external.payOrder("" + body.orderId, "GlobalMath");
										return ;
									}

									errorMsg = body.info;
								}

								$17.jqmHintBox(errorMsg);

							}
						);

						return ;
					}

					form.submit();
                }
            }else{
                $17.loadingStart();
                $.post("/parent/product/globalmath-trial.vpage",{sid:childId},function(result){
                    if(result.success){
                        location.href = result.gmcUrl + "?session_key="+result.session_key;
                    }else{
                        $17.loadingEnd();
                        $17.jqmHintBox(result.info);
                    }
                });
            }
        }
    });
});
