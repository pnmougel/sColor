;
(function($) {
	/** **************************************************************************************** */
	// jquery.pajinate.js - version 0.4
	// A jQuery plugin for paginating through any number of DOM elements
	// 
	// Copyright (c) 2010, Wes Nolte (http://wesnolte.com)
	// Licensed under the MIT License (MIT-LICENSE.txt)
	// http://www.opensource.org/licenses/mit-license.php
	// Created: 2010-04-16 | Updated: 2010-04-26
	//
	/** **************************************************************************************** */

	$.fn.pajinate = function(options) {
		// Setup default option values
		var defaults = {
			item_container_id : '.content',
			nav_panel_id : '.page_navigation'
		};

		var options = $.extend(defaults, options);
		var $page_container;
		var itemsContainer;
		var $items;
		var $allItems;

		var pageSize = 5;

		var $nbPages = 0;

		var curPageNum = 0;

		var $navigation;
		var firstLink;
		var prevLink;
		var nextLink;
		var lastLink;
		var nbPageLinkToShow = 2;

		var searchField;
		var sortField = "nbVotes";
		
		return this
				.each(function() {
					$page_container = $(this);
					$navigation = $page_container.find(options.nav_panel_id);
					itemsContainer = $page_container.find(options.item_container_id);
					$allItems = itemsContainer.children();
					$items = $allItems;
					sortItems();
					
					if(sortField === "nbVotes") {
						$items.slice(0,1).removeClass("defaultIdea").addClass("firstIdea");
						$items.slice(1,2).removeClass("defaultIdea").addClass("secondIdea");
						$items.slice(2,3).removeClass("defaultIdea").addClass("thirdIdea");
					}

					itemsContainer.html($items);

					// Initialize meta data
					// Get the total number of items
					// Calculate the number of pages needed
					$nbPages = Math.ceil($items.size() / pageSize);

					// Construct the nav bar
					var navigationContent = '<ul class="unstyled">'
							+ '<li class="first_link"><a href="#">&laquo</a></li>'
							+ '<li class="previous_link"><a href="#">Prev</a></li>';
					for ( var i = 0; i < $nbPages; i++) {
						navigationContent += '<li class="page_link" longdesc="'
								+ i + '"><a href="#">' + (i + 1) + '</a></li>';
					}
					navigationContent += '<li class="next_link"><a href="#">Next</a></li>'
							+ '<li class="last_link"><a href="#">&raquo</a></li></ul>';
					$navigation.html(navigationContent);
					firstLink = $navigation.find('ul li.first_link');
					prevLink = $navigation.find('ul li.previous_link');
					nextLink = $navigation.find('ul li.next_link');
					lastLink = $navigation.find('ul li.last_link');

					/* Bind the actions to their respective links */
					firstLink.click(function(e) {
						e.preventDefault();
						gotoPage(0);
					});
					lastLink.click(function(e) {
						e.preventDefault();
						gotoPage($nbPages - 1);
					});
					prevLink.click(function(e) {
						e.preventDefault();
						gotoPage(Math.max(0, curPageNum - 1));
					});
					nextLink.click(function(e) {
						e.preventDefault();
						gotoPage(Math.min($nbPages - 1, curPageNum + 1));
					});
					$page_container.find('.page_link').click(function(e) {
						e.preventDefault();
						gotoPage($(this).attr('longdesc'));
					});

					searchField = $("#searchIdea");
					searchField.keyup(search);
					
					// Sort by
					$("#sortBy").find("button").click(function(e) {
						sortField = $(this).attr("data-sort");
						sortItems();
						itemsContainer.html($items);
						$allItems.hide();
						$items.slice(curPageNum * pageSize,
								curPageNum * pageSize + pageSize).show();

					});
					
					gotoPage(0);
				});

		function search() {
			var searchQuery = searchField.val().toLowerCase();
			$items = $allItems.filter(function(index) {
				return ($(this).attr("data-name").indexOf(searchQuery) !== -1 || $(this).attr("data-description").indexOf(searchQuery) !== -1);
			})
			sortItems();
			itemsContainer.html($items);
			$nbPages = Math.ceil($items.size() / pageSize);
			gotoPage(0);
		}

		function sortItems() {
			$items = $items.sort(function(a, b) {
				var valA = $(a).attr("data-" + sortField);
				!isNaN(parseInt(valA)) ? parseInt(valA) : valA;
				var valB = $(b).attr("data-" + sortField);
				!isNaN(parseInt(valB)) ? parseInt(valB) : valB;
				
				if(valA > valB) {
					return -1;
				} else if(valA < valB) {
					return 1;
				} else {
					var nameA = $(a).attr("data-date");
					var nameB = $(b).attr("data-date");
					return (nameA > nameB) ? -1 : (nameA < nameB) ? 1 : 0;
				}
			})
			if(sortField === "nbVotes") {
				$items.slice(0,1).removeClass("defaultIdea").addClass("firstIdea");
				$items.slice(1,2).removeClass("defaultIdea").addClass("secondIdea");
				$items.slice(2,3).removeClass("defaultIdea").addClass("thirdIdea");
			} else {
				$items.removeClass("firstIdea").removeClass("secondIdea").removeClass("thirdIdea").addClass("defaultIdea");
			}
		}

		function gotoPage(pageNum) {
			curPageNum = parseInt(pageNum);

			// Display the items
			$allItems.hide();
			$items.slice(curPageNum * pageSize,
					curPageNum * pageSize + pageSize).show();

			// Reassign the active class
			$navigation.find('ul li.page_link[longdesc=' + curPageNum + ']')
					.addClass('disabled').siblings('.disabled').removeClass(
							'disabled');

			// update num pages to display
			var nbBefore = Math.min(curPageNum, nbPageLinkToShow);
			var nbAfter = (nbPageLinkToShow - nbBefore) + nbPageLinkToShow + 1;
			var startPage = Math.max(0, curPageNum - nbBefore);
			var endPage = Math.min($nbPages, curPageNum + nbAfter);

			$navigation.each(function() {
				$navigation.find('ul li.page_link').hide().slice(startPage,
						endPage).show();
			});

			// Add a class to the next or prev links if there are no more pages
			// next or previous to the active page
			if (curPageNum == 0) {
				prevLink.addClass('disabled')
				firstLink.addClass('disabled')
			}
			if (curPageNum == $nbPages - 1) {
				nextLink.addClass('disabled')
				lastLink.addClass('disabled')
			}
		}
	};

})(jQuery);