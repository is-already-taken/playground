
/* 
 * Demo demonstating basic usage.
 */

const latLngs = [
	[47.4132405,15.2797053],
	[47.4110544,15.2815932]
];

let path = [];

var map = L.map("map").setView(latLngs[0], 13);

L.tileLayer("https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}", {
	attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
	maxZoom: 18,
	id: "mapbox.streets",
	accessToken: MAPBOX_ACCESS_TOKEN
}).addTo(map);
