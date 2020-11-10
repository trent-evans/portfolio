//
// Created by Trent Evans on 2/1/20.
//

#ifndef TESTRUNNER_EXEC_H
#define TESTRUNNER_EXEC_H


#ifndef exec_hpp
#define exec_hpp

#include <string>

class ExecResult {
public:
    int exit_code;
    std::string out;
    std::string err;
    ExecResult() {
        exit_code = 0;
        out = "";
        err = "";
    }
};

extern ExecResult exec_program(const char * const *command, std::string input);

#endif /* exec_hpp */


#endif //TESTRUNNER_EXEC_H
