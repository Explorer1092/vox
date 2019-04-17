<script type="text/html" id="T:VUE_LOADING">
    <div>
        <div style="height: 200px; background-color: white; width: 98%;"><img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" /></div>
    </div>
</script>

<script type="text/javascript">
    (function(){
        "use strict";
        var vueLoading = {
            template : template("T:VUE_LOADING",{}),
            data : function(){
                return {};
            },
            props : {
                imgUrl : {
                    type : String,
                    default : "<@app.link href='public/skin/teacherv3/images/loading.gif' />"
                }
            },
            created : function(){}
        };

        $17.extend($17, {
            vueLoading : vueLoading
        });
    }());
</script>