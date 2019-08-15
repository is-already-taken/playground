
import { findContiguousArea } from "./solution.js";

function assertEquals(actual, expected) {
	if (actual.join("") === expected.join("")) {
		console.info("Assertion met.");
	} else {
		console.error("Not equal");
	}
}

function test() {
	const image = [
		[1,1,1,0,0,0,0,0,0,1,0],
		[1,0,0,0,0,0,0,0,1,1,0],
		[1,0,0,2,0,0,3,1,1,1,0],
		[1,0,0,2,0,0,3,0,1,1,1],
		[1,0,2,2,2,3,3,0,1,1,1],
		[1,0,0,2,2,3,3,3,1,1,1],
		[1,0,0,2,2,3,3,3,3,1,1],
		[1,0,2,2,2,3,3,3,2,2,1],
		[1,0,0,2,2,2,3,3,2,2,2],
		[1,1,0,0,2,2,3,2,2,1,1],
		[0,1,1,0,0,2,3,3,1,1,0]
	];

	const expected = [
		[0,0,0,1,1,1,1,1,1,0,0],
		[0,1,1,1,1,1,1,1,0,0,0],
		[0,1,1,0,1,1,0,0,0,0,0],
		[0,1,1,0,1,1,0,0,0,0,0],
		[0,1,0,0,0,0,0,0,0,0,0],
		[0,1,1,0,0,0,0,0,0,0,0],
		[0,1,1,0,0,0,0,0,0,0,0],
		[0,1,0,0,0,0,0,0,0,0,0],
		[0,1,1,0,0,0,0,0,0,0,0],
		[0,0,1,1,0,0,0,0,0,0,0],
		[0,0,0,1,1,0,0,0,0,0,0]
	];

	let result = findContiguousArea(image);

	console.log(result.map((row) => row.join(" ")).join("\n"));
	
	assertEquals(result, expected);
}

test();
