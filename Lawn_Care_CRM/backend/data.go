package main

import (
	"database/sql"
	"fmt"
	"github.com/360EntSecGroup-Skylar/excelize"
	"strconv"
	"strings"
	"time"
)

func readInMyData(fileName string, sheetName string) bool{

	psqlInfo := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		host, port, user, password, dbname)

	db, err := sql.Open("postgres",psqlInfo)
	if err != nil{
		panic(err)
		return false
	}
	defer db.Close()

	// Varify if the 2T's account exists.  If it doesn't, write it out to Postgres
	accountCheckStatement := `Select landscapername FROM landscapers WHERE companyemail='2tslawn@gmail.com'`
	var landscaperName string
	accountCheck := db.QueryRow(accountCheckStatement)
	switch err := accountCheck.Scan(&landscaperName); err{
	case sql.ErrNoRows:
		landscaperStatement := `INSERT INTO landscapers (landscapername, companyemail, companyphone, streetaddress, city, state, zip)
	VALUES ('2Ts Lawn Care','2tslawn@gmail.com','8016983089','4302 S. Albright Dr.','Holladay','UT','84124')`
		_, err = db.Exec(landscaperStatement)
		if err != nil{
			panic(err)
			return false
		}
	case nil:
		fmt.Println("\nAccount already exists\n")
	}


	// If running in VSCode
	// fileNameTotal := "code/" + fileName
	// f, err := excelize.OpenFile(fileNameTotal)
	// If running in GoLand
	f, err := excelize.OpenFile(fileName)
	if err != nil {
		panic(err)
		return false;
	}

	rows, _ := f.GetRows(sheetName)
	for _, row := range rows {
		var strArray [16]string
		strArray[0] = "No data"
		count := 0
		// Pull each row and populate a string array
		for _, colCell := range row {
			if colCell == "" || colCell == "Customer Name [0]"{
				break
			}else if colCell == "END OF CUSTOMER LIST" {
				return true
			}
			strArray[count] = colCell
			count += 1
			//fmt.Print(colCell, "\t")
		}

		// Prevent reading in the header line
		if strArray[0] == "No data"{
			continue
		}

		// Check if the customer is already in the database.  If not, create it and return the id.  If it is, return the id
		customerCheckStatement := `SELECT customerid FROM customer WHERE customeraddress=$1 AND customerzip=$2`
		var customerID int
		switch err := db.QueryRow(customerCheckStatement,strArray[5],strArray[9]).Scan(&customerID)
		err{
		case sql.ErrNoRows: // If it doesn't exist
			nameSplit := strings.Split(strArray[0]," ")
			firstName := nameSplit[0]
			lastName := nameSplit[1]
			email := strArray[6]
			address := strArray[5]
			city := strArray[7]
			state := strArray[8]
			zip := strArray[9]
			phone := strings.Replace(strArray[10],"-","",2)
			landID := 1

			createCustomer := `INSERT INTO customer (customerfirstname,customerlastname,customeremail,customeraddress,
				customercity,customerstate,customerzip,customerphone,landscaperid)
				VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING customerid`
			makeErr := db.QueryRow(createCustomer,firstName,lastName,email,address,city,state,zip,phone,landID).Scan(&customerID)
			if makeErr != nil {
				fmt.Println("Error creating account for ",strArray[0])
				panic(makeErr)
			}
			fmt.Println("New customer created ",strArray[0]," with customer id # ",customerID)

		case nil: // If it does, just write it out to the command line
			fmt.Println(strArray[0]," 's account at ",strArray[5]," in zip code ",strArray[9]," already exists with id # ",customerID)
		}

		serviceCheckStatement := `SELECT serviceid FROM locations WHERE serviceaddress=$1 AND servicezip=$2`
		var serviceID int
		switch err := db.QueryRow(serviceCheckStatement,strArray[1],strArray[4]).Scan(&serviceID)
		err{
		case sql.ErrNoRows:

			createLocation := `INSERT INTO locations (serviceaddress,servicecity,servicestate,servicezip,daysbetween,preferredday,costpermow,costperaerate,cutheight,landscaperid,customerid)
								VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11) RETURNING serviceid`
			makeErr := db.QueryRow(createLocation,strArray[1],strArray[2],strArray[3],strArray[4],strArray[12],strArray[11],strArray[14],strArray[13],strArray[15],1,customerID).Scan(&serviceID)
			if makeErr != nil{
				panic(makeErr)
			}
			fmt.Println("New service location ",strArray[1]," in zip ",strArray[4]," created with service id # ",serviceID)

		case nil:
			fmt.Println("Service location ",strArray[1]," in zip ",strArray[4]," already exists.  Service ID: ",serviceID)

		}

	}
	return true
}

func generateInitialMows(landID string){

	days := [7]string{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"}
	today := time.Now()
	firstDays := getFirstDaysOfMonth(today.Month())

	sqlPullLocs := `SELECT serviceid, customerid, daysbetween, 
		preferredday, costpermow FROM locations WHERE landscaperid=$1`

	rows, err := globalDB.Query(sqlPullLocs,landID)
	if err != nil {
		fmt.Println("Error retreiving information - GenerateFutureMows")
		fmt.Println(err)
	}

	for rows.Next() {
		var loc Location
		err = rows.Scan(&loc.Id,&loc.CusID,&loc.DaysBetween,
			&loc.PreferredDay,&loc.CostPerMow)
		if err != nil {
			fmt.Println("Issue scanning customer info: Generate Future Mows")
			fmt.Println(err)
		}

		var dayOne int
		for x := 0; x < 7; x++{
			if loc.PreferredDay == days[x]{
				dayOne = firstDays[x]
			}
		}

		firstMow := time.Date(2020,11,dayOne,12,0,0,0,time.UTC)
		decemberEnd := time.Date(2020,12,31,1,0,0,0,time.UTC)
		mowBefore2021 := true
		mowCount := 0
		// Generate mows until the end of December
		for mowBefore2021{
			var complete bool
			daysToNextMow := loc.DaysBetween * mowCount
			nextMow := firstMow.AddDate(0,0,daysToNextMow)
			if decemberEnd.Sub(nextMow) < 0 {
				mowBefore2021 = false // Not needed, but we'll leave it for now
				break
			}

			if today.Sub(nextMow) < 0 {
				complete = false
			}else{
				complete = true
			}

			// Formatting the date because Go's support for date formatting is not terribly clear
			// so that way we get the right date to go into Postgres
			year, month, day := nextMow.Date()
			yearString := strconv.Itoa(year)
			var monthString string
			if int(month) < 10 {
				monthString = "0" + strconv.Itoa(int(month))
			}else{
				monthString = strconv.Itoa(int(month))
			}
			var dayString string
			if day < 10 {
				dayString = "0" + strconv.Itoa(day)
			}else{
				dayString = strconv.Itoa(day)
			}

			fullDate := yearString + "-" + monthString + "-" + dayString
			//fmt.Println("Formated date =",fullDate) // Gives a Postgres friendly date

			jobDoesntExistYet := false

			jobCheck := `SELECT job_id FROM jobs WHERE date_planned = $1 AND job_name = 'Mow' AND prop_id = $2`
			jobId := 0
			switch checkErr := globalDB.QueryRow(jobCheck,fullDate,loc.Id).Scan(&jobId)
				checkErr{
			case sql.ErrNoRows:
				jobDoesntExistYet = true
			case nil:
				fmt.Println("Mow (id =",jobId,") at property id",loc.Id,"on date",fullDate,"exists")
			}

			if complete && jobDoesntExistYet{

				newJob := `INSERT INTO jobs (cus_id, prop_id, land_id, date_planned, date_complete, complete, charge)
			VALUES ($1,$2,$3,$4,$5,$6,$7) RETURNING job_id`
				makeErr := globalDB.QueryRow(newJob,loc.CusID,loc.Id,landID,fullDate,fullDate,complete,loc.CostPerMow).Scan(&jobId)
				if makeErr != nil {
					fmt.Println("Error creating job at location ",loc.Id)
					fmt.Println("Job on date: ",fullDate)
					panic(makeErr)
				}
				fmt.Println("Mow (Job ID =",jobId,") at property ",loc.Id,"created on",fullDate)
			}else if jobDoesntExistYet{
				newJob := `INSERT INTO jobs (cus_id, prop_id, land_id, date_planned, complete, charge)
			VALUES ($1,$2,$3,$4,$5,$6) RETURNING job_id`
				makeErr := globalDB.QueryRow(newJob,loc.CusID,loc.Id,landID,fullDate,complete,loc.CostPerMow).Scan(&jobId)
				if makeErr != nil {
					fmt.Println("Error creating job at location ",loc.Id)
					fmt.Println("Job on date: ",fullDate)
					panic(makeErr)
				}
				fmt.Println("Mow (Job ID =",jobId,") at property ",loc.Id,"created on",fullDate)
			}
			mowCount++
		}
	}
}

func makeFakeCustomerJobs(){
	queryForPALocs := `SELECT serviceid, customerid, costpermow FROM locations WHERE servicestate='PA'`
	rows, err := globalDB.Query(queryForPALocs)
	if err != nil {
		fmt.Println("Couldn't get the PA locations")
		fmt.Println(err)
	}
	firstService := time.Date(2020,12,8,0,0,0,0,time.UTC)
	for rows.Next(){
		var serviceid, customerid, costpermow int
		err = rows.Scan(&serviceid, &customerid, &costpermow)
		if err != nil {
			fmt.Println("Couldn't scan for values for PA locations")
			fmt.Println(err)
		}

		makeJobsCompleteQuery := `INSERT INTO jobs (cus_id, prop_id, land_id, date_planned, date_complete, complete, job_name, charge, paid_full, current_paid)
						VALUES ($1,$2,$3,$4,$5,true,'Mow',$6,true,$7) RETURNING job_id`

		makeJobQuery := `INSERT INTO jobs (cus_id, prop_id, land_id, date_planned, job_name, charge, paid_full, current_paid)
						VALUES ($1,$2,$3,$4,'Mow',$5,false,$6) RETURNING job_id`

		for x :=0; x < 5; x++ {
			serviceDate := firstService.AddDate(0,0,7*x)
			var id int
			if  serviceDate.Day() < 11 {
				err = globalDB.QueryRow(makeJobsCompleteQuery,customerid,serviceid,1,"2020-12-8","2020-12-8",costpermow,costpermow).Scan(&id)
				if err != nil {
					fmt.Println("Couldn't make the job")
					fmt.Println(err)
				}else{
					fmt.Println("Job created with id = ",id)
				}
			}else{
				date := getPostresFriendlyDate(serviceDate)
				err = globalDB.QueryRow(makeJobQuery,customerid,serviceid,1,date,costpermow,0).Scan(&id)
				if err != nil {
					fmt.Println("Couldn't make job")
					fmt.Println(err)
				}else{
					fmt.Println("Job created with id = ",id)
				}

			}
		}


	}
}

