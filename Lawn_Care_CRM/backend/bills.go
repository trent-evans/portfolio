package main

import (
	"fmt"
	"github.com/jung-kurt/gofpdf"
	"os"
	"strconv"
	"time"
)

func makeBillsForLandscaperId(land_id string) int{

	changedDirectories := false
	year, month, _ := time.Now().Date()
	dir := makeAndReturnSubdirectoryForBills(strconv.Itoa(year),month.String())
	err := os.Chdir(dir)
	if err != nil {
		fmt.Println("Couldn't change directories to ",dir)
		fmt.Println(err)
	}else{
		changedDirectories = true
	}

	billCount := 0

	var landscaper Landscaper
	landscaper.Id = land_id
	queryLandInfo := `SELECT landscapername, companyemail, companyphone, streetaddress, city, state, zip
			FROM landscapers WHERE landscaperid=$1`
	row := globalDB.QueryRow(queryLandInfo,land_id)
	err = row.Scan(&landscaper.Name,&landscaper.Email,&landscaper.Phone,&landscaper.Address,
		&landscaper.City,&landscaper.State,&landscaper.Zip)
	if err != nil{
		fmt.Println("Error retrieving landscaper info - makeBills for LandscaperID")
		fmt.Println(err)
	}

	sqlQuery := `SELECT serviceid, customerid, serviceaddress, servicecity, servicestate,
				servicezip, costpermow, costperaerate FROM locations WHERE landscaperid=$1`
	rows, err := globalDB.Query(sqlQuery,land_id)
	if err != nil {
		fmt.Println("Error retrieving location information - makeBillsForLandscaperID")
		fmt.Println(err)
	}
	for rows.Next(){
		var loc Location
		loc.LandID = land_id
		err = rows.Scan(&loc.Id,&loc.CusID,&loc.Address,&loc.City,&loc.State,&loc.Zip,&loc.CostPerMow,&loc.CostPerAerate)
		if err != nil {
			fmt.Println("Issue scanning for location info - makeBillsForLandscaperID")
			fmt.Println(err)
		}
		generateBillForLocId(loc,landscaper)
		//generateBillForLocationID_GROSS(loc, landscaper)
		billCount += 1
	}

	if changedDirectories {
		os.Chdir("../..") // Return back up
	}
	return billCount
}

func generateBillForLocId(loc Location, landscaper Landscaper){

	pdf := gofpdf.New("L","mm","Letter","")
	pdf.AddPage()

	pdf = addCompanyHeader(pdf,landscaper)

	year, month, day := time.Now().Date()

	var customer Customer
	pullCustomer := `SELECT customerfirstname, customerlastname, customeraddress, customeremail, preferredbilltype
				FROM customer WHERE customerid=$1`
	row := globalDB.QueryRow(pullCustomer,loc.CusID)
	err := row.Scan(&customer.FirstName,&customer.LastName,&customer.Address,&customer.Email,&customer.BillType)
	if err != nil {
		fmt.Println("Error scanning for customer info - generate bill for location ID")
		fmt.Println(err)
	}

	pdf = addCustomerHeader(pdf,customer,loc,strconv.Itoa(year),month.String())
	pdf = addTable(pdf,loc,strconv.Itoa(year),month.String(),strconv.Itoa(day))

	// Have to include the loc.Id otherwise we'll overwrite bills for people with multiple properties
	filename := customer.LastName + "_" + customer.FirstName + "_" + loc.Id +
		"_" + month.String() + "_" + strconv.Itoa(year) + ".pdf"
	pdf.OutputFileAndClose(filename)

}

func makeAndReturnSubdirectoryForBills(year string, month string) string {
	billYearDir := "bills_" + year
	err := os.MkdirAll(billYearDir,0755)
	if err != nil {
		fmt.Println("Error creating directory " + billYearDir)
		fmt.Println(err)
	}
	billMonthDir := billYearDir + "/" + month
	err = os.MkdirAll(billMonthDir,0755)
	if err != nil {
		fmt.Println("Error creating directory " + billMonthDir)
		fmt.Println(err)
	}
	return billMonthDir
}

func addCompanyHeader(pdf *gofpdf.Fpdf, landscaper Landscaper) *gofpdf.Fpdf{
	pdf.SetFont("Times","B",12)
	pdf.Cell(100,5,landscaper.Name)
	pdf.SetFont("Times","",12)
	pdf.Ln(-1)
	pdf.Cell(100,5,landscaper.Address)
	pdf.Ln(-1)
	subAddressLine := landscaper.City + ", " + landscaper.State + " " + landscaper.Zip
	pdf.Cell(100,5,subAddressLine)
	pdf.Ln(-1)
	pdf.Cell(100,5,"Phone Number: " + landscaper.Phone)
	pdf.Ln(-1)
	pdf.Cell(100,5,"Email: " + landscaper.Email)
	pdf.Ln(12)
	return pdf
}

func addCustomerHeader(pdf *gofpdf.Fpdf, customer Customer, loc Location, year string, month string) *gofpdf.Fpdf{
	pdf.SetFont("Times","",12)
	customerFullName:= customer.FirstName + " " + customer.LastName
	pdf.Cell(100,5,customerFullName)
	pdf.Ln(-1)
	pdf.Cell(100,5,customer.Address)
	pdf.Ln(-1)
	pdf.Cell(100,5,"RE:")
	pdf.Ln(-1)
	pdf.Cell(100,5,loc.Address)
	pdf.Ln(-1)
	billTitle := month + " " + year + " Bill"
	pdf.Cell(100,5,billTitle)
	pdf.Ln(12)
	return pdf
}

func addTable(pdf *gofpdf.Fpdf, loc Location, year string, month string, day string) *gofpdf.Fpdf {

	pdf = addTableHeader(pdf)

	pdf.SetFont("Times","",12)
	postgresToday := year + "-" + month + "-" + day
	getJobsQuery := `SELECT date_planned, date_complete, job_name, charge, current_paid
			FROM jobs WHERE date_complete < $1 AND paid_full = false AND prop_id = $2`
	rows, err := globalDB.Query(getJobsQuery,postgresToday,loc.Id)
	if err != nil {
		fmt.Println("Error retrieving job information - generateBillForLocationID")
		fmt.Println(err)
	}
	subtotal := 0
	for rows.Next() {
		var jobDeets JobMin
		rows.Scan(&jobDeets.DatePlanned,&jobDeets.DateComplete,&jobDeets.Name,&jobDeets.Charge,&jobDeets.Paid)

		var description string
		if jobDeets.Name == "Mow" {
			description = "Lawn Service"
		}
		if jobDeets.DatePlanned != jobDeets.DateComplete{ // Account for rescheduled services
			description += " - Rescheduled"
		}
		due := jobDeets.Charge - jobDeets.Paid
		subtotal += due

		colData := [4]string{jobDeets.DateComplete,description,strconv.Itoa(due),strconv.Itoa(subtotal)}
		for idx, data := range colData {
			if idx == 0{
				data = data[0:10]
			} else if idx == 2 || idx == 3{
				data = "$" + data
			}
			pdf.CellFormat(40, 7, data, "1", 0, "", false, 0, "")
		}
		pdf.Ln(-1)
	}
	pdf = addTableTotal(pdf,subtotal)

	return pdf
}

func addTableHeader(pdf *gofpdf.Fpdf) *gofpdf.Fpdf{
	pdf.SetFont("Times","B",12)
	pdf.SetFillColor(240,240,240)
	colHeads := [4]string{"Date","Service","Cost","Subtotal"}
	for _, head := range colHeads {
		pdf.CellFormat(40, 7, head, "1", 0, "", true, 0, "")
	}
	pdf.Ln(-1)
	return pdf
}

func addTableTotal(pdf *gofpdf.Fpdf,totalDue int) *gofpdf.Fpdf{
	pdf.SetFont("Times","B",12)
	pdf.CellFormat(120, 7, "Grand Total", "1", 0, "R", false, 0, "")
	pdf.CellFormat(40, 7, "$" + strconv.Itoa(totalDue), "1", 0, "", false, 0, "")
	return pdf
}
