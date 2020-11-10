//
// Created by Trent Evans on 3/4/20.
//

#ifndef ARITHMETICINTERPRETER_ENV_H
#define ARITHMETICINTERPRETER_ENV_H

#include "pointer.h"
#include <string>

class Expr;
class Val;

class Env{
public:
    /**
     * lookup find the variable name inside the Env and then returns the value to be associated with that variable
     * @param find_name - the name of the variable to find
     * @return a Val object with the value associated with that variable
     */
    virtual PTR(Val) lookup(std::string find_name) = 0;
};

class EmptyEnv : public Env{
public:
    EmptyEnv();
    PTR(Val) lookup(std::string find_name);
};

class ExtendedEnv : public Env{
public:
    std::string name;
    PTR(Val) val;
    PTR(Env) rest;

    ExtendedEnv(std::string name, PTR(Val) val, PTR(Env) rest);

    PTR(Val) lookup(std::string find_name);
};

#endif //ARITHMETICINTERPRETER_ENV_H
