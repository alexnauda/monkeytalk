
/*** custom command -- $COMPONENT_TYPE$ ***/
$LIB_NAME$.$COMPONENT_TYPE$ = function(app, monkeyId) {
	this.app = app;
	this.monkeyId = monkeyId;
};

$LIB_NAME$.$COMPONENT_TYPE$.prototype = new MT.CustomType;

MT.Application.prototype.$LOWER_COMPONENT_TYPE$ = function(monkeyId) {
	return new $LIB_NAME$.$COMPONENT_TYPE$(this, monkeyId);
};
