/* global define : true, $ : true */
/**
 *  @date 2015/9/18
 *  @auto liluwei
 *  @description 该模块主要负责将form表单数据 集合到一个对象中
 */

define([], function(){

    'use strict';

    var get_form_record_by_jq = $.noop;

    (function(){
        get_form_record_by_jq = function(form, param) {
            var form_data ={};

            $.each(form._serializeArray(), function(index, value){
                value.name && value.value && (form_data[value.name] = value.value);
            });

            return param ? $.param(form_data) : form_data;
        };

        var serializeArray_form = $('<form>', {
            id : 'serializeArray_form',
            css : {
                display : 'none'
            }
        }).appendTo('body');

        $.fn._serializeArray = function(){

            if($(this).length === 0){
                return $.error('请检查该dom是否存在');
            }

            var form = this.clone(true);

            form = (this[0].tagName.toUpperCase() === 'FORM') ?  form : serializeArray_form.html(form);

            //form.find(':disabled').remove(); // 去除

            var checkbox = {},
                need_add_inputs = [];

            form.find('input:checked').each(function(){
                var self = $(this);

                if( !self.attr('name') || $.trim(self.val()) === '' ){
                    return;
                }

                var checkbox_obj = checkbox[self.attr('name')] = checkbox[self.attr('name')] || [];

                checkbox_obj.push(self.val());

                $(self).remove();

            });

            $.each(checkbox, function(key, value){

                need_add_inputs.push(
                    $('<input>', {
                        name : key,
                        value : value
                    })
                );

            });

            var form_serializeArray = form.append(need_add_inputs).serializeArray();

            form = null;
            serializeArray_form.html('');

            return form_serializeArray;
        };

    })();

    return  get_form_record_by_jq;

});
