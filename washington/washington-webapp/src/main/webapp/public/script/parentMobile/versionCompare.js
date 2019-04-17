/**
 *  @date 2015/12/16
 *  @auto liluwei
 *  @description 该模块主要负责字符串之间的比较
 */

/**
 * 不要想当然的 直接把字符串用来比较大小
 * '111' < '12' ===> true  你怎么看？
 * 字符串比较大小 简而言之就是 一个一个比较。 只要有一个不匹配理解break
 * '111' < '12' ==> '1' <-> '1'   '1' <-> '2'  ==> 1 <2 true ok 后面不比较了 直接返回true吧 ----'1.3.5' '1.11.5'
 */

(function(){
    var win = window;

    var convert_number_version = function(str){
            return str.replace(/\D/g, '');
        },
        auto_fill_zero = function(str, length){
            str = String(str);

            return str.length < length ? (str + "0000000000000000000").substr(0, length) : str;

            /*
            if(str.length < length){
                var diff = length - str.length;

                if(String.prototype.repeat){
                    return str + "0".repeat(diff);
                }

                //return str + (new Array(diff).join(0));

                var index = 0;

                while(index < diff){
                    str += "0";
                    index++;
                }

            }

            return str;
            */
        };

    /*
     * @return 1 : versiona > versionb  0 : versiona = versionb -1: versiona < versionb
     */

    function versionCompare(versiona, versionb){
        // var converted_versiona = convert_number_version(versiona),
        //     converted_versionb = convert_number_version(versionb),
        //     max_length = Math.max(converted_versionb.length, converted_versiona.length),
        //     fill_zero_versiona = auto_fill_zero(converted_versiona),
        //     fill_zero_versionb = auto_fill_zero(converted_versionb);
        var diff;
        for (var i = 0;i <= 3; i++){
            if (Number(versiona.split(".")[i]) > Number(versionb.split(".")[i])){
                diff = 1;
                return diff;
            }else if(Number(versiona.split(".")[i]) < Number(versionb.split(".")[i])){
                diff = -1;
                return diff;
            }
        }
        diff = 0;
        return diff;
    }


    var getVersionCompare = function(){
        return versionCompare;
    };

    if (typeof define === 'function' && define.amd) {
        // AMD
        define([], getVersionCompare);
    } else if (typeof exports === 'object') {
        // CMD, CommonJS之类的
        module.exports = getVersionCompare();
    }else{
        win.io = getVersionCompare();
    }

})();
