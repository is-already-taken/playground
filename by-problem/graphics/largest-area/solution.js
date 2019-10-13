
/* Find the largest contiguous area.
 * Return the input matrix with the area's values replaced by "X".
 * Don't care for performance or memory efficiency _in the first place_,
 * get the algorithm done.
 * Further constraints:
 * - Assume there are no extremes like 1xN or Nx1
 */

function findContiguousArea(data) {
	/* Recursive approach
	 *
	 * Drawbacks:
	 * - recursive (requires more memory, slower)
	 * - multiple checks when area 1 is left and area 2 is probed
	 *
	 * 1. initialize seen with [] ([x,y])
	 * 2. initialize contiguousAreas with [] ([[x,y], [x,y], ...])
	 * 3. iterate x,y
	 * 4.  - if x, y in seen skip to next
	 * 5.  - call traceContiguousArea(x, y, seen)
	 * 6. inner function traceContiguousArea()
	 *     - is called with x, y, seen
	 *     - returns object of list of contiguousArea coordinates
	 *     1. let area be an empty array
	 *     2. call recurseContiguousArea(x, y, seen, area)
	 * 7. inner function recurseContiguousArea()
	 *     - is called with x, y, seen, area
	 *     1. let currentValue be the value at x,y
	 *	   2. if x-1,y is not in seen
	 *         - if the the value of that position is equal to currentValue
	 *            - store x,y in area
	 *            - call recurseContiguousArea(x-1,y,area)
	 *	   3. if x+1,y is not in seen
	 *         - if the the value of that position is equal to currentValue
	 *            - store x,y in area
	 *            - call recurseContiguousArea(x+1,y,area)
	 *	   4. if x,y-1 is not in seen
	 *         - if the the value of that position is equal to currentValue
	 *            - store x,y in area
	 *            - call recurseContiguousArea(x,y-1,area)
	 *	   5. if x,y+1 is not in seen
	 *         - if the the value of that position is equal to currentValue
	 *            - store x,y in area
	 *            - call recurseContiguousArea(x,y+1,area)
	 *
	 */

	const columns = data[0].length;
	const rows = data.length;
	const seen = [];
	const contiguousAreas = [];
	let area,
		offset,
		value,
		largestSize = 0,
		largest,
		x,
		y;

	for (y = 0; y < rows; y++) {
		for (x = 0; x < columns; x++) {
			offset = y * columns + x;
			value = data[y][x];

			if (seen.indexOf(offset) !== -1) {
				continue;
			}

			area = traceContiguousArea(x, y, seen);
			contiguousAreas.push([value, area]);
		}
	}

	for ([value, area] of contiguousAreas) {
		if (area.length <= largestSize) {
			continue;
		}

		largestSize = area.length;
		largest = area;
	}

	area = [];

	for (y = 0; y < rows; y++) {
		area[y] = [];

		for (x = 0; x < columns; x++) {
			area[y][x] = 0;
		}
	}

	for ([x,y] of largest) {
		area[y][x] = 1;
	}

	return area;

	function traceContiguousArea(x, y, seen) {
		const area = [];
		recurseContiguousArea(x, y, seen, area);
		return area;
	}
	
	function recurseContiguousArea(x, y, seen, area) {
		const value = data[y][x];
		const combinations = [];
		let offset,
			currentValue;

		if (x > 0) {
			combinations.push([x-1, y]);
		}

		if (x < columns - 1) {
			combinations.push([x+1, y]);
		}

		if (y > 0) {
			combinations.push([x, y-1]);
		}

		if (y < rows - 1) {
			combinations.push([x, y+1]);
		}

		for ([x, y] of combinations) {
			offset = y * columns + x;
			currentValue = data[y][x];

			if (seen.indexOf(offset) !== -1 || currentValue !== value) {
				continue;
			}

			area.push([x,y]);
			seen.push(offset);

			recurseContiguousArea(x, y, seen, area);
		}
	}
}

export { findContiguousArea };
