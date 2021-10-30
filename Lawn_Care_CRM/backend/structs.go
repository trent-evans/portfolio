package main

type Landscaper struct{
	Id string `json:"land_id"`
	Name string `json:"land_name"`
	Email string `json:"land_email"`
	Phone string `json:"land_phone"`
	Address string `json:"land_address"`
	City string `json:"land_city"`
	State string `json:"land_state"`
	Zip string `json:"land_zip"`
}

type Customer struct{
	Id string `json:"cus_id"`
	FirstName string `json:"cus_first_name"`
	LastName string `json:"cus_last_name"`
	Email string `json:"cus_email"`
	Phone string `json:"cus_phone"`
	Address string `json:"cus_address"`
	City string `json:"cus_city"`
	State string `json:"cus_state"`
	Zip string `json:"cus_zip"`
	LandId string `json:"land_id"`
	BillType string `json:"bill_type"`
}

type CustomerMin struct {
	Id string `json:"cus_id"`
	FirstName string `json:"cus_first_name"`
	LastName string `json:"cus_last_name"`
}

type Location struct{
	Id string `json:"loc_id"`
	Address string `json:"loc_address"`
	City string `json:"loc_city"`
	State string `json:"loc_state"`
	Zip string `json:"loc_zip"`
	CusID string `json:"cus_id"`
	LandID string `json:"land_id"`
	DaysBetween int `json:"days_between"`
	PreferredDay string `json:"preferred_day"`
	CostPerMow int `json:"cost_per_mow"`
	CostPerAerate int `json:"cost_per_aerate"`
	CutHeight float32 `json:"cut_height"`
}

type LocationCard struct{
	Address string `json:"address"`
	Jobs []JobMin `json:"jobs"`
}

type LocId struct {
	Id string `json:"id"`
}

type LocationArray struct {
	Details []LocationCard `json:"details"`
}

type LocMin struct{
	Id string `json:"loc_id"`
	Address string `json:"address"`
}

type Job struct{
	Id string `json:"job_id"`
	CusId string `json:"cus_id"`
	PropId string `json:"prop_id"`
	LandId string `json:"land_id"`
	DatePlanned string `json:"date_planned"`
	DateComplete string `json:"date_complete"`
	Complete bool `json:"complete"`
	JobName string `json:"job_name"`
	Charge int `json:"charge"`
	PaidInFull bool `json:"paid_in_full"`
	CurrentPaid int `json:"current_paid"`
}

type JobMin struct{
	Id int `json:"id"`
	DatePlanned string `json:"date_planned"`
	DateComplete string `json:"date_complete"`
	Name string `json:"name"`
	Charge int `json:"charge"`
	Paid int `json:"paid"`
}

type BillReturn struct {
	BillCount int `json:"bill_count"`
}

type ScheduleJobs struct{
	Id string `json:"job_id"`
	Desc string `json:"job_desc"`
	CustomerName string `json:"customer_name"`
	Complete bool `json:"complete"`
	Date string `json:"date"`
}

type WeeklySchedule struct {
	DateFrom string `json:"date_from"`
	DateTo string `json:"date_to"`
	Monday []ScheduleJobs `json:"monday"`
	Tuesday []ScheduleJobs `json:"tuesday"`
	Wednesday []ScheduleJobs `json:"wednesday"`
	Thursday []ScheduleJobs `json:"thursday"`
	Friday []ScheduleJobs `json:"friday"`
	Saturday []ScheduleJobs `json:"saturday"`
}

type PaymentDetail struct {
	CusID string `json:"cus_id"`
	Name string `json:"name"`
	Due int `json:"due"`
}

type PayAmountWithId struct {
	CusID string `json:"cus_id"`
	Due int `json:"due"`
}

type PostPayment struct{
	CusID string `json:"cus_id"`
	PayAmount string `json:"pay_amount"`
	PayType string `json:"pay_type"`
	CheckNum string `json:"check_num"`
}

type PaymentPostResponse struct{
	PayID string `json:"pay_id"`
	Response bool `json:"response"`
	Remainder int `json:"remainder"`
}

type PostJobComplete struct{
	JobID string `json:"job_id"`
	DateComplete string `json:"date_complete"`
}

type CustomerPaymentDetails struct {
	CusId string `json:"cus_id"`
	CustomerName string `json:"customer_name"`
	PaymentDue int `json:"payment_due"`
}

type JobPostResponse struct {
	JobId string `json:"job_id"`
	Complete bool `json:"complete"`
}

