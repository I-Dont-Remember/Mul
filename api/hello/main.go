package main

import (
	"github.com/I-Dont-Remember/Mul/api/handlers"
	"github.com/aws/aws-lambda-go/lambda"
)

func main() {
	lambda.Start(handlers.Hello)
}
