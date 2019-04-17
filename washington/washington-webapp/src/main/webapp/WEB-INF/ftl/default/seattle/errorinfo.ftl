<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page title="系统提示" fastClickFlag=false>
    <style type="text/css">
        html{font-size:18px}
        @media only screen and (min-width:360px){html{font-size:20px!important}
        }
        @media only screen and (min-width:375px){html{font-size:21px!important}
        }
        @media only screen and (min-width:400px){html{font-size:22px!important}
        }
        @media only screen and (min-width:414px){html{font-size:23px!important}
        }
        @media only screen and (min-width:480px){html{font-size:24px!important}
        }
        @media only screen and (min-width:540px){html{font-size:26px!important}
        }
        @media only screen and (min-width:640px){html{font-size:28px!important}
        }
        @media only screen and (min-width:768px){html{font-size:30px!important}
        }
        @media only screen and (min-width:960px){html{font-size:36px!important}
        }
        html{-webkit-overflow-scrolling:touch;overflow-scrolling:touch;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;-webkit-user-select:none;-ms-user-select:none}
        body,button,input,select,textarea{font-family:TrebuchetMS,Rotobo,"Microsoft YaHei",sans-serif}
        .parentApp-error500 {
            position: absolute; top: 15%; left: 0; padding: 12.875rem 0 0; width: 100%; text-align: center; font-size: .9rem;
            line-height: 150%; background: url("<@app.link href="public/skin/parentMobile/images/parentApp-error500.png"/>") no-repeat 50% 0; background-size: 100% auto;
        }
        .parentApp-error500 div { color: #222; }
        .parentApp-error500 a { color: #a1a2a6; display: block;}
    </style>
    <#if (result.info)?has_content>
        <#assign info = result.info errorCode = result.errorCode>
        <div class="parentApp-error500 code_400">
            <div>${info!}</div>
        </div>
    <#else>
        <div class="parentApp-error500 code_400">
            <div>您访问的页面不存在或已过期~~</div>
        </div>
    </#if>

    <script type="text/javascript">
        var signRunScript = function(){
            var infoContent = "${info!}";

            if(infoContent.indexOf('重新登录') > -1 && isFromWeChat()){
                location.href = '//wx.17zuoye.com/login?return_url=' + encodeURI(location.href);
            }

            //获取WeChat
            function isFromWeChat() {
                return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
            }
        }
    </script>
</@layout.page>