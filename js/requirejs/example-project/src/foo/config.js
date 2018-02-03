requirejs.config({
	baseUrl: "./src/foo/",

	paths: {
		"text": "../plugins/text",
		"somelib": "../libs/somelib/"
	},

	packages: [{
		location: "../packages/component",
		name: "component",
		main: "main"
	}]
});
