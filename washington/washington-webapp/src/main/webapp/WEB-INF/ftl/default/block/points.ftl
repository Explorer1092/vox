<script type="text/javascript">
    /*
    新题库结构：
    "words": [
        {
            "id": "310300004",
            "name": "4-star"
        }
        {
            "id": "310300005",
            "name": "7-up"
        },
        {
            "id": "310300006",
            "name": "a"
        }
    ]*/
    function SearchJson(knowledgeIndex, idValue){
        var strX        = idValue.toString().toLowerCase();
        var x           = strX.substr(0, 1);
        var jsonList    = eval("("+knowledgeIndex+")");
        var item        = "";
        var jsonKey     = "";
        var isHave      = -1;
        var typeArr     = new Array('topicsstr','grammarstr','phoneticstr','characterstr');
        var retArr      = new Array();
        var iCount      = 0;

        for(var i = 0; i < jsonList.length; i++){
            for(var key in jsonList[i][x]){
                var id = jsonList[i][x][key].toString();
                jsonKey = "words";
                isHave = jsonList[i][jsonKey][id]["name"].toString().toLowerCase().indexOf(strX);
                if(Number(isHave) < 0) continue;
                retArr[iCount] = new Array();
                retArr[iCount]['key'] = jsonList[i][jsonKey][id]["name"].toString().replace('’','\''); //为了兼容新题库
                retArr[iCount]['value'] = jsonKey;
                retArr[iCount]['_id'] = jsonList[i][jsonKey][id]["id"];
                iCount++;
            }
            for(var j = 0; j < typeArr.length; j++){
                var jsonKey = typeArr[j];
                for(var key in jsonList[i][jsonKey]){
                    isHave = jsonList[i][jsonKey][key]["name"].toString().toLowerCase().indexOf(strX);
                    if(Number(isHave) < 0) continue;

                    retArr[iCount] = new Array();
                    retArr[iCount]['key'] = jsonList[i][jsonKey][key]["name"].toString().replace('’','\'');
                    retArr[iCount]['value'] = jsonKey;
                    retArr[iCount]['_id'] = jsonList[i][jsonKey][key]["id"];
                    iCount++;
                }
            }
        }

        return (retArr);
    }
</script>