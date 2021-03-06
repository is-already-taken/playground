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

function isRelative(location) {
	return /^[.]/.test(location);
}

/**
 * Parse package config to get direct name => path mapping
 * @param {Object} packages array
 */
function parsePackages(packages) {
	return packages.reduce((mapping, package) => {
		mapping[package.name] = {
			location: package.location,
			main: package.location + "/" + (package.main || "main")
		};

		return mapping;
	}, { })
}

function resolve(configFile, entryModule) {
	/*
	 *
	 * Define baseUrl:
	 * 
	 * 1. if baseUrl is defined calculate absolute path of entry module and baseUrl
	 * 2. else define baseUrl as dirname(entryModule)
	 *
	 * Parsing entry module:
	 *
	 * 1. for each import path or name as module
	 * 2. check for file in dirname(module)
	 * 3. if it exists, load that one
	 * 4. if not resolve path from config
	 * 5a. if packages contains a name like that resolve the package path [RESOLVEBYPKG]
	 * 5b. otherwise if paths contains a name like that resolve the path [RESOLVEBYPATHS]
	 * 5c. else fail
	 *
	 * Resolving package [RESOLVEBYPKG]:
	 * 
	 * 1a. join the fields location and main as relPackagePath
	 * 1b. if main is not defined, join fields location and "main" as relPackagePath
	 * 2. join the paths baseUrl and relPackagePath
	 * 3. fail if the name is not contained in packages
	 * 
	 * Resolving paths [RESOLVEBYPATHS]:
	 *
	 * 1. return the value of the path config
	 * 2. else fail
	 */

	/*
	 * 1. If `baseUrl` is defined in the config, expand `baseUrl` with the current 
	 *    working directory and set `BASEURL` to that value
	 * 1. else `BASEURL` is `dirname(ENTRYMODULE)`
	 * 
	 * 
	 * ## Path expansion
	 * 
	 * 1. Relative locations are referenced to the module's location 
	 *    `dirname(CURRENTMODULE)`
	 * 1. Resolve other locations in the following order, referenced agains the 
	 *    `BASEURL`
	 *    1. If the name is found in `packages` resolve path by the entry module
	 *       of that package
	 *    1. else if the name if found in `path` resolve path by the mapped path
	 *    1. else assume the name lies under `BASEURL`
	 * 
	 * 
	 */

	function expandFromBase(location, optional = "") {
		return path.resolve(baseUrl, location, optional);
	}

	function expandFromEntry(location) {
		return path.resolve(relModulePath, location);
	}

	const cwd = path.dirname(__dirname);

	let relModulePath = path.dirname(entryModule),

		config,

		// Part of the config
		baseUrl = cwd,
		packages,

		// Entry module imports
		moduleImports,
		location,
		locationParts,

		pkgNameOrPathPfx,
		suffix,

		collectedModules = [];

	console.log("CWD: ", cwd);

	config = parseConfig(configFile);

	console.log("parsed config " + config);
	console.log(config);

	packages = parsePackages(config.packages);
	console.log("packages: ", packages);

	if (config.baseUrl) {
		// TODO: should probably relative to CWD
		baseUrl = config.baseUrl;
	}

	moduleImports = parseModule(entryModule);

	console.log("parsed module " + entryModule);
	console.log(moduleImports);

	for (location of moduleImports) {
		location = location.path;

		console.log("# resolving location " + location);

		if (isRelative(location)) {
			// Handling locations like "./foo" or "../foo"
			console.log("# - is relative, expand to " + relModulePath);
			collectedModules.push(expandFromEntry(location));
			continue;
		}

		locationParts = location.split("/");

		// A (non-relative) location may be specified as a path too, in that
		// case the first component refers either to the package name or to
		// the path configuration
		pkgNameOrPathPfx = locationParts[0];
		suffix = locationParts.slice(1).join("/");

		if (pkgNameOrPathPfx in packages) {
			console.log("# - found in packages");
			if (suffix === "") {
				// Only the package name was specified, e.g. "foopackage"
				collectedModules.push(expandFromBase(packages[pkgNameOrPathPfx].main));
			} else {
				// A package relative path was specified, e.g. "foopackage/bar"
				collectedModules.push(expandFromBase(packages[pkgNameOrPathPfx].location, suffix));
			}
		} else if (pkgNameOrPathPfx in config.paths) {
			console.log("# - found in paths");
			collectedModules.push(expandFromBase(config.paths[pkgNameOrPathPfx], suffix));
		} else {
			console.log("# - assumed to be relative to " + relModulePath);
			collectedModules.push(expandFromBase(location));
		}
	}

	return collectedModules;
}

function main(argv) {
	const	config = argv[0],
			entryModule = argv[1];

	let collected;

	collected = resolve(config, entryModule);

	console.log("collected modules", collected);
}

main(process.argv.slice(2));