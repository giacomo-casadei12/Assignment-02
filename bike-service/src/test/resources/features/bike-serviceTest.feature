Feature: Microservice API Endpoints

  Scenario: Get all bikes in the app
    Given the bike microservice is up and running
    When I send a GET request to "/api/ebike/query" specifying the parameters for getting all bikes
    Then the response body should contain a list containing all bikes registered in the app

  Scenario: Get nearby bikes
    Given the bike microservice is up and running
    And there is a bike near the 0 0 position
    When I send a GET request to "/api/ebike/query" specifying the parameters for getting all bikes near the 0 0 position
    Then the response body should contain a list containing all bikes near that position

  Scenario: Recharge a bike
    Given the bike microservice is up and running
    And there is an OUT OF CHARGE bike
    When I send a POST request to "/api/ebike/command" specifying the parameters for recharging that bike
    Then the response body should contain whether the operation was successfully or not

  Scenario: Add a bike
    Given the bike microservice is up and running
    When I send a POST request to "/api/ebike/command" specifying the parameters for adding a new bike
    Then the response body should contain whether the operation was successfully or not
