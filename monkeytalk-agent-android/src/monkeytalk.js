/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

/**
 * 
 * Used by Java to call JS, passing/returning representations of html elements
 * in a WebView
 * 
 * When we return html elements from JS to Java, we convert the elements into
 * delimited strings containing various element properties including id, name,
 * value, textContent, x, y, width and height. In Java, we parse the delimited
 * strings and create HtmlElement objects. When we call JS from Java, we pass in
 * the HtmlElement as a JS literal object, whose values are then used to find
 * the actual html element that the HtmlElement object represents.
 */

if (!(window.hasOwnProperty("monkeytalk"))) {
	window.monkeytalk = {
		/**
		 * Calls a function, passing it the actual element corresponding to the
		 * supplied HtmlElement representation Returns the functions return
		 * value as a string (returned html elements must already be encoded)
		 */

		call : function(elemrep, func) {
			var elem = window.monkeytalk.findElement(elemrep);
			result = func(elem);
			return result;
		},

		/**
		 * Find the actual html element corresponding to this HtmnlElement
		 * representation
		 */
		findElement : function(elemrep) {

			elem = elemrep;
	
			if (elem.id && elem.id.length) {
				result = document.getElementById(elem.id);
				if (result.tagName == elem.tagName) {
					return result;
				}
			}

			if (elem.name && elem.name.length) {
				elems = document.getElementsByName(elem.name);
				for (i = 0; i < elems.length; i++) {
					if (elems.item(i).tagName == elem.tagName) {
						return elems.item(i);
					}
				}
			}

			if (elem.className && elem.className.length) {
				elems = document.getElementsByClassName(elem.className);
				for (i = 0; i < elems.length; i++) {
					if (elems.item(i).tagName == elem.tagName) {
						return elems.item(i);
					}
				}
			}

			elems = document.getElementsByTagName(elem.tagName);

			for (i = 0; i < elems.length; i++) {
				var tag = elems.item(i);
				if (tag.type && tag.type.length && tag.type != elem.type) {
					continue;
				}
				if (elem.monkeyId == "*"
					||tag.value  == elem.monkeyId 
					|| tag.textContent == monkeyId
					|| tag.title == elem.monkeyId
					|| tag.className == elem.monkeyId) {
					return tag;
				}
			}
			return null;
		},

		/**
		 * Encode the html element or list (nodelist or array) of html elements
		 * as a delimited string that can be parsed in Java
		 */
		encodeElements : function(elems, attrs) {

			if (!('length' in elems)) {
				// It's a single element
				return window.monkeytalk.encodeElement(elems);
			}

			result = '';
			for (i = 0; i < elems.length; i++) {
				if (i) {
					result += '<-mte->'
				}

				// Nodelists must be indexed by "item" method, but arrays are
				// accessed as, um, arrays
				var elem = 'item' in elems ? elems.item(i) : elems[i];

				result += window.monkeytalk.encodeElement(elem, attrs);
			}
			return result;
		},

		/**
		 * Encode the supplied html element into a delimited string that can be
		 * parsed in Java
		 * Key identifying attrs including id, name, value, and text are always returned. Additional attrs
		 * identified may be optionally specified as array of names. These will be appended at the end.
		 */
		encodeElement : function(elem, attrs) {

			var parent = elem;
			y = 0;
			x = 0;
			while (parent != null) {
				y += parent.offsetTop;
				x += parent.offsetLeft;
				parent = parent.offsetParent;
			}
			;
			s = elem.tagName + '<-mtf->' + elem.id + '<-mtf->' + elem.name
					+ '<-mtf->' + elem.className + '<-mtf->' + elem.value
					+ '<-mtf->' + elem.textContent + '<-mtf->' + elem.type
					+ '<-mtf->' + x + '<-mtf->' + y + '<-mtf->'
					+ elem.clientWidth + '<-mtf->' + elem.clientHeight
					+ '<-mtf->' + elem.title;
			if (attrs) {
				for (i=0; i<attrs.length; i++) {
					s+= '<-mtf->' + elem[attrs[i]];
				}
			}
			return s;
		},

		/**
		 * Return the (first) table cell with the supplied value
		 */
		findCell : function(table, value) {
			for (i = 0; i < table.rows.length; i++) {
				for (j = 0; j < table.rows[i].cells.length; j++) {
					c = table.rows[i].cells[j];
					if (window.monkeytalk.isMatch(c, value)) {
						return c;
					}
				}
			}
		},
		/**
		 * Return the (first) radiobutton with the supplied value
		 * 
		 * @param group -
		 *            a NodeList of Input/radio tags.
		 */
		findRadioButton : function(group, value) {
			for (i = 0; i < group.length; i++) {
				var c = group.item(i);
				if (window.monkeytalk.ismatch(c, value)) {
					return c;
				}

			}
		},

		/**
		 * Return true if the supplied element has an identifying attr with the
		 * supplied value
		 */
		isMatch : function(elem, value) {
			var c = elem;
			return [ c.id, c.name, c.value, c.textContent, c.title ]
					.indexOf(value) > -1
		},

		/**
		 * Select the option from an Html select
		 */
		selectItem : function(select, value) {
			for (i = 0; i < select.length; i++) {
				if (window.monkeytalk.isMatch(select.options[i], value)) {
					select.selectedIndex = i;
					return;
				}
			}
			console.error('monkeytalk: Unable to find option: ' + value)
		}
	}
}