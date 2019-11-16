

# Macro algorithm

1. Detect cards
   1. Apply Canny
   2. Find contours
   3. Filter contours for proper aspect ratio bounding boxes
   
2. Check if the crads are plausible
   1. Number of cards
   2. Whether they're aligned in a grid

3. Iterate over card rects and run card anaylsis


## Card analysis

1. Extract shape area
   1. Shrink card card bounding box to exclude its edges
   2. Extract sub image of card from color image
   3. Convert to grayscale
   4. Apply blur to create blob of the shape of interest
   5. Apply threshold to determine the region of the shape
   6. Find contours

2. Determine shape count
   1. Extract bounding box of shapes "blob"
   2. Extract sub image of shape/shapes area (from card's grayscale)
   3. Apply threshold to determine shapes
   4. Find contours
   5. Extract bounding boxes of shapes
   6. Reject if shape count is not [1..3]
   7. Return arbitrary contour

3. Determine shape
   1. Declare a list of contours of all three shape contours
   2. Scale contour to match bounding box of the known shapes (necessary?)
   3. Match contour against list of known shapes shapes
   4. Return shape identifier of the best match

4. Determine fill
   1. Apply threshold (~160)
   2. Calculate histogram
   3. Return fill identifier depending on the histogram
      * low-key = filled
      * low/high-key = pattern
      * high-key = empty

5. Determine color
   1. Extract sub image of card (step 1) from color image
      * use the bounding box of one arbitrary contour (step 2)
   2. Calculate RGB histogram 
   3. Return color identifier depending on RGB-mean exceeding a threshold


# Resources

https://docs.opencv.org/master/d2/d96/tutorial_py_table_of_contents_imgproc.html
