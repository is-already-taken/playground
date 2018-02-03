require([
	"./config",
	"component",
	"component/util/some-util",
	"meh"
], function(
	_config,
	Component,
	SomeUtil,
	meh
) {
	console.log("main loaded component: ", Component);
	console.log("component-relative: ", SomeUtil);
	console.log("meh: ", meh);
});