import common;

struct quadTree{

    alias P2 = Point!2;

    // Member variables
    Node root;

    class Node{
        bool isLeaf;
        P2[] pointList;
        Node topLeft, topRight, bottomLeft, bottomRight;
        AABB!2 boundBox;

        this(P2[] points, AABB!2 bounds){
            this.boundBox = bounds;
            
            int threshold = 32; // This is the actual value I'm running with for the buckets
            // int threshold = 4; // This was my test value for my unittest blocks
            if(points.length < threshold){ // Leaf node
                this.isLeaf = true;
                this.pointList = points;

            }else{ // Internal Node
                this.isLeaf = false;

                // Generate the split values x = 0, y = 1
                auto splitValues = (this.boundBox.max + this.boundBox.min)/2;

                // Partition on x to make two halves
                auto pointsRightHalf = points.partitionByDimension!0(splitValues[0]);
                auto pointsLeftHalf = points[0 .. $ - pointsRightHalf.length];

                // Partition those halves on y to make four quadrants
                auto pointsTopRight = pointsRightHalf.partitionByDimension!1(splitValues[1]);
                auto pointsBottomRight = pointsRightHalf[0 .. $ - pointsTopRight.length];
                auto pointsTopLeft = pointsLeftHalf.partitionByDimension!1(splitValues[1]);
                auto pointsBottomLeft = pointsLeftHalf[0 .. $ - pointsTopLeft.length];
                
                // Build nodes
                this.topLeft = new Node(pointsTopLeft,boundingBox(pointsTopLeft));
                this.topRight = new Node(pointsTopRight,boundingBox(pointsTopRight));
                this.bottomLeft = new Node(pointsBottomLeft,boundingBox(pointsBottomLeft));
                this.bottomRight = new Node(pointsBottomRight,boundingBox(pointsBottomRight));
                
            }
        }
    }

    this(P2[] points){
        this.root = new Node(points,boundingBox(points));
    }

    P2[] rangeQuery(P2 point, float rad){
        P2[] ret = new P2[0];

        void recurseRange(Node n){
            if(n.isLeaf){ // Leaf node
                foreach(p; n.pointList){
                    if(distance(point,p) < rad){
                        ret ~= p;
                    }
                }
            }else{ // Internal node
                float xLim = clamp(point[0],n.boundBox.min[0],n.boundBox.max[0]);
                float yLim = clamp(point[1],n.boundBox.min[1],n.boundBox.max[1]);
                P2 nearestPoint = P2([xLim,yLim]);
                if(distance(point,nearestPoint) < rad){ // check if the bounding box is in range
                    recurseRange(n.topLeft);
                    recurseRange(n.topRight);
                    recurseRange(n.bottomLeft);
                    recurseRange(n.bottomRight);
                }
            }
        }

        recurseRange(this.root);
        return ret;
    }

    auto knnQuery(P2 point, int k){
        auto ret = makePriorityQueue(point);

        // Recursive KNN Method
        void recursiveKNN(Node n){
            if(n.isLeaf){
                foreach(p; n.pointList){
                    if(ret.length < k){
                        ret.insert(p);

                    }else if(ret.length == k && distance(point,p) < distance(point,ret.front)){
                        ret.popFront; 
                        ret.insert(p); 
                    }
                }
            }else{
                recursiveKNN(n.topLeft);
                recursiveKNN(n.topRight);
                recursiveKNN(n.bottomLeft);
                recursiveKNN(n.bottomRight);
            }
        }

        recursiveKNN(this.root);
        return ret;
    }

}

unittest{
    writeln("\n\nQuad Tree Tests\n");
    
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

    quadTree quadTest = quadTree(points);

    auto quadRange1 = quadTest.rangeQuery(Point!2([1,3]),2.5);
    writeln("For [1,3] in range 2.5 => ",quadRange1);
    auto quadRange2 = quadTest.rangeQuery(Point!2([1,-3]),4);
    writeln("For [1,-3] in range 4 => ",quadRange2);
    writeln("--------");
    auto quadKNN1 = quadTest.knnQuery(Point!2([0,0]),5);
    writeln("For [0,0] => 5 Closest points = ",quadKNN1);
    auto quadKNN2 = quadTest.knnQuery(Point!2([2,-1]),9);
    writeln("For [2,-1] => 9 Closest points = ",quadKNN2);
}