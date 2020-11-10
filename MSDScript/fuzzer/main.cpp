#include <iostream>
#include "exec.h"
#include "generateTests.h"
#include <string>
#include <cstdlib>

static void check_success(ExecResult &r);
static void check_interp_or_opt_failure(ExecResult &result);
static void check_parse_failure(ExecResult &result);

int main(int argc, const char * argv[]) {

    // Start off with the random number seed to randomize everything downstream
    srand(time(NULL));
    int numberOfTestsPerGrammar = 10; // Number of tests we want to run per possible grammar set
    int currentNumberOfGrammarOptions = 10; // How many grammar entries we have, currently 10
    int currentNumberOfOptGrammarOptions = 10;
    int currentNumberOfComplicatedOptTests = 20;
    int currentNumberOfParseErrorTests = 10;

    for(int y = 0; y < 2; y ++) {
        const char *mathParseFD[] =
                {"/Users/trentevans/TrentEvans/CS6015_Software_Engineering/arithmeticInterpreter/msdscript", "--opt",
                 NULL};
        if(y == 0){
            mathParseFD[1] = NULL;
        }

        if (argc > 1) {
            mathParseFD[0] = argv[1];
        } else {
            std::cerr << "Enter a valid filepath\n\n";
//            return 1;
        }

        bool allTestsPassed = true;
        int numberOfTestsFailed = 0;
        int totalTests;
        if(y == 0){
            totalTests = numberOfTestsPerGrammar * currentNumberOfGrammarOptions;
        }else{
            totalTests = numberOfTestsPerGrammar * currentNumberOfOptGrammarOptions;
        }

        for (int x = 0; x < totalTests; x++) {
            std::string test;
            if (x < numberOfTestsPerGrammar) {
                test = generateRandomNumber();
            } else if (x < numberOfTestsPerGrammar * 2) {
                test = generateRandomVariable();
            } else if (x < numberOfTestsPerGrammar * 3) {
                test = generateRandomBoolean();
            } else if (x < numberOfTestsPerGrammar * 4) {
                test = generateRandomAdd(0);
            } else if (x < numberOfTestsPerGrammar * 5) {
                test = generateRandomMult(0);
            } else if (x < numberOfTestsPerGrammar * 6) {
                test = generateRandomParenthesis(0);
            } else if (x < numberOfTestsPerGrammar * 7) {
                test = generateRandomIf();
            } else if (x < numberOfTestsPerGrammar * 8) {
                test = generateRandomLet();
            } else if (x < numberOfTestsPerGrammar * 9) {
                test = generateRandomFun();
            } else {
                test = generateRandomEquals();
            }
            ExecResult result = exec_program(mathParseFD, test);

            // If we just try to pass a variable, there should be a problem because we can't interp a variable
            if (mathParseFD[1] == NULL && x >= numberOfTestsPerGrammar && x < numberOfTestsPerGrammar * 2) {
                check_interp_or_opt_failure(result);
                if (result.exit_code != 2) {
                    std::cout << test << std::endl;
                    allTestsPassed = false;
                    numberOfTestsFailed++;
                }
            } else { // Otherwise, everything else should be interp-able and optimize_able
                check_success(result);
                if (result.exit_code != 0) {
                    std::cout << test << std::endl;
                    allTestsPassed = false;
                    numberOfTestsFailed++;
                }
            }

        }
        std::string testType;
        if(y == 0){
            testType = "Interpret";
        }else{
            testType = "Optimize";
        }

        if (allTestsPassed) {
            std::cout << "All " << testType << " tests passed! (" << totalTests << ")"
                      << std::endl;
        } else {
            std::cout << "Not all " << testType << " tests passed: " << std::endl << numberOfTestsFailed << " tests failed of "
                      << totalTests << std::endl;
        }
    }

    /** More optimize tests - but more complicated **/

    const char *mathParseOptFD[] =
            {"/Users/trentevans/TrentEvans/CS6015_Software_Engineering/arithmeticInterpreter/msdscript", "--opt",
             NULL};

    bool allComplicatedOptTestsPassed = true;
    int numberComplicatedOfOptTestsFailed = 0;
    int totalTests = numberOfTestsPerGrammar * currentNumberOfComplicatedOptTests;

    for(int x = 0; x < totalTests; x++){
        std::string test = generateComplicatedOptimizeTest();
        ExecResult result = exec_program(mathParseOptFD,test);
        check_success(result);
        if(result.exit_code != 0){
            allComplicatedOptTestsPassed = false;
            currentNumberOfComplicatedOptTests++;
        }
    }

    if(allComplicatedOptTestsPassed){
        std::cout << "All complicated optimize tests passed! (" << totalTests << ")" << std::endl;
    }else{
        std::cout << "Not all complicated optimize tests passed: " << std::endl << numberComplicatedOfOptTestsFailed << " tests failed of "
                  << totalTests << std::endl;
    }


    /** Parse error testing **/ // Still a little in development
    /*
    bool allParseErrorTestsFailed = true;
    int numberOfParseErrorTestsPassed = 0;

    for(int x = 0; x < numberOfTestsPerGrammar * currentNumberOfParseErrorTests; x++){
        std::string test = generateInvalidParenthesis();
        ExecResult interpResult = exec_program(mathParseFD,test);
        ExecResult optResult = exec_program(mathParseOptFD,test);
        check_parse_failure(interpResult);
        check_parse_failure(optResult);
        if(interpResult.exit_code != 1 || optResult.exit_code != 1){
            allParseErrorTestsFailed = false;
            numberOfParseErrorTestsPassed++;
        }
    }

    if(allParseErrorTestsFailed){
        std::cout << "All parsing errors caught! (" << numberOfTestsPerGrammar * currentNumberOfParseErrorTests << ")" << std::endl;
    }else{
        std::cout << "Not all parse errors caught: " << std::endl << numberOfParseErrorTestsPassed << " errors not caught of "
                  << numberOfTestsPerGrammar * currentNumberOfParseErrorTests << std::endl;
    }
     */

    return 0;
}

static void check_success(ExecResult &result) {
    std::cerr << result.err;
    if (result.exit_code != 0)
        std::cerr << "non-zero exit: " << result.exit_code << "\n";
}

static void check_interp_or_opt_failure(ExecResult &result) {
    std::cout << result.out;
    if (result.exit_code != 2)
        std::cerr << "non-2 exit: " << result.exit_code << "\n";
}

static void check_parse_failure(ExecResult &result){
    std::cout << result.err;
    if(result.exit_code != 1){
        std::cerr << "non-1 exit on a parse error: " << result.exit_code << "\n";
    }
}