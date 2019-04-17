/* global $:true */

"use strict";

(function($) {

    var date_unit_format = function(time_unit, unit) {

        var total = 0,
            time_format = {
                y : function(){
                    total += 0;
                },
                M : function(){
                    total += (total + time_unit.y) * 12;
                },
                d : function(){
                    total += (total + time_unit.M) * 30.44;
                },
                h : function(){
                    total += (total + time_unit.d) * 24;
                },
                m : function(){
                    total += (total + time_unit.h) * 60;
                },
                s : function(){
                    total += (total + time_unit.m) * 60;
                }
            },
            time_format_key = Object.keys(time_format);

        time_format_key.some(function(key, index){

            time_format[key]();

            index > 0 && (time_unit[ time_format_key[index - 1] ] = 0);

            if(key === unit){
                time_unit[key] += parseInt(total);
                return true;
            }

        });

    };

    /*
     *http://www.epochconverter.com/
     *Human readable time 	Seconds
     *1 hour	3600 seconds
     *1 day	86400 seconds
     *1 week	604800 seconds
     *1 month (30.44 days) 	2629743 seconds
     *1 year (365.24 days) 	 31556926 seconds
     */
    var date_diff = function(ms, max_unit) {

        ms < 0 && (ms = -1 * ms);

        var floor = Math.floor,
            seconds = ms / 1000,
            hour_s = 3600,
            day_s = 86400,
            year_s = 31556926,

            rest_day_s = seconds % year_s,
            rest_hour_s = rest_day_s % day_s,
            rest_min_s = rest_hour_s % hour_s,

            format_days = floor(rest_day_s / day_s),
            format_month = floor(format_days / 30.44);

        var normal_result = {
            y : floor(seconds / year_s),
            M : format_month,
            d : floor(format_days - (format_month * 30)),
            h : floor(rest_hour_s / 3600),
            m : floor(rest_min_s / 60),
            s : rest_min_s % 60
        };

        max_unit && date_unit_format(normal_result, max_unit);

        return  normal_result;

    };

    var format_date = function(format, date_data, no_strict_date){

        var date_reg_key;

        if( $.isFunction(date_data.getTime) ){

            date_reg_key = {
                "y+" : date_data.getFullYear(),
                "M+" : date_data.getMonth()+1,
                "d+" : date_data.getDate(),
                "h+" : date_data.getHours(),
                "m+" : date_data.getMinutes(),
                "s+" : date_data.getSeconds(),
                "S" :  date_data.getMilliseconds()
            };

        }else if($.isPlainObject(date_data)){
            date_reg_key = date_data;
        }

        if(!date_data){
            throw new Error('请输入日期');
        }


        for(var key in date_reg_key){

            if( date_reg_key.hasOwnProperty(key) ){

                if(new RegExp("("+ key +")").test(format)){

                    var match_key = RegExp.$1,
                        match_value = date_reg_key[key].toString(),
                        sub_length = key === 'y+' ? 6 : (match_value.length > 1 ? 4 : 3);


                    format = format.replace(

                        match_key,

                        (match_key.length === 1 || no_strict_date) ?

                            match_value :

                            ( "00"+ match_value).substr( sub_length - match_key.length)

                    );
                }

            }

        }

        return format;
    };

    $.extend(
        window.PM,
        {
            dateDiff : date_diff,
            formatDate : format_date
        }
    );

})($);
