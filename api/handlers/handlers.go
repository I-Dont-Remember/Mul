package handlers

import (
	"bytes"
	"encoding/json"

	"github.com/I-Dont-Remember/Mul/api/db"
	"github.com/aws/aws-lambda-go/events"
)

// mostly stolen from the hello world example :D
func Hello(req events.APIGatewayProxyRequest, db db.DB) (events.APIGatewayProxyResponse, error) {
	var buf bytes.Buffer

	body, err := json.Marshal(map[string]interface{}{
		"message": "Go Serverless v1.0! Your function executed super successfully!",
	})
	if err != nil {
		return events.APIGatewayProxyResponse{StatusCode: 404}, err
	}
	json.HTMLEscape(&buf, body)

	resp := events.APIGatewayProxyResponse{
		StatusCode:      200,
		IsBase64Encoded: false,
		Body:            buf.String(),
		Headers: map[string]string{
			"Content-Type":           "application/json",
			"X-MyCompany-Func-Reply": "hello-handler",
		},
	}

	return resp, nil
}

func FetchStats(req events.APIGatewayProxyRequest, db db.DB) (events.APIGatewayProxyResponse, error) {
	// provider route; lets them find out their current usage & junk
	return events.APIGatewayProxyResponse{}, nil
}

func RequestMulChunk(req events.APIGatewayProxyRequest, db db.DB) (events.APIGatewayProxyResponse, error) {
	// To request chunk, we need to know client who wants it and provider it should be from
	// get client and provider ids

	// empty 200 if it successfully requests it

	// if it fails, log it and send error to client
	return events.APIGatewayProxyResponse{}, nil
}
