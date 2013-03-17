

$("#selectAll").click( function() {
	$("#subFields button.radio").addClass("active");
    getPage(1);
});

$("#selectNone").click( function() {
	$("#subFields button.radio").removeClass("active");
	getPage(1);
});


$("#showSelectFields").click(function() {
	$("#subFields").toggle();
});

$("button.radio").click(function() {
	$(this).toggleClass('active');
	getPage(1);
	$(this).toggleClass('active');
});

$("#avgSort").click( function() {
	changeSort("avg");
});

$("#extSort").click( function() {
	changeSort("ext");
});

$("#userSort").click( function() {
	changeSort("user");
});

function changeSort(name) {
	if($("#orderBy").attr("value") == name) {
		if($("#sort").attr("value") == "desc") {
			$("#sort").attr("value", "asc");
			$("#"+name+"Sort h6 i").attr("class", "icon-circle-arrow-up");
		} else {
			$("#sort").attr("value", "desc");
			$("#"+name+"Sort h6 i").attr("class", "icon-circle-arrow-down");
		}
	} else {
		$("#extSort h6 i").attr("class", "");
		$("#avgSort h6 i").attr("class", "");
		$("#userSort h6 i").attr("class", "");
		
		if($("#sort").attr("value") == "desc") {
			$("#"+name+"Sort h6 i").attr("class", "icon-circle-arrow-down");
		} else {
			$("#"+name+"Sort h6 i").attr("class", "icon-circle-arrow-up");
		}
		$("#orderBy").attr("value", name);
	}
	
	getPage($("#pageNum").attr("value"));
}

// buildPagination();

function getPage(pageNum) {
	var field = $("#fieldId").attr("value");
	var national = ($("#national").hasClass('active') ? "1" : "0");
	var international = ($("#international").hasClass('active') ? "1" : "0");
	var conference = ($("#conference").hasClass('active') ? "1" : "0");
	var journal = ($("#journal").hasClass('active') ? "1" : "0");
	var workshop = ($("#workshop").hasClass('active') ? "1" : "0");
	var orderBy = $("#orderBy").attr("value");
	var sort = $("#sort").attr("value");
	var nameFilter = $("#nameFilter").attr("value");


    // var subFields = $("#subFields button.active").map(function() {
	var subfields = $("#subFields button.active").map(function() {
		return $(this).attr("id");
	}).get().join(',');

    console.log(subfields);


	$.ajax({
		url: '/ranking/page',
		type: 'GET',
		data: { "pageNum": pageNum, "field": field, 
			"nat": national, "intl": international, 
			"conference": conference, "journal": journal, "workshop": workshop,
			"subFields": subfields,
			"orderBy": orderBy, 
			"sort": sort,
			"nameFilter": nameFilter},
	    success: function(data) {
	    	$("#conferenceList").html(data);
	    	$("#nameFilter").focus();

	    	afterDelayedKeyup('input#nameFilter','getPage($("#pageNum").attr("value"));',500);
	    },
	    error: function(data) {
	    	message.addError(data.responseText)
	    }
    });
}

function afterDelayedKeyup(selector, action, delay){
  jQuery(selector).keyup(function(){
    if(typeof(window['inputTimeout']) != "undefined"){
      clearTimeout(inputTimeout);
    }  
    inputTimeout = setTimeout(action, delay);
  });
}

afterDelayedKeyup('input#nameFilter','getPage($("#pageNum").attr("value"));',500);

/*


var pageSize = 10;
var pageToDisplay = 0;

var pageNum = 1;
var curElementNumber = 0;
var numInPage = 0;

function findElement(idx) {
	curElementNumber += 1;
	numInPage += 1;
	if(pageNum != pageToDisplay) {
		$(this).hide();
	} else {
		$(this).show();
	}
	
	if(numInPage == pageSize) {
		numInPage = 0;
		pageNum += 1;
	}
}


function buildPagination() {
	var nbPages = Math.floor(($("div.confRank").length + pageSize - 1) / pageSize);
	
	// Compute the number of page 
	
	var firstClass = (pageToDisplay == 0 ? "disabled" : "");
	var lastClass = (pageToDisplay + 1 == nbPages ? "disabled" : "");

	var firstPage = $("<li class='" + firstClass + "'><a href='#'>&laquo</a></li>");
	var prevPage = $("<li class='" + firstClass + "'><a href='#'>Prev</a></li>");
	var nextPage = $("<li class='" + lastClass + "'><a href='#'>Next</a></li>");
	var lastPage = $("<li class='" + lastClass + "'><a href='#'>&raquo</a></li>");
	
	firstPage.click(function() { setPage(0); });
	prevPage.click( function() { setPage(Math.max(pageToDisplay - 1, 0)); });
	nextPage.click( function() { setPage(Math.min(pageToDisplay + 1, nbPages - 1)); });
	lastPage.click( function() { setPage(nbPages - 1); });
	
	$("ul#pagination li").remove();
	$("ul#pagination").append(firstPage);
	$("ul#pagination").append(prevPage);
	var i = 0;
	for (i = pageToDisplay - 10; i < pageToDisplay + 10; i++) {
		if(i >= 0 && i < nbPages) {
			var pageClass = (pageToDisplay == i ? "active" : "");
			var page = $("<li class='" + pageClass + "'><a href='#'>" + (i + 1) + "</a></li>");
			page.click(function() { setPage(i); });
			$("ul#pagination").append(page);
		}
	}
	// $("ul#pagination").append("<li><a href='#'>&rsaquo;</a></li>");
	$("ul#pagination").append(nextPage);
	$("ul#pagination").append(lastPage);
}

function setPage(newPageNum) {
	var lala = newPageNum;
	pageToDisplay = lala;
	init();
	buildPagination();
}

function init() {
	curElementNumber = 0;
	numInPage = 0;
	pageNum = 0;
	$("div.confRank").each(findElement);
}

init();
*/