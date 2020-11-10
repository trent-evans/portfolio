import common;

struct bucketKNN(size_t Dim){

    alias PT = Point!Dim;
    alias Bucket = Point!Dim[];
    alias ID = Indices!Dim;

    private Bucket[] buckets;
    private PT cellSize;
    private PT bottomLeftCorner;
    private int numSplits;
    private int numBuckets;

    this(PT[] points, int numSplits){

        auto boundBox = boundingBox(points);
        this.cellSize = (boundBox.max - boundBox.min)/numSplits;
        this.bottomLeftCorner = boundBox.min;
        this.numSplits = numSplits;
        this.numBuckets = pow(numSplits,cast(int)(Dim));
        this.buckets = new Bucket[numBuckets];
 
        foreach(p; points){
            int bucketIdx = getBucketIndex(whichBucket(p));
            buckets[bucketIdx] ~= p;
        }
        
    }

    int getBucketIndex(ID point){
        int idx = 0;
        for(int x = 0; x < Dim; x++){
            idx += clamp(cast(int)(point[x]*pow(numSplits,Dim-x-1)),0,(numBuckets-1));
        }
        return idx;
    }

    ID whichBucket(PT point){
        ID idx;
        for(int x = 0; x < Dim; x++){
            idx[x] = cast(int)(clamp((point[x] - bottomLeftCorner[x])/cellSize[x],0,numSplits-1));
        }
        return idx;
    }

    Point!Dim indicesToPoint(ID idx){
        Point!Dim ret;
        for(int x = 0; x < Dim; x++){
            ret[x] = idx[x];
        }
        return ret;
    }

    Point!Dim[] rangeQuery(PT point, float radius){
        Point!Dim[] ret = new Point!Dim[0];

        auto bottomLeftIdx = whichBucket(point - radius);
        auto topRightIdx = whichBucket(point + radius);
        auto indexList = getIndicesRange!Dim(bottomLeftIdx,topRightIdx);
    
        foreach(idx; indexList){
            int bucketIdx = getBucketIndex(idx);
        
            if(bucketIdx < numBuckets){ // Avoiding imaginary buckets, were it to happen
                foreach(p; buckets[bucketIdx]){
                    if(distance(point,p) < radius){
                        ret ~= p;
                    }
                }

            }else{
                continue;
            }
        }

        return ret;
    }

    Point!Dim[] KNNQuery(PT point, int K){
        float rad = 0;
        for(int x = 0; x < Dim; x++){
            rad += cellSize[x];
        }
        rad = rad/Dim;
        Point!Dim[] ret = rangeQuery(point, rad);

        // If we get less than K points, retry until we get K points
        while(ret.length < K){
            rad *= 1.2;
            ret = rangeQuery(point,rad);
        }

        // If we get more than K points, chop off the furthest points
        if(ret.length > K){
            topNByDistance(ret, point, K);
        }

        return ret;
    }

}

unittest{

    writeln("\nBucket KNN Tests");

    auto points = [Point!2([-5,-5]),
                    Point!2([-4,-3]),
                    Point!2([-1,-4]),
                    Point!2([-2,-1]),
                    Point!2([-3,1]),
                    Point!2([-2,2]),
                    Point!2([-5,3]),
                    Point!2([-3,4]),
                    Point!2([-1,4]),
                    Point!2([2,-1]),
                    Point!2([4,-1]),
                    Point!2([3,-3]),
                    Point!2([4,-4]),
                    Point!2([1,1]),
                    Point!2([3,2]),
                    Point!2([2,3]),
                    Point!2([4,5])];

    auto bucketTest = bucketKNN!2(points,3);

    // Make sure that all the points are going into the buckets
    // foreach(b; bucketTest.buckets){
    //     writeln(b);
    // }
    
    writeln("\n");

    auto idx1 = bucketTest.whichBucket(Point!2([-5,-5]));
    writeln("[-5,-5] => Index ",idx1);
    auto idx2 = bucketTest.whichBucket(Point!2([-3,1]));
    writeln("[-3,1] => Index ",idx2);
    auto idx3 = bucketTest.whichBucket(Point!2([3,-3]));
    writeln("[3,-3] => Index ",idx3);
    auto idx4 = bucketTest.whichBucket(Point!2([3,2]));
    writeln("[3,2] => Index ",idx4);
    writeln("--------");

    Point!2[] rangeTest = bucketTest.rangeQuery(Point!2([1,3]),2.5);
    writeln("For [1,3] in range 2.5 => ",rangeTest);

    Point!2[] rangeTest2 = bucketTest.rangeQuery(Point!2([1,-3]),4);
    writeln("For [1,-3] in range 4 => ",rangeTest2);
    writeln("--------");
    Point!2[] knnTest1 = bucketTest.KNNQuery(Point!2([0,0]),5);
    writeln("For [0,0] => 5 Closest Points = ",knnTest1);
    assert(knnTest1.length == 5); // Double check the length

    Point!2[] knnTest2 = bucketTest.KNNQuery(Point!2([2,-1]),9);
    writeln("For [2,-1] => 9 Closest Points = ",knnTest2);
    assert(knnTest2.length == 9);
}