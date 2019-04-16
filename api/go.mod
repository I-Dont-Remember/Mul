module github.com/I-Dont-Remember/Mul/api

replace github.com/Mul/handlers => ./handlers

require (
	github.com/aws/aws-lambda-go v1.6.0
	github.com/labstack/echo v3.3.10+incompatible
	github.com/labstack/echo/v4 v4.0.0 // indirect
)
