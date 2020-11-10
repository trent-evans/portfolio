import common;

struct kdTree(size_t Dim){

    alias PT = Point!Dim;

    Node!0 root;

    class Node(size_t splitDimension){
        enum thisLevel = splitDimension;
        enum nextLevel = (splitDimension + 1) % Dim;
        Node!nextLevel left, right;
        PT splitPoint;

        this(PT[] points, size_t level){ 
            points.medianByDimension!thisLevel;

            if(points.length > 2){ // If there are at least 3 points
                int splitIdx = cast(int)((points.length)/2);
                this.splitPoint = points[splitIdx]; // Pull the middle array element

                PT[] leftHalfPoints  = points[0 .. splitIdx]; // splitIdx NOT inclusive
                PT[] rightHalfPoints = points[splitIdx+1 .. $];

                if(leftHalfPoints.length != 0){
                    this.left = new Node!nextLevel(leftHalfPoints,this.nextLevel);
                    
                }
                if(rightHalfPoints.length != 0){
                    this.right = new Node!nextLevel(rightHalfPoints,this.nextLevel);
                }

            }else if(points.length == 2){ // If there are only two points, create a right child
                this.splitPoint = points[0];
                this.right = new Node!nextLevel(points[1 .. $],this.nextLevel);

            }else{ // If there is only one point, set the split and be done
                this.splitPoint = points[0];
            }
        }
    }

    this(Point!Dim[] points){
        this.root = new Node!0(points, 0);
        writeln(this.root.splitPoint);
    }
    
    PT[] rangeQuery(PT point, float rad){
        PT[] ret = new PT[0];

        void recursiveRange(size_t splitDimension)(Node!splitDimension node){
            if(distance(point,node.splitPoint) < rad){
                ret ~= node.splitPoint;
            }
            if(point[node.thisLevel] - rad <= node.splitPoint[node.thisLevel] && node.left){
                recursiveRange(node.left);
            }
            if(point[node.thisLevel] + rad >= node.splitPoint[node.thisLevel] && node.right){
                recursiveRange(node.right);
            }
        }

        recursiveRange(this.root);
        return ret;
    }

    auto knnQuery(PT point, int k){
        auto ret = makePriorityQueue(point);

        void recursiveKNN(size_t splitDimension)(Node!splitDimension node, AABB!Dim bounds){

            if(ret.length < k){
                ret.insert(node.splitPoint);
            }else if(ret.length == k && (distance(point,node.splitPoint) < distance(point,ret.front))){
                ret.popFront;
                ret.insert(node.splitPoint);
            }

            float distToFurthest = distance(point,ret.front);
            
            if(node.left){ // Null check - Then proceed to the left child
                auto leftChildBound = bounds;
                leftChildBound.max[node.thisLevel] = node.splitPoint[node.thisLevel];
                if(ret.length < k || distance(closest(leftChildBound,point),point) < distToFurthest){
                    recursiveKNN(node.left, leftChildBound);
                }
            }

            if(node.right){ // Null check - Then proceed to the right child
                auto rightChildBound = bounds;
                rightChildBound.min[node.thisLevel] = node.splitPoint[node.thisLevel];
                if(ret.length < k || distance(closest(rightChildBound,point),point) < distToFurthest){
                    recursiveKNN(node.right, rightChildBound);
                }
            }
        }
 
        PT leftBound, rightBound;
        for(int x = 0; x < Dim; x++){
            leftBound[x] = -float.infinity;
            rightBound[x] = float.infinity;
        }
        recursiveKNN(this.root,boundingBox([leftBound,rightBound]));
        return ret;
    }
}


unittest{

    writeln("\n\nKD Tree Tests\n");
    
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

    auto kdTest = kdTree!2(points);

    auto kdRange1 = kdTest.rangeQuery(Point!2([1,3]),2.5);
    writeln("For [1,3] in range 2.5 => ",kdRange1);
    auto kdRange2 = kdTest.rangeQuery(Point!2([1,-3]),4);
    writeln("For [1,-3] in range 4 => ",kdRange2);
    writeln("--------");
    auto kdKNN1 = kdTest.knnQuery(Point!2([0,0]),5);
    writeln("For [0,0] => 5 Closest points = ",kdKNN1);
    auto kdKNN2 = kdTest.knnQuery(Point!2([2,-1]),9);
    writeln("For [2,-1] => 9 Closest points = ",kdKNN2);
}