//
// Created by Trent Evans on 3/19/20.
//

#ifndef ARITHMETICINTERPRETER_CONT_H
#define ARITHMETICINTERPRETER_CONT_H

#include <string>
#include "pointer.h"

class Expr;
class Env;
class Val;

class Cont ENABLE_THIS(Cont){
public:
    /**
     * step_continue resets each member variable of the global Step variable in order to let Step know how to proceed
     * with the next step of interpreting the expression tree
     * @return it is a void return, but it does make global changes behind the scenes
     */
    virtual void step_continue() = 0;

    static PTR(Cont) done;
};

class DoneCont : public Cont {
public:
    DoneCont();

    void step_continue();
};

class AddCont : public Cont{
public:
    PTR(Val) lhs_val;
    PTR(Cont) rest;

    AddCont(PTR(Val) lhs_val, PTR(Cont) rest);

    void step_continue();
};

class RightThenAddCont : public Cont{
public:
    PTR(Expr) rhs;
    PTR(Env) env;
    PTR(Cont) rest;

    RightThenAddCont(PTR(Expr) rhs, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};


class MultCont : public Cont{
public:
    PTR(Val) lhs_val;
    PTR(Cont) rest;

    MultCont(PTR(Val) lhs_val, PTR(Cont) rest);

    void step_continue();
};

class RightThenMultCont : public Cont{
public:
    PTR(Expr) rhs;
    PTR(Env) env;
    PTR(Cont) rest;

    RightThenMultCont(PTR(Expr) rhs, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};

class CompCont : public Cont{
public:
    PTR(Val) lhs_val;
    PTR(Cont) rest;

    CompCont(PTR(Val) lhs_val, PTR(Cont) rest);

    void step_continue();
};

class RightThenCompCont : public Cont{
public:
    PTR(Expr) rhs;
    PTR(Env) env;
    PTR(Cont) rest;

    RightThenCompCont(PTR(Expr) rhs, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};

class CallCont : public Cont{
public:
    PTR(Val) to_be_called_val;
    PTR(Cont) rest;

    CallCont(PTR(Val) to_be_called_val, PTR(Cont) rest);

    void step_continue();
};

class ArgThenCallCont : public Cont{
public:
    PTR(Expr) actual_arg;
    PTR(Env) env;
    PTR(Cont) rest;

    ArgThenCallCont(PTR(Expr) actual_arg, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};

class IfBranchCont : public Cont{
public:
    PTR(Expr) then_part;
    PTR(Expr) else_part;
    PTR(Env) env;
    PTR(Cont) rest;

    IfBranchCont(PTR(Expr) then_part, PTR(Expr) else_part, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};

class LetBodyCont : public Cont{
public:
    std::string var;
    PTR(Expr) body;
    PTR(Env) env;
    PTR(Cont) rest;

    LetBodyCont(std::string var, PTR(Expr) body, PTR(Env) env, PTR(Cont) rest);

    void step_continue();
};

#endif //ARITHMETICINTERPRETER_CONT_H