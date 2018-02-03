
const parseArrayExpression = require("./json-parser").parseArrayExpression;

/**
 * Parse module import from AST object
 * @param {Object} ast Program AST node to parse module definition config from.
 * @return {Object[]} module imports definitions
 */
function parseModuleImports(ast) {
	let root = ast.body[0].expression,
		argsNode,
		paramNode,
		importPaths = [],
		importNames = [],
		imports = [];

	if (root.type !== "CallExpression") {
		throw new Error("Expected call expression in module");
	}

	if (root.callee.type !== "Identifier" || !["require", "define"].includes(root.callee.name)) {
		throw new Error("Expected call expression to function require() or define() in module");
	}

	argsNode = root.arguments;

	// TODO handles (and checks) define([ ... ], fn() { ... }) yet only, 
	// add support for define(fm() { ... }) later

	if (argsNode.length !== 2 
		|| argsNode[0].type !== "ArrayExpression"
		|| argsNode[1].type !== "FunctionExpression") {
			throw new Error("Expected call expression in form fn([...], function() { ... })");
	}

	// Parse first argument to define() or require(), the array of imports
	importPaths = parseArrayExpression(argsNode[0]);

	// Parse the parameters of the callback function
	for (paramNode of argsNode[1].params) {
		importNames.push(paramNode.name);
	}

	// Merge imports with import names

	imports = importPaths.map((path, index) => {
		return {
			path: path,
			name: importNames[index] || null
		};
	});

	return imports;
}

module.exports = parseModuleImports;
