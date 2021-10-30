package main

import "database/sql"

const(
	host = "localhost"
	port = 5432
	user = "trentevans"
	password = "password"
	dbname = "LawnCRM"
)

var globalDB *sql.DB
