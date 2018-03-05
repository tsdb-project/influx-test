$(document).ready(function() {
    var subnav = $("#subnav").attr("name");
    var nav = $("#nav").attr("name");
    if (subnav == "") {
        $('#' + nav).addClass("navigation__active");
    } else {
        $('#' + nav).addClass("navigation__sub--active navigation__sub--toggled");
        $('#' + subnav).addClass("navigation__active");
    }
});