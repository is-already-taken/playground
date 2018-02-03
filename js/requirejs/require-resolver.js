/* RequireJS resolver 
 * 
 * Pass a config and a module and get the referenced modules out.
 */

const	fs = require("fs"),
		path = require("path"),
		esprima = require("esprima"),
		configParser = require("./lib/config-parser"),
		moduleParser = require("./lib/module-parser");

function parse(file) {
	const script = String(fs.readFileSync(file));

	return esprima.parse(script, {
		range: true,
		tolerant: true
	});
}


function parseModule(file) {
	return moduleParser(parse(file));
}

function parseConfig(file) {
	return configParser(parse(file));
}

function main(argv) {
	const	config = argv[0],
			entryModule = argv[1];

	console.log("parse config " + config);
	console.log(parseConfig(config));

	console.log("parse mmodule " + entryModule);
	console.log(parseModule(entryModule));
}

main(process.argv.slice(2));