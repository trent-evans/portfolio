# Spatial Partitioning Data Structures

## What I did
During my data science course during the summer of 2020 I learned about 3 different data structures used for partitioning spatial data: the KD Tree, the Quad Tree, and the Bucket KNN.  These structures are used to perform KNN Queries (the closest K data points to a given data point) and Range Queries (all the data points within a given range of a given data point) on data.  While the KD Tree works only on data that is two-dimensional, the Quad Tree and Bucket KNN work on data of any dimension.  

## Why use Dlang?

Beyond just learning about these data structures I had the opportunity to implement them myself in [D](dlang.org) (or Dlang).  D is similar to C++ and Java, but it provides some major advantages when working with multi-dimentional data sets.  One beneficial feature is the use of compile-time parameters.  The compile-time parameters tell the Quad tree and Bucket KNN structures the dimension of the data being passed in without needing to initialize those values/lengths up front.  Althought it was a challenge, it was very fun and exciting to make these structures work.  I also performed an analysis on the runtime of each structure in Python based on the number of training data points, the dimension of the data being partitioned, and other metrics.

## What each structure is

Spatial partitioning data structures do some really cool things, but they're not necessarily widely known.  
