package db

import (
	"os"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
)

// trying to use dependency injection, it worked for my API but seems flawed in that if your interface has a lot of methods,
// then anything you want to inherit from has to implement every one of those even if you only need one.  Maybe I did it wrong
// and am supposed to do something where interface only has some connection stuff and just gets a struct reference that can
// have whatever methods attached.  IDK.
type DB interface {
	addMulChunk() error
}

// Dynamo implements DB
type Dynamo struct {
	conn  *dynamodb.DynamoDB
	Table string
}

// Connect returns a DynamoDB connection; local or remote
func Connect() (DB, error) {
	region := "us-east-2"
	localEndpoint := "http://localhost:4569/"
	env := os.Getenv("API_ENV")

	if env != "local" && env != "prod" && env != "dev" {
		// TODO: probably trying to run a test, we should probably pull in and clean up code for passing Mockdb to tests
		return Dynamo{}, nil
	}

	d := &Dynamo{}
	if env == "prod" || env == "local" {
		d.Table = "TableName"
	}

	sess, err := session.NewSession(&aws.Config{Region: aws.String(region)})
	if env == "local" {
		sess, err = session.NewSession(
			&aws.Config{
				Region:   aws.String(region),
				Endpoint: aws.String(localEndpoint),
			})
	}

	if err != nil {
		return nil, err
	}

	d.conn = dynamodb.New(sess)
	return d, nil
}

func (db Dynamo) addMulChunk() error {
	return nil
}
