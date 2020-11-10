import std.stdio;
import std.csv;

import common;
import dumbknn;
import bucketknn;
import quadtree;
import kdtree;

void main()
{

    //because dim is a "compile time parameter" we have to use "static foreach"
    //to loop through all the dimensions we want to test.
    //the {{ are necessary because this block basically gets copy/pasted with
    //dim filled in with 1, 2, 3, ... 7.  The second set of { lets us reuse
    //variable names.
    // static foreach(dim; 1..8){{
    //     //get points of the appropriate dimension
    //     auto trainingPoints = getGaussianPoints!dim(1000);
    //     auto testingPoints = getUniformPoints!dim(100);
    //     auto kd = DumbKNN!dim(trainingPoints);
    //     writeln("tree of dimension ", dim, " built");
    //     StopWatch sw;
    //     sw.start;
    //     foreach(const ref qp; testingPoints){
    //         kd.knnQuery(qp, 10);
    //     }
    //     writeln(dim,",", sw.peek.total!"usecs"); //output the time elapsed in microseconds
    //     //NOTE, I SOMETIMES GOT TOTALLY BOGUS TIMES WHEN TESTING WITH DMD
    //     //WHEN YOU TEST WITH LDC, YOU SHOULD GET ACCURATE TIMING INFO...
    // }}
    
    // auto outFile = File("testing.csv","w");
    // outFile.writeln("structName,structType,K,N,Dim,avgRuntime");
    int numTests = 10;
    int maxPoints = 200000;
    int startPoints = 10000;
    int kStart = 10;
    int kMax = 100;
    int numBucketSplits = 50;
    {
        // Quad tree timing tests
        auto quadFile = File("quadTest.csv","w");
        quadFile.writeln("structName,structType,K,N,Dim,avgRuntime");
        for(int N = startPoints; N <= maxPoints; N += startPoints){
            auto quadTrainingPoints = getUniformPoints!2(N);
            auto quadTestingPoints  = getUniformPoints!2(numTests);
            auto quadTest = quadTree(quadTrainingPoints);
            StopWatch quadSw;
            quadSw.start;
            for(int K = kStart; K <= kMax; K += kStart){
                foreach(const ref p; quadTestingPoints){
                    quadTest.knnQuery(p,K);
                }
                long quadAvgUSecs = quadSw.peek.total!"usecs"/cast(long)numTests;
                quadFile.writeln("Quad",",",1,",",K,",",N,",",2,",",quadAvgUSecs);
            }
        }
        quadFile.flush;
        quadFile.close;
        writeln("Quad done");


        // KDTree timing tests
        auto kdFile = File("kdTest.csv","w");
        kdFile.writeln("structName,structType,K,N,Dim,avgRuntime");
        static foreach(kdDim; 1..8){
            writeln("KD Dimension: ",kdDim);
            for(int N = startPoints; N <= maxPoints; N += startPoints){
                auto kdTrainingPoints = getUniformPoints!kdDim(N);
                auto kdTestingPoints  = getUniformPoints!kdDim(numTests);
                auto kdTest = kdTree!kdDim(kdTrainingPoints);
                StopWatch kdSw;
                kdSw.start;
                for(int K = kStart; K <= kMax; K += kStart){
                    foreach(const ref p; kdTestingPoints){
                        kdTest.knnQuery(p,K);
                    }
                    long kdAvgUSecs = kdSw.peek.total!"usecs"/cast(long)numTests;
                    kdFile.writeln("KD",",",2,",",K,",",N,",",kdDim,",",kdAvgUSecs);
                }
            }
        }
        kdFile.flush;
        kdFile.close;
        writeln("KD Done");



        // BucketKNN Timing Tests 
        // - For some reason 5 dimensions was just too much for my computer to handle for the bucket
        auto bucketFile = File("bucketTest.csv","w");
        bucketFile.writeln("structName,structType,K,N,Dim,avgRuntime");
        static foreach(dim; 1..5){
            writeln("Bucket dimension: ",dim);
            for(int N = startPoints; N <= maxPoints; N += startPoints){
                auto bucketTrainingPoints = getUniformPoints!dim(N);
                auto bucketTestingPoints = getUniformPoints!dim(numTests);
                auto bucketTest = bucketKNN!dim(bucketTrainingPoints,numBucketSplits);
                StopWatch bucketSw;
                bucketSw.start;
                for(int K = kStart; K <=kMax; K += kStart){
                    foreach(const ref p; bucketTestingPoints){
                        bucketTest.KNNQuery(p,K);
                    }
                    long bucketAvgUSecs = bucketSw.peek.total!"usecs"/cast(long)numTests;
                    bucketFile.writeln("Bucket",",",0,",",K,",",N,",",dim,",",bucketAvgUSecs);
                }
                
            }
        }
        writeln("Bucket done");
        bucketFile.flush;
        bucketFile.close;
    }
    
    writeln("Timing complete - move to your analysis hot shot");
}




