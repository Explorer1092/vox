<#import "layout.ftl" as temp >
<@temp.page>
    <div style="position: fixed; width: 100%; height: 100%; top: 0; left: 0; z-index: 1000; background-color: #fff;">
        <div class="loading_error">
            <div class="box">
                <div class="prom-3">
                    <div class="tit">登录信息已失效，请重新登录~</div>
                </div>
                <div onclick="redirectLogin();" class="btn">重新登录</div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        function redirectLogin(){
            if(window.external && ('redirectLogin' in window.external)){
                window.external.redirectLogin("");
            }else{
                $M.appLog('reward',{
                    app : "17homework_my",
                    type : "log_normal",
                    module : "user",
                    operation : "page_login_invalid_error"
                });
            }
            $M.appLog('reward',{
                app : "17homework_my",
                type : "log_normal",
                module : "user",
                operation : "page_login_invalid_click"
            });
        }
        $(function(){
            //log
            $M.appLog('reward',{
                app : "17homework_my",
                type : "log_normal",
                module : "user",
                operation : "page_login_invalid"
            });
        });
    </script>
</@temp.page>