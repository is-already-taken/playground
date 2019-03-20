
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

	console.log("clicked map: ", evt.latlng, evt.sourceTarget === line, evt);

	marker = L.marker(
		[evt.latlng.lat, evt.latlng.lng],
		{ draggable: true }
	).addTo(map);

	marker.on("drag", dragMarker.bind(null, lineLocation));

	path.push(lineLocation);
	line.setLatLngs(path);
});
