package main

import (
	"fmt"
	"io/ioutil"
	"strings"

	"github.com/I-Dont-Remember/Mul/api/handlers"

	"github.com/labstack/echo"
	"github.com/labstack/echo/middleware"

	"github.com/aws/aws-lambda-go/events"
)

// Massage the echo framework request/response to match our AWS Lambda handlers
func adjust(fn func(events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error)) func(echo.Context) error {
	return func(c echo.Context) error {

		// TODO: validate that the string joining nonsense we're doing is actually working correctly
		headers := map[string]string{}
		for k, v := range c.Request().Header {
			headers[k] = strings.Join(v[:], ",")
		}

		paramNames := c.ParamNames()
		paramMap := map[string]string{}
		for _, name := range paramNames {
			paramMap[name] = c.Param(name)
		}

		queryMap := map[string][]string{}
		queryParams := map[string]string{}
		queryMap = c.QueryParams()
		for k, v := range queryMap {
			queryParams[k] = strings.Join(v[:], ",")
		}

		body, err := ioutil.ReadAll(c.Request().Body)
		if err != nil {
			panic(err)
		}

		request := events.APIGatewayProxyRequest{
			HTTPMethod:            c.Request().Method,
			Headers:               headers,
			PathParameters:        paramMap,
			QueryStringParameters: queryParams,
			Body: string(body),
		}
		proxyResponse, _ := fn(request)

		if proxyResponse.StatusCode > 300 {
			fmt.Println("   [!] ", proxyResponse.Body)
		}

		// TODO: check this is actually doing what we thing it is
		for k, v := range proxyResponse.Headers {
			c.Response().Header().Set(k, v)
		}
		return c.JSONBlob(proxyResponse.StatusCode, []byte(proxyResponse.Body))
	}
}

func main() {
	port := ":4500"

	e := echo.New()

	// To see specific header, use ${header:foo} which will show foo's value
	// same for seeing cookie, query, and form
	e.Use(middleware.LoggerWithConfig(middleware.LoggerConfig{
		Format: "${method} ${uri} status:${status} latency:${latency_human} out:${bytes_out} bytes \n",
	}))

	e.Use(middleware.CORS())

	e.GET("/hello", adjust(handlers.Hello))

	e.Logger.Fatal(e.Start(port))
}
