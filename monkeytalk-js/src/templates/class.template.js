//
// CLASS: $CLASS_NAME$
//
/**
 * $CLASS_DESC$
 * @class
 * @extends MT.$CLASS_EXTENDS$
 * @param app The MT.Application containing this $CLASS_NAME$
 * @param monkeyId A monkeyId or ordinal
 */
MT.$CLASS_NAME$ = function(app, monkeyId) {
	MT.$CLASS_EXTENDS$.call(this, app, monkeyId);
	this.componentType = "$CLASS_NAME$";	
};

MT.$CLASS_NAME$.prototype = new MT.$CLASS_EXTENDS$;

/**
 * 
 * @param monkeyId the monkeyId (String) or ordinal (int) for which to search. 
 * @returns {MT.$CLASS_NAME$} the upper-left-most visible $CLASS_NAME$ matching the supplied monkeyId or ordinal
 */
MT.Application.prototype.$CLASS_FACTORY$ = function(monkeyId) {
	if (!monkeyId) {
		monkeyId = "";
	}
	return new MT.$CLASS_NAME$(this, monkeyId);
};

$METHODS$
