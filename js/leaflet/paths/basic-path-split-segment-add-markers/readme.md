
# Basic path editing 

With splitting path segments (adding new points/markers between markers).


## Implementation

With the click on the path the clicked segment is determined by testing whether
the click location fits the bounding box of two succeeding points on the path.

This method is the simplest but fails for some path patterns (see below).


## Limitation

In case of figure of eight / ribbon patterns or nearby (parallel) paths  
multiple click locations (determined from the bounding box of two succeeding 
points) will be found.
