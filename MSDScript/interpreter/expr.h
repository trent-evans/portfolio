//
// Created by Trent Evans on 1/21/20.
//

#ifndef BETTERMATHPARSER_EXPR_H
#define BETTERMATHPARSER_EXPR_H


#include <string>
#include "pointer.h"

class Val;
class Cont;
class Step;
class Env;

class Expr ENABLE_THIS(Expr){
public:

    /**
     * equals compares to Expr objects to see if the values are equal to one another
     * @param e - The Expr to compare with "this"
     * @return - Boolean true or false depending if the input Expr (e) is equal to "this" or not
     */
    virtual bool equals(PTR(Expr) e) = 0;

    /**
     * interp attempts to interpret the "this" Expr using the given environment and return a single value from an Expr
     * @param env - The environment that contains the relevant information for substituting variables
     * @return a single Val containing the interpreted form of the Expr
     */
    virtual PTR(Val) interp(PTR(Env) env) = 0;

   /**
    * substitute attempts to substitute a value in for the given variable name if it is present in "this" Expr
    * @param varName - String of the variable name to be substituted in
    * @param subsVal - A Val containing the value to be substituted in
    * @return "this" with subsVal substituted in where the variables are equal to varName
    */
    virtual PTR(Expr) substitute(std::string varName, PTR(Val) subsVal) = 0;

    /**
     * containsVariable looks through "this" and determines whether a VarExpr is present within "this" Expr
     * @return Boolean true or false depending if a VarExpr is present within the "this" or not
     */
    virtual bool containsVariable() = 0;

    /**
     * optimize returns the simplest possible form of an Expr and will condense the Expr where possible
     * @return an Expr that is as simple as possible given "this" input
     */
    virtual PTR(Expr) optimize() = 0;

    /**
     * printToString returns a String representation of "this" Expr
     * @return a String representation of "this" Expr
     */
    virtual std::string printToString() = 0;

    /**
     * step_interp works the same as interp() in that it attempts to condense the Expr down to a single value
     * It does return void because step_interp() operates on a global Step variable in order to not overwhelm the
     * stack.
     */
    virtual void step_interp() = 0;
};

class NumExpr : public Expr {
public:
    int val;

    NumExpr(int val);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class AddExpr : public Expr {
public:
    PTR(Expr) lhs;
    PTR(Expr) rhs;

    AddExpr(PTR(Expr) lhs, PTR(Expr) rhs);
    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class MultExpr : public Expr {
public:
    PTR(Expr) lhs;
    PTR(Expr) rhs;

    MultExpr(PTR(Expr) lhs, PTR(Expr) rhs);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class VarExpr : public Expr {
public:
    std::string var;

    VarExpr(std::string var);
    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class LetExpr : public Expr{
public:
    PTR(VarExpr) name;
    PTR(Expr) rhs;
    PTR(Expr) body;

    LetExpr(PTR(VarExpr) subsVariable, PTR(Expr) subsExpression, PTR(Expr) baseExpression);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};


class BoolExpr : public Expr{
public:
    bool rep;

    BoolExpr(bool rep);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class IfExpr : public Expr{
public:
    PTR(Expr) testExpr;
    PTR(Expr) thenExpr;
    PTR(Expr) elseExpr;

    IfExpr(PTR(Expr) testExpr, PTR(Expr) thenExpr, PTR(Expr) elseExpr);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class EqualsExpr : public Expr{
public:
    PTR(Expr) rhs;
    PTR(Expr) lhs;

    EqualsExpr(PTR(Expr) lhs, PTR(Expr) rhs);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class FunExpr : public Expr{
public:
    std::string formal_arg;
    PTR(Expr) body;

    FunExpr(std::string formal_arg, PTR(Expr) body);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};

class CallExpr : public Expr{
public:
    PTR(Expr) to_be_called;
    PTR(Expr) actual_arg;

    CallExpr(PTR(Expr) to_be_called, PTR(Expr) actual_arg);

    bool equals(PTR(Expr) e);
    PTR(Val) interp(PTR(Env) env);
    PTR(Expr) substitute(std::string varName, PTR(Val) subsVal);
    bool containsVariable();
    PTR(Expr) optimize();
    std::string printToString();
    void step_interp();
};



#endif //BETTERMATHPARSER_EXPR_H
