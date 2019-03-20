
/* 
 * Demo demonstating map/layer events and how to prevent them.
 */

const latLngs = [
	[47.4132405,15.2797053],
	[47.4110544,15.2815932]
];

var map = L.map("map").setView(latLngs[0], 13);

L.tileLayer("https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}", {
	attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
	maxZoom: 18,
	id: "mapbox.streets",
	accessToken: MAPBOX_ACCESS_TOKEN
}).addTo(map);

let line,
	marker,
	ignoreMapEvent = false;

line = L.polyline(latLngs, { color: "#9999ff" }).addTo(map);
marker = L.circleMarker([latLngs[1][0], latLngs[1][1]]).addTo(map);


map.on("click", (evt) => {
	if (ignoreMapEvent) {
		ignoreMapEvent = false;
		return;
	}

	console.log("clicked map: ", evt.latlng, evt);
});

marker.on("click", (evt) => {
	// Does not prevent the map event ...
	// evt.originalEvent.stopPropagation();

	// ... thus flagging the map click event to be ignored
	ignoreMapEvent = true;

	console.log("clicked marker: ", evt.latlng, evt);
});

