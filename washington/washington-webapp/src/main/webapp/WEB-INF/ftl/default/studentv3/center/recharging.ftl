<#import "module.ftl" as center>
<@center.studentCenter currentPage='recharging'>
<div class="t-center-box w-fl-right">
    <span class="center-rope"></span>
    <span class="center-rope center-rope-1"></span>
    <div class="t-center-data" style="margin: 40px 10px 0;">
        <div id="moduleType"></div>

        <script type="text/javascript">
            $(function(){
                var moduleType = $("#moduleType");

                switch ($17.getQuery("types")){
                    case "recharging-go" :
                        var vendorOrderId = $17.getQuery("vendorOrderId");
                        moduleLoad("/finance/recharge/recharge.vpage?vendorOrderId="+vendorOrderId);
                        break;
                    case "recharging-result" :
                        var flowId = $17.getQuery("flowId");
                        moduleLoad("/finance/recharge/recharge_result.vpage?flowId="+flowId);
                        break;
                    default :
                        moduleLoad("/finance/recharge/index.vpage");
                }

                function moduleLoad(url, dataname){
                    this.dataname = dataname ? dataname : "recharging";
                    moduleType.load(url, function(){
                        moduleType.height("auto");
                    });
                    if(this.dataname){
                        $(".moduleOpen[data-name='"+ this.dataname +"']").addClass("active").siblings().removeClass("active");
                    }
                }
            });
        </script>
    </div>
</div>
</@center.studentCenter>
