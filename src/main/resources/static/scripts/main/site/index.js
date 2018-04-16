$(function () {
    $('#zh-load-more').click(function () {

        var agoData = $('#js-home-feed-list');

        var title = $('.avatar');

        alert(title.html());

        var Data = agoData.html();

        var newData = '';

        $.ajax({
            type: "post",
            dataType: "json",
            url: "/getquestions?offset=10",
            success: function (msg) {

                var i = msg['code'];

                if (i == 1) {

                    var str = msg['questions'];

                   // agoData.html(Data);
                    var x;
                    for(x in str){
                        alert(str[x]['followCount']);
                    }



                    //alert(str);

                   // title.html(1231);

                    //alert('asd');

                }
            },
            error: function () {
                alert("查询失败")
            }
        });
    });
});