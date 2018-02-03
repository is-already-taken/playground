
/**
 * Parse JSON from ArrayExpression AST
 * @param {Object} node ArrayExpression node
 * @return {Array} array of mixed element types parsed from that expression
 */
function parseArrayExpression(node) {
	let result = [],
		element;

	for (element of node.elements) {
		if (element.type === "Literal") {
			result.push(element.value);
		} else if (element.type === "ObjectExpression") {
			result.push(parseObjectExpression(element));
		} else {
			throw new Error("Unhandled element type in ArrayExpression: " + element.type);
		}
	}

	return result;
}

/**
 * Parse JSON from ObjectExpression AST
 * @param {Object} node ObjectExpression node
 * @return {Object} object parsed from the object expression
 */
function parseObjectExpression(node) {
	let result = { },
		value,
		property,
		identifier;

	for (property of node.properties) {
		if (property.type !== "Property") {
			throw new Error("Expected node type Property as entry in PropertyExpression");
		}

		if (property.key.type === "Identifier") {
			// { Identifier: ... }
			identifier = property.key.name;
		} else if (property.key.type === "Literal") {
			// { "Literal": ... }
			identifier = property.key.value;
		}

		if (property.value.type === "Literal") {
			value = property.value.value;
		} else if (property.value.type === "ObjectExpression") {
			value = parseObjectExpression(property.value);
		} else if (property.value.type === "ArrayExpression") {
			value = parseArrayExpression(property.value);
		}

		result[identifier] = value;
	}

	return result;
}

// Export object parser function
module.exports = {
	parseObjectExpression,
	parseArrayExpression
};
