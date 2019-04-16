package handlers

import (
	"fmt"
	"testing"

	"github.com/aws/aws-lambda-go/events"
)

// fun way to run multiple tests on one function is to build a
// struct that has input & expected output and can run for _, test in tests {}
func Test_Hello(t *testing.T) {
	request := events.APIGatewayProxyRequest{}

	resp, err := Hello(request)

	if err != nil {
		fmt.Println("error")
		fmt.Println(err.Error())
	} else {
		fmt.Println(resp)
	}
}
