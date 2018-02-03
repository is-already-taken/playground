
const parseObjectExpression = require("./json-parser").parseObjectExpression;

/**
 * Parse config from AST object
 * @param {Object} ast Program AST node to parse RequireJS config from.
 * @return {Object} RequireJS config
 */
function parseConfig(ast) {
	// Basically the ususal codes have an expression statement at first
	let root = ast.body[0].expression;

	console.log("type=", root.type);

	if (root.type !== "CallExpression") {
		throw new Error("Expected first statement to be a call expression.");
	}

	if (root.callee.type !== "MemberExpression") {
		throw new Error("Expected callee of the call expression to be a member expression.");
	}

	if (root.callee.object.name !== "requirejs" || root.callee.property.name !== "config") {
		throw new Error("Expected callee to be a call to requirejs.config");
	}

	// The first argument passed to that method is the config object.
	root = root.arguments[0];

	return parseObjectExpression(root);
}

module.exports = parseConfig;
