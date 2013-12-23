load("libs/DemoAppTests.js");

DemoAppTests.hierarchy.prototype.run = function() {
	for (var i = 0; i < 2; i++) {
		this.app.tabBar().select("hierarchy");
		this.app.table().select("Boron");
		this.app.device().back();
		this.app.table().select("Hydrogen");
		this.app.device().back();
		this.app.table().verify("Helium", "item(2)");
	}
	print("All done");
};