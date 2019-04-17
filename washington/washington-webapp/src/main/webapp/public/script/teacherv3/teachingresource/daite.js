$(function(){

    //与戴特交互方法
    function callCplus(cmd,msg,tag){
        try{
            if(cef != undefined){
                cef.message.sendMessage(cmd,[msg,tag])
            }
        }catch(e){
            $17.error(e.message);
        }
    }

    $17.daite = $17.daite || {};
    $17.extend($17.daite, {
        callCplus   : callCplus
    });
});