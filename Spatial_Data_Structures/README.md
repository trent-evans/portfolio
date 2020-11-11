# Spatial Partitioning Data Structures

## What I did
During my data science course during the summer of 2020 I learned about 3 different data structures used for partitioning spatial data: the [KD Tree](source/kdtree.d), the [Quad Tree](source/quadtree.d), and the [Bucket KNN](source/bucketknn.d).  These structures are used to perform KNN Queries (the closest K data points to a given data point) and Range Queries (all the data points within a given range of a given data point) on data.  While the KD Tree works only on data that is two-dimensional, the Quad Tree and Bucket KNN work on data of any dimension.  

## Why use [Dlang](dlang.org)?

Beyond just learning about these data structures I had the opportunity to implement them myself in [D](dlang.org) (or Dlang).  D is similar to C++ and Java, but it provides some major advantages when working with multi-dimentional data sets.  One beneficial feature is the use of compile-time parameters.  The compile-time parameters tell the Quad tree and Bucket KNN structures the dimension of the data being passed in without needing to initialize those values/lengths up front.  Althought it was a challenge, it was very fun and exciting to make these structures work.  I also performed [an analysis](analysis.ipynb) on the runtime of each structure in Python based on the number of training data points, the dimension of the data being partitioned, and other metrics.

## [Bucket KNN](source/bucketknn.d)
### How the data is split
The data split in the Bucket KNN is the easiest to conceptualize.  The uesr determines the number of splits they want per dimension.  The space is then split from the most minimal point to the most maximal point according to the number of splits.  Each section of the space is then defined as a bucket and all the points residing within that space are stored in that bucket as a list.  

### Range Query
To perform a range query with a Bucket KNN, we need a point and a radius.  From the point, we use the radius to determine which buckets are inside that range.  Then we loop through the points in each bucket to make sure they're within the range.  If the point is within the range, it is added to the list.  Once all this is done, the list is returned.

### KNN Query
For a KNN Query, we need a point and a K value to determine the number of points to pull.  Then, we simply perform a range query with a set starting radius.  If there aren't enough points to reach K, the radius is expanded and a range query is run again.  If there are more points than K, the points are ordered by distance from the original point and the list is cut to length K.  If the list is length K already, then we're good to go.

## [KD Tree](source/kdtree.d)
### How the data is split
The KD Tree is the tree structure built to handle multi-dimensional data.  Because the KD Tree is a tree structure, it requires its own internal Node class.  The KD Tree works by splitting the data along the midpoint of the data in the current dimension.  As such, each Node contains the dimension that the points are split upon (0,1,2,3,etc.) as well as the next dimension to be split, along with the midpoint of the data set that needs to be split. Beyond that, each Node also has two children Nodes representing each side of the split data.
<p>
The data is split by passing in a list of the data (in my implementation the data always begins by splitting on dimension 0 and increments by 1 for each split, looping back to 0 when it exceeds the dimension of the data).  The data is sorted according to the current split dimension.  The Node's split point is found by taking the median of the list according to the current dimension.  The lower half of the points and the next split dimension are passed to the left child and the upper half of the points and the next split dimension are passed to the right child.  Each child organizes their list of points based on the split dimension they're passed, splits based on the median, and passes the lists of points to the respective children along with the next split dimension.  This continues until the leaves are reached and there are no more points to organize.  

### Range Query
The KD Tree range query takes in a point and a range.  For each node visited if the distance between the query point and split point is less than the range then the split point is added to the return list.  To determine if the child points are within range, find the difference between the current split dimension of the query point and the split point.  If that difference is less than the radius, recurse down that branch.  

### KNN Query
The KD Tree KNN query takes in a point and a value.  The return list is created as a priority queue, automatically sorting input points based on their distance from the query point.  For each node hit, if the distance from the split point to the query point is less than the distance from the query point to the furthest point in the priority queue, then the split point replaces the furthest point in the priority queue.  
<p>
In order to recurse down the tree, begin with box that goes from negative to positive infinity in all of the dimensions.  For each node, change the split dimension of the infinite box with the value of the split point at that same dimension.  If the distance from the furthest point in the priority queue to the query point is greater than the distance from the (theoretical) closest point in the child node to the query point, then recurse down that child.  

## [Quad Tree](source/quadtree.d)
### How the data is split
The first thing to note is that Quad Trees only work on two-dimensional data due to the way that the data is split.  The Quad Tree is a little bit of a mix of the Bucket KNN and the KD Tree.  Just like any good tree strucutre, we make our own Node class.  Each Node contains four child nodes.  In my particular Node class I also added a boolean to indicate if the node is a leaf or not as well as a bounding box indicating the span of the points within that child. Finally, if the child is a leaf, it also contains a list of the points within the bounding box.
<p>
In order to separate the data, the area from the minimum point to the maximum point is split into four equally spaced quadrants (hence, the name quad tree).  If the number of points within that quadrant is at or below the threshold (which is part of the speed of the Quad Tree, though not something I included in my analysis), then those points are bucketed in the point list within the Node and the Node is made a leaf node.  If there are more points than the threshold, the space containing those points is split into four quadrants again and the process continues until all of the points have been bucketed.

### Range Query
The Range Query for the Quad Tree requires a point from which to query and a radius.  For each node, we check to see if the nearest point in the bounding box is within the radius from the query point.  If it is, then we recurse down each child of the node.  If not, we stop recursing down that branch.  Once a leaf node is reached, we iterate through the points stored in that bucket and add them to the return list if they're within the range.  Once all the recursion is done, we return the list.

### KNN Query
The KNN Query for the Quad Tree requires a point and the number of points desired in the return list (K).  The return list is originally set up as a priority queue so that the points are ordered by their distance from the query point.  Because there is no range, we don't know how close each point is supposed to be to the query point.  So, we immediately recurse to the leaves and loop through each of the points in each leaf's bucket.  If there are less than K points within the return list then each point is added.  Once we arrive at K points, if the next point we look at is closer than the furthest point from the query point then the furthest point is popped from the list and replaced with the next point.  This continues until all points are checked and observed and the list contains K points.  

## Testing
Another nice feature of D is that it allows for unit test blocks to be added within each file which will run when the code
 is compiled (note the unittest functions at the bottom of each .d file).  <br>  Testing for these cases was simple.  I
 had a list of points that I copied and pasted into each .d file along with a set range and knn query (some adjustments
 had to be made in order to get each block to run, but the range and K values are the same between all tests).  I also had
 those points drawn out on a piece of graph paper that I could easily reference.  Once the structure produced the correct
 output for each query, I knew the solution was correct.  Beyond that, I compared the results of each structure query to
 the other structure queries in order to make sure that they same answer came up all around.