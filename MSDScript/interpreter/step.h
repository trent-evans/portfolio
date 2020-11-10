//
// Created by Trent Evans on 3/19/20.
//

#ifndef ARITHMETICINTERPRETER_STEP_H
#define ARITHMETICINTERPRETER_STEP_H

#include "pointer.h"
#include "env.h"

class Expr;
class Env;
class Val;
class Cont;

class Step ENABLE_THIS(Step){
public:
    // Define mode_t
    typedef enum{
        interp_mode,
        continue_mode
    } mode_t;

    // Member variables
    static mode_t mode;
    static PTR(Expr) expr;
    static PTR(Env) env;
    static PTR(Val) val;
    static PTR(Cont) cont;

    // One method to rule them all
    /**
     * interp_by_steps returns the same value as a normal call to interp() would, except without overwhelming the stack
     * space
     * @param e - The parsed expression tree to be interpreted
     * @return a Val object containing the interpreted value of the expression tree
     */
    PTR(Val) static interp_by_steps(PTR(Expr) e);
};


#endif //ARITHMETICINTERPRETER_STEP_H
