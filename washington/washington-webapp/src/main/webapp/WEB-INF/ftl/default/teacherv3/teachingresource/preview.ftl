<#import "layout.ftl" as shell />
<@shell.page show="main">
<@sugar.capsule js=["vue"] css=["plugin.venus-pre","teachingresource.preview"]/>
<div id="basicAppIframe" v-cloak>
    <i v-if="showLogo" class="watermark"></i>
    <iframe class="vox17zuoyeIframe" v-bind:src="url" @load="iframeLoaded" width="100%" marginwidth="0" height="100%" marginheight="0" scrolling="no" frameborder="0"></iframe>
</div>
<script type="text/javascript">
    $(function(){
        var ua = navigator.userAgent.toLowerCase();
        var url = decodeURIComponent($17.getQuery("url"));
        new Vue({
            el : "#basicAppIframe",
            data : {
                url : url,
                type : $17.getQuery("type"),
                showLogo : false,
                ua : ua || ""
            },
            computed: {
                isDateDu : function(){
                    //戴特标识
                    return this.ua.indexOf("datedu") !== -1;
                },
                isDateduTeach: function isDateduTeach() {
                    //戴特大屏
                    return this.ua.indexOf("datedu_teach") !== -1;
                },
                isDateduppt : function(){
                    //戴特备课
                    return this.ua.indexOf("datedu_ppt") !== -1;
                }
            },
            methods : {
                iframeLoaded : function () {
                    $17.info("iframe loaded...");
                    var vm = this;
                    vm.showLogo = (vm.type === 'CLASS_COURSE_WARE');
                },
                closePage : function(type){
                    var vm = this;
                    type = (typeof type === "string" ? type : String(type));
                    if(!vm.isDateDu){
                        window.opener = null;
                        window.open("", "_self");
                        window.close();
                    }else{
                        $17.daite.callCplus('mirco.cotroler', JSON.stringify({'type': type}), 'closeResource');
                    }
                }
            },
            created: function () {
                var vm = this;
                $17.info("preview created....");
            },
            mounted: function () {
                $17.info("preview mounted....");
                var vm = this;
                switch (vm.type){
                    case "BASIC_APP":
                        //监听flash中的关闭按钮回调
                        window.nextHomeWork = function(){
                            window.nextHomeWork = null;
                            vm.closePage();
                        };
                        break;
                    case "INTELLIGENT_TEACHING":
                    case "CHINESECHARACTERCULTURE":
                    case "IMAGETEXTRHYME":
                        window.addEventListener("message",function(e){
                            var obj = e.data || {};
                            //过滤掉devTools中Vue发的消息
                            if(obj.hasOwnProperty("devtoolsEnabled")){
                                return false;
                            }
                            vm.closePage();
                        },false);
                        break;
                    case "NATURAL_SPELLING":
                    case "ALL_NATURAL_SPELLING":
                        var iframe = $("iframe.vox17zuoyeIframe")[0];
                        iframe.contentWindow.addEventListener("closePreviewPopup",function(){
                            vm.closePage();
                        });
                        break;
                    default:
                        break;
                }
            }
        });
    });
</script>
<@sugar.capsule js=["teachingresource.daiteutil"]/>
</@shell.page>