//
// Created by Trent Evans on 3/19/20.
//

#include "step.h"
#include "cont.h"
#include "env.h"
#include "expr.h"

Step::mode_t Step::mode;
PTR(Expr) Step::expr;
PTR(Env) Step::env;
PTR(Val) Step::val;
PTR(Cont) Step::cont;

PTR(Val) Step::interp_by_steps(PTR(Expr) e){
    Step::mode = Step::interp_mode;
    Step::expr = e;
    Step::env = NEW(EmptyEnv)();
    Step::val = nullptr;
    Step::cont = Cont::done;

    while(true){
        if(Step::mode == Step::interp_mode){
            Step::expr->step_interp();
        }else{
            if(Step::cont == Cont::done){
                return Step::val;
            }else{
                Step::cont->step_continue();
            }
        }
    }
}
