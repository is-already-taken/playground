
const initialLocation = [
	[47.4132405,15.2797053],
	[47.4110544,15.2815932]
];

const map = L.map("map").setView(initialLocation[0], 13);

L.tileLayer("https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}", {
	attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
	maxZoom: 18,
	id: "mapbox.streets",
	accessToken: MAPBOX_ACCESS_TOKEN
}).addTo(map);

let line,
	path = [];

line = L.polyline(
	path,
	{
		color: "#9999ff",
		bubblingMouseEvents: false
	}
).addTo(map);

function dragMarker(lineLocation, markerDragEvent) {
	console.log("drag marker: ", markerDragEvent);

	// Edit related location in path
	lineLocation[0] = markerDragEvent.latlng.lat;
	lineLocation[1] = markerDragEvent.latlng.lng;
	
	// Update the path after editing the location
	line.setLatLngs(path);
}

// Handle click on map to add maker and extend the path
map.on("click", (evt) => {
	// Prepare location tuple to add to path and be able to pass the reference
	// to the drag handler via bind()
	let lineLocation = [evt.latlng.lat, evt.latlng.lng],
		marker;

	console.log("clicked map: ", evt.latlng, evt);

	marker = L.marker(
		[evt.latlng.lat, evt.latlng.lng],
		{ draggable: true }
	).addTo(map);

	marker.on("drag", dragMarker.bind(null, lineLocation));

	path.push(lineLocation);
	line.setLatLngs(path);
});

/**
 * Return all succeeding location pairs of a path whose bounding boxes may 
 * contain the reference location. In case of overlapping path segments
 * this will return mor than one pair. The indexes of the path's locations 
 * are returned.
 */
function nearestLocations(path, referenceLoc) {
	let i,
		pairs = [];

	// Start at 1: we need to have a predecessor
	for (i = 1; i < path.length; i++) {

		// Test succeeding locations's boundaries whether they contain 
		// the reference location
		if (L.latLngBounds(path[i - 1], path[i]).contains(referenceLoc)) {
			pairs.push([i - 1, i]);
		}
	}

	// NOTE: in case of paths with crossings / figure of eight patterns this 
	//       failed for the segments nearer towards the end of the path.

	return pairs;
}

line.on("click", (evt) => {
	let marker,
		newLocation,
		nLoc;

	console.log("clicked path: ", evt.latlng, evt);

	nLoc = nearestLocations(path, evt.latlng);

	console.log("clicked between: ", nLoc);

	if (nLoc.length === 0) {
		return;
	}

	newLocation = [evt.latlng.lat, evt.latlng.lng];

	marker = L.marker(
		newLocation,
		{ draggable: true }
	).addTo(map);

	marker.on("drag", dragMarker.bind(null, newLocation));

	// Insert clicked location
	path.splice(nLoc[0][0] + 1, 0, newLocation);
	// Force re-render
	line.setLatLngs(path);
});
