package main

import (
	"database/sql"
	"fmt"
	"github.com/pkg/errors"
	"os"
	"strconv"
	"strings"
	"time"
)

func checkDBConnection() bool{
	psqlInfo := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		host, port, user, password, dbname)
	var err error
	db, err := sql.Open("postgres",psqlInfo)
	if err != nil{
		panic(err)
		return false
	}

	err = db.Ping()
	if err != nil{
		panic(err)
		return false
	}

	// Assign the database pointer to a global database variable
	globalDB = db
	return true
}

func getWorkingDirectory() string{
	//Figure out what directory we're in
	dir, err := os.Getwd()
	if err != nil {
		return "err"
	}
	return dir
}

func getFirstDaysOfMonth(month time.Month) [7]int{
	ret := [7]int{0,0,0,0,0,0,0}
	today := time.Now()
	firstDay := time.Date(today.Year(),month,1,12,0,0,0,time.UTC)
	for x := 0; x < 7; x++{
		ret[(int(firstDay.Weekday()) + x) % 7] = x+1
	}
	return ret
}

func getPostresFriendlyDate(date time.Time) string{
	return strconv.Itoa(date.Year()) + "-" + strconv.Itoa(int(date.Month())) + "-" + strconv.Itoa(date.Day())
}

func makeDateHumanFriendly(date time.Time) string{
	return strconv.Itoa(int(date.Month())) + "/" + strconv.Itoa(date.Day()) + "/" + strconv.Itoa(date.Year())
}

func parseDateFromPostgres(date string) time.Time{
	ret,_ := time.Parse(time.RFC3339,date)
	return ret
}

func datesAreSame(a time.Time, b time.Time) bool{
	return (a.Year() == b.Year()) && (a.Month() == b.Month()) && (a.Day() == b.Day())
}

func getDaysInMonth(date time.Time) int {
	// Determine how many days are in the month
	// Is it unnecessary?  Probably.  But, here we are
	// It's in the moment that I have to be impressed with Go's time library
	// because it will roll over to the next month if it has too many days
	// So I can't just put in 31 for all of them
	var ret int
	if date.Month() == 1 || date.Month() == 3 || date.Month() == 5 || date.Month() == 7 ||
		date.Month() == 8 || date.Month() == 10 || date.Month() == 12 {
		ret = 31
	}else if date.Month() == 4 || date.Month() == 6 || date.Month() == 9 || date.Month() == 11{
		ret = 30
	}else{ // February
		if date.Year() % 4 == 0{
			ret = 29
		}else{
			ret = 28
		}
	}
	return ret
}

func parseDateFromJobPost(date string) (string, error) {
	datePieces := strings.Split(date,"/")
	if len(datePieces) != 3 {
		return "na", errors.New("Wrong date input style")
	}
	// This isn't terribly defensive programming right now, and I recognize that.
	// I need to improve this in the future
	month := datePieces[0]
	day := datePieces[1]
	year := datePieces[2]
	ret := year + "-" + month + "-" + day

	return ret, nil
}
