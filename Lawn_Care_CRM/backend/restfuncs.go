package main

/**
	This file contains all of the rest function calls that can be made to the REST API
 */

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"io/ioutil"
	"net/http"
	"strconv"
	"time"
)

func getAllCustomers(w http.ResponseWriter, r *http.Request){
	landId := "1"
	fmt.Println("Endpoint hit: get all customers")

	var customers []CustomerMin

	sqlQuery := `SELECT customerfirstname, customerlastname, customerid FROM customer WHERE landscaperid=$1`

	rows, err := globalDB.Query(sqlQuery,landId)
	if err != nil {
		fmt.Println("Error retreiving information - GetAllCustomers:")
		fmt.Println(err)
	}

	for rows.Next() {
		var addCustomer CustomerMin
		err = rows.Scan(&addCustomer.FirstName, &addCustomer.LastName, &addCustomer.Id)
		if err != nil {
			fmt.Println("Issue scanning for customer information:")
			fmt.Println(err)
		}
		customers = append(customers,addCustomer)
	}

	err = rows.Err()
	if err != nil {
		panic(err)
	}
	json.NewEncoder(w).Encode(customers)
}

func getLandscaperDetails(w http.ResponseWriter, r *http.Request){
	fmt.Println("Endpoint hit:  Get Landscaper Details")
	landId := "1"
	sqlQuery := `SELECT landscapername, companyemail, companyphone, 
		streetaddress, city, state, zip FROM landscapers WHERE landscaperid=$1;`
	var landscaper Landscaper
	landscaper.Id = landId

	row := globalDB.QueryRow(sqlQuery,landId)
	err := row.Scan(&landscaper.Name,&landscaper.Email,&landscaper.Phone,&landscaper.Address,
		&landscaper.City,&landscaper.State,&landscaper.Zip)
	switch err {
	case sql.ErrNoRows:
		fmt.Fprintf(w,"No record found")
	case nil:
		json.NewEncoder(w).Encode(landscaper)
	default:
		fmt.Fprintf(w,"Something happened... not sure what\n")
	}
}

func getCustomerDetail(w http.ResponseWriter, r *http.Request){

	vars := mux.Vars(r)
	customerId := vars["id"]
	fmt.Println("Endpoint hit: get customer detail for id ",customerId)
	var customerDetail Customer
	customerDetail.Id = customerId

	sqlQuery := `SELECT customerfirstname, customerlastname, customeremail, 
		customeraddress, customercity, customerstate, customerzip, customerphone, 
		preferredbilltype FROM customer WHERE customerid=$1`
	row := globalDB.QueryRow(sqlQuery,customerId)
	err := row.Scan(&customerDetail.FirstName, &customerDetail.LastName, &customerDetail.Email,
		&customerDetail.Address, &customerDetail.City, &customerDetail.State, &customerDetail.Zip,
		&customerDetail.Phone, &customerDetail.BillType)
	switch err {
	case sql.ErrNoRows:
		fmt.Fprintf(w,"No record found")
	case nil:
		json.NewEncoder(w).Encode(customerDetail)
	default:
		fmt.Fprintf(w,"Is broke")
	}
}

func getLocationById(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	customerId := vars["id"]
	var location Location

	sqlQuery := `SELECT landscaperid, serviceaddress, servicecity, 
		servicestate, servicezip, daysbetween, preferredday, costpermow, 
		costperaerate, cutheight FROM locations WHERE serviceid=$1`
	row := globalDB.QueryRow(sqlQuery,customerId)
	err := row.Scan(&location.LandID, &location.Address, &location.City,
		&location.State, &location.Zip, &location.DaysBetween, &location.PreferredDay,
		&location.CostPerMow, &location.CostPerAerate, &location.CutHeight)
	switch err {
	case sql.ErrNoRows:
		fmt.Fprintf(w,"No record found")
	case nil:
		json.NewEncoder(w).Encode(location)
	default:
		fmt.Fprintf(w,"Is broke")
	}
}

func getLocIdsByCustomer(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	customerId := vars["id"]

	var locIdArray []LocId
	query := `SELECT serviceid FROM locations WHERE customerid = $1`
	rows, err := globalDB.Query(query,customerId)
	if err != nil {
		fmt.Println("Error pulling loc id #s - getLocIdsByCustomer")
		fmt.Println(err)
	}
	for rows.Next() {
		var idStruct LocId
		err = rows.Scan(&idStruct.Id)
		if err != nil {
			fmt.Println("Error scanning for loc id - getLocIdByCustomer")
			fmt.Println(err)
		}
		locIdArray = append(locIdArray,idStruct)
	}
	json.NewEncoder(w).Encode(locIdArray)
}

func getWeeklySchedule(w http.ResponseWriter, r *http.Request){
	fmt.Println("Endpoint hit: get Weekly schedule")

	landId := "1" // In an ideal world, I'd parse this out.  But I won't right now, because there's only one user for now
	now := time.Now()
	today := int(now.Weekday())
	mondayToToday := 1 - today // Monday - day of the week = days to subtract until Monday
	mondayTime := now.AddDate(0,0, mondayToToday)
	saturdayToToday := 6 - today // Saturday - day of the week = days to add until Saturday
	saturdayTime := now.AddDate(0,0,saturdayToToday)

	mondayPostgres := getPostresFriendlyDate(mondayTime)
	saturdayPostgres := getPostresFriendlyDate(saturdayTime)

	workQuery := `SELECT job_id, cus_id, complete, date_planned, job_name FROM jobs
				WHERE date_planned >= $1 AND date_planned <= $2 AND land_id = $3`
	rows, err := globalDB.Query(workQuery,mondayPostgres,saturdayPostgres,landId)
	if err != nil {
		fmt.Println("Error pulling jobs from between",mondayPostgres,"and",saturdayPostgres)
		fmt.Println(err)
	}
	var scheduleOut WeeklySchedule
	scheduleOut.DateFrom = makeDateHumanFriendly(mondayTime)
	scheduleOut.DateTo = makeDateHumanFriendly(saturdayTime)

	var week [6]time.Time
	week[0] = mondayTime
	for x := 1; x < 6; x++ {
		week[x] = week[x-1].AddDate(0,0,1)
	}

	for rows.Next(){
		var datePlan string
		var scheduleJob ScheduleJobs
		var cusId int
		err := rows.Scan(&scheduleJob.Id,&cusId,&scheduleJob.Complete,&datePlan,&scheduleJob.Desc)
		if err != nil {
			fmt.Println("Error scanning for job data - getWeeklySchedule")
			fmt.Println(err)
		}
		datePlanTime := parseDateFromPostgres(datePlan)
		scheduleJob.Date = makeDateHumanFriendly(datePlanTime)

		var firstName, lastName string
		getCusName := `SELECT customerfirstname, customerlastname FROM customer WHERE customerid=$1`
		err = globalDB.QueryRow(getCusName,cusId).Scan(&firstName,&lastName)
		if err != nil {
			fmt.Println("Error pulling customer's name - getWeeklySchedule")
			fmt.Println(err)
		}
		scheduleJob.CustomerName = firstName + " " + lastName

		var dayOfWeek int
		for x := 0; x < 6; x++ {
			if datesAreSame(datePlanTime,week[x]){
				dayOfWeek = x+1
				break
			}
		}

		// This speaks more to poor design code than anything, but it's crunch time.
		// I could try a map though.  Could be interesting
		if dayOfWeek == 1 {
			scheduleOut.Monday = append(scheduleOut.Monday,scheduleJob)
		}else if dayOfWeek == 2 {
			scheduleOut.Tuesday = append(scheduleOut.Tuesday,scheduleJob)
		}else if dayOfWeek == 3 {
			scheduleOut.Wednesday = append(scheduleOut.Wednesday,scheduleJob)
		}else if dayOfWeek == 4 {
			scheduleOut.Thursday = append(scheduleOut.Thursday,scheduleJob)
		}else if dayOfWeek == 5 {
			scheduleOut.Friday = append(scheduleOut.Friday,scheduleJob)
		}else{
			scheduleOut.Saturday = append(scheduleOut.Saturday,scheduleJob)
		}

	}
	json.NewEncoder(w).Encode(scheduleOut)
}



func generateBills(w http.ResponseWriter, r *http.Request){
	// This is rather involved, so I passed it to different functions so
	// I didnt have one rest function that was hundreds of lines long
	fmt.Println("Endpoint hit: Generate bills")
	billCount := makeBillsForLandscaperId("1")
	var writeOut BillReturn
	writeOut.BillCount = billCount
	json.NewEncoder(w).Encode(writeOut)
}

func postPayment(w http.ResponseWriter, r *http.Request){
	reqBody, _ := ioutil.ReadAll(r.Body)
	var postPayment PostPayment
	err := json.Unmarshal(reqBody,&postPayment)
	if err != nil {
		fmt.Println("Error unmarshalling JSON - postPayment")
		fmt.Println(err)
	}

	fmt.Println("Posting payment for customer",postPayment.CusID,"for amount = $",postPayment.PayAmount)

	year, month, day := time.Now().Date()
	todayPostgresFriendly := strconv.Itoa(year) + "-" + month.String() + "-" + strconv.Itoa(day)

	addQuery := `INSERT INTO payments (cus_id,land_id,pay_amount,pay_date,pay_type,check_num)
			VALUES ($1,$2,$3,$4,$5,$6) RETURNING payment_id`

	var paymentID int
	var payPostRes PaymentPostResponse
	if postPayment.PayType == "Cash" {
		postPayment.CheckNum = "none"
	}
	err = globalDB.QueryRow(addQuery,postPayment.CusID,"1",postPayment.PayAmount,todayPostgresFriendly,
		postPayment.PayType,postPayment.CheckNum).Scan(&payPostRes.PayID)
	if err != nil {
		fmt.Println("Error creating new payment record for customer #",postPayment.CusID)
		fmt.Println(err)
		return
		payPostRes.Response = false
	}else{
		fmt.Println("New payment record created for customer #",
			postPayment.CusID,"with payment id =",paymentID)
		payPostRes.Response = true
	}

	// Write out the response
	json.NewEncoder(w).Encode(payPostRes)


	// Then record the payment data inside the jobs table
	findJobsToRecordPaid := `SELECT date_planned, charge, current_paid, job_id FROM jobs 
			WHERE complete = true AND paid_full = false AND cus_id = $1 ORDER BY date_planned`
	rows, err := globalDB.Query(findJobsToRecordPaid,postPayment.CusID)
	if err != nil {
		fmt.Println("Error getting jobs - postPayment")
		fmt.Println(err)
	}
	amountPaid, err := strconv.Atoi(postPayment.PayAmount)
	if err != nil {
		fmt.Println("Couldn't parse amount paid to integer - postPayment")
		fmt.Println(err)
		return
	}
	var lastJobId string
	for rows.Next(){
		var charge, currentPaid int
		var dateHolder, jobId string
		rows.Scan(&dateHolder,&charge,&currentPaid,&jobId)
		due := charge - currentPaid
		if amountPaid == 0{ // If there's not more pay to record
			break
		}else if amountPaid >= due{
			amountPaid -= due
			updatePayQuery := `UPDATE jobs SET paid_full = true, current_paid = $1
						WHERE job_id = $2`
			globalDB.QueryRow(updatePayQuery,charge, jobId)
			lastJobId = jobId
		}else{ // Didn't pay enough
			fmt.Println("Didn't pay enough")
			fmt.Println("current pay on most recent job: ",currentPaid)

			updatePayQuery := `UPDATE jobs SET current_paid = $1 WHERE job_id = $2`
			if currentPaid == 0 {
				globalDB.QueryRow(updatePayQuery,amountPaid,jobId)
			}else{ // If some of the job has already been paid
				// Extremely niche case, but it's still important to consider it
				recordAmount := currentPaid + amountPaid
				globalDB.QueryRow(updatePayQuery,recordAmount,jobId)
			}
			amountPaid = 0
			break
		}
	}
	if amountPaid > 0{ // If there's a tip
		var currentPay int
		payQuery := `SELECT current_paid FROM jobs WHERE job_id = $1`
		err := globalDB.QueryRow(payQuery,lastJobId).Scan(&currentPay)
		if err != nil {
			fmt.Println("Error pulling single job for tip - postPayment")
			fmt.Println(err)
		}
		currentPay += amountPaid
		var returnPay int
		updatePayQuery := `UPDATE jobs SET current_paid = $1 WHERE job_id = $2 RETURNING current_paid`
		err = globalDB.QueryRow(updatePayQuery,currentPay,lastJobId).Scan(&returnPay)
		if err != nil {
			fmt.Println("Error updating tip pay - postPayment")
			fmt.Println(err)
		}
		if currentPay != returnPay{
			fmt.Println("Error writing tip payment out to jobs table - postPayment")
			fmt.Println(err)
		}
	}
}

func getCustomerProperties(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	customerId := vars["id"]
	fmt.Println("Endpoint hit: Customer properties for id = ",customerId)

	var properties []LocMin

	sqlQuery := `SELECT serviceid, serviceaddress FROM locations WHERE customerid=$1`
	rows, err := globalDB.Query(sqlQuery,customerId)
	if err != nil {
		fmt.Fprintf(w,"Error retreiving information")
	}
	for rows.Next(){
		var propMin LocMin
		err = rows.Scan(&propMin.Id,&propMin.Address)
		if err != nil {
			fmt.Println("Error pulling property information")
		}
		properties = append(properties,propMin)
	}

	today := time.Now()
	firstDayOfMonthPostgres := getPostresFriendlyDate(time.Date(today.Year(),today.Month(),1,0,0,0,0,time.UTC))
	lastDayInMonth := getDaysInMonth(today)
	lastDayOfMonthPostgres := strconv.Itoa(today.Year()) + "-" + strconv.Itoa(int(today.Month())) + "-" + strconv.Itoa(lastDayInMonth)

	var locCardDetails LocationArray
	jobQuery := `SELECT job_id, date_planned, complete, job_name, charge FROM jobs WHERE prop_id=$1 
				AND date_planned >= $2 AND date_planned <= $3 ORDER BY date_planned`
	for _, prop := range properties{
		var locCard LocationCard
		locCard.Address = prop.Address
		rows, err := globalDB.Query(jobQuery,prop.Id,firstDayOfMonthPostgres,lastDayOfMonthPostgres)
		if err != nil {
			fmt.Println("Couldn't pull information about jobs - getCustomerProperties")
			fmt.Println(err)
			continue
		}
		for rows.Next() {
			var datePlan, dateComp, desc string
			var jobComplete bool
			var jobId, charge int
			err := rows.Scan(&jobId,&datePlan,&jobComplete,&desc,&charge)
			if err != nil {
				fmt.Println("Error pulling data from jobs table - getCustomerProperties")
				fmt.Println(err)
			}

			var jobMin JobMin
			jobMin.Id = jobId
			jobMin.Name = desc
			jobMin.Charge = charge
			jobMin.DatePlanned = makeDateHumanFriendly(parseDateFromPostgres(datePlan))

			if jobComplete { // Only correct it if the job is complete
				// If we get this far, everything this query can't fail
				// This is necessary because if date_complete is null, then .Scan() will fail
				fetchCompleteDate := `SELECT date_complete FROM jobs WHERE job_id = $1`
				globalDB.QueryRow(fetchCompleteDate,jobId).Scan(&dateComp)
				jobMin.DateComplete = makeDateHumanFriendly(parseDateFromPostgres(dateComp))
			}else{
				jobMin.DateComplete = "Not complete"
			}
			locCard.Jobs = append(locCard.Jobs,jobMin)
		}
		locCardDetails.Details = append(locCardDetails.Details,locCard)
	}
	json.NewEncoder(w).Encode(locCardDetails)
}

func completeJob(w http.ResponseWriter, r *http.Request){
	reqBody, _ := ioutil.ReadAll(r.Body)
	var postJobComplete PostJobComplete
	json.Unmarshal(reqBody,&postJobComplete)
	fmt.Println(postJobComplete)

	completeDate, err := parseDateFromJobPost(postJobComplete.DateComplete)

	if err != nil {
		fmt.Println("Couldn't parse date from job post request")
		fmt.Println(err)
	}
	fmt.Println("Date complete ",completeDate,"for job id = ",postJobComplete.JobID)

	var postRes JobPostResponse
	postCompleteQuery := `UPDATE jobs SET date_complete = $1, complete = $2 WHERE job_id = $3 RETURNING job_id, complete`
	err = globalDB.QueryRow(postCompleteQuery,completeDate,true,postJobComplete.JobID).Scan(&postRes.JobId,&postRes.Complete)
	if err != nil {
		fmt.Println("Error scanning after post to jobs")
		fmt.Println(err)
	}

	json.NewEncoder(w).Encode(postRes)
}

func getPaymentForCustomerId(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	cus_id := vars["id"]
	fmt.Println("Endpoint hit: Getting payments for customer id = ",cus_id)
	var ret PayAmountWithId
	ret.CusID = cus_id

	payQuery := `SELECT charge, current_paid FROM jobs WHERE complete = true AND paid_full = false AND cus_id = $1`
	payRows, err := globalDB.Query(payQuery,cus_id)
	if err != nil {
		fmt.Println("Error retreiving payment info for customer id = ",cus_id)
		fmt.Println(err)
	}
	totalDue := 0
	for payRows.Next(){
		var charge, currentPaid int
		payRows.Scan(&charge,&currentPaid)
		totalDue += charge - currentPaid
	}
	ret.Due = totalDue
	json.NewEncoder(w).Encode(ret)
}

func getPaymentDueForAllCustomers(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	land_id := vars["id"]
	var ret []PaymentDetail

	customerQuery := `SELECT customerfirstname, customerlastname, customerid FROM customer WHERE landscaperid = $1`
	rows, err := globalDB.Query(customerQuery,land_id)
	if err != nil {
		fmt.Println("Error retreiving customer info - getPaymentDueForAllCustomers")
		fmt.Println(err)
	}
	for rows.Next(){
		var addPayDetail PaymentDetail
		var firstName, lastName string
		rows.Scan(&firstName,&lastName,&addPayDetail.CusID)
		addPayDetail.Name = firstName + " " + lastName

		paymentsQuery := `SELECT charge, current_paid FROM jobs WHERE complete = true AND paid_full = false AND cus_id = $1`
		payRows, err := globalDB.Query(paymentsQuery,addPayDetail.CusID)
		if err != nil {
			fmt.Println("Error retreiving payment info - getPaymentDueForAllCustomers")
			fmt.Println(err)
		}
		totalDue := 0
		for payRows.Next(){
			var charge, currentPaid int
			payRows.Scan(&charge,&currentPaid)
			totalDue += (charge - currentPaid)
		}
		addPayDetail.Due = totalDue
		ret = append(ret,addPayDetail)
	}
	json.NewEncoder(w).Encode(ret)
}

// Just used this to test the date parsing functionality of the Go time package
// Gratefully it did what I wanted it to, so my life is going to be (hopefully)
// easier now
func dateTest(w http.ResponseWriter, r *http.Request){
	query := `SELECT date_planned, date_complete FROM jobs LIMIT 5`
	rows, err := globalDB.Query(query)
	if err != nil {
		fmt.Println("Something aint right")
		fmt.Println(err)
	}
	for rows.Next(){
		var plan,com string
		rows.Scan(&plan,&com)
		fmt.Println("Planned date: ",plan)
		fmt.Println("Completed date: ",com)

		planParse, _ := time.Parse(time.RFC3339,plan)
		fmt.Println("Parsed planned date: ",planParse.String())
		fmt.Println("Postgres friendly: ",getPostresFriendlyDate(planParse))
		fmt.Println("Human friendly:",makeDateHumanFriendly(planParse))
		compParse, _ := time.Parse(time.RFC3339,com)
		fmt.Println("Parsed completed date: ",compParse.String())
		fmt.Println("Postgres friendly: ",getPostresFriendlyDate(compParse))
		fmt.Println("")
		fmt.Println("")
	}
}

func jobTest(w http.ResponseWriter, r *http.Request){
	vars := mux.Vars(r)
	customerId := vars["id"]
	query := `SELECT job_name, charge FROM jobs WHERE prop_id = $1`
	rows, err := globalDB.Query(query,customerId)
	if err != nil {
		fmt.Println("Error in job Test")
		fmt.Println(err)
	}
	for rows.Next() {
		var charge int
		var desc string
		rows.Scan(&desc,&charge)
		fmt.Println("Charge = ",charge)
		fmt.Println("Desc = ",desc)
	}

}
