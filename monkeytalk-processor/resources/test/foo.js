if (typeof SampleProject == "undefined") {
	load("libs/SampleProject.js");
};

SampleProject.foo.prototype.run = function() {

	this.app.button("OK").tap();

};

