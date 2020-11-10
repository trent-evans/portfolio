//
// Created by Trent Evans on 2/3/20.
//

#include "generateTests.h"

/** Forward declarations **/
std::string generateRandomNumber();
std::string generateRandomVariable();
std::string generateRandomBoolean();
std::string generateRandomParenthesis(int numOfVariables);
std::string generateRandomAdd(int numOfVariables);
std::string generateRandomMult(int numOfVariables);
std::string generateRandomEquals();
std::string generateRandomLet();
std::string generateRandomIf();
std::string generateRandomFun();
std::string generateComplicatedOptimizeTest();
std::string generateInvalidParenthesis();


/** Implementations **/
std::string generateRandomNumber(){
    int upperBound = 20; // Control over the upper bound of the value because we don't want to create too large of numbers
    return std::to_string(rand() % upperBound);
}

std::string generateRandomVariable(){ // Only produces single letter variables, but will speed the process
    std::string alphabet[] = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    return alphabet[rand() % 26];
}

std::string generateRandomBoolean(){
    if(rand()%2 == 1){ // Half the time it will give true
        return(" _true ");
    }else{ // Half the time it will give false
        return(" _false ");
    }
}

std::string generateRandomParenthesis(int numOfVariables){
    return "(" + generateRandomAdd(numOfVariables) + ")";
}

std::string generateRandomAdd(int numOfVariables){
    if(numOfVariables == 0){
        return generateRandomNumber() + " + " + generateRandomNumber();
    }else if(numOfVariables == 1){
        return generateRandomVariable() + " + " + generateRandomNumber();
    }else{
        return generateRandomVariable() + " + " + generateRandomVariable();
    }
}

std::string generateRandomMult(int numOfVariables){
    if(numOfVariables == 0){
        return generateRandomNumber() + " * " + generateRandomNumber();
    }else if(numOfVariables == 1){
        return generateRandomVariable() + " * " + generateRandomNumber();
    }else{
        return generateRandomVariable() + " * " + generateRandomVariable();
    }
}

std::string generateRandomEquals(){
    std::string newRandNum = generateRandomNumber();
    if(rand()%2 == 1){ // Return a true equals statement
        return newRandNum + " == " + newRandNum;
    }// Return a false equals statement
    return newRandNum + " == " + generateRandomNumber();
}

std::string generateRandomLet(){
    std::string var = generateRandomVariable();
    if(rand()%2 == 1) { // Generate a _let with addition
        return (" _let " + var + " = " + generateRandomNumber() + " _in " + var + " + " + generateRandomNumber());
    } // Generate a _let with multiplication
    return (" _let " + var + " = " + generateRandomNumber() + " _in " + var + " * " + generateRandomNumber());
}

std::string generateRandomIf(){
    if(rand()%2 == 1) {
        return " _if " + generateRandomBoolean() + " _then " + generateRandomNumber() + " _else " + generateRandomNumber();
    }
    return " _if " + generateRandomEquals() + " _then " + generateRandomNumber() + " _else " + generateRandomNumber();
}

std::string generateRandomFun(){
    std::string dependentVar = generateRandomVariable();
    if(rand()%2 == 1) {
        return " _fun (" + dependentVar + ") " + dependentVar + " + " + generateRandomNumber();
    }
    return " _fun (" + dependentVar + ") " + dependentVar + " * " + generateRandomNumber();
}

std::string generateComplicatedOptimizeTest(){
    std::string dependentVar = generateRandomVariable();
    if(rand()%3 == 0) {
        return  (" _let " + dependentVar + " = " + generateRandomIf() + " _in (" + generateRandomAdd(0) + ") *" +
                dependentVar);
    }else if(rand()%3 == 1){
        return  (" _let " + dependentVar + " = " + generateRandomIf() + " _in (" + generateRandomAdd(1) + ") *" +
                 dependentVar);
    }else{
        return  (" _let " + dependentVar + " = " + generateRandomIf() + " _in (" + generateRandomAdd(2) + ") *" +
                 dependentVar);
    }
}

std::string generateInvalidParenthesis(){
    return (generateComplicatedOptimizeTest() + " + " + " (" + generateRandomParenthesis(0));
}


