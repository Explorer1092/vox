<#list qq as qq>
    <div class="tb-box" id="bindQQ_${qq.id!''}">
        <div class="t-center-list">
            <div class="tf-left w-fl-left">
                <span class="w-detail w-right"></span>
            </div>
            <div class="tf-center w-fl-left">
                <p class="w-green">QQ 绑定：已绑定 ${qq.sourceUserName!''}</p>
                <p>绑定QQ后，可以使用“QQ登录”功能登录一起作业网</p>
            </div>
            <div class="tf-right w-fl-left">
                <a data-id="${qq.id!''}" href="javascript:void(0);" class="w-btn-dic w-btn-gray-new relieveBut">解除绑定</a>
            </div>
            <div class="w-clear"></div>
        </div>
    </div>
</#list>

<script type="text/javascript">
    $(function(){
        //解除绑定
        $(".relieveBut").on('click',function(){
            var id = $(this).data('id');
            $.prompt('<div style="text-align: center;"><h3>确定解绑？</h3> <br /><span>解绑后，将不能使用此QQ一键登录一起作业</span></div>',{
                title : '系统提示',
                buttons : {'解除绑定':true, "不解除": false},
                submit : function(e,v){
                    e.preventDefault();
                    if(v){
                        $.post('/student/center/unbindsso.vpage',{id : id},function(data){
                            if(data.success){
                                $('#bindQQ_'+id).remove();
                                $.prompt.close();
                            }else{
                                $17.alert('解除绑定失败！');
                            }
                        });
                    }else{
                        $.prompt.close();
                    }
                }
            });
        });
    });
</script>