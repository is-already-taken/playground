define([
	"text!./template.tpl"
], function(
	template
) {
	return {
		NAME: "Component: controller/some-controller",
		REQUIRED: ["tpl:" + template]
	};
});
