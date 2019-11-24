
function midPoint(point1, point2) {
	let [x1, y1] = point1,
		[dx, dy] = distance(point1, point2),
		dxHalv = Math.floor(dx / 2),
		dyHalv = Math.floor(dy / 2);

	return [x1 + dxHalv, y1 + dyHalv];
}

function distance(point1, point2) {
	let [x1, y1] = point1,
		[x2, y2] = point2,
		dx = x2 - x1,
		dy = y2 - y1;

	return [dx, dy];
}

function _fillPairRecursively(twoPoints, maxGap) {
	let filled = twoPoints.concat([]),
		[left, right] = twoPoints,
		filledLeft,
		filledRight,
		[dx, dy] = distance(left, right);

	if (!maxGap) {
		throw new Error("_fillPairRecursively() may not be called with undefined maxGap");
	}

	if (maxGap <= 1) {
		throw new Error("_fillPairRecursively() may not be called with maxGap <= 1");
	}

	if (Math.abs(dy) < maxGap && Math.abs(dy) < maxGap) {
		return twoPoints;
	}

	filled.splice(1, 0, midPoint(left, right));

	filledLeft = _fillPairRecursively(filled.slice(0, 2), maxGap);
	filledRight = _fillPairRecursively(filled.slice(1, 3), maxGap);

	return filledLeft.concat(filledRight.slice(1));
}

/**
 * Fill the gaps in the list of points.
 * 
 * @param {Number[][]} points array of point arrays
 * @param {Number} [maxGap] max distance of X and Y from point to point
 * @return {Number[][]} array of points with gaps filled
 */
function fillGaps(points, maxGap) {
	let last = null,
		pointCount = points.length,
		closed = [],
		idx,
		win,
		pointInBetween;

	maxGap = maxGap || 2;

	closed.push(points[0]);

	for (idx = 1; idx < pointCount; idx++) {
		win = points.slice(idx - 1, idx + 1);

		closed = closed.concat(
			_fillPairRecursively(win, maxGap)
			.slice(1)
		);
	}

	return closed;
}

// Just a simple test assertion function
function assertEquals(testname, actual, expected) {
	if (actual.toString() === expected.toString()) {
		console.log(testname, ": assertion met: ", actual);
	} else {
		console.error(testname, ": assertion failed: actual=", actual, " expected=", expected);
	}
}

assertEquals("distance", distance([1,1], [4,7]), [3, 6]);

assertEquals("midPoint", midPoint([1,1], [4,7]), [2, 4]);
assertEquals("midPoint", midPoint([1,1], [1,3]), [1, 2]);
assertEquals("midPoint", midPoint([3,1], [1,3]), [2, 2]);
assertEquals("midPoint", midPoint([3,3], [1,1]), [2, 2]);

assertEquals(
	"_fillPairRecursively",
	_fillPairRecursively([[11,11],[11,13]], 2),
	[[11,11],[11,12],[11,13]]
);

assertEquals(
	"_fillPairRecursively",
	_fillPairRecursively([[11,11],[13,15]], 2),
	[[11,11],[11,12],[12,13],[12,14],[13,15]]
);

assertEquals(
	"fillGaps",
	fillGaps(
		[[11,9],[11,11],[13,15]],
		2
	),
	[
		[11,9],[11,10],[11,11],[11,12],[12,13],[12,14],[13,15]
	]
);

assertEquals(
	"fillGaps",
	fillGaps(
		[[10,7],[11,9],[11,11],[13,15]],
		2
	),
	[
		[10,7],[10,8],[11,9],[11,10],[11,11],[11,12],[12,13],[12,14],[13,15]
	]
);
