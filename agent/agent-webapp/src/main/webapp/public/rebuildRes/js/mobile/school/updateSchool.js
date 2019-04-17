$(document).ready(function () {
    var setTopBar = {
        show:true,
        rightText:'提交',
        rightTextColor:"ff7d5a",
        needCallBack:true
    } ;
    var rightFn = function () {
        getRealTimeDetail();
        var layerIndex;
        if (layer){
            layerIndex = layer.load(1, {
                shade: [0.1,'#fff'] //0.1透明度的白色背景
            });
        }
        $.ajax({
            type:'POST',
            url:"save_update_school.vpage",
            contentType:'application/json;charset=UTF-8',
            data:JSON.stringify(schoolDetail),
            timeout:60000,
            dataType:'json',
            success:function(res){
                if (layer){
                    layer.close(layerIndex);
                }
                if(res.success){
                    if(!res.appraisalSchool){
                        AT.alert("修改学校信息成功");
                        setTimeout("window.history.back()",1500);
                    }else{
                        $("#repatePane").show();
                    }
                }else{
                    AT.alert(res.info)
                }

            },
            error:function () {
                if (layer){
                    layer.close(layerIndex);
                }
                AT.alert('保存失败')
            }
        });
    }
    var topBarCallBack = function () {
        rightFn();
    };
    setTopBarFn(setTopBar,topBarCallBack)
});
