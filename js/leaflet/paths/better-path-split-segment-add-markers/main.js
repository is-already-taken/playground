
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

let path = [],
	// Store line instances between two points
	segments = [];

function findLocationIndex([lat, lng]) {
	let index = 0,
		pathLat,
		pathLng;

	for ([pathLat, pathLng] of path) {
		if (pathLat === lat && pathLng === lng) {
			return index;
		}

		index++;
	}

	return -1;
}

function dragMarker(locationIndex, markerDragEvent) {
	let segmentA,
		segmentB,
		segmentIdx;

	console.log("drag marker: ", markerDragEvent, "location index: ", locationIndex);

	// Update location
	path[locationIndex] = [
		markerDragEvent.latlng.lat,
		markerDragEvent.latlng.lng
	];

	// path     = [a,b,c,d,e]                    <= locationIndex
	// segments = [[a,b], [b,c], [c,d], [d,e]]
	// 
	// When dragging "a" we've to modify the segments [a,b] only
	// When dragging "b" we've to modify the segments [a,b] and [b,c]
	// The same goes for all following indexes.

	if (locationIndex === 0) {
		// Referring to the above example we clicked "a"
		segmentA = segments[locationIndex];
		segmentA.setLatLngs(path.slice(locationIndex, locationIndex + 2));
	} else if (locationIndex === (path.length - 1)) {
		// Referring to the above example we clicked "e"
		segmentB = segments[locationIndex - 1];	
		segmentB.setLatLngs(path.slice(locationIndex - 1, locationIndex + 1));
	} else {
		// Referring to the above example we clicked "c"
		// segmentA is [b,c] and segmentB is [c,d]
		// Referring to the above example we clicked "b"
		// segmentA is [a,b] and segmentB is [b,c]
		segmentA = segments[locationIndex - 1];
		segmentB = segments[locationIndex];

		segmentA.setLatLngs(path.slice(locationIndex - 1, locationIndex + 1));
		segmentB.setLatLngs(path.slice(locationIndex, locationIndex + 2));
	}
}

// Segment clicked: add a new maker and split the segment
function segmentClick(segmentIndex, evt) {
	let clickedLocation = [evt.latlng.lat, evt.latlng.lng],
		marker,
		newPathIndex = segmentIndex + 1,
		segment,
		preceedingSegment,
		draggedIndex;

	// path     = [a,b,c,d,e]
	// segments = [[a,b], [b,c], [c,d], [d,e]]
	// 
	// Say segment #1 (b,c) was clicked. Path index for splicing will
	// be 2. Beside adding the new, spliced segment we've to update 
	// segment #1 and #3 *afterwards*.

	// Add marker where clicked
	marker = L.marker(
		clickedLocation,
		{ draggable: true }
	).addTo(map);

	marker.on("dragstart", (evt) => {
		draggedIndex = findLocationIndex([
			evt.target.getLatLng().lat,
			evt.target.getLatLng().lng
		]);
		console.log("start dragging index", draggedIndex);
	});
	marker.on("drag", (evt) => {
		dragMarker(draggedIndex, evt);
	});

	path.splice(newPathIndex, 0, clickedLocation);

	segment = L.polyline(
		path.slice(newPathIndex, newPathIndex + 2),
		{
			color: "#0000ff",
			bubblingMouseEvents: false
		}
	).addTo(map);
	
	// Insert new segment after the clicked one
	// We now have to edit the segment at the current index
	segments.splice(segmentIndex + 1, 0, segment);

	segment.on("click", (evt) => {
		segmentClick(segments.indexOf(segment), evt);
	});

	// Now for segmentIndex = 1 we've
	// path     = [a,b,N,c,d,e]
	// segments = [[a,b], [b,c], [c,d], [d,e]]
	// segments = [[a,b], [b,N], [N,c], [c,d], [d,e]]
	
	// segments = [[a,b], [b,c], [c,d], [d,e]]
	// segments = [[a,N], {N,b}, [b,c], [c,d], [d,e]]


	// We've inserted at the previous segment's index, which now
	// moved left and used the succeeding location. Update the 
	// current segment index.

	preceedingSegment = segments[segmentIndex];
	preceedingSegment.setLatLngs(path.slice(newPathIndex - 1, newPathIndex + 1));
}


// Handle click on map to add maker and extend the path
map.on("click", (evt) => {
	let clickedLocation = [evt.latlng.lat, evt.latlng.lng],
		lastLocation,
		marker,
		segment,
		draggedIndex;

	console.log("clicked map: ", evt.latlng, evt);

	path.push(clickedLocation);

	// Add marker where clicked
	marker = L.marker(
		clickedLocation,
		{ draggable: true }
	).addTo(map);

	marker.on("dragstart", (dragStartEvt) => {
		draggedIndex = findLocationIndex([
			dragStartEvt.target.getLatLng().lat,
			dragStartEvt.target.getLatLng().lng
		]);
		console.log("start dragging index", draggedIndex);
	});
	marker.on("drag", (evt) => {
		dragMarker(draggedIndex, evt);
	});

	// We need at least two locations to create a segment
	if (path.length >= 2) {
		console.log("Segment incomplete. Complete it now...");
		// So complete it now ...

		segment = L.polyline(
			path.slice(path.length - 2, path.length),
			{
				color: "#0000ff",
				bubblingMouseEvents: false
			}
		).addTo(map);
		
		segments.push(segment);

		segment.on("click", (evt) => {
			segmentClick(segments.indexOf(segment), evt);
		});
	}
});
