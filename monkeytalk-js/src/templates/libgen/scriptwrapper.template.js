
/*** script -- $SCRIPT_NAME$ ***/
$LIB_NAME$.$SCRIPT_NAME$ = function(app) {
	MT.Script.call(this, app, "$SCRIPT_NAME$.mt");
};

$LIB_NAME$.$SCRIPT_NAME$.prototype = new MT.Script;

$LIB_NAME$.$SCRIPT_NAME$.prototype.call = function($PARAMS$) {
	//run: $SCRIPT_NAME$.mt
	MT.Script.prototype.call($THIS_AND_PARAMS$);
};

MT.Application.prototype.$LOWER_SCRIPT_NAME$ = function() {
	return new $LIB_NAME$.$SCRIPT_NAME$(this);
};
