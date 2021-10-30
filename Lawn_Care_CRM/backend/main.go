package main

import (
	"fmt"
	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
	"github.com/rs/cors"
	"log"
	"net/http"
)

func handleRequests(){

	myRouter := mux.NewRouter().StrictSlash(true)

	myRouter.HandleFunc("/land_details",getLandscaperDetails)
	myRouter.HandleFunc("/customer/{id}",getCustomerDetail)
	myRouter.HandleFunc("/customer_prop/{id}",getCustomerProperties)
	myRouter.HandleFunc("/customer_all",getAllCustomers)
	myRouter.HandleFunc("/loc_id_by_cus_id/{id}",getLocIdsByCustomer)
	myRouter.HandleFunc("/getLocationDetail/{id}",getLocationById)
	myRouter.HandleFunc("/schedule/{id}",getWeeklySchedule)
	myRouter.HandleFunc("/bills",generateBills)
	myRouter.HandleFunc("/postPayment",postPayment).Methods("POST")
	myRouter.HandleFunc("/completeJob",completeJob).Methods("POST")
	myRouter.HandleFunc("/getOutstandingPayments/{id}",getPaymentDueForAllCustomers)
	myRouter.HandleFunc("/customer_payment/{id}",getPaymentForCustomerId)
	// Test functions.  I never use them except for tests
	myRouter.HandleFunc("/dateTest",dateTest)
	myRouter.HandleFunc("/jobTest/{id}",jobTest)


	handler := cors.Default().Handler(myRouter)
	log.Fatal(http.ListenAndServe(":8080",handler))
}

func main() {

	// Open and ping the database to make sure that the connection is open
	if !checkDBConnection() {
		fmt.Println("Database connection failed.  Shutting down the program")
		return
	}
	if globalDB == nil{
		fmt.Println("Not able to connect to the database.  Shutting down the program")
		return
	}
	// Checking to make sure that the global db exists and can be accessed
	err := globalDB.Ping()
	if err != nil{
		//panic(err)
	}

	// Located in data.go - complete now, so no need to run these every time (though neither will generate new entries because of error handling)
	//readInMyData("customerInfo.xlsx","Customer List")
	//generateInitialMows("1")

	// Make fake data for the presentation
	//makeFakeCustomerJobs()

	handleRequests()
}
