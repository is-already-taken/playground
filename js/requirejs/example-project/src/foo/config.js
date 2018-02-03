requirejs.config({
	baseUrl: "./src/foo/",

	paths: {
		"text": "../plugins/text",
		"somelib": "../libs/somelib/",
		"component": "../dummy/dummy-script"
	},

	packages: [{
		location: "../packages/component",
		name: "component",
		main: "main"
	}]
});
