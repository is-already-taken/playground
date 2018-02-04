require([
	"./config",
	"component",
	"component/util/some-util",
	"somelib/complex/foocomplex",
	"meh"
], function(
	_config,
	Component,
	SomeUtil,
	FooComplex,
	meh
) {
	console.log("main loaded component: ", Component);
	console.log("component-relative: ", SomeUtil);
	console.log("somelib-relative: ", FooComplex);
	console.log("meh: ", meh);
});