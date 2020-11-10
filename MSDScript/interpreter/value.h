//
// Created by Trent Evans on 2/4/20.
//

#pragma once
#include <string>
#include "pointer.h"
#include "step.h"

class Expr; // Forward declaration
class Env;
class Cont;

class Val ENABLE_THIS(Val){
public:
    /**
     * equals compares to Val objects to tell if they're equal or not
     * @param val - The Val to compare with "this"
     * @return Boolean true or false based on whether the two Val objects are equal or not
     */
    virtual bool equals(PTR(Val) val) = 0;
    /**
     * add_to attempts to add two NumVal objects together to create a single NumVal object when possible
     * @param other_val - The Val to add to "this" Val
     * @return a NumVal with a value equal to the sum of "this" and other_val
     */
    virtual PTR(Val) add_to(PTR(Val) other_val) = 0;
    /**
     * mult_with attempts to multiply two NumVal object together to create a single NumVal object when possible
     * @param other_val - The Val to multiply with "this" Val
     * @return a NumVal with a value equal to the product of "this" and other_val
     */
    virtual PTR(Val) mult_with(PTR(Val) other_val) = 0;
    /**
     * to_expr converts Vals to Exprs
     * @return an Expr that represents "this" Val
     */
    virtual PTR(Expr) to_expr() = 0;
    /**
     * printToString returns a string representation of "this" Val object
     * @return a std::string representation of "this
     */
    virtual std::string printToString() = 0;
    /**
     * is_true returns whether "this" evaluates to true or not (doesn't work if not a BoolVal)
     * @return Boolean true or false based on the BoolVal rep
     */
    virtual bool is_true() = 0;
    /**
     * call is the method that actually allows us to call functions.  It returns what the value of the function
     * would be when called with the input value
     * @param actual_arg - the input value to the function
     * @return a Val object that contains the value of the function with the input of the actual_arg
     */
    virtual PTR(Val) call(PTR(Val) actual_arg) = 0;
    /**
     * call_step is what allows us to call functions when in interpret by steps mode
     * @param actual_arg_val - the value of the actual argument to be called by the function
     * @param rest - the Cont that determines the continuation through the rest of the solution process
     * @return - there is no return because answers are managed via the global Step variable
     */
    virtual void call_step(PTR(Val) actual_arg_val, PTR(Cont) rest) = 0;
};

class NumVal : public Val {
public:
    int rep;

    NumVal(int rep);

    bool equals(PTR(Val) val);
    PTR(Val) add_to(PTR(Val) other_val);
    PTR(Val) mult_with(PTR(Val) other_val);
    PTR(Expr) to_expr();
    std::string printToString();
    bool is_true();
    PTR(Val) call(PTR(Val) actual_arg);
    void call_step(PTR(Val) actual_arg_val, PTR(Cont) rest);
};

class BoolVal : public Val {
public:
    bool rep;

    BoolVal(bool rep);

    bool equals(PTR(Val) val);
    PTR(Val) add_to(PTR(Val) other_val);
    PTR(Val) mult_with(PTR(Val) other_val);
    PTR(Expr) to_expr();
    std::string printToString();
    bool is_true();
    PTR(Val) call(PTR(Val) actual_arg);
    void call_step(PTR(Val) actual_arg_val, PTR(Cont) rest);
};

class FunVal : public Val {
public:
    std::string formal_arg;
    PTR(Expr) body;
    PTR(Env) env;

    FunVal(std::string formal_arg, PTR(Expr) body, PTR(Env) env);

    bool equals(PTR(Val) val);
    PTR(Val) add_to(PTR(Val) other_val);
    PTR(Val) mult_with(PTR(Val) other_val);
    PTR(Expr) to_expr();
    std::string printToString();
    bool is_true();
    PTR(Val) call(PTR(Val) actual_arg);
    void call_step(PTR(Val) actual_arg_val, PTR(Cont) rest);
};



