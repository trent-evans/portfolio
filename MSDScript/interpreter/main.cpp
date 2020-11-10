#include <iostream>
#include <sstream>

#define CATCH_CONFIG_RUNNER
#include "catch.hpp"
#include "step.h"
#include "cont.h"
#include "parse.h"
#include "expr.h"
#include "value.h"

int main(int argc, char** argv) {

    // To run tests
//    Catch::Session().run(argc, argv);


    // To do commandline stuff
    try {
        PTR(Expr) e;

        try { // Inner try/catch is to grab parsing errors
             e = parseInStream(std::cin);
        }catch(std::runtime_error parseErr){
            std::cerr << "Error - Error in parsing\n";
            return 1;
        }

        // Outer try/catch is grabbing optimize and interp errors
        if((argc > 1) && strcmp(argv[1], "--opt") == 0) {
            std::cout << e->optimize()->printToString() << std::endl;
        }else if((argc > 1) && strcmp(argv[1], "--step") == 0){
            std::cout << Step::interp_by_steps(e)->printToString() << std::endl;
        }else {
            std::cout << e->interp(NEW(EmptyEnv)())->printToString() << std::endl;
        }
        return 0;
    }catch(std::runtime_error runErr){
        std::cerr << "Error - invalid input\n";
        return 2;
    }

    return 0;
}
