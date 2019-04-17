<style>
    .vox_reduced_state{ clear: both; height: 60px; position: relative; z-index: 10000;}
    .vox_reduced_state .popup{ width: 100%; background-color: #fff2d0; position: fixed; top: 0; left: 0; _position:absolute;  _top:expression(documentElement.scrollTop);}
    .vox_reduced_state .inner{ width: 100%; overflow: hidden; margin: 0 auto; position: relative; color: #98682a; text-align: center; font-size: 16px; padding: 20px 0; white-space: nowrap;}
    .vox_reduced_state .inner .close{ position: absolute; top:5px; right: 10px; cursor: pointer; line-height: 1.125;}
</style>
<div id="20140514_detectzoom" class="vox_reduced_state" style="display: none;">
    <div class=popup>
        <div class="inner text_yahei">
            <i class="vox_custom_icon vox_custom_icon_4"></i> 您的浏览器目前处于<span id="20140514_detectzoom_text"></span>状态，会导致作业显示不正常，您可以键盘按“ctrl+数字0”组合键恢复初始状态。
            <div class="close"><i class="vox_custom_icon vox_custom_icon_3"></i></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var DetectZoom = new $17.Model({
            closeThat : false
        });
        DetectZoom.extend({
            checkThat: function(){
                var status = $17.detectZoom();
                if(status != 100 && !this.closeThat){
                    $("#20140514_detectzoom_text").text(status < 100 ? "缩小" : "放大");
                    $("#20140514_detectzoom").show();
                    if(!$(DetectZoom).isFreezing()){
                        $(DetectZoom).freezing();
                        $17.tongji("网页被缩放");
                    }
                }else{
                    $("#20140514_detectzoom").hide();
                }
            },
            init: function(){
                var $this = this;

                $(document).on("keydown mousemove", function(e){
                    $this.checkThat();
                });

                $(document).on("DOMMouseScroll mousewheel", function(e){
                    if(e.ctrlKey){
                        $this.closeThat = false;
                    }
                    $this.checkThat();
                });

                $("#20140514_detectzoom .close").on("click", function(){
                    $this.closeThat = true;
                    $this.checkThat();
                });
            }
        }).init();
    });
</script>