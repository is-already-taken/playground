define([
	"somelib/tool",
	"./util/some-util",
	"./controller/some-controller"
], function(
	SomeLibTool,
	SomeUtil,
	SomeController
) {
	return {
		NAME: "component",
		REQUIRED: [SomeLibTool.NAME, SomeUtil.NAME, SomeController.NAME]
	};
});
